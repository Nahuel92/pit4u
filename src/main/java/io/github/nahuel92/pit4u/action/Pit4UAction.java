package io.github.nahuel92.pit4u.action;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiQualifiedNamedElement;
import io.github.nahuel92.pit4u.configuration.Pit4URunConfiguration;
import org.jetbrains.annotations.NotNull;

public class Pit4UAction extends AnAction {
    private Pit4URunConfiguration config;

    public Pit4UAction() {
        /*this.config = new Pit4URunConfiguration(
                "Test",
                ProjectManager.getInstance().getDefaultProject(),
                new Pit4UConfigurationFactory(new Pit4UConfigurationType())
        );*/
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        super.update(e);
        final var navigatables = e.getData(CommonDataKeys.NAVIGATABLE_ARRAY);
        if (navigatables == null || navigatables[0] == null) {
            return;
        }
        final var navigatable = navigatables[0];
        if (navigatable instanceof PsiDirectoryNode psiDirectoryNode) {
            final var javaDirectoryService = JavaDirectoryService.getInstance();
            final var dirPackage = javaDirectoryService.getPackage(psiDirectoryNode.getValue());
            if (dirPackage != null) {
                final var builder = ExecutionEnvironmentBuilder.create(
                        DefaultRunExecutor.getRunExecutorInstance(),
                        config
                );
                ProgramRunnerUtil.executeConfiguration(
                        builder.activeTarget().build(),
                        true,
                        true
                );
                return;
            }
        }
        if (navigatable instanceof ClassTreeNode classTreeNode) {
            if (classTreeNode.getPsiClass() instanceof PsiQualifiedNamedElement psiQualifiedNamedElement) {
                e.getPresentation().setText("Mutate Class " + psiQualifiedNamedElement.getQualifiedName());
                final var builder = ExecutionEnvironmentBuilder.create(
                        DefaultRunExecutor.getRunExecutorInstance(),
                        config
                );
                ProgramRunnerUtil.executeConfiguration(
                        builder.activeTarget().build(),
                        true,
                        true
                );
                return;
            }
        }
        final var project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setText("Mutate Project " + project.getName());
        final var builder = ExecutionEnvironmentBuilder.create(
                DefaultRunExecutor.getRunExecutorInstance(),
                config
        );
        ProgramRunnerUtil.executeConfiguration(
                builder.activeTarget().build(),
                true,
                true
        );
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final var element = e.getData(CommonDataKeys.PSI_ELEMENT);
        final var project = e.getProject();
        final var test = getFullyQualifiedPackage(element, project);

        Messages.showMessageDialog(test, "My Test Dialog", null);
    }

    private String getFullyQualifiedPackage(final PsiElement element, final Project project) {
        if (element instanceof PsiQualifiedNamedElement e1) {
            return e1.getQualifiedName();
        }
        if (element instanceof PsiDirectory e1) {
            final var javaDirectoryService = JavaDirectoryService.getInstance();
            final var dirPackage = javaDirectoryService.getPackage(e1);
            if (dirPackage != null) {
                return dirPackage.getQualifiedName();
            }
        }
        return project.getName();
    }
}
