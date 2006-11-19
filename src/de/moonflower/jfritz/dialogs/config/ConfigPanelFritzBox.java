package de.moonflower.jfritz.dialogs.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.network.SSDPPacket;

public class ConfigPanelFritzBox extends JPanel implements ActionListener,
		ConfigPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -2094680014900642941L;

	private JComboBox addressCombo;

	private Vector devices;

	private JTextField address;

	private JPasswordField pass;

	private JTextField port;

	private JButton boxtypeButton;

	private JLabel boxtypeLabel;

	private String password;

	private FritzBoxFirmware firmware;

	public ConfigPanelFritzBox() {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
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
		add(label, c);
		label = new JLabel(Main.getMessage("FRITZ!Box_Preferences")); //$NON-NLS-1$
		add(label, c);

		c.gridy = 2;
		label = new JLabel(Main.getMessage("FRITZ!Box") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		add(label, c);

		addressCombo = new JComboBox();
		c.fill = GridBagConstraints.HORIZONTAL;
		boolean boxAddressAdded = false;

		// initialize the drop down box
		devices = JFritz.getDevices();
		if (devices != null) {
			Enumeration en = devices.elements();
			while (en.hasMoreElements()) {
				SSDPPacket p = (SSDPPacket) en.nextElement();
				// addressCombo.addItem(p.getShortName());
				addressCombo.addItem(p.getIP().getHostAddress());
				if (p.getIP().getHostAddress().equals(
						JFritz.getFritzBox().getAddress()))
					boxAddressAdded = true;
			}
		}

		// make sure the stored address is listed
		if (!boxAddressAdded)
			addressCombo.addItem(JFritz.getFritzBox().getAddress());

		addressCombo.setActionCommand("addresscombo"); //$NON-NLS-1$
		addressCombo.addActionListener(this);
		add(addressCombo, c);

		c.gridy = 3;
		label = new JLabel(Main.getMessage("ip_address") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		add(label, c);
		address = new JTextField("", 16); //$NON-NLS-1$
		address.setMinimumSize(new Dimension(200, 20));
		add(address, c);

		c.gridy = 4;
		label = new JLabel(Main.getMessage("password") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		add(label, c);
		pass = new JPasswordField("", 16); //$NON-NLS-1$
		pass.setMinimumSize(new Dimension(200, 20));
		add(pass, c);

		c.gridy = 5;
		label = new JLabel(Main.getMessage("box.port") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		add(label, c);
		port = new JTextField("", 16); //$NON-NLS-1$
		port.setMinimumSize(new Dimension(200, 20));
		add(port, c);

		c.gridy = 6;
		boxtypeButton = new JButton(Main.getMessage("detect_box_type")); //$NON-NLS-1$
		boxtypeButton.setActionCommand("detectboxtype"); //$NON-NLS-1$
		boxtypeButton.addActionListener(this);
		add(boxtypeButton, c);
		boxtypeLabel = new JLabel();
		add(boxtypeLabel, c);
	}

	public void actionPerformed(ActionEvent e) {

		password = new String(pass.getPassword());

		if (e.getActionCommand().equals("addresscombo")) { //$NON-NLS-1$
			int i = addressCombo.getSelectedIndex();
			if (devices.size() != 0) {
				SSDPPacket dev = (SSDPPacket) devices.get(i);
				address.setText(dev.getIP().getHostAddress());
				firmware = dev.getFirmware();
			}
			setBoxTypeLabel();

		} else if (e.getActionCommand().equals("detectboxtype")) { //$NON-NLS-1$
			try {
				firmware = FritzBoxFirmware.detectFirmwareVersion(address
						.getText(), password, port.getText());

				// firmware = new FritzBoxFirmware("14", "1", "35");
				setBoxTypeLabel();
			} catch (WrongPasswordException e1) {
				Debug.err("Password wrong!"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(Main.getMessage("wrong_password")); //$NON-NLS-1$
				firmware = null;
			} catch (InvalidFirmwareException ife) {
				Debug.err("Invalid firmware detected"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(Main.getMessage("box_address_wrong")); //$NON-NLS-1$
				firmware = null;
			} catch (IOException e1) {
				Debug.err("Address wrong!"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(Main.getMessage("box_address_wrong")); //$NON-NLS-1$
				firmware = null;
			}
			// firmware = new FritzBoxFirmware("14", "1", "35");
			setBoxTypeLabel();
		}
	}

	private void setBoxTypeLabel() {
		if (firmware != null) {
			boxtypeLabel.setForeground(Color.BLUE);
			boxtypeLabel.setText(firmware.getBoxName() + " (" //$NON-NLS-1$
					+ firmware.getFirmwareVersion() + ")"); //$NON-NLS-1$
		} else {
			boxtypeLabel.setForeground(Color.RED);
			boxtypeLabel.setText(Main.getMessage("unknown")); //$NON-NLS-1$
		}
	}

	public void loadSettings() {
		firmware = JFritz.getFritzBox().getFirmware(); //$NON-NLS-1$
		pass.setText(JFritz.getFritzBox().getPassword()); //$NON-NLS-1$
		password = JFritz.getFritzBox().getPassword(); //$NON-NLS-1$
		address.setText(JFritz.getFritzBox().getAddress()); //$NON-NLS-1$,  //$NON-NLS-2$
		port.setText(JFritz.getFritzBox().getPort()); //$NON-NLS-1$,  //$NON-NLS-2$

		if (devices != null) {
			for (int i = 0; i < devices.size(); i++) {
				SSDPPacket p = (SSDPPacket) devices.get(i);
				if (p.getIP().getHostAddress().equals(address.getText())) {
					addressCombo.setSelectedIndex(i);
				}
			}
		}
		setBoxTypeLabel();
	}

	public void saveSettings() {
		Main.setProperty("box.address", address.getText()); //$NON-NLS-1$
		Main.setProperty("box.password", Encryption.encrypt(password)); //$NON-NLS-1$
		Main.setProperty("box.port", port.getText()); //$NON-NLS-1$
		JFritz.getFritzBox().setAddress(address.getText());
		JFritz.getFritzBox().setPassword(password);
		JFritz.getFritzBox().setPort(port.getText());

		if (firmware != null) {
			Main.setProperty("box.firmware", firmware.getFirmwareVersion()); //$NON-NLS-1$
		} else {
			Main.removeProperty("box.firmware"); //$NON-NLS-1$
		}
	}

	public String getPassword() {
		return password;
	}

	public String getAddress() {
		return address.getText();
	}

	public String getPort() {
		return port.getText();
	}
}
