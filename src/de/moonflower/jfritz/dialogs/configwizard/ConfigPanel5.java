package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;


import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
/**
 * @author Brian Jensen
 *
 * This is the panel for the call monitor settings
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel5 extends JPanel implements ActionListener{

    private static final long serialVersionUID = 1;

    public JComboBox callMonitorCombo;

	private JPanel callMonitorPane;

	public JCheckBox callMonitorAfterStartButton, soundButton, externProgramCheckBox;

	public JTextField externProgramTextField;

	public ConfigPanel5() {

		callMonitorPane = new JPanel();
		callMonitorPane.setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		callMonitorCombo = new JComboBox();
		callMonitorCombo.addItem(JFritz.getMessage("no_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("fritz_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("telnet_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("syslog_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("yac_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("callmessage_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addActionListener(this);

		callMonitorPane.add(callMonitorCombo, BorderLayout.NORTH);

		JPanel pane = new JPanel();
		callMonitorPane.add(pane, BorderLayout.CENTER);

		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		callMonitorAfterStartButton = new JCheckBox(JFritz
				.getMessage("call_monitor_prog_start")); //$NON-NLS-1$
		pane.add(callMonitorAfterStartButton, c);

		soundButton = new JCheckBox(JFritz.getMessage("play_sound")); //$NON-NLS-1$
		c.gridy = 2;
		pane.add(soundButton, c);

		externProgramCheckBox = new JCheckBox(JFritz
				.getMessage("run_external_program")); //$NON-NLS-1$
		c.gridy = 3;
		pane.add(externProgramCheckBox, c);

		externProgramTextField = new JTextField("", 40); //$NON-NLS-1$
		externProgramTextField.setMinimumSize(new Dimension(300, 20));
		c.gridy = 4;
		pane.add(externProgramTextField, c);

		add(callMonitorPane);

		callMonitorCombo.setSelectedIndex(Integer.parseInt(JFritz.getProperty(
				"option.callMonitorType", "0"))); //$NON-NLS-1$,  //$NON-NLS-2$
		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.autostartcallmonitor", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		soundButton.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"option.playSounds", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.startExternProgram", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(JFritz
				.getProperty("option.externProgram", ""))); //$NON-NLS-1$,  //$NON-NLS-2$

	}

	private void hideCallMonitorPanel() {
		callMonitorAfterStartButton.setVisible(false);
		soundButton.setVisible(false);
		externProgramCheckBox.setVisible(false);
		externProgramTextField.setVisible(false);
		callMonitorPane.repaint();
	}

	private void showCallMonitorPanel() {

		callMonitorAfterStartButton.setVisible(true);
		soundButton.setVisible(true);
		externProgramCheckBox.setVisible(true);
		externProgramTextField.setVisible(true);
		callMonitorPane.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		if ("comboboxchanged".equalsIgnoreCase(e.getActionCommand())) { //$NON-NLS-1$
			// Zur Darstellung der gewünschten Einstellungspanels
			switch (callMonitorCombo.getSelectedIndex()) {
				case 0 : {
					hideCallMonitorPanel();
					Debug.msg("Call monitor not wanted"); //$NON-NLS-1$
					//stopAllCallMonitors();
					break;
				}
				case 1 : {
					showCallMonitorPanel();
					Debug.msg("FRITZ!Box call monitor chosen"); //$NON-NLS-1$
					//stopAllCallMonitors();
					break;
				}
				case 2 : {
					showCallMonitorPanel();
					Debug.msg("Telnet call monitor chosen"); //$NON-NLS-1$
					//stopAllCallMonitors();
					break;

				}
				case 3 : {
					showCallMonitorPanel();
					Debug.msg("Syslog call monitor chosen"); //$NON-NLS-1$
					//stopAllCallMonitors();
					break;
				}
				case 4 : {
					showCallMonitorPanel();
					Debug.msg("YAC call monitor chosen"); //$NON-NLS-1$
					//stopAllCallMonitors();
					break;
				}
				case 5 : {
					showCallMonitorPanel();
					Debug.msg("Callmessage call monitor chosen"); //$NON-NLS-1$

					break;
				}
			}
		}
	}


}
