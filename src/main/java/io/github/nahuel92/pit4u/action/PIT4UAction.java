package io.github.nahuel92.pit4u.action;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiQualifiedNamedElement;
import io.github.nahuel92.pit4u.configuration.PIT4UConfigurationType;
import io.github.nahuel92.pit4u.configuration.PIT4UEditorStatus;
import io.github.nahuel92.pit4u.configuration.PIT4URunConfiguration;
import io.github.nahuel92.pit4u.icons.PIT4UIcon;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

public class PIT4UAction extends AnAction {
    private static final Logger log = Logger.getInstance(PIT4UAction.class);

    PIT4UAction() {
        getTemplatePresentation().setIcon(PIT4UIcon.ICON);
    }

    private static RunnerAndConfigurationSettings getRunConfig(final RunManager runManager) {
        final var runConfig = runManager.findConfigurationByName("PIT4URunConfiguration");
        if (runConfig != null) {
            return runConfig;
        }
        final var newRunConfig = runManager.createConfiguration(
                "PIT4URunConfiguration 1",
                PIT4UConfigurationType.class
        );
        runManager.addConfiguration(newRunConfig);
        runManager.setSelectedConfiguration(newRunConfig);
        return newRunConfig;
    }

    private static void setDirectoryLabel(final AnActionEvent e, final PsiDirectoryNode psiDirectoryNode) {
        final var javaDirectoryService = JavaDirectoryService.getInstance();
        final var dirPackage = javaDirectoryService.getPackage(psiDirectoryNode.getValue());
        if (dirPackage != null) {
            e.getPresentation().setText("Mutate All Classes in " + dirPackage.getQualifiedName());
        }
    }

    private static void setClassLabel(final AnActionEvent e, final ClassTreeNode classTreeNode) {
        if (classTreeNode.getPsiClass() instanceof PsiQualifiedNamedElement psiQualifiedNamedElement) {
            e.getPresentation().setText("Mutate Class " + psiQualifiedNamedElement.getQualifiedName());
        }
    }

    private static void setProjectLabel(final AnActionEvent e) {
        final var project = e.getProject();
        if (project == null) {
            return;
        }
        e.getPresentation().setText("Mutate Project " + project.getName());
    }

    private static Optional<ExecutionEnvironment> getExecutionEnvironmentBuilder(
            final RunnerAndConfigurationSettings runConfig) {
        try {
            return Optional.of(ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), runConfig)
                    .activeTarget()
                    .build()
            );
        } catch (final ExecutionException ex) {
            log.error("failed to create execution environment", ex);
            return Optional.empty();
        }
    }

    private static PIT4UEditorStatus getPit4UEditorStatus(final AnActionEvent e, final String projectName, final String basePath) {
        final var fullyQualifiedPackage = getFullyQualifiedPackage(e.getData(CommonDataKeys.PSI_ELEMENT), projectName);
        final var status = new PIT4UEditorStatus();
        final var path = Path.of(basePath);
        status.setReportDir(path.resolve("target").toString());
        status.setSourceDir(path.resolve("src").resolve("main").resolve("java").toString());
        status.setTargetClasses(fullyQualifiedPackage + "*");
        status.setTargetTests(fullyQualifiedPackage + "*");
        return status;
    }

    private static String getFullyQualifiedPackage(final PsiElement element, final String projectName) {
        if (element instanceof PsiQualifiedNamedElement qualifiedNamedElement) {
            return qualifiedNamedElement.getQualifiedName();
        }
        if (element instanceof PsiDirectory psiDirectory) {
            final var javaDirectoryService = JavaDirectoryService.getInstance();
            final var dirPackage = javaDirectoryService.getPackage(psiDirectory);
            if (dirPackage != null) {
                return dirPackage.getQualifiedName();
            }
        }
        return projectName;
    }

    private static void executeRunConfiguration(final RunnerAndConfigurationSettings runConfig) {
        final var executionBuilder = getExecutionEnvironmentBuilder(runConfig);
        if (executionBuilder.isEmpty()) {
            log.error("ExecutionBuilder is empty");
            return;
        }
        ProgramRunnerUtil.executeConfiguration(executionBuilder.get(), true, true);
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        super.update(e);
        final var navigatables = e.getData(CommonDataKeys.NAVIGATABLE_ARRAY);
        if (navigatables == null || navigatables[0] == null) {
            return;
        }
        switch (navigatables[0]) {
            case PsiDirectoryNode psiDirectoryNode -> setDirectoryLabel(e, psiDirectoryNode);
            case ClassTreeNode classTreeNode -> setClassLabel(e, classTreeNode);
            default -> setProjectLabel(e);
        }
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final var project = e.getProject();
        if (project == null || project.getBasePath() == null) {
            return;
        }
        final var runManager = RunManager.getInstance(project);
        final var runConfig = getRunConfig(runManager);
        if (!(runConfig.getConfiguration() instanceof PIT4URunConfiguration config)) {
            log.error("PIT4URunConfiguration wasn't found!");
            return;
        }
        config.setPit4UEditorStatus(getPit4UEditorStatus(e, project.getName(), project.getBasePath()));
        executeRunConfiguration(runConfig);
    }
}
