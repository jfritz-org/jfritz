/**
 *
 * JFritz
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
 * 		kleinc		Christian Klein <kleinch@users.sourceforge.net>
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
 *
 * BUGS: bitte bei Sourceforge nachschauen und dort auch den Status ändern
 * BUG: die Autoerkennung, ob telefond für Syslog richtig läuft hat ein Sicherheitsloch. Nun kann jede IP auf Port 1011 zugreifen.
 *
 * FeatureRequests: bitte bei Sourceforge nachschauen und dort auch den Status ändern

 * CHANGELOG:
 * - Anrufen aus der Anrufliste heraus (noch nicht getestet)
 * TODO: Checken, ob alle Bibliotheken vorhanden sind
 *
 * JFritz 0.5.4
 * - Beim neuen Anrufmonitor auf # achten.
 * - Callmonitor: Beim Ausführen eines externen Programmes werden %Firstname, %Surname, %Compnay ersetzt.
 * - Beim Beenden von JFritz keine Speicherung von Calls und Phonebook mehr
 * - Bei den Einstellungen die MAC weggenommen
 * - Bugfix: Sonderzeichen bei "Externes Programm starten" werden korrekt gespeichert
 * - Watchdog: Anrufmonitor wird nach dem Ruhezustand neu gestartet
 * - Anrufliste wird per CSV und nicht mehr per Webinterface abgeholt
 * - Unterstützung für Firmware xx.04.03
 *
 * JFritz 0.5.3
 * - Bugfix-Anrufmonitor: Nummern werden internationalisiert
 *
 * JFritz 0.5.2
 * - Parameter -n funktioniert wieder
 * - XML-Dateien angepasst. DTDs werden nicht mehr gespeichert. Kann zu Datenverlust kommen
 * - Kompatibel zur Firmware xx.04.01
 * - FRITZ!Box-Anrufmonitor: Abholen der Anrufliste nach dem Auflegen
 *
 * JFritz 0.5.1
 * - Priorität auf 5 erhöht
 * - Kompatibel zur Firmware xx.03.101
 * - Datenverbindungen werden als solche angezeigt
 * - Outlookimport verbessert
 *
 * JFritz 0.5.0
 * - Neuer Anrufmonitor: FRITZ!Box Anrufmonitor
 * - Kompatibel zur Firmware xx.03.99
 * - Einstelloption für "minimieren statt schließen"
 *
 * JFritz 0.4.7
 * - New Feature: Variable Programmpriorität (1..10)
 * - Neuer Kommandozeilenparameter -p5 --priority=5
 * - Kompatibel zur FRITZ!Box 7170
 * - Anzeige der Gesamtgesprächsdauer in Stunden und Minuten
 * - Bugfix: Manche Spalten ließen sich nicht klein genug machen
 * - Bugfix: Kommandozeilenparameter -c funktionierte nicht mehr
 * - Bugfix: Outlook-Import
 * - Bugfix: RESSOURCES: filter_callbycall, filter_sip
 * - Bugfix: Telefonbuchsortierung
 *
 * JFritz 0.4.6
 * - Reset-Button bei den Filtern deaktiviert alle Filter
 * - Neuer Filter: Kontextmenü bei "Verpasste Anrufe"-Filter
 * - Neuer Filter: Kommentarfilter
 * - Neuer Befehl für "Anrufmonitor - Externes Programm starten": %URLENCODE();
 * - Kompatibel zu FritzBox 5010 und 5012
 * - Automatische Erkennung der Firmware
 * - Bugfix: Danisahne-Mod wird richtig erkannt
 * - Bugfix: Outlook-Import (entfernen von Klammern)
 * - Bugfix: Anzeigefehler beim Start behoben
 * - Bugfix: Sortierfunktion beim Telefonbuch korrigiert
 *
 * JFritz 0.4.5
 * - Unterstützung für FRITZ!Box Firmware .85
 * - Unterstützung für FRITZ!Box Firmware .87
 * - Unterstützung für FRITZ!Box Firmware .88
 * - Spalten sind jetzt frei verschiebbar
 * - Kommentarspalte hinzugefügt
 * - Kommentar- und Anschluß-Spalte können ausgeblendet werden
 * - Suche der FritzBox über UPNP/SSDP abschaltbar
 * - Telefonbuch nun nach allen Spalten sortierbar
 * - Beim Export merkt sich JFritz die Verzeichnisse
 * - Drucken der Anrufliste (und Export nach Excel, RTF, PDF, CSV, ...)
 * - Neue Kommandozeilenoption -n: Schaltet die Tray-Unterstützung aus
 * - Direkter Import von Outlook-Kontakten
 * - Datumsfilter unterstützt nun "Gestern"
 * - Unterstützung für die neue Version des Callmessage-Anrufomitors (http://www.evil-dead.org/traymessage/index.php4)
 * - Bugfix: Firmware konnte beim ersten Start nicht erkannt werden
 * - Bugfix: Spaltenbreite wurde nicht korrekt gespeichert
 * - Bugfix: Falsche SIP-ID bei gelöschten Einträgen
 * - Bugfix: Wenn Kurzwahl unbekannt war, wurde eine falsche Rufnummer angezeigt
 * - Bugfix: Anrufliste wird nur gelöscht, wenn mind. 1 Eintrag abgeholt wurde
 *
 * Internal:
 * - SipProvider-Informationen werden nicht mehr in den
 * 	 jfritz.properties.xml sondern in jfritz.sipprovider.xml
 *   gespeichert.
 * - Zugriff auf SipProvider über jfritz.getSIPProviderTableModel()
 *
 * JFritz 0.4.4
 * - CallByCall information is saved (only 010xy and 0100yy)
 * - Added support for MacOSX Application Menu
 * - Telnet: Timeout handling
 * - Telnet-Callmonitor: support for username, password
 * - Syslog-Callmonitor: syslogd and telefond check configurable
 * - Added Callmessage-Callmonitor. See Thread-Nr. 178199 in IPPF
 * - Wait, when no network reachable (On startup, return of standby, ...)
 * - Added context menu to phonebook and callerlist
 * - New Callfilter: Route, Fixed call, CallByCall
 * - New Datefilter: Right click on date filter button
 * - Display more information in status bar
 * - Export to XML
 * - Export CallByCall to CSV
 * - Phonenumber with wildcard support (PhoneNumber-Type "main")
 * - Start external Program on incoming call (%Number, %Name, %Called)
 * - Bugfix: Syslog-Monitor get Callerlist on Restart
 * - Bugfix: Check for double entries in Callerlist
 * - Bugfix: Reverselookup on call
 *
 * Internal:
 * - VCard Export moved from CallerTable to PhoneBook
 *
 *
 * JFritz 0.4.2
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
 *
 * JFritz 0.4.0
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
 * JFritz 0.3.6
 * - New mobile phone filter feature
 * - Systray support for Linux/Solaris/Windows
 * - Systray ballon messages for Linux/Solaris/Windows
 * - Browser opening on Unix platforms
 * - Bugfix: Call with same timestamp are collected
 *
 * JFritz 0.3.4
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
 * JFritz 0.3.2:
 * - Saves and restores window position/size
 * - Saves and restores width of table columns
 * - CallTypeFilter works now (thanks to robotniko)
 * - Filter option is saved
 * - Added filter for calls without displayed number
 * - Total duration of calls now displayed in status bar
 *
 * JFritz 0.3.0: Major release
 * - Compatibility for JRE 1.4.2
 * - Severel bugfixes
 *
 * JFritz 0.2.8:
 * - Bugfix: Firmware detection had nasty bug
 * - Bugfix: Firmware detection detects modded firmware properly
 * - Bugfix: RegExps adapted for modded firmware
 * - Support for SIP-Provider for fritzbox fon wlan
 * - Notify users whenn calls have been retrieved
 * - CSV Export
 *
 * JFritz 0.2.6:
 * - Several bugfixes
 * - Support for Fritz!Boxes with modified firmware
 * - Improved config dialog
 * - Improved firmware detection
 * - Initial support für SIP-Provider
 * - Firmware/SIP-Provider are saved in config file
 *
 * JFritz 0.2.4:
 * - Several bugfixes
 * - Improventsment on number resolution
 * - Optimized Reverse Lookup
 *
 * JFritz 0.2.2:
 * - FRITZ!Box FON WLAN works again and is detected automatically.
 * - Target MSN is displayed
 * - Bugfixes for Reverse Lookup (Mobile phone numbers are filtered now)
 * - Nice icons for calltypes (Regular call, Area call, Mobile call)
 * - Several small bugfixes
 *
 * JFritz 0.2.0: Major release
 * - Improved GUI, arranged colours for win32 platform
 * - New ToolBar with nice icons
 * - Bugfix: Not all calls had been retrieved from box
 * - Improved reverse lookup
 * - Automatic box detection (does not yet work perfectly)
 * - Internal class restructuring
 *
 * JFritz 0.1.6:
 * - Calls are now saved in XML format
 * - Support for Fritz!Box 7050
 *
 * JFritz 0.1.0:
 * - Initial version
 */

