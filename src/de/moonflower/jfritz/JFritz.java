/**
 *
 */

package de.moonflower.jfritz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.callmonitor.CallMonitorInterface;
import de.moonflower.jfritz.callmonitor.CallMonitorList;
import de.moonflower.jfritz.callmonitor.CallmessageCallMonitor;
import de.moonflower.jfritz.callmonitor.DisconnectMonitor;
import de.moonflower.jfritz.callmonitor.DisplayCallsMonitor;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV1;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV3;
import de.moonflower.jfritz.callmonitor.SyslogCallMonitor;
import de.moonflower.jfritz.callmonitor.TelnetCallMonitor;
import de.moonflower.jfritz.callmonitor.YACCallMonitor;
import de.moonflower.jfritz.dialogs.quickdial.QuickDials;
import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.network.ClientLoginsTableModel;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.struct.FritzBox;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StatusListener;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;
import de.moonflower.jfritz.utils.network.SSDPdiscoverThread;

/**
 *
 */
public final class JFritz implements  StatusListener, ItemListener {

	// when changing this, don't forget to check the resource bundles!!

	public final static String PROGRAM_SECRET = "jFrItZsEcReT"; //$NON-NLS-1$

	public final static String DOCUMENTATION_URL = "http://www.jfritz.org/wiki/Kategorie:Hilfe"; //$NON-NLS-1$

	public final static String CALLS_FILE = "jfritz.calls.xml"; //$NON-NLS-1$

	public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml"; //$NON-NLS-1$

	public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml"; //$NON-NLS-1$

	public final static String SIPPROVIDER_FILE = "jfritz.sipprovider.xml"; //$NON-NLS-1$

	public final static String CLIENT_SETTINGS_FILE = "jfritz.clientsettings.xml"; //$NON-NLS-1$

	public final static String CALLS_CSV_FILE = "calls.csv"; //$NON-NLS-1$

	public final static String PHONEBOOK_CSV_FILE = "contacts.csv"; //$NON-NLS-1$

	public final static int SSDP_TIMEOUT = 1000;

	public final static int SSDP_MAX_BOXES = 3;

	private static SystemTray systray;

	private static JFritzWindow jframe;

	private static SSDPdiscoverThread ssdpthread;

	private static CallerList callerlist;

	private static TrayIcon trayIcon;

	private static PhoneBook phonebook;

	private static SipProviderTableModel sipprovider;

	private static URL ringSound, callSound;

	private static CallMonitorInterface callMonitor = null;

	private static String HostOS = "other"; //$NON-NLS-1$

	private static WatchdogThread watchdog;

	private static Timer watchdogTimer;

	private static FritzBox fritzBox;

	private static QuickDials quickDials;

	public static CallMonitorList callMonitorList;

	private static Main main;

	private static ClientLoginsTableModel clientLogins;

