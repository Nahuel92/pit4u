package io.github.nahuel92.pit4u.action;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiQualifiedNamedElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScopesCore.DirectoryScope;
import io.github.nahuel92.pit4u.configuration.PIT4UConfigurationType;
import io.github.nahuel92.pit4u.configuration.PIT4UEditorStatus;
import io.github.nahuel92.pit4u.configuration.PIT4URunConfiguration;
import io.github.nahuel92.pit4u.icons.PIT4UIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

public class PIT4UAction extends AnAction {
    private static final Logger log = Logger.getInstance(PIT4UAction.class);

    PIT4UAction() {
        getTemplatePresentation().setIcon(PIT4UIcon.ICON);
    }

    private static boolean shouldShow(final AnActionEvent e) {
        final var project = e.getProject();
        final var module = e.getData(PlatformCoreDataKeys.MODULE);
        if (project == null || module == null) {
            return false;
        }
        final var psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement instanceof PsiDirectory psiDirectory) {
            final var scope = new DirectoryScope(project, psiDirectory.getVirtualFile(), true);
            final var containsJavaFiles = FileTypeIndex.containsFileOfType(
                    JavaFileType.INSTANCE,
                    scope
            );
            final var containsTestFiles = FileTypeIndex.processFiles(
                    JavaFileType.INSTANCE,
                    a -> a.getPath().contains("test"),
                    scope
            );
            return containsJavaFiles && !containsTestFiles;
        }
        return psiElement instanceof PsiClass psiClass &&
                !psiClass.getContainingFile()
                        .getContainingDirectory()
                        .getVirtualFile()
                        .getPath()
                        .contains("test");
    }

    private static boolean isProjectRoot(final Project project, final PsiDirectory psiDirectory) {
        if (project == null || project.getBasePath() == null) {
            return false;
        }
        return project.getBasePath().equals(psiDirectory.getVirtualFile().getPath());
    }

    private static void setProjectLabel(final AnActionEvent e) {
        final var project = e.getProject();
        if (project != null) {
            e.getPresentation().setText("Mutate all in " + project.getName());
        }
    }

    private static void setDirectoryLabel(final AnActionEvent e, final PsiDirectory psiDirectory) {
        final var javaDirectoryService = JavaDirectoryService.getInstance();
        final var dirPackage = javaDirectoryService.getPackage(psiDirectory);
        e.getPresentation().setText("Mutate all in " + getDirectoryName(dirPackage, psiDirectory));
    }

    private static String getDirectoryName(final PsiPackage dirPackage, final PsiDirectory psiDirectory) {
        if (dirPackage == null || dirPackage.getQualifiedName().isBlank()) {
            return psiDirectory.getName();
        }
        return dirPackage.getQualifiedName();
    }

    private static void setClassLabel(final AnActionEvent e, final PsiClass psiClass) {
        e.getPresentation().setText("Mutate " + psiClass.getQualifiedName());
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

    private static PIT4UEditorStatus getPit4UEditorStatus(final AnActionEvent e, final Project project, final String basePath) {
        final var status = new PIT4UEditorStatus();
        setSourceAndReportDirs(e, status, basePath);
        final var fullyQualifiedPackages = getFullyQualifiedPackages(e, project);
        status.setTargetClasses(fullyQualifiedPackages);
        status.setTargetTests(fullyQualifiedPackages);
        return status;
    }

    private static void setSourceAndReportDirs(final AnActionEvent e, final PIT4UEditorStatus status, final String basePath) {
        final var module = e.getData(PlatformCoreDataKeys.MODULE);
        if (module == null) {
            return;
        }
        final var path = Path.of(basePath);
        final var propManager = ExternalSystemModulePropertyManager.getInstance(module);
        if (MavenUtil.isMavenModule(module)) {
            status.setReportDir(path.resolve("target").toString());
            status.setSourceDir(path.resolve("src").resolve("main").resolve("java").toString());
            return;
        }
        if ("gradle".equalsIgnoreCase(propManager.getExternalSystemId())) {
            status.setReportDir(path.resolve("build").toString());
            status.setSourceDir(path.resolve("src").resolve("main").resolve("java").toString());
        }
        log.info("Module is not using Maven or Gradle as build system!");
    }

    private static String getFullyQualifiedPackages(final AnActionEvent event, final Project project) {
        if (event.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiQualifiedNamedElement psiQualifiedNamedElement) {
            return psiQualifiedNamedElement.getQualifiedName();
        }

        final var selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selectedFile == null) {
            return null;
        }

        final var javaFiles = FileTypeIndex.getFiles(
                JavaFileType.INSTANCE,
                new DirectoryScope(project, selectedFile, true)
        );
        return javaFiles.stream()
                .map(PsiManager.getInstance(project)::findFile)
                .filter(e -> e instanceof PsiJavaFile)
                .map(e -> ((PsiJavaFile) e).getPackageName() + ".*")
                .collect(Collectors.joining(","));
    }

    private static void executeRunConfiguration(final RunnerAndConfigurationSettings runConfig) {
        final var executionBuilder = getExecEnvBuilder(runConfig);
        if (executionBuilder.isEmpty()) {
            log.error("ExecutionBuilder is empty");
            return;
        }
        ProgramRunnerUtil.executeConfiguration(executionBuilder.get(), true, true);
    }

    private static Optional<ExecutionEnvironment> getExecEnvBuilder(final RunnerAndConfigurationSettings runConfig) {
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

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        if (!shouldShow(e)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().setEnabledAndVisible(true);
        final var element = e.getData(CommonDataKeys.PSI_ELEMENT);
        switch (element) {
            case PsiDirectory psiDirectory when isProjectRoot(e.getProject(), psiDirectory) -> setProjectLabel(e);
            case PsiDirectory psiDirectory -> setDirectoryLabel(e, psiDirectory);
            case PsiClass psiClass -> setClassLabel(e, psiClass);
            case null, default -> throw new IllegalStateException("Unexpected value: " + element);
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
        config.setPit4UEditorStatus(getPit4UEditorStatus(e, project, project.getBasePath()));
        executeRunConfiguration(runConfig);
    }
}
