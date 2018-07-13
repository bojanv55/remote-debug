package me.vukas;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.JavaSdkVersionUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.SideBorder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class RemoteDebugConfigurable extends SettingsEditor<RemoteDebugConfiguration> {
    private enum Mode {
        ATTACH("Attach to remote JVM"),
        LISTEN("Listen to remote JVM");

        private final String text;
        Mode(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private enum Transport {
        SOCKET("Socket"),
        SHMEM("Shared memory");

        private final String text;
        Transport(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private enum JDKVersionItem {
        JDK9(JavaSdkVersion.JDK_1_9) {
            @Override
            String getLaunchCommandLine(RemoteConnection connection) {
                String commandLine = JDK5to8.getLaunchCommandLine(connection);
                if (connection.isUseSockets() && !connection.isServerMode()) {
                    commandLine = commandLine.replace(connection.getAddress(), "*:" + connection.getAddress());
                }
                return commandLine;
            }

            @Override
            public String toString() {
                return "JDK 9 or later";
            }
        },
        JDK5to8(JavaSdkVersion.JDK_1_5)  {
            @Override
            String getLaunchCommandLine(RemoteConnection connection) {
                return connection.getLaunchCommandLine().replace("-Xdebug", "").replace("-Xrunjdwp:", "-agentlib:jdwp=").trim();
            }

            @Override
            public String toString() {
                return "JDK 5 - 8";
            }
        },
        JDK1_4(JavaSdkVersion.JDK_1_4) {
            @Override
            String getLaunchCommandLine(RemoteConnection connection) {
                return connection.getLaunchCommandLine();
            }

            @Override
            public String toString() {
                return "JDK 1.4.x";
            }
        },
        JDK1_3(JavaSdkVersion.JDK_1_3) {
            @Override
            String getLaunchCommandLine(RemoteConnection connection) {
                return "-Xnoagent -Djava.compiler=NONE " + connection.getLaunchCommandLine();
            }

            @Override
            public String toString() {
                return "JDK 1.3.x or earlier";
            }
        };

        private final JavaSdkVersion myVersion;

        JDKVersionItem(JavaSdkVersion version) {
            myVersion = version;
        }

        abstract String getLaunchCommandLine(RemoteConnection connection);
    }

    private final JPanel          mainPanel;
    private final JTextArea       myArgsArea = new JTextArea();
    private final JComboBox<Mode> myModeCombo = new ComboBox<>(Mode.values());
    private final JComboBox<Transport> myTransportCombo = new ComboBox<>(Transport.values());

    private final ConfigurationModuleSelector myModuleSelector;

    private final JTextField myHostName = new JTextField();
    private final JTextField myAddress = new JTextField();

    public RemoteDebugConfigurable(Project project) {
        myTransportCombo.setSelectedItem(Transport.SOCKET);


        GridBagConstraints gc = new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                JBUI.insets(4, 0, 0, 8), 0, 0);
        mainPanel = createModePanel(gc);

        JavaSdkVersion version = JavaSdkVersionUtil.getJavaSdkVersion(ProjectRootManager.getInstance(project).getProjectSdk());
        JDKVersionItem vi = version != null ?
                Arrays.stream(JDKVersionItem.values()).filter(v -> version.isAtLeast(v.myVersion)).findFirst().orElse(JDKVersionItem.JDK9)
                : JDKVersionItem.JDK9;

        myArgsArea.setLineWrap(true);
        myArgsArea.setWrapStyleWord(true);
        myArgsArea.setRows(2);
        myArgsArea.setEditable(false);
        myArgsArea.setBorder(new SideBorder(JBColor.border(), SideBorder.ALL));
        myArgsArea.setMinimumSize(myArgsArea.getPreferredSize());

        updateArgsText(vi);


        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 5;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;
        gc.insets = JBUI.insetsTop(10);


        ModuleDescriptionsComboBox myModuleCombo = new ModuleDescriptionsComboBox();
        myModuleCombo.allowEmptySelection("<whole project>");
        myModuleSelector = new ConfigurationModuleSelector(project, myModuleCombo);

        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 5;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;
        gc.insets = JBUI.insetsTop(21);

        gc.gridy++;
        gc.fill = GridBagConstraints.REMAINDER;
        gc.insets = JBUI.emptyInsets();
        gc.weighty = 1.0;
        mainPanel.add(new JPanel(), gc);



    }

    private void updateArgsText(@NotNull JDKVersionItem vi) {
        boolean useSockets = myTransportCombo.getSelectedItem() == Transport.SOCKET;

        RemoteConnection connection = new RemoteConnection(useSockets, myHostName.getText().trim(),
                useSockets ? "" : myAddress.getText().trim(),
                myModeCombo.getSelectedItem() == Mode.LISTEN
        );

        myArgsArea.setText(vi.getLaunchCommandLine(connection));
    }

    @Override
    protected void resetEditorFrom(@NotNull RemoteDebugConfiguration rc) {


        myModuleSelector.reset(rc);
    }

    @Override
    protected void applyEditorTo(@NotNull RemoteDebugConfiguration rc) throws ConfigurationException {

        myModuleSelector.applyTo(rc);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    private JPanel createModePanel(GridBagConstraints gc) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel modeLabel = new JLabel("Debugger mode:");
        JLabel transportLabel = new JLabel("Transport:");
        JLabel hostLabel = new JLabel("Host:");

        panel.add(modeLabel, gc);

        gc.gridx += 2;
        gc.gridwidth = 1;
        gc.insets = JBUI.insetsTop(4);
        panel.add(myModeCombo, gc);

        gc.gridx++;
        gc.weightx = 1.0;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.REMAINDER;
        panel.add(new JPanel(), gc);

        if (SystemInfo.isWindows) {
            JLabel addressLabel = new JLabel("Address:");

            addressLabel.setVisible(false);
            myAddress.setVisible(false);

            gc.gridx = 0;
            gc.gridy++;
            gc.weightx = 0.0;
            gc.gridwidth = 2;
            gc.fill = GridBagConstraints.NONE;
            gc.insets = JBUI.insets(4, 0, 0, 8);
            panel.add(transportLabel, gc);

            gc.gridx += 2;
            gc.gridwidth = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = JBUI.insetsTop(4);
            panel.add(myTransportCombo, gc);

            gc.gridx++;
            gc.weightx = 1.0;
            gc.gridwidth = 2;
            gc.fill = GridBagConstraints.REMAINDER;
            panel.add(new JPanel(), gc);

            gc.gridy++;
            gc.gridx = 0;
            gc.weightx = 0.0;
            gc.gridwidth = 1;
            gc.insets = JBUI.insets(4, 0, 0, 8);
            gc.fill = GridBagConstraints.NONE;
            panel.add(addressLabel, gc);

            gc.gridx++;
            gc.gridwidth = 2;
            gc.insets = JBUI.insetsTop(4);
            gc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(myAddress, gc);

            myTransportCombo.addActionListener(e -> {
                boolean isShmem = myTransportCombo.getSelectedItem() == Transport.SHMEM;

                hostLabel.setVisible(!isShmem);
                myHostName.setVisible(!isShmem);

                addressLabel.setVisible(isShmem);
                myAddress.setVisible(isShmem);
            });
        }

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        gc.gridwidth = 1;
        gc.insets = JBUI.insets(4, 0, 0, 8);
        gc.fill = GridBagConstraints.NONE;
        panel.add(hostLabel, gc);

        gc.gridx++;
        gc.gridwidth = 2;
        gc.insets = JBUI.insetsTop(4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(myHostName, gc);

        gc.gridx += 2;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = JBUI.insets(4, 20, 0, 8);

        gc.gridx++;
        gc.insets = JBUI.insetsTop(4);

        return panel;
    }
}
