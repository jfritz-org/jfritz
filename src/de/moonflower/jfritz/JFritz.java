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
 *      little_ben  Benjamin Schmitt <little_ben@users.sourceforge.net>
 *      baefer		Bastian Schaefer <baefer@users.sourceforge.net>
 *      capncrunch	Brian Jensen <capncrunch@users.sourceforge.net>
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
 * BUGS: http://sourceforge.net/tracker/?group_id=138196&atid=741413
 * BUG: die Autoerkennung, ob telefond für Syslog richtig läuft hat ein Sicherheitsloch. Nun kann jede IP auf Port 1011 zugreifen.
 *
 * FeatureRequests: bitte bei Sourceforge nachschauen und dort auch den Status ändern
 * FeatureRequests: http://sourceforge.net/tracker/?func=browse&group_id=138196&atid=741416

 * CHANGELOG:
 *
 * (TODO: Checken, ob alle Bibliotheken vorhanden sind)
 * (TODO: Neue Kurzwahlen von der Beta-FW ins Telefonbuch aufnehmen)
 * (TODO: Import der Anrufliste im XML-Format beim Kontextmenü einbauen)
 * TODO: Einstelloption, wie lange die Popups geöffnet bleiben sollen
 * TODO: Filter für Ports
 * TODO: Internationalisierung abschließen
 * TODO: Language-Files checken, ob tatsächlich alle Werte benötigt werden
 * TODO: Sonderzeichen werden in den Balloontips unter Windows nicht korrekt angezeigt. Scheint ein Windowsproblem zu sein. Lösung/Workaround noch nicht gefunden.
 *
 * JFritz 0.6.1
 * - Neue Strings:
 *		delete_duplicate_phonebook_entries
 *		delete_duplicate_phonebook_entries_confirm_msg
 *		delete_duplicate_phonebook_entries_inform_msg
 * - Neu: Konfigurationswizard für Erstbenutzer
 * - Neu: Logfiles werden jetzt mittels Stream redirection geschrieben (heißt auch die Exceptions werden in den Logfiles aufgenommen :) )
 * - Neu: Entfernen doppelter Einträge beim Telefonbuch
 * - Neu: Automatisches Scrollen zum selektierten Telefonbucheintrag
 * - Neu: Englische Firmware wird unterstützt
 * - Intern: Firmware wird beim Start erkannt und in JFritz.firmware gespeichert. Zugriff nicht mehr über JFritz.getProperties("box.firmware") sondern über JFritz.getFirmware()
 * - Bugfix: Kurzwahlen werden weider korrekt abgeholt
 * - Bugfix: Standardtelefonnummern können wieder geändert werden
 * - Bugfix: Problem mit dem Holen der Anrufliste behoben
 * - Bugfix: Nebenstellenbezeichnungen und Route können jetzt Sonderzeichen enthalten
 * - Bugfix: Anzeige eines analogen Anrufs beim Anrufmonitor
 * - Bugfix: PersonDialog ("Person editieren") wird nun mittig zum JFritz.JFrame angezeigt - SF.net-Request:[1503523] Adress-/Telefonbuch
 * - Neu: Default- und Close-Button für PersonDialog ("Person editieren")
 *
 * JFritz 0.6.0
 * - Neue Strings:
 * - Neuer Kommandozeilenparameter: -w, deaktiviert die Kontrolle von mehrfachen Instanzen
 * - Bugfix: Alle internationalen Gespräche werden jetzt erkannt.
 * - Neu: Sprache einstellbar ( <- Wahlhilfe im Telefonbuch funktioniert bei englischer Sprache nicht (Bastian)
 * 								<- TrayMenu angepasst (Benjamin)
 * 								<- komplett geändert, Sprachfiles werden jetzt dynamisch erkannt und können in den Einstellungen ausgewählt werden. (Bastian))
 * - Bugfix: Spracheinstellungen werden gespeichert.
 * - Neu: Verbesserte Anzeige des aus- und eingehenden Verbindungstyps bei verwendung des JFritz-Anrufmonitors im Format "interne MSN (Leitungsart)", z.B. "1234 (ISDN)" oder "1234 (SIP)" bei eingehenden Anrufen oder "56789 (88sdg4@dus.net)" bei ausgehenden
 * - Bugfix: Anrufmonitor zeigt ausgehende und eingehende Anrufe im gleichen Format an
 * - Bugfix: Neues JFritz-Anrufmonitor-Format besser unterstützt, jetzt wieder Anzeige von angerufener MSN
 * - Bugfix: MAC-Handling funktioniert wieder
 * - Bugfix: Wahlhilfe im Telefonbuch funktioniert jetzt bei englischer Sprache (Brian)
 * - Bugfix: Beim Metal-LAF werden jetzt immer die Metal-Decorations verwendet.
 * - Bugfix: Beim Ändern des Look And Feel's werden die Buttons korrekt dargestellt.
 * - Neu: Fritzbox Anrufliste als CSV-Datei importieren
 * - Neu: Thunderbird/Mozilla-Kontakte importieren
 * - Neu: Telefonbuch als CSV-Datei exportieren
 * - Neu: Anruferliste importieren (CSV-Dateien)
 * - Neu: Wahlhilfe (<- funktioniert nicht richtig. Es wird immer der Port vom letzten Versuch benutzt
 *                      Beispiel: ich habe zuletzt ISDN 1 benutzt, will jetzt mit ISDN 2 anrufen, dann wird aber ISDN 1 benutzt.
 *                      Benutze ich dann die Wahlhilfe erneut, wird ISDN 2 benutzt - egal welchen Port ich einstelle. D.h., benutze
 *                      ich ständig die gleichen Ports, fällt es nicht weiter auf.
 *                      Ich denke, das hängt damit zusammen, dass man auf der Weboberfläche erst den Port auswählt, dann übernehmen
 *                      drückt und dann erst die Nummer anklickt. Diesen Vorgang müsste man in JFritz nachbilden.  (KCh)
 *                   <- Eigentlich sollte es auch mit einem direkten URL-Aufruf funktionieren. Machen andere Tools genau so. (Robert)
 *                   <- Kann denn keiner das Verhalten meiner Box nachvollziehen? Ist das evtl. ISDN-spezifisch? Ich kanns es 100%ig reproduzieren (KCh))
 * - Neu: (JFritz)Telefonbuch importieren (XML)
 * - Neu: Manuelle Backups erstellen (Menü und Toolbar)
 * - Neu: per Funktionstaste "F5" Anrufliste aktualisieren
 * - Neu: Suchfunktion für Telefonbuch
 * - Neue Option: Nach Standby oder Ruhezustand die Anrufliste automatisch abholen
 * - Neue Option: Sicherungskopien bei jedem Laden der Anruferliste erstellen
 * - Änderung: Das Durchsuchen der Anruferliste muss nun per [ENTER] gestartet werden.
 * - Bugfix: "Übernehmen" Button im Telefonbuch wird nun anklickbar, wenn man eine Telefonnummer geändert hat.
 * - Bugfix: Sonderzeichen bei "Externes Programm starten" werden korrekt gespeichert
 * - Bugfix: tritt der unwahrscheinliche Fall auf, dass kein Tray-Icon angezeigt wird, der User aber früher einmal
 * 			 (als das Tray-Icon noch verfügbar war) Tray-Messages zu Benachrichtigung ausgewählt hatte, wurde gar kein
 * 			 Anruf mehr signalisiert. Jetzt wird in diesem Fall auf ein PopUp zurückgegriffen.
 * - Bugfix: unvollständige Anzeige des Einstellungsdialoges -> Weiteres
 * - Bugfix: Speicherung der Kommentare
 * - Bugfix: Überschreiben der Rufnummer im Telefonbuch tritt nicht mehr auf
 * - INTERN: Bereitstellen von utils.JFritzClipboard und CallerList.getSelectedCall
 * - INTERN: JDIC-Update auf "JDIC 20050930 Build"
 *
 * JFritz 0.5.5
 * - Nummer und Anschrift können aus der Anrufliste heraus in die Zwischenablage kopiert werden
 * - Schutz vor mehrfachem Programmstart (<- was ist mit Kommandozeilenstart?, =>BS: werden berücksichtigt - enableInstanceControl=false)
 * - Löschfunktionalität für Anrufliste der FRITZ!Box (Menü und Toolbar)
 * - Bugfix: Start auch bei fehlendem Tray
 * - Bugfix: Anrufmonitor arbeitete bei einem Reverselookup einer nicht im Telefonbuch
 *           eingetragenen Person nicht mehr
 * - Bugfix: Eintragen einer über Reverse-Lookup gefundenen Person korrigiert
 * - Neuer Kommandozeilenparameter: -d, --delete_on_box, löscht Anrufliste auf der Box und beendet sich dann (kein GUI)
 * - Neuer Kommandozeilenparameter: -b, --backup, erstellt eine Sicherungskopie von allen XML-Dateien
 * - Neue Option: Sicherungskopien beim Start erstellen
 * - Bugfix: Bei der Suche nach einer Rufnummer werden vor der Zentrale ggf. vorhandene Durchwahlnummern berücksichtigt
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
 * - Display more information in status barm Zielfon hör ich "Ihre Nummerwird gehalten...". Bitte Einbauen!! Das ist der Hammer!

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
import javax.swing.UIManager;
import javax.swing.table.TableColumn;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.dialogs.configwizard.ConfigWizard;
import de.moonflower.jfritz.dialogs.phonebook.PhoneBook;
import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.CLIOption;
import de.moonflower.jfritz.utils.CLIOptions;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.utils.network.CallMonitor;
import de.moonflower.jfritz.utils.network.SSDPdiscoverThread;

