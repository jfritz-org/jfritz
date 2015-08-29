/**
 * JFritz
 * http://jfritz.sourceforge.net/
 *
 *
 * (c) Arno Willig <akw@thinkwiki.org>
 *
 * Created on 08.04.2005
 *
 * Authors working on the project:
 * 		robotniko	Robert Palmer <robotniko@gmx.de>
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
 * (TODO: Checken, ob alle Bibliotheken vorhanden sind)
 * (TODO: Neue Kurzwahlen von der Beta-FW ins Telefonbuch aufnehmen)
 * (TODO: Import der Anrufliste im XML-Format beim Kontextmenü einbauen)
 * TODO: Language-Files checken, ob tatsächlich alle Werte benötigt werden
 * TODO: Sonderzeichen werden in den Balloontips unter Windows nicht korrekt angezeigt. Scheint ein Windowsproblem zu sein. Lösung/Workaround noch nicht gefunden.
 * TODO: JFritz.ico mitliefern
 * TODO: Linux-Startscript mitliefern
 * TODO: Vor dem Release noch den installDirectory-Pfad in JFritzUpdate auf "." anpassen
 *
 * Roadmap:
 * JFritz 1.0
 * Bewertung - Feature
 * rob - brian
 * 10 - 10 - Fehlermeldung an den Benutzer, wenn Daten nicht auf Festplatte gespeichert werden können. (Vielleicht schon implementiert -- Rob)
 * 10 - 10 - Kommentarspalte im Telefonbuch
 * 10 - 10 - Einstellen der Landes- und Ortsvorwahlen pro SIP-Account und nicht nur global (SF [ 1438932 ])
 * 10 - 7 - Webverkehr über Proxy (Was für Proxys sind gemeint: Socks 4 /5, oder HTTP(S)?)
 * 9 -  9 - Einstellungen-Seiten überarbeiten.       Größe veränderbar machen!(bei modalen Dialoge geht das nicht)
 * 9 -  9 - Name für die Nebenstellen aus der Weboberfläche auslesen und zuweisen (SF [ 1498487 ])
 * 9 -  8 - Export des gesamten Adressbuchs als VCard (http://www.ip-phone-forum.de/showthread.php?t=106758)
 * 9 -  7 - Vollständiger Outlook-Support (SF [ 1498489 ])
 * 8 -  8 - Analoge Rufnummer aus der FritzBox auslesen
 * 6 -  8 - Synchronisierung von JFritz Telefonbuch und FritzBox Telefonbuch (SF [ 1494436 ])
 * 6 -  8 - Datumsfilter konfigurierbar gestalten (SF [ 1498488 ])
 * 7 -  7 - Einige Icons auslagern - unterschiedliche Icon-Packs
 * 7 -  7 - Sounddateien auslagern - unterschiedliche Sound-Packs (gute Ideen, Brian)
 * 7 -  5 - CSV-Export nicht nur mit ";", sondern auch mit "TAB", "SPACE" und "," (SF [ 1509248 ])
 * 5 -  7 - Mehrere FritzBoxen abfragen (SF [ 1515855 ]) Dafür sollten wir alle zugriffe auf die Box in eigene Threads unterbringen.
 *                      Dann würde JFritz sich beim Hochfahren nicht so lange verzögern, wenn die Box nicht erreichbar ist.
 *                      Unterscheidung der Boxen anhand der MAC-Adresse (jpcap-Biblipthek für Java für ARP-Anfragen)
 * 6 -  6 - Internationalisierung abschließen, drunter Flaggencode optimieren (Nummer <-> flaggenfile Zuordnung in einer Hashmap ablegen).
 * 5 -  5 - Anrufmonitor: Anrufmonitor mächtiger machen (Aktionen nur für best. Nummern, verschiedene Aktionen, Log der Anrufe, Notizen zu einem laufenden Anruf) (SF [ 1525107 ])
 * 5 -  5 - Signalisieren der neu eingegangenen Anrufe im Tray (blinken, oder Zahl)
 * 5 -  5 - Button zum Löschen der Anrufliste
 * 4 -  5 - Visualisierung der aktuellen Gespräche (Frei, Nummer, Name, Dauer des Gesprächs ...)
 * 4 -  4 - Plugins (Mögliche Plugins: Drucken, Anrufmonitor)
 * 4 -  4 - Begrenzen der Anzeige der Anrufe in der Anrufliste (z.B. maximal 100 Einträge)
 * 4 -  4 - CSV-Export anpassbar machen (wie bei Thunderbird).
 * 4 -  4 - Registrierstatus der VoIP-Provider (SF [ 1315159 ])
 * 4 -  3 - Exportieren/Anzeige der Anrufliste nach Monaten getrennt
 * 4 -  2 - Import vom Tool Fritzinfo (http://www.ip-phone-forum.de/showthread.php?t=101090)
 * 4 -  1 - Einstellen der Farben, Symbolleisten, Schriftart, -größe (SF [ 1458892 ])
 * 3 -  3 - Anzeige des letzten Telefonats nicht nur abhängig von der Standardnummer und anzeige der gesprochenen Minuten pro Telefonbucheintrag
 * 3 -  2 - SQL-Anbindung (SF [ 1515305 ])
 * 3 -  2 - Tastaturkürzel für Aktionen sollen editierbar sein
 * 3 -  2 - Spalte "Privatkontakt" in CSV-Liste hinzufügen (SF [ 1480617 ])
 * 2 -  2 - 64-bit Unterstützung
 * 1 -  1 - LDAP-Anbindung
 * 1 -  1 - SMS Benachrichtigung (Über Festnetzgateway)
 * 1 -  1 - Style-Sheet für die Anzeige der Anrufliste als HTML
 * 1 -  1 - Statistikfunktionen
 * 1 -  1 - Skinns (SF [ 1471202 ])
 * 1 - (-1) - Unterstützung für das Adressbuch von Lotus Notes (SF [ 1445456 ]) (Ich bin dagegen, denn man bräuchte nochmal so ne Plugin wie bei Outlook,
 * 						nur ich schätze es gibt gar keins => wir müssten eine schreiben.
 * 						Habe das programm bei mir in der Arbeit, und ich hasse es. Ich werde nicht mehr Zeit als notwendig ist damit verbringen.
 * 1 -  1 - Rufton / Farbe eines bestimmten Anrufers
 * 1 -  1 - Verschiedene Klingeltöne per Rufnummer
 * - Einteilung der Benutzer in Gruppen
 * - Einfachere Verwaltung der Telefonbucheinträge, speziell das mergen zweier Einträge. Speziell wenn jemand mehrere Nummern hat, also Handy, Privat und SIP. Hier wäre es schön, wenn die eine Nummer leicht einem bestehenden Telefonbucheintrag hinzugefügt werden könnte und eben der 2te Eintrag dann gelöscht werden würde
 * - Adressbuchimport nur XML :-(    CSV!?
 * - Adressbuchabgleich mit SeaMonkey oder Thunderbird
 * - Adressbuchabgleich mit Outlook
 * - Adressbuchabgleich jfritz <-> FritzBox
 * - Mehrere lokale Benutzer?
 *
 * CHANGELOG:
 *
 * * TODO:
 * - Alle Zugriffe auf FritzBox in eigenen Threads
 * - Markieren der Zeilen per STRG auch in der "Teilnehmer"-Spalte
 * - Ändern der Standardrufnummer per Häckchen führt nicht zur Speicherung, erst wenn man noch ein Datum ändert
 * - Filter für Nebenstelle (Port) kombiniert mit eingetragenem Namen in der Weboberfläche
 * - Copy & Paste für Spalteneinträge (in Anrufliste, Telefonbuch und Kurzwahlliste)
 * - Importierte Rufnummern auf Sonderzeichen ( -, /, (, ) ) überprüfen
 * - Durchwahlnummern vor Zentrale-Nummern bei der Anzeige bevorzugen (sollte eigentlich gehen, aber scheint einen Bug zu haben)
 * - Schnittstelle zu externen Inverssuche-Programmen
 * - Andere Anrufmonitore noch an die neuen Listener anpassen und TestCases schreiben
 * - Kurzwahlliste sortierbar und Spaltenreihenfolge änderbar
 * - Bug "Doppelt erfasste Anrufe" behoben?
 * - http://www.ip-phone-forum.de/showthread.php?t=112348
 * - Überprüfen, geht wohl nicht mehr: Rückwärtssuche für Österreich über dasoertliche.de wieder eingebaut
 * - Connection-Timeout für ReverseLookup setzen
 * - Möglichst alle Fenstergrößen und -positionen speichern und wiederherstellen
 * - Alle Strings im Wizard überprüfen, vor allem die Sprache sollte stimmen
 * - Hilfe für jede Einstellungsseite, womit zur Wiki-Seite verlinkt wird
 * - .jfritz eigentlich unter Windows unter Anwendungsdaten\.jfritz
 * - Eigenständige Inverssuche nach beliebiger Nummer
 * - Fertigstellen von Event/Action
 * - Umbau auf Plugin-Konzept
 * - Umbau der Filter: Kein vorhergehendes Klicken in der Anrufliste, sondern alle Optionen als Kontextmenü
 * - Popup trotz fehlender Inverssuche
 * - Überprüfen, ob Internetüberwachung auch mit neueren Firmwares geht
 * - Popup am Bildschirmrand andocken.
 * - option.clientStandAlone wird nirgends verwendet
 * TODO-END
 *
 * FIXME:
 * - Bestehenden dummy Eintrag überschreiben
 * - Rechtsklick in Teilnehmerspalte
 * - Text ändern für reverse_lookup_dummy
 * - Änderungen einer Telefonnummer im Client wird beim Server nicht aktualisiert
 * - ^ auch Löschen von Rufnummern funktioniert nicht, Hinzufügen EINER Nummer jedoch schon (mehrerer nicht)
 * - Rückwärtssuche beim Client funktioniert nicht
 * FIXME-END
 *
 */

