package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import io.github.nahuel92.pit4u.icons.PitColorIcon;
import io.github.nahuel92.pit4u.runner.PitMutationDataService;

import javax.swing.Icon;
import java.awt.Color;
import java.util.stream.Collectors;

public final class PitUIPainter {
    private static final Key<String> PIT_TOOLTIP_KEY = Key.create("PIT_TOOLTIP");
    private static final TextAttributes KILLED_ATTRS = new TextAttributes(
            null,
            new JBColor(
                    new Color(215, 245, 215),
                    new Color(43, 68, 43)
            ),
            null,
            null,
            0
    );
    private static final TextAttributes SURVIVED_ATTRS = new TextAttributes(
            null,
            new JBColor(
                    new Color(255, 215, 215),
                    new Color(75, 43, 43)
            ),
            null,
            null,
            0
    );
    private static final TextAttributes NO_COVERAGE_ATTRS = new TextAttributes(
            null,
            new JBColor(
                    new Color(242, 242, 242),
                    new Color(53, 53, 53)
            ),
            null,
            null,
            0
    );

    public static void paintEditor(final Editor editor, final PsiFile psiFile) {
        if (!(psiFile instanceof PsiClassOwner classOwner)) {
            return;
        }

        editor.getMarkupModel().removeAllHighlighters();

        var project = psiFile.getProject();
        var dataService = PitMutationDataService.getInstance(project);

        var classes = classOwner.getClasses();
        if (classes.length == 0) {
            return;
        }
        String fqName = classes[0].getQualifiedName();
        if (fqName == null) {
            return;
        }

        final var mutations = dataService.getMutationsForClass(fqName);
        if (mutations == null || mutations.isEmpty()) {
            return;
        }

        int totalLines = editor.getDocument().getLineCount();

        final var mutationsByLine = mutations.stream()
                .collect(Collectors.groupingBy(Mutation::lineNumber));

        for (final var entry : mutationsByLine.entrySet()) {
            int rawLineNumber = entry.getKey();
            final var lineMutations = entry.getValue();

            int targetLine = rawLineNumber - 1; // 0-indexed adjustment
            if (targetLine < 0 || targetLine >= totalLines) {
                continue;
            }

            // Rule: If even one mutation survived (not detected), the line counts as SURVIVED (Red).
            boolean anySurvived = lineMutations.stream().anyMatch(m -> !m.detected());
            boolean allKilled = lineMutations.stream().allMatch(m -> m.status() == Mutation.Status.KILLED);

            TextAttributes attributes = NO_COVERAGE_ATTRS;
            Icon gutterIcon = new PitColorIcon(JBColor.GRAY);

            if (anySurvived) {
                attributes = SURVIVED_ATTRS;
                gutterIcon = new PitColorIcon(new JBColor(new Color(178, 34, 34), new Color(220, 20, 60))); // Red
            } else if (allKilled) {
                attributes = KILLED_ATTRS;
                gutterIcon = new PitColorIcon(new JBColor(new Color(46, 139, 87), new Color(60, 179, 113))); // Green
            }

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><body>");

            htmlBuilder.append("<div style='max-height: 250px; width: 400px; overflow-y: auto; padding-right: 5px;'>");

            htmlBuilder.append("<b>PIT Mutations on this line (").append(lineMutations.size()).append("):</b>");
            htmlBuilder.append("<hr style='border: 0; border-top: 1px solid #555; margin: 5px 0;'/>");
            htmlBuilder.append("<ul style='margin-left: 15px; padding-left: 0;'>");

            for (Mutation m : lineMutations) {
                String badgeColor = m.status() == Mutation.Status.KILLED ? "#4E8B57" : "#B22222";
                htmlBuilder.append("<li style='margin-bottom: 8px;'>")
                        .append("<span style='color: ").append(badgeColor).append("; font-weight: bold;'>[")
                        .append(m.status()).append("]</span> ")
                        .append("<b>Method:</b> ").append(m.mutatedMethod()).append("<br/>")
                        .append("<b>Detail:</b> ").append(m.description() != null ? m.description() : "None")
                        .append("</li>");
            }

            htmlBuilder.append("</ul>");
            htmlBuilder.append("</div>"); // Close the scrolling div
            htmlBuilder.append("</body></html>");

            String combinedHtmlTooltip = htmlBuilder.toString();

            final var highlighter = editor.getMarkupModel().addLineHighlighter(
                    targetLine,
                    HighlighterLayer.SELECTION - 1,
                    attributes
            );

            highlighter.setGutterIconRenderer(new PitMutationGutterIconRenderer(combinedHtmlTooltip, gutterIcon));
            highlighter.putUserData(PIT_TOOLTIP_KEY, combinedHtmlTooltip);
        }
    }
}
