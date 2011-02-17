package de.moonflower.jfritz.testsuites.utils.reverselookup;

import junit.framework.Assert;

import org.junit.Test;

import de.moonflower.jfritz.utils.reverselookup.ParseItem;
import de.moonflower.jfritz.utils.reverselookup.ParseItemType;

public class ParseItemTest {

	@Test
	public void testEqualsNull() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		Assert.assertFalse(i1.equals(null));
	}

	@Test
	public void testEqualsType() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		Assert.assertTrue(i1.equals(i2));
		Assert.assertTrue(i2.equals(i1));
	}

	@Test
	public void testEqualsLine() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(0);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(0);
		Assert.assertTrue(i1.equals(i2));
		Assert.assertTrue(i2.equals(i1));
	}

	@Test
	public void testEqualsStartIndex() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setStartIndex(0);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setStartIndex(0);
		Assert.assertTrue(i1.equals(i2));
		Assert.assertTrue(i2.equals(i1));
	}

	@Test
	public void testEqualsValue() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setValue("abc");
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setValue("abc");
		Assert.assertTrue(i1.equals(i2));
		Assert.assertTrue(i2.equals(i1));
	}

	@Test
	public void testEqualsAll() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(1);
		i1.setStartIndex(2);
		i1.setValue("abc");
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(1);
		i2.setStartIndex(2);
		i2.setValue("abc");
		Assert.assertTrue(i1.equals(i2));
		Assert.assertTrue(i2.equals(i1));
	}

	@Test
	public void testReflexivity() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(1);
		i1.setStartIndex(2);
		i1.setValue("abc");
		Assert.assertTrue(i1.equals(i1));
	}

	@Test
	public void testEqualsTransitivity() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(1);
		i1.setStartIndex(2);
		i1.setValue("abc");
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(1);
		i2.setStartIndex(2);
		i2.setValue("abc");
		ParseItem i3 = new ParseItem(ParseItemType.FIRSTNAME);
		i3.setLine(1);
		i3.setStartIndex(2);
		i3.setValue("abc");
		Assert.assertTrue(i1.equals(i2));
		Assert.assertTrue(i2.equals(i3));
		Assert.assertTrue(i1.equals(i3));
	}

	@Test
	public void testEqualsFailType() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		ParseItem i2 = new ParseItem(ParseItemType.LASTNAME);
		Assert.assertFalse(i1.equals(i2));
		Assert.assertFalse(i2.equals(i1));
	}

	@Test
	public void testEqualsFailLine() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(0);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(1);
		Assert.assertFalse(i1.equals(i2));
		Assert.assertFalse(i2.equals(i1));
	}

	@Test
	public void testEqualsFailStartIndex() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setStartIndex(0);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setStartIndex(1);
		Assert.assertFalse(i1.equals(i2));
		Assert.assertFalse(i2.equals(i1));
	}

	@Test
	public void testEqualsFailValue() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setValue("abc");
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setValue("xyz");
		Assert.assertFalse(i1.equals(i2));
		Assert.assertFalse(i2.equals(i1));
	}

	@Test
	public void testCompareUninitialized() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		Assert.assertEquals(0, i1.compareTo(i2));
	}

	@Test
	public void testCompareLineGreater() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(1);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(0);
		Assert.assertEquals(1, i1.compareTo(i2));
	}

	@Test
	public void testCompareLineLower() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(0);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(1);
		Assert.assertEquals(-1, i1.compareTo(i2));
	}

	@Test
	public void testCompareLineEquals() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(1);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(1);
		Assert.assertEquals(0, i1.compareTo(i2));
	}

	@Test
	public void testCompareNoLineStartIndexEquals() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setStartIndex(1);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setStartIndex(1);
		Assert.assertEquals(0, i1.compareTo(i2));
	}

	@Test
	public void testCompareStartIndexGreater() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(0);
		i1.setStartIndex(1);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(0);
		i2.setStartIndex(-1);
		Assert.assertEquals(1, i1.compareTo(i2));
	}

	@Test
	public void testCompareStartIndexLower() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(0);
		i1.setStartIndex(-10);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(0);
		i2.setStartIndex(1);
		Assert.assertEquals(-1, i1.compareTo(i2));
	}


	@Test
	public void testCompareStartIndexEquals() {
		ParseItem i1 = new ParseItem(ParseItemType.FIRSTNAME);
		i1.setLine(0);
		i1.setStartIndex(-10);
		ParseItem i2 = new ParseItem(ParseItemType.FIRSTNAME);
		i2.setLine(0);
		i2.setStartIndex(-10);
		Assert.assertEquals(0, i1.compareTo(i2));
	}
}