package de.moonflower.jfritz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
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
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;

/**
 * @author Arno Willig
 *
 */
public final class JFritz {

    public final static String PROGRAM_NAME = "JFritz";

    public final static String PROGRAM_VERSION = "0.5.4";

    public final static String PROGRAM_URL = "http://www.jfritz.org/";

    public final static String PROGRAM_SECRET = "jFrItZsEcReT";

    public final static String DOCUMENTATION_URL = "http://www.jfritz.org/hilfe/";

    public final static String CVS_TAG = "$Id: JFritz.java,v 1.161 2006/03/09 14:56:34 kleinch Exp $";

    public final static String PROGRAM_AUTHOR = "Arno Willig <akw@thinkwiki.org>";

    public final static String PROPERTIES_FILE = "jfritz.properties.xml";

    public final static String CALLS_FILE = "jfritz.calls.xml";

    public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml";

    public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml";

    public final static String SIPPROVIDER_FILE = "jfritz.sipprovider.xml";

    public final static String CALLS_CSV_FILE = "calls.csv";

    public final static int SSDP_TIMEOUT = 1000;

    public final static int SSDP_MAX_BOXES = 3;

    public static boolean SYSTRAY_SUPPORT = false;

    public static boolean checkSystray = true;

    private JFritzProperties defaultProperties;

