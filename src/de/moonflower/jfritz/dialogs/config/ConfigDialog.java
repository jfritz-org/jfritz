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
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
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
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StartEndFilenameFilter;
import de.moonflower.jfritz.utils.network.SSDPPacket;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 */
public class ConfigDialog extends JDialog {

	public static boolean refreshWindow;

	private static final long serialVersionUID = 1;

	private JFritz jfritz;

	private JComboBox addressCombo, callMonitorCombo, languageCombo;

	private JTextField address, areaCode, countryCode, areaPrefix,
			countryPrefix, externProgramTextField, port, popupDelay,
			save_location, dialPrefix;

	private JPasswordField pass;

	private String password = "", loc; //$NON-NLS-1$

	String localeList[];

	private JSlider timerSlider;

	private JButton okButton, cancelButton, boxtypeButton,
			callMonitorOptionsButton;

	private JToggleButton startCallMonitorButton;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
			notifyOnCallsButton, confirmOnExitButton, startMinimizedButton,
			timerAfterStartButton, passwordAfterStartButton, soundButton,
			callMonitorAfterStartButton, lookupAfterFetchButton,
			externProgramCheckBox, searchWithSSDP, showCallByCallColumnButton,
			showCommentColumnButton, showPortColumnButton,
			minimizeInsteadOfClose, createBackup, createBackupAfterFetch,
			fetchAfterStandby, activateDialPrefix, checkNewVersionAfterStart;

	private JPanel callMonitorPane;

	private JLabel boxtypeLabel, macLabel, timerLabel;

	private FritzBoxFirmware firmware = null;

	private boolean pressed_OK = false;

	private Vector devices;

	private JRadioButton popupNoButton, popupDialogButton, popupTrayButton;

    static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$
	final String langID = FILESEP + "lang";										//$NON-NLS-1$

