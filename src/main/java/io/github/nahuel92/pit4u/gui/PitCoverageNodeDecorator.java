package io.github.nahuel92.pit4u.gui;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.psi.PsiClass;
import com.intellij.ui.SimpleTextAttributes;
import io.github.nahuel92.pit4u.highlighter.MutationDataService;

final class PitCoverageNodeDecorator implements ProjectViewNodeDecorator {
    @Override
    public void decorate(final ProjectViewNode<?> node, final PresentationData data) {
        final var project = node.getProject();
        if (project == null) {
            return;
        }
        final var value = node.getValue();
        if (!(value instanceof PsiClass psiClass)) {
            return;
        }
        final var qName = psiClass.getQualifiedName();
        final var service = project.getService(MutationDataService.class);
        final var stats = service.getCoverageSummaryForClass(qName);
        if (stats == null) {
            return;
        }
        if (data.getColoredText().isEmpty()) {
            final var name = psiClass.getName();
            if (name != null) {
                data.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
        data.addText(" " + stats, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
}