/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.testsuites.callerlist;

import junit.framework.Test;
import junit.framework.TestSuite;

public class All_Callerlist_Tests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites.callerlist");
        //$JUnit-BEGIN$
        suite.addTestSuite(CallerListTest.class);
        //$JUnit-END$
        return suite;
    }

}
