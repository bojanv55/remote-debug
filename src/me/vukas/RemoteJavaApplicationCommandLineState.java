package me.vukas;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.runners.ExecutionEnvironment;

public class RemoteJavaApplicationCommandLineState<T extends ApplicationConfiguration> extends ApplicationConfiguration.JavaApplicationCommandLineState<T> {
	public RemoteJavaApplicationCommandLineState(@NotNull T t, ExecutionEnvironment executionEnvironment) {
		super(t, executionEnvironment);
	}

	public GeneralCommandLine getCommandLine() throws ExecutionException {
		return this.createCommandLine();
	}
}
