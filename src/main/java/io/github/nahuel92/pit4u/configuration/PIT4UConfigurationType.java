package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class PIT4UConfigurationType implements ConfigurationType {
    private static final Icon ICON = IconLoader.getIcon("/pit4u.svg", PIT4UConfigurationType.class);
    private static final String ID = "Pit4UConfigurationType";

    @Override
    @NotNull
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getDisplayName() {
        return "PIT4U";
    }

    @Override
    @Nls(capitalization = Nls.Capitalization.Sentence)
    public String getConfigurationTypeDescription() {
        return "PIT runner that assess your tests strength";
    }

    @Override
    @NotNull
    @NonNls
    public String getId() {
        return ID;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new PIT4UConfigurationFactory(this)};
    }
}