/**
 * @author Arno Willig
 *
 */
public final class JFritz {

	//when changing this, don't forget to check the resource bundles!!
	public final static String PROGRAM_NAME = "JFritz"; //$NON-NLS-1$

    public final static String PROGRAM_VERSION = "0.6.1"; //$NON-NLS-1$

    public final static String PROGRAM_URL = "http://www.jfritz.org/"; //$NON-NLS-1$

    public final static String PROGRAM_SECRET = "jFrItZsEcReT"; //$NON-NLS-1$

    public final static String DOCUMENTATION_URL = "http://www.jfritz.org/hilfe/"; //$NON-NLS-1$

    public final static String CVS_TAG = "$Id: JFritz.java,v 1.260 2006/06/15 20:11:19 little_ben Exp $"; //$NON-NLS-1$

    public final static String PROGRAM_AUTHOR = "Arno Willig <akw@thinkwiki.org>"; //$NON-NLS-1$

    public final static String PROPERTIES_FILE = "jfritz.properties.xml"; //$NON-NLS-1$

    public final static String CALLS_FILE = "jfritz.calls.xml"; //$NON-NLS-1$

    public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml"; //$NON-NLS-1$

    public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml"; //$NON-NLS-1$

    public final static String SIPPROVIDER_FILE = "jfritz.sipprovider.xml"; //$NON-NLS-1$

    public final static String CALLS_CSV_FILE = "calls.csv"; //$NON-NLS-1$

    public final static String PHONEBOOK_CSV_FILE = "contacts.csv"; //$NON-NLS-1$

    public final static int SSDP_TIMEOUT = 1000;

    public final static int SSDP_MAX_BOXES = 3;

    public static boolean SYSTRAY_SUPPORT = false;

    public static boolean checkSystray = true;

    private JFritzProperties defaultProperties;

    private static JFritzProperties properties;

    private static ResourceBundle localeMeanings;

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

    private static String HostOS = "other"; //$NON-NLS-1$

    public static final int CALLMONITOR_START = 0;

    public static final int CALLMONITOR_STOP = 1;

    private static JFritz jfritz;

    private static WatchdogThread watchdog;

    private static boolean isRunning = false;

    private static Locale locale;

    private static FritzBoxFirmware firmware = null;

