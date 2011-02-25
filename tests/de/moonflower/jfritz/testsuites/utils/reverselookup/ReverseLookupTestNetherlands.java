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

public class ReverseLookupTestNetherlands extends ReverseLookupTestBase {
	private CheckEntry entry;
	private PhoneNumberOld checkNum;

//	0515 559019 zeigt : Wijnia aber muss sein Wijnia, Germ en Martine
//	0566 601481 zeigt: Vliet aber muss sein Vliet, M. van
//	0515 420833 zeigt: Vries aber muss sein Vries, M A V de

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
	public void testCamping() {
		checkNum = new PhoneNumberOld("+31117371233", false);
		entry = new CheckEntry(checkNum, "", "Camping International", "Sint Bavodijk 2/D", "4504AA", "Nieuwvliet");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void testSmid() {
		checkNum = new PhoneNumberOld("+31207711969", false);
		entry = new CheckEntry(checkNum, "C", "Smid", "Westerdok 328", "1013BH", "Amsterdam");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

