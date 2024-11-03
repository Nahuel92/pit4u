package io.github.nahuel92.pit4u.highlighter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.execution.lineMarker.RunLineMarkerProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Highlighter extends RunLineMarkerProvider {
    private List<PITLine> results;

    private static Editor[] getEditors(final PsiFile file) {
        final var virtualFile = file.getVirtualFile();
        return EditorFactory.getInstance()
                .getEditors(
                        Objects.requireNonNull(
                                FileDocumentManager.getInstance()
                                        .getDocument(virtualFile)
                        )
                );
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement psiElement) {
        if (results == null) {
            return null;
        }
        final var file = psiElement.getContainingFile();
        final var fileName = file.getName();
        final var document = file.getViewProvider().getDocument();
        //final var lineNumber = document.getLineNumber(psiElement.getTextOffset());

        final var editors = getEditors(file);
        if (editors.length == 0) {
            return null;
        }

        final var markupModel = editors[0].getMarkupModel();

        Map<Integer, RangeHighlighter> highlighters = new HashMap<>();

        final var covered = results.stream()
                .filter(e -> PITLine.Status.KILLED.equals(e.status()))
                .map(PITLine::lineNumber)
                .map(e -> e + 1)
                .toList();

        //for (int line : coverageData.getCoveredLines()) {
        for (final var line : covered) {
            RangeHighlighter highlighter = addLineHighlighter(
                    markupModel,
                    line - 1,
                    new JBColor(
                            new Color(0, 255, 0, 128),
                            new Color(0, 255, 0, 128)
                    )
            );
            highlighters.put(line, highlighter);
        }

        final var uncovered = results.stream()
                .filter(e -> PITLine.Status.NO_COVERAGE.equals(e.status()))
                .map(PITLine::lineNumber)
                .map(e -> e + 1)
                .toList();

        for (final var line : uncovered) {
            //for (int line : coverageData.getUncoveredLines()) {
            RangeHighlighter highlighter = addLineHighlighter(
                    markupModel,
                    line - 1,
                    new JBColor(
                            new Color(255, 0, 0, 128),
                            new Color(255, 0, 0, 128)
                    )
            );
            highlighters.put(line, highlighter);
        }

        //fileHighlighters.put(virtualFile, highlighters);
        return null;
    }

    public void updateMarkers(final Project project, final List<PITLine> results) {
        this.results = results;

        refreshEditor(project);
    }

    private void refreshEditor(final Project project) {
        Stream.of(FileEditorManager.getInstance(project).getSelectedEditors())
                .filter(e -> e instanceof TextEditor)
                .forEach(editor -> editor.getComponent().repaint());
    }

    private RangeHighlighter addLineHighlighter(final MarkupModel markupModel, int line, final Color color) {
        final var highlighter = markupModel.addLineHighlighter(
                line,
                HighlighterLayer.ADDITIONAL_SYNTAX,
                null
        );
        highlighter.setThinErrorStripeMark(true);
        highlighter.setGreedyToLeft(true);
        highlighter.setGreedyToRight(true);
        highlighter.setLineMarkerRenderer((editor, graphics, rectangle) -> {
            graphics.setColor(color);
            graphics.fillRect(0, rectangle.y, 8, rectangle.height);
        });
        return highlighter;
    }
}
