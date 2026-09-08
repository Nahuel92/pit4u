package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

final class FileListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        final var project = source.getProject();
        final var psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return;
        }
        for (final var fileEditor : source.getEditors(file)) {
            if (fileEditor instanceof TextEditor textEditor) {
                UIPainter.paintEditor(textEditor.getEditor(), psiFile);
            }
        }
    }
}
