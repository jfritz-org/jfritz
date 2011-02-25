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

public class ReverseLookupTestBelgien extends ReverseLookupTestBase {
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
	public void test1() {
		checkNum = new PhoneNumberOld("+3225156111", false);
		entry = new CheckEntry(checkNum, "communale de et ‡ Ixelles (Ixelles)", "Admin.", "ChaussÈe d'Ixelles 168/a", "1050", "Ixelles");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void test2() {
		checkNum = new PhoneNumberOld("+3222824770", false);
		entry = new CheckEntry(checkNum, "d'Informatique pour la RÈgion Bruxelloise (CIRB)", "Centre", "Avenue des Arts 21", "1000", "Bruxelles");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void test3() {
		checkNum = new PhoneNumberOld("+3232052011", false);
		entry = new CheckEntry(checkNum, "Havenbedrijf Antwerpen", "Gemeentelijk", "Entrepotkaai 1", "2000", "Anvers");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}

	@Test
	public void test4() {
		checkNum = new PhoneNumberOld("+3261275331", false);
		entry = new CheckEntry(checkNum, "du Procureur du Roi de Neufch‚teau (Parquet du Procureur du Roi prËs le tribunal de premiËre instance de Neufch‚teau)", "Parquet", "Place Charles-Bergh 1", "6840", "Neufch‚teau");
		ReverseLookup.lookup(checkNum,  entry, true);
		checkEntry(entry);
	}
}

