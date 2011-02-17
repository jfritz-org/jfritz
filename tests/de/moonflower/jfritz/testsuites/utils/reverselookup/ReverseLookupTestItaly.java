package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestItaly extends ReverseLookupTestBase {
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
	public void testLuigi() {
		checkNum = new PhoneNumberOld("+3928260860", false);
		entry = new CheckEntry(checkNum, "Luigi", "Ferrari", "Via Baroni Costantino 114", "20142", "Milano");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testMario() {
		checkNum = new PhoneNumberOld("+39655262755", false);
		entry = new CheckEntry(checkNum, "Dr. Mario Studio", "Rossi", "Largo La Loggia Gaetano 33", "00149", "Roma");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testOttica() {
		checkNum = new PhoneNumberOld("+3931642176", false);
		entry = new CheckEntry(checkNum, "Foto Ottica (S.N.C.)", "Fantinato", "Corso XXV Aprile 123", "22036", "Erba");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testPrenotazioni() {
		checkNum = new PhoneNumberOld("+39226830102", false);
		entry = new CheckEntry(checkNum, "S.R.L. Prenotazioni Hotel", "Initalia", "Via Carnia 33/A", "20132", "Milano");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

