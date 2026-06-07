package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.awt.Color;
import java.util.List;

public final class PitCoveragePainter {
    // Define accessible, soft colors for Light and Dark (Darcula) IDE modes
    private static final TextAttributes KILLED_ATTRS = new TextAttributes(
            null, new JBColor(new Color(210, 245, 210), new Color(40, 70, 40)),
            null, null, 0
    );
    private static final TextAttributes SURVIVED_ATTRS = new TextAttributes(
            null, new JBColor(new Color(255, 210, 210), new Color(70, 40, 40)),
            null, null, 0
    );
    private static final TextAttributes NO_COVERAGE_ATTRS = new TextAttributes(
            null, new JBColor(new Color(240, 240, 240), new Color(55, 55, 55)),
            null, null, 0
    );

    public static void paintEditor(final Editor editor, final List<Mutation> mutations) {
        final var markupModel = editor.getMarkupModel();

        // Essential: Clear prior PIT decorations so they don't stack up infinitely
        markupModel.removeAllHighlighters();

        int totalLines = editor.getDocument().getLineCount();

        for (Mutation mutation : mutations) {
            int targetLine = mutation.lineNumber() - 1; // IntelliJ lines are 0-indexed
            if (targetLine < 0 || targetLine >= totalLines) {
                continue;
            }

            // Determine background treatment depending on PIT results
            TextAttributes attributes = NO_COVERAGE_ATTRS;
            if (mutation.status() == Mutation.Status.KILLED) {
                attributes = KILLED_ATTRS;
            } else if (mutation.detected()) {
                // You can expand your Mutation.Status enum if you parse SURVIVED later
                attributes = SURVIVED_ATTRS;
            }

            // Layering below standard selections so it behaves like native coverage bars
            RangeHighlighter highlighter = markupModel.addLineHighlighter(
                    targetLine,
                    HighlighterLayer.SELECTION - 1,
                    attributes
            );

            // Construct structural HTML context for the tooltip hover
            String htmlTooltip = String.format(
                    "<html>" +
                            "<b>PIT Mutation Status:</b> %s<br>" +
                            "<b>Method:</b> %s<br>" +
                            "<b>Mutator:</b> %s" +
                            "</html>",
                    mutation.status(),
                    mutation.mutatedMethod(),
                    mutation.description() != null ? mutation.description() : "N/A"
            );

            //highlighter.setGutterIconRenderer(new PitMutationGutterIconRenderer(htmlTooltip));
        }
    }
}
