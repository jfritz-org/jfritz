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
		CheckEntry entry = new CheckEntry("ANDREA", "PIETROPAOLI", "VL. DEI PINI 5", "80131", "NAPOLI");
		ReverseLookup.lookup(new PhoneNumber("+39817410047", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("S.R.L. (PRENOTAZIONI HOTEL)", "INITALIA", "V. CARNIA 33/A", "20132", "MILANO");
		ReverseLookup.lookup(new PhoneNumber("+39226830102", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("MARIO", "ROSSI", "LG. GAETANO LA LOGGIA 33", "00149", "ROMA");
		ReverseLookup.lookup(new PhoneNumber("+39655262755", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("FOTO OTTICA (S.N.C.)", "FANTINATO", "C. VENTICINQUE APRILE 123", "22036", "ERBA");
		ReverseLookup.lookup(new PhoneNumber("+3931642176", false), entry, true);
		checkEntry(entry);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupSwitzerland()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry = new CheckEntry("GmbH", "Cytracon", "Fabrikhof 3", "8134", "Adliswil");
		ReverseLookup.lookup(new PhoneNumber("+41447712727", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Martha (-Müller)", "Hauser", "Werdgässchen 15", "8004", "Zürich/ZH");
		ReverseLookup.lookup(new PhoneNumber("+41442425243", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Achim und Cornelia", "Geiser", "Webereistrasse 39", "5703", "Seon");
		ReverseLookup.lookup(new PhoneNumber("+41627750431", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Kurt und Nadja (-Siegenthaler)", "Krebs", "Wichelackerstrasse 31", "3144", "Gasel");
		ReverseLookup.lookup(new PhoneNumber("+41318493427", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Marketing AG", "Minisoft", "Papiermühleweg 1", "6048", "Horw");
		ReverseLookup.lookup(new PhoneNumber("+41413402320", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Telemarketing AG", "CallWorld", "Heiligkreuzstrasse 2", "9008", "St. Gallen");
		ReverseLookup.lookup(new PhoneNumber("+41715550800", false), entry, true);
		checkEntry(entry);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupUsa()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry = new CheckEntry("Engelen", "M", "1349 15th Ave S", "56301-5439", "Saint Cloud");
		ReverseLookup.lookup(new PhoneNumber("+13202304187", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("D Smith", "Mark", "316 Streett Cir", "21050-3061", "Forest Hill");
		ReverseLookup.lookup(new PhoneNumber("+14104200629", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("S Levison", "Adam", "602 Sun Valley Way", "07932", "Florham Park");
		ReverseLookup.lookup(new PhoneNumber("+19736350430", false), entry, true);
		checkEntry(entry);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupNetherland()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry = new CheckEntry("International", "Camping", "Sint Bavodijk 2/D", "4504AA", "Nieuwvliet");
		ReverseLookup.lookup(new PhoneNumber("+31117371233", false), entry, true);
		checkEntry(entry);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupAustria()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry = new CheckEntry("Fritz, Ing.", "Aberger", "Fischerg  12", "5020", "Salzburg");
		ReverseLookup.lookup(new PhoneNumber("+43662439860", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Krankenhaus - Universitätskliniken", "Allgemeines", "Währinger Gürtel  18-20", "1090", "Wien");
		ReverseLookup.lookup(new PhoneNumber("+43140400", false), entry, true);
		checkEntry(entry);
	}

	/**
	 * This method tests the reverse lookup for several italian phone numbers.
	 * IT IS NOT ALLOWED TO USE ANY OF THIS INFORMATION IN THIS FILE FOR OTHER PURPOSES THAN TESTING.
	 */
	public void testReverseLookupGermany()
	{
		// CheckEntry(firstName, lastName, street, postalCode, city);
		CheckEntry entry = new CheckEntry("City", "Reisebüro", "Kronprinzstr. 8", "70173", "Stuttgart");
		ReverseLookup.lookup(new PhoneNumber("+497112800144", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("LAGO", "Bowling", "Gablonzer Str. 13", "76185", "Karlsruhe");
		ReverseLookup.lookup(new PhoneNumber("+497215704230", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Car", "City", "Kleiststr. 48", "46539", "Dinslaken");
		ReverseLookup.lookup(new PhoneNumber("+4920648286171", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("Karlsruhe", "Studentenwerk", "Adenauerring 7", "76131", "Karlsruhe");
		ReverseLookup.lookup(new PhoneNumber("+4972169090", false), entry, true);
		checkEntry(entry);

		entry = new CheckEntry("", "Universitätsklinikum", "Im Neuenheimer Feld 672", "69120", "Heidelberg");
		ReverseLookup.lookup(new PhoneNumber("+496221567200", false), entry, true);
		checkEntry(entry);
	}

	private void checkEntry(CheckEntry entry)
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
		assertTrue(entry.hasSucceeded());
	}
}
