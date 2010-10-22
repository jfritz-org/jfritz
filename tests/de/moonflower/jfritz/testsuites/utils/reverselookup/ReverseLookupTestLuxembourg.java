package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestLuxembourg extends ReverseLookupTestBase {
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
	public void testArcelorMittal() {
		checkNum = new PhoneNumberOld("+35247921", false);
		entry = new CheckEntry(checkNum, "International Luxembourg", "ArcelorMittal", "", "", "");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testImmobiliere() {
		checkNum = new PhoneNumberOld("+352225533", false);
		entry = new CheckEntry(checkNum, "ImmobiliËre Luxembourgeoise Immosol S‡rl", "Agence", "14 AV. DE LA LIBERTE", "1930", "LUXEMBOURG");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testSARL() {
		checkNum = new PhoneNumberOld("+35226203026", false);
		entry = new CheckEntry(checkNum, "LUXEMBOURG S.A R.L.", "PEARLE", "", "", "");
//		entry = new CheckEntry(checkNum, "LUXEMBOURG S.A R.L.", "PEARLE", "18 R. NOTRE-DAME", "2240", "LUXEMBOURG");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testCremation() {
		checkNum = new PhoneNumberOld("+352220335", false);
		entry = new CheckEntry(checkNum, "Luxembourgeoise de CrÈmation SA", "SociÈtÈ", "1 R. DES BENEDICTINS", "6414", "ECHTERNACH");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testRischette() {
		checkNum = new PhoneNumberOld("+352788331", false);
		entry = new CheckEntry(checkNum, "S‡rl", "Rischette", "4 RTE DE LUXEMBOURG", "6130", "JUNGLINSTER");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

