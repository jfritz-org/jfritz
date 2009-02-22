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

	public JFritz jfritz;

	private boolean testItalyFailed = false;
	private boolean testSwitzerlandFailed = false;
	private boolean testUsaFailed = false;
	private boolean testNetherlandFailed = false;
	private boolean testFranceFailed = false;
	private boolean testAustriaFailed = false;
	private boolean testGermanyFailed = false;

	public void setUp(){
		Debug.on();
		Main.loadProperties();
		Main.loadMessages(new Locale("de_DE"));
		JFritz.loadNumberSettings();
		jfritz = new Main(new String[0]).getJfritz();
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
		entry = new CheckEntry(checkNum, "Foto Ottica (s.n.c.)", "", "Corso XXV Aprile, 123", "22036", "Erba");
		ReverseLookup.lookup(checkNum, entry, true);
		testItalyFailed = checkEntry(entry, testItalyFailed);

		checkNum = new PhoneNumber("+39226830102", false);
		entry = new CheckEntry(checkNum, "S.r.l. Prenotazioni Hotel", "Initalia", "Via Carnia, 33/A", "20132", "Milano");
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
		entry = new CheckEntry(checkNum, "Marketing AG", "Minisoft", "Papiermühleweg 1", "6048", "Horw");
		ReverseLookup.lookup(checkNum, entry, true);
		testSwitzerlandFailed = checkEntry(entry, testSwitzerlandFailed);

		checkNum = new PhoneNumber("+41442425243", false);
		entry = new CheckEntry(checkNum, "Martha (-Müller)", "Hauser", "Werdgässchen 15", "8004", "Zürich");
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
		entry = new CheckEntry(checkNum, "S Levison", "Adam", "602 Sun Valley Way", "07932", "Florham Park");
		ReverseLookup.lookup(checkNum, entry, true);
		testUsaFailed = checkEntry(entry, testUsaFailed);

		checkNum = new PhoneNumber("+13202304187", false);
		entry = new CheckEntry(checkNum, "Engelen", "M", "1349 15th Ave S", "56301-5439", "Saint Cloud");
		ReverseLookup.lookup(checkNum, entry, true);
		testUsaFailed = checkEntry(entry, testUsaFailed);

		checkNum =  new PhoneNumber("+14104200629", false);
		entry = new CheckEntry(checkNum, "D Smith", "Mark", "316 Streett Cir", "21050-3061", "Forest Hill");
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
		entry = new CheckEntry(checkNum, "International", "Camping", "Sint Bavodijk 2/D", "4504AA", "Nieuwvliet");
		ReverseLookup.lookup(checkNum,  entry, true);
		testNetherlandFailed = checkEntry(entry, testNetherlandFailed);

		checkNum = new PhoneNumber("+31207711969", false);
		entry = new CheckEntry(checkNum, "Smid", "C", "Lijnbaansgracht 153/HS", "1016VW", "Amsterdam");
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

		checkNum = new PhoneNumber("+33387936406", false);
		entry = new CheckEntry(checkNum, "Fabien", "Losson", "26 RUE DE BIBLING", "57550", "MERTEN");
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
		entry = new CheckEntry(checkNum, "GmbH", "Trivadis", "Handelskai  94-96", "1200", "Wien");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+4353365227", false);
		entry = new CheckEntry(checkNum, "Böglerhof GmbH u Co KG", "Romantikhotel", "166", "", "Alpbach");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+43662439860", false);
		entry = new CheckEntry(checkNum, "Fritz, Ing.", "Aberger", "Fischerg  12", "5020", "Salzburg");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+436507522840", false);
		entry = new CheckEntry(checkNum, "Birgitt", "Duftner", "Römerstr  25", "6230", "Brixlegg");
		ReverseLookup.lookup(checkNum, entry, true);
		testAustriaFailed = checkEntry(entry, testAustriaFailed);

		checkNum = new PhoneNumber("+43140400", false);
		entry = new CheckEntry(checkNum, "Krankenhaus - Universitätskliniken", "Allgemeines", "Währinger Gürtel  18-20", "1090", "Wien");
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

		checkNum = new PhoneNumber("+499115402808", false);
		entry = new CheckEntry(checkNum, "Car", "City", "Kleiststr. 48", "46539", "Dinslaken");
		ReverseLookup.lookup(checkNum,  entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+4920648286171", false);
		entry = new CheckEntry(checkNum, "Car", "City", "Kleiststr. 48", "46539", "Dinslaken");
		ReverseLookup.lookup(checkNum,  entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+4921149277129", false);
		entry = new CheckEntry(checkNum, "", "BEGIS-GRUPPE", "Jägerhofstr. 31", "40479", "Düsseldorf");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+496084950130", false);
		entry = new CheckEntry(checkNum, "Michael u. Martina", "Schmidt", "Treisberger Weg 12", "61389", "Schmitten");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+496221567200", false);
		entry = new CheckEntry(checkNum, "", "Universitätsklinikum", "Im Neuenheimer Feld 672", "69120", "Heidelberg");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+497112800144", false);
		entry = new CheckEntry(checkNum, "City", "Reisebüro", "Kronprinzstr. 8", "70173", "Stuttgart");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum =  new PhoneNumber("+497215704230", false);
		entry = new CheckEntry(checkNum, "LAGO", "Bowling", "Gablonzer Str. 13", "76185", "Karlsruhe");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+4972169090", false);
		entry = new CheckEntry(checkNum, "Karlsruhe", "Studentenwerk", "Adenauerring 7", "76131", "Karlsruhe");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+4989963853", false);
		entry = new CheckEntry(checkNum, "Birgit u. Dirk", "Lütkefent", "Moarstr. 6", "85737", "Ismaning");
		ReverseLookup.lookup(checkNum, entry, true);
		testGermanyFailed = checkEntry(entry, testGermanyFailed);

		checkNum = new PhoneNumber("+498962021830", false);
		entry = new CheckEntry(checkNum, "Birgit u. Dirk", "Lütkefent", "Moarstr. 6", "85737", "Ismaning");
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
			Debug.err("Failed " + entry.getCheckedNumber());
			Debug.err("Erwartet: " + entry.getCheckPerson().toCSV());
			Debug.err("Bekommen: " + entry.getReceivedPerson().toCSV());
			return true;
		}
		else
		{
			Debug.err("Passed " + entry.getCheckedNumber());
			return previousTestResult;
		}
	}
}
