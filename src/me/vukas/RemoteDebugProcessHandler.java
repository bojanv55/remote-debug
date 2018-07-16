package me.vukas;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;

public class RemoteDebugProcessHandler extends ProcessHandler {
	private final Project a;
	private PrintWriter out;

	public RemoteDebugProcessHandler(Project var1, PrintWriter out) {
		this.a = var1;
		this.out = out;
	}

	public void startNotify() {
		final DebugProcess var1 = DebuggerManager.getInstance(this.a).getDebugProcess(this);
		DebugProcessListener var2 = new DebugProcessListener() {
			public void processDetached(DebugProcess var1x, boolean var2) {
				var1.removeDebugProcessListener(this);
				RemoteDebugProcessHandler.this.notifyProcessDetached();
				out.println("[STOP]");
			}
		};
		var1.addDebugProcessListener(var2);

		try {
			super.startNotify();
		} finally {
			if (var1.isDetached()) {
				var1.removeDebugProcessListener(var2);
				this.notifyProcessDetached();
			}

		}

	}

	protected void destroyProcessImpl() {
		DebugProcess var1 = DebuggerManager.getInstance(this.a).getDebugProcess(this);
		if (var1 != null) {
			var1.stop(true);
		}

	}

	protected void detachProcessImpl() {
		DebugProcess var1 = DebuggerManager.getInstance(this.a).getDebugProcess(this);
		if (var1 != null) {
			var1.stop(false);
		}

	}

	public boolean detachIsDefault() {
		return true;
	}

	public OutputStream getProcessInput() {
		return null;
	}
}
