/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 * TODO: A lot of I18N..
 */
public class ConfigDialog extends JDialog {

	protected JTextField address, areaCode, countryCode, areaPrefix,
			countryPrefix;

	protected JPasswordField pass;

	protected JSlider timerSlider;

	protected JButton okButton, cancelButton, boxtypeButton;

	protected JCheckBox deleteAfterFetchButton, fetchAfterStartButton;

	protected JLabel boxtypeLabel;

	protected FritzBoxFirmware firmware;

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

	public void setValues(Properties properties) {
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
	}

	public void storeValues(Properties properties) {
		// Remove leading "0" from areaCode
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));

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
		JPanel otherpane = new JPanel(gridbag);
		JPanel phonepane = new JPanel(gridbag);
		JPanel quickdialpane = new JPanel(gridbag);

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
				} else if (source == boxtypeButton) {

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

		label = new JLabel(": ");
		fetchAfterStartButton = new JCheckBox("Nach Programmstart Liste holen");
		otherpane.add(fetchAfterStartButton);
		// TODO Make this work :)
		fetchAfterStartButton.setEnabled(false);

		label = new JLabel("Nach Laden auf Box löschen: ");
		deleteAfterFetchButton = new JCheckBox("Nach Laden auf Box löschen");
		otherpane.add(deleteAfterFetchButton);
		// TODO Make this work :)
		deleteAfterFetchButton.setEnabled(false);

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
		tpane.addTab("FRITZ!Box", boxpane);
		tpane.addTab("Telefon", phonepane);
		tpane.addTab("Weiteres", otherpane);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tpane, BorderLayout.CENTER);
		getContentPane().add(okcancelpanel, BorderLayout.SOUTH);

		addKeyListener(keyListener);

		pack();
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