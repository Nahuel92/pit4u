package io.github.nahuel92.pit4u.configuration;

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
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import io.github.nahuel92.pit4u.gui.PIT4USettingsEditor;
import io.github.nahuel92.pit4u.highlighter.MutationDataService;
import io.github.nahuel92.pit4u.highlighter.UIPainter;
import io.github.nahuel92.pit4u.highlighter.XMLDataParser;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public final class PIT4URunConfiguration
        extends ModuleBasedConfiguration<JavaRunConfigurationModule, PIT4URunConfiguration>
        implements Disposable {
    private static final Logger LOG = Logger.getInstance(PIT4URunConfiguration.class);
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("junit-platform-engine-(1\\.\\d+\\.\\d+)\\.jar");
    private PIT4UEditorStatus pit4UEditorStatus = new PIT4UEditorStatus();

    PIT4URunConfiguration(final String name, final Project project, final ConfigurationFactory factory) {
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
    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment environment) {
        final var javaCommandLineState = new JavaCommandLineState(environment) {
            private ConsoleView consoleView;

            @Override
            protected JavaParameters createJavaParameters() {
                final var alignedPath = resolveLauncherPathSynchronously(getEnvironment());
                return JavaParametersCreator.create(
                        getConfigurationModule(),
                        getProject(),
                        pit4UEditorStatus,
                        alignedPath
                );
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

                                final var results = XMLDataParser.parse(path);
                                ApplicationManager.getApplication().invokeLater(() -> {
                                            MutationDataService.getInstance(getProject()).loadData(results.mutations());
                                            final var fileEditorManager = FileEditorManager.getInstance(getProject());
                                            for (final var editorWrapper : fileEditorManager.getAllEditors()) {
                                                if (editorWrapper instanceof TextEditor textEditor) {
                                                    var psiFile = PsiManager.getInstance(getProject()).findFile(editorWrapper.getFile());
                                                    if (psiFile != null) {
                                                        UIPainter.paintEditor(textEditor.getEditor(), psiFile);
                                                    }
                                                }
                                            }
                                        }
                                );
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
        LOG.info("PIT4URunConfiguration Disposed");
    }

    public PIT4UEditorStatus getPit4UEditorStatus() {
        return pit4UEditorStatus;
    }

    public void setPit4UEditorStatus(final PIT4UEditorStatus pit4UEditorStatus) {
        this.pit4UEditorStatus = pit4UEditorStatus;
    }

    private String resolveLauncherPathSynchronously(final ExecutionEnvironment environment) {
        final var project = environment.getProject();
        final var module = getConfigurationModule().getModule();
        if (module == null) {
            return StringUtils.EMPTY;
        }

        final var detectedVersion = ApplicationManager.getApplication()
                .runReadAction((Computable<String>) () -> detectJUnitPlatformVersion(module));
        if (detectedVersion == null) {
            return StringUtils.EMPTY;
        }

        try {
            return getOrDownloadMatchingLauncherAsync(project, detectedVersion)
                    .get(5, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            return StringUtils.EMPTY;
        }
    }

    private String detectJUnitPlatformVersion(final Module module) {
        final var files = OrderEnumerator.orderEntries(module)
                .recursively()
                .exportedOnly()
                .classes()
                .getRoots();

        for (final var file : files) {
            final var name = file.getName();
            final var matcher = DEPENDENCY_PATTERN.matcher(name);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private CompletableFuture<String> getOrDownloadMatchingLauncherAsync(final Project project, final String version) {
        final var future = new CompletableFuture<String>();
        final var repositoryLibraryProperties = new RepositoryLibraryProperties(
                "org.junit.platform", "junit-platform-launcher", version
        );
        final var repositories = RemoteRepositoriesConfiguration.getInstance(project).getRepositories();

        JarRepositoryManager.loadDependenciesAsync(
                        project,
                        repositoryLibraryProperties,
                        false,
                        false,
                        repositories,
                        null
                )
                .onSuccess(resolvedRoots -> {
                    if (resolvedRoots == null || resolvedRoots.isEmpty()) {
                        future.complete(StringUtils.EMPTY);
                        return;
                    }
                    final var cleanPath = PathUtil.toPresentableUrl(resolvedRoots.getFirst().getFile().getUrl());
                    future.complete(cleanPath);
                })
                .onError(error -> future.complete(StringUtils.EMPTY));
        return future;
    }
}