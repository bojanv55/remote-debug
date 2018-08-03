package me.vukas;

import java.util.Collection;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RemoteRunProfile;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

public class RemoteDebugConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule>
        implements RunConfigurationWithSuppressedDefaultRunAction, RemoteRunProfile {

    public String APPCONF = "ApplicationConfigurationName";
    public String HOST = "localhost";
    public String PORT = "55004";

	@Override
	public void writeExternal(@NotNull final Element element) throws WriteExternalException {
		super.writeExternal(element);
		DefaultJDOMExternalizer.writeExternal(this, element);
	}

	@Override
	public void readExternal(@NotNull final Element element) throws InvalidDataException {
		super.readExternal(element);
		DefaultJDOMExternalizer.readExternal(this, element);
	}

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

        RunManagerImpl manager = RunManagerImpl.getInstanceImpl(getProject());
        ApplicationConfiguration rc = (ApplicationConfiguration)manager.getAllConfigurationsList().stream().filter(x -> x.getName().equals(APPCONF)
            && ApplicationConfiguration.class.isInstance(x)).findFirst().orElse(null);

        String cl = "";
        if(rc!=null) {
            RemoteJavaApplicationCommandLineState cls = new RemoteJavaApplicationCommandLineState<>(rc, env);

            GeneralCommandLine gcl = cls.getCommandLine();

            cl = gcl.getWorkDirectory().getPath() + " | ";

            cl = cl + gcl.getPreparedCommandLine();

        }

        int port = Integer.parseInt(PORT);
        RemoteDebugState state = new RemoteDebugState(getProject()/*, createRemoteConnection()*/, HOST, port, cl);

        final GenericDebuggerRunnerSettings debuggerSettings = (GenericDebuggerRunnerSettings)env.getRunnerSettings();
        if (debuggerSettings != null) {
            // sync self state with execution environment's state if available
            debuggerSettings.LOCAL = false;
            debuggerSettings.setDebugPort(state.getPort());
            debuggerSettings.setTransport(DebuggerSettings.SOCKET_TRANSPORT);



            //new ApplicationConfigurable(getProject())
        }

        return state;
    }

//    public RemoteConnection createRemoteConnection() {
//        return new RemoteConnection(true, "10.10.121.137", "123", false);
//    }
}