	/**
	 * Constructs JFritz object
	 */
	public JFritz(Main mn) {
		main = mn;

		/*
		JFritzEventDispatcher eventDispatcher = new JFritzEventDispatcher();
		JFritzEventDispatcher.registerEventType(new MessageEvent());

		JFritzEventDispatcher.registerActionType(new PopupAction());
		JFritzEventDispatcher.registerActionType(new TrayMessageAction());

		JFritzEventDispatcher.loadFromXML();

		*/

		if (JFritzUtils.parseBoolean(Main.getProperty(
				"option.createBackup", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			Main.doBackup();
		}

			//option was removed from the config dialog in 0.7.1, make sure
			//it is automatically deselected
		if(Main.getProperty("option.callMonitorType", "0").equals("6"))
			Main.setProperty("option.callMonitorType", "0");

		// make sure there is a plus on the country code, or else the number
		// scheme won't work
		if (!Main.getProperty("country.code").startsWith("+"))
			Main.setProperty("country.code", "+"
					+ Main.getProperty("country.code"));

		loadSounds();
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		Debug.msg("Operating System : " + osName); //$NON-NLS-1$
		if (osName.toLowerCase().startsWith("mac os")) //$NON-NLS-1$
			HostOS = "Mac"; //$NON-NLS-1$
		else if (osName.startsWith("Windows")) //$NON-NLS-1$
			HostOS = "Windows"; //$NON-NLS-1$
		else if (osName.equals("Linux")) { //$NON-NLS-1$
			HostOS = "Linux"; //$NON-NLS-1$
		}
		Debug.msg("JFritz runs on " + HostOS); //$NON-NLS-1$

		if (HostOS.equals("Mac")) { //$NON-NLS-1$
			new MacHandler(this);
		}

		//once the machandler has been installed, activate the debug panel
		//otherwise it will cause ui problems on the mac
		//stupid concept really, but it has to be done
		Debug.generatePanel();

	}

	public void initNumbers()
	{
		// loads various country specific number settings and tables
		loadNumberSettings();
	}

	public int initFritzBox() throws WrongPasswordException, InvalidFirmwareException, IOException
	{
		int result = 0;
		Exception ex = null;
		fritzBox = new FritzBox(Main
				.getProperty("box.address", "192.168.178.1"), Encryption //$NON-NLS-1$,  //$NON-NLS-2$
				.decrypt(Main.getProperty("box.password", Encryption //$NON-NLS-1$
						.encrypt(""))), Main.getProperty("box.port", "80"), ex); //$NON-NLS-1$
		if ( ex != null)
		{
			try {
				throw ex;
			} catch (Exception e)
			{
				Debug.err("Error: " + e.getLocalizedMessage());
			}
		}
		String macStr = Main.getProperty("box.mac", "");
		if ( fritzBox.getFirmware() != null )
		{
			if ( (!macStr.equals("")) && (!fritzBox.getFirmware().getMacAddress().equals("")) && (!fritzBox.getFirmware().getMacAddress().equals(macStr)))
			{
				int answer = JOptionPane.showConfirmDialog(null,
						Main.getMessage("new_fritzbox") + "\n" //$NON-NLS-1$
								+ Main.getMessage("accept_fritzbox_communication"), //$NON-NLS-1$
						Main.getMessage("information"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
				if (answer == JOptionPane.YES_OPTION) {
					Debug.msg("New FritzBox detected, user decided to accept connection."); //$NON-NLS-1$
					result = 0;
				} else {
					Debug.msg("New FritzBox detected, user decided to prohibit connection."); //$NON-NLS-1$
					result = -1;
				}
			}
		}
		return result;
	}

	public void initSipProvider()
	{
		sipprovider = new SipProviderTableModel();
		sipprovider.loadFromXMLFile(Main.SAVE_DIR + SIPPROVIDER_FILE);
	}

	public void initQuickDials()
	{
		quickDials = new QuickDials();
		quickDials.loadFromXMLFile(Main.SAVE_DIR + JFritz.QUICKDIALS_FILE);
	}

	public void initCallerListAndPhoneBook()
	{
		callerlist = new CallerList();
		phonebook = new PhoneBook(PHONEBOOK_FILE);
		callerlist.setPhoneBook(phonebook);
		phonebook.setCallerList(callerlist);

		phonebook.loadFromXMLFile(Main.SAVE_DIR + PHONEBOOK_FILE);
		callerlist.loadFromXMLFile(Main.SAVE_DIR + CALLS_FILE);
		phonebook.findAllLastCalls();
		callerlist.findAllPersons();
	}

	public void initCallMonitor()
	{
		callMonitorList = new CallMonitorList();
		callMonitorList.addCallMonitorListener(new DisplayCallsMonitor());
		callMonitorList.addCallMonitorListener(new DisconnectMonitor());
	}

	public void initClientServer()
	{
		clientLogins = new ClientLoginsTableModel();

		ClientLoginsTableModel.loadFromXMLFile(Main.SAVE_DIR+CLIENT_SETTINGS_FILE);
	}

	public void initLookAndFeel()
	{
		setDefaultLookAndFeel();
		if (Main
				.getProperty(
						"lookandfeel", UIManager.getSystemLookAndFeelClassName()).endsWith("MetalLookAndFeel")) { //$NON-NLS-1$,  //$NON-NLS-2$
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true); // uses L&F
			// decorations for
			// dialogs
		}
	}

	public void createJFrame(boolean showConfWizard) {
		Debug.msg("New instance of JFrame"); //$NON-NLS-1$
		jframe = new JFritzWindow(this);
		if (Main.checkForSystraySupport()) {
			Debug.msg("Check Systray-Support"); //$NON-NLS-1$
			try {
				systray = SystemTray.getDefaultSystemTray();
				createTrayMenu();
			} catch (UnsatisfiedLinkError ule) {
				Debug.err(ule.toString());
				Main.SYSTRAY_SUPPORT = false;
			} catch (Exception e) {
				Debug.err(e.toString());
				Main.SYSTRAY_SUPPORT = false;
			}
		}
		jframe.checkStartOptions();
		//FIXME ich nehme mal an SSDPdiscover macht irgendwas mit der Fritzbox? => nach Fritzbox verschieben
		//only do the search if jfritz is not running as a client and using the call list from server
		if (JFritzUtils.parseBoolean(Main.getProperty("option.useSSDP",//$NON-NLS-1$
				"true")) && !(Main.getProperty("network.type", "0").equals("2")
						&& Boolean.parseBoolean(Main.getProperty("option.clientCallList", "false"))) ) {//$NON-NLS-1$
			Debug.msg("Searching for  FritzBox per UPnP / SSDP");//$NON-NLS-1$

			ssdpthread = new SSDPdiscoverThread(SSDP_TIMEOUT);
			ssdpthread.getStatusBarController().addStatusBarListener(this);
			ssdpthread.start();
			try {
				ssdpthread.join();//FIXME start a thread just to call join in the next line?
			} catch (InterruptedException ie) {
	        	Thread.currentThread().interrupt();
			}
//			ssdpthread.interrupt();
		}

		if (showConfWizard) {
			Debug.msg("Presenting user with the configuration dialog");
			jframe.showConfigWizard();
		}

		javax.swing.SwingUtilities.invokeLater(jframe);

		if(Main.getProperty("network.type", "0").equals("1") &&
				Boolean.parseBoolean(Main.getProperty("option.listenOnStartup", "false"))){
			Debug.msg("listening on startup enabled, starting client listener!");
			NetworkStateMonitor.startServer();
		}else if(Main.getProperty("network.type", "0").equals("2") &&
				Boolean.parseBoolean(Main.getProperty("option.connectOnStartup", "false"))){
			Debug.msg("Connect on startup enabled, connectig to server");
			NetworkStateMonitor.startClient();
		}
		startWatchdog();

	}

	/**
	 * This constructor is used for JUnit based testing suites Only the default
	 * settings are loaded for this jfritz object
	 *
	 * @author brian jensen
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 * @throws WrongPasswordException
	 */
	public JFritz(String test) throws WrongPasswordException, InvalidFirmwareException, IOException {

		// make sure there is a plus on the country code, or else the number
		// scheme won't work
		if (!Main.getProperty("country.code").startsWith("+"))
			Main.setProperty("country.code", "+"
					+ Main.getProperty("country.code"));

		// loadSounds();

		// loads various country specific number settings and tables
		loadNumberSettings();

		Exception ex = null;
		fritzBox = new FritzBox(Main
				.getProperty("box.address", "192.168.178.1"), Encryption //$NON-NLS-1$, //$NON-NLS-2$
				.decrypt(Main.getProperty("box.password", Encryption //$NON-NLS-1$
						.encrypt(""))), Main.getProperty("box.port", "80"), ex); // //$NON-NLS-1$
		// $NON-NLS-2$
		if ( ex != null )
		{
			try {
				throw ex;
			} catch (Exception e)
			{
				Debug.err("Error: " + e.getLocalizedMessage());
			}
		}
		sipprovider = new SipProviderTableModel();
		// sipprovider.loadFromXMLFile(SAVE_DIR + SIPPROVIDER_FILE);

		callerlist = new CallerList();
		// callerlist.loadFromXMLFile(SAVE_DIR + CALLS_FILE);

		phonebook = new PhoneBook(PHONEBOOK_FILE);
		// phonebook.loadFromXMLFile(SAVE_DIR + PHONEBOOK_FILE);
		phonebook.setCallerList(callerlist);
		callerlist.setPhoneBook(phonebook);
		phonebook.findAllLastCalls();
		callerlist.findAllPersons();
	}

	/**
	 * Loads sounds from resources
	 */
	private void loadSounds() {
		ringSound = getClass().getResource(
				"/de/moonflower/jfritz/resources/sounds/call_in.wav"); //$NON-NLS-1$
		callSound = getClass().getResource(
				"/de/moonflower/jfritz/resources/sounds/call_out.wav"); //$NON-NLS-1$
	}

	/**
	 * Creates the tray icon menu
	 */
	private void createTrayMenu() {
		System.setProperty("javax.swing.adjustPopupLocationToFit", "false"); //$NON-NLS-1$,  //$NON-NLS-2$

		LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
		ButtonGroup lnfgroup = new ButtonGroup();

		JMenu lnfMenu = new JMenu(Main.getMessage("lnf_menu")); //$NON-NLS-1$
		// Add system dependent look and feels
		for (int i = 0; i < lnfs.length; i++) {
			JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lnfs[i]
					.getName());
			lnfMenu.add(rbmi);
			rbmi.setSelected(UIManager.getLookAndFeel().getClass().getName()
					.equals(lnfs[i].getClassName()));
			rbmi.putClientProperty("lnf name", lnfs[i]); //$NON-NLS-1$
			rbmi.addItemListener(this);
			lnfgroup.add(rbmi);
		}

		// Add additional look and feels from looks-2.1.4.jar
		LookAndFeelInfo lnf = new LookAndFeelInfo("Plastic","com.jgoodies.looks.plastic.PlasticLookAndFeel");
		JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(this);
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic 3D","com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(this);
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic XP","com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(this);
		lnfgroup.add(rb);


		JPopupMenu menu = new JPopupMenu("JFritz Menu"); //$NON-NLS-1$
		JMenuItem menuItem = new JMenuItem(Main.PROGRAM_NAME + " v" //$NON-NLS-1$
				+ Main.PROGRAM_VERSION);
		menuItem.setActionCommand("showhide");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem(Main.getMessage("fetchlist")); //$NON-NLS-1$
		menuItem.setActionCommand("fetchList"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage("reverse_lookup")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.add(lnfMenu);
		menuItem = new JMenuItem(Main.getMessage("config")); //$NON-NLS-1$
		menuItem.setActionCommand("config"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem(Main.getMessage("prog_exit")); //$NON-NLS-1$
		menuItem.setActionCommand("exit"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);

		ImageIcon icon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/trayicon.png")); //$NON-NLS-1$

		trayIcon = new TrayIcon(icon, Main.PROGRAM_NAME, menu); //$NON-NLS-1$
		trayIcon.setIconAutoSize(false);
		trayIcon.setCaption(Main.PROGRAM_NAME + " v" + Main.PROGRAM_VERSION); //$NON-NLS-1$
		trayIcon.addActionListener(new ActionListener() {
			private long oldTimeStamp = 0;
			public void actionPerformed(ActionEvent e) {
				long timeStamp = e.getWhen();
				if ( timeStamp-oldTimeStamp>600 ) {
					if ( jframe != null )
					{
						jframe.hideShowJFritz();
						oldTimeStamp = timeStamp;
					}
				}
			}
		});
		systray.addTrayIcon(trayIcon);
	}

	/**
	 * Displays balloon info message
	 *
	 * @param msg
	 *            Message to be displayed
	 */
	public static void infoMsg(String msg) {
		switch (Integer.parseInt(Main.getProperty("option.popuptype", "1"))) { //$NON-NLS-1$,  //$NON-NLS-2$
		case 0: { // No Popup
			break;
		}
		case 1: {
			MessageDlg msgDialog = new MessageDlg();
			msgDialog.showMessage(msg, Long.parseLong(Main.getProperty(
					"option.popupDelay", "10")) * 1000);
			msgDialog.repaint();
			msgDialog.toFront();
			break;
		}
		case 2: {
			if (trayIcon != null)
				trayIcon.displayMessage(Main.PROGRAM_NAME, msg,
						TrayIcon.INFO_MESSAGE_TYPE);
			else if (trayIcon == null) {
				MessageDlg msgDialog = new MessageDlg();
				msgDialog.showMessage(msg, Long.parseLong(Main.getProperty(
						"option.popupDelay", "10")) * 1000);
				msgDialog.repaint();
				msgDialog.toFront();
			}
			break;
		}
		}
	}


	/**
	 * Plays a sound by a given resource URL
	 *
	 * @param sound
	 *            URL of sound to be played
	 * @param volume
	 * 			  Volume in percent. 1.0 means 100 percent (loudest volume)
	 */
	public static void playSound(URL sound, float volume) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(sound);
			DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat(),
					((int) ais.getFrameLength() * ais.getFormat()
							.getFrameSize()));
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(ais);
            FloatControl gainControl =
                (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float diff = max - min;
            gainControl.setValue(volume);
			clip.start();
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
		        	Thread.currentThread().interrupt();
				}
				if (!clip.isRunning()) {
					break;
				}
			}
			clip.stop();
			clip.close();
		} catch (UnsupportedAudioFileException e) {
		} catch (IOException e) {
		} catch (LineUnavailableException e) {
		}
	}

