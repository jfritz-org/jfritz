package de.moonflower.jfritz.structs;

import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhoneNumberOldTest {

    @Test
    public void testIsNumeric() {
        assertFalse(PhoneNumberOld.isNumeric(null));
        assertFalse(PhoneNumberOld.isNumeric(""));
        assertFalse(PhoneNumberOld.isNumeric(" "));
        assertFalse(PhoneNumberOld.isNumeric("a"));
        assertFalse(PhoneNumberOld.isNumeric("+"));
        assertFalse(PhoneNumberOld.isNumeric("+123"));
        assertFalse(PhoneNumberOld.isNumeric("-123"));
        assertFalse(PhoneNumberOld.isNumeric(" 123"));
        assertFalse(PhoneNumberOld.isNumeric("123 "));
        assertFalse(PhoneNumberOld.isNumeric("**798"));

        assertTrue(PhoneNumberOld.isNumeric("0"));
        assertTrue(PhoneNumberOld.isNumeric("123"));
    }

    @Test
    public void testIsNumericOrStartsWithPlusSign() {
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(null));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(""));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(" "));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("a"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("+"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("-123"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(" 123"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("123 "));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("**798"));

        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("0"));
        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("123"));
        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("+123"));
        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("+4972112345678"));
    }

    @Test
    public void testIsValidForReverseLookup() {
        PhoneNumberOld n;

        n = new PhoneNumberOld(PropertyProvider.getInstance(), null, false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), " ", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "a", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "**798", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "1234567@sipgate.de", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), " 1234567", false); // space will be deleted
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "1234567 ", false); // space will be deleted
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "1234567", false);
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "+1234567", false);
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "+497211234567", false);
        assertTrue(n.isValidForReverseLookup());
    }
}
