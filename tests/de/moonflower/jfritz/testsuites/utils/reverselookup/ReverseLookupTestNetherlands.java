package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestNetherlands extends ReverseLookupTestBase {
	private CheckEntry entry;
	private PhoneNumberOld checkNum;

	@BeforeClass
	public static void init() {
		Debug.on();
    	Debug.setVerbose(true);
    	Debug.setDebugLevel(Debug.LS_DEBUG);
		Main.loadProperties(false);
		Main.loadMessages(new Locale("de_DE"));
		JFritz.loadNumberSettings();
	}

	@Test
	public void testCamping() {
		checkNum = new PhoneNumberOld("+31117371233", false);
		entry = new CheckEntry(checkNum, "", "Camping International", "Sint Bavodijk 2/D", "4504AA", "Nieuwvliet");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testSmid() {
		checkNum = new PhoneNumberOld("+31207711969", false);
		entry = new CheckEntry(checkNum, "", "Smid", "Westerdok 328", "1013BH", "Amsterdam");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

