package io.github.nahuel92.pit4u.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import io.github.nahuel92.pit4u.runner.PitMutationDataService;
import org.jetbrains.annotations.NotNull;

public class ClearPitHighlightsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        PitMutationDataService.getInstance(project).clear();

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (var editorWrapper : fileEditorManager.getAllEditors()) {
            if (editorWrapper instanceof TextEditor textEditor) {
                Editor editor = textEditor.getEditor();
                editor.getMarkupModel().removeAllHighlighters();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            var service = PitMutationDataService.getInstance(project);
            e.getPresentation().setEnabledAndVisible(true);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}