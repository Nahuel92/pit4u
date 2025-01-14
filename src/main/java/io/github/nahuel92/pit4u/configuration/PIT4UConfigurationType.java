package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NotNullLazyValue;
import io.github.nahuel92.pit4u.icons.PIT4UIcon;

import java.util.Objects;

public class PIT4UConfigurationType extends ConfigurationTypeBase {
    private static final String ID = "Pit4UConfigurationType";

    protected PIT4UConfigurationType() {
        super(
                ID,
                "PIT4U",
                "PIT runner that assess your tests strength",
                NotNullLazyValue.createValue(() -> Objects.requireNonNullElse(PIT4UIcon.ICON, AllIcons.Empty))
        );
        addFactory(new PIT4UConfigurationFactory(this));
    }
}