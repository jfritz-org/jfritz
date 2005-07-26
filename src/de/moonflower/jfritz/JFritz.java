/**
 *
 * JFritz!
 * http://jfritz.sourceforge.net/
 *
 *
 * (c) Arno Willig <akw@thinkwiki.org>
 *
 * Created on 08.04.2005
 *
 * Authors working on the project:
 * 		akw			Arno Willig <akw@thinkwiki.org>
 * 		robotniko	Robert Palmer <robotniko@gmx.de>
 *
 *
 * This tool is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This piece of software is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * GLOBAL TODO:
 *
 * CallerList: Einträge löschen älter als Datum
 * CallerList: ev. Popup-Menu? (finde ich auch gut => löschen, reverselookup, ... Rob)
 * Statistik: Top-Caller (Name/Nummer, Wie oft, Wie lange)
 * Watchdog for CallMonitor
 *
 * BUG: No new Phonebook entries after reverselookup, only after restart or double click on an call entry
 * BUG: Eingabe im IP-Eingabe-PopUp wird ignoriert! (ist glaube ich schon behoben)
 * BUG: Box restart beim Call Monitor (ab und zu wird auch der telefond nicht gestartet)
 * BUG: 2 Einstellungsfenster können geöffnet werden. Eins normal, eines über Tray
 * BUG: die Autoerkennung, ob telefond für Syslog richtig läuft hat ein Sicherheitsloch. Nun kann jede IP auf Port 1011 zugreifen.
 * BUG: Bestätigung beim Schliessen von JFritz ist bei manchen Betriebssystemfunktionen (shutdown, ...) nicht vorhanden
 * BUG: Mehrere Instanzen können zu Datenverlust führen
 *
 * CHANGELOG:
 *
 * JFritz! 0.4.2
 * - CallByCall information is saved
 * - Added Phonebookfilter (Private Phonebook)
 * - Callerlist deleteable
 * - Advanced CSV-File
 * - Callmonitor with Telnet, Syslog, YAC
 * - Syslog passthrough
 * - CMD Option -e : Export CSV
 * - CMD Option -c : Clear Callerlist
 * - CMD Option -l : Debug to Logfile
 * - Bugfix: Statistic-Dialog uses box.ip not 192.168.178.1
 * - Bugfix: Compatibility to Java 1.4.2
 * - Bugfix: Passwords with special chars
 * - Bugfix: Some charset bugfixing
 * - Bugfix: Phonebook XML-Saving fixed (UTF-8 coding)
 *
 * TODO:
 * - Merging of person entries
 * - Implement reverselookup for Switzerland (www.telsearch.ch)
 * - Password Dialog mit "Speichern" Haken
 * - TFTP Box Konfig verwalten
 *
 * JFritz! 0.4.0
 * - Systray minimizes JFrame
 * - Mobile filter inverted
 * - Removed participant support in favour of person
 * - Phonebook support
 * - Added commandline option --fetch
 * - Rewrote xml handler for phonebook
 * - Data statistics
 * - Call monitor with sound notification
 * - Crypted password
 * - Option for password check on program start
 * - Option for disabling sounds
 *
 * Internal:
 * - Added PhoneNumber class
 * - Added PhoneType class
 * - Restructured packages
 *
 *
 * JFritz! 0.3.6
 * - New mobile phone filter feature
 * - Systray support for Linux/Solaris/Windows
 * - Systray ballon messages for Linux/Solaris/Windows
 * - Browser opening on Unix platforms
 * - Bugfix: Call with same timestamp are collected
 *
 * JFritz! 0.3.4
 * - New search filter feature
 * - New date and date range filter feature
 * - Sorting of columns by clicking on column headers
 * - VOIP numbers starting with 49 are now rewritten correctly
 * - SSDP Autodetection of Fritz!Boxes
 * - QuickDial Management
 * - Selection of multiple rows copies VCards to clipboard
 * - Bugfix: Config-Dialog now saves all values correctly
 * - Bugfix: No empty SIP provider after detection
 * - Bugfix: Save-Dialog on export functions
 * - Code rearrangement
 *
 * JFritz! 0.3.2:
 * - Saves and restores window position/size
 * - Saves and restores width of table columns
 * - CallTypeFilter works now (thanks to robotniko)
 * - Filter option is saved
 * - Added filter for calls without displayed number
 * - Total duration of calls now displayed in status bar
 *
 * JFritz! 0.3.0: Major release
 * - Compatibility for JRE 1.4.2
 * - Severel bugfixes
 *
 * JFritz! 0.2.8:
 * - Bugfix: Firmware detection had nasty bug
 * - Bugfix: Firmware detection detects modded firmware properly
 * - Bugfix: RegExps adapted for modded firmware
 * - Support for SIP-Provider for fritzbox fon wlan
 * - Notify users whenn calls have been retrieved
 * - CSV Export
 *
 * JFritz! 0.2.6:
 * - Several bugfixes
 * - Support for Fritz!Boxes with modified firmware
 * - Improved config dialog
 * - Improved firmware detection
 * - Initial support für SIP-Provider
 * - Firmware/SIP-Provider are saved in config file
 *
 * JFritz! 0.2.4:
 * - Several bugfixes
 * - Improventsment on number resolution
 * - Optimized Reverse Lookup
 *
 * JFritz! 0.2.2:
 * - FRITZ!Box FON WLAN works again and is detected automatically.
 * - Target MSN is displayed
 * - Bugfixes for Reverse Lookup (Mobile phone numbers are filtered now)
 * - Nice icons for calltypes (Regular call, Area call, Mobile call)
 * - Several small bugfixes
 *
 * JFritz! 0.2.0: Major release
 * - Improved GUI, arranged colours for win32 platform
 * - New ToolBar with nice icons
 * - Bugfix: Not all calls had been retrieved from box
 * - Improved reverse lookup
 * - Automatic box detection (does not yet work perfectly)
 * - Internal class restructuring
 *
 * JFritz! 0.1.6:
 * - Calls are now saved in XML format
 * - Support for Fritz!Box 7050
 *
 * JFritz! 0.1.0:
 * - Initial version
 */

