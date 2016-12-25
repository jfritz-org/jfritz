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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.nexes.wizard.Wizard;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.MultiLabel;
import de.robotniko.fboxlib.enums.LoginMode;
import de.robotniko.fboxlib.exceptions.FirmwareNotDetectedException;
import de.robotniko.fboxlib.exceptions.InvalidCredentialsException;
import de.robotniko.fboxlib.exceptions.LoginBlockedException;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class ConfigPanelFritzBoxLogin extends JPanel implements ActionListener,
		ConfigPanel, DocumentListener {

	private final static Logger log = Logger.getLogger(ConfigPanelFritzBoxLogin.class);
	private static final long serialVersionUID = -2094680014900642941L;

	private String configPath;

	private JLabel userLabel;
	private JTextField user;
	private JLabel passLabel;
	private JPasswordField pass;
	private JButton checkLoginButton;

	private MultiLabel loginResultLabel;

	private RefreshTimeoutTask refreshTimeoutTask = null;

	private FritzBox fritzBox = null;
	private ConfigPanelSip sipPanel = null;
	private Wizard wizard = null;

	private boolean settingsChanged = false;
	private boolean shouldUseUsername = false;

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
				String error;
				if (fritzBox.getFirmware().isLowerThan(5, 50)) {
					error = messages.getMessage("box.wrong_password.wait");
				} else {
					error = messages.getMessage("box.wrong_password_or_username.wait");
				}
				
				error = error.replaceFirst("%WAIT%", Integer.toString(current));
				setErrorMessage(error);
				checkLoginButton.setEnabled(false);
				current -= 1;
			}
			else
			{
				String error;
				if (fritzBox.getFirmware().isLowerThan(5, 50)) {
					error = " \n" + messages.getMessage("box.wrong_password") + "\n ";
				} else {
					error = " \n" + messages.getMessage("box.wrong_password_or_username") + "\n ";
				}
				setErrorMessage(error);
				checkLoginButton.setEnabled(true);
				this.cancel();
			}
		}

		public int getCurrent()
		{
			return current;
		}

	}

	public ConfigPanelFritzBoxLogin() {
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
		label = new JLabel(messages.getMessage("FRITZ!Box_Credentials")); //$NON-NLS-1$
		cPane.add(label, c);

		c.insets.top = 2;
		c.insets.bottom = 2;	
		c.gridy = gridPosY++;
		userLabel = new JLabel(messages.getMessage("username") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(userLabel, c);
		user = new JTextField("", 30); //$NON-NLS-1$
		user.setMinimumSize(new Dimension(200, 20));
		user.getDocument().addDocumentListener(this);
		
		cPane.add(user, c);

		c.insets.top = 2;
		c.insets.bottom = 2;
		c.gridy = gridPosY++;
		passLabel = new JLabel(messages.getMessage("password") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(passLabel, c);
		pass = new JPasswordField("", 30); //$NON-NLS-1$
		pass.setMinimumSize(new Dimension(200, 20));
		pass.getDocument().addDocumentListener(this);
		cPane.add(pass, c);
		
		c.gridy = gridPosY++;
		checkLoginButton = new JButton(messages.getMessage("check_login")); //$NON-NLS-1$
		checkLoginButton.setActionCommand("checkLogin"); //$NON-NLS-1$
		checkLoginButton.addActionListener(this);
		cPane.add(checkLoginButton, c);
		loginResultLabel = new MultiLabel(" \n \n \n");
		cPane.add(loginResultLabel, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void setWizardReference(Wizard wizard) {
		this.wizard = wizard;
		disableNextButtonInWizard();
		updateGui();
	}
	
	@Override
    public void updateUI() {
		super.updateUI();
		updateGui();
	}
	
	public void updateGui() {
		try {
			if (fritzBox != null) 
			{
				if (fritzBox.getFirmware() == null) {
					fritzBox.detectFirmware();
				}
				
				if (fritzBox.getLoginMode() == LoginMode.PASSWORD) {
					shouldUseUsername = false;
					userLabel.setVisible(false);
					user.setVisible(false);
					passLabel.setVisible(true);
					checkLoginButton.setVisible(true);
					pass.setEnabled(true);
				} else if (fritzBox.getLoginMode() == LoginMode.USERNAME_PASSWORD) {
					shouldUseUsername = true;
					userLabel.setVisible(true);
					user.setVisible(true);
					passLabel.setVisible(true);
					checkLoginButton.setVisible(true);
					pass.setEnabled(true);
				} else {
					shouldUseUsername = false;
					userLabel.setVisible(false);
					user.setVisible(false);
					passLabel.setVisible(false);
					checkLoginButton.setVisible(false);
					enableNextButtonInWizard();
					pass.setEnabled(false);
				}
			}
		} catch (IOException e) {
			setErrorMessage(messages.getMessage("box.not_found"));
		} catch (PageNotFoundException e) {
			handlePageNotFoundException(e);
		} catch (FirmwareNotDetectedException e) {
			handleFirmwareNotDetectedException(e);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("checkLogin")) { //$NON-NLS-1$
			Container c = getPanel(); // get the window's content pane
				
			c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			setSuccessMessage(messages.getMessage("check_login"));
			
			checkLoginData();
				
			c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void loadSettings() {
		user.setText(properties.getProperty("box.username")); //$NON-NLS-1$
		pass.setText(fritzBox.getPassword()); //$NON-NLS-1$

		loginResultLabel.setText(" \n \n ");
				
		settingsChanged = false;
	}

	public void saveSettings() throws WrongPasswordException, InvalidFirmwareException, IOException {
		saveSettings(true);
	}

	public void saveSettings(boolean checkChanges) {
		if (refreshTimeoutTask != null)
		{
			refreshTimeoutTask.cancel();
		}

		if ( (!checkChanges)
			|| somethingChanged()
			)
				
		{
			settingsChanged = true;
		}

		if (settingsChanged) {
			properties.setProperty("box.loginUsingUsername", fritzBox.getLoginMode() == LoginMode.USERNAME_PASSWORD);
			properties.setProperty("box.username", user.getText()); //$NON-NLS-1$
			properties.setProperty("box.password", Encryption.encrypt(new String(pass.getPassword()))); //$NON-NLS-1$
			if (fritzBox.getFirmware() != null) {
				properties.setProperty("box.firmware", fritzBox.getFirmware().toSimpleString()); //$NON-NLS-1$
			} else {
				properties.removeProperty("box.firmware"); //$NON-NLS-1$
			}

			checkLoginData();
		}
	}
	
	private boolean somethingChanged() {
		return (!properties.getProperty("box.username").equals(user.getText()))
				|| (!properties.getProperty("box.password").equals(Encryption.encrypt(new String(pass.getPassword()))))
				|| (Boolean.parseBoolean(properties.getProperty("box.loginUsingUsername")) != (fritzBox.getLoginMode() == LoginMode.USERNAME_PASSWORD));
	}

	public String getUsername() {
		return user.getText();
	}
	
	public boolean shouldUseUsername() {
		return shouldUseUsername;
	}
	
	public String getPassword() {
		return new String(pass.getPassword());
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "https://jfritz.org/wiki/JFritz_Handbuch:Deutsch#FRITZ.21Box";
	}

	public void cancel() {
		if (refreshTimeoutTask != null)
		{
			refreshTimeoutTask.cancel();
		}
	}

	public void checkLoginData()
	{
		try {
			if (fritzBox.getFirmware() == null) {
				fritzBox.detectFirmware();
			}
			
			if (shouldUseUsername) {
				fritzBox.setUseUsername(true);
				fritzBox.setUsername(user.getText());
			} else {
				fritzBox.setUseUsername(false);
				fritzBox.setUsername("");
			}
			
			fritzBox.setPassword(new String(pass.getPassword()));

			fritzBox.detectFirmwareAndLogin();
			
			if (sipPanel != null)
			{
				sipPanel.updateTable();
			}
			setSuccessMessage(messages.getMessage("check_login_ok"));
			enableNextButtonInWizard();

		} catch (ClientProtocolException e) {
			log.error(e.getMessage());
			setErrorMessage(e.getMessage());
		} catch (InvalidCredentialsException e) {
			handleInvalidCredentialsException(e);
		} catch (LoginBlockedException e) {
			handleLoginBlockedException(e);
		} catch (IOException e) {
			setErrorMessage(messages.getMessage("box.not_found"));
		} catch (PageNotFoundException e) {
			handlePageNotFoundException(e);
		} catch (FirmwareNotDetectedException e) {
			handleFirmwareNotDetectedException(e);
		}
	}

	public void setFritzBox(FritzBox fritzBox)
	{
		this.fritzBox = fritzBox;
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
	
	public void setPath(String path) {
		this.configPath = path;
	}

	public String getPath()
	{
		return this.configPath;
	}
	
	private void setSuccessMessage(final String msg) {
		loginResultLabel.setForeground(Color.BLUE);
		loginResultLabel.setText(" \n" + msg + "\n "); //$NON-NLS-1$
		Dimension size = loginResultLabel.getPreferredSize();
		loginResultLabel.setPreferredSize(size);
		loginResultLabel.setSize(size);
		loginResultLabel.repaint();
	}
	
	private void setErrorMessage(final String msg) {
		loginResultLabel.setForeground(Color.RED);
		loginResultLabel.setText(msg); //$NON-NLS-1$ //$NON-NLS-2$
		Dimension size = loginResultLabel.getPreferredSize();
		loginResultLabel.setPreferredSize(size);
		loginResultLabel.setSize(size);
		loginResultLabel.repaint();
	}

	public void handleInvalidCredentialsException(InvalidCredentialsException e) {
		if (fritzBox.getFirmware().isLowerThan(05, 50)) {
			setErrorMessage(messages.getMessage("box.wrong_password"));
		} else {
			setErrorMessage(messages.getMessage("box.wrong_password_or_username"));
		}
	}
	
	public void handleLoginBlockedException(LoginBlockedException e) {
		if (fritzBox.getFirmware().isLowerThan(05, 50)) {
			setErrorMessage(messages.getMessage("box.wrong_password.wait").replaceAll("%WAIT%", e.getRemainingBlockTime()));
		} else {
			setErrorMessage(messages.getMessage("box.wrong_password_or_username.wait").replaceAll("%WAIT%", e.getRemainingBlockTime()));
		}
		
		int time;
		try {
			time = Integer.parseInt(e.getRemainingBlockTime());
		} catch (NumberFormatException nfe) {
			time = 10;
		}
		startRefreshTimer(time);
	}
	
	private void startRefreshTimer(int time) {
		Timer wrongPasswordTask = new Timer();
		if (refreshTimeoutTask != null)
		{
			refreshTimeoutTask.cancel();
		}
		refreshTimeoutTask = new RefreshTimeoutTask(time);
		wrongPasswordTask.schedule(refreshTimeoutTask, 0, 1000);
	}

	private void handlePageNotFoundException(PageNotFoundException e) {
		setErrorMessage("Could not execute command, page not found!");
	}

	private void handleFirmwareNotDetectedException(FirmwareNotDetectedException e) {
		setErrorMessage("Could not detect firmware!");
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
