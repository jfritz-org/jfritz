package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestGermany extends ReverseLookupTestBase {
	private CheckEntry entry;
	private PhoneNumberOld checkNum;

	@BeforeClass
	public static void init() {
		Debug.on();
    	Debug.setVerbose(true);
    	Debug.setDebugLevel(Debug.LS_DEBUG);
		PropertyProvider.getInstance().loadProperties(false);
		MessageProvider.getInstance().loadMessages(new Locale("de_DE"));
		JFritz.loadNumberSettings();
	}

	@Test
	public void testStaatstheater() {
		checkNum = new PhoneNumberOld("+495311234567", false);
		entry = new CheckEntry(checkNum, "Am Theater", "STAATSTHEATER", "Am Theater", "38100", "Braunschweig");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testZehner() {
		checkNum = new PhoneNumberOld("+49277242239", false);
		entry = new CheckEntry(checkNum, "Annette", "Zehner", "An der Alten Kirche 3", "35745", "Herborn");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testUllrich() {
		checkNum = new PhoneNumberOld("+498990199190", false);
		entry = new CheckEntry(checkNum, "Rainer", "Ullrich", "Zugspitzstr. 20", "85609", "Aschheim");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testThen() {
		checkNum = new PhoneNumberOld("+498104889820", false);
		entry = new CheckEntry(checkNum, "O. Dr.med.", "Then", "Bahnhofplatz 7", "82054", "Sauerlach");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testOptik() {
		checkNum = new PhoneNumberOld("+499115402808", false);
		entry = new CheckEntry(checkNum, "", "Apollo-Optik", "Äußere Bayreuther Str. 80", "90491", "Nürnberg");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testCityCar() {
		checkNum = new PhoneNumberOld("+4920648286171", false);
		entry = new CheckEntry(checkNum, "Car", "City", "Kleiststr. 48", "46539", "Dinslaken");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testMichael() {
		checkNum = new PhoneNumberOld("+496084950130", false);
		entry = new CheckEntry(checkNum, "Michael , Martina", "Schmidt", "Treisberger Weg 12", "61389", "Schmitten");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testKlinikum() {
		checkNum = new PhoneNumberOld("+496221567200", false);
		entry = new CheckEntry(checkNum, "Heidelberg", "Universitätsklinikum", "Im Neuenheimer Feld 325", "69120", "Heidelberg");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testLago() {
		checkNum =  new PhoneNumberOld("+497215704230", false);
		entry = new CheckEntry(checkNum, "LAGO", "Bowling-Center", "Gablonzer Str. 13", "76185", "Karlsruhe");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testDirk() {
		checkNum = new PhoneNumberOld("+4989963853", false);
		entry = new CheckEntry(checkNum, "Birgit u. Dirk", "Lütkefent", "Moarstr. 6", "85737", "Ismaning");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testInfraplan() {
		checkNum = new PhoneNumberOld("+498962021830", false);
		entry = new CheckEntry(checkNum, "Hausverwaltungs GmbH & Co. Betreuungs KG", "Infraplan", "Franziskanerstr. 14", "81669", "München");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

