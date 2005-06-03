/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz.dialogs;

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

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.upnp.SSDPPacket;
import de.moonflower.jfritz.window.JFritzWindow;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 * TODO: A lot of I18N..
 */
public class ConfigDialog extends JDialog {

	private JFritz jfritz;

	private JComboBox addressCombo;

	private JTextField address, areaCode, countryCode, areaPrefix,
			countryPrefix;

	private JPasswordField pass;

	private JSlider timerSlider;

	private JButton okButton, cancelButton, boxtypeButton;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
			notifyOnCallsButton, confirmOnExitButton, startMinimizedButton,
			timerAfterStartButton;

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
		setTitle(getJfritz().getMessages().getString("config"));
		devices = jfritz.getDevices();
		drawDialog();
		setValues();
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	/**
	 * sets properties to dialog components
	 *
	 * @param properties
	 */
	public void setValues() {
		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(getJfritz()
				.getProperties().getProperty("option.notifyOnCalls")));
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(getJfritz()
				.getProperties().getProperty("option.fetchAfterStart")));
		timerAfterStartButton.setSelected(JFritzUtils.parseBoolean(getJfritz()
				.getProperties().getProperty("option.timerAfterStart")));
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(getJfritz()
				.getProperties().getProperty("option.deleteAfterFetch")));
		confirmOnExitButton.setSelected(JFritzUtils.parseBoolean(getJfritz()
				.getProperties().getProperty("option.confirmOnExit", "true")));
		startMinimizedButton
				.setSelected(JFritzUtils.parseBoolean(getJfritz()
						.getProperties().getProperty("option.startMinimized",
								"false")));

		pass.setText(getJfritz().getProperties().getProperty("box.password"));
		address.setText(getJfritz().getProperties().getProperty("box.address"));
		areaCode.setText(getJfritz().getProperties().getProperty("area.code"));
		countryCode.setText(getJfritz().getProperties().getProperty(
				"country.code"));
		areaPrefix.setText(getJfritz().getProperties().getProperty(
				"area.prefix"));
		countryPrefix.setText(getJfritz().getProperties().getProperty(
				"country.prefix"));
		timerSlider.setValue(Integer.parseInt(getJfritz().getProperties()
				.getProperty("fetch.timer")));

		for (int i = 0; i < devices.size(); i++) {
			SSDPPacket p = (SSDPPacket) devices.get(i);
			if (p.getIP().getHostAddress().equals(address.getText())) {
				addressCombo.setSelectedIndex(i);
			}

		}

		try {
			firmware = new FritzBoxFirmware(getJfritz().getProperties()
					.getProperty("box.firmware"));
		} catch (InvalidFirmwareException e) {
		}
		setBoxTypeLabel();
		for (int i = 0; i < 10; i++) {
			String sipstr = getJfritz().getProperties().getProperty("SIP" + i);
			if (sipstr != null) {
				String[] parts = sipstr.split("@");
				SipProvider sip = new SipProvider(i, parts[0], parts[1]);
				sipmodel.addProvider(sip);
			}
		}
	}

	/**
	 * stores values in dialog components to programm properties
	 *
	 * @param properties
	 */
	public void storeValues(JFritzProperties properties) {
		// Remove leading "0" from areaCode
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));

		properties.setProperty("option.notifyOnCalls", Boolean
				.toString(notifyOnCallsButton.isSelected()));
		properties.setProperty("option.fetchAfterStart", Boolean
				.toString(fetchAfterStartButton.isSelected()));
		properties.setProperty("option.timerAfterStart", Boolean
				.toString(timerAfterStartButton.isSelected()));
		properties.setProperty("option.deleteAfterFetch", Boolean
				.toString(deleteAfterFetchButton.isSelected()));
		properties.setProperty("option.confirmOnExit", Boolean
				.toString(confirmOnExitButton.isSelected()));
		properties.setProperty("option.startMinimized", Boolean
				.toString(startMinimizedButton.isSelected()));

		properties.setProperty("box.password", new String(pass.getPassword()));
		properties.setProperty("box.address", address.getText());
		properties.setProperty("area.code", areaCode.getText());
		properties.setProperty("country.code", countryCode.getText());
		properties.setProperty("area.prefix", areaPrefix.getText());
		properties.setProperty("country.prefix", countryPrefix.getText());
		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		properties.setProperty("fetch.timer", Integer.toString(timerSlider
				.getValue()));

		if (firmware != null) {
			properties.setProperty("box.firmware", firmware
					.getFirmwareVersion());
		} else {
			properties.remove("box.firmware");
		}

		Enumeration en = sipmodel.getData().elements();
		while (en.hasMoreElements()) {
			SipProvider sip = (SipProvider) en.nextElement();
			properties.setProperty("SIP" + sip.getProviderID(), sip.toString());
		}
	}

	protected void drawDialog() {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		// Create JTabbedPane
		JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);
		JPanel boxpane = new JPanel(gridbag);
		JPanel phonepane = new JPanel(gridbag);
		JPanel otherpane = new JPanel();
		JPanel quickdialpane = new JPanel(gridbag);
		JPanel sippane = new JPanel();

		tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		boxpane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

		JLabel label;
		okButton = new JButton("Okay");
		cancelButton = new JButton("Abbruch");
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
								address.getText(), new String(pass
										.getPassword()));

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
								.getText(), new String(pass.getPassword()),
								firmware);
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

		c.gridy = 1;
		ImageIcon boxicon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/fritzbox.png")));
		label = new JLabel("");
		label.setIcon(boxicon);
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		label = new JLabel("FRITZ!Box-Einstellungen");
		gridbag.setConstraints(label, c);
		boxpane.add(label);

		c.gridy = 2;
		label = new JLabel("FRITZ!Box: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		address = new JTextField("", 16);
		gridbag.setConstraints(address, c);

		addressCombo = new JComboBox();
		Enumeration en = devices.elements();
		while (en.hasMoreElements()) {
			SSDPPacket p = (SSDPPacket) en.nextElement();
			addressCombo.addItem(p.getShortName());
		}

		gridbag.setConstraints(addressCombo, c);
		addressCombo.setActionCommand("addresscombo");
		addressCombo.addActionListener(actionListener);
		boxpane.add(addressCombo);

		c.gridy = 3;
		label = new JLabel("IP-Addresse: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		address = new JTextField("", 16);
		gridbag.setConstraints(address, c);
		boxpane.add(address);

		c.gridy = 4;
		label = new JLabel("Passwort: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		pass = new JPasswordField("", 16);
		gridbag.setConstraints(pass, c);
		boxpane.add(pass);

		c.gridy = 5;
		boxtypeButton = new JButton("Typ erkennen");
		boxtypeButton.setActionCommand("detectboxtype");
		boxtypeButton.addActionListener(actionListener);
		gridbag.setConstraints(boxtypeButton, c);
		boxpane.add(boxtypeButton);
		boxtypeLabel = new JLabel();
		gridbag.setConstraints(boxtypeLabel, c);
		boxpane.add(boxtypeLabel);

		c.gridy = 6;
		label = new JLabel("MAC-Addresse: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		macLabel = new JLabel();
		gridbag.setConstraints(macLabel, c);
		boxpane.add(macLabel);

		c.gridy = 1;
		label = new JLabel("Ortsvorwahl: ");
		gridbag.setConstraints(label, c);
		phonepane.add(label);
		areaCode = new JTextField("", 6);
		gridbag.setConstraints(areaCode, c);
		phonepane.add(areaCode);

		c.gridy = 2;
		label = new JLabel("Landesvorwahl: ");
		gridbag.setConstraints(label, c);
		phonepane.add(label);
		countryCode = new JTextField("", 3);
		gridbag.setConstraints(countryCode, c);
		phonepane.add(countryCode);

		c.gridy = 3;
		label = new JLabel("Orts-Prefix: ");
		gridbag.setConstraints(label, c);
		phonepane.add(label);
		areaPrefix = new JTextField("", 3);
		gridbag.setConstraints(areaPrefix, c);
		phonepane.add(areaPrefix);

		c.gridy = 4;
		label = new JLabel("Landes-Prefix: ");
		gridbag.setConstraints(label, c);
		phonepane.add(label);
		countryPrefix = new JTextField("", 3);
		gridbag.setConstraints(countryPrefix, c);
		phonepane.add(countryPrefix);

		otherpane.setLayout(new BoxLayout(otherpane, BoxLayout.Y_AXIS));
		timerLabel = new JLabel("Timer (in min): ");
		otherpane.add(timerLabel);
		otherpane.add(timerSlider);

		fetchAfterStartButton = new JCheckBox("Nach Programmstart Liste holen");
		otherpane.add(fetchAfterStartButton);

		timerAfterStartButton = new JCheckBox(
				"Nach Programmstart Timer aktivieren");
		otherpane.add(timerAfterStartButton);

		deleteAfterFetchButton = new JCheckBox("Nach Laden auf Box löschen");
		otherpane.add(deleteAfterFetchButton);
		// TODO Make this work :)
		//		deleteAfterFetchButton.setEnabled(false);

		startMinimizedButton = new JCheckBox("Programm minimiert starten");
		otherpane.add(startMinimizedButton);

		notifyOnCallsButton = new JCheckBox(
				"Bei neuen Anrufen Fenster in den Vordergrund");
		otherpane.add(notifyOnCallsButton);

		confirmOnExitButton = new JCheckBox("Bei Beenden nachfragen");
		otherpane.add(confirmOnExitButton);

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
		// Create OK/Cancel Panel
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel okcancelpanel = new JPanel();
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		okcancelpanel.add(okButton);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
		okcancelpanel.add(cancelButton);
		gridbag.setConstraints(okcancelpanel, c);

		tpane.addTab("FRITZ!Box", boxpane); // TODO I18N
		tpane.addTab("Telefon", phonepane);
		tpane.addTab("SIP-Nummern", sippane);
		tpane.addTab("Weiteres", otherpane);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tpane, BorderLayout.CENTER);
		getContentPane().add(okcancelpanel, BorderLayout.SOUTH);

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