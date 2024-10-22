package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;

import javax.swing.*;

class PIT4UConfigurationType extends ConfigurationTypeBase {
    private static final String ID = "Pit4UConfigurationType";
    private static final Icon ICON = IconLoader.findIcon(
            PIT4UConfigurationType.class.getResource("/icons/pit4u.svg"),
            false
    );

    protected PIT4UConfigurationType() {
        super(
                ID,
                "PIT4U",
                "PIT runner that assess your tests strength",
                NotNullLazyValue.createValue(() -> ICON == null ? AllIcons.Empty : ICON)
        );
        addFactory(new PIT4UConfigurationFactory(this));
    }
}