package de.moonflower.jfritz;

import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;

import jd.nutils.OSDetector;

import org.apache.http.auth.InvalidCredentialsException;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.DOMConfigurator;

import de.moonflower.jfritz.backup.JFritzBackup;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.dialogs.configwizard.ConfigWizard;
import de.moonflower.jfritz.dialogs.simple.AddressPasswordDialog;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.messages.UpdateMessageProvider;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.CLIOption;
import de.moonflower.jfritz.utils.CLIOptions;
import de.moonflower.jfritz.utils.ComplexJOptionPaneMessage;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ShutdownHook;
import de.moonflower.jfritz.utils.reverselookup.IReverseLookupFinishedWithResultListener;
import de.moonflower.jfritz.utils.reverselookup.IReverseLookupProgressListener;
import de.moonflower.jfritz.utils.reverselookup.JFritzReverseLookup;
import de.robotniko.fboxlib.exceptions.LoginBlockedException;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;
//import org.apache.http.auth.InvalidCredentialsException;

/**
 * @author robroy
 *
 */
public class Main  {
	private final static Logger log = Logger.getLogger(Main.class);

	public final static String PROGRAM_SECRET = "jFrItZsEcReT"; //$NON-NLS-1$

	public final static String PROGRAM_SEED = "10D4KK3L"; //$NON-NLS-1$

	public final static String CVS_TAG = "$Id: Main.java 240 2012-01-16 12:31:42Z robotniko $"; //$NON-NLS-1$

	public final static String PROGRAM_URL = "http://www.jfritz.org/"; //$NON-NLS-1$

	public final static String JFRITZ_PROJECT = "all members of the JFritz-Team";

	public final static String PROJECT_ADMIN = "Robert Palmer <robotniko@users.sourceforge.net>"; //$NON-NLS-1$

	public final static String JFRITZ_HIDDEN_DIR = ".jfritz";