package de.moonflower.jfritz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.dialogs.phonebook.PhoneBook;
//import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.CLIOptions;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.CLIOption;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.utils.network.SSDPdiscoverThread;
import de.moonflower.jfritz.utils.network.CallMonitor;
//import de.moonflower.jfritz.dialogs.simple.CallMessageDlg;

/**
 * @author Arno Willig
 *
 */
public final class JFritz {

	public final static String PROGRAM_NAME = "JFritz!";

	public final static String PROGRAM_VERSION = "0.4.1";

	public final static String PROGRAM_URL = "http://jfritz.sourceforge.net/";

	public final static String PROGRAM_SECRET = "jFrItZsEcReT";

	public final static String DOCUMENTATION_URL = "http://jfritz.sourceforge.net/documentation.php";

	public final static String CVS_TAG = "$Id: JFritz.java,v 1.89 2005/07/26 16:24:42 jfahrner Exp $";

	public final static String PROGRAM_AUTHOR = "Arno Willig <akw@thinkwiki.org>";

	public final static String PROPERTIES_FILE = "jfritz.properties.xml";

	public final static String CALLS_FILE = "jfritz.calls.xml";

	public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml";

	public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml";

	public final static String CALLS_CSV_FILE = "calls.csv";

	public final static int SSDP_TIMEOUT = 1000;

	public final static int SSDP_MAX_BOXES = 3;

	public final static boolean DEVEL_VERSION = Integer
			.parseInt(PROGRAM_VERSION.substring(PROGRAM_VERSION
					.lastIndexOf(".") + 1)) % 2 == 1;

	public static boolean SYSTRAY_SUPPORT = false;

