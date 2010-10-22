package de.moonflower.jfritz.testsuites.utils.reverselookup;

import junit.framework.Assert;

public class ReverseLookupTestBase {
	protected void checkEntry(CheckEntry entry)
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