    /**
     * Main method for starting JFritz
     *
     * LAST MODIFIED: Brian Jensen 04.06.06
     * added option to disable mulitple instance control
     * added a new parameter switch: -w
     *
     * @param args
     *            Program arguments (-h -v ...)
     *
     */
    public static void main(String[] args) {
        System.out.println(PROGRAM_NAME + " v" + PROGRAM_VERSION //$NON-NLS-1$
                + " (c) 2005 by " + PROGRAM_AUTHOR); //$NON-NLS-1$
        Thread.currentThread().setPriority(5);
        boolean fetchCalls = false;
        boolean clearList = false;
        boolean csvExport = false;
		boolean foreign = false;
        String csvFileName = ""; //$NON-NLS-1$
        boolean enableInstanceControl = true;

        //TODO: If we ever make different packages for different languages
        //change the default language here
        locale = new Locale("de", "DE"); //$NON-NLS-1$,  //$NON-NLS-2$
        CLIOptions options = new CLIOptions();

        options.addOption('h', "help", null, "This short description"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('v', "verbose", null, "Turn on debug information"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('s', "systray", null, "Turn on systray support"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('n', "nosystray", null, "Turn off systray support"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('f', "fetch", null, "Fetch new calls and exit"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		options.addOption('d', "delete_on_box", null, //$NON-NLS-1$,  //$NON-NLS-2$
				"Delete callerlist of the Fritz!Box."); //$NON-NLS-1$
        options.addOption('b', "backup", null, "Creates a backup of all xml-Files in the directory 'backup'"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('c', "clear_list", null, //$NON-NLS-1$,  //$NON-NLS-2$
                "Clears Caller List and exit"); //$NON-NLS-1$
        options.addOption('e', "export", "filename",  //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                "Fetch calls and export to CSV file."); //$NON-NLS-1$
        options.addOption('z', "exportForeign", null, //$NON-NLS-1$,  //$NON-NLS-2$
				"Write phonebooks compatible to BIT FBF Dialer and some other callmonitors."); //$NON-NLS-1$
        options.addOption('l', "logfile", "filename", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                "Writes debug messages to logfile"); //$NON-NLS-1$,
        options.addOption('p', "priority", "level", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                "Set program priority [1..10]"); //$NON-NLS-1$
        options.addOption('w', "without-control", null, //$NON-NLS-1$,  //$NON-NLS-2$
        		"Turns off multiple instance control. DON'T USE, unless you know what your are doing"); //$NON-NLS-1$

        Vector foundOptions = options.parseOptions(args);
        Enumeration en = foundOptions.elements();
        while (en.hasMoreElements()) {
            CLIOption option = (CLIOption) en.nextElement();

            switch (option.getShortOption()) {
            case 'h': //$NON-NLS-1$
                System.out.println("Usage: java -jar jfritz.jar [Options]"); //$NON-NLS-1$
                options.printOptions();
                System.exit(0);
                break;
            case 'v': //$NON-NLS-1$
                Debug.on();
                break;
            case 'b': //$NON-NLS-1$
                doBackup();
                break;
            case 's': //$NON-NLS-1$
                JFritz.SYSTRAY_SUPPORT = true;
                break;
            case 'z':
            	foreign = true;
                break;
            case 'f':
            	enableInstanceControl = false;
            	fetchCalls = true;
                break;
            case 'e':
            	enableInstanceControl = false;
            	csvExport = true;
                csvFileName = option.getParameter();
                if (csvFileName == null || csvFileName.equals("")) { //$NON-NLS-1$
                    System.err.println(JFritz.getMessage("parameter_not_found")); //$NON-NLS-1$
                    System.exit(0);
                }
                break;
            case 'd': //$NON-NLS-1$
				// enableInstanceControl = false; // ungütig, GUI wird nicht gestartet
				Debug.on();
				clearCallsOnBox();
                System.exit(0);
                break;
            case 'c': //$NON-NLS-1$
            	enableInstanceControl = false;
            	clearList = true;
                break;
            case 'l': //$NON-NLS-1$
                String logFilename = option.getParameter();
                if (logFilename == null || logFilename.equals("")) { //$NON-NLS-1$
                    System.err.println(JFritz.getMessage("parameter_not_found")); //$NON-NLS-1$
                    System.exit(0);
                } else {
                    Debug.logToFile(logFilename);
                    break;
                }
            case 'n': //$NON-NLS-1$
                checkSystray = false;
                break;
            case 'w': //$NON-NLS-1$
            	enableInstanceControl = false;
            	System.err.println("Turning off Multiple instance control!"); //$NON-NLS-1$
            	System.err.println("You were warned! Data loss may occur."); //$NON-NLS-1$
            	break;

            case 'p': //$NON-NLS-1$
                String priority = option.getParameter();
                if (priority == null || priority.equals("")) { //$NON-NLS-1$
                    System.err.println(JFritz.getMessage("parameter_not_found")); //$NON-NLS-1$
                    System.exit(0);
                } else {
                    try {
                        int level = Integer.parseInt(priority);
                        Thread.currentThread().setPriority(level);
                        Debug.msg("Set priority to level " + priority); //$NON-NLS-1$
                    } catch (NumberFormatException nfe) {
                        System.err
                                .println(JFritz.getMessage("parameter_wrong_priority")); //$NON-NLS-1$
                        System.exit(0);
                    } catch (IllegalArgumentException iae) {
                        System.err
                                .println(JFritz.getMessage("parameter_wrong_priority")); //$NON-NLS-1$
                        System.exit(0);
                    }
                    break;
                }
            default:
                break;
            }
        }

        new JFritz(fetchCalls, csvExport, csvFileName, clearList, enableInstanceControl, foreign);

    }

    /**
     * Constructs JFritz object
     */
    public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
            boolean clearList, boolean enableInstanceControl) {
		this(fetchCalls,csvExport,csvFileName,clearList,enableInstanceControl,false);
    }


    /**
     * Constructs JFritz object
     * @author Benjamin Schmitt
     */
    public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
            boolean clearList){
    	this(fetchCalls,csvExport,csvFileName,clearList,true,false);
    }

    /**
     * Constructs JFritz object
     */
    public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
            boolean clearList, boolean enableInstanceControl, boolean writeForeignFormats) {
        jfritz = this;
        loadProperties();
        loadMessages(new Locale(JFritz.getProperty("locale","de_DE"))); //$NON-NLS-1$,  //$NON-NLS-2$
        loadLocaleMeanings(new Locale("int","INT"));

        if (JFritzUtils.parseBoolean(properties.getProperty("option.createBackup", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            doBackup();
        }

        if (enableInstanceControl)
        {
	        //check isRunning and exit or set lock
	        isRunning=(properties.getProperty("jfritz.isRunning","false").equals("true")?true:false); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
	        if (!isRunning)
	        {
	        	Debug.msg("Multiple instance lock: set lock."); //$NON-NLS-1$
	        	properties.setProperty("jfritz.isRunning","true"); //$NON-NLS-1$,  //$NON-NLS-2$
	        }
	        else
	        {
	        	Debug.msg("Multiple instance lock: Another instance is already running."); //$NON-NLS-1$
	        	int answer=JOptionPane.showConfirmDialog(null,
	        			JFritz.getMessage("lock_error_dialog1") //$NON-NLS-1$
	        			+JFritz.getMessage("lock_error_dialog2") //$NON-NLS-1$
	        			+JFritz.getMessage("lock_error_dialog3") //$NON-NLS-1$
	        			+JFritz.getMessage("lock_error_dialog4"), //$NON-NLS-1$
	        			JFritz.getMessage("information"),JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
	        	if (answer==JOptionPane.YES_OPTION)
	        	{
	            	Debug.msg("Multiple instance lock: User decided to shut down this instance.");  //$NON-NLS-1$
	        		System.exit(0);
	        	}
	        	else
	        	{
	        		Debug.msg("Multiple instance lock: User decided NOT to shut down this instance."); //$NON-NLS-1$
	        	}
	        }

	        //saveProperties cannot used here because jframe (and its dimensions) is not yet initilized.
	        try {
	            Debug.msg("Save other properties"); //$NON-NLS-1$
	            properties.storeToXML(JFritz.PROPERTIES_FILE);
	        } catch (IOException e) {
	            Debug.err("Couldn't save Properties"); //$NON-NLS-1$
	        }
        }
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
        autodetectFirmware();

        phonebook = new PhoneBook(this);
        phonebook.loadFromXMLFile(PHONEBOOK_FILE);

        sipprovider = new SipProviderTableModel();
        sipprovider.loadFromXMLFile(SIPPROVIDER_FILE);

        callerlist = new CallerList(this);
        callerlist.loadFromXMLFile(CALLS_FILE);

        Debug.msg("Start commandline parsing"); //$NON-NLS-1$
        if (fetchCalls) {
            Debug.msg("Fetch caller list ..."); //$NON-NLS-1$
            try {
                callerlist.getNewCalls();
            } catch (WrongPasswordException e) {
                Debug.err(e.toString());
            } catch (IOException e) {
                Debug.err(e.toString());
            } finally {
                if (csvExport) {
                    Debug.msg("Exporting Call list (csv) to " + csvFileName); //$NON-NLS-1$
                    callerlist.saveToCSVFile(csvFileName, true);
                }
                if (clearList) {
                    Debug.msg("Clearing Call List"); //$NON-NLS-1$
                    callerlist.clearList();
                }
                Debug.msg("JFritz will now terminate"); //$NON-NLS-1$
                System.exit(0);
            }
        }
        if (csvExport) {
            Debug.msg("Exporting Call list (csv) to " + csvFileName); //$NON-NLS-1$
            callerlist.saveToCSVFile(csvFileName, true);
            if (clearList) {
                Debug.msg("Clearing Call List"); //$NON-NLS-1$
                callerlist.clearList();
            }
            System.exit(0);
        }
        if (clearList) {
            Debug.msg("Clearing Call List"); //$NON-NLS-1$
            callerlist.clearList();
            System.exit(0);
        }
		if (writeForeignFormats) {
			phonebook.saveToBITFBFDialerFormat("bitbook.dat"); //$NON-NLS-1$
			phonebook.saveToCallMonitorFormat("CallMonitor.adr"); //$NON-NLS-1$
		}



      if(JFritz.getProperty("lookandfeel",UIManager.getSystemLookAndFeelClassName()).endsWith("MetalLookAndFeel")){ //$NON-NLS-1$,  //$NON-NLS-2$
    	  JFrame.setDefaultLookAndFeelDecorated(true);
      }

      Debug.msg("New instance of JFrame"); //$NON-NLS-1$
      jframe = new JFritzWindow(this);

        if (checkForSystraySupport()) {
            Debug.msg("Check Systray-Support"); //$NON-NLS-1$
            try {
                systray = SystemTray.getDefaultSystemTray();
                createTrayMenu();
            } catch (UnsatisfiedLinkError ule) {
                Debug.err(ule.toString());
                SYSTRAY_SUPPORT = false;
            } catch (Exception e) {
                Debug.err(e.toString());
                SYSTRAY_SUPPORT = false;
            }
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.useSSDP",//$NON-NLS-1$
                "true"))) {//$NON-NLS-1$
            Debug.msg("Searching for  FritzBox per UPnP / SSDP");//$NON-NLS-1$

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
     * Loads resource messages
     *
     * @param locale
     */
    private void loadMessages(Locale locale) {
        try {
            messages = ResourceBundle.getBundle(
                    "jfritz", locale);//$NON-NLS-1$
        } catch (MissingResourceException e) {
            Debug.err("Can't find i18n resource! (\"jfritz_"+locale+".properties\")");//$NON-NLS-1$
            JOptionPane.showMessageDialog(null, JFritz.PROGRAM_NAME + " v"//$NON-NLS-1$
                    + JFritz.PROGRAM_VERSION
                    + "\n\nCannot find the language file \"jfritz_"+locale+".properties\"!"
                    + "\nProgram will exit!");//$NON-NLS-1$
            System.exit(0);
        }
    }

    /**
     * Loads locale meanings
     *
     * @param locale
     */
    private void loadLocaleMeanings(Locale locale) {
        try {
            localeMeanings = ResourceBundle.getBundle(
                    "languages", locale);//$NON-NLS-1$
        } catch (MissingResourceException e) {
            Debug.err("Can't find locale Meanings resource!");//$NON-NLS-1$
        }
    }


    /**
     * Loads properties from xml files
     */
    public void loadProperties() {
        defaultProperties = new JFritzProperties();
        properties = new JFritzProperties(defaultProperties);

        // Default properties
        defaultProperties.setProperty("box.address", "192.168.178.1");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("box.password", Encryption.encrypt(""));//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("country.prefix", "00");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("area.prefix", "0");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("country.code", "49");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("area.code", "441");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("fetch.timer", "5");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("jfritz.isRunning", "false");//$NON-NLS-1$, //$NON-NLS-2$

        try {
            properties.loadFromXML(JFritz.PROPERTIES_FILE);
            replaceOldProperties();
        } catch (FileNotFoundException e) {
            Debug.err("File " + JFritz.PROPERTIES_FILE //$NON-NLS-1$
                    + " not found, using default values"); //$NON-NLS-1$
        } catch (Exception e) {
        }
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
     * Checks for systray availability
     */
    private boolean checkForSystraySupport() {
        if (!checkSystray)
            return false;
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        if (os.equals("Linux") || os.equals("Solaris") //$NON-NLS-1$,  //$NON-NLS-2$
                || os.startsWith("Windows")) { //$NON-NLS-1$
            SYSTRAY_SUPPORT = true;
        }
        return SYSTRAY_SUPPORT;
    }

    private void autodetectFirmware() {
        try {
            firmware = FritzBoxFirmware.detectFirmwareVersion(JFritz
                    .getProperty("box.address", "192.168.178.1"), Encryption //$NON-NLS-1$,  //$NON-NLS-2$
                    .decrypt(JFritz.getProperty("box.password", Encryption //$NON-NLS-1$
                            .encrypt("")))); //$NON-NLS-1$
        } catch (WrongPasswordException e1) {
            Debug.err("Wrong Password!"); //$NON-NLS-1$
            firmware = null;
        } catch (IOException e1) {
            Debug.err("Address wrong!"); //$NON-NLS-1$
            firmware = null;
        } catch (InvalidFirmwareException ife) {
        	Debug.err("Invalid firmware");
        	firmware = null;
        }
        if (firmware != null) {
            Debug.msg("Found FritzBox-Firmware: " //$NON-NLS-1$
                    + firmware.getFirmwareVersion());
        } else {
            Debug.msg("Found no FritzBox-Firmware"); //$NON-NLS-1$
        }
    }

    /**
     * Creates the tray icon menu
     */
    private void createTrayMenu() {
        System.setProperty("javax.swing.adjustPopupLocationToFit", "false"); //$NON-NLS-1$,  //$NON-NLS-2$

        JPopupMenu menu = new JPopupMenu("JFritz Menu"); //$NON-NLS-1$
        JMenuItem menuItem = new JMenuItem(PROGRAM_NAME + " v" //$NON-NLS-1$
                + PROGRAM_VERSION);
        menuItem.setActionCommand("showhide");
        menuItem.addActionListener(jframe);
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem(getMessage("fetchlist")); //$NON-NLS-1$
        menuItem.setActionCommand("fetchList"); //$NON-NLS-1$
        menuItem.addActionListener(jframe);
        menu.add(menuItem);
        menuItem = new JMenuItem(getMessage("reverse_lookup")); //$NON-NLS-1$
        menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
        menuItem.addActionListener(jframe);
        menu.add(menuItem);
        menuItem = new JMenuItem(getMessage("config")); //$NON-NLS-1$
        menuItem.setActionCommand("config"); //$NON-NLS-1$
        menuItem.addActionListener(jframe);
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem(getMessage("prog_exit")); //$NON-NLS-1$
        menuItem.setActionCommand("exit"); //$NON-NLS-1$
        menuItem.addActionListener(jframe);
        menu.add(menuItem);

        ImageIcon icon = new ImageIcon(
                JFritz.class
                        .getResource("/de/moonflower/jfritz/resources/images/trayicon.png")); //$NON-NLS-1$

        trayIcon = new TrayIcon(icon, JFritz.PROGRAM_NAME , menu); //$NON-NLS-1$
        trayIcon.setIconAutoSize(false);
        trayIcon
                .setCaption(JFritz.PROGRAM_NAME + " v" + JFritz.PROGRAM_VERSION); //$NON-NLS-1$
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideShowJFritz();
            }
        });
        systray.addTrayIcon(trayIcon);
    }

    /**
     * Replace old property values with new one p.e. column0.width =>
     * column.type.width
     *
     */
    private void replaceOldProperties() {
        for (int i = 0; i < 10; i++) {
            JFritz.removeProperty("SIP" + i); //$NON-NLS-1$
        }

        if (properties.containsKey("column.Typ.width")) { //$NON-NLS-1$
            properties.setProperty("column.type.width", //$NON-NLS-1$
                    properties.getProperty("column.Typ.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Typ.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Zeitpunkt.width")) { //$NON-NLS-1$
            properties.setProperty("column.date.width", //$NON-NLS-1$
                    properties.getProperty("column.Zeitpunkt.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Zeitpunkt.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Call-By-Call.width")) { //$NON-NLS-1$
            properties.setProperty("column.callbycall.width", //$NON-NLS-1$
                    properties.getProperty("column.Call-By-Call.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Call-By-Call.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Rufnummer.width")) { //$NON-NLS-1$
            properties.setProperty("column.number.width", //$NON-NLS-1$
                    properties.getProperty("column.Rufnummer.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Rufnummer.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Teilnehmer.width")) { //$NON-NLS-1$
            properties.setProperty("column.participant.width",  //$NON-NLS-1$
            		properties.getProperty("column.Teilnehmer.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Teilnehmer.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Anschluß.width")) { //$NON-NLS-1$
            properties.setProperty("column.port.width", //$NON-NLS-1$
                    properties.getProperty("column.Anschluß.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Anschluß.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.MSN.width")) { //$NON-NLS-1$
            properties.setProperty("column.route.width", //$NON-NLS-1$
                    properties.getProperty("column.MSN.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.MSN.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Dauer.width")) { //$NON-NLS-1$
            properties.setProperty("column.duration.width",  //$NON-NLS-1$
            		properties.getProperty("column.Dauer.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Dauer.width"); //$NON-NLS-1$
        }
        if (properties.containsKey("column.Kommentar.width")) { //$NON-NLS-1$
            properties.setProperty("column.comment.width", //$NON-NLS-1$
                    properties.getProperty("column.Kommentar.width")); //$NON-NLS-1$
            JFritz.removeProperty("column.Kommentar.width"); //$NON-NLS-1$
        }
    }



    /**
     *
     */
    public static void clearCallsOnBox() {
		Debug.msg("Clearing callerlist on box."); //$NON-NLS-1$
		properties = new JFritzProperties();
        try {
			properties.loadFromXML(JFritz.PROPERTIES_FILE);
        } catch (FileNotFoundException e) {
            Debug.err("File " + JFritz.PROPERTIES_FILE //$NON-NLS-1$
                    + " not found, using default values"); //$NON-NLS-1$
        } catch (Exception e) {
			Debug.err("Exception: " + e.toString()); //$NON-NLS-1$
        }
		try {
			JFritzUtils.clearListOnFritzBox(properties.getProperty("box.address"), Encryption.decrypt(properties.getProperty("box.password")), firmware);  //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			Debug.msg("Clearing done"); //$NON-NLS-1$
		} catch (WrongPasswordException e) {
			Debug.err("Wrong password, can not delete callerlist on Box."); //$NON-NLS-1$
		}
		catch (IOException e) {
			Debug.err("IOException while deleting callerlist on box (wrong IP-address?)."); //$NON-NLS-1$
		}
    }

    /**
     * Saves properties to xml files
     */
    public void saveProperties() {

        Debug.msg("Save window position"); //$NON-NLS-1$
        properties.setProperty("position.left", Integer.toString(jframe //$NON-NLS-1$
                .getLocation().x));
        properties.setProperty("position.top", Integer.toString(jframe //$NON-NLS-1$
                .getLocation().y));
        properties.setProperty("position.width", Integer.toString(jframe //$NON-NLS-1$
                .getSize().width));
        properties.setProperty("position.height", Integer.toString(jframe //$NON-NLS-1$
                .getSize().height));

        Debug.msg("Save column widths"); //$NON-NLS-1$
        Enumeration en = jframe.getCallerTable().getColumnModel().getColumns();
        int i = 0;
        while (en.hasMoreElements()) {
            TableColumn col = (TableColumn) en.nextElement();

            properties.setProperty("column." + col.getIdentifier().toString() //$NON-NLS-1$
                    + ".width", Integer.toString(col.getWidth())); //$NON-NLS-1$
            properties.setProperty("column" + i + ".name", col.getIdentifier() //$NON-NLS-1$,  //$NON-NLS-2$
                    .toString());
            i++;
        }

        try {
            Debug.msg("Save other properties"); //$NON-NLS-1$
            properties.storeToXML(JFritz.PROPERTIES_FILE);
        } catch (IOException e) {
            Debug.err("Couldn't save Properties"); //$NON-NLS-1$
        }
    }

    /**
     * Displays balloon info message
     *
     * @param msg
     *            Message to be displayed
     */
    public static void infoMsg(String msg) {
        switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) { //$NON-NLS-1$,  //$NON-NLS-2$
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
            else if(trayIcon == null){
                MessageDlg msgDialog = new MessageDlg();
                msgDialog.showMessage(msg);
            }
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
        callInMsg(caller, called, ""); //$NON-NLS-1$
    }

    private String searchNameToPhoneNumber(String caller) {
        String name = ""; //$NON-NLS-1$
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searchin in local database ..."); //$NON-NLS-1$
        Person callerperson = phonebook.findPerson(callerPhoneNumber);
        if (callerperson != null) {
            name = callerperson.getFullname();
            Debug.msg("Found in local database: " + name); //$NON-NLS-1$
        } else {
            Debug.msg("Searchin on dasoertliche.de ..."); //$NON-NLS-1$
            Person person = ReverseLookup.lookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) { //$NON-NLS-1$
                name = person.getFullname();
                Debug.msg("Found on dasoertliche.de: " + name); //$NON-NLS-1$
                Debug.msg("Add person to database"); //$NON-NLS-1$
                phonebook.addEntry(person);
                phonebook.fireTableDataChanged();
            } else {
                person = new Person();
                person.addNumber(new PhoneNumber(caller));
                Debug.msg("Found no person"); //$NON-NLS-1$
                Debug.msg("Add dummy person to database"); //$NON-NLS-1$
                phonebook.addEntry(person);
                phonebook.fireTableDataChanged();
            }
        }
        return name;
    }

    private String[] searchFirstAndLastNameToPhoneNumber(String caller) {
        String name[] = {"", "", ""}; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searching in local database ..."); //$NON-NLS-1$
        Person callerperson = phonebook.findPerson(callerPhoneNumber);
        if (callerperson != null) {
            name[0] = callerperson.getFirstName();
			name[1] = callerperson.getLastName();
			name[2] = callerperson.getCompany();
			Debug.msg("Found in local database: " + name[1] + ", " + name[0]); //$NON-NLS-1$,  //$NON-NLS-2$
        } else {
            Debug.msg("Searching on dasoertliche.de ..."); //$NON-NLS-1$
            Person person = ReverseLookup.lookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) { //$NON-NLS-1$
				name[0] = callerperson.getFirstName();
				name[1] = callerperson.getLastName();
				name[2] = callerperson.getCompany();
                Debug.msg("Found on dasoertliche.de: " + name[1] + ", " + name[0]); //$NON-NLS-1$,  //$NON-NLS-2$
                Debug.msg("Add person to database"); //$NON-NLS-1$
                phonebook.addEntry(person);
                phonebook.fireTableDataChanged();
            } else {
                person = new Person();
                person.addNumber(new PhoneNumber(caller));
                Debug.msg("Found no person"); //$NON-NLS-1$
                Debug.msg("Add dummy person to database"); //$NON-NLS-1$
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

        Debug.msg("Caller: " + callerInput); //$NON-NLS-1$
        Debug.msg("Called: " + calledInput); //$NON-NLS-1$
        Debug.msg("Name: " + name); //$NON-NLS-1$

        String callerstr = "", calledstr = ""; //$NON-NLS-1$,  //$NON-NLS-2$
		String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

		callerstr = calledInput;
        if (!callerInput.startsWith("SIP")) { //$NON-NLS-1$
            PhoneNumber caller = new PhoneNumber(callerInput);
            if (caller.getIntNumber().equals("")) { //$NON-NLS-1$
                callerstr = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                callerstr = caller.getIntNumber();
        }

		calledstr = calledInput;
		if (calledInput.startsWith("SIP")) //$NON-NLS-1$
			calledstr = getSIPProviderTableModel().getSipProvider(calledInput,
                    calledInput);


        if (name.equals("") && !callerstr.equals(JFritz.getMessage("unknown"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			name = searchNameToPhoneNumber(callerstr);
			String [] nameArray = searchFirstAndLastNameToPhoneNumber(callerstr);
			firstname = nameArray[0];
			surname = nameArray[1];
			company = nameArray[2];
        }
		if (name.equals("")) name = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
		if (firstname.equals("") && surname.equals("")) surname = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		if (company.equals("")) company = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

		if (callerstr.startsWith("+49")) callerstr = "0" + callerstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

        Debug.msg("Caller: " + callerstr); //$NON-NLS-1$
        Debug.msg("Called: " + calledstr); //$NON-NLS-1$
        Debug.msg("Name: " + name); //$NON-NLS-1$

        switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) { //$NON-NLS-1$,  //$NON-NLS-2$
	        case 0: { // No Popup
	            break;
	        }
	        default: {
	            String outstring = JFritz.getMessage("incoming_call") + "\n " + JFritz.getMessage("from") //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
	                    + " " + callerstr; //$NON-NLS-1$
	            if (!name.equals(JFritz.getMessage("unknown"))) { //$NON-NLS-1$
	                outstring = outstring + " (" + name + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
	            }
	            if (!calledstr.equals(JFritz.getMessage("unknown"))) { //$NON-NLS-1$
	                outstring = outstring + "\n " + JFritz.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
	            }
	            infoMsg(outstring);
	            break;

	        }
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds", //$NON-NLS-1$
                "true"))) { //$NON-NLS-1$
            playSound(ringSound);
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.startExternProgram", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            String programString = JFritzUtils.deconvertSpecialChars(JFritz.getProperty("option.externProgram", //$NON-NLS-1$
                    "")); //$NON-NLS-1$

            programString = programString.replaceAll("%Number", callerstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
            programString = programString.replaceAll("%Called", calledstr); //$NON-NLS-1$
			programString = programString.replaceAll("%Firstname", firstname); //$NON-NLS-1$
			programString = programString.replaceAll("%Surname", surname); //$NON-NLS-1$
			programString = programString.replaceAll("%Company", company); //$NON-NLS-1$

            if (programString.indexOf("%URLENCODE") > -1) { //$NON-NLS-1$
                try {
                    Pattern p;
                    p = Pattern.compile("%URLENCODE\\(([^;]*)\\);"); //$NON-NLS-1$
                    Matcher m = p.matcher(programString);
                    while (m.find()) {
                        String toReplace = m.group();
                        toReplace = toReplace.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$,  //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\(", "\\\\("); //$NON-NLS-1$, //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\)", "\\\\)"); //$NON-NLS-1$, //$NON-NLS-2$
                        String toEncode = m.group(1);
                        programString = programString.replaceAll(toReplace,
                                URLEncoder.encode(toEncode, "UTF-8")); //$NON-NLS-1$
                    }
                } catch (UnsupportedEncodingException uee) {
                    Debug.err("JFritz.class: UnsupportedEncodingException: " //$NON-NLS-1$
                            + uee.toString());
                }
            }

            if (programString.equals("")) { //$NON-NLS-1$
                Debug
                        .errDlg(JFritz.getMessage("no_external_program") //$NON-NLS-1$
                                + programString);
                return;
            }
            Debug.msg("Start external Program: " + programString); //$NON-NLS-1$
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg(JFritz.getMessage("not_external_program_start") //$NON-NLS-1$
                        + programString);
                Debug.err(e.toString());
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
        Debug.msg("Called: " + calledInput); //$NON-NLS-1$
        Debug.msg("Provider: " + providerInput); //$NON-NLS-1$

        String calledstr = "", providerstr = "", name = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

		calledstr = calledInput;
        if (!calledInput.startsWith("SIP")) { //$NON-NLS-1$
            PhoneNumber called = new PhoneNumber(calledInput);
            if (!called.getIntNumber().equals("")) //$NON-NLS-1$
				calledstr = called.getIntNumber();
        }

		providerstr = providerInput;
        if (providerInput.startsWith("SIP")) //$NON-NLS-1$
			providerstr = getSIPProviderTableModel().getSipProvider(
                    providerInput, providerInput);

        name = searchNameToPhoneNumber(calledstr);
		String [] nameArray = searchFirstAndLastNameToPhoneNumber(calledstr);
		firstname = nameArray[0];
		surname = nameArray[1];
		company = nameArray[2];

		if (name.equals("")) name = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
		if (firstname.equals("") && surname.equals("")) surname = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		if (company.equals("")) company = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

		if (calledstr.startsWith("+49")) calledstr = "0" + calledstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

		String outstring = JFritz.getMessage("outgoing_call")+"\n " //$NON-NLS-1$,  //$NON-NLS-2$
		                   + JFritz.getMessage("to") +  " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$
		if (!name.equals(JFritz.getMessage("unknown"))) outstring += " (" + name + ")\n "; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		else  outstring += "\n "; //$NON-NLS-1$
		outstring += JFritz.getMessage("through_provider") + " " + providerstr; //$NON-NLS-1$,  //$NON-NLS-2$

        infoMsg(outstring);

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds", //$NON-NLS-1$
                "true"))) { //$NON-NLS-1$
            playSound(callSound);
        }

        // z.Z. noch deaktiviert
        if (false && JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.startExternProgram", "false"))) {  //$NON-NLS-1$,  //$NON-NLS-2$
            String programString = JFritz.getProperty("option.externProgram", //$NON-NLS-1$
                    ""); //$NON-NLS-1$

            programString = programString.replaceAll("%Number", providerstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
            programString = programString.replaceAll("%Called", calledstr); //$NON-NLS-1$
			programString = programString.replaceAll("%Firstname", firstname); //$NON-NLS-1$
			programString = programString.replaceAll("%Surname", surname); //$NON-NLS-1$
			programString = programString.replaceAll("%Company", company); //$NON-NLS-1$

            if (programString.indexOf("%URLENCODE") > -1) { //$NON-NLS-1$
                try {
                    Pattern p;
                    p = Pattern.compile("%URLENCODE\\(([^;]*)\\);"); //$NON-NLS-1$
                    Matcher m = p.matcher(programString);
                    while (m.find()) {
                        String toReplace = m.group();
                        toReplace = toReplace.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$,  //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\(", "\\\\("); //$NON-NLS-1$,  //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\)", "\\\\)"); //$NON-NLS-1$,  //$NON-NLS-2$
                        String toEncode = m.group(1);
                        programString = programString.replaceAll(toReplace,
                                URLEncoder.encode(toEncode, "UTF-8")); //$NON-NLS-1$
                    }
                } catch (UnsupportedEncodingException uee) {
                    Debug.err("JFritz.class: UnsupportedEncodingException: " //$NON-NLS-1$
                            + uee.toString());
                }
            }

            if (programString.equals("")) { //$NON-NLS-1$
                Debug
                        .errDlg(JFritz.getMessage("no_external_program") //$NON-NLS-1$
                                + programString);
                return;
            }
            Debug.msg("Starting external Program: " + programString); //$NON-NLS-1$
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg(JFritz.getMessage("not_external_program_start") //$NON-NLS-1$
                        + programString);
                Debug.err(e.toString());
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
        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.useSSDP", //$NON-NLS-1$
                "true"))) { //$NON-NLS-1$
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
     * Last modified: 26.04.06 by Bastian
     */
    public static String getMessage(String msg) {
        String i18n = ""; //$NON-NLS-1$
        try {
        	if(!messages.getString(msg).equals("")){
        		i18n = messages.getString(msg);
            	}else{
            		i18n = msg;
            	}
        } catch (MissingResourceException e) {
            Debug.err("Can't find resource string for " + msg); //$NON-NLS-1$
            i18n = msg;
        }
        return i18n;
    }

    /**
     * @return Returns the meanings of a locale abbreviation.
     */
    public static String getLocaleMeaning(String msg) {
        String localeMeaning = ""; //$NON-NLS-1$
        try {
        	if(!localeMeanings.getString(msg).equals("")){
        	localeMeaning = localeMeanings.getString(msg);
        	}else{
        		localeMeaning = msg;
        	}
        } catch (MissingResourceException e) {
            Debug.err("Can't find resource string for " + msg); //$NON-NLS-1$
            localeMeaning = msg;
        } catch (NullPointerException e){
            Debug.err("Can't find locale Meanings file"); //$NON-NLS-1$
            localeMeaning = msg;
        }
        return localeMeaning;
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
        return getProperty(property, ""); //$NON-NLS-1$
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
            Debug.msg("Hide JFritz-Window"); //$NON-NLS-1$
            jframe.setState(JFrame.ICONIFIED);
            jframe.setVisible(false);
        } else {
            Debug.msg("Show JFritz-Window"); //$NON-NLS-1$
            jframe.setState(JFrame.NORMAL);
            jframe.setVisible(true);
            jframe.toFront();
        }
    }

    public SipProviderTableModel getSIPProviderTableModel() {
        return sipprovider;
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
            Debug.msg("Watchdog enabled"); //$NON-NLS-1$
    }

    private static void doBackup() {
        CopyFile backup = new CopyFile();
        backup.copy(".","xml"); //$NON-NLS-1$,  //$NON-NLS-2$
    }

    /**
     * @Brian Jensen
     * This function changes the state of the ResourceBundle object
     * currently available locales: see lang subdirectory
     * Then it destroys the old window and redraws a new one with new locale
     *
     * @param l the locale to change the language to
     */
    public void createNewWindow(Locale l){
    	locale = l;

    	Debug.msg("Loading new locale"); //$NON-NLS-1$
    	loadMessages(locale);

    	refreshWindow();

    }

    /**
     * @ Bastian Schaefer
     *
     *	Destroys and repaints the Main Frame.
     *
     */

    public void refreshWindow(){
    	jfritz.saveProperties();
    	jframe.dispose();
    	javax.swing.SwingUtilities.invokeLater(jframe);
    	jframe = new JFritzWindow(this);
    	javax.swing.SwingUtilities.invokeLater(jframe);
    	jframe.checkOptions();
    	javax.swing.SwingUtilities.invokeLater(jframe);
    	jframe.setVisible(true);

    }

    /**
     *	Deletes actual systemtray and creates a new one.
     * @author Benjamin Schmitt
     */
    public void refreshTrayMenu()
    {
    	if (systray!=null && trayIcon!=null)
    	{
    		systray.removeTrayIcon(trayIcon);
    		this.createTrayMenu();
    	}
    }

    /**
     * Returns current firmware version
     * @return firmware
     */
    public static FritzBoxFirmware getFirmware()
    {
    	return firmware;
    }

    /**
     * Set current firmware version
     * @param fw
     */
    public static void setFirmware(FritzBoxFirmware fw)
    {
    	firmware = fw;
    }

    /**
     * @author Brian Jensen
     * This creates and then display the config wizard
     *
     */
    public void showConfigWizard(){
    	ConfigWizard wizard = new ConfigWizard(jfritz);
    	wizard.showWizard();

    }
}