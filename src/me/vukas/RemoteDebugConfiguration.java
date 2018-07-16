package me.vukas;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RemoteRunProfile;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;

public class RemoteDebugConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule>
        implements RunConfigurationWithSuppressedDefaultRunAction, RemoteRunProfile {

    public RemoteDebugConfiguration(final Project project, ConfigurationFactory configurationFactory) {
        super(new JavaRunConfigurationModule(project, true), configurationFactory);
    }

    @Override
    public Collection<Module> getValidModules() {
        return getAllModules();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<RemoteDebugConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor("Remote Debug", new RemoteDebugConfigurable(getProject()));
        group.addEditor("Log", new LogConfigurationPanel<>());
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        final GenericDebuggerRunnerSettings debuggerSettings = (GenericDebuggerRunnerSettings)env.getRunnerSettings();
        if (debuggerSettings != null) {
            // sync self state with execution environment's state if available
            debuggerSettings.LOCAL = false;
            debuggerSettings.setDebugPort("30505");
            debuggerSettings.setTransport(DebuggerSettings.SOCKET_TRANSPORT);



            //new ApplicationConfigurable(getProject())
        }
        return new RemoteDebugState(getProject(), createRemoteConnection());
    }

    public RemoteConnection createRemoteConnection() {
        return new RemoteConnection(true, "10.10.121.137", "30505", false);
    }
}
