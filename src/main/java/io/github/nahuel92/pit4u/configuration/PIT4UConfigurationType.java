package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NotNullLazyValue;
import io.github.nahuel92.pit4u.icons.PIT4UIcon;

import java.util.Objects;

public final class PIT4UConfigurationType extends ConfigurationTypeBase {
    private static final String ID = "Pit4UConfigurationType";

    PIT4UConfigurationType() {
        super(
                ID,
                "PIT4U",
                "PIT runner that assesses test strength",
                NotNullLazyValue.createValue(() -> Objects.requireNonNullElse(PIT4UIcon.ICON, AllIcons.Empty))
        );
        addFactory(new PIT4UConfigurationFactory(this));
    }
}