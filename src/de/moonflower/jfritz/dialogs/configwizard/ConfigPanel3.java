package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.network.SSDPPacket;

/**
 * @author Brian Jensen
 *
 * This is the panel for the box settings
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel3 extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1;

	public JComboBox addressCombo;

	public JTextField address;

	public JPasswordField pass;

	public JButton boxtypeButton;

	private JLabel boxtypeLabel;

	public Vector devices;

	public String password;

	public JTextField port;

	public FritzBoxFirmware firmware;

	public ConfigPanel3(JFritz jfritz) {

		// draw the panel
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

		// initialize the drop down box
		devices = jfritz.getDevices();
		if (devices != null) {
			Enumeration en = devices.elements();
			while (en.hasMoreElements()) {
				SSDPPacket p = (SSDPPacket) en.nextElement();
				addressCombo.addItem(p.getShortName());
			}
		}

		addressCombo.setActionCommand("addresscombo"); //$NON-NLS-1$
		addressCombo.addActionListener(this);
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
		boxtypeButton.addActionListener(this);
		boxpane.add(boxtypeButton, c);
		boxtypeLabel = new JLabel();
		boxpane.add(boxtypeLabel, c);

		// initialize the rest of the values
		pass.setText(Encryption.decrypt(JFritz.getProperty("box.password"))); //$NON-NLS-1$
		password = Encryption.decrypt(JFritz.getProperty("box.password")); //$NON-NLS-1$
		address.setText(JFritz.getProperty("box.address", "192.168.178.1")); //$NON-NLS-1$,  //$NON-NLS-2$
		port.setText(JFritz.getProperty("box.port", "80")); //$NON-NLS-1$,  //$NON-NLS-2$

		if (devices != null) {
			for (int i = 0; i < devices.size(); i++) {
				SSDPPacket p = (SSDPPacket) devices.get(i);
				if (p.getIP().getHostAddress().equals(address.getText())) {
					addressCombo.setSelectedIndex(i);
				}
			}
		}
		firmware = JFritz.getFirmware(); //$NON-NLS-1$
		setBoxTypeLabel();

		add(boxpane, BorderLayout.CENTER);

	}

	public void actionPerformed(ActionEvent e) {

		password = new String(pass.getPassword());

		if (e.getActionCommand().equals("addresscombo")) { //$NON-NLS-1$
			int i = addressCombo.getSelectedIndex();
			SSDPPacket dev = (SSDPPacket) devices.get(i);
			address.setText(dev.getIP().getHostAddress());
			firmware = dev.getFirmware();
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
				boxtypeLabel.setText(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
				firmware = null;
			} catch (InvalidFirmwareException ife) {
				Debug.err("Invalid firmware detected"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(JFritz.getMessage("box_address_wrong")); //$NON-NLS-1$
				firmware = null;
			} catch (IOException e1) {
				Debug.err("Address wrong!"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(JFritz.getMessage("box_address_wrong")); //$NON-NLS-1$
				firmware = null;
			}
			// firmware = new FritzBoxFirmware("14", "1", "35");
			setBoxTypeLabel();
		}
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

}
