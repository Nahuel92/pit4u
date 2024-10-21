package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
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
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import io.github.nahuel92.pit4u.gui.PIT4USettingsEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class PIT4URunConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule, PIT4URunConfiguration>
        implements RunConfiguration, Disposable {
    private final Logger log = Logger.getInstance(PIT4URunConfiguration.class);
    private final PIT4UEditorStatus pit4UEditorStatus = new PIT4UEditorStatus();

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

            @Override
            protected JavaParameters createJavaParameters() {
                return JavaParametersCreator.create(getConfigurationModule(), getProject(), pit4UEditorStatus);
            }

            @Override
            @NotNull
            protected OSProcessHandler startProcess() throws ExecutionException {
                final var osProcessHandler = super.startProcess();
                osProcessHandler.addProcessListener(
                        new ProcessAdapter() {
                            @Override
                            public void processTerminated(@NotNull final ProcessEvent event) {
                                final var reportLink = "file:///" +
                                        Path.of(pit4UEditorStatus.getReportDir()).toAbsolutePath() +
                                        "/index.html";
                                consoleView.printHyperlink(
                                        "Report ready, click to open it in your browser",
                                        new OpenUrlHyperlinkInfo(reportLink)
                                );
                            }
                        }
                );
                return osProcessHandler;
            }

            @Override
            @NotNull
            public ExecutionResult execute(@NotNull final Executor executor,
                                           @NotNull final ProgramRunner<?> runner) throws ExecutionException {
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
}