	public final static String USER_DIR = System.getProperty("user.home")
			+ File.separator + JFRITZ_HIDDEN_DIR;

	public final static String USER_JFRITZ_FILE = "jfritz.txt";

	public final static String LOCK_FILE = ".lock"; //$NON-NLS-1$

	public static boolean systraySupport = false;

	public static boolean showSplashScreen = true;

	public static boolean updateBetaUrl = false;

	private static ResourceBundle localeMeanings;

	private static boolean showConfWizard;

	private static boolean enableInstanceControl = true;

	private static boolean checkSystray = true;

	private static String jfritzHomedir;

	private static JFritz jfritz;

	private CLIOptions options;

	private static int exitCode = 0;

	private static boolean alreadyDoneShutdown;

	private static Vector<Locale> supported_languages;

	private static ShutdownHook.Handler shutdownHandler;
	private static ShutdownThread shutdownThread;

	private static int EXIT_CODE_OK = 0;
	private static int EXIT_CODE_HELP = -1;
	private static int EXIT_CODE_PARAMETER_NOT_FOUND = -2;
	private static int EXIT_CODE_PARAMETER_WRONG_FORMAT = -3;
	private static int EXIT_CODE_MULTIPLE_INSTANCE_LOCK = 1;
	public static int EXIT_CODE_FORBID_COMMUNICATION_WITH_FRITZBOX = 2;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();
	protected UpdateMessageProvider updateMessages = UpdateMessageProvider.getInstance();
	
	private Level loggingLevel = Level.INFO;
	