	/**
	 * Displays balloon error message
	 *
	 * @param msg
	 */
	public static void errorMsg(String msg) {
		Debug.err(msg);
		if (Main.SYSTRAY_SUPPORT) {
			trayIcon.displayMessage(Main.PROGRAM_NAME, msg,
					TrayIcon.ERROR_MESSAGE_TYPE);
		}
	}

	/**
	 * @return Returns the callerlist.
	 */
	public static final CallerList getCallerList() {
		return callerlist;
	}

	/**
	 * @return Returns the phonebook.
	 */
	public static final PhoneBook getPhonebook() {
		return phonebook;
	}

	/**
	 * @return Returns the jframe.
	 */
	public static final JFritzWindow getJframe() {
		return jframe;
	}

	/**
	 * @return Returns the fritzbox devices.
	 */
	public static final Vector getDevices() {
		//avoid using the ssdp thread if jfritz is running as a client and using the call list from server
		if (JFritzUtils.parseBoolean(Main.getProperty("option.useSSDP", //$NON-NLS-1$
				"true")) && !(Main.getProperty("network.type", "0").equals("2")
						&& Boolean.parseBoolean(Main.getProperty("option.clientCallList", "false")))) { //$NON-NLS-1$
			try {
				ssdpthread.join();
			} catch (InterruptedException e) {
			}
			return ssdpthread.getDevices();
		} else {
			Debug.netMsg("jfritz is configured as a client, canceling box detection");
			return null;
		}
	}

