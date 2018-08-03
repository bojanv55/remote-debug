package me.vukas;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jetbrains.annotations.NotNull;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SideBorder;
import com.intellij.util.ui.JBUI;

public class RemoteDebugConfigurable extends SettingsEditor<RemoteDebugConfiguration> {


    private final JPanel          mainPanel;
    private final JTextArea       myArgsArea = new JTextArea();

    private List<String> configItems = new ArrayList<>(Collections.singletonList("Select..."));
    private ComboBoxModel<String> appConfigs = new CollectionComboBoxModel<>(configItems);

    private final ConfigurationModuleSelector myModuleSelector;

    private final JComboBox<String> myModeCombo = new ComboBox<>(appConfigs);
    //private final JTextField myACN = new JTextField();
    private final JTextField myHostName = new JTextField();
    private final JTextField myPort = new JTextField();
    private final JTextField myAddress = new JTextField();

    private Project p;

    public RemoteDebugConfigurable(Project project) {

        this.p = project;

        RunManagerImpl manager = RunManagerImpl.getInstanceImpl(project);
        List<String> rc = manager.getAllConfigurationsList()
            .stream().filter(ApplicationConfiguration.class::isInstance).map(RunProfile::getName).collect(Collectors.toList());

        //configItems =
        configItems.addAll(rc);


        GridBagConstraints gc = new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                JBUI.insets(4, 0, 0, 8), 0, 0);
        mainPanel = createModePanel(gc);

        myArgsArea.setLineWrap(true);
        myArgsArea.setWrapStyleWord(true);
        myArgsArea.setRows(2);
        myArgsArea.setEditable(false);
        myArgsArea.setBorder(new SideBorder(JBColor.border(), SideBorder.ALL));
        myArgsArea.setMinimumSize(myArgsArea.getPreferredSize());

        updateArgsText();

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

        DocumentListener textUpdateListener = new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                //updateArgsText(ddl.getChosenItem());
            }
        };

        myHostName.getDocument().addDocumentListener(textUpdateListener);



    }

    private void updateArgsText() {


        RemoteConnection connection = new RemoteConnection(true, myHostName.getText().trim(),"", false);

    }

    @Override
    protected void resetEditorFrom(@NotNull RemoteDebugConfiguration rc) {
        myModuleSelector.reset(rc);
        myModeCombo.setSelectedItem(rc.APPCONF);
        myHostName.setText(rc.HOST);
        myPort.setText(rc.PORT);
    }

    @Override
    protected void applyEditorTo(@NotNull RemoteDebugConfiguration rc) throws ConfigurationException {
        rc.APPCONF = myModeCombo.getSelectedItem().toString().trim();
        if (rc.APPCONF.isEmpty()) {
            rc.APPCONF = null;
        }
        rc.HOST = myHostName.getText().trim();
        if (rc.HOST.isEmpty()) {
            rc.HOST = null;
        }
        rc.PORT = myPort.getText().trim();
        if (rc.PORT.isEmpty()) {
            rc.PORT = null;
        }
        myModuleSelector.applyTo(rc);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    private JPanel createModePanel(GridBagConstraints gc) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel acnLabel = new JLabel("Application Configuration Name:");
        JLabel hostLabel = new JLabel("Host:");
        JLabel portLabel = new JLabel("Port:");

        gc.gridx += 2;
        gc.gridwidth = 1;
        gc.insets = JBUI.insetsTop(4);

        gc.gridx++;
        gc.weightx = 1.0;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JPanel jp = new JPanel();
        panel.add(jp, gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        gc.gridwidth = 1;
        gc.insets = JBUI.insets(4, 0, 0, 8);
        gc.fill = GridBagConstraints.NONE;
        panel.add(acnLabel, gc);

        gc.gridx++;
        gc.gridwidth = 2;
        gc.insets = JBUI.insetsTop(4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(myModeCombo, gc);

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

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        gc.gridwidth = 1;
        gc.insets = JBUI.insets(4, 0, 0, 8);
        gc.fill = GridBagConstraints.NONE;
        panel.add(portLabel, gc);

        gc.gridx++;
        gc.gridwidth = 2;
        gc.insets = JBUI.insetsTop(4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(myPort, gc);

        gc.gridx += 2;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = JBUI.insets(4, 20, 0, 8);

        gc.gridx++;
        gc.insets = JBUI.insetsTop(4);

        return panel;
    }
}
