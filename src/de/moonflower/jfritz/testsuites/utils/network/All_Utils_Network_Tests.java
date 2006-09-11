/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.testsuites.utils.network;

import junit.framework.Test;
import junit.framework.TestSuite;

public class All_Utils_Network_Tests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites.utils.network");
        //$JUnit-BEGIN$
        suite.addTestSuite(FBoxListenerV3Test.class);
        //$JUnit-END$
        return suite;
    }

}
