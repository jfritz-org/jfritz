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

public class ReverseLookupTestFrance extends ReverseLookupTestBase {
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
	public void testCamping() {
		checkNum = new PhoneNumberOld("+33387065155", false);
		entry = new CheckEntry(checkNum, "Hanau Plage", "Camping", "Rue Etang", "57230", "Philippsbourg");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testRestaurant() {
		checkNum = new PhoneNumberOld("+33388862622", false);
		entry = new CheckEntry(checkNum, "Au Cerf", "Restaurant", "2 Rue Fort Louis", "67480", "Roeschwoog");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testHeldt() {
		checkNum = new PhoneNumberOld("+33388863772", false);
		entry = new CheckEntry(checkNum, "FrÈdÈric", "Heldt", "2 Rue Eglise", "67480", "Auenheim");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

