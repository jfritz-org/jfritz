package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.network.SSDPPacket;
import de.moonflower.jfritz.utils.MultiLabel;

public class ConfigPanelFritzBox extends JPanel implements ActionListener,
		ConfigPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -2094680014900642941L;

	private JComboBox addressCombo;

	private Vector<SSDPPacket> devices;

	private JTextField address;

	private JPasswordField pass;

	private JTextField port;

	private JButton boxtypeButton;

	private MultiLabel boxtypeLabel;

	private FritzBoxFirmware firmware;

	private JCheckBox defaultFritzBox;

	private RefreshTimeoutTask refreshTimeoutTask = null;

	class RefreshTimeoutTask extends TimerTask
	{
		private int current;
		public RefreshTimeoutTask(int start)
		{
			current = start;
		}

		@Override
		public void run() {
			if (current > 0)
			{
				String error = Main.getMessage("box.wrong_password.wait");
				error = error.replaceFirst("%WAIT%", Integer.toString(current));
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(error); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
				boxtypeButton.setEnabled(false);
				current -= 1;
			}
			else
			{
				String error = " \n" + Main.getMessage("box.wrong_password") + "\n ";
				error = error.replaceFirst("%WAIT%", Integer.toString(current));
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(error); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
				boxtypeButton.setEnabled(true);
				this.cancel();
			}
		}

		public int getCurrent()
		{
			return current;
		}

	}

	@SuppressWarnings("unchecked")
	public ConfigPanelFritzBox() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();

		cPane.setLayout(new GridBagLayout());
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
		cPane.add(label, c);
		label = new JLabel(Main.getMessage("FRITZ!Box_Preferences")); //$NON-NLS-1$
		cPane.add(label, c);

		c.gridy = 2;
		label = new JLabel(Main.getMessage("FRITZ!Box") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);

		addressCombo = new JComboBox();
		c.fill = GridBagConstraints.HORIZONTAL;
		boolean boxAddressAdded = false;

		// initialize the drop down box
		devices = JFritz.getDevices();
		if (devices != null) {
			Enumeration<SSDPPacket> en = (Enumeration<SSDPPacket>)devices.elements();
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
		if (!boxAddressAdded) {
			addressCombo.addItem(JFritz.getFritzBox().getAddress());
			addressCombo.setSelectedIndex(addressCombo.getItemCount() - 1);
		}

		addressCombo.setActionCommand("addresscombo"); //$NON-NLS-1$
		addressCombo.addActionListener(this);
		cPane.add(addressCombo, c);

		c.gridy = 3;
		label = new JLabel(Main.getMessage("ip_address") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		address = new JTextField("", 16); //$NON-NLS-1$
		address.setMinimumSize(new Dimension(200, 20));
		cPane.add(address, c);

		c.gridy = 4;
		label = new JLabel(Main.getMessage("password") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		pass = new JPasswordField("", 16); //$NON-NLS-1$
		pass.setMinimumSize(new Dimension(200, 20));
		cPane.add(pass, c);

		c.gridy = 5;
		label = new JLabel(Main.getMessage("box.port") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		port = new JTextField("", 16); //$NON-NLS-1$
		port.setMinimumSize(new Dimension(200, 20));
		cPane.add(port, c);

		c.gridy = 6;
		label = new JLabel("");
		cPane.add(label, c);
		defaultFritzBox = new JCheckBox(Main.getMessage("set_default_fritzbox"));
		defaultFritzBox.setSelected(true);
		cPane.add(defaultFritzBox, c);

		c.gridy = 7;
		boxtypeButton = new JButton(Main.getMessage("detect_box_type")); //$NON-NLS-1$
		boxtypeButton.setActionCommand("detectboxtype"); //$NON-NLS-1$
		boxtypeButton.addActionListener(this);
		cPane.add(boxtypeButton, c);
		boxtypeLabel = new MultiLabel("a\nb\nc\n");
		cPane.add(boxtypeLabel, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {

		String password = new String(pass.getPassword());

		if (e.getActionCommand().equals("addresscombo")) { //$NON-NLS-1$
			int i = addressCombo.getSelectedIndex();
				if (devices.size() != 0 && i < devices.size()) {
					SSDPPacket dev = (SSDPPacket) devices.get(i);
					address.setText(dev.getIP().getHostAddress());
				} else {
					String selectedItem = addressCombo.getItemAt(addressCombo.getSelectedIndex()).toString();
					if (!selectedItem.equals(""))
					{
						address.setText(selectedItem);
					} else {
						Debug.err("Address wrong!"); //$NON-NLS-1$
						boxtypeLabel.setForeground(Color.RED);
						boxtypeLabel.setText(Main.getMessage("box.not_found")); //$NON-NLS-1$
						firmware = null;
					}
				}
				setBoxTypeLabel();

		} else if (e.getActionCommand().equals("detectboxtype")) { //$NON-NLS-1$
			try {
				firmware = FritzBoxFirmware.detectFirmwareVersion(address
						.getText(), password, port.getText());

				// firmware = new FritzBoxFirmware("14", "1", "35");
				setBoxTypeLabel();
			} catch (WrongPasswordException e1) {
				Timer wrongPasswordTask = new Timer();
				if (refreshTimeoutTask != null)
				{
					refreshTimeoutTask.cancel();
				}
				refreshTimeoutTask = new RefreshTimeoutTask(e1.getRetryTime());
				wrongPasswordTask.schedule(refreshTimeoutTask, 0, 1000);
				String error = Main.getMessage("box.wrong_password.wait");
				error = error.replaceFirst("%WAIT%", Integer.toString(e1.getRetryTime()));
				Debug.err(error); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(error); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
				firmware = null;
				this.repaint();
			} catch (InvalidFirmwareException ife) {
				Debug.err("Invalid firmware detected"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
				firmware = null;
			} catch (IOException e1) {
				Debug.err("Address wrong!"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(Main.getMessage("box.not_found")); //$NON-NLS-1$
				firmware = null;
			}
		}
	}

	private void setBoxTypeLabel() {
		if (firmware != null) {
			boxtypeLabel.setForeground(Color.BLUE);
			boxtypeLabel.setText(" \n" + firmware.getBoxName() + " (" //$NON-NLS-1$
					+ firmware.getFirmwareVersion() + ") \n "); //$NON-NLS-1$
			checkDefaultFritzBox();
		} else {
			boxtypeLabel.setForeground(Color.RED);
			boxtypeLabel.setText(" \n" + Main.getMessage("unknown")+"\n "); //$NON-NLS-1$
		}
	}

	private void checkDefaultFritzBox()
	{
		if (Main.getProperty("box.mac").equals(""))
		{
				defaultFritzBox.setSelected(true);
		} else {
			if (firmware.getMacAddress().equals(Main.getProperty("box.mac")))
			{
				defaultFritzBox.setSelected(true);
			} else {
				defaultFritzBox.setSelected(false);
			}
		}
	}

	public void loadSettings() {
		firmware = JFritz.getFritzBox().getFirmware(); //$NON-NLS-1$
		pass.setText(JFritz.getFritzBox().getPassword()); //$NON-NLS-1$
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

		if(Main.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(Main.getProperty("option.clientCallList"))){

			Debug.netMsg("JFritz is running as a client and using call list from server, disabeling FritzBox panel");
			this.boxtypeButton.setEnabled(false);
		}
	}

	public void saveSettings() throws WrongPasswordException, InvalidFirmwareException, IOException {
		if (refreshTimeoutTask != null)
		{
			refreshTimeoutTask.cancel();
		}
		Main.setProperty("box.address", address.getText()); //$NON-NLS-1$
		Main.setProperty("box.password", Encryption.encrypt(new String(pass.getPassword()))); //$NON-NLS-1$
		Main.setProperty("box.port", port.getText()); //$NON-NLS-1$
		JFritz.getFritzBox().updateSettings();

		if (JFritz.getFritzBox().getFirmware() != null) {
			Main.setProperty("box.firmware", JFritz.getFritzBox().getFirmware().getFirmwareVersion()); //$NON-NLS-1$
			if (defaultFritzBox.isSelected())
			{
				Main.setProperty("box.mac", JFritz.getFritzBox().getFirmware().getMacAddress());
			}
		} else {
			Main.removeProperty("box.firmware"); //$NON-NLS-1$
			throw new InvalidFirmwareException("Invalid firmware");
		}
	}

	public String getPassword() {
		return new String(pass.getPassword());
	}

	public String getAddress() {
		return address.getText();
	}

	public String getPort() {
		return port.getText();
	}

	public String getPath() {
		return Main.getMessage("FRITZ!Box");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#FRITZ.21Box";
	}

	public void cancel() {
		if (refreshTimeoutTask != null)
		{
			refreshTimeoutTask.cancel();
		}
	}
}