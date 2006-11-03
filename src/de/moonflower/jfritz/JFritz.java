/**
 *
 */

package de.moonflower.jfritz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.callmonitor.CallMonitorInterface;
import de.moonflower.jfritz.callmonitor.CallMonitorList;
import de.moonflower.jfritz.callmonitor.DisconnectMonitor;
import de.moonflower.jfritz.callmonitor.DisplayCallsMonitor;
import de.moonflower.jfritz.dialogs.configwizard.ConfigWizard;
import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.FritzBox;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;
import de.moonflower.jfritz.utils.network.SSDPdiscoverThread;

/**
 * @author Arno Willig
 *
 */
public final class JFritz {

	// when changing this, don't forget to check the resource bundles!!

	public final static String PROGRAM_SECRET = "jFrItZsEcReT"; //$NON-NLS-1$

	public final static String DOCUMENTATION_URL = "http://www.jfritz.org/wiki/Kategorie:Hilfe"; //$NON-NLS-1$

	public final static String CVS_TAG = "$Id: JFritz.java,v 1.372 2006/11/03 16:45:28 robotniko Exp $"; //$NON-NLS-1$

	public final static String CALLS_FILE = "jfritz.calls.xml"; //$NON-NLS-1$

	public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml"; //$NON-NLS-1$

	public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml"; //$NON-NLS-1$

	public final static String SIPPROVIDER_FILE = "jfritz.sipprovider.xml"; //$NON-NLS-1$

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

	public static final int CALLMONITOR_START = 0;

	public static final int CALLMONITOR_STOP = 1;

	private static WatchdogThread watchdog;

	private static FritzBox fritzBox;

	private static int oldFrameState; // saves old frame state to restore old

	// state

	public static CallMonitorList callMonitorList;

	private Main main;