	public Main() {
		// NICHT VERWENDEN, nur f�r TestCases, nicht alles initialisiert. NICHT
		// VERWENDEN!
		loadLanguages();
		JFritzDataDirectory.getInstance().loadSaveDir(this);
		showConfWizard = properties.loadProperties(false);
		String loc = properties.getProperty("locale");
		messages.loadMessages(new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_") + 1, loc.length()))); //$NON-NLS-1$,  //$NON-NLS-2$
		updateMessages.loadMessages(new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_") + 1, loc.length()))); //$NON-NLS-1$,  //$NON-NLS-2$
		loadLocaleMeanings(new Locale("int", "INT"));

	}

	public Main(String[] args) {
		setupLogging();
		
		cleanupOldInstallation();

		Calendar cal = Calendar.getInstance();
		cal.getTime();

		System.out.println(ProgramConstants.PROGRAM_NAME
			+ " v" + ProgramConstants.PROGRAM_VERSION //$NON-NLS-1$
			+ " Rev. " + ProgramConstants.REVISION //$NON-NLS-1$
			+ " (c) 2005-" + cal.get(Calendar.YEAR) + " by " + JFRITZ_PROJECT); //$NON-NLS-1$
		Thread.currentThread().setPriority(5);
		Thread.currentThread().setName("main thread");

		alreadyDoneShutdown = false;

		// Catch non-user-initiated VM shutdown
		shutdownHandler = new ShutdownHook.Handler() {
			public void shutdown(String signal_name) {
				Runtime.getRuntime().removeShutdownHook(shutdownThread);
				log.debug("Core: Caught signal " + signal_name);
				prepareShutdown(false, true);
			}
		};

		ShutdownHook.install(shutdownHandler);

		shutdownThread = new ShutdownThread(this);
		Runtime.getRuntime().addShutdownHook(shutdownThread);

		jfritzHomedir = JFritzUtils.getFullPath(JFritzUtils.FILESEP + ".update");
		jfritzHomedir = jfritzHomedir.substring(0, jfritzHomedir.length() - 7);

		initiateCLIParameters();

		// load supported languages
		loadLanguages();

		// move save dir and default file location
		shouldMoveDataToRightSaveDir();

		log.info(ProgramConstants.PROGRAM_NAME
				+ " v" + ProgramConstants.PROGRAM_VERSION //$NON-NLS-1$
				+ " Rev. " + ProgramConstants.REVISION //$NON-NLS-1$
				+ " (c) 2005-" + cal.get(Calendar.YEAR) + " by " + JFRITZ_PROJECT); //$NON-NLS-1$

		printSystemInfo();
		if (checkDebugParameters(args)) { // false if jfritz should stop
											// execution
			initJFritz(args, this);
		}
	}

	private void setupLogging() {
		DOMConfigurator.configure(JFritzUtils.getFullPath("/log4j.xml"));
		JFritzDataDirectory.getInstance().loadSaveDir(this);
		initLog4jAppender();
	}
	
	private void printSystemInfo() {
		log.info("JFritz runs on " + OSDetector.getOSString());
		log.info("OS Language: " + System.getProperty("user.language"));
		log.info("OS Country: " + System.getProperty("user.country"));
		
		String jvm_version = System.getProperty("java.version");
		log.info("Java version: " + jvm_version);
	}

	/**
	 * Main method for starting JFritz
	 *
	 * LAST MODIFIED: Brian 04.06.06 added option to disable mulitple instance
	 * control added a new parameter switch: -w
	 *
	 * @param args
	 *            Program arguments (-h -v ...)
	 * @throws de.robotniko.fboxlib.exceptions.InvalidCredentialsException 
	 * @throws InvalidCredentialsException 
	 * @throws PageNotFoundException 
	 * @throws LoginBlockedException 
	 *
	 */
	public static void main(String[] args) {
		new Main(args);
	}

	/**
	 * Initialisiert die erlaubten Kommandozeilenparameter
	 *
	 */
	private void initiateCLIParameters() {
		options = new CLIOptions();

		options.addOption('b',
				"backup" //$NON-NLS-1$,  //$NON-NLS-2$
				, null,
				"Creates a backup of all xml-Files in the directory 'backup'"); //$NON-NLS-1$
		options.addOption('c', "clear_list" //$NON-NLS-1$,  //$NON-NLS-2$
				,null, "Clears Caller List and exit"); //$NON-NLS-1$
		options.addOption('e', "export" //$NON-NLS-1$,  //$NON-NLS-2$
				,"filename", "Fetch calls and export to CSV file."); //$NON-NLS-1$,  //$NON-NLS-2$
		options.addOption('f', "fetch" //$NON-NLS-1$,  //$NON-NLS-2$
				,null, "Fetch new calls and exit"); //$NON-NLS-3$
		options.addOption('h', "help" //$NON-NLS-1$,  //$NON-NLS-2$
				,null, "This short description"); //$NON-NLS-1$
		options.addOption('i',"lang" //$NON-NLS-1$,  //$NON-NLS-2$
				,"language", "Set the display language, currently supported: german, english"); //$NON-NLS-1$,  //$NON-NLS-2$
		options.addOption('n', "nosystray" //$NON-NLS-1$,  //$NON-NLS-2$
				,null, "Turn off systray support"); //$NON-NLS-1$
		options.addOption('p', "priority" //$NON-NLS-1$,  //$NON-NLS-2$
				,"level", "Set program priority [1..10]"); //$NON-NLS-1$,  //$NON-NLS-2$
		options.addOption('q', "quiet" //$NON-NLS-1$,  //$NON-NLS-2$
				,null, "Hides splash screen"); //$NON-NLS-1$
		options.addOption('r',"reverse-lookup" //$NON-NLS-1$,  //$NON-NLS-2$
				,null,"Do a reverse lookup and exit. Can be used together with -e -f and -z"); //$NON-NLS-1$
		options.addOption('s', "systray" //$NON-NLS-1$,  //$NON-NLS-2$
				,null, "Turn on systray support"); //$NON-NLS-1$
		options.addOption('v',"verbose" //$NON-NLS-1$,  //$NON-NLS-2$
				,"level", "Turn on debug information on console. Possible values: ERROR, WARNING, INFO, DEBUG"); //$NON-NLS-1$
		options.addOption('u',"updateBeta" //$NON-NLS-1$,  //$NON-NLS-2$
				,null,"Set update url to check for beta-version. Only for beta-testers, you can loose all your data!"); //$NON-NLS-1$
		options.addOption('w',"without-control" //$NON-NLS-1$,  //$NON-NLS-2$
				,null,"Turns off multiple instance control. DON'T USE, unless you know what your are doing"); //$NON-NLS-1$
		options.addOption('z',"exportForeign" //$NON-NLS-1$,  //$NON-NLS-2$
				,null,"Write phonebooks compatible to BIT FBF Dialer and some other callmonitors."); //$NON-NLS-1$
	}

	/**
	 * Überprüft, ob die -h, -v oder -l Startparameter gesetzt sind
	 *
	 * @param args
	 *            Kommandozeilenargumente
	 * @return true if jfritz should stop execution
	 */
	private boolean checkDebugParameters(String[] args) {
		Vector<CLIOption> foundOptions = options.parseOptions(args);

		// Checke den help, verbose/debug, quiet und log-to-file parameter
		Enumeration<CLIOption> en = foundOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = (CLIOption) en.nextElement();

			if (option == null) {
				log.info("Unknown command line parameter specified!");
				log.info("Usage: java -jar jfritz.jar [Options]"); //$NON-NLS-1$
				options.printOptions();
				exit(EXIT_CODE_OK);
				return false;
			}

			switch (option.getShortOption()) {
			case 'h': //$NON-NLS-1$
				log.info("Usage: java -jar jfritz.jar [Options]"); //$NON-NLS-1$
				options.printOptions();
				exit(EXIT_CODE_HELP);
				return false;
			case 'v': //$NON-NLS-1$
				Debug.setVerbose(true);
				String level = option.getParameter();
				if ("ERROR".equals(level)) {
					loggingLevel = Level.ERROR;
				} else if ("WARNING".equals(level)) {
					loggingLevel = Level.WARN;
				} else if ("INFO".equals(level)) {
					loggingLevel = Level.INFO;
				} else if ("DEBUG".equals(level)) {
					loggingLevel = Level.DEBUG;
				}
				Logger.getRootLogger().setLevel(loggingLevel);
				break;
			case 'q': //$NON-NLS-1$
				showSplashScreen = false;
				break;
			case 'u': //$NON-NLS-1$
				updateBetaUrl = true;
				break;
			}
		}
		return true;
	}

	private void initJFritz(String[] args, Main main) {
		SplashScreen splash = new SplashScreen(showSplashScreen);
		splash.setVersion("v" + ProgramConstants.PROGRAM_VERSION + " Rev: " + ProgramConstants.REVISION);
		splash.setStatus("Initializing JFritz...");

		splash.setStatus("Loading properties...");
		showConfWizard = properties.loadProperties(true);
		if (showConfWizard) {
			JFritzDataDirectory.getInstance().writeSaveDir();
		}

		if (properties.getProperty("locale").equals("")) {
			log.info("No language set yet ... Setting language to OS language");
			// Check if language is supported. If not switch to english
			if (supported_languages.contains(new Locale(System
					.getProperty("user.language"), System
					.getProperty("user.country")))) {
				properties.setProperty("locale", System
						.getProperty("user.language")
						+ "_" + System.getProperty("user.country"));
			} else {
				log.warn("Your language ist not yet supported.");
				properties.setProperty("locale", "en_US");
			}
		}
		String loc = properties.getProperty("locale");
		log.info("Selected language: " + loc);

		messages.loadMessages(new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_") + 1, loc.length()))); //$NON-NLS-1$,  //$NON-NLS-2$
		updateMessages.loadMessages(new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_") + 1, loc.length()))); //$NON-NLS-1$,  //$NON-NLS-2$
		loadLocaleMeanings(new Locale("int", "INT"));

		int result = 0;
		splash.setStatus("Checking startup password...");
		String ask = properties.getProperty("jfritz.pwd");//$NON-NLS-1$
		String decrypted_pwd = Encryption.decrypt(properties.getProperty("jfritz.seed"));
		String pass = "";
		if ((decrypted_pwd != null)
				&& (decrypted_pwd.length() > Main.PROGRAM_SEED.length())) {
			pass = decrypted_pwd.substring(Main.PROGRAM_SEED.length());
		} else {
			String message = "Configuration file \"jfritz.properties.xml\" is corrupt."
					+ "\nSend an EMail to support@jfritz.org with this error"
					+ "\nmessage and the attached \"jfritz.properties.xml\"-file.";
			log.error(message);
			Debug.errDlg(message);
			result = 1;
		}
		if (!(Main.PROGRAM_SECRET + pass).equals(Encryption.decrypt(ask))) {
			String password = "1";
			while (result == 0 && !password.equals(pass)) {
				password = main.showPasswordDialog(""); //$NON-NLS-1$
				if (password == null) { // PasswordDialog canceled
					result = 1;
				} else if (!password.equals(pass)) {
					log.error(messages.getMessage("box.wrong_password")); //$NON-NLS-1$
					Debug.errDlg(messages.getMessage("box.wrong_password")); //$NON-NLS-1$
				}
			}
		}

		if (result == 0) {
			parseMultipleInstanceCheckArgument(args);			
			if (!main.checkInstanceControl()) {
				result = -1;
				splash.dispose();
			}
		}
		boolean wizardCancelled = false;
		if (result == 0 && showConfWizard) {
			splash.setStatus("Config wizard ...");
			splash.setVisible(false);
			wizardCancelled = showConfigWizard(splash);
			splash.setVisible(true);
		}

		if (result == 0) {
			splash.setStatus("Initializing main application...");
			jfritz = new JFritz(main);

			jfritz.initNumbers();
			splash.setStatus("Initializing Fritz!Box ...");
			try {
				result = jfritz.initFritzBox();
			} catch (WrongPasswordException e1) {
				log.error(messages.getMessage("box.wrong_password")); //$NON-NLS-1$
			} catch (IOException e1) {
				log.error(messages.getMessage("box.not_found")); //$NON-NLS-1$
			} catch (InvalidFirmwareException e1) {
				log.error(messages.getMessage("unknown_firmware")); //$NON-NLS-1$
			}
		}

		if (result == 0) {
			splash.setStatus("Loading quick dials...");
			jfritz.initQuickDials();
		}

		if (result == 0) {
			splash.setStatus("Loading caller list and phonebook...");
			jfritz.initCallerListAndPhoneBook();
		}
		
		if (result == 0) {
			// must be called before main.checkCLIParameters
			jfritz.registerListeners();
		}

		if (result == 0) {
			boolean shutdownInvoked = main.checkCLIParameters(args);
			if (shutdownInvoked) {
				result = -1;
			}
		}

		if (result == 0) {
			splash.setStatus("Initializing sounds...");
			jfritz.initSounds();
		}

		if (result == 0) {
			splash.setStatus("Initializing call monitor listener...");
			jfritz.initCallMonitorListener();
		}
		if (result == 0) {
			splash.setStatus("Initializing Client/Server...");
			jfritz.initClientServer();
		}
		if (result == 0) {
			splash.setStatus("Setting default look and feel...");
			jfritz.setDefaultLookAndFeel();
		}

		if (result == 0) {
			splash.setStatus("Starting ...");
			jfritz.createJFrame();
			splash.dispose();
			jfritz.startClientServer();
			jfritz.startWatchdog();
			jfritz.registerGuiListeners();
		}

		log.info("Main is now exiting...");
		if (result != 0) {
			splash.dispose();
			main.exit(result);
		}

		if (result == 0 && !wizardCancelled
				&& JFritz.getJframe() != null) {
			JFritz.getJframe().checkOptions();
		}
	}

	public static boolean showConfigWizard(final SplashScreen splash) {
		log.info("Presenting user with the configuration dialog");
		ConfigWizard wizard = new ConfigWizard(null);

		boolean wizardCanceled = true;
		try {
			wizardCanceled = wizard.showWizard(splash);
		} catch (WrongPasswordException e) {
			e.printStackTrace();
		} catch (InvalidFirmwareException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wizardCanceled;
	}

	public void initLog4jAppender() {
		Appender oldFileAppender = Logger.getRootLogger().getAppender("FileAppender");
		if (oldFileAppender != null) {
			Logger.getRootLogger().removeAppender(oldFileAppender);
		}

		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("%d{dd.MM.yy HH:mm:ss}|%t|%p|%C{1} - %m%n");
		String path = Debug.debugLogFilePath;
		FileAppender fa;
		try {
			fa = new FileAppender(layout, path, false);
			fa.activateOptions();
			Logger.getRootLogger().setLevel(loggingLevel);
			Logger.getRootLogger().addAppender(fa);
			Logger.getRootLogger().info("Logging to " + fa.getFile());
		} catch (IOException e) {
			Logger.getRootLogger().error("Could not write log file to " + path);
		}
	}

	private String preparePattern(final String input) {
		String output = input;
		if (input.indexOf("\\") > -1) {
			output = input.replaceAll("[\\\\$]", "\\\\$0");
		}
		return output;
	}

	private void parseMultipleInstanceCheckArgument(String[] args) {
		Vector<CLIOption> foundOptions = options.parseOptions(args);
		Enumeration<CLIOption> en = foundOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = (CLIOption) en.nextElement();

			switch (option.getShortOption()) {
			case 'w': //$NON-NLS-1$
				enableInstanceControl = false;
				System.err.println("Turning off Multiple instance control!"); //$NON-NLS-1$
				System.err.println("You were warned! Data loss may occur."); //$NON-NLS-1$
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Überprüft die weiteren Kommandozeilenparameter
	 *
	 * @param args
	 *            Kommandozeilenargumente
	 * @return True if shutdown has been invoked, false otherwise.
	 * @throws InvalidCredentialsException 
	 */
	private boolean checkCLIParameters(String[] args) {
		boolean shutdown = false;
		log.debug("Start commandline parsing"); //$NON-NLS-1$
		// Checke alle weiteren Parameter
		Vector<CLIOption> foundOptions = options.parseOptions(args);
		Enumeration<CLIOption> en = foundOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = (CLIOption) en.nextElement();

			switch (option.getShortOption()) {
			case 'b': //$NON-NLS-1$
				JFritzBackup.getInstance().doBackup();
				break;
			case 's': //$NON-NLS-1$
				systraySupport = true;
				break;
			case 'n': //$NON-NLS-1$
				checkSystray = false;
				break;
			case 'f':
				log.info("Fetch caller list from command line ..."); //$NON-NLS-1$
				JFritz.getBoxCommunication().getCallerList(null);
				shutdown = true;
				exit(EXIT_CODE_OK);
				break;
			case 'r':
				doReverseLookup();
				shutdown = true;
				exit(EXIT_CODE_OK);
				break;
			case 'e':
				String csvFileName = option.getParameter();
				if (csvFileName == null || csvFileName.equals("")) { //$NON-NLS-1$
					System.err.println(messages.getMessage("parameter_not_found")); //$NON-NLS-1$
					shutdown = true;
					exit(EXIT_CODE_PARAMETER_NOT_FOUND);
					break;
				}
				log.info("Exporting Call list (csv) to " + csvFileName); //$NON-NLS-1$
				JFritz.getCallerList().saveToCSVFile(csvFileName, true);
				shutdown = true;
				exit(EXIT_CODE_OK);
				break;
			case 'z':
				JFritz.getPhonebook().saveToBITFBFDialerFormat("bitbook.dat"); //$NON-NLS-1$
				JFritz.getPhonebook()
						.saveToCallMonitorFormat("CallMonitor.adr"); //$NON-NLS-1$
				shutdown = true;
				exit(EXIT_CODE_OK);
				break;
			case 'c': //$NON-NLS-1$
				log.info("Clearing Call List"); //$NON-NLS-1$
				JFritz.getCallerList().clearList();
				shutdown = true;
				exit(EXIT_CODE_OK);
				break;
			case 'i': //$NON-NLS-1$
				String language = option.getParameter();
				if (language == null) {
					System.err.println(messages.getMessage("invalid_language")); //$NON-NLS-1$
					System.err.println("Deutsch: de"); //$NON-NLS-1$
					System.err.println("English: en"); //$NON-NLS-1$
					System.err.println("Italian: it"); //$NON-NLS-1$
					System.err.println("Netherland: nl"); //$NON-NLS-1$
					System.err.println("Poland: pl"); //$NON-NLS-1$
					System.err.println("Russia: ru"); //$NON-NLS-1$
					exit(EXIT_CODE_PARAMETER_WRONG_FORMAT);
					shutdown = true;
				} else if (language.equals("english") || language.equals("en")) { //$NON-NLS-1$
					properties.setProperty("locale", "en_US");
				} else if (language.equals("german") || language.equals("de")) { //$NON-NLS-1$
					properties.setProperty("locale", "de_DE");
				} else if (language.equals("italian") || language.equals("it")) { //$NON-NLS-1$
					properties.setProperty("locale", "it_IT");
				} else if (language.equals("netherlands") || language.equals("nl")) { //$NON-NLS-1$
					properties.setProperty("locale", "nl_NL");
				} else if (language.equals("poland") || language.equals("pl")) { //$NON-NLS-1$
					properties.setProperty("locale", "pl_PL");
				} else if (language.equals("russian") || language.equals("ru")) { //$NON-NLS-1$
					properties.setProperty("locale", "ru_RU");
				} else {
					System.err.println(messages.getMessage("invalid_language")); //$NON-NLS-1$
					System.err.println("Deutsch: de"); //$NON-NLS-1$
					System.err.println("English: en"); //$NON-NLS-1$
					System.err.println("Italian: it"); //$NON-NLS-1$
					System.err.println("Netherland: nl"); //$NON-NLS-1$
					System.err.println("Poland: pl"); //$NON-NLS-1$
					System.err.println("Russia: ru"); //$NON-NLS-1$
					exit(EXIT_CODE_PARAMETER_WRONG_FORMAT);
					shutdown = true;
				}
				messages.loadMessages(new Locale(properties.getProperty("locale"))); //$NON-NLS-1$,  //$NON-NLS-2$
				updateMessages.loadMessages(new Locale(properties.getProperty("locale"))); //$NON-NLS-1$,  //$NON-NLS-2$
				break;
			case 'p': //$NON-NLS-1$
				String priority = option.getParameter();
				if (priority == null || priority.equals("")) { //$NON-NLS-1$
					System.err.println(messages
							.getMessage("parameter_not_found")); //$NON-NLS-1$
					exit(EXIT_CODE_PARAMETER_NOT_FOUND);
					shutdown = true;
				} else {
					try {
						int level = Integer.parseInt(priority);
						Thread.currentThread().setPriority(level);
						log.info("Set priority to level " + priority); //$NON-NLS-1$
					} catch (NumberFormatException nfe) {
						System.err.println(messages
								.getMessage("parameter_wrong_priority")); //$NON-NLS-1$
						exit(EXIT_CODE_PARAMETER_WRONG_FORMAT);
						shutdown = true;
					} catch (IllegalArgumentException iae) {
						System.err.println(messages
								.getMessage("parameter_wrong_priority")); //$NON-NLS-1$
						exit(EXIT_CODE_PARAMETER_WRONG_FORMAT);
						shutdown = true;
					}
					break;
				}
			default:
				break;
			}
		}
		return shutdown;
	}

	public static boolean lockExists() {
		File f = new File(JFritzDataDirectory.getInstance().getDataDirectory()
				+ LOCK_FILE);
		return f.exists();
	}

	public static void createLock() {
		File f = new File(JFritzDataDirectory.getInstance().getDataDirectory()
				+ LOCK_FILE);
		try {
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
		} catch (SecurityException se) {
			log.error("Could not delete instance lock");
		} catch (IOException e) {
			log.error("Could not set instance lock");
		}
	}

	public static void removeLock() {
		File f = new File(JFritzDataDirectory.getInstance().getDataDirectory()
				+ LOCK_FILE);
		try {
			if (f.exists()) {
				f.delete();
			}
		} catch (SecurityException se) {
			log.error("Could not delete instance lock");
		}
	}

	private void shouldMoveDataToRightSaveDir() {
		// zeigt auf altes Verzeichnis
		log.debug("Old SAVE_DIR: " + JFritzDataDirectory.getInstance().getDataDirectory());
		boolean shouldMoveDirectory = false;
		String newSaveDir = JFritzDataDirectory.getInstance().getDataDirectory();
		if (JFritzDataDirectory.getInstance().getDataDirectory().equals(
				System.getProperty("user.dir") + File.separator)) {
			shouldMoveDirectory = true;
			newSaveDir = JFritzDataDirectory.getInstance().getDefaultSaveDirectory();
			if (JFritzDataDirectory.getInstance().getDataDirectory().equals(newSaveDir)) {
				shouldMoveDirectory = false;
			}
		}

		if (shouldMoveDirectory) {
			showConfWizard = properties.loadProperties(false);
			if (properties.getProperty("locale").equals("")) {
				log.info("No language set yet ... Setting language to OS language");
				// Check if language is supported. If not switch to english
				if (supported_languages.contains(new Locale(System
						.getProperty("user.language"), System
						.getProperty("user.country")))) {
					properties.setProperty("locale", System
							.getProperty("user.language")
							+ "_" + System.getProperty("user.country"));
				} else {
					log.warn("Your language ist not yet supported.");
					properties.setProperty("locale", "en_US");
				}
			}

			String loc = properties.getProperty("locale");
			log.info("Selected language: " + loc);

			messages.loadMessages(new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_") + 1, loc.length()))); //$NON-NLS-1$,  //$NON-NLS-2$
			updateMessages.loadMessages(new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_") + 1, loc.length()))); //$NON-NLS-1$,  //$NON-NLS-2$
			loadLocaleMeanings(new Locale("int", "INT"));
			log.info("Shall JFritz move data from "
					+ JFritzDataDirectory.getInstance().getDataDirectory()
					+ " to " + newSaveDir + " ?");

			int answer = JOptionPane.NO_OPTION;

			File dir = new File(JFritzDataDirectory.getInstance()
					.getDataDirectory());
			File[] entries = dir.listFiles(new FileFilter() {
				public boolean accept(File arg0) {
					if (arg0.getName().endsWith(".xml")) {
						if (!"build-release-pwd.xml".equals(arg0.getName())
								&& !"build-release.xml".equals(arg0.getName())
								&& !"build.xml".equals(arg0.getName())) {
							log.debug("Found XML-File to move: "
									+ arg0.getName());
							return true;
						}
					}
					return false;
				}
			});

			String message = messages.getMessage("moveDataDirectory_Warning"); //$NON-NLS-1$

			message = message.replaceAll("%FROM", preparePattern(JFritzDataDirectory.getInstance().getDataDirectory()));
			message = message.replaceAll("%TO", preparePattern(newSaveDir));
			ComplexJOptionPaneMessage msg = new ComplexJOptionPaneMessage(
					"legalInfo.moveDataDirectory", //$NON-NLS-1$
					message);
			if (msg.showDialogEnabled() && (entries.length != 0)) {
				log.debug("Show confirm dialog to move data");
				answer = JOptionPane.showConfirmDialog(null, msg
						.getComponents(), messages.getMessage("information"),
						JOptionPane.YES_NO_OPTION);
				msg.saveProperty();
				properties.saveStateProperties();
			}

			if (answer == JOptionPane.YES_OPTION || (entries.length == 0)) {
				log.debug("Moving data from "
						+ JFritzDataDirectory.getInstance().getDataDirectory()
						+ " to " + newSaveDir + " !");
				JFritzDataDirectory.getInstance().changeSaveDir(newSaveDir);
			}

		} else {
			log.debug("Data is already at an exclusive directory: "
					+ JFritzDataDirectory.getInstance().getDataDirectory());
		}
	}

	/**
	 * Ist die Mehrfachstart-Überprüfung aktiv, so wird ein Dialog angezeigt mit
	 * dem der User JFritz sicher beenden kann.
	 *
	 * @return true, if everything is ok. false if user decided to shutdown
	 *         jfritz.
	 */
	private boolean checkInstanceControl() {
		boolean result = true;
		if (enableInstanceControl) {
			// check isRunning and exit or set lock
			if (!lockExists()) {
				log.info("Multiple instance lock: set lock."); //$NON-NLS-1$
				result = true;
				createLock();
			} else {
				log.warn("Multiple instance lock: Another instance is already running."); //$NON-NLS-1$
				int answer = JOptionPane
						.showConfirmDialog(
								null,
								messages.getMessage("lock_error_dialog1") //$NON-NLS-1$
										+ messages.getMessage("lock_error_dialog2") //$NON-NLS-1$
										+ messages.getMessage("lock_error_dialog3") //$NON-NLS-1$
										+ messages.getMessage("lock_error_dialog4"), //$NON-NLS-1$
								ProgramConstants.PROGRAM_NAME
										+ " - " + messages.getMessage("information"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
				if (answer == JOptionPane.YES_OPTION) {
					log.warn("Multiple instance lock: User decided to shut down this instance."); //$NON-NLS-1$
					exit(EXIT_CODE_MULTIPLE_INSTANCE_LOCK);
					result = false;
				} else {
					log.warn("Multiple instance lock: User decided NOT to shut down this instance."); //$NON-NLS-1$
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * The function is called mostly from the mac quit handler code to safely
	 * end jfritz when the program should be terminated either through user
	 * input or through a system event (logoff / restart ..)
	 *
	 * @param i
	 *            the exit code
	 */
	public void exit(int i) {
		log.debug("Main.exit(" + i + ")");
		exitCode = i;
		Runtime.getRuntime().removeShutdownHook(shutdownThread);
		prepareShutdown(false, false);
	}

	public void closeOpenConnections() {
		log.info("Closing all open network connections");

		String networkType = properties.getProperty("network.type");

		if (networkType != null
				&& networkType.equals("1")
				&& NetworkStateMonitor.isListening()) {
			NetworkStateMonitor.stopServer();
		} else {
			if (networkType != null
					&& networkType.equals("2")
					&& NetworkStateMonitor.isConnectedToServer())
				NetworkStateMonitor.stopClient();
		}
	}

	/**
	 * Loads locale meanings
	 *
	 * @param locale
	 */
	private static void loadLocaleMeanings(Locale locale) {
		try {
			localeMeanings = ResourceBundle.getBundle("languages", locale);//$NON-NLS-1$
		} catch (MissingResourceException e) {
			log.error("Can't find locale Meanings resource!");//$NON-NLS-1$
		}
	}

	/**
	 * @return Returns the meanings of a locale abbreviation.
	 */
	public static String getLocaleMeaning(String msg) {
		String localeMeaning = ""; //$NON-NLS-1$
		try {
			if (!localeMeanings.getString(msg).equals("")) {
				localeMeaning = localeMeanings.getString(msg);
			} else {
				localeMeaning = msg;
			}
		} catch (MissingResourceException e) {
			log.error("Can't find resource string for " + msg); //$NON-NLS-1$
			localeMeaning = msg;
		} catch (NullPointerException e) {
			log.error("Can't find locale Meanings file"); //$NON-NLS-1$
			localeMeaning = msg;
		}
		return localeMeaning;
	}

	public static boolean isInstanceControlEnabled() {
		return enableInstanceControl;
	}

	/**
	 * Checks for systray availability
	 */
	public static boolean checkForSystraySupport() {
		if (!checkSystray)
			return false;
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os.equals("Linux") || os.equals("Solaris") //$NON-NLS-1$,  //$NON-NLS-2$
				|| os.startsWith("Windows")) { //$NON-NLS-1$
			systraySupport = true;
		}
		return systraySupport;
	}

	public static String getHomeDirectory() {
		return jfritzHomedir;
	}

	public JFritz getJfritz() {
		return jfritz;
	}

	// private void showActiveThreads()
	// {
	// log.debug("Active Threads: " + Thread.activeCount());
	// Thread[] threadarray = new Thread[Thread.activeCount()];
	// int threadCount = Thread.enumerate(threadarray);
	// log.debug("Threads: " + threadCount);
	// for (int i=0; i<threadCount; i++)
	// {
	// log.debug("ID: " + i);
	// log.debug("Name: " + threadarray[i].getName());
	// log.debug("Class: " + threadarray[i].getClass().toString());
	// log.debug("State: " + threadarray[i].getState());
	// log.debug("Daemon: " + threadarray[i].isDaemon());
	// log.debug("Thread group: " + threadarray[i].getThreadGroup());
	// log.debug("Thread priority: " + threadarray[i].getPriority());
	// log.debug("---");
	// }
	// }

	public void prepareShutdown(boolean shutdownThread, boolean shutdownHook) {
		try {
			if (!alreadyDoneShutdown) {
				// showActiveThreads();
				alreadyDoneShutdown = true;
				log.info("Shutting down JFritz..."); //$NON-NLS-1$
				closeOpenConnections();
				if (exitCode != -1
						&& exitCode != EXIT_CODE_MULTIPLE_INSTANCE_LOCK) {
					log.info("Multiple instance lock: release lock."); //$NON-NLS-1$
					removeLock();
				}

				// This must be the last call, after disposing JFritzWindow
				// nothing
				// is executed at windows-shutdown
				if (jfritz != null) {
					jfritz.prepareShutdown(shutdownThread, shutdownHook);
				}
				// showActiveThreads();
				if (JFritz.getJframe() != null) {
					Frame[] frames = Frame.getFrames();
					for (int i = 0; i < frames.length; i++) {
						log.debug("Frame: " + frames[i]);
						log.debug("Frame name: " + frames[i].getName());
						log.debug("Frame visible: " + frames[i].isVisible());
						log.debug("Frame displayable: " + frames[i].isDisplayable());
						log.debug("---");
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * function does a command line lookup, gathers all unkown entries
	 *
	 */
	private void doReverseLookup() {
		JFritzReverseLookup.doAsyncLookup(JFritz.getCallerList().getAllUnknownEntries(false), 10, 10, new IReverseLookupProgressListener() {

			@Override
			public void progress(int percent, Vector<Person> result) {
				if (result != null && result.size() > 0) {
					JFritz.getPhonebook().addEntries(result);
				}
			}
		}, new IReverseLookupFinishedWithResultListener() {

			@Override
			public void finished(Vector<Person> result) {
				if (result != null && result.size() > 0) {
					JFritz.getPhonebook().addEntries(result);
				}
			}
		});
	}

	private static void loadLanguages() {
		supported_languages = new Vector<Locale>();
		supported_languages.add(new Locale("de", "DE"));
		supported_languages.add(new Locale("en", "US"));
		supported_languages.add(new Locale("it", "IT"));
		supported_languages.add(new Locale("nl", "NL"));
		supported_languages.add(new Locale("pl", "PL"));
		supported_languages.add(new Locale("ru", "RU"));
	}

	/**
	 * Shows the password dialog
	 *
	 * @param old_password
	 * @return new_password
	 */
	public String showPasswordDialog(String old_password) {
		String password = null;
		AddressPasswordDialog p = new AddressPasswordDialog(null, true);
		p.setPass(old_password);

		if (p.showDialog()) {
			password = p.getPass();
		}
		p.dispose();
		p = null;
		return password;
	}
	
	/**
	 * This method is used to cleanup old installation files
	 */
	private void cleanupOldInstallation() {
		removeLog4jFile();
	}
	
	private void removeLog4jFile() {
		try {
			String path = JFritzDataDirectory.getInstance().getDataDirectory() + "log4j.log";
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		} catch (Throwable t) {
			log.warn("Could not delete log4j.log");
		}
	}
}