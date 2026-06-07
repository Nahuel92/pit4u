package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public final class PitFileListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        final var project = source.getProject();
        final var psiFile = PsiManager.getInstance(project).findFile(file);
        final var fileEditor = source.getSelectedEditor(file);
        if (psiFile != null && fileEditor instanceof TextEditor textEditor) {
            PitUIPainter.paintEditor(textEditor.getEditor(), psiFile);
        }
    }
}
