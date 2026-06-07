package io.github.nahuel92.pit4u.configuration;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.jarRepository.JarRepositoryManager;
import com.intellij.jarRepository.RemoteRepositoriesConfiguration;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import io.github.nahuel92.pit4u.gui.PIT4USettingsEditor;
import io.github.nahuel92.pit4u.highlighter.Mutations;
import io.github.nahuel92.pit4u.highlighter.PitUIPainter;
import io.github.nahuel92.pit4u.runner.PitMutationDataService;
import org.apache.commons.lang3.exception.UncheckedException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class PIT4URunConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule, PIT4URunConfiguration>
        implements Disposable {
    private static final Logger log = Logger.getInstance(PIT4URunConfiguration.class);
    private PIT4UEditorStatus pit4UEditorStatus = new PIT4UEditorStatus();

    protected PIT4URunConfiguration(final String name, final Project project, final ConfigurationFactory factory) {
        super(name, new JavaRunConfigurationModule(project, true), factory);
    }

    @Override
    @NotNull
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new PIT4USettingsEditor(getProject(), pit4UEditorStatus);
    }

    @Override
    public Collection<Module> getValidModules() {
        return List.of(ModuleManager.getInstance(getProject()).getModules());
    }

    @Override
    @Nullable
    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment environment) {
        final var javaCommandLineState = new JavaCommandLineState(environment) {
            private ConsoleView consoleView;

            private String resolveLauncherPathSynchronously() {
                final var project = getEnvironment().getProject();
                final var module = getConfigurationModule().getModule();
                if (module == null) {
                    return null;
                }

                // 1. Sniff out the version safely (this part requires a Read Action, which we are currently in)
                String detectedVersion = ApplicationManager.getApplication().runReadAction(
                        (Computable<String>) () -> detectJUnitPlatformVersion(module)
                );
                if (detectedVersion == null) return null;

                // 2. We must jump OUT of the read action to download.
                // We can use a synchronous background task that IntelliJ handles natively without deadlocking.
                final String[] result = new String[1];

                try {
                    return getOrDownloadMatchingLauncherAsync(project, detectedVersion)
                            .get(5, TimeUnit.SECONDS); // Hard timeout safety barrier so the IDE never freezes
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    // Fallback silently to your plugin's bundled jar if anything times out or fails
                    return JavaParametersCreator.JPL_PATH.toString();
                }
            }

            private String detectJUnitPlatformVersion(Module module) {
                // Regex to match "junit-platform-engine-1.x.x.jar" and capture the version
                final var pattern = Pattern.compile("junit-platform-engine-(1\\.\\d+\\.\\d+)\\.jar");

                // Extract all class/library roots compiled into this specific module
                final var files = OrderEnumerator.orderEntries(module)
                        .recursively()
                        .exportedOnly()
                        .classes()
                        .getRoots();

                for (VirtualFile file : files) {
                    final var name = file.getName();
                    final var matcher = pattern.matcher(name);
                    if (matcher.find()) {
                        // Found it! Return the captured version group (e.g., "1.10.2")
                        return matcher.group(1);
                    }
                }
                // Return null if the project isn't using JUnit 5 platform engines
                return null;
            }

            private CompletableFuture<String> getOrDownloadMatchingLauncherAsync(Project project, String version) {
                CompletableFuture<String> future = new CompletableFuture<>();

                final var props = new RepositoryLibraryProperties(
                        "org.junit.platform", "junit-platform-launcher", version
                );

                var repos = RemoteRepositoriesConfiguration.getInstance(project).getRepositories();

                // FIX: Using loadDependencies instead of loadDependenciesModal bypassing the UI layer entirely
                JarRepositoryManager.loadDependenciesAsync(project, props, false, false, repos, null)
                        .onSuccess(resolvedRoots -> {
                            if (resolvedRoots != null && !resolvedRoots.isEmpty()) {
                                // Return the local file system absolute path
                                String cleanPath = PathUtil.toPresentableUrl(resolvedRoots.getFirst().getFile().getUrl());
                                future.complete(cleanPath);
                            } else {
                                future.complete(JavaParametersCreator.JPL_PATH.toString());
                            }
                        })
                        .onError(error -> {
                            future.complete(JavaParametersCreator.JPL_PATH.toString());
                        });

                return future;
            }

            @Override
            protected JavaParameters createJavaParameters() {
                String alignedPath = resolveLauncherPathSynchronously();
                return JavaParametersCreator.create(getConfigurationModule(), getProject(), pit4UEditorStatus, alignedPath);
            }

            @Override
            @NotNull
            protected OSProcessHandler startProcess() throws com.intellij.execution.ExecutionException {
                final var osProcessHandler = super.startProcess();
                final var reportIndexPath = Path.of(pit4UEditorStatus.getReportDir())
                        .resolve("index.html")
                        .toAbsolutePath();

                osProcessHandler.addProcessListener(
                        new ProcessListener() {
                            @Override
                            public void processTerminated(@NotNull final ProcessEvent event) {
                                if (!Files.exists(reportIndexPath)) {
                                    consoleView.print(
                                            "Pitest execution failed. Please check the output above for more information",
                                            ConsoleViewContentType.ERROR_OUTPUT
                                    );
                                    return;
                                }
                                final var path = Path.of(pit4UEditorStatus.getReportDir())
                                        .resolve("mutations.xml")
                                        .toAbsolutePath();

                                if (!Files.exists(path)) {
                                    return;
                                }

                                final var xmlMapper = new XmlMapper();
                                final Mutations results;
                                try {
                                    results = xmlMapper.readValue(path.toFile(), Mutations.class);
                                } catch (final IOException e) {
                                    throw new UncheckedException(e);
                                }

                                ApplicationManager.getApplication().invokeLater(() -> {
                                    // 1. Save data into the project scope
                                    PitMutationDataService.getInstance(getProject()).loadData(results.mutations());

                                    // 2. Paint any editor currently open right now
                                    var fileEditorManager = FileEditorManager.getInstance(getProject());
                                    for (var editorWrapper : fileEditorManager.getAllEditors()) {
                                        if (editorWrapper instanceof TextEditor textEditor) {
                                            var psiFile = PsiManager.getInstance(getProject()).findFile(editorWrapper.getFile());
                                            if (psiFile != null) {
                                                PitUIPainter.paintEditor(textEditor.getEditor(), psiFile);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                );
                return osProcessHandler;
            }

            @Override
            @NotNull
            public ExecutionResult execute(@NotNull final Executor executor,
                                           @NotNull final ProgramRunner<?> runner) throws com.intellij.execution.ExecutionException {
                final var processHandler = startProcess();
                final var console = createConsole(executor);
                if (console != null) {
                    console.attachToProcess(processHandler);
                }
                this.consoleView = console;
                return new DefaultExecutionResult(
                        console,
                        processHandler,
                        createActions(console, processHandler, executor)
                );
            }
        };

        javaCommandLineState.setConsoleBuilder(
                TextConsoleBuilderFactory.getInstance().createBuilder(getProject())
        );
        return javaCommandLineState;
    }

    @Override
    public void readExternal(@NotNull final Element element) throws InvalidDataException {
        super.readExternal(element);
        PIT4UConfigurationStore.readExternal(pit4UEditorStatus, element);
    }

    @Override
    public void writeExternal(@NotNull final Element element) throws WriteExternalException {
        super.writeExternal(element);
        PIT4UConfigurationStore.writeExternal(pit4UEditorStatus, element);
    }

    @Override
    public void dispose() {
        log.info("PIT4URunConfiguration Disposed");
    }

    public void setPit4UEditorStatus(final PIT4UEditorStatus pit4UEditorStatus) {
        this.pit4UEditorStatus = pit4UEditorStatus;
    }
}