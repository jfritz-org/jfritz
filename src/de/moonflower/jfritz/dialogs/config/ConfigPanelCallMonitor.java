package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelCallMonitor extends JPanel implements ActionListener,
		ConfigPanel {

	private static final long serialVersionUID = 7267124419351267208L;

	private JComboBox callMonitorCombo;

	private JCheckBox callMonitorAfterStartButton, soundButton,
			externProgramCheckBox;

	private JTextField externProgramTextField;

	private JToggleButton startCallMonitorButton;

	private JButton callMonitorOptionsButton;

	private JDialog parent;

	private boolean showButtons;

	private ConfigPanelFritzBox fritzBoxPanel;

	public ConfigPanelCallMonitor(JDialog parent, boolean showButtons, ConfigPanelFritzBox fritzBoxPanel) {
		this.parent = parent;
		this.showButtons = showButtons;
		this.fritzBoxPanel = fritzBoxPanel;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		callMonitorCombo = new JComboBox();
		callMonitorCombo.addItem(Main.getMessage("no_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("fritz_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("telnet_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("syslog_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("yac_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("callmessage_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addActionListener(this);

		add(callMonitorCombo, BorderLayout.NORTH);

		JPanel pane = new JPanel();
		add(pane, BorderLayout.CENTER);

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
		callMonitorAfterStartButton = new JCheckBox(Main
				.getMessage("call_monitor_prog_start")); //$NON-NLS-1$
		pane.add(callMonitorAfterStartButton, c);

		soundButton = new JCheckBox(Main.getMessage("play_sound")); //$NON-NLS-1$
		c.gridy = 2;
		pane.add(soundButton, c);

		externProgramCheckBox = new JCheckBox(Main
				.getMessage("run_external_program")); //$NON-NLS-1$
		c.gridy = 3;
		pane.add(externProgramCheckBox, c);

		externProgramTextField = new JTextField("", 40); //$NON-NLS-1$
		externProgramTextField.setMinimumSize(new Dimension(300, 20));
		c.gridy = 4;
		pane.add(externProgramTextField, c);

		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		startCallMonitorButton = new JToggleButton();
		startCallMonitorButton.setActionCommand("startCallMonitor"); //$NON-NLS-1$
		startCallMonitorButton.addActionListener(this);
		startCallMonitorButton.setVisible(showButtons);
		pane.add(startCallMonitorButton, c);

		c.gridx = 1;
		c.gridy = 5;
		callMonitorOptionsButton = new JButton(Main.getMessage("config")); //$NON-NLS-1$
		callMonitorOptionsButton.setActionCommand("startCallMonitorOptions"); //$NON-NLS-1$
		callMonitorOptionsButton.addActionListener(this);
		startCallMonitorButton.setVisible(showButtons);
		pane.add(callMonitorOptionsButton, c);
	}

	public void loadSettings() {

		callMonitorCombo.setSelectedIndex(Integer.parseInt(Main.getProperty(
				"option.callMonitorType", "0"))); //$NON-NLS-1$,  //$NON-NLS-2$

		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.autostartcallmonitor", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		if (JFritz.getCallMonitor() == null) {
			startCallMonitorButton.setSelected(false);
		} else {
			startCallMonitorButton.setSelected(true);
		}
		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main
					.getProperty("option.autostartcallmonitor", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		setCallMonitorButtonPushed(startCallMonitorButton.isSelected());


		soundButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.playSounds", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.startExternProgram", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(Main
				.getProperty("option.externProgram", ""))); //$NON-NLS-1$,  //$NON-NLS-2$

		soundButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.playSounds", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.startExternProgram", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(Main
				.getProperty("option.externProgram", ""))); //$NON-NLS-1$,  //$NON-NLS-2$


	}

	public void saveSettings() {
		// save the various settings
		Main.setProperty("option.playSounds", Boolean.toString(soundButton //$NON-NLS-1$
				.isSelected()));
		Main.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
				.toString(externProgramCheckBox.isSelected()));
		Main.setProperty("option.externProgram", JFritzUtils //$NON-NLS-1$
				.convertSpecialChars(externProgramTextField.getText()));


		Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));

		Main.setProperty("option.callmonitorStarted", Boolean //$NON-NLS-1$
				.toString(startCallMonitorButton.isSelected()));

		Main.setProperty("option.autostartcallmonitor", Boolean //$NON-NLS-1$
				.toString(callMonitorAfterStartButton.isSelected()));
		Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));


		Main.setProperty("option.playSounds", Boolean.toString(soundButton //$NON-NLS-1$
				.isSelected()));

		Main.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
				.toString(externProgramCheckBox.isSelected()));
		Main
				.setProperty(
						"option.externProgram", JFritzUtils.convertSpecialChars(externProgramTextField //$NON-NLS-1$
										.getText()));
	}

	private void hideCallMonitorPanel() {
		startCallMonitorButton.setVisible(false);
		callMonitorOptionsButton.setVisible(false);
		callMonitorAfterStartButton.setVisible(false);
		soundButton.setVisible(false);
		externProgramCheckBox.setVisible(false);
		externProgramTextField.setVisible(false);
		repaint();
	}

	private void showCallMonitorPanel() {
		startCallMonitorButton.setVisible(showButtons);
		callMonitorOptionsButton.setVisible(showButtons);
		callMonitorAfterStartButton.setVisible(true);
		soundButton.setVisible(true);
		externProgramCheckBox.setVisible(true);
		externProgramTextField.setVisible(true);
		repaint();
	}

	protected void stopAllCallMonitors() {
		if (startCallMonitorButton.isSelected()) {
			setCallMonitorButtonPushed(false);
			JFritz.stopCallMonitor();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("comboboxchanged".equalsIgnoreCase(e.getActionCommand())) { //$NON-NLS-1$
			// Zur Darstellung der gewünschten Einstellungspanels
			switch (callMonitorCombo.getSelectedIndex()) {
			case 0: {
				hideCallMonitorPanel();
				Debug.msg("Call monitor not wanted"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 1: {
				showCallMonitorPanel();
				Debug.msg("FRITZ!Box call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 2: {
				showCallMonitorPanel();
				Debug.msg("Telnet call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;

			}
			case 3: {
				showCallMonitorPanel();
				Debug.msg("Syslog call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 4: {
				showCallMonitorPanel();
				Debug.msg("YAC call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 5: {
				showCallMonitorPanel();
				Debug.msg("Callmessage call monitor chosen"); //$NON-NLS-1$

				break;
			}

			}
		} else if ("startCallMonitor".equals(e.getActionCommand())) { //$NON-NLS-1$
			// Aktion des StartCallMonitorButtons
			Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
					.valueOf(callMonitorCombo.getSelectedIndex()));

			JFritz.getFritzBox().setAddress(fritzBoxPanel.getAddress());
			JFritz.getFritzBox().setPassword(fritzBoxPanel.getPassword());
			JFritz.getFritzBox().setPort(fritzBoxPanel.getPort());
			try {
				JFritz.getFritzBox().detectFirmware();
				JFritz.getJframe().switchMonitorButton();
				// Speichere den Button-Status
				Main.setProperty("option.callmonitorStarted", Boolean //$NON-NLS-1$
						.toString(startCallMonitorButton.isSelected()));
				setCallMonitorButtonPushed(startCallMonitorButton.isSelected());
			} catch (WrongPasswordException e1) {
				JFritz.errorMsg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
				startCallMonitorButton.setSelected(!startCallMonitorButton.isSelected());
			} catch (IOException e1) {
				JFritz.errorMsg(Main.getMessage("box.address_wrong")); //$NON-NLS-1$
				startCallMonitorButton.setSelected(!startCallMonitorButton.isSelected());
			} catch (InvalidFirmwareException e1) {
				JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
				startCallMonitorButton.setSelected(!startCallMonitorButton.isSelected());
			}
		} else if ("startCallMonitorOptions".equals(e //$NON-NLS-1$
				.getActionCommand())) {
			CallMonitorConfigDialog callMonitorConfigDialog = null;
			switch (callMonitorCombo.getSelectedIndex()) {
			case 1:
				callMonitorConfigDialog = new FRITZBOXConfigDialog(parent);
				break;
			case 2:
				callMonitorConfigDialog = new TelnetConfigDialog(parent);
				break;
			case 3:
				callMonitorConfigDialog = new SyslogConfigDialog(parent);
				break;
			case 4:
				callMonitorConfigDialog = new YacConfigDialog(parent);
				break;
			case 5:
				callMonitorConfigDialog = new CallmessageConfigDialog(parent);
				break;

			}
			if (callMonitorConfigDialog != null) {
				callMonitorConfigDialog.showConfigDialog();
				callMonitorConfigDialog.dispose();
			}

		}
	}

	/**
	 * Let startCallMonitorButtons start or stop callMonitor Changes caption of
	 * buttons and their status
	 *
	 * @param option
	 *            CALLMONITOR_START or CALLMONITOR_STOP
	 */
	public void setCallMonitorButtonPushed(boolean isPushed) {
		if (isPushed) {
			startCallMonitorButton
					.setText(Main.getMessage("stop_call_monitor")); //$NON-NLS-1$
			startCallMonitorButton.setSelected(true);
			JFritz.getJframe().getMonitorButton().setSelected(true);
		} else {
			startCallMonitorButton.setText(Main
					.getMessage("start_call_monitor")); //$NON-NLS-1$
			startCallMonitorButton.setSelected(false);
			JFritz.getJframe().getMonitorButton().setSelected(false);
		}
	}
}