	public void startChosenCallMonitor(boolean showErrorMessage) {
		if(Main.getProperty("option.clientCallMonitor", "false").equals("true")
			 && NetworkStateMonitor.isConnectedToServer()){

			return;
		}


		switch (Integer.parseInt(Main.getProperty("option.callMonitorType", //$NON-NLS-1$
				"0"))) { //$NON-NLS-1$
			case 1: {
				try {
					getJframe().setConnectedStatus();
					if (JFritz.getFritzBox().checkValidFirmware()) {
						FritzBoxFirmware currentFirm = JFritz.getFritzBox()
								.getFirmware();
						if ((currentFirm.getMajorFirmwareVersion() == 3)
								&& (currentFirm.getMinorFirmwareVersion() < 96)) {
							Debug.errDlg(Main
									.getMessage("callmonitor_error_wrong_firmware")); //$NON-NLS-1$
							getJframe().getMonitorButton().setSelected(false);
							getJframe().setCallMonitorButtonPushed(true);
							getJframe().setCallMonitorConnectedStatus();
						} else {
							if ((currentFirm.getMajorFirmwareVersion() >= 4)
									&& (currentFirm.getMinorFirmwareVersion() >= 3)) {
								JFritz.setCallMonitor(new FBoxCallMonitorV3());
							} else {
								JFritz.setCallMonitor(new FBoxCallMonitorV1());
							}
							getJframe().setCallMonitorButtonPushed(true);
							getJframe().setCallMonitorConnectedStatus();
						}
					}
				} catch (WrongPasswordException e1) {
					JFritz.setCallMonitor(new FBoxCallMonitorV3());
					getJframe().setCallMonitorDisconnectedStatus();
					if (showErrorMessage)
					{
						JFritz.errorMsg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
					}
				} catch (IOException e1) {
					JFritz.setCallMonitor(new FBoxCallMonitorV3());
					getJframe().setCallMonitorDisconnectedStatus();
					if (showErrorMessage)
					{
						JFritz.errorMsg(Main.getMessage("box.not_found")); //$NON-NLS-1$
					}
				} catch (InvalidFirmwareException e1) {
					JFritz.setCallMonitor(new FBoxCallMonitorV3());
					getJframe().setCallMonitorDisconnectedStatus();
					if (showErrorMessage)
					{
						JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
					}
				}
				break;
			}
			case 2: {
				JFritz.setCallMonitor(new TelnetCallMonitor(this));
				getJframe().setCallMonitorButtonPushed(true);
				break;
			}
			case 3: {
				JFritz.setCallMonitor(new SyslogCallMonitor(this));
				getJframe().setCallMonitorButtonPushed(true);
				break;
			}
			case 4: {
				JFritz.setCallMonitor(new YACCallMonitor(Integer.parseInt(Main
						.getProperty("option.yacport", //$NON-NLS-1$
								"10629")))); //$NON-NLS-1$
				getJframe().setCallMonitorButtonPushed(true);
				break;
			}
			case 5: {
				JFritz.setCallMonitor(new CallmessageCallMonitor(Integer
						.parseInt(Main.getProperty("option.callmessageport", //$NON-NLS-1$
								"23232")))); //$NON-NLS-1$
				getJframe().setCallMonitorButtonPushed(true);
				break;
			}
		}

	}

