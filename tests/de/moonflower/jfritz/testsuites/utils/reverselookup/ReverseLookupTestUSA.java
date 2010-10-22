package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestUSA extends ReverseLookupTestBase {
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
	public void testAdam() {
		checkNum = new PhoneNumberOld("+19736350430", false);
		entry = new CheckEntry(checkNum, "Adam", "Levison", "602 Sun Valley Way", "07932", "Florham Park");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testJennifer() {
		checkNum =  new PhoneNumberOld("+14104200629", false);
		entry = new CheckEntry(checkNum, "Jennifer", "Smith", "316 Streett Cir", "21050", "Forest Hill");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

