/**
 *
 */

package de.moonflower.jfritz;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jd.nutils.OSDetector;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.BoxCommunication;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.callmonitor.CallMonitorList;
import de.moonflower.jfritz.callmonitor.DisconnectMonitor;
import de.moonflower.jfritz.callmonitor.DisplayCallsMonitor;
import de.moonflower.jfritz.dialogs.quickdial.QuickDials;
import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.network.ClientLoginsTableModel;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.sounds.PlaySound;
import de.moonflower.jfritz.sounds.SoundProvider;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.tray.ClickListener;
import de.moonflower.jfritz.tray.JDICTray;
import de.moonflower.jfritz.tray.SwingTray;
import de.moonflower.jfritz.tray.Tray;
import de.moonflower.jfritz.tray.TrayMenu;
import de.moonflower.jfritz.tray.TrayMenuItem;
import de.moonflower.jfritz.utils.ComplexJOptionPaneMessage;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StatusListener;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

/**
 *
 */
public final class JFritz implements  StatusListener {
	public final static String DOCUMENTATION_URL = "http://www.jfritz.org/wiki/Kategorie:Hilfe"; //$NON-NLS-1$

	public final static String CALLS_FILE = "jfritz.calls.xml"; //$NON-NLS-1$

	public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml"; //$NON-NLS-1$

	public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml"; //$NON-NLS-1$

	public final static String CLIENT_SETTINGS_FILE = "jfritz.clientsettings.xml"; //$NON-NLS-1$

	public final static String CALLS_CSV_FILE = "calls.csv"; //$NON-NLS-1$

	public final static String PHONEBOOK_CSV_FILE = "contacts.csv"; //$NON-NLS-1$

	private static Tray tray;

	private static JFritzWindow jframe;

	private static CallerList callerlist;

	private static ImageIcon trayIcon;

	private static PhoneBook phonebook;

	private static WatchdogThread watchdog;

	private static Timer watchdogTimer;

	private static QuickDials quickDials;

	public static CallMonitorList callMonitorList;

	private static Main main;

	private static ClientLoginsTableModel clientLogins;

	private static boolean shutdownInvoked = false;

	private static boolean wizardCanceled = false;

	private static BoxCommunication boxCommunication;

	private static SoundProvider soundProvider;

	private static PlaySound playSound;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

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

