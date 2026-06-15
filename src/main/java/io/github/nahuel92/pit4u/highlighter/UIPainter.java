package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import io.github.nahuel92.pit4u.highlighter.dto.Mutation;

import javax.swing.Icon;
import java.awt.Color;
import java.util.Collection;
import java.util.stream.Collectors;

public final class UIPainter {
    private static final Key<String> PIT_TOOLTIP_KEY = Key.create("PIT_TOOLTIP");

    private static final TextAttributes KILLED_ATTRS = createTextAttributes(
            new Color(215, 245, 215),
            new Color(43, 68, 43)
    );
    private static final LineIcon KILLED_GUTTER_ICON = new LineIcon(
            new JBColor(
                    new Color(46, 139, 87),
                    new Color(60, 179, 113)
            )
    );
    private static final MutationResult KILLED_RESULT = new MutationResult(KILLED_ATTRS, KILLED_GUTTER_ICON);

    private static final TextAttributes SURVIVED_ATTRS = createTextAttributes(
            new Color(255, 215, 215),
            new Color(75, 43, 43)
    );
    private static final LineIcon SURVIVED_GUTTER_ICON = new LineIcon(
            new JBColor(
                    new Color(178, 34, 34),
                    new Color(220, 20, 60))
    );
    private static final MutationResult SURVIVED_RESULT = new MutationResult(SURVIVED_ATTRS, SURVIVED_GUTTER_ICON);

    private static final TextAttributes NO_COVERAGE_ATTRS = createTextAttributes(
            new Color(242, 242, 242),
            new Color(53, 53, 53)
    );
    private static final LineIcon NO_COVERAGE_GUTTER_ICON = new LineIcon(JBColor.GRAY);
    private static final MutationResult NO_COVERAGE_RESULT = new MutationResult(NO_COVERAGE_ATTRS, NO_COVERAGE_GUTTER_ICON);

    private static TextAttributes createTextAttributes(final Color regular, final Color dark) {
        return new TextAttributes(
                null,
                new JBColor(regular, dark),
                null,
                null,
                0
        );
    }

    public static void paintEditor(final Editor editor, final PsiFile psiFile) {
        if (!(psiFile instanceof PsiClassOwner classOwner)) {
            return;
        }
        editor.getMarkupModel().removeAllHighlighters();
        final var classes = classOwner.getClasses();
        if (classes.length == 0) {
            return;
        }
        final var fqName = classes[0].getQualifiedName();
        if (fqName == null) {
            return;
        }

        final var dataService = MutationDataService.getInstance(psiFile.getProject());
        final var mutations = dataService.getMutationsForClass(fqName);
        if (mutations == null || mutations.isEmpty()) {
            return;
        }

        final var totalLines = editor.getDocument().getLineCount();
        final var mutationsByLine = mutations.stream()
                .collect(Collectors.groupingBy(Mutation::lineNumber));

        for (final var entry : mutationsByLine.entrySet()) {
            final var targetLine = entry.getKey() - 1;
            if (targetLine < 0 || targetLine >= totalLines) {
                continue;
            }
            final var lineMutations = entry.getValue();
            final var result = evaluateMutationResult(lineMutations);
            final var highlighter = editor.getMarkupModel()
                    .addLineHighlighter(
                            targetLine,
                            HighlighterLayer.SELECTION - 1,
                            result.attributes()
                    );
            final var combinedHtmlTooltip = buildHtmlToolTip(lineMutations);
            highlighter.setGutterIconRenderer(new MutationGutterIconRenderer(combinedHtmlTooltip, result.gutterIcon()));
            highlighter.putUserData(PIT_TOOLTIP_KEY, combinedHtmlTooltip);
        }
    }

    private static MutationResult evaluateMutationResult(final Collection<Mutation> lineMutations) {
        boolean anySurvived = false;
        boolean allKilled = true;
        for (final var lineMutation : lineMutations) {
            if (!lineMutation.detected()) {
                anySurvived = true;
            }
            if (lineMutation.status() != Mutation.Status.KILLED) {
                allKilled = false;
            }
            if (anySurvived && !allKilled) {
                break;
            }
        }
        if (anySurvived) {
            return SURVIVED_RESULT;
        }
        if (allKilled) {
            return KILLED_RESULT;
        }
        return NO_COVERAGE_RESULT;
    }

    private static String buildHtmlToolTip(final Collection<Mutation> lineMutations) {
        final var htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body>")
                .append("<div style='max-height: 250px; width: 400px; overflow-y: auto; padding-right: 5px;'>")
                .append("<b>PIT Mutations on this line (").append(lineMutations.size()).append("):</b>")
                .append("<hr style='border: 0; border-top: 1px solid #555; margin: 5px 0;'/>")
                .append("<ul style='margin-left: 15px; padding-left: 0;'>");

        for (final var mutation : lineMutations) {
            final var badgeColor = mutation.status() == Mutation.Status.KILLED ? "#4E8B57" : "#B22222";
            htmlBuilder.append("<li style='margin-bottom: 8px;'>")
                    .append("<span style='color: ").append(badgeColor).append("; font-weight: bold;'>[")
                    .append(mutation.status()).append("]</span> ")
                    .append("<b>Method:</b> ").append(mutation.mutatedMethod()).append("<br/>")
                    .append("<b>Detail:</b> ").append(mutation.description() != null ? mutation.description() : "None")
                    .append("</li>");
        }

        htmlBuilder.append("</ul>")
                .append("</div>")
                .append("</body></html>");
        return htmlBuilder.toString();
    }

    private record MutationResult(TextAttributes attributes, Icon gutterIcon) {
    }
}
