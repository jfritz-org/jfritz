package de.moonflower.jfritz.network;

import java.util.Vector;

import de.moonflower.jfritz.callerlist.filter.CallFilter;

/**
 * This class is responsible for storing all the permissions for a
 * given jfritz client account
 *
 * @author brian
 *
 */
public class Login {

	public String user, password;

	public boolean allowAddList, allowUpdateList, allowRemoveList, allowAddBook,
			allowUpdateBook, allowRemoveBook, allowLookup, allowGetList,
			allowCallList, allowPhoneBook, allowCallMonitor;

	public Vector<CallFilter> callFilters;

	public String contactFilter;

	public Login(String user, String password, boolean allowCalllist, boolean allowAddlist, boolean allowUpdatelist,
			boolean allowRemovelist,boolean allowPhonebook, boolean allowAddbook, boolean allowUpdatebook,
			boolean allowRemovebook, boolean allowCallmonitor, boolean allowlookup, boolean allowGetlist,
			Vector<CallFilter> CallFilters, String ContactFilter){

		this.user = user;
		this.password = password;
		allowAddList = allowAddlist;
		allowCallList = allowCalllist;
		allowUpdateList = allowUpdatelist;
		allowRemoveList = allowRemovelist;
		allowPhoneBook = allowPhonebook;
		allowAddBook = allowAddbook;
		allowUpdateBook = allowUpdatebook;
		allowRemoveBook = allowRemovebook;
		allowCallMonitor = allowCallmonitor;
		allowLookup = allowlookup;
		allowGetList = allowGetlist;
		callFilters = CallFilters;
		contactFilter = ContactFilter;
	}

	public String getUser(){
		return user;
	}

	public String getPassword(){
		return password;
	}

}