		if (JFritzUtils.parseBoolean(properties.getProperty(
				"option.createBackup"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			Main.doBackup();
		}

			//option was removed from the config dialog in 0.7.1, make sure
			//it is automatically deselected
		if(properties.getProperty("option.callMonitorType").equals("6"))
			properties.setProperty("option.callMonitorType", "0");

		// make sure there is a plus on the country code, or else the number
		// scheme won't work
		if (!properties.getProperty("country.code").startsWith("+"))
			properties.setProperty("country.code", "+"
					+ properties.getProperty("country.code"));

		if (OSDetector.isMac()) { //$NON-NLS-1$
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

		FritzBox fritzBox = new FritzBox("Fritz!Box",
									     "My Fritz!Box",
									     "http",
										 properties.getProperty("box.address"),
										 properties.getProperty("box.port"),
										 Encryption.decrypt(properties.getProperty("box.password")),
										 ex);

		if ( ex != null)
		{
			try {
				throw ex;
			} catch (Exception e)
			{
				Debug.error(e.toString());
			}
		}
		boxCommunication = new BoxCommunication();
		boxCommunication.addBox(fritzBox);

		// if a mac address is set and this box has a different mac address, ask user
		// if communication to this box should be allowed.
		String macStr = properties.getProperty("box.mac");
		if ((!("".equals(macStr))
		&& ( !("".equals(fritzBox.getMacAddress())))
		&& (fritzBox.getMacAddress() != null)))
		{
			ComplexJOptionPaneMessage msg = null;
			int answer = JOptionPane.YES_OPTION;
			if (messages.getMessage("unknown").equals(fritzBox.getMacAddress()))
			{
				Debug.info("MAC-Address could not be determined. Ask user how to proceed..."); //$NON-NLS-1$
				msg = new ComplexJOptionPaneMessage("legalInfo.macNotFound",
						messages.getMessage("mac_not_found") + "\n"
						+ messages.getMessage("accept_fritzbox_communication")); //$NON-NLS-1$
				if (msg.showDialogEnabled()) {
					answer = JOptionPane.showConfirmDialog(null,
							msg.getComponents(),
							messages.getMessage("information"), JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION)
					{
						msg.saveProperty();
						properties.saveStateProperties();
					}
				}
			} else if ( !(macStr.equals(fritzBox.getMacAddress())))
			{
				Debug.info("New FRITZ!Box detected. Ask user how to proceed..."); //$NON-NLS-1$
				msg = new ComplexJOptionPaneMessage("legalInfo.newBox",
						messages.getMessage("new_fritzbox") + "\n"
						+ messages.getMessage("accept_fritzbox_communication")); //$NON-NLS-1$
				if (msg.showDialogEnabled()) {
					answer = JOptionPane.showConfirmDialog(null,
							msg.getComponents(),
							messages.getMessage("information"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
					if (answer == JOptionPane.YES_OPTION)
					{
						msg.saveProperty();
						properties.saveStateProperties();
					}
				}
			}
			if (answer == JOptionPane.YES_OPTION) {
				Debug.info("User decided to accept connection."); //$NON-NLS-1$
				properties.setProperty("box.mac", fritzBox.getMacAddress());
				properties.saveConfigProperties();
				result = 0;
			} else {
				Debug.info("User decided to prohibit connection."); //$NON-NLS-1$
				result = -1;
			}
		}
		return result;
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

//		phonebook.findAllLastCalls();
//		callerlist.findAllPersons();
	}

	public void initSounds() {
		soundProvider = new SoundProvider();
		playSound = new PlaySound(soundProvider);
	}

	public void initCallMonitorListener()
	{
		callMonitorList = new CallMonitorList();
		callMonitorList.addCallMonitorListener(new DisplayCallsMonitor(playSound));
		callMonitorList.addCallMonitorListener(new DisconnectMonitor());
	}

	public void initClientServer()
	{
		clientLogins = new ClientLoginsTableModel();

		ClientLoginsTableModel.loadFromXMLFile(Main.SAVE_DIR+CLIENT_SETTINGS_FILE);
	}

	public void createJFrame(boolean showConfWizard) {
		Debug.info("New instance of JFrame"); //$NON-NLS-1$
		jframe = new JFritzWindow(this);
		if (Main.checkForSystraySupport()) {
			Debug.info("Check Systray-Support"); //$NON-NLS-1$
			try {
				if(Integer.parseInt(System.getProperty("java.version").substring(2, 3)) < 6)
				{
					tray = new JDICTray();
				} else {
					tray = new SwingTray();
				}
				createTrayMenu();
			} catch (Throwable e) {
				Main.systraySupport = false;
				Debug.error(e.toString());
			}
		}
		jframe.checkStartOptions();

		if (!shutdownInvoked && showConfWizard) {
			Debug.info("Presenting user with the configuration dialog");
			wizardCanceled = jframe.showConfigWizard();
		}

		if (!shutdownInvoked)
		{
			javax.swing.SwingUtilities.invokeLater(jframe);

			if(properties.getProperty("network.type").equals("1") &&
					Boolean.parseBoolean(properties.getProperty("option.listenOnStartup"))){
				Debug.info("listening on startup enabled, starting client listener!");
				NetworkStateMonitor.startServer();
			}else if(properties.getProperty("network.type").equals("2") &&
					Boolean.parseBoolean(properties.getProperty("option.connectOnStartup"))){
				Debug.info("Connect on startup enabled, connectig to server");
				NetworkStateMonitor.startClient();
			}
			startWatchdog();
		}

		boxCommunication.registerCallMonitorStateListener(jframe);
		boxCommunication.registerCallListProgressListener(jframe.getCallerListPanel());
		boxCommunication.registerCallListProgressListener(getCallerList());
		boxCommunication.registerBoxStatusListener(jframe);
		boxCommunication.registerBoxCallBackListener(JFritz.getCallerList());
	}

	/**
	 * This constructor is used for JUnit based testing suites
	 * Only the default settings are loaded for this jfritz object
	 *
	 * @author brian jensen
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 * @throws WrongPasswordException
	 */
	public JFritz(String test) throws WrongPasswordException, InvalidFirmwareException, IOException {

		// make sure there is a plus on the country code, or else the number
		// scheme won't work
		if (!properties.getProperty("country.code").startsWith("+"))
			properties.setProperty("country.code", "+"
					+ properties.getProperty("country.code"));

		// loadSounds();

		// loads various country specific number settings and tables
		loadNumberSettings();

		Exception ex = null;

		FritzBox fritzBox = new FritzBox("Fritz!Box",
									     "My Fritz!Box",
									     "http",
										 properties.getProperty("box.address"),
										 properties.getProperty("box.port"),
										 Encryption.decrypt(properties.getProperty("box.password")),
										 ex);

		if ( ex != null)
		{
			try {
				throw ex;
			} catch (Exception e)
			{
				Debug.error(e.toString());
			}
		}
		boxCommunication = new BoxCommunication();
		boxCommunication.addBox(fritzBox);

		callerlist = new CallerList();
		// callerlist.loadFromXMLFile(SAVE_DIR + CALLS_FILE);

		phonebook = new PhoneBook(PHONEBOOK_FILE);
		// phonebook.loadFromXMLFile(SAVE_DIR + PHONEBOOK_FILE);
		phonebook.setCallerList(callerlist);
		callerlist.setPhoneBook(phonebook);
	}

	/**
	 * Creates the tray icon menu
	 */
	private void createTrayMenu() {
		System.setProperty("javax.swing.adjustPopupLocationToFit", "false"); //$NON-NLS-1$,  //$NON-NLS-2$

		LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
		ButtonGroup lnfgroup = new ButtonGroup();

		JMenu lnfMenu = new JMenu(messages.getMessage("lnf_menu")); //$NON-NLS-1$
		// Add system dependent look and feels
		for (int i = 0; i < lnfs.length; i++) {
			JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lnfs[i]
					.getName());
			lnfMenu.add(rbmi);
			rbmi.setSelected(UIManager.getLookAndFeel().getClass().getName()
					.equals(lnfs[i].getClassName()));
			rbmi.putClientProperty("lnf name", lnfs[i]); //$NON-NLS-1$
			rbmi.addItemListener(jframe);
			lnfgroup.add(rbmi);
		}

		// Add additional look and feels from looks-2.1.4.jar
		LookAndFeelInfo lnf = new LookAndFeelInfo("Plastic","com.jgoodies.looks.plastic.PlasticLookAndFeel");
		JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(jframe);
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic 3D","com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(jframe);
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic XP","com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(jframe);
		lnfgroup.add(rb);


		TrayMenu menu = new TrayMenu("JFritz Menu"); //$NON-NLS-1$
		TrayMenuItem menuItem = new TrayMenuItem(ProgramConstants.PROGRAM_NAME + " v" //$NON-NLS-1$
				+ ProgramConstants.PROGRAM_VERSION);
		menuItem.setActionCommand("showhide");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.addSeparator();
		for (int i=0; i<getBoxCommunication().getBoxCount(); i++) {
			String boxName = getBoxCommunication().getBox(i).getName();
			BoxClass box = getBoxCommunication().getBox(boxName);
			if (box != null) {
				JMenu boxItem = new JMenu(boxName);
				menuItem = new TrayMenuItem("IP: " + box.getExternalIP());
				boxItem.add(menuItem.getJMenuItem());
				boxItem.addSeparator();
				menuItem = new TrayMenuItem(messages.getMessage("fetchlist"));
				menuItem.setActionCommand("fetchList-"+boxName);
				menuItem.addActionListener(jframe);
				boxItem.add(menuItem.getJMenuItem());
				menuItem = new TrayMenuItem(messages.getMessage("renew_ip"));
				menuItem.setActionCommand("renewIP-"+boxName);
				menuItem.addActionListener(jframe);
				boxItem.add(menuItem.getJMenuItem());
				menuItem = new TrayMenuItem("Reboot");
				menuItem.setActionCommand("reboot-"+boxName);
				menuItem.addActionListener(jframe);
				boxItem.add(menuItem.getJMenuItem());
				menu.add(boxItem);
			}
		}
		menu.addSeparator();
		menuItem = new TrayMenuItem(messages.getMessage("fetchlist")); //$NON-NLS-1$
		menuItem.setActionCommand("fetchList"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menuItem = new TrayMenuItem(messages.getMessage("reverse_lookup")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menuItem = new TrayMenuItem(messages.getMessage("dial_assist")); //$NON-NLS-1$
		menuItem.setActionCommand("callDialog");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menuItem = new TrayMenuItem(messages.getMessage("dial_assist") + "(" + messages.getMessage("clipboard") + ")"); //$NON-NLS-1$
		menuItem.setActionCommand("callDialogTray");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.add(lnfMenu);
		menuItem = new TrayMenuItem(messages.getMessage("config")); //$NON-NLS-1$
		menuItem.setActionCommand("config"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new TrayMenuItem(messages.getMessage("prog_exit")); //$NON-NLS-1$
		menuItem.setActionCommand("exit"); //$NON-NLS-1$
		menuItem.addActionListener(jframe);
		menu.add(menuItem);

		trayIcon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/trayicon.png")); //$NON-NLS-1$

		tray.add(trayIcon);
		tray.setTooltip(ProgramConstants.PROGRAM_NAME + " v"+ProgramConstants.PROGRAM_VERSION);
		tray.setPopupMenu(menu);
		refreshTrayActionListener();
	}

	private void refreshTrayActionListener() {
		String trayClick = properties.getProperty("tray.clickCount");
		int clickCount = ClickListener.CLICK_COUNT_SINGLE;
		if ("2".equals(trayClick)) {
			clickCount = ClickListener.CLICK_COUNT_DOUBLE;
		}

		tray.clearActionListeners();
		tray.addActionListener(new ClickListener(ClickListener.CLICK_LEFT,
												 clickCount) {
			private long oldTimeStamp = 0;
			private void showHide() {
				if ( jframe != null )
				{
					jframe.hideShowJFritz(true);
				}
			}

			public void actionPerformed(ActionEvent e) {
				if (tray instanceof JDICTray) {
					// old JDICTray has no mouse listener,
					// get timestamp to simulate single/double-click
					if (this.getClickCount() == ClickListener.CLICK_COUNT_SINGLE) {
						long timeStamp = e.getWhen();
						if ( timeStamp-oldTimeStamp>600 ) {
							showHide();
							oldTimeStamp = timeStamp;
						}
					} else {
						long timeStamp = e.getWhen();
						if ( timeStamp-oldTimeStamp<600 ) {
							showHide();
						}
						oldTimeStamp = timeStamp;
					}
				} else if (tray instanceof SwingTray) {
					showHide();
				}
			}
		});
	}

	/**
	 * Displays balloon info message
	 *
	 * @param msg
	 *            Message to be displayed
	 */
	public static void infoMsg(String msg) {
		switch (Integer.parseInt(PropertyProvider.getInstance().getProperty("option.popuptype"))) { //$NON-NLS-1$,  //$NON-NLS-2$
		case 0: { // No Popup
			break;
		}
		case 1: {
			MessageDlg msgDialog = new MessageDlg();
			msgDialog.showMessage(msg, Long.parseLong(PropertyProvider.getInstance().getProperty(
					"option.popupDelay")) * 1000);
			msgDialog.repaint();
			msgDialog.toFront();
			break;
		}
		case 2: {
			if (tray.isSupported())
				tray.displayMessage(ProgramConstants.PROGRAM_NAME, msg,
						Tray.MESSAGE_TYPE_INFO);
			else {
				MessageDlg msgDialog = new MessageDlg();
				msgDialog.showMessage(msg, Long.parseLong(PropertyProvider.getInstance().getProperty(
						"option.popupDelay")) * 1000);
				msgDialog.repaint();
				msgDialog.toFront();
			}
			break;
		}
		}
	}

	/**
	 * Displays balloon error message
	 *
	 * @param msg
	 */
	public static void errorMsg(String msg) {
		Debug.error(msg);
		if (Main.systraySupport && tray != null) {
			tray.displayMessage(ProgramConstants.PROGRAM_NAME, msg,
					Tray.MESSAGE_TYPE_ERROR);
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
	 * start timer for watchdog
	 *
	 */
	private void startWatchdog() {
		if (!shutdownInvoked)
		{
			int interval = 5; // seconds
			int factor = 2; // factor how many times a STANDBY will be checked
			watchdogTimer = new Timer("Watchdog-Timer", true);
			watchdog = new WatchdogThread(interval, factor);
			watchdogTimer.schedule(new TimerTask() {
				public void run() {
					if (shutdownInvoked)
						this.cancel();
					watchdog.run();
				}
			}, interval*1000, interval * 1000);
			Debug.info("Watchdog enabled"); //$NON-NLS-1$
		}
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
		Debug.info("Loading new locale"); //$NON-NLS-1$
		messages.loadMessages(l);

		refreshWindow();
	}

	/**
	 * Sets default Look'n'Feel
	 */
	public void setDefaultLookAndFeel() {
		if (JFritzUtils.parseBoolean(properties.getProperty("window.useDecorations"))) {
			JFritzWindow.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			JFrame.setDefaultLookAndFeelDecorated(true);
		} else {
			JFritzWindow.setDefaultLookAndFeelDecorated(false);
			JDialog.setDefaultLookAndFeelDecorated(false);
			JFrame.setDefaultLookAndFeelDecorated(false);
		}
		try {
			Debug.info("Changing look and feel to: " + properties.getStateProperty("lookandfeel")); //$NON-NLS-1$
			UIManager.setLookAndFeel(properties.getStateProperty("lookandfeel")); //$NON-NLS-1$
			if ( jframe != null )
			{
				SwingUtilities.updateComponentTreeUI(jframe);
			}
			// Wunsch eines MAC Users, dass das Default LookAndFeel des
			// Betriebssystems genommen wird
		} catch (Exception ex) {
			Debug.error(ex.toString());
		}
	}

	/**
	 * @ Bastian Schaefer
	 *
	 * Destroys and repaints the Main Frame.
	 *
	 */

	public void refreshWindow() {
		boxCommunication.unregisterCallMonitorStateListener(jframe);
		boxCommunication.unregisterCallListProgressListener(jframe.getCallerListPanel());
		jframe.dispose();
		setDefaultLookAndFeel();
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe = new JFritzWindow(this);
		boxCommunication.registerCallMonitorStateListener(jframe);
		boxCommunication.registerCallListProgressListener(jframe.getCallerListPanel());
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe.checkOptions();
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe.setVisible(true);
	}

	boolean maybeExit(int i, boolean check) {
		boolean exit = true;
		if (check &&
				JFritzUtils.parseBoolean(properties.getProperty(
				"option.confirmOnExit"))) { //$NON-NLS-1$ $NON-NLS-2$
			exit = showExitDialog();
		}
		if (exit) {
			main.exit(0);
		}
		return exit;
	}

	void prepareShutdown(boolean shutdownThread, boolean shutdownHook) throws InterruptedException {
		shutdownInvoked = true;

		// TODO maybe some more cleanup is needed
		Debug.debug("prepareShutdown in JFritz.java");

		if ( jframe != null) {
			jframe.prepareShutdown();
			properties.saveStateProperties();
		}

		Debug.info("Stopping reverse lookup");
		ReverseLookup.terminate();

		if ( (Main.systraySupport) && (tray != null) )
		{
			Debug.info("Removing systray"); //$NON-NLS-1$
			tray.remove();
			tray = null;
		}

		Debug.info("Stopping watchdog"); //$NON-NLS-1$

		if ( watchdog != null ) {
			watchdogTimer.cancel();
			watchdog = null;
			watchdogTimer = null;
//			// FIXME: interrupt() lässt JFritz beim System-Shutdown hängen
//			//			watchdog.interrupt();
		}

		Debug.debug("prepareShutdown in JFritz.java done");

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
		exit = JOptionPane.showConfirmDialog(jframe, messages
				.getMessage("really_quit"), ProgramConstants.PROGRAM_NAME, //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

		return exit;
	}

	/**
	 * Deletes actual systemtray and creates a new one.
	 *
	 * @author Benjamin Schmitt
	 */
	public void refreshTrayMenu() {
		if (tray != null && trayIcon != null) {
			tray.remove();
			createTrayMenu();
		}
	}

	public static void loadNumberSettings() {
		// load the different area code -> city mappings
		ReverseLookup.loadSettings();
		PhoneNumberOld.loadFlagMap();
		PhoneNumberOld.loadCbCXMLFile();
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
			 statusMsg = messages.getMessage("telephone_entries").replaceAll("%N", Integer.toString(JFritz.getCallerList().getRowCount())) + ", " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
					+ messages.getMessage("total_duration") + ": " + hours + "h " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
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

	public static boolean isShutdownInvoked()
	{
		return shutdownInvoked;
	}

	public static boolean isWizardCanceled()
	{
		return wizardCanceled;
	}

	public static BoxCommunication getBoxCommunication()
	{
		return boxCommunication;
	}
}