package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class Pit4UConfigurationFactory extends ConfigurationFactory {
    public Pit4UConfigurationFactory(@NotNull final ConfigurationType type) {
        super(type);
    }

    @Override
    @NotNull
    public String getId() {
        return getType().getId();
    }

    @Override
    public Icon getIcon(@NotNull final RunConfiguration configuration) {
        return configuration.getIcon();
    }

    @Override
    @NotNull
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
        return new Pit4URunConfiguration("PIT4U Configuration", project, this);
    }
}
