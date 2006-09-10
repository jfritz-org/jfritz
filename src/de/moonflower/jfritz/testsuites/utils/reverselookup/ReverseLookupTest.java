package de.moonflower.jfritz.testsuites.utils.reverselookup;

import junit.framework.TestCase;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.*;
import de.moonflower.jfritz.utils.reverselookup.*;


/**
 * This class is responsible for testing the Reverselookup functions
 *
 * @author brian jensen
 *
 */
public class ReverseLookupTest extends TestCase {

	public JFritz jfritz;

	public void setUp(){
		jfritz = new JFritz();
	}

	/**
	 * This class tests both local and international italian numbers
	 *
	 *@author brian jensen
	 */
	public void testReverseLookupItaly() {

		//Test know functioning lookup cases
		//These first cases are testing reverse lookup from inside of Italy
		Person result = ReverseLookupItaly.lookup("0226830102");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		result = ReverseLookupItaly.lookup("0655262755");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		result = ReverseLookupItaly.lookup("031642176");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		//test numbers internationally
		result = ReverseLookupItaly.lookup("+3931642176");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

	}

	public void testReverseLookupSwitzerland(){

		//Test known functioning lookup cases
		//These first cases are testing reverse lookup from inside of Switzerland
		Person result = ReverseLookupSwitzerland.lookup("0447712727");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		result = ReverseLookupSwitzerland.lookup("0442425243");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		result = ReverseLookupSwitzerland.lookup("0627750431");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		//Test one result internationally
		result = ReverseLookupSwitzerland.lookup("+41627750431");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

	}

	public void testReverseLookupUSA(){

		//Test known functioning lookup cases
		//These first cases are testing reverse lookup from inside of the USA
		Person result = ReverseLookupUnitedStates.lookup("13202304187");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		result = ReverseLookupUnitedStates.lookup("16503647720");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));

		result = ReverseLookupUnitedStates.lookup("+14104200629");

		assertTrue(!result.getFullname().equals(""));
		assertTrue(!result.getPhoneNumber("home").equals(""));
		assertTrue(!result.getAddress().equals(""));
		assertTrue(!result.getCity().equals(""));


	}
}
