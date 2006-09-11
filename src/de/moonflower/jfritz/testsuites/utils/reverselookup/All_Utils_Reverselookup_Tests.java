/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.testsuites.utils.reverselookup;

import junit.framework.Test;
import junit.framework.TestSuite;

public class All_Utils_Reverselookup_Tests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites.utils.reverselookup");
        //$JUnit-BEGIN$
        suite.addTestSuite(ReverseLookupTest.class);
        //$JUnit-END$
        return suite;
    }

}