	public ConfigDialog(Frame parent) {
		super(parent, true);
		if (parent != null) {
			//setLocationRelativeTo(parent);
			jfritz = ((JFritzWindow) parent).getJFritz();
		}
		setTitle(JFritz.getMessage("config")); //$NON-NLS-1$
		devices = jfritz.getDevices();
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
		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.notifyOnCalls"))); //$NON-NLS-1$
		checkNewVersionAfterStart.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty("option.checkNewVersionAfterStart" , "true")));//$NON-NLS-1$, //§NON-NLS-2$
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.fetchAfterStart"))); //$NON-NLS-1$
		timerAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.timerAfterStart"))); //$NON-NLS-1$
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.deleteAfterFetch"))); //$NON-NLS-1$
		confirmOnExitButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.confirmOnExit", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		startMinimizedButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.startMinimized", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		minimizeInsteadOfClose.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.minimize", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		createBackup.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"option.createBackup", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		createBackupAfterFetch.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.createBackupAfterFetch", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		soundButton.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"option.playSounds", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramCheckBox.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.startExternProgram", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		externProgramTextField.setText(JFritzUtils.deconvertSpecialChars(JFritz
				.getProperty("option.externProgram", ""))); //$NON-NLS-1$,  //$NON-NLS-2$
        activateDialPrefix.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.activateDialPrefix", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$


		callMonitorCombo.setSelectedIndex(Integer.parseInt(JFritz.getProperty(
				"option.callMonitorType", "0"))); //$NON-NLS-1$,  //$NON-NLS-2$

		int index = 0;
		String loc = JFritz.getProperty("locale", "de_DE");
		for (int a = 0; a < localeList.length; a++) {
			if (localeList[a].equals(loc)) index = a;
		}
		languageCombo.setSelectedIndex(index);

		if (jfritz.getCallMonitor() == null) {
			startCallMonitorButton.setSelected(false);
		} else {
			startCallMonitorButton.setSelected(true);
		}
		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.autostartcallmonitor", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		if (startCallMonitorButton.isSelected()) {
			setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
		} else {
			setCallMonitorButtons(JFritz.CALLMONITOR_START);
		}

		if (!JFritz.SYSTRAY_SUPPORT) {
			popupTrayButton.setVisible(false);
		}
		switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			case 0 : {
				popupNoButton.setSelected(true);
				break;
			}
			case 1 : {
				popupDialogButton.setSelected(true);
				break;
			}
			case 2 : {
				popupTrayButton.setSelected(true);
				break;
			}
		}

		popupDelay.setText(JFritz.getProperty("option.popupDelay", "10"));

		lookupAfterFetchButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.lookupAfterFetch", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCallByCallColumnButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.showCallByCallColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCommentColumnButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.showCommentColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showPortColumnButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.showPortColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		fetchAfterStandby.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.watchdog.fetchAfterStandby", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		boolean pwAfterStart = !Encryption.decrypt(
				JFritz.getProperty("jfritz.password", "")).equals( //$NON-NLS-1$,  //$NON-NLS-2$
				JFritz.PROGRAM_SECRET
						+ Encryption.decrypt(JFritz.getProperty("box.password", //$NON-NLS-1$
								""))); //$NON-NLS-1$
		passwordAfterStartButton.setSelected(pwAfterStart);

		pass.setText(jfritz.getFritzBox().getPassword()); //$NON-NLS-1$
		password = jfritz.getFritzBox().getPassword();
		address.setText(jfritz.getFritzBox().getAddress());
		port.setText(jfritz.getFritzBox().getPort());
		areaCode.setText(JFritz.getProperty("area.code")); //$NON-NLS-1$
		countryCode.setText(JFritz.getProperty("country.code")); //$NON-NLS-1$
        areaPrefix.setText(JFritz.getProperty("area.prefix")); //$NON-NLS-1$
        dialPrefix.setText(JFritz.getProperty("dial.prefix")); //$NON-NLS-1$
		countryPrefix.setText(JFritz.getProperty("country.prefix")); //$NON-NLS-1$
		timerSlider.setValue(Integer
				.parseInt(JFritz.getProperty("fetch.timer"))); //$NON-NLS-1$

		searchWithSSDP.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"option.useSSDP", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		if (devices != null) {
			for (int i = 0; i < devices.size(); i++) {
				SSDPPacket p = (SSDPPacket) devices.get(i);
				if (p.getIP().getHostAddress().equals(address.getText())) {
					addressCombo.setSelectedIndex(i);
				}
			}
		}
		firmware = jfritz.getFritzBox().getFirmware(); //$NON-NLS-1$
		setBoxTypeLabel();
	}

	/**
	 * Stores values in dialog components to programm properties
	 */
	public void storeValues() {

		//only write the save dir to disk if the user changed something
		if(!save_location.getText().equals(JFritz.SAVE_DIR)){
			JFritz.SAVE_DIR = save_location.getText();
			jfritz.writeSaveDir();
		}

		//		 Remove leading "0" from areaCode
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));

		JFritz.setProperty("option.useSSDP", Boolean.toString(searchWithSSDP //$NON-NLS-1$
				.isSelected()));
		JFritz.setProperty("option.notifyOnCalls", Boolean //$NON-NLS-1$
				.toString(notifyOnCallsButton.isSelected()));
		JFritz.setProperty("option.fetchAfterStart", Boolean //$NON-NLS-1$
				.toString(fetchAfterStartButton.isSelected()));
		JFritz.setProperty("option.timerAfterStart", Boolean //$NON-NLS-1$
				.toString(timerAfterStartButton.isSelected()));
		JFritz.setProperty("option.deleteAfterFetch", Boolean //$NON-NLS-1$
				.toString(deleteAfterFetchButton.isSelected()));
		JFritz.setProperty("option.confirmOnExit", Boolean //$NON-NLS-1$
				.toString(confirmOnExitButton.isSelected()));
		JFritz.setProperty("option.startMinimized", Boolean //$NON-NLS-1$
				.toString(startMinimizedButton.isSelected()));
		JFritz.setProperty("option.minimize", Boolean //$NON-NLS-1$
				.toString(minimizeInsteadOfClose.isSelected()));
		JFritz
				.setProperty(
						"option.createBackup", Boolean.toString(createBackup.isSelected())); //$NON-NLS-1$
        JFritz
				.setProperty(
						"option.createBackupAfterFetch", Boolean.toString(createBackupAfterFetch.isSelected())); //$NON-NLS-1$
		JFritz.setProperty("option.playSounds", Boolean.toString(soundButton //$NON-NLS-1$
				.isSelected()));

		JFritz.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
				.toString(externProgramCheckBox.isSelected()));
		JFritz
				.setProperty(
						"option.externProgram", JFritzUtils.convertSpecialChars(externProgramTextField //$NON-NLS-1$
										.getText()));

		JFritz.setProperty("option.startcallmonitor", Boolean //$NON-NLS-1$
				.toString(startCallMonitorButton.isSelected()));
		JFritz.setProperty("option.autostartcallmonitor", Boolean //$NON-NLS-1$
				.toString(callMonitorAfterStartButton.isSelected()));
		JFritz.setProperty("option.callMonitorType", String //$NON-NLS-1$
				.valueOf(callMonitorCombo.getSelectedIndex()));
        JFritz
        .setProperty(
                "option.activateDialPrefix", Boolean.toString(activateDialPrefix.isSelected())); //$NON-NLS-1$
        JFritz
		.setProperty(
				"option.checkNewVersionAfterStart", Boolean.toString(checkNewVersionAfterStart.isSelected())); //$NON-NLS-1$



		// Set Popup Messages Type
		if (popupNoButton.isSelected()) {
			JFritz.setProperty("option.popuptype", "0"); //$NON-NLS-1$, //$NON-NLS-2$
		} else if (popupDialogButton.isSelected()) {
			JFritz.setProperty("option.popuptype", "1"); //$NON-NLS-1$, //$NON-NLS-2$
		} else {
			JFritz.setProperty("option.popuptype", "2"); //$NON-NLS-1$, //$NON-NLS-2$
		}

		if (!passwordAfterStartButton.isSelected()) {
			JFritz.setProperty("jfritz.password", Encryption //$NON-NLS-1$
					.encrypt(JFritz.PROGRAM_SECRET + password));
		} else {
			JFritz.removeProperty("jfritz.password"); //$NON-NLS-1$
		}

		JFritz.setProperty("option.lookupAfterFetch", Boolean //$NON-NLS-1$
				.toString(lookupAfterFetchButton.isSelected()));

		JFritz.setProperty("option.showCallByCallColumn", Boolean //$NON-NLS-1$
				.toString(showCallByCallColumnButton.isSelected()));

		JFritz.setProperty("option.showCommentColumn", Boolean //$NON-NLS-1$
				.toString(showCommentColumnButton.isSelected()));

		JFritz.setProperty("option.showPortColumn", Boolean //$NON-NLS-1$
				.toString(showPortColumnButton.isSelected()));

		JFritz.setProperty("option.watchdog.fetchAfterStandby", Boolean //$NON-NLS-1$
				.toString(fetchAfterStandby.isSelected()));

		JFritz.setProperty("box.password", Encryption.encrypt(password)); //$NON-NLS-1$
		JFritz.setProperty("box.address", address.getText()); //$NON-NLS-1$
		JFritz.setProperty("box.port", port.getText()); //$NON-NLS-1$
		JFritz.setProperty("area.code", areaCode.getText()); //$NON-NLS-1$

		//Phone stuff here
		//make sure country code has a plus on it
		if(!countryCode.getText().startsWith("+"))
			countryCode.setText("+"+countryCode.getText());

		JFritz.setProperty("country.code", countryCode.getText()); //$NON-NLS-1$
		JFritz.setProperty("area.prefix", areaPrefix.getText()); //$NON-NLS-1$
        JFritz.setProperty("dial.prefix", dialPrefix.getText()); //$NON-NLS-1$
		JFritz.setProperty("country.prefix", countryPrefix.getText()); //$NON-NLS-1$
		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		JFritz.setProperty("fetch.timer", Integer.toString(timerSlider //$NON-NLS-1$
				.getValue()));

		jfritz.getFritzBox().detectFirmware();

		if (!JFritz.getProperty("locale", "de_DE").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
			JFritz.setProperty(
					"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
			loc = localeList[languageCombo.getSelectedIndex()];
			jfritz.getJframe().setLanguage(
					new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length())));
		}

		JFritz.setProperty("option.popupDelay", popupDelay.getText());



		Debug.msg("Saved config"); //$NON-NLS-1$
		jfritz.getSIPProviderTableModel()
				.saveToXMLFile(JFritz.SAVE_DIR + JFritz.SIPPROVIDER_FILE);
		jfritz.getCallerlist().saveToXMLFile(JFritz.SAVE_DIR+JFritz.CALLS_FILE, true);
		jfritz.getPhonebook().saveToXMLFile(JFritz.SAVE_DIR+JFritz.PHONEBOOK_FILE);
	}

	protected JPanel createBoxPane(ActionListener actionListener) {
		JPanel boxpane = new JPanel();
		boxpane.setLayout(new GridBagLayout());
		boxpane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 1;
		ImageIcon boxicon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/fritzbox.png"))); //$NON-NLS-1$
		JLabel label = new JLabel(""); //$NON-NLS-1$
		label.setIcon(boxicon);
		boxpane.add(label, c);
		label = new JLabel(JFritz.getMessage("FRITZ!Box_Preferences")); //$NON-NLS-1$
		boxpane.add(label, c);

		c.gridy = 2;
		label = new JLabel(JFritz.getMessage("FRITZ!Box") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		boxpane.add(label, c);

		addressCombo = new JComboBox();
		c.fill = GridBagConstraints.HORIZONTAL;
		if (devices != null) {
			Enumeration en = devices.elements();
			while (en.hasMoreElements()) {
				SSDPPacket p = (SSDPPacket) en.nextElement();
				addressCombo.addItem(p.getShortName());
			}
		}

		addressCombo.setActionCommand("addresscombo"); //$NON-NLS-1$
		addressCombo.addActionListener(actionListener);
		boxpane.add(addressCombo, c);

		c.gridy = 3;
		label = new JLabel(JFritz.getMessage("ip_address") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		boxpane.add(label, c);
		address = new JTextField("", 16); //$NON-NLS-1$
		address.setMinimumSize(new Dimension(200, 20));
		boxpane.add(address, c);

		c.gridy = 4;
		label = new JLabel(JFritz.getMessage("password") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		boxpane.add(label, c);
		pass = new JPasswordField("", 16); //$NON-NLS-1$
		pass.setMinimumSize(new Dimension(200, 20));
		boxpane.add(pass, c);

		c.gridy = 5;
		label = new JLabel(JFritz.getMessage("box.port") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		boxpane.add(label, c);
		port = new JTextField("", 16); //$NON-NLS-1$
		port.setMinimumSize(new Dimension(200, 20));
		boxpane.add(port, c);

		c.gridy = 6;
		boxtypeButton = new JButton(JFritz.getMessage("detect_box_type")); //$NON-NLS-1$
		boxtypeButton.setActionCommand("detectboxtype"); //$NON-NLS-1$
		boxtypeButton.addActionListener(actionListener);
		boxpane.add(boxtypeButton, c);
		boxtypeLabel = new JLabel();
		boxpane.add(boxtypeLabel, c);

		//c.gridy = 7;
		//label = new JLabel(JFritz.getMessage("mac_address") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		// boxpane.add(label, c);
		macLabel = new JLabel();
		// boxpane.add(macLabel, c);

		return boxpane;
	}

	protected JPanel createPhonePane() {
		JPanel phonepane = new JPanel();
		phonepane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 1;
		JLabel label = new JLabel(JFritz.getMessage("area_code")); //$NON-NLS-1$
		phonepane.add(label, c);
		areaCode = new JTextField("", 6); //$NON-NLS-1$
		phonepane.add(areaCode, c);

		c.gridy = 2;
		label = new JLabel(JFritz.getMessage("country_code")); //$NON-NLS-1$
		phonepane.add(label, c);
		countryCode = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(countryCode, c);

		c.gridy = 3;
		label = new JLabel(JFritz.getMessage("area_prefix")); //$NON-NLS-1$
		phonepane.add(label, c);
		areaPrefix = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(areaPrefix, c);

		c.gridy = 4;
		label = new JLabel(JFritz.getMessage("country_prefix")); //$NON-NLS-1$
		phonepane.add(label, c);
		countryPrefix = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(countryPrefix, c);

        c.gridy = 5;
        activateDialPrefix = new JCheckBox(JFritz.getMessage("dial_prefix")); //$NON-NLS-1$
        phonepane.add(activateDialPrefix, c);
        dialPrefix = new JTextField("", 3); //$NON-NLS-1$
        phonepane.add(dialPrefix, c);

		return phonepane;
	}

	protected JPanel createSipPane(ActionListener actionListener) {
		JPanel sippane = new JPanel();
		sippane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel sipButtonPane = new JPanel();
		final JTable siptable = new JTable(jfritz.getSIPProviderTableModel()) {
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
		JButton b1 = new JButton(JFritz.getMessage("get_sip_provider_from_box")); //$NON-NLS-1$
		b1.setActionCommand("fetchSIP"); //$NON-NLS-1$
		b1.addActionListener(actionListener);
		JButton b2 = new JButton(JFritz.getMessage("save_sip_provider_on_box")); //$NON-NLS-1$
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
		timerLabel = new JLabel(JFritz.getMessage("timer_in") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		otherpane.add(timerLabel);
		otherpane.add(timerSlider);

		checkNewVersionAfterStart = new JCheckBox(JFritz
				.getMessage("check_for_new_version_after_start")); //$NON-NLS-1$
		otherpane.add(checkNewVersionAfterStart);

		passwordAfterStartButton = new JCheckBox(JFritz
				.getMessage("ask_for_password_before_start")); //$NON-NLS-1$
		otherpane.add(passwordAfterStartButton);

		timerAfterStartButton = new JCheckBox(JFritz
				.getMessage("get_timer_after")); //$NON-NLS-1$
		otherpane.add(timerAfterStartButton);

		startMinimizedButton = new JCheckBox(JFritz
				.getMessage("start_minimized")); //$NON-NLS-1$
		otherpane.add(startMinimizedButton);

		confirmOnExitButton = new JCheckBox(JFritz
				.getMessage("confirm_on_exit")); //$NON-NLS-1$
		otherpane.add(confirmOnExitButton);

		searchWithSSDP = new JCheckBox(JFritz.getMessage("search_with_SSDP")); //$NON-NLS-1$
		otherpane.add(searchWithSSDP);

		minimizeInsteadOfClose = new JCheckBox(JFritz
				.getMessage("minimize_instead_close")); //$NON-NLS-1$
		otherpane.add(minimizeInsteadOfClose);

		createBackup = new JCheckBox(JFritz.getMessage("create_backup_start")); //$NON-NLS-1$
		otherpane.add(createBackup);

		createBackupAfterFetch = new JCheckBox(JFritz
				.getMessage("create_backup_fetch")); //$NON-NLS-1$
		otherpane.add(createBackupAfterFetch);

		//FIXME: This does not display correctly
		JPanel panel = new JPanel();

		JLabel label = new JLabel(JFritz.getMessage("save_directory"));
		panel.add(label);

		save_location = new JTextField(JFritz.SAVE_DIR);
		save_location.setPreferredSize(new Dimension(200, 20));
		panel.add(save_location);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(JFritz.SAVE_DIR);
				fc.setDialogTitle(JFritz.getMessage("save_directory")); //$NON-NLS-1$
				fc.setDialogType(JFileChooser.SAVE_DIALOG);

				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (!file.exists()) {
						JOptionPane.showMessageDialog(null, JFritz.getMessage("file_not_found"), //$NON-NLS-1$
								JFritz.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					} else {
						save_location.setText(file.getAbsolutePath());
					}
				}
			}
		};

		JButton browseButton = new JButton(JFritz.getMessage("browse"));
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
		fetchAfterStartButton = new JCheckBox(JFritz
				.getMessage("fetch_after_start")); //$NON-NLS-1$
		cPanel.add(fetchAfterStartButton, c);

		c.gridy = 1;
		notifyOnCallsButton = new JCheckBox(JFritz
				.getMessage("notify_on_calls")); //$NON-NLS-1$
		cPanel.add(notifyOnCallsButton, c);

		c.gridy = 2;
		deleteAfterFetchButton = new JCheckBox(JFritz
				.getMessage("delete_after_fetch")); //$NON-NLS-1$
		cPanel.add(deleteAfterFetchButton, c);

		c.gridy = 3;
		lookupAfterFetchButton = new JCheckBox(JFritz
				.getMessage("lookup_after_fetch")); //$NON-NLS-1$
		cPanel.add(lookupAfterFetchButton, c);

		c.gridy = 4;
		showCallByCallColumnButton = new JCheckBox(JFritz
				.getMessage("show_callbyball_column")); //$NON-NLS-1$
		cPanel.add(showCallByCallColumnButton, c);

		c.gridy = 5;
		showCommentColumnButton = new JCheckBox(JFritz
				.getMessage("show_comment_column")); //$NON-NLS-1$
		cPanel.add(showCommentColumnButton, c);

		c.gridy = 6;
		showPortColumnButton = new JCheckBox(JFritz
				.getMessage("show_port_column")); //$NON-NLS-1$
		cPanel.add(showPortColumnButton, c);

		c.gridy = 7;
		fetchAfterStandby = new JCheckBox(JFritz
				.getMessage("fetch_after_standby")); //$NON-NLS-1$
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

		JLabel label = new JLabel(""); //$NON-NLS-1$
		c.gridy = 2;
		label = new JLabel(JFritz.getMessage("language") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		localePane.add(label, c);

		String lang = JFritzUtils.getFullPath(langID);
		File file = new File(lang);
		FilenameFilter props = new StartEndFilenameFilter("jfritz","properties");//$NON-NLS-1$,  //$NON-NLS-2$
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
			images[i].setDescription(JFritz.getLocaleMeaning(localeList[i]));
		}


		c.fill = GridBagConstraints.HORIZONTAL;

		languageCombo = new JComboBox(images);
		languageComboBoxRenderer renderer = new languageComboBoxRenderer();
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
			jfritz.stopCallMonitor();
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
					JFritz.setProperty("option.callMonitorType", String //$NON-NLS-1$
							.valueOf(callMonitorCombo.getSelectedIndex()));
					JFritz.setProperty("box.password", Encryption //$NON-NLS-1$
							.encrypt(password));
					JFritz.setProperty("box.address", address.getText()); //$NON-NLS-1$
					jfritz.getFritzBox().detectFirmware();
					jfritz.getJframe().switchMonitorButton();
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
									configDialog, jfritz);
							break;
						case 2 :
							callMonitorConfigDialog = new TelnetConfigDialog(
									configDialog, jfritz);
							break;
						case 3 :
							callMonitorConfigDialog = new SyslogConfigDialog(
									configDialog, jfritz);
							break;
						case 4 :
							callMonitorConfigDialog = new YacConfigDialog(
									configDialog, jfritz);
							break;
						case 5 :
							callMonitorConfigDialog = new CallmessageConfigDialog(
									configDialog, jfritz);
							break;
					}
					if (callMonitorConfigDialog != null) {
						callMonitorConfigDialog.showConfigDialog();
					}

				}
			}
		};

		callMonitorPane = new JPanel();
		callMonitorPane.setLayout(new BorderLayout());
		callMonitorCombo = new JComboBox();
		callMonitorCombo.addItem(JFritz.getMessage("no_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("fritz_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("telnet_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("syslog_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("yac_call_monitor")); //$NON-NLS-1$
		callMonitorCombo.addItem(JFritz.getMessage("callmessage_call_monitor")); //$NON-NLS-1$
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
		callMonitorOptionsButton = new JButton(JFritz.getMessage("config")); //$NON-NLS-1$
		callMonitorOptionsButton.setActionCommand("startCallMonitorOptions"); //$NON-NLS-1$
		callMonitorOptionsButton.addActionListener(actionListener);
		pane.add(callMonitorOptionsButton, c);

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

		return callMonitorPane;
	}

	protected JPanel createMessagePane() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		JLabel text = new JLabel(JFritz.getMessage("popup_for_information")); //$NON-NLS-1$
		panel.add(text, c);

		final JLabel delayLbl = new JLabel(JFritz.getMessage("popup_delay"));

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (popupNoButton.isSelected()) {
					JFritz.setProperty("option.popuptype", "0"); //$NON-NLS-1$,  //$NON-NLS-2$
					delayLbl.setVisible(false);
					popupDelay.setVisible(false);

				} else if (popupDialogButton.isSelected()) {
					JFritz.setProperty("option.popuptype", "1"); //$NON-NLS-1$,  //$NON-NLS-2$
					delayLbl.setVisible(true);
					popupDelay.setVisible(true);
				} else {
					JFritz.setProperty("option.popuptype", "2"); //$NON-NLS-1$,  //$NON-NLS-2$
					delayLbl.setVisible(false);
					popupDelay.setVisible(false);
				}
			}
		};

		ButtonGroup popupGroup = new ButtonGroup();
		c.gridy = 1;
		popupNoButton = new JRadioButton(JFritz.getMessage("no_popups")); //$NON-NLS-1$
		popupNoButton.addActionListener(actionListener);
		popupGroup.add(popupNoButton);
		panel.add(popupNoButton, c);

		c.gridy = 2;
		popupDialogButton = new JRadioButton(JFritz.getMessage("popup_windows")); //$NON-NLS-1$
		popupDialogButton.addActionListener(actionListener);
		popupGroup.add(popupDialogButton);
		panel.add(popupDialogButton, c);

		c.gridy = 3;
		popupTrayButton = new JRadioButton(JFritz.getMessage("tray_messages")); //$NON-NLS-1$
		popupTrayButton.addActionListener(actionListener);
		popupGroup.add(popupTrayButton);
		panel.add(popupTrayButton, c);

		c.gridy = 4;
		c.insets.top = 10;
		popupDelay = new JTextField();
		popupDelay.setPreferredSize(new Dimension(30, 20));
		panel.add(delayLbl, c);
		c.gridx = 1;
		c.insets.left = 15;
		panel.add(popupDelay, c);


		return panel;
	}

	protected void drawDialog() {

		// Create JTabbedPane
		JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

		tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		okButton = new JButton(JFritz.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(JFritz.getMessage("cancel")); //$NON-NLS-1$
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
						.setText(JFritz.getMessage("timer")+": " + timerSlider.getValue() +" "+ JFritz.getMessage("abbreviation_minutes")); //$NON-NLS-1$,  //$NON-NLS-2$
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
				password = new String(pass.getPassword());
				pressed_OK = (source == pass || source == okButton);
				if (source == pass || source == okButton
						|| source == cancelButton) {
					ConfigDialog.this.setVisible(false);
				} else if (e.getActionCommand().equals("addresscombo")) { //$NON-NLS-1$
					int i = addressCombo.getSelectedIndex();
					SSDPPacket dev = (SSDPPacket) devices.get(i);
					address.setText(dev.getIP().getHostAddress());
					firmware = dev.getFirmware();
					setBoxTypeLabel();
					macLabel.setText(dev.getMAC());
				} else if (e.getActionCommand().equals("detectboxtype")) { //$NON-NLS-1$
					try {
						firmware = FritzBoxFirmware.detectFirmwareVersion(
								address.getText(), password, port.getText());

						// firmware = new FritzBoxFirmware("14", "1", "35");
						setBoxTypeLabel();
					} catch (WrongPasswordException e1) {
						Debug.err("Password wrong!"); //$NON-NLS-1$
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText(JFritz
								.getMessage("wrong_password")); //$NON-NLS-1$
						firmware = null;
					} catch (InvalidFirmwareException ife) {
						Debug.err("Invalid firmware detected"); //$NON-NLS-1$
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText(JFritz
								.getMessage("box_address_wrong")); //$NON-NLS-1$
						firmware = null;
					} catch (IOException e1) {
						Debug.err("Address wrong!"); //$NON-NLS-1$
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText(JFritz
								.getMessage("box_address_wrong")); //$NON-NLS-1$
						firmware = null;
					}
				} else if (e.getActionCommand().equals("fetchSIP")) { //$NON-NLS-1$
					try {
						JFritz.setProperty("box.password", Encryption.encrypt(password)); //$NON-NLS-1$
						JFritz.setProperty("box.address", address.getText()); //$NON-NLS-1$
						JFritz.setProperty("box.port", port.getText()); //$NON-NLS-1$
						jfritz.getFritzBox().detectFirmware();
						Vector data = jfritz.getFritzBox().retrieveSipProvider();
						jfritz.getSIPProviderTableModel().updateProviderList(
								data);
						jfritz.getSIPProviderTableModel()
								.fireTableDataChanged();
						jfritz.getCallerlist().fireTableDataChanged();

					} catch (WrongPasswordException e1) {
						jfritz.errorMsg(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
						Debug.errDlg(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
					} catch (IOException e1) {
						jfritz.errorMsg(JFritz.getMessage("box_address_wrong")); //$NON-NLS-1$
						Debug.errDlg(JFritz.getMessage("box_address_wrong")); //$NON-NLS-1$
					} catch (InvalidFirmwareException e1) {
						jfritz.errorMsg(JFritz.getMessage("unknown_firmware")); //$NON-NLS-1$
						Debug.errDlg(JFritz.getMessage("unknown_firmware")); //$NON-NLS-1$
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
				JFritz.getMessage("FRITZ!Box"), createBoxPane(actionListener)); //$NON-NLS-1$
		tpane.addTab(JFritz.getMessage("telephone"), createPhonePane()); //$NON-NLS-1$
		tpane
				.addTab(
						JFritz.getMessage("sip_numbers"), createSipPane(actionListener)); //$NON-NLS-1$
		JScrollPane callerListPaneScrollable = new JScrollPane(
				createCallerListPane());
		tpane.addTab(JFritz.getMessage("callerlist"), callerListPaneScrollable); //$NON-NLS-1$
		tpane.addTab(JFritz.getMessage("callmonitor"), createCallMonitorPane()); //$NON-NLS-1$
		tpane.addTab(JFritz.getMessage("messages"), createMessagePane()); //$NON-NLS-1$
		JScrollPane otherPaneScrollable = new JScrollPane(createOtherPane()); //$NON-NLS-1$
		tpane.addTab(JFritz.getMessage("other"), otherPaneScrollable); //$NON-NLS-1$
		tpane.addTab(JFritz.getMessage("language"),
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

	public void setBoxTypeLabel() {
		if (firmware != null) {
			boxtypeLabel.setForeground(Color.BLUE);
			boxtypeLabel.setText(firmware.getBoxName() + " (" //$NON-NLS-1$
					+ firmware.getFirmwareVersion() + ")"); //$NON-NLS-1$
		} else {
			boxtypeLabel.setForeground(Color.RED);
			boxtypeLabel.setText(JFritz.getMessage("unknown")); //$NON-NLS-1$
		}
	}

	/**
	 * @return Returns the jfritz object.
	 */
	public final JFritz getJfritz() {
		return jfritz;
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
			startCallMonitorButton.setText(JFritz
					.getMessage("start_call_monitor")); //$NON-NLS-1$
			startCallMonitorButton.setSelected(false);
			jfritz.getJframe().getMonitorButton().setSelected(false);
		} else if (option == JFritz.CALLMONITOR_STOP) {
			startCallMonitorButton.setText(JFritz
					.getMessage("stop_call_monitor")); //$NON-NLS-1$
			startCallMonitorButton.setSelected(true);
			jfritz.getJframe().getMonitorButton().setSelected(true);
		}
	}

	public FritzBoxFirmware getFirmware() {
		return firmware;
	}
}