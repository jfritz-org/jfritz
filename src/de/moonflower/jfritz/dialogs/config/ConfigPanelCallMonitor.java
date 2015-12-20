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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.robotniko.fboxlib.exceptions.FirmwareNotDetectedException;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class ConfigPanelCallMonitor extends JPanel implements ActionListener,
		ConfigPanel {
	private final static Logger log = Logger.getLogger(ConfigPanelCallMonitor.class);

	private static final long serialVersionUID = 7267124419351267208L;

	private JComboBox<String> callMonitorCombo;

	private JCheckBox callMonitorAfterStartButton, soundButton,
			externProgramCheckBox;

	private JLabel externProgramArgsLabel, externProgramLabel;
	private JTextField externProgramTextField;
	private JTextField externProgramArgsTextField;

	private JToggleButton startStopCallMonitorButton;

	private JButton callMonitorOptionsButton;

	private JDialog parent;

	private boolean showButtons;

	private ConfigPanelFritzBoxIP fritzBoxPanel;

	private String configPath;

	private boolean loadingSettingsDone = false;

	private Vector<CallMonitorStatusListener> stateListener;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelCallMonitor(JDialog parent, boolean showButtons,
								  ConfigPanelFritzBoxIP fritzBoxPanel,
								  Vector<CallMonitorStatusListener> listener) {
		this.parent = parent;
		this.showButtons = showButtons;
		this.fritzBoxPanel = fritzBoxPanel;
		this.stateListener = listener;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
		callMonitorCombo = new JComboBox<String>();
		callMonitorCombo.addItem(messages.getMessage("no_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(messages.getMessage("fritz_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(messages.getMessage("yac_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(messages.getMessage("callmessage_call_monitor")); //$NON-NLS-1$
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
		callMonitorAfterStartButton = new JCheckBox(messages.getMessage("call_monitor_prog_start")); //$NON-NLS-1$
		pane.add(callMonitorAfterStartButton, c);

		soundButton = new JCheckBox(messages.getMessage("play_sound")); //$NON-NLS-1$
		c.gridy = 2;
		pane.add(soundButton, c);

		externProgramCheckBox = new JCheckBox(messages.getMessage("run_external_program")); //$NON-NLS-1$
		externProgramCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateExternalProgramUI();
			}			
		});
		c.gridy = 3;
		pane.add(externProgramCheckBox, c);

		externProgramLabel = new JLabel("          " + messages.getMessage("external_program"));
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		pane.add(externProgramLabel, c);
		
		externProgramTextField = new JTextField("", 40); //$NON-NLS-1$
		externProgramTextField.setMinimumSize(new Dimension(300, 20));
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		pane.add(externProgramTextField, c);

		externProgramArgsLabel = new JLabel("          " + messages.getMessage("external_program_args"));
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		pane.add(externProgramArgsLabel, c);

		externProgramArgsTextField = new JTextField("", 40); //$NON-NLS-1$
		externProgramArgsTextField.setMinimumSize(new Dimension(300, 20));
		c.gridx = 1;
		c.gridy = 5;
		pane.add(externProgramArgsTextField, c);

		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		startStopCallMonitorButton = new JToggleButton();
		startStopCallMonitorButton.setActionCommand("startStopCallMonitor"); //$NON-NLS-1$
		startStopCallMonitorButton.addActionListener(this);
		startStopCallMonitorButton.setVisible(showButtons);
		pane.add(startStopCallMonitorButton, c);

		c.gridx = 1;
		c.gridy = 6;
		callMonitorOptionsButton = new JButton(messages.getMessage("config")); //$NON-NLS-1$
		callMonitorOptionsButton.setActionCommand("startCallMonitorOptions"); //$NON-NLS-1$
		callMonitorOptionsButton.addActionListener(this);
		pane.add(callMonitorOptionsButton, c);
	}

	public void loadSettings() {
		loadingSettingsDone = false;

		int selectedCallMonitorType = Integer.parseInt(properties.getProperty("option.callMonitorType"));
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

		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(properties
					.getProperty("option.autostartcallmonitor"))); //$NON-NLS-1$,  //$NON-NLS-2$

		setCallMonitorButtonPushed(startStopCallMonitorButton.isSelected());

		soundButton.setSelected(JFritzUtils.parseBoolean(properties.getProperty(
				"option.playSounds"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.startExternProgram"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(properties
				.getProperty("option.externProgram"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramArgsTextField.setText(JFritzUtils.deconvertSpecialChars(properties
				.getProperty("option.externProgramArgs"))); //$NON-NLS-1$,  //$NON-NLS-2$
		
		updateExternalProgramUI();
		loadingSettingsDone = true;
	}

	public void saveSettings() {
		// save the various settings
		properties.setProperty("option.playSounds", Boolean.toString(soundButton //$NON-NLS-1$
				.isSelected()));
		properties.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
				.toString(externProgramCheckBox.isSelected()));
		properties.setProperty("option.externProgram", JFritzUtils //$NON-NLS-1$
				.convertSpecialChars(externProgramTextField.getText()));
		properties.setProperty("option.externProgramArgs", JFritzUtils //$NON-NLS-1$
				.convertSpecialChars(externProgramArgsTextField.getText()));


		properties.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));

		properties.setProperty("option.autostartcallmonitor", Boolean //$NON-NLS-1$
				.toString(callMonitorAfterStartButton.isSelected()));
		properties.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));
	}

	private void hideCallMonitorPanel() {
		startStopCallMonitorButton.setVisible(false);
		callMonitorOptionsButton.setVisible(false);
		callMonitorAfterStartButton.setVisible(false);
		soundButton.setVisible(false);
		externProgramCheckBox.setVisible(false);
		externProgramLabel.setVisible(false);
		externProgramTextField.setVisible(false);
		externProgramArgsLabel.setVisible(false);
		externProgramArgsTextField.setVisible(false);
		repaint();
	}

	private void showCallMonitorPanel() {
		startStopCallMonitorButton.setVisible(showButtons);
		callMonitorOptionsButton.setVisible(showButtons);
		callMonitorAfterStartButton.setVisible(true);
		soundButton.setVisible(true);
		externProgramCheckBox.setVisible(true);
		externProgramLabel.setVisible(true);
		externProgramTextField.setVisible(true);
		externProgramArgsLabel.setVisible(true);
		externProgramArgsTextField.setVisible(true);
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
				log.info("Call monitor not wanted"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 1: {
				showCallMonitorPanel();
				log.info("FRITZ!Box call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 2: {
				showCallMonitorPanel();
				log.info("YAC call monitor chosen"); //$NON-NLS-1$
				stopAllCallMonitors();
				break;
			}
			case 3: {
				showCallMonitorPanel();
				log.info("Callmessage call monitor chosen"); //$NON-NLS-1$

				break;
			}

			}
		} else if ("startStopCallMonitor".equals(e.getActionCommand())) { //$NON-NLS-1$
			// Aktion des StartCallMonitorButtons
			properties.setProperty("option.callMonitorType", String //$NON-NLS-1$
					.valueOf(callMonitorCombo.getSelectedIndex()));

		    Container c = getPanel(); // get the window's content pane
			try {
				c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				fritzBoxPanel.detectBoxType();
				if (fritzBoxPanel.getFritzBox().getFirmware() != null) {
					// TODO login using settings!
				}
				this.startStopCallMonitor();
			} catch (IOException e1) {
				String message = messages.getMessage("box.not_found"); //$NON-NLS-1$
				log.error(message, e1);
				Debug.errDlg(message); 
				startStopCallMonitorButton.setSelected(!startStopCallMonitorButton.isSelected());
			} catch (PageNotFoundException e1) {
				String message = messages.getMessage("box.communication_error"); //$NON-NLS-1$
				log.error(message, e1);
				Debug.errDlg(message); 
				startStopCallMonitorButton.setSelected(!startStopCallMonitorButton.isSelected());
			} catch (FirmwareNotDetectedException e1) {
				String message = messages.getMessage("unknown_firmware"); //$NON-NLS-1$
				log.error(message, e1);
				Debug.errDlg(message); 
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
					.setText(messages.getMessage("stop_call_monitor")); //$NON-NLS-1$
			startStopCallMonitorButton.setSelected(true);
		} else {
			startStopCallMonitorButton.setText(messages.getMessage("start_call_monitor")); //$NON-NLS-1$
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

	private void updateExternalProgramUI() {
		if (externProgramCheckBox.isSelected()) {
			externProgramTextField.setEnabled(true);
			externProgramArgsTextField.setEnabled(true);
		} else {
			externProgramTextField.setEnabled(false);
			externProgramArgsTextField.setEnabled(false);
		}
	}
}
