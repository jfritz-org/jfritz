/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.testsuites.utils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class All_Utils_Tests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites.utils");
        //$JUnit-BEGIN$
        suite.addTestSuite(JFritzUtilsTest.class);
        //$JUnit-END$
        return suite;
    }

}
