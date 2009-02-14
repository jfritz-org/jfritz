package de.moonflower.jfritz.testsuites.phonebook;

import junit.framework.Test;
import junit.framework.TestSuite;

public class All_Phonebook_Tests {
    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites.phonebook");
        //$JUnit-BEGIN$
        suite.addTestSuite(NumberMultiHashMapTest.class);
        //$JUnit-END$
        return suite;
    }
}