	private JFritzProperties defaultProperties;

	private static JFritzProperties properties;

	private static ResourceBundle messages;

	private SystemTray systray;

	private JFritzWindow jframe;

	private SSDPdiscoverThread ssdpthread;

	private CallerList callerlist;

	private static TrayIcon trayIcon;

	private static PhoneBook phonebook;

	//private static URL ringSound, callSound;

	private CallMonitor callMonitor = null;

	private static String HostOS = "other";

	/**
	 * Constructs JFritz object
	 */
	public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
			boolean clearList) {
		loadProperties();
		loadMessages(new Locale("de", "DE"));
		loadSounds();

		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS"))
			HostOS = "mac";
		else if (osName.startsWith("Windows"))
			HostOS = "windows";
		Debug.msg("JFritz runs on "+ HostOS);

		phonebook = new PhoneBook(this);
		phonebook.loadFromXMLFile(PHONEBOOK_FILE);

		callerlist = new CallerList(this);
		callerlist.loadFromXMLFile(CALLS_FILE);

		if (fetchCalls) {
			Debug.msg("Anrufliste wird von Fritz!Box geholt..");
			try {
				callerlist.getNewCalls();
			} catch (WrongPasswordException e) {
				Debug.err(e.toString());
			} catch (IOException e) {
				Debug.err(e.toString());
			} finally {
				if (csvExport) {
					Debug.msg("CSV-Export to " + csvFileName);
					callerlist.saveToCSVFile(csvFileName);
				}
				if (clearList) {
					Debug.msg("Clearing Caller List");
					callerlist.clearList();
				}
				Debug.msg("JFritz! beendet sich nun.");
				System.exit(0);
			}
		}
		if (csvExport) {
			Debug.msg("CSV-Export to " + csvFileName);
			callerlist.saveToCSVFile(csvFileName);
			if (clearList) {
				Debug.msg("Clearing Caller List");
				callerlist.clearList();
			}
			System.exit(0);
		}
		if (clearList) {
			Debug.msg("Clearing Caller List");
			callerlist.clearList();
			System.exit(0);
		}
		jframe = new JFritzWindow(this);

		if (checkForSystraySupport()) {
			try {
				systray = SystemTray.getDefaultSystemTray();
				createTrayMenu();
			} catch (Exception e) {
				Debug.err(e.toString());
				SYSTRAY_SUPPORT = false;
			}
		}

		ssdpthread = new SSDPdiscoverThread(this, SSDP_TIMEOUT);
		ssdpthread.start();

		javax.swing.SwingUtilities.invokeLater(jframe);

	}

	/**
	 * Loads sounds from resources
	 */
	private void loadSounds() {
	//	ringSound = getClass().getResource(
	//			"/de/moonflower/jfritz/resources/sounds/call_in.wav");
	//	callSound = getClass().getResource(
	//			"/de/moonflower/jfritz/resources/sounds/call_out.wav");
	}

	/**
	 * Checks for systray availability
	 */
	private boolean checkForSystraySupport() {
		String os = System.getProperty("os.name");
		if (os.equals("Linux") || os.equals("Solaris")
				|| os.startsWith("Windows")) {
			SYSTRAY_SUPPORT = true;
		}
		return SYSTRAY_SUPPORT;
	}

	/**
	 * Main method for starting JFritz!
	 *
	 * @param args
	 *            Program arguments (-h -v)
	 */
	public static void main(String[] args) {
		System.out.println(PROGRAM_NAME + " v" + PROGRAM_VERSION
				+ " (c) 2005 by " + PROGRAM_AUTHOR);
		if (DEVEL_VERSION)
			Debug.on();

		boolean fetchCalls = false;
		boolean clearList = false;
		boolean csvExport = false;
		String csvFileName = "";

		CLIOptions options = new CLIOptions();

		options.addOption('h', "help", null, "This short description");
		options.addOption('v', "verbose", null, "Turn on debug information");
		options.addOption('v', "debug", null, "Turn on debug information");
		options.addOption('s', "systray", null, "Turn on systray support");
		options.addOption('f', "fetch", null, "Fetch new calls and exit");
		options.addOption('c', "clear_list", null,
				"Clears Caller List and exit");
		options.addOption('e', "export", "filename",
				"Fetch calls and export to CSV file.");
		options.addOption('l', "logfile", "filename",
				"Writes debug messages to logfile");

		Vector foundOptions = options.parseOptions(args);
		Enumeration en = foundOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = (CLIOption) en.nextElement();

			switch (option.getShortOption()) {
			case 'h':
				System.out.println("Call: java -jar jfritz.jar [Options]");
				options.printOptions();
				System.exit(0);
				break;
			case 'v':
				Debug.on();
				break;
			case 's':
				JFritz.SYSTRAY_SUPPORT = true;
				break;
			case 'f':
				fetchCalls = true;
				break;
			case 'e':
				csvExport = true;
				csvFileName = option.getParameter();
				if (csvFileName == null) {
					System.err.println("Parameter not found!");
					System.exit(0);
				}
				break;
			case 'c':
				clearList = true;
				break;
			case 'l':
				String logFilename = option.getParameter();
				if (logFilename == null) {
					System.err.println("Parameter not found!");
					System.exit(0);
				} else {
					Debug.logToFile(logFilename);
					break;
				}
			default:
				break;
			}
		}
		new JFritz(fetchCalls, csvExport, csvFileName, clearList);
	}

	/**
	 * Creates the tray icon menu
	 */
	private void createTrayMenu() {
		System.setProperty("javax.swing.adjustPopupLocationToFit", "false");

		JPopupMenu menu = new JPopupMenu("JFritz! Menu");
		JMenuItem menuItem = new JMenuItem(PROGRAM_NAME + " v"
				+ PROGRAM_VERSION);
		menuItem.setEnabled(false);
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem(getMessage("fetchlist"));
		menuItem.setActionCommand("fetchList");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menuItem = new JMenuItem(getMessage("reverse_lookup"));
		menuItem.setActionCommand("reverselookup");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menuItem = new JMenuItem(getMessage("config"));
		menuItem.setActionCommand("config");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem(getMessage("prog_exit"));
		menuItem.setActionCommand("exit");
		menuItem.addActionListener(jframe);
		menu.add(menuItem);

		ImageIcon icon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/trayicon.png"));

		trayIcon = new TrayIcon(icon, "JFritz!", menu);
		trayIcon.setIconAutoSize(false);
		trayIcon
				.setCaption(JFritz.PROGRAM_NAME + " v" + JFritz.PROGRAM_VERSION);
		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (jframe.isVisible()) {
					Debug.msg("Hide JFritz-Window");
					jframe.setState(JFrame.ICONIFIED);
					jframe.setVisible(false);
				} else {
					Debug.msg("Show JFritz-Window");
					jframe.setState(JFrame.NORMAL);
					jframe.setVisible(true);
					jframe.toFront();
				}
			}
		});
		systray.addTrayIcon(trayIcon);
	}

	/**
	 * Loads resource messages
	 *
	 * @param locale
	 */
	private void loadMessages(Locale locale) {
		try {
			messages = ResourceBundle.getBundle(
					"de.moonflower.jfritz.resources.jfritz", locale);
		} catch (MissingResourceException e) {
			Debug.err("Can't find i18n resource!");
			JOptionPane.showMessageDialog(null, JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION
					+ "\n\nCannot start if there is an '!' in path!");
			System.exit(0);
		}
	}

	/**
	 * Loads properties from xml files
	 */
	public void loadProperties() {
		defaultProperties = new JFritzProperties();
		properties = new JFritzProperties(defaultProperties);

		// Default properties
		defaultProperties.setProperty("box.address", "192.168.178.1");
		defaultProperties.setProperty("box.password", Encryption.encrypt(""));
		defaultProperties.setProperty("country.prefix", "00");
		defaultProperties.setProperty("area.prefix", "0");
		defaultProperties.setProperty("country.code", "49");
		defaultProperties.setProperty("area.code", "441");
		defaultProperties.setProperty("fetch.timer", "5");
		defaultProperties.setProperty("option.yacport", "10629");

		try {
			FileInputStream fis = new FileInputStream(JFritz.PROPERTIES_FILE);
			properties.loadFromXML(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Debug.err("File " + JFritz.PROPERTIES_FILE
					+ " not found, using default values");
		} catch (Exception e) {
		}
	}

	/**
	 * Saves properties to xml files
	 */
	public void saveProperties() {

		properties.setProperty("position.left", Integer.toString(jframe
				.getLocation().x));
		properties.setProperty("position.top", Integer.toString(jframe
				.getLocation().y));
		properties.setProperty("position.width", Integer.toString(jframe
				.getSize().width));
		properties.setProperty("position.height", Integer.toString(jframe
				.getSize().height));

		Enumeration en = jframe.getCallerTable().getColumnModel().getColumns();
		int i = 0;
		while (en.hasMoreElements()) {
			int width = ((TableColumn) en.nextElement()).getWidth();
			properties.setProperty("column" + i + ".width", Integer
					.toString(width));
			i++;
		}

		try {
			FileOutputStream fos = new FileOutputStream(JFritz.PROPERTIES_FILE);
			properties.storeToXML(fos, "Properties for " + JFritz.PROGRAM_NAME
					+ " v" + JFritz.PROGRAM_VERSION);
			fos.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Displays balloon info message TODO: Window when no systray_support
	 *
	 * @param msg
	 *            Message to be displayed
	 */
	public static void infoMsg(String msg) {
		switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) {
		case 0: { // No Popup
			break;
		}
		case 1: {
			//MessageDlg msgDialog = new MessageDlg(msg);
			break;
		}
		case 2: {
			trayIcon.displayMessage(JFritz.PROGRAM_NAME, msg,
					TrayIcon.INFO_MESSAGE_TYPE);
			break;
		}
		}
	}

	/**
	 * Display call monitor message
	 *
	 * @param caller
	 *            Caller number
	 * @param called
	 *            Called number
	 */
	public static void callInMsg(String caller, String called) {
		String callerstr = "", calledstr = "";

		Debug.msg("Caller: " + caller);
		Debug.msg("Called: " + called);
		if (!caller.equals("")) {
			Debug.msg("Searchin in local database ...");
			Person callerperson = phonebook.findPerson(new PhoneNumber(caller));
			if (callerperson != null) {
				if (callerperson.getFullname().equals("")) {
					callerstr = caller + "(" + callerperson.getFullname() + ")";
				}
				else {
				callerstr = caller + "(" + callerperson.getFullname() + ")";
				}
				Debug.msg("Found in local database: "
						+ callerstr);
			} else {
				Debug
						.msg("Searchin on dasoertliche.de ...");
				Person person = ReverseLookup
						.lookup(new PhoneNumber(caller));
				if (!person.getFullname().equals("")) {
					callerstr = caller + "(" + person.getFullname() + ")";
					Debug.msg("Found on dasoertliche.de: "
							+ callerstr);
				}
				else {
					callerstr = caller;
				}
			}

		}
		if (!called.equals("")) {
			Debug.msg("Searchin in local database ...");
			Person calledperson = phonebook.findPerson(new PhoneNumber(called));
			if (calledperson != null) {
				if (calledperson.getFullname().equals("")) {
					calledstr = called + "(" + calledperson.getFullname() + ")";
				}
				else {
				calledstr = called + "(" + calledperson.getFullname() + ")";
				}
				Debug.msg("Found in local database: "
						+ calledstr);
			} else {
				Debug
						.msg("Searchin on dasoertliche.de ...");
				Person person = ReverseLookup
						.lookup(new PhoneNumber(called));
				if (!person.getFullname().equals("")) {
					calledstr = called + "(" + person.getFullname() + ")";
					Debug.msg("Found on dasoertliche.de: "
							+ calledstr);
				}
				else {
					calledstr = called;
				}
			}

		}

		switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) {
		case 0: { // No Popup
			break;
		}
		case 1: {
			//CallMessageDlg msgDialog = new CallMessageDlg(callerstr, calledstr);
			break;
		}
		case 2: {
			if (callerstr.equals("")) {
				trayIcon.displayMessage(JFritz.PROGRAM_NAME,
						"Ankommender Telefonanruf\nan " + calledstr + "!",
						TrayIcon.INFO_MESSAGE_TYPE);
			} else {
				trayIcon.displayMessage(JFritz.PROGRAM_NAME,
						"Ankommender Telefonanruf\nvon " + callerstr + "\nan "
								+ calledstr + "!", TrayIcon.INFO_MESSAGE_TYPE);
			}
			break;
		}
		}

		if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds",
				"true"))) {
		}
	}

	/**
	 * Display call monitor message
	 *
	 * @param called
	 *            Called number
	 */
	public static void callOutMsg(String called) {
		String calledstr = "";
		Debug.msg("Called: " + called);

		infoMsg("Ausgehender Telefonanruf\n " + "\nan " + calledstr + "!");
		if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds",
				"true"))) {
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
	public void errorMsg(String msg) {
		Debug.err(msg);
		if (SYSTRAY_SUPPORT) {
			trayIcon.displayMessage(JFritz.PROGRAM_NAME, msg,
					TrayIcon.ERROR_MESSAGE_TYPE);
		}
	}

	/**
	 * @return Returns the callerlist.
	 */
	public final CallerList getCallerlist() {
		return callerlist;
	}

	/**
	 * @return Returns the phonebook.
	 */
	public final PhoneBook getPhonebook() {
		return phonebook;
	}

	/**
	 * @return Returns the jframe.
	 */
	public final JFritzWindow getJframe() {
		return jframe;
	}

	/**
	 * @return Returns the fritzbox devices.
	 */
	public final Vector getDevices() {
		try {
			ssdpthread.join();
		} catch (InterruptedException e) {
		}
		return ssdpthread.getDevices();
	}

	/**
	 * @return Returns an internationalized message.
	 */
	public static String getMessage(String msg) {
		String i18n = "";
		try {
			i18n = messages.getString(msg);
		} catch (MissingResourceException e) {
			Debug.err("Can't find resource string for " + msg);
			i18n = msg;
		}
		return i18n;
	}

	/**
	 *
	 * @param property
	 *            Property to get the value from
	 * @param defaultValue
	 *            Default value to be returned if property does not exist
	 * @return Returns value of a specific property
	 */
	public static String getProperty(String property, String defaultValue) {
		return properties.getProperty(property, defaultValue);
	}

	/**
	 *
	 * @param property
	 *            Property to get the value from
	 * @return Returns value of a specific property
	 */
	public static String getProperty(String property) {
		return getProperty(property, "");
	}

	/**
	 * Sets a property to a specific value
	 *
	 * @param property
	 *            Property to be set
	 * @param value
	 *            Value of property
	 */
	public static void setProperty(String property, String value) {
		properties.setProperty(property, value);
	}

	/**
	 * Removes a property
	 *
	 * @param property
	 *            Property to be removed
	 */
	public static void removeProperty(String property) {
		properties.remove(property);
	}

	public void stopCallMonitor() {
		if (callMonitor != null) {
			callMonitor.stopCallMonitor();
			callMonitor = null;
		}
	}

	public CallMonitor getCallMonitor() {
		return callMonitor;
	}

	public void setCallMonitor(CallMonitor cm) {
		callMonitor = cm;
	}

	public static String runsOn() {
		return HostOS;
	}

}
