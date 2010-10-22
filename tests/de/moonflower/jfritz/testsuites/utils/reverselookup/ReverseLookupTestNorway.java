package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestNorway {
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
	public void test() {
		checkNum = new PhoneNumberOld("+4773505023", false);
		entry = new CheckEntry(checkNum, "Florentin", "Moser", "Schiötzvei 5", "7020", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void test2() {
		checkNum = new PhoneNumberOld("+4773945687", false);
		entry = new CheckEntry(checkNum, "Ulrike", "Griep", "Loholtbakken 7", "7049", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
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
		Assert.assertTrue("Not finished yet", entry.isDone());

		Assert.assertEquals("Firstname does not match", entry.getCheckPerson().getFirstName(),
				entry.getReceivedPerson().getFirstName());
		Assert.assertEquals("Lastname does not match", entry.getCheckPerson().getLastName(),
				entry.getReceivedPerson().getLastName());
		Assert.assertEquals("Street does not match", entry.getCheckPerson().getStreet(),
				entry.getReceivedPerson().getStreet());
		Assert.assertEquals("City does not match", entry.getCheckPerson().getCity(),
				entry.getReceivedPerson().getCity());
		Assert.assertEquals("Postalcode does not match", entry.getCheckPerson().getPostalCode(),
				entry.getReceivedPerson().getPostalCode());
	}
}

