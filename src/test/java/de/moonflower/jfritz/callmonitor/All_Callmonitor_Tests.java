/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import junit.framework.Test;
import junit.framework.TestSuite;

public class All_Callmonitor_Tests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites.utils.network");
        //$JUnit-BEGIN$
        suite.addTestSuite(DisplayCallsMonitorTest.class);
        suite.addTestSuite(FBoxListenerV3Test.class);
        suite.addTestSuite(MonitoredCallsTest.class);
        //$JUnit-END$
        return suite;
    }

}
