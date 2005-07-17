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
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.JToggleButton;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.network.SSDPPacket;
import de.moonflower.jfritz.utils.network.SyslogListener;
import de.moonflower.jfritz.utils.network.TelnetListener;
import de.moonflower.jfritz.utils.network.YAClistener;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 * TODO: A lot of I18N..
 */
public class ConfigDialog extends JDialog {

	private JFritz jfritz;

	private JComboBox addressCombo, callMonitorCombo;

	private JTextField address, areaCode, countryCode, areaPrefix,
			countryPrefix, yacPort;

	private JPasswordField pass;

	private String encodedPassword = "";

	private JSlider timerSlider;

	private JButton okButton, cancelButton, boxtypeButton,
			startSyslogOnFritzBoxButton;

	private JToggleButton startCallMonitorButton;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
			notifyOnCallsButton, confirmOnExitButton, startMinimizedButton,
			timerAfterStartButton, passwordAfterStartButton, soundButton,
			callMonitorAfterStartButton, lookupAfterFetchButton;

	private JPanel callMonitorPane, yacMonitorPane, telnetMonitorPane,
			syslogMonitorPane;

	private JLabel boxtypeLabel, macLabel, timerLabel;

	private FritzBoxFirmware firmware;

	private SipProviderTableModel sipmodel;

	private boolean pressed_OK = false;

	private Vector devices;

