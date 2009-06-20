package de.moonflower.jfritz.box;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
//import de.moonflower.jfritz.struct.PhonePort;

public interface BoxInterface {

	// Login, basierend auf Passwort und/oder SessionID
//	public boolean login() throws WrongPasswordException, IOException, InvalidFirmwareException;

	// Anrufliste
//	public Vector<Call> getCallerList(Vector<IProgressListener> callListProgressListener)
//			throws IOException, MalformedURLException;
//	public void clearCallerList() throws WrongPasswordException, IOException;

	//@TODO Anrufe sperren

	// Wählhilfe
//	public boolean isDialAssistEnabled();
//	public void setDialAssistEnabled(boolean state);
//	public PhonePort[] getAvailablePorts();
//	public void doCall(String number, PhonePort port) throws WrongPasswordException, IOException;
//	public void hangup() throws WrongPasswordException, IOException;

	// Telefonbuch
//	public Vector<QuickDial> getQuickDialList() throws WrongPasswordException,
//													   IOException, InvalidFirmwareException;
//	public void addQuickDialEntry(QuickDial entry);
//	public void deleteQuickDialEntry();
//	public void editQuickDialEntry();

	// SIP-Liste
//	public Vector<SipProvider> getSipList() throws WrongPasswordException,
//												   IOException, InvalidFirmwareException;

	// Interne und Externe Rufnummern
//	public void getInternalNumbersList(); // Nur interne Nummern, DECT, SIP
//	public void getExternalNumbersList(); // Analog, ISDN und SIP

	// Anrufmonitor
//	public void setCallMonitorEnabled(boolean state); // (de-)aktiviert den Anrufmonitor auf der Box

	// UPnP
//	public void getInternetStats();
//	public void getWebService();
//	public void getStatusInfo();
//	public void getExternalIpAddress();
//	public void getCommonLinkInfo();
//	public void getInfo();
//	public void getAutoConfig();
//	public void getConnectionTypeInfo();
//	public void getGenericPortMappingEntry();

	// Internet
	// |-- Zugangsdaten
	// |-- Kindersicherung
	// |-- Freigaben
	// |   |-- PortWeiterleitungen
	// |   |-- RemoteAdministration
	// |   |-- DynDns
	// |   |-- VPN
	// |-- DSL-Informationen
	// |-- Priorisierung

	// Telefonie
	// |-- Anrufe
	// |   |-- Anrufliste
	// |   |-- Anrufe sperren
	// |   |-- Wählhilfe
	// |-- Telefonbuch
	// |   |-- Telefonbuch
	// |   |-- Interne Nummern
	// |   |-- Wählhilfe
	// |-- Weckruf 1 && 2
	// |-- Rufumleitung + Callthrough
	// |-- Telefoniegeräte
	// |   |-- Übersicht
	// |   |-- Festnetz
	// |-- Internettelefonie
	// |   |-- Internetrufnummern
	// |   |-- Erweiterte Einstellungen
	// |   |-- Sprachübertragung
	// |-- Wahlregeln && Anbietervorwahlen

	// USB-Geräte

	// Speicher / NAS

	// WLAN
	// |-- Monitor
	// |-- Funkeinstellungen
	// |-- Sicherheit
	// |-- WDS-Repeater

	// DECT
	// |-- Schnurlostelefone
	// |-- DECT-Einstellungen
	// |-- Monitor

	// System
	// |-- Ereignismonitor (Telefonie, Internet, USB, WLAN, System)
	// |-- Energiemonitor
	// |-- Push-Service
	// |-- Info-Anzeige
	// |-- Nachtschaltung
	// |-- FRITZ!Box Kennwort
	// |-- Netzwerk
	// |-- Ansicht
}
