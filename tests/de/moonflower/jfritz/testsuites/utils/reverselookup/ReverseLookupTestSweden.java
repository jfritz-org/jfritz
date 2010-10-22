package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestSweden extends ReverseLookupTestBase {
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
	public void testBirgitta() {
		checkNum = new PhoneNumberOld("+4652610580", false);
		entry = new CheckEntry(checkNum, "Birgitta", "Lystad", "Västra klevgatan 7", "45230", "STRÖMSTAD");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testJohan() {
		checkNum = new PhoneNumberOld("+46702619401", false);
		entry = new CheckEntry(checkNum, "Johan", "Nilsson", "Utsädesgatan 3B", "43146", "MÖLNDAL");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