	public ConfigDialog(Frame parent) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
			jfritz = ((JFritzWindow) parent).getJFritz();
		}
		setTitle(JFritz.getMessage("config"));
		devices = jfritz.getDevices();
		drawDialog();
		setValues();
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	/**
	 * Sets properties to dialog components
	 */
	public void setValues() {
		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.notifyOnCalls")));
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.fetchAfterStart")));
		timerAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.timerAfterStart")));
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.deleteAfterFetch")));
		confirmOnExitButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.confirmOnExit", "true")));
		startMinimizedButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.startMinimized", "false")));
		soundButton.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"option.playSounds", "true")));

		callMonitorCombo.setSelectedIndex(Integer.parseInt(JFritz.getProperty(
				"option.callMonitorType", "0")));

		if (jfritz.getCallMonitor() == null) {
			startCallMonitorButton.setSelected(false);
		} else {
			startCallMonitorButton.setSelected(true);
		}
		yacPort.setText(JFritz.getProperty("option.yacport", "10629"));
		callMonitorAfterStartButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.autostartcallmonitor", "false")));
		if (startCallMonitorButton.isSelected()) {
			startCallMonitorButton.setText("Stop Call-Monitor");
		} else {
			startCallMonitorButton.setText("Start Call-Monitor");
		}

		lookupAfterFetchButton.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.lookupAfterFetch", "false")));

		boolean pwAfterStart = !Encryption.decrypt(
				JFritz.getProperty("jfritz.password", "")).equals(
				JFritz.PROGRAM_SECRET
						+ Encryption.decrypt(JFritz.getProperty("box.password",
								"")));
		passwordAfterStartButton.setSelected(pwAfterStart);

		try {
			pass.setText(URLEncoder.encode(Encryption.decrypt(JFritz
					.getProperty("box.password")), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Debug
					.msg("Exception (ConfigDialog:setValues): UnsupportedEncodungException");
		}
		encodedPassword = Encryption
				.decrypt(JFritz.getProperty("box.password"));
		address.setText(JFritz.getProperty("box.address"));
		areaCode.setText(JFritz.getProperty("area.code"));
		countryCode.setText(JFritz.getProperty("country.code"));
		areaPrefix.setText(JFritz.getProperty("area.prefix"));
		countryPrefix.setText(JFritz.getProperty("country.prefix"));
		timerSlider.setValue(Integer
				.parseInt(JFritz.getProperty("fetch.timer")));

		for (int i = 0; i < devices.size(); i++) {
			SSDPPacket p = (SSDPPacket) devices.get(i);
			if (p.getIP().getHostAddress().equals(address.getText())) {
				addressCombo.setSelectedIndex(i);
			}

		}

		try {
			firmware = new FritzBoxFirmware(JFritz.getProperty("box.firmware"));
		} catch (InvalidFirmwareException e) {
		}
		setBoxTypeLabel();
		for (int i = 0; i < 10; i++) {
			String sipstr = JFritz.getProperty("SIP" + i);
			if (sipstr != null && sipstr.length() > 0) {
				String[] parts = sipstr.split("@");
				SipProvider sip = new SipProvider(i, parts[0], parts[1]);
				sipmodel.addProvider(sip);
			}
		}
	}

	/**
	 * Stores values in dialog components to programm properties
	 */
	public void storeValues() {
		// Remove leading "0" from areaCode
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));

		JFritz.setProperty("option.notifyOnCalls", Boolean
				.toString(notifyOnCallsButton.isSelected()));
		JFritz.setProperty("option.fetchAfterStart", Boolean
				.toString(fetchAfterStartButton.isSelected()));
		JFritz.setProperty("option.timerAfterStart", Boolean
				.toString(timerAfterStartButton.isSelected()));
		JFritz.setProperty("option.deleteAfterFetch", Boolean
				.toString(deleteAfterFetchButton.isSelected()));
		JFritz.setProperty("option.confirmOnExit", Boolean
				.toString(confirmOnExitButton.isSelected()));
		JFritz.setProperty("option.startMinimized", Boolean
				.toString(startMinimizedButton.isSelected()));
		JFritz.setProperty("option.playSounds", Boolean.toString(soundButton
				.isSelected()));
		JFritz.setProperty("option.startcallmonitor", Boolean
				.toString(startCallMonitorButton.isSelected()));
		JFritz.setProperty("option.yacport", yacPort.getText());
		JFritz.setProperty("option.autostartcallmonitor", Boolean
				.toString(callMonitorAfterStartButton.isSelected()));
		JFritz.setProperty("option.callMonitorType", String
				.valueOf(callMonitorCombo.getSelectedIndex()));

		if (!passwordAfterStartButton.isSelected()) {
			JFritz.setProperty("jfritz.password", Encryption
					.encrypt(JFritz.PROGRAM_SECRET + encodedPassword));
		} else {
			JFritz.removeProperty("jfritz.password");
		}

		JFritz.setProperty("option.lookupAfterFetch", Boolean
				.toString(lookupAfterFetchButton.isSelected()));

		JFritz.setProperty("box.password", Encryption.encrypt(encodedPassword));
		JFritz.setProperty("box.address", address.getText());
		JFritz.setProperty("area.code", areaCode.getText());
		JFritz.setProperty("country.code", countryCode.getText());
		JFritz.setProperty("area.prefix", areaPrefix.getText());
		JFritz.setProperty("country.prefix", countryPrefix.getText());
		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		JFritz.setProperty("fetch.timer", Integer.toString(timerSlider
				.getValue()));

		if (firmware != null) {
			JFritz.setProperty("box.firmware", firmware.getFirmwareVersion());
		} else {
			JFritz.removeProperty("box.firmware");
		}

		Enumeration en = sipmodel.getData().elements();
		while (en.hasMoreElements()) {
			SipProvider sip = (SipProvider) en.nextElement();
			JFritz.setProperty("SIP" + sip.getProviderID(), sip.toString());
		}
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
						"/de/moonflower/jfritz/resources/images/fritzbox.png")));
		JLabel label = new JLabel("");
		label.setIcon(boxicon);
		boxpane.add(label, c);
		label = new JLabel("FRITZ!Box-Einstellungen");
		boxpane.add(label, c);

		c.gridy = 2;
		label = new JLabel("FRITZ!Box: ");
		boxpane.add(label, c);
		address = new JTextField("", 16);

		addressCombo = new JComboBox();
		Enumeration en = devices.elements();
		while (en.hasMoreElements()) {
			SSDPPacket p = (SSDPPacket) en.nextElement();
			addressCombo.addItem(p.getShortName());
		}

		addressCombo.setActionCommand("addresscombo");
		addressCombo.addActionListener(actionListener);
		boxpane.add(addressCombo, c);

		c.gridy = 3;
		label = new JLabel("IP-Addresse: ");
		boxpane.add(label, c);
		address = new JTextField("", 16);
		boxpane.add(address, c);

		c.gridy = 4;
		label = new JLabel("Passwort: ");
		boxpane.add(label, c);
		pass = new JPasswordField("", 16);
		boxpane.add(pass, c);

		c.gridy = 5;
		boxtypeButton = new JButton("Typ erkennen");
		boxtypeButton.setActionCommand("detectboxtype");
		boxtypeButton.addActionListener(actionListener);
		boxpane.add(boxtypeButton, c);
		boxtypeLabel = new JLabel();
		boxpane.add(boxtypeLabel, c);

		c.gridy = 6;
		label = new JLabel("MAC-Addresse: ");
		boxpane.add(label, c);
		macLabel = new JLabel();
		boxpane.add(macLabel, c);
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
		JLabel label = new JLabel("Ortsvorwahl: ");
		phonepane.add(label, c);
		areaCode = new JTextField("", 6);
		phonepane.add(areaCode, c);

		c.gridy = 2;
		label = new JLabel("Landesvorwahl: ");
		phonepane.add(label, c);
		countryCode = new JTextField("", 3);
		phonepane.add(countryCode, c);

		c.gridy = 3;
		label = new JLabel("Orts-Prefix: ");
		phonepane.add(label, c);
		areaPrefix = new JTextField("", 3);
		phonepane.add(areaPrefix, c);

		c.gridy = 4;
		label = new JLabel("Landes-Prefix: ");
		phonepane.add(label, c);
		countryPrefix = new JTextField("", 3);
		phonepane.add(countryPrefix, c);
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
		sipmodel = new SipProviderTableModel();
		JTable siptable = new JTable(sipmodel) {
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
		JButton b1 = new JButton("Von der Box holen");
		b1.setActionCommand("fetchSIP");
		b1.addActionListener(actionListener);
		JButton b2 = new JButton("Auf die Box speichern");
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
		timerLabel = new JLabel("Timer (in min): ");
		otherpane.add(timerLabel);
		otherpane.add(timerSlider);

		passwordAfterStartButton = new JCheckBox(
				"Vor Programmstart Passwort erfragen?");
		otherpane.add(passwordAfterStartButton);

		timerAfterStartButton = new JCheckBox(
				"Nach Programmstart Timer aktivieren");
		otherpane.add(timerAfterStartButton);

		startMinimizedButton = new JCheckBox("Programm minimiert starten");
		otherpane.add(startMinimizedButton);

		notifyOnCallsButton = new JCheckBox(
				"Bei neuen Anrufen Fenster in den Vordergrund");
		otherpane.add(notifyOnCallsButton);

		confirmOnExitButton = new JCheckBox("Bei Beenden nachfragen");
		otherpane.add(confirmOnExitButton);
		return otherpane;
	}

	protected JPanel createCallerListPane() {
		JPanel cPanel = new JPanel();

		cPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		fetchAfterStartButton = new JCheckBox("Nach Programmstart Liste holen");
		cPanel.add(fetchAfterStartButton, c);

		c.gridy = 1;
		deleteAfterFetchButton = new JCheckBox("Nach Laden auf Box löschen");
		cPanel.add(deleteAfterFetchButton, c);

		c.gridy = 2;
		lookupAfterFetchButton = new JCheckBox(
				"Nach Laden Rückwärtssuche ausführen");
		cPanel.add(lookupAfterFetchButton, c);

		return cPanel;
	}

	protected void stopAllCallMonitors() {
		if (startCallMonitorButton.isSelected()) {
			startCallMonitorButton.setText("Starte Anrufmonitor");
			startCallMonitorButton.setSelected(false);
			jfritz.stopCallMonitor();
		}
	}

	protected JPanel createCallMonitorPane() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("comboboxchanged".equalsIgnoreCase(e.getActionCommand())) {
					// Zur Darstellung der gewünschten Einstellungspanels
					switch (callMonitorCombo.getSelectedIndex()) {
					case 0: {
						startCallMonitorButton.setVisible(false);
						callMonitorAfterStartButton.setVisible(false);
						soundButton.setVisible(false);
						yacMonitorPane.setVisible(false);
						telnetMonitorPane.setVisible(false);
						syslogMonitorPane.setVisible(false);
						callMonitorPane.repaint();
						Debug.msg("Kein Anrufmonitor erwünscht");
						stopAllCallMonitors();
						break;
					}
					case 1: {
						startCallMonitorButton.setVisible(true);
						callMonitorAfterStartButton.setVisible(true);
						soundButton.setVisible(true);
						yacMonitorPane.setVisible(false);
						telnetMonitorPane.setVisible(true);
						syslogMonitorPane.setVisible(false);
						callMonitorPane.repaint();
						Debug.msg("Telnet Anrufmonitor gewählt");
						stopAllCallMonitors();
						break;

					}
					case 2: {
						startCallMonitorButton.setVisible(true);
						callMonitorAfterStartButton.setVisible(true);
						soundButton.setVisible(true);
						yacMonitorPane.setVisible(false);
						telnetMonitorPane.setVisible(false);
						syslogMonitorPane.setVisible(true);
						callMonitorPane.repaint();
						Debug.msg("Syslog Anrufmonitor gewählt");
						stopAllCallMonitors();
						break;
					}
					case 3: {
						startCallMonitorButton.setVisible(true);
						callMonitorAfterStartButton.setVisible(true);
						soundButton.setVisible(true);
						yacMonitorPane.setVisible(true);
						telnetMonitorPane.setVisible(false);
						syslogMonitorPane.setVisible(false);
						callMonitorPane.repaint();
						Debug.msg("YAC Anrufmonitor gewählt");
						stopAllCallMonitors();
						break;
					}
					}
				} else if ("startcallmonitor".equalsIgnoreCase(e
						.getActionCommand())) {
					JFritz.setProperty("option.yacport", yacPort.getText());
					// Aktion des StartCallMonitorButtons
					if (startCallMonitorButton.isSelected()) {
						if (jfritz.getCallMonitor() != null) {
							jfritz.getCallMonitor().stopCallMonitor();
						}
						switch (callMonitorCombo.getSelectedIndex()) {
						case 1: {
							jfritz.setCallMonitor(new TelnetListener(jfritz));
							break;
						}
						case 2: {
							jfritz.setCallMonitor(new SyslogListener(jfritz));
							break;
						}
						case 3: {
							jfritz.setCallMonitor(new YAClistener(Integer
									.parseInt(JFritz.getProperty(
											"option.yacport", "10629"))));
							break;
						}
						}
						startCallMonitorButton.setText("Stoppe Anrufmonitor");
					} else {
						jfritz.stopCallMonitor();
						startCallMonitorButton.setText("Starte Anrufmonitor");
					}
				}

			}
		};

		callMonitorPane = new JPanel();
		callMonitorPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		callMonitorCombo = new JComboBox();
		callMonitorCombo.addItem("Keiner");
		callMonitorCombo.addItem("Telnet");
		callMonitorCombo.addItem("Syslog");
		callMonitorCombo.addItem("YAC");
		callMonitorPane.add(callMonitorCombo, c);

		c.gridy = 1;
		c.gridwidth = 2;
		startCallMonitorButton = new JToggleButton();
		startCallMonitorButton.setActionCommand("startCallMonitor");
		startCallMonitorButton.addActionListener(actionListener);
		callMonitorPane.add(startCallMonitorButton, c);

		c.gridy = 2;
		c.gridwidth = 2;
		callMonitorAfterStartButton = new JCheckBox(
				"Call-Monitor nach Programmstart automatisch starten?");
		callMonitorPane.add(callMonitorAfterStartButton, c);

		soundButton = new JCheckBox("Bei eingehenden Anrufen Sound abspielen");
		c.gridy = 3;
		c.gridwidth = 3;
		callMonitorPane.add(soundButton, c);

		c.gridy = 4;
		telnetMonitorPane = new JPanel();
		telnetMonitorPane = createTelnetPane();
		syslogMonitorPane = new JPanel();
		syslogMonitorPane = createSyslogPane();
		yacMonitorPane = new JPanel();
		yacMonitorPane = createYACPane();
		callMonitorPane.add(telnetMonitorPane, c);
		callMonitorPane.add(syslogMonitorPane, c);
		callMonitorPane.add(yacMonitorPane, c);

		callMonitorCombo.addActionListener(actionListener);

		return callMonitorPane;
	}

	protected JPanel createYACPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JLabel label = new JLabel("YAC-Port: ");
		panel.add(label, c);
		yacPort = new JTextField("", 5);
		panel.add(yacPort, c);

		return panel;
	}

	protected JPanel createTelnetPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		// TODO: UserName und Passwort einstellen lassen
		/**
		 * JLabel label = new JLabel("YAC-Port: "); panel.add(label, c); yacPort =
		 * new JTextField("", 5); panel.add(yacPort, c);
		 */
		return panel;
	}

	protected JPanel createSyslogPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		// TODO: Syslog Pass-Through
		startSyslogOnFritzBoxButton = new JButton(
				"Starte Syslog auf der FritzBox");
		startSyslogOnFritzBoxButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jfritz.getJframe().getFetchButton().doClick();
				SyslogListener.startSyslogOnFritzBox();
			}

		});
		panel.add(startSyslogOnFritzBoxButton, c);
		/**
		 * JLabel label = new JLabel("YAC-Port: "); panel.add(label, c); yacPort =
		 * new JTextField("", 5); panel.add(yacPort, c);
		 */
		return panel;
	}

	protected void drawDialog() {

		// Create JTabbedPane
		JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

		tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label;
		okButton = new JButton(JFritz.getMessage("okay"));
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png"))));
		cancelButton = new JButton(JFritz.getMessage("cancel"));
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
						.setText("Timer: " + timerSlider.getValue() + " min.");
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
				try {
					encodedPassword = URLDecoder.decode(new String(pass
							.getPassword()), "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					Debug
							.msg("Exception (ConfigDialog:drawDialog): UnsupportedEncodungException");
				}
				pressed_OK = (source == pass || source == okButton);
				if (source == pass || source == okButton
						|| source == cancelButton) {
					ConfigDialog.this.setVisible(false);
				} else if (e.getActionCommand().equals("addresscombo")) {
					int i = addressCombo.getSelectedIndex();
					SSDPPacket dev = (SSDPPacket) devices.get(i);
					address.setText(dev.getIP().getHostAddress());
					firmware = dev.getFirmware();
					setBoxTypeLabel();
					macLabel.setText(dev.getMAC());
				} else if (e.getActionCommand().equals("detectboxtype")) {
					try {
						firmware = FritzBoxFirmware.detectFirmwareVersion(
								address.getText(), encodedPassword);

						// firmware = new FritzBoxFirmware("14", "1", "35");
						setBoxTypeLabel();
					} catch (WrongPasswordException e1) {
						Debug.err("Password wrong!");
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText("Passwort ungültig!");
						firmware = null;
					} catch (IOException e1) {
						Debug.err("Address wrong!");
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText("Box-Adresse ungültig!");
						firmware = null;
					}
				} else if (e.getActionCommand().equals("fetchSIP")) {
					try {
						Vector data = JFritzUtils.retrieveSipProvider(address
								.getText(), encodedPassword, firmware);
						sipmodel.setData(data);
						sipmodel.fireTableDataChanged();
						jfritz.getCallerlist().fireTableDataChanged();

					} catch (WrongPasswordException e1) {
						jfritz.errorMsg("Passwort ungültig!");
					} catch (IOException e1) {
						jfritz.errorMsg("FRITZ!Box-Adresse ungültig!");
					} catch (InvalidFirmwareException e1) {
						jfritz.errorMsg("Firmware-Erkennung gescheitert!");
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

		tpane.addTab("FRITZ!Box", createBoxPane(actionListener)); // TODO I18N
		tpane.addTab("Telefon", createPhonePane());
		tpane.addTab("SIP-Nummern", createSipPane(actionListener));
		tpane.addTab("Anrufliste", createCallerListPane());
		tpane.addTab("Anrufmonitor", createCallMonitorPane());
		tpane.addTab("Weiteres", createOtherPane());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tpane, BorderLayout.CENTER);
		getContentPane().add(okcancelpanel, BorderLayout.SOUTH);
		c.fill = GridBagConstraints.HORIZONTAL;

		addKeyListener(keyListener);

		setSize(new Dimension(480, 350));
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
			boxtypeLabel.setText(firmware.getBoxName() + " ("
					+ firmware.getFirmwareVersion() + ")");
		} else {
			boxtypeLabel.setForeground(Color.RED);
			boxtypeLabel.setText("unbekannt");
		}
	}

	/**
	 * @return Returns the jfritz object.
	 */
	public final JFritz getJfritz() {
		return jfritz;
	}
}