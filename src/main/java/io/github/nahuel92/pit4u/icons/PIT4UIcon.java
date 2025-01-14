package io.github.nahuel92.pit4u.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface PIT4UIcon {
    Icon ICON = IconLoader.findIcon(
            PIT4UIcon.class.getResource("/icons/pit4u.svg"),
            false
    );
}
