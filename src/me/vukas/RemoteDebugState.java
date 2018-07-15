package me.vukas;

import com.intellij.debugger.engine.RemoteDebugProcessHandler;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RemoteState;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RemoteDebugState implements RemoteState {
    private final Project a;
    private final RemoteConnection b;

    public RemoteDebugState(Project var1, RemoteConnection var2) {
        this.a = var1;
        this.b = var2;
    }

    public ExecutionResult execute(Executor var1, @NotNull ProgramRunner var2) throws ExecutionException {
        ConsoleViewImpl var3 = new ConsoleViewImpl(this.a, false);
        RemoteDebugProcessHandler var4 = new RemoteDebugProcessHandler(this.a);
        var3.attachToProcess(var4);

        //https://github.com/JetBrains/intellij-community/blob/7a4aac4280588c4fe9a258ea9b15085588c6714c/java/execution/impl/src/com/intellij/execution/remote/RemoteConfigurable.java
        //app https://github.com/JetBrains/intellij-community/tree/7a4aac4280588c4fe9a258ea9b15085588c6714c/java/execution/impl/src/com/intellij/execution/application
        var3.print("bagababgabgbag \n", ConsoleViewContentType.NORMAL_OUTPUT);

        return new DefaultExecutionResult(var3, var4);
    }

    public RemoteConnection getRemoteConnection() {
        return this.b;
    }

    private static ExecutionException b(ExecutionException var0) {
        return var0;
    }
}
