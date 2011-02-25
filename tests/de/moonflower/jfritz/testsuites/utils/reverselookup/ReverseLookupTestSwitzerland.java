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

public class ReverseLookupTestSwitzerland extends ReverseLookupTestBase {
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
	public void testKurt() {
		checkNum = new PhoneNumberOld("+41318493427", false);
		entry = new CheckEntry(checkNum, "Kurt und Nadja (-Siegenthaler)", "Krebs", "Wichelackerstrasse 31", "3144", "Gasel");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testMinisoft() {
		checkNum = new PhoneNumberOld("+41413402320", false);
		entry = new CheckEntry(checkNum, "Marketing AG", "Minisoft", "Papiermühleweg 1", "6048", "Horw");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testCytracon() {
		checkNum = new PhoneNumberOld("+41447712727", false);
		entry = new CheckEntry(checkNum, "GmbH", "Cytracon", "Rütistrasse 20a", "8134", "Adliswil");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testGeiser() {
		checkNum = new PhoneNumberOld("+41627750431", false);
		entry = new CheckEntry(checkNum, "Achim und Cornelia", "Geiser", "Webereistrasse 39", "5703", "Seon");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testCallWorld() {
		checkNum =  new PhoneNumberOld("+41715550800", false);
		entry = new CheckEntry(checkNum, "Telemarketing AG", "CallWorld", "Heiligkreuzstrasse 2", "9008", "St. Gallen");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

