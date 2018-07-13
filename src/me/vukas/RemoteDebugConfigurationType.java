package me.vukas;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RemoteDebugConfigurationType implements ConfigurationType {
    private final ConfigurationFactory myFactory;

    public RemoteDebugConfigurationType() {
        myFactory = new ConfigurationFactory(this) {
            @NotNull
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new RemoteDebugConfiguration(project, this);
            }
        };
    }

    @Override
    public String getDisplayName() {
        return "Remote Debug";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Remote Debug App";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.RunConfigurations.Remote;
    }

    @NotNull
    @Override
    public String getId() {
        return "remote-debug";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }
}
