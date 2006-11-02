/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StartEndFilenameFilter;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 */
public class ConfigDialog extends JDialog {

	public static boolean refreshWindow;

	private static final long serialVersionUID = 1;

	private JComboBox callMonitorCombo, languageCombo;

	private JTextField externProgramTextField, save_location;

	private String loc; //$NON-NLS-1$

	String localeList[];

	private JSlider timerSlider;

	private JButton okButton, cancelButton,
			callMonitorOptionsButton;

	private JToggleButton startCallMonitorButton;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
			notifyOnCallsButton, confirmOnExitButton, startMinimizedButton,
			timerAfterStartButton, passwordAfterStartButton, soundButton,
			callMonitorAfterStartButton, lookupAfterFetchButton,
			externProgramCheckBox, searchWithSSDP, showCallByCallColumnButton,
			showCommentColumnButton, showPortColumnButton,
			minimizeInsteadOfClose, createBackup, createBackupAfterFetch,
			fetchAfterStandby, checkNewVersionAfterStart;

	private JPanel callMonitorPane;

	private PhonePanel phonePanel;
	private FritzBoxPanel fritzBoxPanel;
	private MessagePanel messagePanel;

	private JLabel timerLabel;

	private boolean pressed_OK = false;

    static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$
	final String langID = FILESEP + "lang";										//$NON-NLS-1$

	public ConfigDialog(Frame parent) {
		super(parent, true);
		setTitle(Main.getMessage("config")); //$NON-NLS-1$

		phonePanel = new PhonePanel();
		fritzBoxPanel = new FritzBoxPanel();
		messagePanel = new MessagePanel();

		drawDialog();
		setValues();
		if (parent != null) {
			setLocationRelativeTo(parent);
		}

	}

	public boolean okPressed() {
		return pressed_OK;
	}

	/**
	 * Sets properties to dialog components
	 */
	public void setValues() {
		fritzBoxPanel.loadSettings();
		phonePanel.loadSettings();
		messagePanel.loadSettings();

		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.notifyOnCalls"))); //$NON-NLS-1$
		checkNewVersionAfterStart.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.checkNewVersionAfterStart" , "false")));//$NON-NLS-1$, //§NON-NLS-2$
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.fetchAfterStart"))); //$NON-NLS-1$
		timerAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.timerAfterStart"))); //$NON-NLS-1$
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.deleteAfterFetch"))); //$NON-NLS-1$
		confirmOnExitButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.confirmOnExit", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		startMinimizedButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.startMinimized", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		minimizeInsteadOfClose.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.minimize", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		createBackup.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.createBackup", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		createBackupAfterFetch.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.createBackupAfterFetch", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		soundButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.playSounds", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.startExternProgram", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(Main.getProperty("option.externProgram", ""))); //$NON-NLS-1$,  //$NON-NLS-2$

		callMonitorCombo.setSelectedIndex(Integer.parseInt(Main.getProperty(
				"option.callMonitorType", "0"))); //$NON-NLS-1$,  //$NON-NLS-2$

		int index = 0;
		String loc = Main.getProperty("locale", "de_DE");
		for (int a = 0; a < localeList.length; a++) {
			if (localeList[a].equals(loc)) index = a;
		}
		languageCombo.setSelectedIndex(index);

		if (JFritz.getCallMonitor() == null) {
			startCallMonitorButton.setSelected(false);
		} else {
			startCallMonitorButton.setSelected(true);
		}
		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.autostartcallmonitor", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		if (startCallMonitorButton.isSelected()) {
			setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
		} else {
			setCallMonitorButtons(JFritz.CALLMONITOR_START);
		}

		lookupAfterFetchButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.lookupAfterFetch", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCallByCallColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCallByCallColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCommentColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCommentColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showPortColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showPortColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		fetchAfterStandby.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.watchdog.fetchAfterStandby", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		boolean pwAfterStart = !Encryption.decrypt(
				Main.getProperty("jfritz.password", "")).equals( //$NON-NLS-1$,  //$NON-NLS-2$
				JFritz.PROGRAM_SECRET
						+ Encryption.decrypt(Main.getProperty("box.password", //$NON-NLS-1$
								""))); //$NON-NLS-1$
		passwordAfterStartButton.setSelected(pwAfterStart);

