package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestNorway extends ReverseLookupTestBase {
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
	public void testFlorentin() {
		checkNum = new PhoneNumberOld("+4773505023", false);
		entry = new CheckEntry(checkNum, "Florentin", "Moser", "Schiötzvei 5", "7020", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testUlrike() {
		checkNum = new PhoneNumberOld("+4773945687", false);
		entry = new CheckEntry(checkNum, "Ulrike", "Griep", "Loholtbakken 7", "7049", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testAksness() {
		checkNum = new PhoneNumberOld("+4756553530", false);
		entry = new CheckEntry(checkNum, "Holding AS", "Aksnes", "Grovabrotet 2", "5600", "Norheimsund (Kvam)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testHansen() {
		checkNum = new PhoneNumberOld("+4756553850", false);
		entry = new CheckEntry(checkNum, "Hansen", "Ragnar", "Hardangerfjordvegen 650", "5610", "Øystese (Kvam)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testJostein() {
		checkNum = new PhoneNumberOld("+4755226351", false);
		entry = new CheckEntry(checkNum, "Jostein", "Skage", "SKAGEVEGEN 148", "5258", "BLOMSTERDALEN");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testKraftverk() {
		checkNum = new PhoneNumberOld("+4756553300", false);
		entry = new CheckEntry(checkNum, "Kraftverk AS", "Kvam", "Kaldestad 40", "5600", "Norheimsund (Kvam)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testLars() {
		checkNum = new PhoneNumberOld("+4798043923", false);
		entry = new CheckEntry(checkNum, "Lars", "Ove Øye", "", "2315", "Hamar");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testAS() {
		checkNum = new PhoneNumberOld("+4756551733", false);
		entry = new CheckEntry(checkNum, "AS", "Scopus", "5600 Norheimsund (Kvam)", "5600", "Norheimsund (Kvam)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testColor() {
		checkNum = new PhoneNumberOld("+4722018500", false);
		entry = new CheckEntry(checkNum, "Line", "Color", "Hjortneskaia", "0250", "Oslo");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testVurderingsenheten() {
		checkNum = new PhoneNumberOld("+4755562250", false);
		entry = new CheckEntry(checkNum, "Vurderingsenheten", "", "Kalfarveien 20", "5018", "Bergen");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testSkodei() {
		checkNum = new PhoneNumberOld("+4778445050", false);
		entry = new CheckEntry(checkNum, "", "Skodei", "Markveien 6", "9510", "Alta");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testKvitbrygga() {
		checkNum = new PhoneNumberOld("+4775750080", false);
		entry = new CheckEntry(checkNum, "Kvitbrygga", "AS", "Forøy", "8178", "Halsa");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testKompetansesenter() {
		checkNum = new PhoneNumberOld("+4722249090", false);
		entry = new CheckEntry(checkNum, "For Distriktsutvik...", "Kompetansesenter", "8860 Tjøtta (Alstahaug)", "0180", "Oslo");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

