/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.testsuites;

import de.moonflower.jfritz.callmonitor.All_Callmonitor_Tests;
import de.moonflower.jfritz.testsuites.callerlist.All_Callerlist_Tests;
import de.moonflower.jfritz.testsuites.phonebook.All_Phonebook_Tests;
import de.moonflower.jfritz.testsuites.utils.All_Utils_Tests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.moonflower.jfritz.testsuites");
        //$JUnit-BEGIN$
        suite.addTest(All_Callerlist_Tests.suite());
        suite.addTest(All_Utils_Tests.suite());
        suite.addTest(All_Callmonitor_Tests.suite());
//        suite.addTest(All_Utils_Reverselookup_Tests.suite());
        suite.addTest(All_Phonebook_Tests.suite());
        //$JUnit-END$
        return suite;
    }

}
