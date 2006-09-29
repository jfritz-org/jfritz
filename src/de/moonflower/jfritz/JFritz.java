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
 * BUGS: bitte bei Sourceforge nachschauen und dort auch den Status ändern
 * BUGS: http://sourceforge.net/tracker/?group_id=138196&atid=741413
 *
 * FeatureRequests: bitte bei Sourceforge nachschauen und dort auch den Status ändern
 * FeatureRequests: http://sourceforge.net/tracker/?func=browse&group_id=138196&atid=741416

 *
 * (TODO: Checken, ob alle Bibliotheken vorhanden sind)
 * (TODO: Neue Kurzwahlen von der Beta-FW ins Telefonbuch aufnehmen)
 * (TODO: Import der Anrufliste im XML-Format beim Kontextmenü einbauen)
 * TODO: Language-Files checken, ob tatsächlich alle Werte benötigt werden
 * TODO: Sonderzeichen werden in den Balloontips unter Windows nicht korrekt angezeigt. Scheint ein Windowsproblem zu sein. Lösung/Workaround noch nicht gefunden.
 * TODO: JFritz.ico mitliefern
 * TODO: Linux-Startscript mitliefern
 *
 * Roadmap:
 * JFritz 1.0
 * Bewertung - Feature
 * rob - brian
 * 10 - 10 - Fehlermeldung an den Benutzer, wenn Daten nicht auf Festplatte gespeichert werden können. (Vielleicht schon implementiert -- Rob)
 * 10 - 9 - "Verbindungsgerät" in "MSN/Rufnummer" ändern
 * 10 - 10 - Kommentarspalte im Telefonbuch
 * 10 - 7 - Webverkehr über Proxy (Was für Proxys sind gemeint: Socks 4 /5, oder HTTP(S)?)
 * 10 - 10 - Einstellen der Landes- und Ortsvorwahlen pro SIP-Account und nicht nur global (SF [ 1438932 ])
 * 9 -  8 - Export des gesamten Adressbuchs als VCard (http://www.ip-phone-forum.de/showthread.php?t=106758)
 * 9 -  9 - Einstellungen-Seiten überarbeiten.       Größe veränderbar machen!(bei modalen Dialoge geht das nicht)
 * 9 -  9 - Name für die Nebenstellen aus der Weboberfläche auslesen und zuweisen (SF [ 1498487 ])
 * 9 -  7 - Vollständiger Outlook-Support (SF [ 1498489 ])
 * 8 -  8 - Analoge Rufnummer aus der FritzBox auslesen
 * 7 -  7 - Einige Icons auslagern - unterschiedliche Icon-Packs
 * 7 -  7 - Sounddateien auslagern - unterschiedliche Sound-Packs (gute Ideen, Brian)
 * 7 -  7 - Popup und Tray-Message für Anrufmonitor anpassbar machen (Name, Nummer, Adresse, Nebenstelle, Stadt, "von Arbeit", "von SIP", anderer Text, Größe des Popups)
 * 7 -  5 - CSV-Export nicht nur mit ";", sondern auch mit "TAB", "SPACE" und "," (SF [ 1509248 ])
 * 6 -  8 - Synchronisierung von JFritz Telefonbuch und FritzBox Telefonbuch (SF [ 1494436 ])
 * 6 -  8 - Datumsfilter konfigurierbar gestalten (SF [ 1498488 ])
 * 6 -  6 - Internationalisierung abschließen, drunter Flaggencode optimieren (Nummer <-> flaggenfile Zuordnung in einer Hashmap ablegen).
 * 5 -  5 - Anrufmonitor: Anrufmonitor mächtiger machen (Aktionen nur für best. Nummern, verschiedene Aktionen, Log der Anrufe, Notizen zu einem laufenden Anruf) (SF [ 1525107 ])
 * 5 -  5 - Signalisieren der neu eingegangenen Anrufe im Tray (blinken, oder Zahl)
 * 5 -  5 - Button zum Löschen der Anrufliste
 * 5 -  7 - Mehrere FritzBoxen abfragen (SF [ 1515855 ]) Dafür sollten wir alle zugriffe auf die Box in eigene Threads unterbringen.
 *                      Dann würde JFritz sich beim Hochfahren nicht so lange verzögern, wenn die Box nicht erreichbar ist.
 * 4 -  5 - Visualisierung der aktuellen Gespräche (Frei, Nummer, Name, Dauer des Gesprächs ...)
 * 4 -  4 - Plugins (Mögliche Plugins: Drucken, Anrufmonitor)
 * 4 -  2 - Import vom Tool Fritzinfo (http://www.ip-phone-forum.de/showthread.php?t=101090)
 * 4 -  4 - Begrenzen der Anzeige der Anrufe in der Anrufliste (z.B. maximal 100 Einträge)
 * 4 -  4 - CSV-Export anpassbar machen (wie bei Thunderbird).
 * 4 -  4 - Registrierstatus der VoIP-Provider (SF [ 1315159 ])
 * 4 -  1 - Einstellen der Farben, Symbolleisten, Schriftart, -größe (SF [ 1458892 ])
 * 4 -  3 - Exportieren/Anzeige der Anrufliste nach Monaten getrennt
 * 3 -  2 - SQL-Anbindung (SF [ 1515305 ])
 * 3 -  3 - Anzeige des letzten Telefonats nicht nur abhängig von der Standardnummer und anzeige der gesprochenen Minuten pro Telefonbucheintrag
 * 3 -  2 - Tastaturkürzel für Aktionen sollen editierbar sein
 * 3 -  2 - Netzwerkfunktionen (Client/Server) (SF [ 1485417 ]) Das wird das allerschwierigste von allen, und am meisten Planung benötigen.
 * 3 -  2 - Spalte "Privatkontakt" in CSV-Liste hinzufügen (SF [ 1480617 ])
 * 2 -  2 - 64-bit Unterstützung
 * 1 -  1 - LDAP-Anbindung
 * 1 -  1 - SMS Benachrichtigung (Über Festnetzgateway)
 * 1 -  1 - Style-Sheet für die Anzeige der Anrufliste als HTML
 * 1 -  1 - Einige ausgewählte Statisken über die DSL benutzung, damit JFritz eine komplette Lösung für die Fritz!Box anbietet.
 * 1 -  1 - umstieg auf Mustang, damit verbunden jdic rauswerfen und nur noch Java-interne Bibliotheken nutzen
 * 					Gut, dann können wir endlich diese ganze String.indexOf('@') > 0 rausschmeißen :)
 *                  Das würde aber heissen, dass wir nicht mehr zu Java 1.4 kompatibel sind. Einige Plattformen (wie MAC) werden
 *                      bestimmt noch über längere Zeit kein Java SE 6 anbieten. -- Rob
 * 1 -  1 - Statistikfunktionen
 * 1 -  1 - WAN IP beim Tray-Icon anzeigen lassen ?
 * 1 -  1 - Skinns (SF [ 1471202 ])
 * 1 - (-1) - Unterstützung für das Adressbuch von Lotus Notes (SF [ 1445456 ]) (Ich bin dagegen, denn man bräuchte nochmal so ne Plugin wie bei Outlook,
 * 						nur ich schätze es gibt gar keins => wir müssten eine schreiben.
 * 						Habe das programm bei mir in der Arbeit, und ich hasse es. Ich werde nicht mehr Zeit als notwendig ist damit verbringen.
 * 1 -  1 - Bild / Rufton / Farbe eines bestimmten Anrufers
 *
 *
 * CHANGELOG:
 *
 * Jfritz 0.6.2
 * TODO:
 * - Alle Zugriffe auf FritzBox in eigenen Threads
 * - Bug: Eingabe der IP-Nummer nach Ruhezustand
 * - Markieren der Zeilen per STRG auch in der "Teilnehmer"-Spalte
 * - Ändern der Standardrufnummer per Häckchen führt nicht zur Speicherung, erst wenn man noch ein Datum ändert
 * - Filter für Nebenstelle (Port) kombiniert mit eingetragenem Namen in der Weboberfläche
 * - Copy & Paste für Spalteneinträge (in Anrufliste, Telefonbuch und Kurzwahlliste)
 * - Importierte Rufnummern auf Sonderzeichen ( -, /, (, ) ) überprüfen
 * - Durchwahlnummern vor Zentrale-Nummern bei der Anzeige bevorzugen (sollte eigentlich gehen, aber scheint einen Bug zu haben)
 * - Schnittstelle zu externen Inverssuche-Programmen
 * - Bug: Neue Telefonnummern in PersonDialog der Anrufliste werden nicht gespeichert
 * - Bug: Importieren von Thunderbird-Einträgen (es werden nur die ersten 5 Felder der CSV-Datei erkannt)
 * - Andere Anrufmonitore noch an die neuen Listener anpassen und TestCases schreiben
 * - Kurzwahlliste sortierbar und Spaltenreihenfolge änderbar
 * - Bug "Doppelt erfasste Anrufe" behoben?
 * - http://www.ip-phone-forum.de/showthread.php?t=112348
 * - 0900 Nummern werden nicht korrekt erkannt http://www.ip-phone-forum.de/showthread.php?t=114325 => Liste mit Call-By-Call Vorwahlen
 * - Überprüfen, geht wohl nicht mehr: Rückwärtssuche für Österreich über dasoertliche.de wieder eingebaut
 * TODO-ENDE
 *
 * - Neue Strings:
 * 	new_version
 * 	new_version_text
 *  new_version_quit
 *  check_for_new_version_after_start
 *  no_new_version_found
 *  update_JFritz
 *
 * - Suchfeld in Anrufliste umfasst nun auch die Call-By-Call vorwahlen
 * - Bugfix: Suche nach Rufnummern im internationalen Format
 * - Neu: JFritz-Fenster wird nun korrekt wiederhergestellt (maximiert...). Neues Property: window.state
 * - Neu: Falls Ort per ReverseLookup nicht gefunden wird, wird anhand einer Tabelle der passende Ort zu einer Vorwahl eingetragen werden (Österreich)
 * - Neu: Falls Ort per ReverseLookup nicht gefunden wird, wird anhand einer Tabelle der passende Ort zu einer Vorwahl eingetragen werden Deutschland (SF [ 1315144 ])
 * - Bugfix: Jetzt werden IP-Addressen von den Boxen in der Einstellungen angezeigt. Man kann jetzt Fehlerfrei zwei boxes im gleichen Netz haben.
 * - Neu: Rückwärtssuche für die USA über www.whitepages.com, danke an Reiner Gebhardt
 * - Neu: Menüeintrag ->JFritz aktualisieren
 * - Internationaler FreeCall 00800 (http://www.ip-phone-forum.de/showthread.php?t=111645)
 * - Datumsfilter "aktueller-Tag" sollte auch immer den aktuellen Tag anzeigen. (SF [ 1530172 ])
 * - INTERN: Filter der Anrufliste in neues Package. Abstrakte Klasse CallFilter
 * - INTERN: Statische Methoden in JFritz.java => keine jfritz-Referenzen in den anderen Klassen notwendig
 * - INTERN: Diverse JUnit-TestCases
 * - INTERN: Neue Klasse CallMonitoring, die alle aktuellen Anrufe verwaltet und die Anrufinformation auf den Bildschirm bringt
 * - INTERN: Anrufmonitore in neues Package callmonitor gepackt und umbenannt
 * - INTERN: Anzeige der Anrufe, die vom Anrufmonitor erkannt werden, über Listener. Abholen der Anrufliste nach dem Gesprächsende nun über den DisconnectMonitor
 * - Neu: Unterstützung für die "Labor-Firmware" (Firmwareerkennung und CSV-Import)
 * - Neu: Automatisches Update von JFritz auf neue Version. TODO: Checken, ob es auch mit .so und .dll funktioniert TODO: Am Ende der Update-Prozedur JFritz nicht beenden, sondern neu starten. Blos wie?
 * - Wahlhilfe: Immer mit Ortsvorwahl wählen (getShortNumber in getAreaNumber verändert)
 * - Neu: Unterstützung für neue englische Firmware xx.04.20
 * - INTERN: Buildfile überarbeitet. TODO: release und junit anpassen
 * - Neu: Rückwärtssuche nicht mehr über DasOertliche.de sondern über dastelefonbuch.de
 *
 * JFritz 0.6.1
 * - Neue Strings:
 *		browse
 *		save_directory
 *		delete_duplicate_phonebook_entries
 *		delete_duplicate_phonebook_entries_confirm_msg
 *		delete_duplicate_phonebook_entries_inform_msg
 *		box.port
 *		config_wizard_info1
 *		config_wizard_info2
 *		config_wizard_info3
 *		config_wizard_info4
 *		config_wizard
 *		popup_delay
 *      dial_prefix
 *
 * - Intern: Multiple-Instance-Lock nun Dateibasiert und nicht mehr als Property. (JFritz.LOCK_FILE)
 * - Neu: Meldung bei neuer JFritz-Version
 * - Neu: Flaggen werden bei bekannten Ländervorwählen angezeigt anstelle vom Weltkugel, für bekannte Länder siehe PhoneNumber.java
 * - Bugfix: SIP-Routen behalten ihre historische Zuordnung
 * - Neu: Neuer Kommandozeilenparameter: -r, führt eine Rückwärtssuche aus und beendet sich
 * - Neu: Rückwärtssuche für Frankreich über http://www.annuaireinverse.com, wird automatisch aufgerufen
 * - Neu: Rückwärtssuche für die Niederlande über http://www.gebeld.nl/content.asp?zoek=numm, wird automatisch aufgerufen
 * - Neu: Rückwärtssuche für Italien über www.paginebianche.it, wird automatisch aufgerufen
 * - Neu: Rückwärtssuche für die Schweiz über tel.search.ch, JFritz ruft automatisch die richtige Rückwärtssuche auf.
 * - Neu: Dummy-Telefonbucheinträge werden gelöscht, falls ein Eintrag mit derselben Nummer existiert
 * - Neu: Anrufe mit einer AKZ werden jetzt richtig verarbeitet.
 * - Neu: Der Speicherordner kann jetzt frei gewählt werden, bleibt nach dem Beenden erhalten. SF-Tracker [1248965]
 * - Bugfix: Die Sprachauswahlbox zeigt jetzt auch unter Linux Flaggen-Icons an.
 * - Bugfix: Das Telefonbuch wird nach einem erfolgreichen Outlook-Import sofort gespeichert. SF-Tracker [ 1503185 ]
 * - Neu: Zeit, bis Popup-Nachrichten ausgeblendet sind, einstellbar gemacht (Zeit von 0 bedeutet nie schließen) SF-Request Nr: [1340678] [1518330]
 * - Bugfix: JFritz kann jetzt von einem beliebigen Verzeichnis aus aufgerufen (bestätigt unter Linux, Windows, Mac??)
 * - Neu: Rückwärtssuche auch für Handynummern
 * - Neu: Wählhilfe merkt sich den zuletzt benutzen Port
 * - Neu: JFritz kann jetzt beliebige Nummer mit der Wahlhilfe wählen (noch nicht ausführlich getestet, z.B. funktionieren auch die Tastencode?)
 * - Bugfix: Kurzwahlen werden jetzt korrekt geparst beim Abholen der Anrufliste
 * - Neu: Port einstellbar
 * - Neu: Konfigurationswizard für Erstbenutzer
 * - Neu: Logfiles werden jetzt mittels Stream redirection geschrieben (heißt auch die Exceptions werden in den Logfiles aufgenommen :) )
 * - Neu: Entfernen doppelter Einträge beim Telefonbuch
 * - Neu: Automatisches Scrollen zum selektierten Telefonbucheintrag
 * - Neu: Englische Firmware wird unterstützt
 * - Intern: Firmware wird beim Start erkannt und in JFritz.firmware gespeichert. Zugriff nicht mehr über JFritz.getProperties("box.firmware") sondern über JFritz.getFirmware()
 * - Bugfix: Kurzwahlen werden wieder korrekt abgeholt
 * - Bugfix: Standardtelefonnummern können wieder geändert werden
 * - Bugfix: Problem mit dem Holen der Anrufliste behoben
 * - Bugfix: Nebenstellenbezeichnungen und Route können jetzt Sonderzeichen enthalten
 * - Bugfix: Anzeige eines analogen Anrufs beim Anrufmonitor
 * - Bugfix: PersonDialog ("Person editieren") wird nun mittig zum JFritz.JFrame angezeigt - SF.net-Request:[1503523] Adress-/Telefonbuch
 * - Neu: Default- und Close-Button für PersonDialog ("Person editieren"), Icon (JFritz) gesetzt
 * - Bugfix: Wahlhilfe: Anwahl aller analogen Telefone konnte nicht gehen -> Tippfehler in JFritzUtils: JFritz.getMessage("analoge_telephones_all") -> korrigiert in JFritz.getMessage("analog_telephones_all")
 * - Neu: Default-Button bei Rückfrage 'Box-Anruferliste löschen' geändert auf 'Nein'
 * - Neu: Berücksichtigung der Metal-Decorations bei Dialogen
 * - Intern: Funktionen, die mit der Kommunikation mit der FritzBox zu tun hatten, in eine neue Klasse FritzBox exportiert.
 * - Intern: CallDialog: Auswahl der Nummern wiederhergestellt, editierbare JComboBox/JTextField (je nach Anzahl vorhandener Nummern)
 * - Neu: Default- und Close-Button für CallDialog ("Anrufen"), Icon (JFritz) gesetzt
 * - Neu: Überarbeitung der Dialoge bzgl. OK/Cancel, Icon, Position
 *   jfritz.dialogs.config.CallmessageDialog,
 *   jfritz.dialogs.config.ConfigDialog,
 *   jfritz.dialogs.config.FRITZBOXConfigDialog,
 *   jfritz.dialogs.config.SipConfigDialog,
 *   jfritz.dialogs.config.SyslogConfigDialog,
 *   jfritz.dialogs.config.TelnetConfigDialog,
 *   dialogs.config.YacConfigDialog,
 *
 *   jfritz.dialogs.simple.AddressPasswordDialog,
 *   jfritz.dialogs.stats.StatsDialog,
 *
 *   jfritz.callerlist.CallDialog,
 *
 *   jfritz.utils.ImportOutlookContacts,
 *   jfritz.utils.NoticeDialog
 *
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
 *                      ich ständig die gleichen Ports, fllt es nicht weiter auf.
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
 * - Zugriff auf SipProvider über JFritz.getSIPProviderTableModel()
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
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
import javax.swing.table.TableColumn;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.callmonitor.CallMonitorInterface;
import de.moonflower.jfritz.callmonitor.CallMonitorList;
import de.moonflower.jfritz.callmonitor.DisconnectMonitor;
import de.moonflower.jfritz.callmonitor.DisplayCallsMonitor;
import de.moonflower.jfritz.dialogs.configwizard.ConfigWizard;
import de.moonflower.jfritz.dialogs.phonebook.PhoneBook;
import de.moonflower.jfritz.dialogs.simple.MessageDlg;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.FritzBox;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.CLIOption;
import de.moonflower.jfritz.utils.CLIOptions;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;
import de.moonflower.jfritz.utils.network.VersionCheckThread;
import de.moonflower.jfritz.utils.network.SSDPdiscoverThread;

/**
 * @author Arno Willig
 *
 */
public final class JFritz {

    // when changing this, don't forget to check the resource bundles!!
    public final static String PROGRAM_NAME = "JFritz"; //$NON-NLS-1$

    public final static String PROGRAM_VERSION = "0.6.2"; //$NON-NLS-1$

    public final static String PROGRAM_URL = "http://www.jfritz.org/"; //$NON-NLS-1$

    public final static String PROGRAM_SECRET = "jFrItZsEcReT"; //$NON-NLS-1$

    public final static String DOCUMENTATION_URL = "http://www.jfritz.org/hilfe/"; //$NON-NLS-1$

    public final static String CVS_TAG = "$Id: JFritz.java,v 1.352 2006/09/29 12:00:28 marc0815 Exp $"; //$NON-NLS-1$

    public final static String PROGRAM_AUTHOR = "Arno Willig <akw@thinkwiki.org>"; //$NON-NLS-1$

    public final static String USER_DIR = System.getProperty("user.home")
            + File.separator + ".jfritz";

    public final static String USER_JFRITZ_FILE = "jfritz.txt";

    public static String SAVE_DIR = System.getProperty("user.dir")
            + File.separator;

    public static String SAVE_DIR_TEXT = "Save_Directory=";

    public final static String PROPERTIES_FILE = "jfritz.properties.xml"; //$NON-NLS-1$

    public final static String CALLS_FILE = "jfritz.calls.xml"; //$NON-NLS-1$

    public final static String QUICKDIALS_FILE = "jfritz.quickdials.xml"; //$NON-NLS-1$

    public final static String PHONEBOOK_FILE = "jfritz.phonebook.xml"; //$NON-NLS-1$

    public final static String SIPPROVIDER_FILE = "jfritz.sipprovider.xml"; //$NON-NLS-1$

    public final static String CALLS_CSV_FILE = "calls.csv"; //$NON-NLS-1$

    public final static String PHONEBOOK_CSV_FILE = "contacts.csv"; //$NON-NLS-1$

    public final static String LOCK_FILE = ".lock"; //$NON-NLS-1$

    public final static int SSDP_TIMEOUT = 1000;

    public final static int SSDP_MAX_BOXES = 3;

    public static boolean SYSTRAY_SUPPORT = false;

    public static boolean checkSystray = true;

    private static JFritzProperties defaultProperties;

    private static JFritzProperties properties;

    private static ResourceBundle localeMeanings;

    private static ResourceBundle messages;

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

    private static boolean isRunning = false;

    private static Locale locale;

    private static boolean showConfWizard = false;

    private static boolean doReverseLookup = false;

    private static FritzBox fritzBox;

    private static boolean enableInstanceControl = true;

    private static int oldFrameState; // saves old frame state to restore old

    public static CallMonitorList callMonitorList;

    // state

    /**
     * Main method for starting JFritz
     *
     * LAST MODIFIED: Brian Jensen 04.06.06 added option to disable mulitple
     * instance control added a new parameter switch: -w
     *
     * @param args
     *            Program arguments (-h -v ...)
     *
     */
    public static void main(String[] args) {
        System.out.println(PROGRAM_NAME + " v" + PROGRAM_VERSION //$NON-NLS-1$
                + " (c) 2005 by " + PROGRAM_AUTHOR); //$NON-NLS-1$
        Thread.currentThread().setPriority(5);
        loadSaveDir();

        boolean fetchCalls = false;
        boolean clearList = false;
        boolean csvExport = false;
        boolean foreign = false;
        String csvFileName = ""; //$NON-NLS-1$

        // TODO: If we ever make different packages for different languages
        // change the default language here
        locale = new Locale("de", "DE"); //$NON-NLS-1$,  //$NON-NLS-2$
        CLIOptions options = new CLIOptions();

        options.addOption('h', "help", null, "This short description"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('v', "verbose", null, "Turn on debug information"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('s', "systray", null, "Turn on systray support"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('n', "nosystray", null, "Turn off systray support"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('f', "fetch", null, "Fetch new calls and exit"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('d', "delete_on_box", null, //$NON-NLS-1$,  //$NON-NLS-2$
                "Delete callerlist of the Fritz!Box."); //$NON-NLS-1$
        options
                .addOption(
                        'b',
                        "backup", null, "Creates a backup of all xml-Files in the directory 'backup'"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        options.addOption('c', "clear_list", null, //$NON-NLS-1$,  //$NON-NLS-2$
                "Clears Caller List and exit"); //$NON-NLS-1$
        options.addOption('e', "export", "filename", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                "Fetch calls and export to CSV file."); //$NON-NLS-1$
        options
                .addOption('z', "exportForeign", null, //$NON-NLS-1$,  //$NON-NLS-2$
                        "Write phonebooks compatible to BIT FBF Dialer and some other callmonitors."); //$NON-NLS-1$
        options.addOption('l', "logfile", "filename", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                "Writes debug messages to logfile"); //$NON-NLS-1$,
        options.addOption('p', "priority", "level", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                "Set program priority [1..10]"); //$NON-NLS-1$
        options
                .addOption(
                        'w',
                        "without-control", null, //$NON-NLS-1$,  //$NON-NLS-2$
                        "Turns off multiple instance control. DON'T USE, unless you know what your are doing"); //$NON-NLS-1$
        options
                .addOption('r', "reverse-lookup", null,
                        "Do a reverse lookup and exit. Can be used together with -e -f and -z");
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
                    System.err
                            .println(JFritz.getMessage("parameter_not_found")); //$NON-NLS-1$
                    System.exit(0);
                }
                break;
            case 'd': //$NON-NLS-1$
                // enableInstanceControl = false; // ungütig, GUI wird nicht
                // gestartet
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
                    System.err
                            .println(JFritz.getMessage("parameter_not_found")); //$NON-NLS-1$
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
            case 'r':
                enableInstanceControl = false;
                doReverseLookup = true;
                break;

            case 'p': //$NON-NLS-1$
                String priority = option.getParameter();
                if (priority == null || priority.equals("")) { //$NON-NLS-1$
                    System.err
                            .println(JFritz.getMessage("parameter_not_found")); //$NON-NLS-1$
                    System.exit(0);
                } else {
                    try {
                        int level = Integer.parseInt(priority);
                        Thread.currentThread().setPriority(level);
                        Debug.msg("Set priority to level " + priority); //$NON-NLS-1$
                    } catch (NumberFormatException nfe) {
                        System.err.println(JFritz
                                .getMessage("parameter_wrong_priority")); //$NON-NLS-1$
                        System.exit(0);
                    } catch (IllegalArgumentException iae) {
                        System.err.println(JFritz
                                .getMessage("parameter_wrong_priority")); //$NON-NLS-1$
                        System.exit(0);
                    }
                    break;
                }
            default:
                break;
            }
        }

        new JFritz(fetchCalls, csvExport, csvFileName, clearList, foreign);

    }

    /**
     * Constructs JFritz object
     *
     * @author Benjamin Schmitt
     */
    public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
            boolean clearList) {
        this(fetchCalls, csvExport, csvFileName, clearList, false);
    }

    /**
     * Constructs JFritz object
     */
    public JFritz(boolean fetchCalls, boolean csvExport, String csvFileName,
            boolean clearList, boolean writeForeignFormats) {

        Debug.msg("Save Dir: " + SAVE_DIR);
        loadProperties();
        loadMessages(new Locale(JFritz.getProperty("locale", "de_DE"))); //$NON-NLS-1$,  //$NON-NLS-2$
        loadLocaleMeanings(new Locale("int", "INT"));

        if (JFritzUtils.parseBoolean(properties.getProperty(
                "option.createBackup", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            doBackup();
        }

        // make sure there is a plus on the country code, or else the number
        // scheme won't work
        if (!JFritz.getProperty("country.code").startsWith("+"))
            JFritz.setProperty("country.code", "+"
                    + JFritz.getProperty("country.code"));

        if (enableInstanceControl) {
            // check isRunning and exit or set lock
            File f = new File(JFritz.SAVE_DIR + JFritz.LOCK_FILE);
            isRunning = f.exists();

            if (!isRunning) {
                Debug.msg("Multiple instance lock: set lock."); //$NON-NLS-1$
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    Debug.err("Could not set instance lock");
                }
            } else {
                Debug
                        .msg("Multiple instance lock: Another instance is already running."); //$NON-NLS-1$
                int answer = JOptionPane
                        .showConfirmDialog(
                                null,
                                JFritz.getMessage("lock_error_dialog1") //$NON-NLS-1$
                                        + JFritz
                                                .getMessage("lock_error_dialog2") //$NON-NLS-1$
                                        + JFritz
                                                .getMessage("lock_error_dialog3") //$NON-NLS-1$
                                        + JFritz
                                                .getMessage("lock_error_dialog4"), //$NON-NLS-1$
                                JFritz.getMessage("information"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
                if (answer == JOptionPane.YES_OPTION) {
                    Debug
                            .msg("Multiple instance lock: User decided to shut down this instance."); //$NON-NLS-1$
                    exit(0);
                } else {
                    Debug
                            .msg("Multiple instance lock: User decided NOT to shut down this instance."); //$NON-NLS-1$
                }
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
            new MacHandler();
        }

        // loads various country specific number settings and tables
        loadNumberSettings();

        fritzBox = new FritzBox(JFritz.getProperty(
                "box.address", "192.168.178.1"), Encryption //$NON-NLS-1$,  //$NON-NLS-2$
                .decrypt(JFritz.getProperty("box.password", Encryption //$NON-NLS-1$
                        .encrypt(""))), JFritz.getProperty("box.port", "80")); //$NON-NLS-1$

        phonebook = new PhoneBook();
        phonebook.loadFromXMLFile(SAVE_DIR + PHONEBOOK_FILE);

        sipprovider = new SipProviderTableModel();
        sipprovider.loadFromXMLFile(SAVE_DIR + SIPPROVIDER_FILE);

        callerlist = new CallerList();
        callerlist.loadFromXMLFile(SAVE_DIR + CALLS_FILE);

        callMonitorList = new CallMonitorList();
        callMonitorList.addEventListener(new DisplayCallsMonitor());
        callMonitorList.addEventListener(new DisconnectMonitor());

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
                if (doReverseLookup)
                    reverseLookup();

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
            if (doReverseLookup)
                reverseLookup();

            System.exit(0);
        }
        if (clearList) {
            Debug.msg("Clearing Call List"); //$NON-NLS-1$
            callerlist.clearList();
            System.exit(0);
        }
        if (writeForeignFormats) {
            if (doReverseLookup)
                reverseLookup();

            phonebook.saveToBITFBFDialerFormat("bitbook.dat"); //$NON-NLS-1$
            phonebook.saveToCallMonitorFormat("CallMonitor.adr"); //$NON-NLS-1$
        }

        if (doReverseLookup) {
            reverseLookup();
            System.exit(0);
        }

        if (JFritz
                .getProperty(
                        "lookandfeel", UIManager.getSystemLookAndFeelClassName()).endsWith("MetalLookAndFeel")) { //$NON-NLS-1$,  //$NON-NLS-2$
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true); // uses L&F
            // decorations for
            // dialogs
        }

        Debug.msg("New instance of JFrame"); //$NON-NLS-1$
        try{
        	jframe = new JFritzWindow();
        }catch(WrongPasswordException wpe){
        	exit(0);
        }
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

        // check the version and display a message if newer version is
        // available
        if (!showConfWizard
                && JFritzUtils.parseBoolean(JFritz.getProperty(
                        "option.checkNewVersionAfterStart",//$NON-NLS-1$
                        "false"))) {//$NON-NLS-1$
            VersionCheckThread vct = new VersionCheckThread(false);
            vct.run();
        }
        jframe.checkStartOptions();

        javax.swing.SwingUtilities.invokeLater(jframe);

        startWatchdog();
    }

    /**
     * This constructor is used for JUnit based testing suites Only the default
     * settings are loaded for this jfritz object
     *
     * @author brian jensen
     */
    public JFritz() {
        loadProperties();
        loadMessages(new Locale(JFritz.getProperty("locale", "de_DE"))); //$NON-NLS-1$,  //$NON-NLS-2$
        loadLocaleMeanings(new Locale("int", "INT"));

        // make sure there is a plus on the country code, or else the number
        // scheme won't work
        if (!JFritz.getProperty("country.code").startsWith("+"))
            JFritz.setProperty("country.code", "+"
                    + JFritz.getProperty("country.code"));

        // loadSounds();

        // loads various country specific number settings and tables
        loadNumberSettings();

        fritzBox = new FritzBox(
        JFritz.getProperty("box.address", "192.168.178.1"), Encryption //$NON-NLS-1$, //$NON-NLS-2$
        .decrypt(JFritz.getProperty("box.password", Encryption //$NON-NLS-1$
        .encrypt(""))), JFritz.getProperty("box.port", "80")); // //$NON-NLS-1$ $NON-NLS-2$

        phonebook = new PhoneBook();
        // phonebook.loadFromXMLFile(SAVE_DIR + PHONEBOOK_FILE);

        sipprovider = new SipProviderTableModel();
        // sipprovider.loadFromXMLFile(SAVE_DIR + SIPPROVIDER_FILE);

        callerlist = new CallerList();
        // callerlist.loadFromXMLFile(SAVE_DIR + CALLS_FILE);

    }

    /**
     * Loads resource messages
     *
     * @param locale
     */
    private static void loadMessages(Locale locale) {
        try {
            messages = ResourceBundle.getBundle("jfritz", locale);//$NON-NLS-1$
        } catch (MissingResourceException e) {
            Debug
                    .err("Can't find i18n resource! (\"jfritz_" + locale + ".properties\")");//$NON-NLS-1$
            JOptionPane.showMessageDialog(null, JFritz.PROGRAM_NAME + " v"//$NON-NLS-1$
                    + JFritz.PROGRAM_VERSION
                    + "\n\nCannot find the language file \"jfritz_" + locale
                    + ".properties\"!" + "\nProgram will exit!");//$NON-NLS-1$
            System.exit(0);
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
            Debug.err("Can't find locale Meanings resource!");//$NON-NLS-1$
        }
    }

    /**
     * Loads properties from xml files
     */
    public static void loadProperties() {
        defaultProperties = new JFritzProperties();
        properties = new JFritzProperties(defaultProperties);

        // Default properties
        defaultProperties.setProperty("box.address", "192.168.178.1");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("box.password", Encryption.encrypt(""));//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("box.port", "80");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("country.prefix", "00");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("area.prefix", "0");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("country.code", "+49");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("area.code", "441");//$NON-NLS-1$, //$NON-NLS-2$
        defaultProperties.setProperty("fetch.timer", "5");//$NON-NLS-1$, //$NON-NLS-2$

        try {
            properties.loadFromXML(SAVE_DIR + JFritz.PROPERTIES_FILE);
            replaceOldProperties();
        } catch (FileNotFoundException e) {
            Debug.err("File " + SAVE_DIR + JFritz.PROPERTIES_FILE //$NON-NLS-1$
                    + " not found => showing config wizard"); //$NON-NLS-1$
            showConfWizard = true;
        } catch (IOException ioe) {
            Debug.err("File " + SAVE_DIR + JFritz.PROPERTIES_FILE //$NON-NLS-1$
                    + " not readable => showing config wizard"); //$NON-NLS-1$
            showConfWizard = true;
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

    /**
     * Creates the tray icon menu
     */
    private static void createTrayMenu() {
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

        trayIcon = new TrayIcon(icon, JFritz.PROGRAM_NAME, menu); //$NON-NLS-1$
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
    private static void replaceOldProperties() {
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
            properties.setProperty("column.participant.width", //$NON-NLS-1$
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
            properties.setProperty("column.duration.width", //$NON-NLS-1$
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
            fritzBox.clearListOnFritzBox();
            Debug.msg("Clearing done"); //$NON-NLS-1$
        } catch (WrongPasswordException e) {
            Debug.err("Wrong password, can not delete callerlist on Box."); //$NON-NLS-1$
        } catch (IOException e) {
            Debug
                    .err("IOException while deleting callerlist on box (wrong IP-address?)."); //$NON-NLS-1$
        }
    }

    /**
     * Saves properties to xml files
     */
    public static void saveProperties() {

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
            properties.storeToXML(JFritz.SAVE_DIR + JFritz.PROPERTIES_FILE);
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
            msgDialog.showMessage(msg, Long.parseLong(JFritz.getProperty(
                    "option.popupDelay", "10")) * 1000);
            break;
        }
        case 2: {
            if (trayIcon != null)
                trayIcon.displayMessage(JFritz.PROGRAM_NAME, msg,
                        TrayIcon.INFO_MESSAGE_TYPE);
            else if (trayIcon == null) {
                MessageDlg msgDialog = new MessageDlg();
                msgDialog.showMessage(msg, Long.parseLong(JFritz.getProperty(
                        "option.popupDelay", "10")) * 1000);
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
        if (SYSTRAY_SUPPORT) {
            trayIcon.displayMessage(JFritz.PROGRAM_NAME, msg,
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
     * @return Returns an internationalized message. Last modified: 26.04.06 by
     *         Bastian
     */
    public static String getMessage(String msg) {
        String i18n = ""; //$NON-NLS-1$
        try {
            if (!messages.getString(msg).equals("")) {
                i18n = messages.getString(msg);
            } else {
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
            if (!localeMeanings.getString(msg).equals("")) {
                localeMeaning = localeMeanings.getString(msg);
            } else {
                localeMeaning = msg;
            }
        } catch (MissingResourceException e) {
            Debug.err("Can't find resource string for " + msg); //$NON-NLS-1$
            localeMeaning = msg;
        } catch (NullPointerException e) {
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

    private static void doBackup() {
        CopyFile backup = new CopyFile();
        backup.copy(".", "xml"); //$NON-NLS-1$,  //$NON-NLS-2$
    }

    /**
     * @Brian Jensen This function changes the state of the ResourceBundle
     *        object currently available locales: see lang subdirectory Then it
     *        destroys the old window and redraws a new one with new locale
     *
     * @param l
     *            the locale to change the language to
     */
    public static void createNewWindow(Locale l) {
        locale = l;

        Debug.msg("Loading new locale"); //$NON-NLS-1$
        loadMessages(locale);

        refreshWindow();

    }

    /**
     * @ Bastian Schaefer
     *
     * Destroys and repaints the Main Frame.
     *
     */

    public static void refreshWindow() {
        saveProperties();
        jframe.dispose();
        javax.swing.SwingUtilities.invokeLater(jframe);
        try{
        	jframe = new JFritzWindow();
        }catch(WrongPasswordException wpe){
        	exit(0);
        }
        javax.swing.SwingUtilities.invokeLater(jframe);
        jframe.checkOptions();
        javax.swing.SwingUtilities.invokeLater(jframe);
        jframe.setVisible(true);

    }

    private static void exit(int i) {
    	//TODO maybe some cleanup is needed
		System.exit(i);
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

    /**
     * Funktion reads the user specified save location from a simple text file
     * If any error occurs the function bails out and uses the current directory
     * as the save dir, as the functionality was in JFritz < 0.6.0
     *
     * @author Brian Jensen
     *
     */
    public static void loadSaveDir() {
    	BufferedReader br=null;
    	try {
            br = new BufferedReader(new FileReader(USER_DIR
                    + File.separator + USER_JFRITZ_FILE));
            String line = br.readLine();
            if(line == null){
            	br.close();
            	Debug.msg("File"+USER_DIR+ File.separator + USER_JFRITZ_FILE+"empty");
            }
            String[] entries = line.split("=");
            if (!entries[1].equals("")) {
                SAVE_DIR = entries[1];
                File file = new File(SAVE_DIR);
                if (!file.isDirectory())
                    SAVE_DIR = System.getProperty("user.dir") + File.separator;
                else if (!SAVE_DIR.endsWith(File.separator))
                    SAVE_DIR = SAVE_DIR + File.separator;
            }
            Debug.msg("Save directory: " + SAVE_DIR);
        } catch (FileNotFoundException e) {
            Debug
                    .msg("Error processing the user save location(File not found), using defaults");
            // If something happens, just bail out and use the standard dir
        }catch(IOException ioe){
            Debug.msg("Error processing the user save location, using defaults");
        }finally{
        	try{
        		if ( br != null )
        			br.close();
        	}catch(IOException ioe){
                Debug.msg("Error closing stream");
        	}
        }
    }

    /**
     * This function writes a file $HOME/.jfritz/jfritz.txt, which contains the
     * location of the folder containing jfritz's data If the dir $HOME/.jfritz
     * does not exist, it is created if the save location isnt a directory, then
     * the default save directory is used
     *
     * @author Brian Jensen
     *
     */
    public static void writeSaveDir() {
        try {

            // if $HOME/.jfritz doesn't exist create it
            File file = new File(USER_DIR);
            if (!file.isDirectory() && !file.isFile())
                file.mkdir();

            BufferedWriter bw = new BufferedWriter(new FileWriter(USER_DIR
                    + File.separator + USER_JFRITZ_FILE, false));

            // make sure the user didn't screw something up
            if (!SAVE_DIR.endsWith(File.separator))
                SAVE_DIR = SAVE_DIR + File.separator;

            file = new File(SAVE_DIR);
            if (!file.isDirectory())
                SAVE_DIR = System.getProperty("user.dir") + File.separator;

            bw.write(SAVE_DIR_TEXT + SAVE_DIR);
            bw.newLine();
            bw.close();
            Debug.msg("Successfully wrote save dir to disk");

        } catch (Exception e) {
            Debug
                    .err("Error writing save dir to disk, reverting back to default save dir");
            SAVE_DIR = System.getProperty("user.dir") + File.separator;
            // if there was an error, bail out and revert to the default save
            // location
        }
    }

    private static void reverseLookup() {
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
            getPhonebook().saveToXMLFile(
                    JFritz.SAVE_DIR + JFritz.PHONEBOOK_FILE);

    }

    public static boolean isInstanceControlEnabled() {
        return enableInstanceControl;
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