	public static void stopCallMonitor() {
		if (callMonitor != null) {
			callMonitor.stopCallMonitor();
			// Let buttons enable start of callMonitor
			callMonitor = null;
		}
		getJframe().setCallMonitorButtonPushed(false);
		getJframe().setCallMonitorDisconnectedStatus();
	}

	public static CallMonitorInterface getCallMonitor() {
		return callMonitor;
	}

	public static void setCallMonitor(CallMonitorInterface cm) {
		callMonitor = cm;
	}

	public static String runsOn() {
		return HostOS;
	}

	public static SipProviderTableModel getSIPProviderTableModel() {
		return sipprovider;
	}

	/**
	 * start timer for watchdog
	 *
	 */
	private void startWatchdog() {
		int interval = 10; // seconds
		watchdogTimer = new Timer();
		watchdog = new WatchdogThread(interval, this);
		watchdog.setDaemon(false);
		watchdog.setName("Watchdog-Thread");
		watchdogTimer.schedule(new TimerTask() {
			public void run() {
				watchdog.run();
			}
		}, interval*1000, interval * 1000);
		Debug.msg("Watchdog enabled"); //$NON-NLS-1$
	}

	/**
	 * @Brian Jensen This function changes the state of the ResourceBundle
	 *        object currently available locales: see lang subdirectory Then it
	 *        destroys the old window and redraws a new one with new locale
	 *
	 * @param l
	 *            the locale to change the language to
	 */
	public void createNewWindow(Locale l) {
		Debug.msg("Loading new locale"); //$NON-NLS-1$
		Main.loadMessages(l);

		refreshWindow();

	}

