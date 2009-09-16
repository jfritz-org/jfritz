package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import junit.framework.TestCase;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;


/**
 * This class is responsible for testing the Reverselookup functions
 *
 * @author brian jensen
 *
 */
public class ReverseLookupTest extends TestCase {

	private boolean testLuxemburgFailed = false;
	private boolean testItalyFailed = false;
	private boolean testSwitzerlandFailed = false;
	private boolean testUsaFailed = false;
	private boolean testNetherlandFailed = false;
	private boolean testFranceFailed = false;
	private boolean testAustriaFailed = false;
	private boolean testGermanyFailed = false;
	private boolean testNorwayFailed = false;
	private boolean testSwedenFailed = false;

	public void setUp(){
		Debug.on();
    	Debug.setVerbose(true);
    	Debug.setDebugLevel(Debug.LS_DEBUG);
		Main.loadProperties(false);
		Main.loadMessages(new Locale("de_DE"));
		JFritz.loadNumberSettings();
	}

	/**
	 * This method tests the reverse lookup for several norwegian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupSweden() {
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+4652610580", false);
		entry = new CheckEntry(checkNum, "Birgitta", "Lystad", "Västra klevgatan 7", "45230", "STRÖMSTAD");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwedenFailed = checkEntry(entry, testSwedenFailed);

		checkNum = new PhoneNumber("+46702619401", false);
		entry = new CheckEntry(checkNum, "Johan", "Nilsson", "Utsädesgatan 3B", "43146", "MÖLNDAL");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwedenFailed = checkEntry(entry, testSwedenFailed);

		assertFalse(testSwedenFailed);
	}

	/**
	 * This method tests the reverse lookup for several norwegian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupNorway()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+4793895329", false);
		entry = new CheckEntry(checkNum, "Inger", "Andresen", "Bjarne Ness veg 25", "7033", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		testNorwayFailed = checkEntry(entry, testNorwayFailed);

		checkNum = new PhoneNumber("+4773505023", false);
		entry = new CheckEntry(checkNum, "Florentin", "Moser", "- Schiötzvei 5", "7020", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		testNorwayFailed = checkEntry(entry, testNorwayFailed);

		checkNum = new PhoneNumber("+4773945687", false);
		entry = new CheckEntry(checkNum, "Ulrike", "Griep", "Loholtbakken 7", "7049", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		testNorwayFailed = checkEntry(entry, testNorwayFailed);

		assertFalse(testNorwayFailed);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupItaly()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+3928260860", false);
		entry = new CheckEntry(checkNum, "Luigi", "Ferrari", "Via Baroni Costantino, 114", "20142", "Milano");
		ReverseLookup.lookup(checkNum, entry, true);
		testItalyFailed = checkEntry(entry, testItalyFailed);

		checkNum = new PhoneNumber("+39655262755", false);
		entry = new CheckEntry(checkNum, "Dr. Mario Studio", "Rossi", "Largo La Loggia Gaetano, 33", "00149", "Roma");
		ReverseLookup.lookup(checkNum, entry, true);
		testItalyFailed = checkEntry(entry, testItalyFailed);

		checkNum = new PhoneNumber("+3931642176", false);
		entry = new CheckEntry(checkNum, "Foto Ottica (S.N.C.)", "Fantinato", "", "22036", "Erba");
		ReverseLookup.lookup(checkNum, entry, true);
		testItalyFailed = checkEntry(entry, testItalyFailed);

		checkNum = new PhoneNumber("+39226830102", false);
		entry = new CheckEntry(checkNum, "S.R.L. Prenotazioni Hotel", "Initalia", "Via Carnia, 33/A", "20132", "Milano");
		ReverseLookup.lookup(checkNum, entry, true);
		testItalyFailed = checkEntry(entry, testItalyFailed);

//		checkNum = new PhoneNumber("+39817410047", false);
//		entry = new CheckEntry(checkNum, "ANDREA", "PIETROPAOLI", "VL. DEI PINI 5", "80131", "NAPOLI");
//		ReverseLookup.lookup(checkNum, entry, true);
//		testItalyFailed = checkEntry(entry, testItalyFailed);

		assertFalse(testItalyFailed);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupSwitzerland()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+41318493427", false);
		entry = new CheckEntry(checkNum, "Kurt und Nadja (-Siegenthaler)", "Krebs", "Wichelackerstrasse 31", "3144", "Gasel");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		checkNum = new PhoneNumber("+41413402320", false);
		entry = new CheckEntry(checkNum, "Monika und Bernard (-Hopf)", "Clalüna", "Schönbühlstrand 32", "6005", "Luzern");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		checkNum = new PhoneNumber("+41447712727", false);
		entry = new CheckEntry(checkNum, "GmbH", "Cytracon", "Fabrikhof 3", "8134", "Adliswil");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		checkNum = new PhoneNumber("+41449264500", false);
		entry = new CheckEntry(checkNum, "Heinz", "Habegger", "Glärnischstrasse 61", "8712", "Stäfa");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		checkNum = new PhoneNumber("+41627750431", false);
		entry = new CheckEntry(checkNum, "Achim und Cornelia", "Geiser", "Webereistrasse 39", "5703", "Seon");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		checkNum =  new PhoneNumber("+41715550800", false);
		entry = new CheckEntry(checkNum, "Telemarketing AG", "CallWorld", "Heiligkreuzstrasse 2", "9008", "St. Gallen");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		assertFalse(testSwitzerlandFailed);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupUsa()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+19736350430", false);
		entry = new CheckEntry(checkNum, "Adam", "Levison", "602 Sun Valley Way", "07932", "Florham Park");
		ReverseLookup.lookup(checkNum, entry, true);
		testUsaFailed = checkEntry(entry, testUsaFailed);

		checkNum = new PhoneNumber("+13202304187", false);
		entry = new CheckEntry(checkNum, "Jeff", "Engelen", "1349 15th Ave S", "56301", "Saint Cloud");
		ReverseLookup.lookup(checkNum, entry, true);
		testUsaFailed = checkEntry(entry, testUsaFailed);

		checkNum =  new PhoneNumber("+14104200629", false);
		entry = new CheckEntry(checkNum, "Jennifer", "Smith", "316 Streett Cir", "21050", "Forest Hill");
		ReverseLookup.lookup(checkNum, entry, true);
		testUsaFailed = checkEntry(entry, testUsaFailed);

		assertFalse(testUsaFailed);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupNetherland()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+31117371233", false);
		entry = new CheckEntry(checkNum, "", "Camping International", "Sint Bavodijk 2/D", "4504AA", "Nieuwvliet");
		ReverseLookup.lookup(checkNum,  entry, true);
		testNetherlandFailed = checkEntry(entry, testNetherlandFailed);

		checkNum = new PhoneNumber("+31207711969", false);
		entry = new CheckEntry(checkNum, "", "Smid", "Westerdok 328", "1013BH", "Amsterdam");
		ReverseLookup.lookup(checkNum, entry, true);
		testNetherlandFailed = checkEntry(entry, testNetherlandFailed);

		assertFalse(testNetherlandFailed);
	}

	/**
	 * This method tests the reverse lookup for several france phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupFrance()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+33387065155", false);
		entry = new CheckEntry(checkNum, "", "Camping Hanau Plage", "r Etang", "57230", "PHILIPPSBOURG");
		ReverseLookup.lookup(checkNum, entry, true);
		testFranceFailed = checkEntry(entry, testFranceFailed);

		checkNum = new PhoneNumber("+33388862622", false);
		entry = new CheckEntry(checkNum, "", "Restaurant Au Cerf", "2 r Fort Louis", "67480", "ROESCHWOOG");
		ReverseLookup.lookup(checkNum, entry, true);
		testFranceFailed = checkEntry(entry, testFranceFailed);

		checkNum = new PhoneNumber("+33388863772", false);
		entry = new CheckEntry(checkNum, "Frédéric", "Heldt", "2 r Eglise", "67480", "AUENHEIM");
		ReverseLookup.lookup(checkNum, entry, true);
		testFranceFailed = checkEntry(entry, testFranceFailed);

		assertFalse(testFranceFailed);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupAustria()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+4313323531", false);
		entry = new CheckEntry(checkNum, "GmbH", "Trivadis", "Handelskai 94-96", "1200", "Wien");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+4353365227", false);
		entry = new CheckEntry(checkNum, "Karin", "Duftner", "Nr 166", "6236", "Alpbach");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+43662439860", false);
		entry = new CheckEntry(checkNum, "Fritz, Ing.", "Aberger", "", "Fischerg 12", "5020", "Salzburg");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+436507522840", false);
		entry = new CheckEntry(checkNum, "Birgitt", "Duftner", "Römerstr 25", "6230", "Brixlegg");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+43140400", false);
		entry = new CheckEntry(checkNum, "Krankenhaus - Universitätskliniken", "Allgemeines", "Währinger Gürtel 18-20", "1090", "Wien");
		ReverseLookup.lookup(checkNum,  entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		assertFalse(testAustriaFailed);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupGermany()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+498990199190", false);
		entry = new CheckEntry(checkNum, "Rainer", "Ullrich", "Zugspitzstr. 20", "85609", "Aschheim");
		ReverseLookup.lookup(checkNum,  entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+498104889820", false);
		entry = new CheckEntry(checkNum, "O. Dr.med.", "Then", "", "", "Sauerlach");
		ReverseLookup.lookup(checkNum,  entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+499115402808", false);
		entry = new CheckEntry(checkNum, "", "Apollo-Optik", "Äußere Bayreuther Str. 80", "90491", "Nürnberg");
		ReverseLookup.lookup(checkNum,  entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+4920648286171", false);
		entry = new CheckEntry(checkNum, "Car", "City", "Kleiststr. 48", "46539", "Dinslaken");
		ReverseLookup.lookup(checkNum,  entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+496084950130", false);
		entry = new CheckEntry(checkNum, "Michael , Martina", "Schmidt", "Treisberger Weg 12", "61389", "Schmitten");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+496221567200", false);
		entry = new CheckEntry(checkNum, "", "Universitätsklinikum", "Im Neuenheimer Feld 672", "69120", "Heidelberg");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum =  new PhoneNumber("+497215704230", false);
		entry = new CheckEntry(checkNum, "Karlsruhe", "Bowling-Center", "Gablonzer Str. 13", "76185", "Karlsruhe");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+4989963853", false);
		entry = new CheckEntry(checkNum, "Birgit u. Dirk", "Lütkefent", "Moarstr. 6", "85737", "Ismaning");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+498962021830", false);
		entry = new CheckEntry(checkNum, "Hausverwaltung GmbH & Co. Beteiligungs KG", "Infraplan", "Franziskanerstr. 14", "81669", "München");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		assertFalse(testGermanyFailed);
	}

	private boolean checkEntry(CheckEntry entry, boolean previousTestResult)
	{
		while (!entry.isDone())
		{
			try {
				Thread.yield();
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		assertTrue(entry.isDone());
		if (!entry.hasSucceeded())
		{
			Debug.error("Failed " + entry.getCheckedNumber());
			Debug.error("Erwartet: " + entry.getCheckPerson().toCSV());
			Debug.error("Bekommen: " + entry.getReceivedPerson().toCSV());
			return true;
		}
		else
		{
			Debug.error("Passed " + entry.getCheckedNumber());
			return previousTestResult;
		}
	}

	/**
	 * This method tests the reverse lookup for several luxemburg phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupLuxemburg()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry;
		PhoneNumber checkNum;

		checkNum = new PhoneNumber("+35247921", false);
		entry = new CheckEntry(checkNum, "Headquarters", "ArcelorMittal", "19 AV. DE LA LIBERTE", "2930", "LUXEMBOURG");
		ReverseLookup.lookup(checkNum, entry, true);
		testLuxemburgFailed = checkEntry(entry, testLuxemburgFailed);

		checkNum = new PhoneNumber("+352225533", false);
		entry = new CheckEntry(checkNum, "Immobilière Luxembourgeoise Immosol Sàrl", "Agence", "14 AV. DE LA LIBERTE", "1930", "LUXEMBOURG");
		ReverseLookup.lookup(checkNum, entry, true);
		testLuxemburgFailed = checkEntry(entry, testLuxemburgFailed);

		checkNum = new PhoneNumber("+35226203026", false);
		entry = new CheckEntry(checkNum, "LUXEMBOURG S.A R.L.", "PEARLE", "", "", "");
//		entry = new CheckEntry(checkNum, "LUXEMBOURG S.A R.L.", "PEARLE", "18 R. NOTRE-DAME", "2240", "LUXEMBOURG");
		ReverseLookup.lookup(checkNum, entry, true);
		testLuxemburgFailed = checkEntry(entry, testLuxemburgFailed);

		checkNum = new PhoneNumber("+3523279011", false);
		entry = new CheckEntry(checkNum, "CASTERMANS SARL", "GARAGE", "175 RTE DE LUXEMBOURG", "7540", "ROLLINGEN");
		ReverseLookup.lookup(checkNum, entry, true);
		testLuxemburgFailed = checkEntry(entry, testLuxemburgFailed);

		checkNum = new PhoneNumber("+352220335", false);
		entry = new CheckEntry(checkNum, "Luxembourgeoise de Crémation SA", "Société", "1 R. DES BENEDICTINS", "6414", "ECHTERNACH");
		ReverseLookup.lookup(checkNum, entry, true);
		testLuxemburgFailed = checkEntry(entry, testLuxemburgFailed);

		checkNum = new PhoneNumber("+352788331", false);
		entry = new CheckEntry(checkNum, "S.A R.L.", "RISCHETTE", "4 RTE DE LUXEMBOURG", "6130", "JUNGLINSTER");
		ReverseLookup.lookup(checkNum, entry, true);
		testLuxemburgFailed = checkEntry(entry, testLuxemburgFailed);

		assertFalse(testLuxemburgFailed);
	}
}