    private static JFritzProperties properties;

    private static ResourceBundle messages;

    private SystemTray systray;

    private JFritzWindow jframe;

    private SSDPdiscoverThread ssdpthread;

    private CallerList callerlist;

    private static TrayIcon trayIcon;

    private static PhoneBook phonebook;

    private static SipProviderTableModel sipprovider;

    private static URL ringSound, callSound;

    private CallMonitor callMonitor = null;

    private static String HostOS = "other";

    public static final int CALLMONITOR_START = 0;

    public static final int CALLMONITOR_STOP = 1;

    private static JFritz jfritz;

    private static WatchdogThread watchdog;

    /**
     * Constructs JFritz object
     */
    public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
            boolean clearList) {
        jfritz = this;
        loadMessages(new Locale("de", "DE"));
        loadProperties();
        loadSounds();

        String osName = System.getProperty("os.name");
        Debug.msg("Betriebssystem: " + osName);
        if (osName.startsWith("Mac OS"))
            HostOS = "Mac";
        else if (osName.startsWith("Windows"))
            HostOS = "Windows";
        else if (osName.equals("Linux")) {
            HostOS = "Linux";
        }
        Debug.msg("JFritz runs on " + HostOS);

        if (HostOS.equalsIgnoreCase("mac")) {
            new MacHandler(this);
        }
        autodetectFirmware();

        phonebook = new PhoneBook(this);
        phonebook.loadFromXMLFile(PHONEBOOK_FILE);

        sipprovider = new SipProviderTableModel();
        sipprovider.loadFromXMLFile(SIPPROVIDER_FILE);

        callerlist = new CallerList(this);
        callerlist.loadFromXMLFile(CALLS_FILE);

        Debug.msg("Start des commandline parsing");
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
                    callerlist.saveToCSVFile(csvFileName, true);
                }
                if (clearList) {
                    Debug.msg("Clearing Caller List");
                    callerlist.clearList();
                }
                Debug.msg("JFritz beendet sich nun.");
                System.exit(0);
            }
        }
        if (csvExport) {
            Debug.msg("CSV-Export to " + csvFileName);
            callerlist.saveToCSVFile(csvFileName, true);
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

        Debug.msg("Neue Instanz von JFrame");
        jframe = new JFritzWindow(this);

        Debug.msg("Checke Systray-Support");
        if (checkForSystraySupport()) {
            try {
                systray = SystemTray.getDefaultSystemTray();
                createTrayMenu();
            } catch (Exception e) {
                Debug.err(e.toString());
                SYSTRAY_SUPPORT = false;
            }
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.useSSDP",
                "true"))) {
            Debug.msg("Suche FritzBox über UPnP / SSDP");

            ssdpthread = new SSDPdiscoverThread(this, SSDP_TIMEOUT);
            ssdpthread.start();
            try {
                ssdpthread.join();
            } catch (InterruptedException ie) {

            }
        }

        jframe.checkStartOptions();

        javax.swing.SwingUtilities.invokeLater(jframe);

        startWatchdog();
    }

    /**
     * Loads sounds from resources
     */
    private void loadSounds() {
        ringSound = getClass().getResource(
                "/de/moonflower/jfritz/resources/sounds/call_in.wav");
        callSound = getClass().getResource(
                "/de/moonflower/jfritz/resources/sounds/call_out.wav");
    }

    /**
     * Checks for systray availability
     */
    private boolean checkForSystraySupport() {
        if (!checkSystray)
            return false;
        String os = System.getProperty("os.name");
        if (os.equals("Linux") || os.equals("Solaris")
                || os.startsWith("Windows")) {
            SYSTRAY_SUPPORT = true;
        }
        return SYSTRAY_SUPPORT;
    }

    /**
     * Main method for starting JFritz
     *
     * @param args
     *            Program arguments (-h -v ...)
     */
    public static void main(String[] args) {
        System.out.println(PROGRAM_NAME + " v" + PROGRAM_VERSION
                + " (c) 2005 by " + PROGRAM_AUTHOR);
        Thread.currentThread().setPriority(5);
        boolean fetchCalls = false;
        boolean clearList = false;
        boolean csvExport = false;
        String csvFileName = "";

        CLIOptions options = new CLIOptions();

        options.addOption('h', "help", null, "This short description");
        options.addOption('v', "verbose", null, "Turn on debug information");
        options.addOption('v', "debug", null, "Turn on debug information");
        options.addOption('s', "systray", null, "Turn on systray support");
        options.addOption('n', "nosystray", null, "Turn off systray support");
        options.addOption('f', "fetch", null, "Fetch new calls and exit");
        options.addOption('c', "clear_list", null,
                "Clears Caller List and exit");
        options.addOption('e', "export", "filename",
                "Fetch calls and export to CSV file.");
        options.addOption('l', "logfile", "filename",
                "Writes debug messages to logfile");
        options.addOption('p', "priority", "level",
                "Set program priority [1..10]");

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
                if (csvFileName == null || csvFileName.equals("")) {
                    System.err.println("Parameter not found!");
                    System.exit(0);
                }
                break;
            case 'c':
                clearList = true;
                break;
            case 'l':
                String logFilename = option.getParameter();
                if (logFilename == null || logFilename.equals("")) {
                    System.err.println("Parameter not found!");
                    System.exit(0);
                } else {
                    Debug.logToFile(logFilename);
                    break;
                }
            case 'n':
                checkSystray = false;
                break;
            case 'p':
                String priority = option.getParameter();
                if (priority == null || priority.equals("")) {
                    System.err.println("Parameter not found!");
                    System.exit(0);
                } else {
                    try {
                        int level = Integer.parseInt(priority);
                        Thread.currentThread().setPriority(level);
                        Debug.msg("Set priority to level " + priority);
                    } catch (NumberFormatException nfe) {
                        System.err
                                .println("Wrong parameter. Only values from 1 to 10 are allowed.");
                        System.exit(0);
                    } catch (IllegalArgumentException iae) {
                        System.err
                                .println("Wrong parameter. Only values from 1 to 10 are allowed.");
                        System.exit(0);
                    }
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

        JPopupMenu menu = new JPopupMenu("JFritz Menu");
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

        trayIcon = new TrayIcon(icon, "JFritz", menu);
        trayIcon.setIconAutoSize(false);
        trayIcon
                .setCaption(JFritz.PROGRAM_NAME + " v" + JFritz.PROGRAM_VERSION);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideShowJFritz();
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
     * Replace old property values with new one p.e. column0.width =>
     * column.type.width
     *
     */
    private void replaceOldProperties() {
        for (int i = 0; i < 10; i++) {
            JFritz.removeProperty("SIP" + i);
        }

        if (properties.contains("column0.width")) {
            properties.setProperty("column." + getMessage("type") + ".width",
                    properties.getProperty("column0.width"));
            JFritz.removeProperty("column0.width");
        } else if (properties.contains("column1.width")) {
            properties.setProperty("column." + getMessage("date") + ".width",
                    properties.getProperty("column1.width"));
            JFritz.removeProperty("column1.width");
        } else if (properties.contains("column2.width")) {
            properties.setProperty("column." + "Call-By-Call" + ".width",
                    properties.getProperty("column2.width"));
            JFritz.removeProperty("column2.width");
        } else if (properties.contains("column3.width")) {
            properties.setProperty("column." + getMessage("number") + ".width",
                    properties.getProperty("column3.width"));
            JFritz.removeProperty("column3.width");
        } else if (properties.contains("column4.width")) {
            properties.setProperty("column." + getMessage("participant")
                    + ".width", properties.getProperty("column4.width"));
            JFritz.removeProperty("column4.width");
        } else if (properties.contains("column5.width")) {
            properties.setProperty("column." + getMessage("port") + ".width",
                    properties.getProperty("column5.width"));
            JFritz.removeProperty("column5.width");
        } else if (properties.contains("column6.width")) {
            properties.setProperty("column." + getMessage("route") + ".width",
                    properties.getProperty("column6.width"));
            JFritz.removeProperty("column6.width");
        } else if (properties.contains("column7.width")) {
            properties.setProperty("column." + getMessage("duration")
                    + ".width", properties.getProperty("column7.width"));
            JFritz.removeProperty("column7.width");
        } else if (properties.contains("column8.width")) {
            properties.setProperty("column." + "Kommentar" + ".width",
                    properties.getProperty("column8.width"));
            JFritz.removeProperty("column8.width");
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

        try {
            properties.loadFromXML(JFritz.PROPERTIES_FILE);
            replaceOldProperties();
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

        removeProperty("state.warningFreeminutesShown"); // don't save
        // warningState

        Debug.msg("Save window position");
        properties.setProperty("position.left", Integer.toString(jframe
                .getLocation().x));
        properties.setProperty("position.top", Integer.toString(jframe
                .getLocation().y));
        properties.setProperty("position.width", Integer.toString(jframe
                .getSize().width));
        properties.setProperty("position.height", Integer.toString(jframe
                .getSize().height));

        Debug.msg("Save column widths");
        Enumeration en = jframe.getCallerTable().getColumnModel().getColumns();
        int i = 0;
        while (en.hasMoreElements()) {
            TableColumn col = (TableColumn) en.nextElement();

            properties.setProperty("column." + col.getHeaderValue().toString()
                    + ".width", Integer.toString(col.getWidth()));
            properties.setProperty("column" + i + ".name", col.getHeaderValue()
                    .toString());
            i++;
        }

        try {
            Debug.msg("Save other properties");
            properties.storeToXML(JFritz.PROPERTIES_FILE);
        } catch (IOException e) {
            Debug.err("Couldn't save Properties");
        }
    }

    /**
     * Displays balloon info message
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
            MessageDlg msgDialog = new MessageDlg();
            msgDialog.showMessage(msg);
            break;
        }
        case 2: {
            if (trayIcon != null)
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
    public void callInMsg(String caller, String called) {
        callInMsg(caller, called, "");
    }

    private String searchNameToPhoneNumber(String caller) {
        String name = "";
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searchin in local database ...");
        Person callerperson = phonebook.findPerson(callerPhoneNumber);
        if (callerperson != null) {
            name = callerperson.getFullname();
            Debug.msg("Found in local database: " + name);
        } else {
            Debug.msg("Searchin on dasoertliche.de ...");
            Person person = ReverseLookup.lookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) {
                name = person.getFullname();
                Debug.msg("Found on dasoertliche.de: " + name);
                Debug.msg("Add person to database");
                phonebook.addEntry(person);
                phonebook.fireTableDataChanged();
            }
        }
        return name;
    }

    private String[] searchFirstAndLastNameToPhoneNumber(String caller) {
        String name[] = new String[3];
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searchin in local database ...");
        Person callerperson = phonebook.findPerson(callerPhoneNumber);
        if (callerperson != null) {
            name[0] = callerperson.getFirstName();
			name[1] = callerperson.getLastName();
			name[2] = callerperson.getCompany();
			Debug.msg("Found on dasoertliche.de: " + name[1] + ", " + name[0]);
        } else {
            Debug.msg("Searchin on dasoertliche.de ...");
            Person person = ReverseLookup.lookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) {
				name[0] = callerperson.getFirstName();
				name[1] = callerperson.getLastName();
				name[2] = callerperson.getCompany();
                Debug.msg("Found on dasoertliche.de: " + name[1] + ", " + name[0]);
                Debug.msg("Add person to database");
                phonebook.addEntry(person);
                phonebook.fireTableDataChanged();
            }
        }
        return name;
    }

    /**
     * Display call monitor message
     *
     * @param caller
     *            Caller number
     * @param called
     *            Called number
     * @param name
     *            Known name (only YAC)
     */
    public void callInMsg(String callerInput, String calledInput, String name) {

        Debug.msg("Caller: " + callerInput);
        Debug.msg("Called: " + calledInput);
        Debug.msg("Name: " + name);

        String callerstr = "", calledstr = "";
		String firstname = "", surname = "", company = "";

        if (!callerInput.startsWith("SIP")) {
            PhoneNumber caller = new PhoneNumber(callerInput);
            if (caller.getIntNumber().equals("")) {
                callerstr = "Unbekannt";
            } else
                callerstr = caller.getIntNumber();
        }

		if (!calledInput.startsWith("SIP")) {
            PhoneNumber called = new PhoneNumber(calledInput);
            if (called.getIntNumber().equals("")) {
                calledstr = "Unbekannt";
            } else
                calledstr = calledInput;
        } else
            calledstr = getSIPProviderTableModel().getSipProvider(calledInput,
                    calledInput);

        if (name.equals("") && !callerstr.equals("Unbekannt")) {
			name = searchNameToPhoneNumber(callerstr);
			String [] nameArray = searchFirstAndLastNameToPhoneNumber(callerstr);
			firstname = nameArray[0];
			surname = nameArray[1];
			company = nameArray[2];
        }
		if (name.equals("")) name = "Unbekannt";
		if (firstname.equals("") && surname.equals("")) surname = "Unbekannt";
		if (company.equals("")) company = "Unbekannt";

		if (callerstr.startsWith("+49")) callerstr = "0" + callerstr.substring(3);

        Debug.msg("Caller: " + callerstr);
        Debug.msg("Called: " + calledstr);
        Debug.msg("Name: " + name);

        switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) {
        case 0: { // No Popup
            break;
        }
        default: {
            String outstring = JFritz.getMessage("incoming_call") + "\nvon "
                    + callerstr;
            if (!name.equals("Unbekannt")) {
                outstring = outstring + " (" + name + ")";
            }

            if (!calledstr.equals("Unbekannt")) {
                outstring = outstring + "\nan " + calledstr;
            }
            infoMsg(outstring);
            break;

        }
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds",
                "true"))) {
            playSound(ringSound);
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.startExternProgram", "false"))) {
            String programString = JFritzUtils.deconvertSpecialChars(JFritz.getProperty("option.externProgram",
                    ""));

            programString = programString.replaceAll("%Number", callerstr);
            programString = programString.replaceAll("%Name", name);
            programString = programString.replaceAll("%Called", calledstr);
			programString = programString.replaceAll("%Firstname", firstname);
			programString = programString.replaceAll("%Surname", surname);
			programString = programString.replaceAll("%Company", company);

            if (programString.indexOf("%URLENCODE") > -1) {
                try {
                    Pattern p;
                    p = Pattern.compile("%URLENCODE\\(([^;]*)\\);");
                    Matcher m = p.matcher(programString);
                    while (m.find()) {
                        String toReplace = m.group();
                        toReplace = toReplace.replaceAll("\\\\", "\\\\\\\\");
                        toReplace = toReplace.replaceAll("\\(", "\\\\(");
                        toReplace = toReplace.replaceAll("\\)", "\\\\)");
                        String toEncode = m.group(1);
                        programString = programString.replaceAll(toReplace,
                                URLEncoder.encode(toEncode, "UTF-8"));
                    }
                } catch (UnsupportedEncodingException uee) {
                    Debug.err("JFritz.class: UnsupportedEncodingException: "
                            + uee.toString());
                }
            }

            if (programString.equals("")) {
                Debug
                        .errDlg("Kein externes Programm angegeben"
                                + programString);
                return;
            }
            Debug.msg("Starte externes Programm: " + programString);
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg("Konnte externes Programm nicht ausführen: "
                        + programString);
                e.printStackTrace();
            }
        }

    }

    /**
     * Display call monitor message
     *
     * @param called
     *            Called number
     */
    public void callOutMsg(String calledInput, String providerInput) {
        Debug.msg("Called: " + calledInput);
        Debug.msg("Provider: " + providerInput);

        String calledstr = "", providerstr = "", name = "";
		String firstname = "", surname = "", company = "";

        if (!calledInput.startsWith("SIP")) {
            PhoneNumber called = new PhoneNumber(calledInput);
            if (called.getIntNumber().equals("")) {
                calledstr = "Unbekannt";
            } else
                calledstr = called.getIntNumber();
        }

        if (!providerInput.startsWith("SIP") && !providerInput.startsWith("Analog")) {
            PhoneNumber provider = new PhoneNumber(providerInput);
            if (provider.getIntNumber().equals("")) {
                providerstr = "Unbekannt";
            } else
                providerstr = providerInput;
        } else if (providerInput.equals("Analog")) {
            providerstr = "Analog";
        } else
            providerstr = getSIPProviderTableModel().getSipProvider(
                    providerInput, providerInput);

        name = searchNameToPhoneNumber(calledstr);
		String [] nameArray = searchFirstAndLastNameToPhoneNumber(calledstr);
		firstname = nameArray[0];
		surname = nameArray[1];
		company = nameArray[2];

		if (name.equals("")) name = "Unbekannt";
		if (firstname.equals("") && surname.equals("")) surname = "Unbekannt";
		if (company.equals("")) company = "Unbekannt";

		if (calledstr.startsWith("+49")) calledstr = "0" + calledstr.substring(3);

        infoMsg("Ausgehender Telefonanruf\n " + "\nan " + calledstr + " ("
                + name + ") " + "über " + providerstr);
        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds",
                "true"))) {
            playSound(callSound);
        }

        if (false && JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.startExternProgram", "false"))) { //z.Z. noch deaktiviert
            String programString = JFritz.getProperty("option.externProgram",
                    "");

            programString = programString.replaceAll("%Number", providerstr);
            programString = programString.replaceAll("%Name", name);
            programString = programString.replaceAll("%Called", calledstr);
			programString = programString.replaceAll("%Firstname", firstname);
			programString = programString.replaceAll("%Surname", surname);
			programString = programString.replaceAll("%Company", company);

            if (programString.indexOf("%URLENCODE") > -1) {
                try {
                    Pattern p;
                    p = Pattern.compile("%URLENCODE\\(([^;]*)\\);");
                    Matcher m = p.matcher(programString);
                    while (m.find()) {
                        String toReplace = m.group();
                        toReplace = toReplace.replaceAll("\\\\", "\\\\\\\\");
                        toReplace = toReplace.replaceAll("\\(", "\\\\(");
                        toReplace = toReplace.replaceAll("\\)", "\\\\)");
                        String toEncode = m.group(1);
                        programString = programString.replaceAll(toReplace,
                                URLEncoder.encode(toEncode, "UTF-8"));
                    }
                } catch (UnsupportedEncodingException uee) {
                    Debug.err("JFritz.class: UnsupportedEncodingException: "
                            + uee.toString());
                }
            }

            if (programString.equals("")) {
                Debug
                        .errDlg("Kein externes Programm angegeben"
                                + programString);
                return;
            }
            Debug.msg("Starte externes Programm: " + programString);
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg("Konnte externes Programm nicht ausführen: "
                        + programString);
                e.printStackTrace();
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
        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.useSSDP",
                "true"))) {
            try {
                ssdpthread.join();
            } catch (InterruptedException e) {
            }
            return ssdpthread.getDevices();
        } else
            return null;
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
            // Let buttons enable start of callMonitor
            getJframe().setCallMonitorButtons(CALLMONITOR_START);
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

    public void hideShowJFritz() {
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

    public SipProviderTableModel getSIPProviderTableModel() {
        return sipprovider;
    }

    private void autodetectFirmware() {
        FritzBoxFirmware firmware;
        try {
            firmware = FritzBoxFirmware.detectFirmwareVersion(JFritz
                    .getProperty("box.address", "192.168.178.1"), Encryption
                    .decrypt(JFritz.getProperty("box.password", Encryption
                            .encrypt(""))));

        } catch (WrongPasswordException e1) {
            Debug.err("Password wrong!");
            firmware = null;
        } catch (IOException e1) {
            Debug.err("Address wrong!");
            firmware = null;
        }
        if (firmware != null) {
            Debug.msg("Found FritzBox-Firmware: "
                    + firmware.getFirmwareVersion());
            JFritz.setProperty("box.firmware", firmware.getFirmwareVersion());
        } else {
            Debug.msg("Found no FritzBox-Firmware");
            JFritz.removeProperty("box.firmware");
        }
    }

    /**
     * start timer for watchdog
     *
     */
    private void startWatchdog() {
            Timer timer = new Timer();
            watchdog = new WatchdogThread(jfritz, 1);
            timer.schedule(new TimerTask() {
                        public void run() {
                            watchdog.run();
                        }
                    }, 5000, 1*60000);
            Debug.msg("Watchdog enabled");
    }
}