	/**
	 * Sets default Look'n'Feel
	 */
	public void setDefaultLookAndFeel() {
		JFritzWindow.setDefaultLookAndFeelDecorated(true);
		try {
			Debug.msg("Changing look and feel to: " + Main.getStateProperty("lookandfeel", //$NON-NLS-1$
					UIManager.getSystemLookAndFeelClassName()));
			UIManager.setLookAndFeel(Main.getStateProperty("lookandfeel", //$NON-NLS-1$
					UIManager.getSystemLookAndFeelClassName()));
			if ( jframe != null )
			{
				SwingUtilities.updateComponentTreeUI(jframe);
			}
			// Wunsch eines MAC Users, dass das Default LookAndFeel des
			// Betriebssystems genommen wird
		} catch (Exception ex) {
			Debug.err(ex.toString());
		}
	}

	/**
	 * @ Bastian Schaefer
	 *
	 * Destroys and repaints the Main Frame.
	 *
	 */

	public void refreshWindow() {
		jframe.dispose();
		setDefaultLookAndFeel();
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe = new JFritzWindow(this);
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe.checkOptions();
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe.setVisible(true);

	}

	boolean maybeExit(int i) {
		boolean exit = true;
		if (JFritzUtils.parseBoolean(Main.getProperty(
				"option.confirmOnExit", "false"))) { //$NON-NLS-1$ $NON-NLS-2$
			exit = showExitDialog();
		}
		if (exit) {
			main.exit(0);
		}
		return exit;
	}

