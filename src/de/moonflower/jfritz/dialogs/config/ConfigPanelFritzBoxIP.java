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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.nexes.wizard.Wizard;

import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.MultiLabel;
import de.moonflower.jfritz.utils.network.SSDPPacket;
import de.robotniko.fboxlib.exceptions.FirmwareNotDetectedException;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class ConfigPanelFritzBoxIP extends JPanel implements ActionListener,
		ConfigPanel, DocumentListener {

	private final static Logger log = Logger.getLogger(ConfigPanelFritzBoxIP.class);

	private static final long serialVersionUID = -2094680014900642941L;

	private JComboBox<String> addressCombo;

	private Vector<SSDPPacket> devices;

	private JTextField address;

	private JTextField port;

	private JButton boxtypeButton;

	private MultiLabel boxtypeLabel;

	private JCheckBox defaultFritzBox;

	private FritzBox fritzBox;

	private boolean settingsChanged = false;
	private Wizard wizard = null;
	private ConfigPanelFritzBoxLogin fritzBoxPanelLogin;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelFritzBoxIP(FritzBox fritzBox) {
		if (fritzBox != null) {
			this.fritzBox = fritzBox;
		} else {
			this.fritzBox = new FritzBox("FRITZ!Box", "", "http", "", properties.getProperty("box.port"), false, "", "");
		}
		if ("".equals(this.fritzBox.getAddress())) {
			this.fritzBox.setAddress("192.168.178.1");
		}

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		int gridPosY = 0;
		JPanel cPane = new JPanel();

		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = gridPosY++;
		ImageIcon boxicon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/fritzbox.png"))); //$NON-NLS-1$
		
		JLabel label = new JLabel(""); //$NON-NLS-1$
		label.setIcon(boxicon);
		cPane.add(label, c);
		label = new JLabel(messages.getMessage("FRITZ!Box_Preferences")); //$NON-NLS-1$
		cPane.add(label, c);

		c.gridy = gridPosY++;
		label = new JLabel(messages.getMessage("FRITZ!Box") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);

		addressCombo = new JComboBox<String>();
		c.fill = GridBagConstraints.HORIZONTAL;
		boolean boxAddressAdded = false;

		// initialize the drop down box
		BoxClass.detectBoxesWithSSDP(log);
		devices = BoxClass.getDevices(log);
		Vector<String> deviceAddress = new Vector<String>();
		if (devices != null) {
			Enumeration<SSDPPacket> en = devices.elements();
			while (en.hasMoreElements()) {
				SSDPPacket p = en.nextElement();
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

		c.gridy = gridPosY++;
		label = new JLabel(messages.getMessage("ip_address") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		address = new JTextField("", 30); //$NON-NLS-1$
		address.setMinimumSize(new Dimension(200, 20));
		address.getDocument().addDocumentListener(this);
		cPane.add(address, c);

		c.gridy = gridPosY++;
		label = new JLabel(messages.getMessage("box.port") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);
		port = new JTextField("", 30); //$NON-NLS-1$
		port.setMinimumSize(new Dimension(200, 20));
		port.getDocument().addDocumentListener(this);
		cPane.add(port, c);

		c.insets.top = 3;
		c.insets.bottom = 3;	
		c.gridy = gridPosY++;
		label = new JLabel("");
		cPane.add(label, c);
		defaultFritzBox = new JCheckBox(messages.getMessage("set_default_fritzbox"));
		defaultFritzBox.setSelected(true);
		cPane.add(defaultFritzBox, c);

		c.gridy = gridPosY++;
		boxtypeButton = new JButton(messages.getMessage("detect_box_type")); //$NON-NLS-1$
		boxtypeButton.setActionCommand("detectboxtype"); //$NON-NLS-1$
		boxtypeButton.addActionListener(this);
		cPane.add(boxtypeButton, c);
		boxtypeLabel = new MultiLabel("a\nb\nc\n");
		cPane.add(boxtypeLabel, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);
	}
	
	public void setWizardReference(Wizard wizard) {
		this.wizard = wizard;
		if (this.fritzBox.getFirmware() == null) {
			disableNextButtonInWizard();
		}
	}

	public void setFritzBoxPanelLogin(ConfigPanelFritzBoxLogin fritzBoxPanel)
	{
		this.fritzBoxPanelLogin = fritzBoxPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("addresscombo")) { //$NON-NLS-1$
			String selectedItem = addressCombo.getItemAt(addressCombo.getSelectedIndex()).toString();
			if (!selectedItem.equals(""))
			{
				address.setText(selectedItem);
			} else {
				log.error("Address wrong!"); //$NON-NLS-1$
				setErrorMessage(messages.getMessage("box.not_found"));
			}
			disableNextButtonInWizard();
		} else if (e.getActionCommand().equals("detectboxtype")) { //$NON-NLS-1$
			executeDetectBoxType();
		}
	}

	private void executeDetectBoxType() {
		Container c = getPanel(); // get the window's content pane
		c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		setSuccessMessage(messages.getMessage("detect_firmware"));
		
		try {
			detectBoxType();
		} catch (IOException e1) {
			// nothing to do, already handled in detectBoxType
		} catch (FirmwareNotDetectedException e1) {
			// nothing to do, already handled in detectBoxType
		} catch (PageNotFoundException e1) {
			// nothing to do, already handled in detectBoxType
		}

		if (fritzBoxPanelLogin != null) {
			fritzBoxPanelLogin.updateGui();
		}
		
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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

	@Override
	public void loadSettings() {
		address.setText(fritzBox.getAddress());
		port.setText(fritzBox.getPort());

		executeDetectBoxType();
		
		for (int i=0; i < addressCombo.getItemCount(); i++) {
			if (((String)addressCombo.getItemAt(i)).equals(address.getText())) {
				addressCombo.setSelectedIndex(i);
			}
		}
		updateFirmwareLabel();

		if(properties.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(properties.getProperty("option.clientCallList"))){

			log.info("NETWORKING: JFritz is running as a client and using call list from server, disabeling FritzBox panel");
			this.boxtypeButton.setEnabled(false);
		}
						
		settingsChanged = false;
	}

	@Override
	public void saveSettings() throws WrongPasswordException, InvalidFirmwareException, IOException {
		saveSettings(true);
	}

	public void saveSettings(boolean checkChanges) throws InvalidFirmwareException {
		if ( (!checkChanges)
				|| somethingChanged()
			)
				
		{
			settingsChanged = true;
		}

		if (settingsChanged) {
			properties.setProperty("box.address", address.getText()); //$NON-NLS-1$
			properties.setProperty("box.port", port.getText()); //$NON-NLS-1$
			
			fritzBox.setUseUsername(Boolean.parseBoolean(properties.getProperty("box.loginUsingUsername")));
			fritzBox.setUsername(properties.getProperty("box.username"));
			fritzBox.setPassword(Encryption.decrypt(properties.getProperty("box.password")));
			
			try {
				detectBoxType();
				fritzBox.updateSettings();
			} catch (IOException e1) {
				// TODO this seems wrong to me, updateSettings also throws exceptions! 
				// nothing to do, already handled in detectBoxType
			} catch (FirmwareNotDetectedException e1) {
				// TODO this seems wrong to me, updateSettings also throws exceptions! 
				// nothing to do, already handled in detectBoxType
			} catch (PageNotFoundException e1) {
				// TODO this seems wrong to me, updateSettings also throws exceptions! 
				// nothing to do, already handled in detectBoxType
			} catch (WrongPasswordException e1) {
				// TODO this seems wrong to me, updateSettings also throws exceptions! 
				// nothing to do, already handled in detectBoxType
			}
			
			if (fritzBox.getFirmware() != null) {
				properties.setProperty("box.firmware", fritzBox.getFirmware().toSimpleString()); //$NON-NLS-1$
				if (defaultFritzBox.isSelected() && fritzBox.getMacAddress() != null)
				{
					properties.setProperty("box.mac", fritzBox.getMacAddress());
				}
			} else {
				properties.removeProperty("box.firmware"); //$NON-NLS-1$
				throw new InvalidFirmwareException("Invalid firmware");
			}
		}
	}

	public String getAddress() {
		return address.getText();
	}

	public String getPort() {
		return port.getText();
	}

	@Override
	public String getPath() {
		return messages.getMessage("FRITZ!Box");
	}

	@Override
	public JPanel getPanel() {
		return this;
	}

	@Override
	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#FRITZ.21Box";
	}

	@Override
	public void cancel() {
	}

	public void detectBoxType() throws IOException, FirmwareNotDetectedException, PageNotFoundException
	{
		fritzBox.setAddress(address.getText());
		fritzBox.setPort(port.getText());
		fritzBox.detectFirmware();
		updateFirmwareLabel();
	}

	private void updateFirmwareLabel() {
		if (fritzBox.getFirmware() != null) {
			setSuccessMessage(fritzBox.getFirmware().getName() + "\n" + fritzBox.getFirmware().toSimpleString());
			enableNextButtonInWizard();
			checkDefaultFritzBox();
		} else {
			setErrorMessage(messages.getMessage("unknown")); //$NON-NLS-1$
		}
	}
	
	private void setSuccessMessage(final String msg) {
		boxtypeLabel.setForeground(Color.BLUE);
		boxtypeLabel.setText(" \n" + msg + "\n "); //$NON-NLS-1$
		boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
		boxtypeLabel.repaint();
	}
	
	private void setErrorMessage(final String msg) {
		boxtypeLabel.setForeground(Color.RED);
		boxtypeLabel.setText(" \n" + msg + "\n "); //$NON-NLS-1$ //$NON-NLS-2$
		boxtypeLabel.setPreferredSize(boxtypeLabel.getPreferredSize());
		boxtypeLabel.repaint();
	}

	public FritzBox getFritzBox()
	{
		return fritzBox;
	}
	
	@Override
	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	@Override
	public boolean shouldRefreshTrayMenu() {
		return false;
	}

	private boolean somethingChanged() {
		boolean firmwareChanged = false;
		if (fritzBox != null && fritzBox.getFirmware() != null) {
			firmwareChanged = !properties.getProperty("box.firmware").equals(fritzBox.getFirmware().toSimpleString());
		}
		
		return (!properties.getProperty("box.address").equals(address.getText()))
				|| (!properties.getProperty("box.port").equals(port.getText()))
				|| (firmwareChanged);

	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		if (wizard != null && somethingChanged()) {
			disableNextButtonInWizard();
			setSuccessMessage("");
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (wizard != null && somethingChanged()) {
			disableNextButtonInWizard();
			setSuccessMessage("");
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (wizard != null && somethingChanged()) {
			disableNextButtonInWizard();
			setSuccessMessage("");
		}
	}
	
	private void disableNextButtonInWizard() {
		if (wizard != null) {
			wizard.setNextFinishButtonEnabled(false);
		}
	}

	private void enableNextButtonInWizard() {
		if (wizard != null) {
			wizard.setNextFinishButtonEnabled(true);
		}
	}
}