		timerSlider.setValue(Integer
				.parseInt(Main.getProperty("fetch.timer"))); //$NON-NLS-1$

		searchWithSSDP.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.useSSDP", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		//Buggy code
		/*if (devices != null) {
			for (int i = 0; i < devices.size(); i++) {
				SSDPPacket p = (SSDPPacket) devices.get(i);
				if (p.getIP().getHostAddress().equals(address.getText())) {
					addressCombo.setSelectedIndex(i);
				}
			}
		}*/
	}

	/**
	 * Stores values in dialog components to programm properties
	 */
	public void storeValues() {
		phonePanel.saveSettings();
		fritzBoxPanel.saveSettings();
		messagePanel.saveSettings();

		//only write the save dir to disk if the user changed something
		if(!save_location.getText().equals(Main.SAVE_DIR)){
			Main.SAVE_DIR = save_location.getText();
			Main.writeSaveDir();
		}

		Main.setProperty("option.useSSDP", Boolean.toString(searchWithSSDP //$NON-NLS-1$
				.isSelected()));
		Main.setProperty("option.notifyOnCalls", Boolean //$NON-NLS-1$
				.toString(notifyOnCallsButton.isSelected()));
		Main.setProperty("option.fetchAfterStart", Boolean //$NON-NLS-1$
				.toString(fetchAfterStartButton.isSelected()));
		Main.setProperty("option.timerAfterStart", Boolean //$NON-NLS-1$
				.toString(timerAfterStartButton.isSelected()));
		Main.setProperty("option.deleteAfterFetch", Boolean //$NON-NLS-1$
				.toString(deleteAfterFetchButton.isSelected()));
		Main.setProperty("option.confirmOnExit", Boolean //$NON-NLS-1$
				.toString(confirmOnExitButton.isSelected()));
		Main.setProperty("option.startMinimized", Boolean //$NON-NLS-1$
				.toString(startMinimizedButton.isSelected()));
		Main.setProperty("option.minimize", Boolean //$NON-NLS-1$
				.toString(minimizeInsteadOfClose.isSelected()));
		Main.setProperty(
						"option.createBackup", Boolean.toString(createBackup.isSelected())); //$NON-NLS-1$
		Main.setProperty(
						"option.createBackupAfterFetch", Boolean.toString(createBackupAfterFetch.isSelected())); //$NON-NLS-1$
		Main.setProperty("option.playSounds", Boolean.toString(soundButton //$NON-NLS-1$
				.isSelected()));

		Main.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
				.toString(externProgramCheckBox.isSelected()));
		Main.setProperty(
						"option.externProgram", JFritzUtils.convertSpecialChars(externProgramTextField //$NON-NLS-1$
										.getText()));

		Main.setProperty("option.startcallmonitor", Boolean //$NON-NLS-1$
				.toString(startCallMonitorButton.isSelected()));
		Main.setProperty("option.autostartcallmonitor", Boolean //$NON-NLS-1$
				.toString(callMonitorAfterStartButton.isSelected()));
		Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));
		Main.setProperty(
				"option.checkNewVersionAfterStart", Boolean.toString(checkNewVersionAfterStart.isSelected())); //$NON-NLS-1$

		if (!passwordAfterStartButton.isSelected()) {
			Main.setProperty("jfritz.password", Encryption //$NON-NLS-1$
					.encrypt(JFritz.PROGRAM_SECRET + fritzBoxPanel.getPassword()));
		} else {
			Main.removeProperty("jfritz.password"); //$NON-NLS-1$
		}

		Main.setProperty("option.lookupAfterFetch", Boolean //$NON-NLS-1$
				.toString(lookupAfterFetchButton.isSelected()));

		Main.setProperty("option.showCallByCallColumn", Boolean //$NON-NLS-1$
				.toString(showCallByCallColumnButton.isSelected()));

		Main.setProperty("option.showCommentColumn", Boolean //$NON-NLS-1$
				.toString(showCommentColumnButton.isSelected()));

		Main.setProperty("option.showPortColumn", Boolean //$NON-NLS-1$
				.toString(showPortColumnButton.isSelected()));

		Main.setProperty("option.watchdog.fetchAfterStandby", Boolean //$NON-NLS-1$
				.toString(fetchAfterStandby.isSelected()));

		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		Main.setProperty("fetch.timer", Integer.toString(timerSlider //$NON-NLS-1$
				.getValue()));

		JFritz.getFritzBox().detectFirmware();

		if (!Main.getProperty("locale", "de_DE").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
			Main.setProperty(
					"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
			loc = localeList[languageCombo.getSelectedIndex()];
			JFritz.getJframe().setLanguage(
					new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length())));
		}


		Debug.msg("Saved config"); //$NON-NLS-1$
		JFritz.getSIPProviderTableModel()
				.saveToXMLFile(Main.SAVE_DIR + JFritz.SIPPROVIDER_FILE);
		JFritz.getCallerList().saveToXMLFile(Main.SAVE_DIR+JFritz.CALLS_FILE, true);
		JFritz.getPhonebook().saveToXMLFile(Main.SAVE_DIR+JFritz.PHONEBOOK_FILE);
        Main.saveUpdateProperties();
	}

	protected JPanel createSipPane(ActionListener actionListener) {
		JPanel sippane = new JPanel();
		sippane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel sipButtonPane = new JPanel();
		final JTable siptable = new JTable(JFritz.getSIPProviderTableModel()) {
			private static final long serialVersionUID = 1;

			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(new Color(255, 255, 200));
				} else if (!isCellSelected(rowIndex, vColIndex)) {
					// If not shaded, match the table's background
					c.setBackground(getBackground());
				} else {
					c.setBackground(new Color(204, 204, 255));
				}
				return c;
			}
		};
		siptable.setRowHeight(24);
		siptable.setFocusable(false);
		siptable.setAutoCreateColumnsFromModel(false);
		siptable.setColumnSelectionAllowed(false);
		siptable.setCellSelectionEnabled(false);
		siptable.setRowSelectionAllowed(true);
		siptable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		siptable.getColumnModel().getColumn(0).setMinWidth(20);
		siptable.getColumnModel().getColumn(0).setMaxWidth(20);
		siptable.getColumnModel().getColumn(1).setMinWidth(40);
		siptable.getColumnModel().getColumn(1).setMaxWidth(40);
		siptable.setSize(200, 200);
		JButton b1 = new JButton(Main.getMessage("get_sip_provider_from_box")); //$NON-NLS-1$
		b1.setActionCommand("fetchSIP"); //$NON-NLS-1$
		b1.addActionListener(actionListener);
		JButton b2 = new JButton(Main.getMessage("save_sip_provider_on_box")); //$NON-NLS-1$
		b2.setEnabled(false);
		sipButtonPane.add(b1);
		sipButtonPane.add(b2);

		sippane.setLayout(new BorderLayout());
		sippane.add(sipButtonPane, BorderLayout.NORTH);
		sippane.add(new JScrollPane(siptable), BorderLayout.CENTER);
		return sippane;
	}

	protected JPanel createOtherPane() {

		JPanel otherpane = new JPanel();

		otherpane.setLayout(new BoxLayout(otherpane, BoxLayout.Y_AXIS));
		timerLabel = new JLabel(Main.getMessage("timer_in") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		otherpane.add(timerLabel);
		otherpane.add(timerSlider);

		checkNewVersionAfterStart = new JCheckBox(Main.getMessage("check_for_new_version_after_start")); //$NON-NLS-1$
		otherpane.add(checkNewVersionAfterStart);

		passwordAfterStartButton = new JCheckBox(Main.getMessage("ask_for_password_before_start")); //$NON-NLS-1$
		otherpane.add(passwordAfterStartButton);

		timerAfterStartButton = new JCheckBox(Main.getMessage("get_timer_after")); //$NON-NLS-1$
		otherpane.add(timerAfterStartButton);

		startMinimizedButton = new JCheckBox(Main.getMessage("start_minimized")); //$NON-NLS-1$
		otherpane.add(startMinimizedButton);

		confirmOnExitButton = new JCheckBox(Main.getMessage("confirm_on_exit")); //$NON-NLS-1$
		otherpane.add(confirmOnExitButton);

		searchWithSSDP = new JCheckBox(Main.getMessage("search_with_SSDP")); //$NON-NLS-1$
		otherpane.add(searchWithSSDP);

		minimizeInsteadOfClose = new JCheckBox(Main.getMessage("minimize_instead_close")); //$NON-NLS-1$
		otherpane.add(minimizeInsteadOfClose);

		createBackup = new JCheckBox(Main.getMessage("create_backup_start")); //$NON-NLS-1$
		otherpane.add(createBackup);

		createBackupAfterFetch = new JCheckBox(Main.getMessage("create_backup_fetch")); //$NON-NLS-1$
		otherpane.add(createBackupAfterFetch);

		JPanel panel = new JPanel();

		JLabel label = new JLabel(Main.getMessage("save_directory"));
		panel.add(label);

		save_location = new JTextField(Main.SAVE_DIR);
		save_location.setPreferredSize(new Dimension(200, 20));
		panel.add(save_location);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(Main.SAVE_DIR);
				fc.setDialogTitle(Main.getMessage("save_directory")); //$NON-NLS-1$
				fc.setDialogType(JFileChooser.SAVE_DIALOG);

				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (!file.exists()) {
						JOptionPane.showMessageDialog(null, Main.getMessage("file_not_found"), //$NON-NLS-1$
								Main.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					} else {
						save_location.setText(file.getAbsolutePath());
					}
				}
			}
		};

		JButton browseButton = new JButton(Main.getMessage("browse"));
		browseButton.addActionListener(actionListener);

		panel.add(browseButton);
		otherpane.add(panel);


		return otherpane;
	}

	protected JPanel createCallerListPane() {
		JPanel cPanel = new JPanel();

		cPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		fetchAfterStartButton = new JCheckBox(Main.getMessage("fetch_after_start")); //$NON-NLS-1$
		cPanel.add(fetchAfterStartButton, c);

		c.gridy = 1;
		notifyOnCallsButton = new JCheckBox(Main.getMessage("notify_on_calls")); //$NON-NLS-1$
		cPanel.add(notifyOnCallsButton, c);

		c.gridy = 2;
		deleteAfterFetchButton = new JCheckBox(Main.getMessage("delete_after_fetch")); //$NON-NLS-1$
		cPanel.add(deleteAfterFetchButton, c);

		c.gridy = 3;
		lookupAfterFetchButton = new JCheckBox(Main.getMessage("lookup_after_fetch")); //$NON-NLS-1$
		cPanel.add(lookupAfterFetchButton, c);

		c.gridy = 4;
		showCallByCallColumnButton = new JCheckBox(Main.getMessage("show_callbyball_column")); //$NON-NLS-1$
		cPanel.add(showCallByCallColumnButton, c);

		c.gridy = 5;
		showCommentColumnButton = new JCheckBox(Main.getMessage("show_comment_column")); //$NON-NLS-1$
		cPanel.add(showCommentColumnButton, c);

		c.gridy = 6;
		showPortColumnButton = new JCheckBox(Main.getMessage("show_port_column")); //$NON-NLS-1$
		cPanel.add(showPortColumnButton, c);

		c.gridy = 7;
		fetchAfterStandby = new JCheckBox(Main.getMessage("fetch_after_standby")); //$NON-NLS-1$
		cPanel.add(fetchAfterStandby, c);

		return cPanel;
	}

	protected JPanel createLocalePane(ActionListener actionListener) {
		JPanel localePane = new JPanel();
		localePane.setLayout(new GridBagLayout());
		localePane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		JLabel label;
		c.gridy = 2;
		label = new JLabel(Main.getMessage("language") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		localePane.add(label, c);

		String lang = JFritzUtils.getFullPath(langID);
		File file = new File(lang);
		FilenameFilter props = new StartEndFilenameFilter("jfritz_","properties");//$NON-NLS-1$,  //$NON-NLS-2$
		String[] list = file.list(props);
		localeList= new String[list.length];

		ImageIcon[]  images = new ImageIcon[list.length];

		for (int i = 0; i < list.length; i++) {
			localeList[i] = list[i].substring(list[i].indexOf("_") + 1,list[i].indexOf("."));//$NON-NLS-1$,  //$NON-NLS-2$
			String imagePath =
			     lang + FILESEP + "flags" + FILESEP +						//$NON-NLS-1$,  //$NON-NLS-2$
			     localeList[i].substring(localeList[i].indexOf("_")+1,
			         localeList[i].length()).toLowerCase() + ".gif";		//$NON-NLS-1$
			Debug.msg("Found resources for locale '" + localeList[i] +		//$NON-NLS-1$
			     "', loading flag image '" + imagePath + "'");				//$NON-NLS-1$,  //$NON-NLS-2$
			images[i] = new ImageIcon(imagePath);
			images[i].setDescription(Main.getLocaleMeaning(localeList[i]));
		}


		c.fill = GridBagConstraints.HORIZONTAL;

		languageCombo = new JComboBox(images);
		LanguageComboBoxRenderer renderer = new LanguageComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(180, 15));

		languageCombo.setRenderer(renderer);
		languageCombo.setActionCommand("languageCombo"); //$NON-NLS-1$
		languageCombo.setMaximumRowCount(8);
		languageCombo.addActionListener(actionListener);

		localePane.add(languageCombo, c);

		return localePane;
	}

	protected void stopAllCallMonitors() {
		if (startCallMonitorButton.isSelected()) {
			setCallMonitorButtons(JFritz.CALLMONITOR_START);
			JFritz.stopCallMonitor();
		}
	}

	private void hideCallMonitorPanel() {
		startCallMonitorButton.setVisible(false);
		callMonitorOptionsButton.setVisible(false);
		callMonitorAfterStartButton.setVisible(false);
		soundButton.setVisible(false);
		externProgramCheckBox.setVisible(false);
		externProgramTextField.setVisible(false);
		callMonitorPane.repaint();
	}

	private void showCallMonitorPanel() {
		startCallMonitorButton.setVisible(true);
		callMonitorAfterStartButton.setVisible(true);
		callMonitorOptionsButton.setVisible(true);
		soundButton.setVisible(true);
		externProgramCheckBox.setVisible(true);
		externProgramTextField.setVisible(true);
		callMonitorPane.repaint();
	}

	protected JPanel createCallMonitorPane() {
		final ConfigDialog configDialog = this;
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("comboboxchanged".equalsIgnoreCase(e.getActionCommand())) { //$NON-NLS-1$
					// Zur Darstellung der gewünschten Einstellungspanels
					switch (callMonitorCombo.getSelectedIndex()) {
						case 0 : {
							hideCallMonitorPanel();
							Debug.msg("Call monitor not wanted"); //$NON-NLS-1$
							stopAllCallMonitors();
							break;
						}
						case 1 : {
							showCallMonitorPanel();
							Debug.msg("FRITZ!Box call monitor chosen"); //$NON-NLS-1$
							stopAllCallMonitors();
							break;
						}
						case 2 : {
							showCallMonitorPanel();
							Debug.msg("Telnet call monitor chosen"); //$NON-NLS-1$
							stopAllCallMonitors();
							break;

						}
						case 3 : {
							showCallMonitorPanel();
							Debug.msg("Syslog call monitor chosen"); //$NON-NLS-1$
							stopAllCallMonitors();
							break;
						}
						case 4 : {
							showCallMonitorPanel();
							Debug.msg("YAC call monitor chosen"); //$NON-NLS-1$
							stopAllCallMonitors();
							break;
						}
						case 5 : {
							showCallMonitorPanel();
							Debug.msg("Callmessage call monitor chosen"); //$NON-NLS-1$
							stopAllCallMonitors();
							break;
						}
					}
				} else if ("startCallMonitor".equals(e.getActionCommand())) { //$NON-NLS-1$
					// Aktion des StartCallMonitorButtons
					Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
							.valueOf(callMonitorCombo.getSelectedIndex()));
					JFritz.getFritzBox().detectFirmware();
					JFritz.getJframe().switchMonitorButton();
					if (startCallMonitorButton.isSelected()) {
						setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
					} else {
						setCallMonitorButtons(JFritz.CALLMONITOR_START);
					}
				} else if ("startCallMonitorOptions".equals(e //$NON-NLS-1$
						.getActionCommand())) {
					CallMonitorConfigDialog callMonitorConfigDialog = null;
					switch (callMonitorCombo.getSelectedIndex()) {
						case 1 :
							callMonitorConfigDialog = new FRITZBOXConfigDialog(
									configDialog);
							break;
						case 2 :
							callMonitorConfigDialog = new TelnetConfigDialog(
									configDialog);
							break;
						case 3 :
							callMonitorConfigDialog = new SyslogConfigDialog(
									configDialog);
							break;
						case 4 :
							callMonitorConfigDialog = new YacConfigDialog(
									configDialog);
							break;
						case 5 :
							callMonitorConfigDialog = new CallmessageConfigDialog(
									configDialog);
							break;
					}
					if (callMonitorConfigDialog != null) {
						callMonitorConfigDialog.showConfigDialog();
						callMonitorConfigDialog.dispose();
					}

				}
			}
		};

		callMonitorPane = new JPanel();
		callMonitorPane.setLayout(new BorderLayout());
		callMonitorCombo = new JComboBox();
		callMonitorCombo.addItem(Main.getMessage("no_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("fritz_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("telnet_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("syslog_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("yac_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(Main.getMessage("callmessage_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addActionListener(actionListener);

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
		c.gridy = 0;
		c.gridwidth = 1;

		c.gridx = 1;
		c.gridy = 0;
		startCallMonitorButton = new JToggleButton();
		startCallMonitorButton.setActionCommand("startCallMonitor"); //$NON-NLS-1$
		startCallMonitorButton.addActionListener(actionListener);
		pane.add(startCallMonitorButton, c);

		c.gridx = 2;
		c.gridy = 0;
		callMonitorOptionsButton = new JButton(Main.getMessage("config")); //$NON-NLS-1$
		callMonitorOptionsButton.setActionCommand("startCallMonitorOptions"); //$NON-NLS-1$
		callMonitorOptionsButton.addActionListener(actionListener);
		pane.add(callMonitorOptionsButton, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		callMonitorAfterStartButton = new JCheckBox(Main.getMessage("call_monitor_prog_start")); //$NON-NLS-1$
		pane.add(callMonitorAfterStartButton, c);

		soundButton = new JCheckBox(Main.getMessage("play_sound")); //$NON-NLS-1$
		c.gridy = 2;
		pane.add(soundButton, c);

		externProgramCheckBox = new JCheckBox(Main.getMessage("run_external_program")); //$NON-NLS-1$
		c.gridy = 3;
		pane.add(externProgramCheckBox, c);

		externProgramTextField = new JTextField("", 40); //$NON-NLS-1$
		externProgramTextField.setMinimumSize(new Dimension(300, 20));
		c.gridy = 4;
		pane.add(externProgramTextField, c);

		return callMonitorPane;
	}

	protected void drawDialog() {

		// Create JTabbedPane
		JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

		tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$
		timerSlider = new JSlider(0, 120, 30);
		timerSlider.setPaintTicks(true);
		timerSlider.setMinorTickSpacing(10);
		timerSlider.setMajorTickSpacing(30);
		timerSlider.setPaintLabels(true);
		timerSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (timerSlider.getValue() < 3)
					timerSlider.setValue(3);
				timerLabel
						.setText(Main.getMessage("timer")+": " + timerSlider.getValue() +" "+ Main.getMessage("abbreviation_minutes")); //$NON-NLS-1$,  //$NON-NLS-2$
			}

		});

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					pressed_OK = false;
					ConfigDialog.this.setVisible(false);
				}
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					pressed_OK = true;
					ConfigDialog.this.setVisible(false);
				}
			}
		});

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				pressed_OK = (source == okButton);
				if (source == okButton
						|| source == cancelButton) {
					ConfigDialog.this.setVisible(false);
				} else if (e.getActionCommand().equals("fetchSIP")) { //$NON-NLS-1$
					try {
						JFritz.getFritzBox().detectFirmware();
						Vector data = JFritz.getFritzBox().retrieveSipProvider();
						JFritz.getSIPProviderTableModel().updateProviderList(
								data);
						JFritz.getSIPProviderTableModel()
								.fireTableDataChanged();
						JFritz.getCallerList().fireTableDataChanged();

					} catch (WrongPasswordException e1) {
						JFritz.errorMsg(Main.getMessage("wrong_password")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("wrong_password")); //$NON-NLS-1$
					} catch (IOException e1) {
						JFritz.errorMsg(Main.getMessage("box_address_wrong")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box_address_wrong")); //$NON-NLS-1$
					} catch (InvalidFirmwareException e1) {
						JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
					}
				}
			}
		};

		// Create OK/Cancel Panel
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel okcancelpanel = new JPanel();
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		okcancelpanel.add(okButton, c);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
		okcancelpanel.add(cancelButton);

        //set default confirm button (Enter)
        getRootPane().setDefaultButton(okButton);

        //set default close button (ESC)
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            private static final long serialVersionUID = 3L;

            public void actionPerformed(ActionEvent e)
            {
                 cancelButton.doClick();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

		tpane.addTab(
				Main.getMessage("FRITZ!Box"), fritzBoxPanel); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("telephone"), phonePanel); //$NON-NLS-1$
		tpane
				.addTab(
						Main.getMessage("sip_numbers"), createSipPane(actionListener)); //$NON-NLS-1$
		JScrollPane callerListPaneScrollable = new JScrollPane(
				createCallerListPane());
		tpane.addTab(Main.getMessage("callerlist"), callerListPaneScrollable); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("callmonitor"), createCallMonitorPane()); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("messages"), messagePanel); //$NON-NLS-1$
		JScrollPane otherPaneScrollable = new JScrollPane(createOtherPane()); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("other"), otherPaneScrollable); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("language"),
				createLocalePane(actionListener));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tpane, BorderLayout.CENTER);
		getContentPane().add(okcancelpanel, BorderLayout.SOUTH);
		c.fill = GridBagConstraints.HORIZONTAL;

		addKeyListener(keyListener);

		setSize(new Dimension(510, 360));
		setResizable(false);
		// pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed();
	}

	/**
	 * Let startCallMonitorButtons start or stop callMonitor Changes caption of
	 * buttons and their status
	 *
	 * @param option
	 *            CALLMONITOR_START or CALLMONITOR_STOP
	 */
	public void setCallMonitorButtons(int option) {
		if (option == JFritz.CALLMONITOR_START) {
			startCallMonitorButton.setText(Main.getMessage("start_call_monitor")); //$NON-NLS-1$
			startCallMonitorButton.setSelected(false);
			JFritz.getJframe().getMonitorButton().setSelected(false);
		} else if (option == JFritz.CALLMONITOR_STOP) {
			startCallMonitorButton.setText(Main.getMessage("stop_call_monitor")); //$NON-NLS-1$
			startCallMonitorButton.setSelected(true);
			JFritz.getJframe().getMonitorButton().setSelected(true);
		}
	}
}