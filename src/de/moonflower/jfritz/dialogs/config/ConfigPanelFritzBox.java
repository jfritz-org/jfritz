package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
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

import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.MultiLabel;
import de.moonflower.jfritz.utils.network.SSDPPacket;

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

	private JCheckBox defaultFritzBox;

	private RefreshTimeoutTask refreshTimeoutTask = null;

	private FritzBox fritzBox;

	private ConfigPanelSip sipPanel = null;

	private boolean settingsChanged = false;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

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
				String error = messages.getMessage("box.wrong_password.wait");
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
				String error = " \n" + messages.getMessage("box.wrong_password") + "\n ";
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

	public ConfigPanelFritzBox(FritzBox fritzBox) {
		if (fritzBox != null) {
			this.fritzBox = fritzBox;
		} else {
			Exception e = null;
			this.fritzBox = new FritzBox("FRITZ!Box", "", "http", "", "80", "", e);
			if (e != null) {
				Debug.error(e.toString());
			}
		}
		if ("".equals(this.fritzBox.getAddress())) {
			this.fritzBox.setAddress("192.168.178.1");
		}

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
		label = new JLabel(messages.getMessage("FRITZ!Box_Preferences")); //$NON-NLS-1$
		cPane.add(label, c);

		c.gridy = 2;
		label = new JLabel(messages.getMessage("FRITZ!Box") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);

		addressCombo = new JComboBox();
		c.fill = GridBagConstraints.HORIZONTAL;
		boolean boxAddressAdded = false;

		// initialize the drop down box
		BoxClass.detectBoxesWithSSDP();
		devices = BoxClass.getDevices();
		Vector<String> deviceAddress = new Vector<String>();
		if (devices != null) {
			Enumeration<SSDPPacket> en = (Enumeration<SSDPPacket>)devices.elements();
			while (en.hasMoreElements()) {
				SSDPPacket p = (SSDPPacket) en.nextElement();
				if (!deviceAddress.contains(p.getIP().getHostAddress()))
				{
					deviceAddress.add(p.getIP().getHostAddress());
					// addressCombo.addItem(p.getShortName());
					if (p.getIP().getHostAddress().equals(this.fritzBox.getAddress()))
					{
						boxAddressAdded = true;
					}
				}
			}
		}

		// make sure the stored address is listed
		if (!boxAddressAdded) {
			deviceAddress.add(this.fritzBox.getAddress());
		}

		Collections.sort(deviceAddress);

		for (String device:deviceAddress) {
			addressCombo.addItem(device);
		}

		for (int i=0; i < addressCombo.getItemCount(); i++) {
			if (((String)addressCombo.getItemAt(i)).equals(this.fritzBox.getAddress())) {
				addressCombo.setSelectedIndex(i);
			}
		}

		addressCombo.setActionCommand("addresscombo"); //$NON-NLS-1$
		addressCombo.addActionListener(this);
		cPane.add(addressCombo, c);

		c.gridy = 3;
		label = new JLabel(messages.getMessage("ip_address") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		address = new JTextField("", 30); //$NON-NLS-1$
		address.setMinimumSize(new Dimension(200, 20));
		cPane.add(address, c);

		c.gridy = 4;
		label = new JLabel(messages.getMessage("password") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		pass = new JPasswordField("", 30); //$NON-NLS-1$
		pass.setMinimumSize(new Dimension(200, 20));
		cPane.add(pass, c);

		c.gridy = 5;
		label = new JLabel(messages.getMessage("box.port") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		port = new JTextField("", 30); //$NON-NLS-1$
		port.setMinimumSize(new Dimension(200, 20));
		cPane.add(port, c);

		c.gridy = 6;
		label = new JLabel("");
		cPane.add(label, c);
		defaultFritzBox = new JCheckBox(messages.getMessage("set_default_fritzbox"));
		defaultFritzBox.setSelected(true);
		cPane.add(defaultFritzBox, c);

		c.gridy = 7;
		boxtypeButton = new JButton(messages.getMessage("detect_box_type")); //$NON-NLS-1$
		boxtypeButton.setActionCommand("detectboxtype"); //$NON-NLS-1$
		boxtypeButton.addActionListener(this);
		cPane.add(boxtypeButton, c);
		boxtypeLabel = new MultiLabel("a\nb\nc\n");
		cPane.add(boxtypeLabel, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("addresscombo")) { //$NON-NLS-1$
			String selectedItem = addressCombo.getItemAt(addressCombo.getSelectedIndex()).toString();
			if (!selectedItem.equals(""))
			{
				address.setText(selectedItem);
			} else {
				Debug.error("Address wrong!"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(messages.getMessage("box.not_found")); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
			}
			setBoxTypeLabel();
		} else if (e.getActionCommand().equals("detectboxtype")) { //$NON-NLS-1$
		   Container c = getPanel(); // get the window's content pane
			try {
			   c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				boxtypeLabel.setForeground(Color.BLUE);
				boxtypeLabel.setText(messages.getMessage("detect_firmware"));//$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();

				detectBoxType();
				if (sipPanel != null)
				{
					sipPanel.updateTable();
				}

			} catch (WrongPasswordException e1) {
				Timer wrongPasswordTask = new Timer();
				if (refreshTimeoutTask != null)
				{
					refreshTimeoutTask.cancel();
				}
				refreshTimeoutTask = new RefreshTimeoutTask(e1.getRetryTime());
				wrongPasswordTask.schedule(refreshTimeoutTask, 0, 1000);
				String error = messages.getMessage("box.wrong_password.wait");
				error = error.replaceFirst("%WAIT%", Integer.toString(e1.getRetryTime()));
				Debug.error(error); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(error); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
				this.repaint();
			} catch (InvalidFirmwareException ife) {
				Debug.error("Invalid firmware detected"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(messages.getMessage("unknown_firmware")); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
			} catch (IOException e1) {
				Debug.error("Address wrong!"); //$NON-NLS-1$
				boxtypeLabel.setForeground(Color.RED);
				boxtypeLabel.setText(messages.getMessage("box.not_found")); //$NON-NLS-1$
				boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
				boxtypeLabel.repaint();
			}
			c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void setBoxTypeLabel() {
		if (fritzBox.getFirmware() != null) {
			boxtypeLabel.setForeground(Color.BLUE);
			boxtypeLabel.setText(" \n" + fritzBox.getFirmware().getBoxName() + "\n" //$NON-NLS-1$
					+ fritzBox.getFirmware().getFirmwareVersion() + "\n "); //$NON-NLS-1$
			boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
			boxtypeLabel.repaint();
			checkDefaultFritzBox();
		} else {
			boxtypeLabel.setForeground(Color.RED);
			boxtypeLabel.setText(" \n" + messages.getMessage("unknown")+"\n "); //$NON-NLS-1$
			boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
			boxtypeLabel.repaint();
		}
	}

	private void checkDefaultFritzBox()
	{
		if (properties.getProperty("box.mac").equals(""))
		{
				defaultFritzBox.setSelected(true);
		} else {
			if ((fritzBox.getMacAddress() != null)
			&&	(fritzBox.getMacAddress().equals(properties.getProperty("box.mac"))))
			{
				defaultFritzBox.setSelected(true);
			} else {
				defaultFritzBox.setSelected(false);
			}
		}
	}

	public void loadSettings() {
		pass.setText(fritzBox.getPassword()); //$NON-NLS-1$
		address.setText(fritzBox.getAddress()); //$NON-NLS-1$,  //$NON-NLS-2$
		port.setText(fritzBox.getPort()); //$NON-NLS-1$,  //$NON-NLS-2$

		for (int i=0; i < addressCombo.getItemCount(); i++) {
			if (((String)addressCombo.getItemAt(i)).equals(address.getText())) {
				addressCombo.setSelectedIndex(i);
			}
		}
		setBoxTypeLabel();

		if(properties.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(properties.getProperty("option.clientCallList"))){

			Debug.netMsg("JFritz is running as a client and using call list from server, disabeling FritzBox panel");
			this.boxtypeButton.setEnabled(false);
		}
		settingsChanged = false;
	}

	public void saveSettings() throws WrongPasswordException, InvalidFirmwareException, IOException {
		saveSettings(true);
	}

	public void saveSettings(boolean checkChanges) throws WrongPasswordException, InvalidFirmwareException, IOException {
		if (refreshTimeoutTask != null)
		{
			refreshTimeoutTask.cancel();
		}

		if ( (!checkChanges)
			|| (!properties.getProperty("box.address").equals(address.getText()))
			|| (!properties.getProperty("box.password").equals(Encryption.encrypt(new String(pass.getPassword()))))
			|| (!properties.getProperty("box.port").equals(port.getText())))
		{
			settingsChanged = true;
		}

		if (settingsChanged) {
			properties.setProperty("box.address", address.getText()); //$NON-NLS-1$
			properties.setProperty("box.password", Encryption.encrypt(new String(pass.getPassword()))); //$NON-NLS-1$
			properties.setProperty("box.port", port.getText()); //$NON-NLS-1$

			fritzBox.setAddress(address.getText());
			fritzBox.setPassword(new String(pass.getPassword()));
			fritzBox.setPort(port.getText());
			fritzBox.updateSettings();

			if (fritzBox.getFirmware() != null) {
				properties.setProperty("box.firmware", fritzBox.getFirmware().getFirmwareVersion()); //$NON-NLS-1$
				if (defaultFritzBox.isSelected())
				{
					properties.setProperty("box.mac", fritzBox.getMacAddress());
				}
			} else {
				properties.removeProperty("box.firmware"); //$NON-NLS-1$
				throw new InvalidFirmwareException("Invalid firmware");
			}
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
		return messages.getMessage("FRITZ!Box");
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

	public void detectBoxType() throws WrongPasswordException, IOException, InvalidFirmwareException
	{
		fritzBox.setAddress(address.getText());
		fritzBox.setPassword(new String(pass.getPassword()));
		fritzBox.setPort(port.getText());
		fritzBox.updateSettings();

		setBoxTypeLabel();
	}

	public FritzBox getFritzBox()
	{
		return fritzBox;
	}

	public void setSipPanel(ConfigPanelSip sipPanel)
	{
		this.sipPanel = sipPanel;
	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}
}