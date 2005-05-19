/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.table.TableCellRenderer;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 * TODO: A lot of I18N..
 */
public class ConfigDialog extends JDialog {

	private JTextField address, areaCode, countryCode, areaPrefix,
			countryPrefix;

	private JPasswordField pass;

	private JSlider timerSlider;

	private JButton okButton, cancelButton, boxtypeButton;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton, notifyOnCallsButton;

	private JLabel boxtypeLabel;

	private FritzBoxFirmware firmware;

	private SipProviderTableModel sipmodel;

	private boolean pressed_OK = false;

	public ConfigDialog(Frame parent) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		setTitle("JFritz Konfiguration");
		drawDialog();
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	/**
	 * sets properties to dialog components
	 *
	 * @param properties
	 */
	public void setValues(Properties properties) {
		notifyOnCallsButton.setSelected( Boolean.parseBoolean(properties.getProperty("option.notifyOnCalls")));
		fetchAfterStartButton.setSelected( Boolean.parseBoolean(properties.getProperty("option.fetchAfterStart")));
		deleteAfterFetchButton.setSelected( Boolean.parseBoolean(properties.getProperty("option.deleteAfterFetch")));
		pass.setText(properties.getProperty("box.password"));
		address.setText(properties.getProperty("box.address"));
		areaCode.setText(properties.getProperty("area.code"));
		countryCode.setText(properties.getProperty("country.code"));
		areaPrefix.setText(properties.getProperty("area.prefix"));
		countryPrefix.setText(properties.getProperty("country.prefix"));
		timerSlider.setValue(Integer.parseInt(properties
				.getProperty("fetch.timer")));
		try {
			firmware = new FritzBoxFirmware(properties
					.getProperty("box.firmware"));
		} catch (InvalidFirmwareException e) {
		}
		setBoxTypeLabel();
		for (int i = 0; i < 10; i++) {
			String sipstr = properties.getProperty("SIP"+i);
			if (sipstr != null) {
				String[] parts = sipstr.split("@");
				SipProvider sip = new SipProvider(i,parts[0],parts[1]);
				sipmodel.addProvider(sip);
			}
		}
	}

	/**
	 * stores values in dialog components to programm properties
	 *
	 * @param properties
	 */
	public void storeValues(Properties properties) {
		// Remove leading "0" from areaCode
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));

		properties.setProperty("option.notifyOnCalls", Boolean.toString(notifyOnCallsButton.isSelected()));
		properties.setProperty("option.fetchAfterStart", Boolean.toString(fetchAfterStartButton.isSelected()));
		properties.setProperty("option.deleteAfterFetch", Boolean.toString(deleteAfterFetchButton.isSelected()));
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
		c.anchor = GridBagConstraints.EAST;

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
				} else if (e.getActionCommand().equals("detectboxtype")) {
					try {
						firmware = FritzBoxFirmware.detectFirmwareVersion(
								address.getText(), new String(pass
										.getPassword()));

						// firmware = new FritzBoxFirmware("14", "1", "35");
						setBoxTypeLabel();
					} catch (WrongPasswordException e1) {
						System.err.println("Password wrong!");
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText("Passwort ungültig!");
						firmware = null;
					} catch (IOException e1) {
						System.err.println("Address wrong!");
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText("Box-Adresse ungültig!");
						firmware = null;
					}
				} else if (e.getActionCommand().equals("fetchSIP")) {
					try {
						Vector data = JFritzUtils.retrieveSipProvider(address
								.getText(), new String(pass.getPassword()),
								firmware.getBoxType());
						sipmodel.setData(data);
						sipmodel.fireTableDataChanged();
					} catch (WrongPasswordException e1) {
						System.err.println("Password wrong");
					} catch (IOException e1) {
						System.err.println("Box address wrong");
					}
				}
			}
		};

		c.gridy = 1;
		label = new JLabel("Fritz!Box-Addresse: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		address = new JTextField("", 16);
		gridbag.setConstraints(address, c);
		boxpane.add(address);

		c.gridy = 2;
		label = new JLabel("Fritz!Box-Passwort: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		pass = new JPasswordField("", 16);
		gridbag.setConstraints(pass, c);
		boxpane.add(pass);

		c.gridy = 3;
		label = new JLabel("Fritz!Box-Typ: ");
		gridbag.setConstraints(label, c);
		boxpane.add(label);
		boxtypeLabel = new JLabel();
		gridbag.setConstraints(boxtypeLabel, c);
		boxpane.add(boxtypeLabel);
		c.gridy = 4;
		boxtypeButton = new JButton("Typ erkennen");
		boxtypeButton.setActionCommand("detectboxtype");
		boxtypeButton.addActionListener(actionListener);

		gridbag.setConstraints(boxtypeButton, c);
		boxpane.add(boxtypeButton);

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
		label = new JLabel("Timer (in min): ");
		otherpane.add(label);
		otherpane.add(timerSlider);

		fetchAfterStartButton = new JCheckBox("Nach Programmstart Liste holen");
		otherpane.add(fetchAfterStartButton);
		// TODO Make this work :)
		fetchAfterStartButton.setEnabled(false);

		deleteAfterFetchButton = new JCheckBox("Nach Laden auf Box löschen");
		otherpane.add(deleteAfterFetchButton);
		// TODO Make this work :)
		deleteAfterFetchButton.setEnabled(false);

		notifyOnCallsButton = new JCheckBox("Bei neuen Anrufen benachrichtigen");
		otherpane.add(notifyOnCallsButton);
		// TODO Make this work :)
		// notifyOnCallsButton.setEnabled(false);

		// Create SIP Panel
		// TODO: To do it :-)

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

		setSize(new Dimension(400,350));
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
}