	public static String showPasswordDialog(String str)
	{
		return main.showPasswordDialog(str);
	}

	void prepareShutdown(boolean shutdownThread, boolean shutdownHook) throws InterruptedException {
		// TODO maybe some more cleanup is needed
		Debug.msg("prepareShutdown in JFritz.java");

		if ( jframe != null) {
			jframe.prepareShutdown();
			Main.saveStateProperties();
		}

		Debug.msg("Stopping reverse lookup");
		ReverseLookup.terminate();

		Debug.msg("Stopping callMonitor"); //$NON-NLS-1$
		if (callMonitor != null)
		{
			callMonitor.stopCallMonitor();
		}

		if ( (Main.SYSTRAY_SUPPORT) && (systray != null) )
		{
			Debug.msg("Removing systray"); //$NON-NLS-1$
			systray.removeTrayIcon(trayIcon);
			systray = null;
		}

		Debug.msg("Stopping watchdog"); //$NON-NLS-1$

		if ( watchdog != null ) {
			watchdogTimer.cancel();
			watchdog = null;
			watchdogTimer = null;
//			// FIXME: interrupt() lässt JFritz beim System-Shutdown hängen
//			//			watchdog.interrupt();
		}

		Debug.msg("prepareShutdown in JFritz.java done");

		// Keep this order to properly shutdown windows. First interrupt thread,
		// then dispose.
		if ( ((shutdownThread) || (shutdownHook)) && (jframe != null))
		{
			jframe.interrupt();
		}
		// This must be the last call, after disposing JFritzWindow nothing
		// is executed at windows-shutdown
		if ( (!shutdownThread) && (!shutdownHook) && (jframe != null) )
		{
			jframe.dispose();
		}
	}

	/**
	 * Shows the exit dialog
	 */
	boolean showExitDialog() {
		boolean exit = true;
		exit = JOptionPane.showConfirmDialog(jframe, Main
				.getMessage("really_quit"), Main.PROGRAM_NAME, //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

		return exit;
	}

	/**
	 * Deletes actual systemtray and creates a new one.
	 *
	 * @author Benjamin Schmitt
	 */
	public void refreshTrayMenu() {
		if (systray != null && trayIcon != null) {
			systray.removeTrayIcon(trayIcon);
			createTrayMenu();
		}
	}

	/**
	 * Returns reference on current FritzBox-class
	 *
	 * @return
	 */
	public static FritzBox getFritzBox() {
		return fritzBox;
	}

	public static void loadNumberSettings() {
		// load the different area code -> city mappings
		ReverseLookup.loadSettings();
		PhoneNumber.loadFlagMap();
		PhoneNumber.loadCbCXMLFile();
	}

	public static URL getRingSound() {
		return ringSound;
	}

	public static URL getCallSound() {
		return callSound;
	}

	public static CallMonitorList getCallMonitorList() {
		return callMonitorList;
	}

	public void statusChanged(Object status) {
		String statusMsg = "";

		if(status instanceof Integer){
			int duration = ((Integer)status).intValue();
			int hours = duration / 3600;
			int mins = duration % 3600 / 60;
			 statusMsg = Main.getMessage("telephone_entries").replaceAll("%N", Integer.toString(JFritz.getCallerList().getRowCount())) + ", " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
					+ Main.getMessage("total_duration") + ": " + hours + "h " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
					+ mins + " min " + " (" + duration / 60 + " min)"; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			;
		}
		if(status instanceof String){
			statusMsg = (String) status;
		}
		jframe.setStatus(statusMsg);
	}

	public static QuickDials getQuickDials() {
		return quickDials;
	}

	public static ClientLoginsTableModel getClientLogins(){
		return clientLogins;
	}

	/**
	 * ItemListener for LookAndFeel Menu
	 *
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent ie) {
		JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) ie.getSource();
		if (rbmi.isSelected()) {
			UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) rbmi
					.getClientProperty("lnf name"); //$NON-NLS-1$
			try {
//				UIManager.setLookAndFeel(info.getClassName());
				Main.setStateProperty("lookandfeel", info.getClassName()); //$NON-NLS-1$
				refreshWindow();
			} catch (Exception e) {
				Debug.err("Unable to set UI " + e.getMessage()); //$NON-NLS-1$
			}
		}
	}

}