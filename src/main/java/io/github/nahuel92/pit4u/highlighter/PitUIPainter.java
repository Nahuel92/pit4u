package io.github.nahuel92.pit4u.highlighter;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import io.github.nahuel92.pit4u.icons.PitColorIcon;
import io.github.nahuel92.pit4u.runner.PitMutationDataService;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Point;
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

        // Clear existing highlights first
        editor.getMarkupModel().removeAllHighlighters();

        // Find data for this file
        var project = psiFile.getProject();
        var dataService = PitMutationDataService.getInstance(project);

        // Basic fallback to find the primary FQ name of the file
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

            // 🔥 4. DETERMINE COMBINED LINE STATUS
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

            // 🔥 5. COMBINE TOOLTIPS INTO A SINGLE HTML MESSAGE
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><body>");
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

            htmlBuilder.append("</ul></body></html>");
            String combinedHtmlTooltip = htmlBuilder.toString();

            // 6. Paint the single consolidated line marker
            final var highlighter = editor.getMarkupModel().addLineHighlighter(
                    targetLine,
                    HighlighterLayer.SELECTION - 1,
                    attributes
            );

            highlighter.setGutterIconRenderer(new PitMutationGutterIconRenderer(combinedHtmlTooltip, gutterIcon));
            highlighter.putUserData(PIT_TOOLTIP_KEY, combinedHtmlTooltip);
        }

        /*
        for (final var mutation : mutations) {
            int targetLine = mutation.lineNumber() - 1;
            if (targetLine < 0 || targetLine >= totalLines) {
                continue;
            }

            TextAttributes attributes = NO_COVERAGE_ATTRS;
            var gutterIcon = new PitColorIcon(JBColor.GRAY);
            if (mutation.status() == Mutation.Status.KILLED) {
                attributes = KILLED_ATTRS;
                gutterIcon = new PitColorIcon(new JBColor(new Color(46, 139, 87), new Color(60, 179, 113)));

            } else if (!mutation.detected()) {
                attributes = SURVIVED_ATTRS;
                gutterIcon = new PitColorIcon(new JBColor(new Color(178, 34, 34), new Color(220, 20, 60)));
            }

            final var highlighter = editor.getMarkupModel().addLineHighlighter(
                    targetLine,
                    HighlighterLayer.SELECTION - 1,
                    attributes
            );

            final var htmlTooltip = "<html><body><b>PIT Mutation:</b> %s<br/><b>Method:</b> %s<br/><b>Detail:</b> %s</body></html>"
                    .formatted(mutation.status(), mutation.mutatedMethod(), mutation.description());
            highlighter.setGutterIconRenderer(new PitMutationGutterIconRenderer(htmlTooltip, gutterIcon));

            highlighter.putUserData(PIT_TOOLTIP_KEY, htmlTooltip);
            //setupInstantHoverListener(editor);
        }
         */
    }

    private static void setupInstantHoverListener(final Editor editor) {
        // Remove prior listener instances if already attached to avoid duplicates
        editor.addEditorMouseMotionListener(new EditorMouseMotionListener() {
            private int lastHoveredLine = -1;

            @Override
            public void mouseMoved(@NotNull EditorMouseEvent e) {
                // We only care if the cursor is hanging out over the Gutter!
                if (e.getArea() != EditorMouseEventArea.LINE_MARKERS_AREA) {
                    // If the mouse leaves the gutter area, reset our tracking line
                    lastHoveredLine = -1;
                    return;
                }

                // Determine what line the user is looking at
                int logicalLine = editor.xyToLogicalPosition(e.getMouseEvent().getPoint()).line;
                if (logicalLine == lastHoveredLine) {
                    return;
                }
                boolean foundHighlighterForLine = false;

                // Scan the highlighters applied to this specific line
                for (RangeHighlighter highlighter : editor.getMarkupModel().getAllHighlighters()) {
                    if (!highlighter.isValid()) {
                        continue;
                    }

                    int highlighterLine = editor.getDocument().getLineNumber(highlighter.getStartOffset());
                    if (highlighterLine != logicalLine) {
                        continue;
                    }
                    String tooltipText = highlighter.getUserData(PIT_TOOLTIP_KEY);

                    if (tooltipText != null && !tooltipText.isEmpty()) {
                        foundHighlighterForLine = true;
                        // Update our state to track this active line
                        lastHoveredLine = logicalLine;

                        JComponent hintComponent = HintUtil.createInformationLabel(tooltipText);
                        Point mouseScreenPoint = e.getMouseEvent().getLocationOnScreen();

                        mouseScreenPoint.x += 15;
                        mouseScreenPoint.y += 10;

                        HintManager.getInstance().showHint(
                                hintComponent,
                                new RelativePoint(mouseScreenPoint),
                                HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING,
                                0
                        );
                        break;
                    }
                }

                if (!foundHighlighterForLine) {
                    lastHoveredLine = -1;
                }
            }
        });
    }
}
