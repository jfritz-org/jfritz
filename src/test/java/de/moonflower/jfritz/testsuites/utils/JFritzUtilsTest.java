package de.moonflower.jfritz.testsuites.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.moonflower.jfritz.utils.JFritzUtils;
import junit.framework.TestCase;

public class JFritzUtilsTest extends TestCase {

    public void testDateSubtraction1Day() {
    	GregorianCalendar gc1 = new GregorianCalendar(2006, Calendar.JANUARY, 1);
    	GregorianCalendar gc2 = new GregorianCalendar(2006, Calendar.JANUARY, 2);
    	int diff = JFritzUtils.subtractDays(gc2.getTime(), gc1.getTime());
    	assertEquals(diff, 1);
    }

    public void testDateSubtraction1Month() {
    	GregorianCalendar gc1 = new GregorianCalendar(2006, Calendar.JANUARY, 1);
    	GregorianCalendar gc2 = new GregorianCalendar(2006, Calendar.FEBRUARY, 1);
    	int diff = JFritzUtils.subtractDays(gc2.getTime(), gc1.getTime());
    	assertEquals(diff, 31);
    }

    public void testDateSubtraction1Month2() {
    	GregorianCalendar gc1 = new GregorianCalendar(2006, Calendar.FEBRUARY, 1);
    	GregorianCalendar gc2 = new GregorianCalendar(2006, Calendar.MARCH, 1);
    	int diff = JFritzUtils.subtractDays(gc2.getTime(), gc1.getTime());
    	assertEquals(diff, 28);
    }

    public void testDateSubtraction1Year() {
    	GregorianCalendar gc1 = new GregorianCalendar(2006, Calendar.JANUARY, 1);
    	GregorianCalendar gc2 = new GregorianCalendar(2007, Calendar.JANUARY, 1);
    	int diff = JFritzUtils.subtractDays(gc2.getTime(), gc1.getTime());
    	assertEquals(diff, 365);
    }

    public void testDateSubtraction2Years() {
    	GregorianCalendar gc1 = new GregorianCalendar(2006, Calendar.JANUARY, 1);
    	GregorianCalendar gc2 = new GregorianCalendar(2008, Calendar.JANUARY, 1);
    	int diff = JFritzUtils.subtractDays(gc2.getTime(), gc1.getTime());
    	assertEquals(diff, 2*365);
    }

    public void testDateSubtraction3Years() {
    	GregorianCalendar gc1 = new GregorianCalendar(2006, Calendar.JANUARY, 1);
    	GregorianCalendar gc2 = new GregorianCalendar(2009, Calendar.JANUARY, 1);
    	int diff = JFritzUtils.subtractDays(gc2.getTime(), gc1.getTime());
    	assertEquals(diff, (3*365)+1);
    }
}
