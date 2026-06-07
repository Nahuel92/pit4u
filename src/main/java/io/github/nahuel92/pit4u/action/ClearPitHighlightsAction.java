package io.github.nahuel92.pit4u.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import io.github.nahuel92.pit4u.runner.PitMutationDataService;
import org.jetbrains.annotations.NotNull;

public final class ClearPitHighlightsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final var project = e.getProject();
        if (project == null) {
            return;
        }

        PitMutationDataService.getInstance(project).clear();

        final var fileEditorManager = FileEditorManager.getInstance(project);
        for (var editorWrapper : fileEditorManager.getAllEditors()) {
            if (editorWrapper instanceof TextEditor textEditor) {
                final var editor = textEditor.getEditor();
                editor.getMarkupModel().removeAllHighlighters();
            }
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final var project = e.getProject();
        if (project != null) {
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }
        e.getPresentation().setEnabledAndVisible(false);
    }
}