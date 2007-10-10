package de.moonflower.jfritz.network;

import java.util.Vector;

import de.moonflower.jfritz.callerlist.filter.CallFilter;

/**
 * This class is responsible for storing all the permissions for a
 * given jfritz client
 *
 * @author brian
 *
 */
public class Login {

	public String user, password;

	public boolean allowAddList, allowUpdateList, allowRemoveList, allowAddBook,
			allowUpdateBook, allowRemoveBook, allowLookup, allowGetList;

	public Vector<CallFilter> callFilters;

	public String contactFilter;

	public Login(String user, String password, boolean allowAddlist, boolean allowUpdatelist,
			boolean allowRemovelist, boolean allowAddbook, boolean allowUpdatebook,
			boolean allowRemovebook, boolean allowlookup, boolean allowGetlist,
			Vector<CallFilter> CallFilters, String ContactFilter){

		this.user = user;
		this.password = password;
		allowAddList = allowAddlist;
		allowUpdateList = allowUpdatelist;
		allowRemoveList = allowRemovelist;
		allowAddBook = allowAddbook;
		allowUpdateBook = allowUpdatebook;
		allowRemoveBook = allowRemovebook;
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