	/**
	 * Constructs JFritz object
	 */
	public JFritz(Main main) {
		this.main = main;

		if (JFritzUtils.parseBoolean(Main.getProperty(
				"option.createBackup", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			Main.doBackup();
		}

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

		// loads various country specific number settings and tables
		loadNumberSettings();

		fritzBox = new FritzBox(Main
				.getProperty("box.address", "192.168.178.1"), Encryption //$NON-NLS-1$,  //$NON-NLS-2$
				.decrypt(Main.getProperty("box.password", Encryption //$NON-NLS-1$
						.encrypt(""))), Main.getProperty("box.port", "80")); //$NON-NLS-1$
		sipprovider = new SipProviderTableModel();
		sipprovider.loadFromXMLFile(Main.SAVE_DIR + SIPPROVIDER_FILE);

		callerlist = new CallerList();
		phonebook = new PhoneBook(PHONEBOOK_FILE);
		callerlist.setPhoneBook(phonebook);
		phonebook.setCallerList(callerlist);

		phonebook.loadFromXMLFile(Main.SAVE_DIR + PHONEBOOK_FILE);
		callerlist.loadFromXMLFile(Main.SAVE_DIR + CALLS_FILE);
		phonebook.findAllLastCalls();
		callerlist.findAllPersons();

		callMonitorList = new CallMonitorList();
		callMonitorList.addCallMonitorListener(new DisplayCallsMonitor());
		callMonitorList.addCallMonitorListener(new DisconnectMonitor());

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
		try {
			jframe = new JFritzWindow(this);
		} catch (WrongPasswordException wpe) {
			exit(0);
		}
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

		if (JFritzUtils.parseBoolean(Main.getProperty("option.useSSDP",//$NON-NLS-1$
				"true"))) {//$NON-NLS-1$
			Debug.msg("Searching for  FritzBox per UPnP / SSDP");//$NON-NLS-1$

			ssdpthread = new SSDPdiscoverThread(SSDP_TIMEOUT);
			ssdpthread.start();
			try {
				ssdpthread.join();
			} catch (InterruptedException ie) {

			}
		}

		if (showConfWizard) {
			Debug.msg("Presenting user with the configuration dialog");
			showConfigWizard();
		}

		javax.swing.SwingUtilities.invokeLater(jframe);

		startWatchdog();

	}

	/**
	 * This constructor is used for JUnit based testing suites Only the default
	 * settings are loaded for this jfritz object
	 *
	 * @author brian jensen
	 */
	public JFritz(String test) {

		// make sure there is a plus on the country code, or else the number
		// scheme won't work
		if (!Main.getProperty("country.code").startsWith("+"))
			Main.setProperty("country.code", "+"
					+ Main.getProperty("country.code"));

		// loadSounds();

		// loads various country specific number settings and tables
		loadNumberSettings();

		fritzBox = new FritzBox(Main
				.getProperty("box.address", "192.168.178.1"), Encryption //$NON-NLS-1$, //$NON-NLS-2$
				.decrypt(Main.getProperty("box.password", Encryption //$NON-NLS-1$
						.encrypt(""))), Main.getProperty("box.port", "80")); // //$NON-NLS-1$
		// $NON-NLS-2$

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
	private static void createTrayMenu() {
		System.setProperty("javax.swing.adjustPopupLocationToFit", "false"); //$NON-NLS-1$,  //$NON-NLS-2$

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
			public void actionPerformed(ActionEvent e) {
				hideShowJFritz();
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
	 */
	public static void playSound(URL sound) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(sound);
			DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat(),
					((int) ais.getFrameLength() * ais.getFormat()
							.getFrameSize()));
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(ais);
			clip.start();
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
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
		if (JFritzUtils.parseBoolean(Main.getProperty("option.useSSDP", //$NON-NLS-1$
				"true"))) { //$NON-NLS-1$
			try {
				ssdpthread.join();
			} catch (InterruptedException e) {
			}
			return ssdpthread.getDevices();
		} else
			return null;
	}

	public static void stopCallMonitor() {
		if (callMonitor != null) {
			callMonitor.stopCallMonitor();
			// Let buttons enable start of callMonitor
			getJframe().setCallMonitorButtons(CALLMONITOR_START);
			callMonitor = null;
		}
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

	public static void hideShowJFritz() {
		if (jframe.isVisible()) {
			oldFrameState = jframe.getExtendedState();
			Debug.msg("Hide JFritz-Window"); //$NON-NLS-1$
			jframe.setExtendedState(JFrame.ICONIFIED);
			jframe.setVisible(false);
		} else {
			Debug.msg("Show JFritz-Window"); //$NON-NLS-1$
			jframe.setVisible(true);
			jframe.toFront();
			jframe.setExtendedState(oldFrameState);
		}
	}

	public static SipProviderTableModel getSIPProviderTableModel() {
		return sipprovider;
	}

	/**
	 * start timer for watchdog
	 *
	 */
	private static void startWatchdog() {
		Timer timer = new Timer();
		watchdog = new WatchdogThread(1);
		timer.schedule(new TimerTask() {
			public void run() {
				watchdog.run();
			}
		}, 5000, 1 * 60000);
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
	 * @ Bastian Schaefer
	 *
	 * Destroys and repaints the Main Frame.
	 *
	 */

	public void refreshWindow() {
		jframe.saveWindowProperties();
		jframe.dispose();
		javax.swing.SwingUtilities.invokeLater(jframe);
		try {
			jframe = new JFritzWindow(this);
		} catch (WrongPasswordException wpe) {
			exit(0);
		}
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe.checkOptions();
		javax.swing.SwingUtilities.invokeLater(jframe);
		jframe.setVisible(true);

	}

	void maybeExit(int i) {
		boolean exit = true;
		if (JFritzUtils.parseBoolean(Main.getProperty(
				"option.confirmOnExit", "false"))) { //$NON-NLS-1$ $NON-NLS-2$
			exit = showExitDialog();
		}
		if (exit) {
			exit(0);
		}
	}

	/**
	 * clean up and exit
	 *
	 * @param i
	 *            exit status.
	 */
	void exit(int i) {
		Debug.msg("Shut down JFritz");

		// TODO maybe some more cleanup is needed
		jframe.saveProperties();

		if (callMonitor != null) {
			callMonitor.stopCallMonitor();
		}
		Debug.msg("disposing jframe");
		if (jframe != null)
			jframe.dispose();
		main.exit(i);
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
	public static void refreshTrayMenu() {
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

	/**
	 * @author Brian Jensen This creates and then display the config wizard
	 *
	 */
	public static void showConfigWizard() {
		ConfigWizard wizard = new ConfigWizard(jframe);
		wizard.showWizard();

	}

	public static void reverseLookup() {
		Debug.msg("Doing reverse Lookup");
		int j = 0;
		for (int i = 0; i < getCallerList().getRowCount(); i++) {
			Vector data = getCallerList().getFilteredCallVector();
			Call call = (Call) data.get(i);
			PhoneNumber number = call.getPhoneNumber();
			if (number != null && (call.getPerson() == null)) {
				j++;

				Debug.msg("Reverse lookup for " //$NON-NLS-1$
						+ number.getIntNumber());

				Person newPerson = ReverseLookup.lookup(number);
				if (newPerson != null) {
					getPhonebook().addEntry(newPerson);
					getPhonebook().fireTableDataChanged();
					getCallerList().fireTableDataChanged();
				}

			}
		}

		if (j > 0)
			getPhonebook().saveToXMLFile(Main.SAVE_DIR + JFritz.PHONEBOOK_FILE);

	}

	public static void loadNumberSettings() {
		// load the different area code -> city mappings
		ReverseLookup.loadAreaCodes();
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
}