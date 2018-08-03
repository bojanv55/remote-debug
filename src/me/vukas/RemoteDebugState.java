package me.vukas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RemoteState;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

public class RemoteDebugState implements RemoteState {
    private final Project a;
    private RemoteConnection b;
    private Pattern patternPort = Pattern.compile("<START_DEBUG_PROCESS_PORT>((?!</START_DEBUG_PROCESS_PORT>$).*)</START_DEBUG_PROCESS_PORT>");
    private String port;
    private String cl;

    public String getPort(){
        return this.port;
    }

    private ConsoleViewImpl var3;
    private final AnsiEscapeDecoder myAnsiEscapeDecoder = new AnsiEscapeDecoder();

    public RemoteDebugState(Project var1/*, RemoteConnection var2*/, String HOST, Integer PORT, String cl) {
        this.a = var1;
        this.cl = cl;
        /*this.b = var2;*/



        try {
            clientSocket = new Socket(HOST, PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("<START_DEBUG_PROCESS_PORT />");
            Matcher m;
            if ((m = patternPort.matcher(in.readLine())).matches()) {
                this.port = m.group(1);
                this.b = new RemoteConnection(true, HOST, this.port, false);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public ExecutionResult execute(Executor var1, @NotNull ProgramRunner var2) throws ExecutionException {



        var3 = new ConsoleViewImpl(this.a, false);

        try {


            out.println("<START_DEBUG_PROCESS>"+this.cl.replace("\n", " ").replace("\r", " ")+"</START_DEBUG_PROCESS>");

            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        //var3.print(inputLine + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                        myAnsiEscapeDecoder.escapeText(inputLine + "\n", ProcessOutputTypes.STDOUT, (@NotNull String text, Key outputType) -> {
                            var3.print(text, ConsoleViewContentType.getConsoleViewType(outputType));
                        });
                    }
                }
                catch (Exception e){

                }
            });

            //Thread.sleep(10000); //!!!! WAIT FOR SERVER TO START DEBUG JVM
//            Matcher m;
//            while ((m = patternPort.matcher(in.readLine())).matches()) {
//                String port = m.group(1);
//                this.b = new RemoteConnection(true, "10.10.121.137", port, false);
//            }

            //out.println("[START]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        RemoteDebugProcessHandler var4 = new RemoteDebugProcessHandler(this.a, out);



        var3.attachToProcess(var4);


        //https://github.com/JetBrains/intellij-community/blob/7a4aac4280588c4fe9a258ea9b15085588c6714c/java/execution/impl/src/com/intellij/execution/remote/RemoteConfigurable.java
        //app https://github.com/JetBrains/intellij-community/tree/7a4aac4280588c4fe9a258ea9b15085588c6714c/java/execution/impl/src/com/intellij/execution/application
        //var3.print("bagababgabgbag \n", ConsoleViewContentType.NORMAL_OUTPUT);

        return new DefaultExecutionResult(var3, var4);
    }

    public RemoteConnection getRemoteConnection() {
        return this.b;
    }

    private static ExecutionException b(ExecutionException var0) {
        return var0;
    }
}
