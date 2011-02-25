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

public class ReverseLookupTestAustria extends ReverseLookupTestBase {
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
	public void testTrivadis() {
		checkNum = new PhoneNumberOld("+4313323531", false);
		entry = new CheckEntry(checkNum, "Delphi GmbH", "Trivadis", "Handelskai 94-96", "1200", "Wien");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testKarin() {
		checkNum = new PhoneNumberOld("+4353365227", false);
		entry = new CheckEntry(checkNum, "Böglerhof GmbH", "Romantikhotel", "Alpbach 166", "6236", "Alpbach (T)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testPrammerRudolf() {
		checkNum = new PhoneNumberOld("+43732641574", false);
		entry = new CheckEntry(checkNum, "Rudolf / Edeltraud", "Prammer", "Götzelsdorf 17", "4221", "Steyregg (OÖ)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testPrammerJohann() {
		checkNum = new PhoneNumberOld("+4372374145", false);
		entry = new CheckEntry(checkNum, "Johann", "Prammer", "Stelzhamerstr 7", "4225", "Luftenberg an der Donau");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testPrammerWalter() {
		checkNum = new PhoneNumberOld("+4372372698", false);
		entry = new CheckEntry(checkNum, "Walter", "Prammer", "Abwinden Opalweg 8", "4225", "Luftenberg an der Donau (OÖ)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

