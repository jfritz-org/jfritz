package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;
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

	private JToggleButton startStopCallMonitorButton;

	private JButton callMonitorOptionsButton;

	private JDialog parent;

	private boolean showButtons;

	private ConfigPanelFritzBox fritzBoxPanel;

	private String configPath;

	private boolean loadingSettingsDone = false;

	private Vector<CallMonitorStatusListener> stateListener;

	public ConfigPanelCallMonitor(JDialog parent, boolean showButtons,
								  ConfigPanelFritzBox fritzBoxPanel,
								  Vector<CallMonitorStatusListener> listener) {
		this.parent = parent;
		this.showButtons = showButtons;
		this.fritzBoxPanel = fritzBoxPanel;
		this.stateListener = listener;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
		callMonitorCombo = new JComboBox();
		callMonitorCombo.addItem(Main.getMessage("no_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("fritz_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("yac_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("callmessage_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addActionListener(this);

		add(callMonitorCombo, BorderLayout.NORTH);

		JPanel pane = new JPanel();
		add(new JScrollPane(pane), BorderLayout.CENTER);

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
		startStopCallMonitorButton = new JToggleButton();
		startStopCallMonitorButton.setActionCommand("startStopCallMonitor"); //$NON-NLS-1$
		startStopCallMonitorButton.addActionListener(this);
		startStopCallMonitorButton.setVisible(showButtons);
		pane.add(startStopCallMonitorButton, c);

		c.gridx = 1;
		c.gridy = 5;
		callMonitorOptionsButton = new JButton(Main.getMessage("config")); //$NON-NLS-1$
		callMonitorOptionsButton.setActionCommand("startCallMonitorOptions"); //$NON-NLS-1$
		callMonitorOptionsButton.addActionListener(this);
		pane.add(callMonitorOptionsButton, c);
	}

	public void loadSettings() {
		loadingSettingsDone = false;

		int selectedCallMonitorType = Integer.parseInt(Main.getProperty("option.callMonitorType"));
		if (callMonitorCombo.getSelectedIndex() != selectedCallMonitorType)
		{
			callMonitorCombo.setSelectedIndex(selectedCallMonitorType);
		}

		if ((fritzBoxPanel != null)
			&& (fritzBoxPanel.getFritzBox() != null))
		{
			if (fritzBoxPanel.getFritzBox().isCallMonitorConnected())
			{
				startStopCallMonitorButton.setSelected(true);
			}
			else
			{
				startStopCallMonitorButton.setSelected(false);
			}
		}

		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main
					.getProperty("option.autostartcallmonitor"))); //$NON-NLS-1$,  //$NON-NLS-2$

		setCallMonitorButtonPushed(startStopCallMonitorButton.isSelected());

		soundButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.playSounds"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.startExternProgram"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(Main
				.getProperty("option.externProgram"))); //$NON-NLS-1$,  //$NON-NLS-2$

		loadingSettingsDone = true;
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

		Main.setProperty("option.autostartcallmonitor", Boolean //$NON-NLS-1$
				.toString(callMonitorAfterStartButton.isSelected()));
		Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));


		Main.setProperty("option.playSounds", Boolean.toString(soundButton //$NON-NLS-1$
				.isSelected()));

		Main.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
				.toString(externProgramCheckBox.isSelected()));
		Main.setProperty("option.externProgram",  //$NON-NLS-1$
				JFritzUtils.convertSpecialChars(externProgramTextField.getText()));
	}

	private void hideCallMonitorPanel() {
		startStopCallMonitorButton.setVisible(false);
		callMonitorOptionsButton.setVisible(false);
		callMonitorAfterStartButton.setVisible(false);
		soundButton.setVisible(false);
		externProgramCheckBox.setVisible(false);
		externProgramTextField.setVisible(false);
		repaint();
	}

	private void showCallMonitorPanel() {
		startStopCallMonitorButton.setVisible(showButtons);
		callMonitorOptionsButton.setVisible(showButtons);
		callMonitorAfterStartButton.setVisible(true);
		soundButton.setVisible(true);
		externProgramCheckBox.setVisible(true);
		externProgramTextField.setVisible(true);
		repaint();
	}

	protected void stopAllCallMonitors() {
		if ((fritzBoxPanel != null)
			&& (fritzBoxPanel.getFritzBox() != null)
			&& loadingSettingsDone)
		{
			fritzBoxPanel.getFritzBox().stopCallMonitor(stateListener);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("comboboxchanged".equalsIgnoreCase(e.getActionCommand())) { //$NON-NLS-1$
			// Zur Darstellung der gewünschten Einstellungspanels
			switch (callMonitorCombo.getSelectedIndex()) {
			case 0: {
				hideCallMonitorPanel();
				Debug.info("Call monitor not wanted"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 1: {
				showCallMonitorPanel();
				Debug.info("FRITZ!Box call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 2: {
				showCallMonitorPanel();
				Debug.info("YAC call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 3: {
				showCallMonitorPanel();
				Debug.info("Callmessage call monitor chosen"); //$NON-NLS-1$

				break;
			}

			}
		} else if ("startStopCallMonitor".equals(e.getActionCommand())) { //$NON-NLS-1$
			// Aktion des StartCallMonitorButtons
			Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
					.valueOf(callMonitorCombo.getSelectedIndex()));

		    Container c = getPanel(); // get the window's content pane
			try {
				c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				fritzBoxPanel.detectBoxType();
				this.startStopCallMonitor();
			} catch (WrongPasswordException e1) {
				Debug.errDlg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
				startStopCallMonitorButton.setSelected(!startStopCallMonitorButton.isSelected());
			} catch (IOException e1) {
				Debug.errDlg(Main.getMessage("box.not_found")); //$NON-NLS-1$
				startStopCallMonitorButton.setSelected(!startStopCallMonitorButton.isSelected());
			} catch (InvalidFirmwareException e1) {
				Debug.errDlg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
				startStopCallMonitorButton.setSelected(!startStopCallMonitorButton.isSelected());
			}
			c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if ("startCallMonitorOptions".equals(e //$NON-NLS-1$
				.getActionCommand())) {
			CallMonitorConfigDialog callMonitorConfigDialog = null;
			switch (callMonitorCombo.getSelectedIndex()) {
			case 1:
				callMonitorConfigDialog = new FRITZBOXConfigDialog(parent);
				break;
			case 2:
				callMonitorConfigDialog = new YacConfigDialog(parent);
				break;
			case 3:
				callMonitorConfigDialog = new CallmessageConfigDialog(parent);
				break;

			}
			if (callMonitorConfigDialog != null) {
				callMonitorConfigDialog.pack();
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
			startStopCallMonitorButton
					.setText(Main.getMessage("stop_call_monitor")); //$NON-NLS-1$
			startStopCallMonitorButton.setSelected(true);
		} else {
			startStopCallMonitorButton.setText(Main
					.getMessage("start_call_monitor")); //$NON-NLS-1$
			startStopCallMonitorButton.setSelected(false);
		}
	}

	public void setPath(String path)
	{
		this.configPath = path;
	}

	public String getPath()
	{
		return configPath;
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Anrufmonitor";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	private void startStopCallMonitor()
	{
		if ((fritzBoxPanel != null)
			&& (fritzBoxPanel.getFritzBox() != null))
		{
			if (fritzBoxPanel.getFritzBox().isCallMonitorConnected())
			{
				fritzBoxPanel.getFritzBox().stopCallMonitor(stateListener);
			}
			else
			{
				fritzBoxPanel.getFritzBox().startCallMonitor(stateListener);
			}
			// Speichere den Button-Status
			setCallMonitorButtonPushed(startStopCallMonitorButton.isSelected());
		}
	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}

}
