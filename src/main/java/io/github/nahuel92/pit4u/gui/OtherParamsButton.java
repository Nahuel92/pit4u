package io.github.nahuel92.pit4u.gui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class OtherParamsButton extends TextFieldWithBrowseButton {
    @Override
    @NotNull
    protected Icon getDefaultIcon() {
        return AllIcons.General.InlineVariables;
    }

    @Override
    @NotNull
    protected Icon getHoveredIcon() {
        return AllIcons.General.InlineVariablesHover;
    }

    @Override
    @NotNull
    @NlsContexts.Tooltip
    protected String getIconTooltip() {
        return "Edit Advanced Parameters" +
                " (" +
                KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK)) +
                ")";
    }
}