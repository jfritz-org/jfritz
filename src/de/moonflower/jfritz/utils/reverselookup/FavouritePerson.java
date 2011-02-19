package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.ReverseLookupSite;

public class FavouritePerson {
	private Vector<ReverseLookupSite> rls_list;

	int numMostFilledFields = -1;
	Person mostFilledFieldsPerson = null;
	int mostFilledFieldsLookupSite = 999;

	public FavouritePerson(final Vector<ReverseLookupSite> rls_list) {
		this.rls_list = rls_list;
	}

	public boolean addPerson(final Person p) {
		boolean newFavouritePerson = false;
		int tmp = p.getNumFilledFields();
		if (tmp>numMostFilledFields) {
			newFavouritePerson = setNewFavourite(p, tmp);
		} else if (tmp == numMostFilledFields) {
			if (getLookupIndexByString(p.getLookupSite()) < mostFilledFieldsLookupSite) {
				newFavouritePerson = setNewFavourite(p, tmp);
			}
		}

		return newFavouritePerson;
	}

	private boolean setNewFavourite(final Person p, int tmp) {
		boolean newFavouritePerson;
		numMostFilledFields = tmp;
		mostFilledFieldsPerson = p;
		mostFilledFieldsLookupSite = getLookupIndexByString(p.getLookupSite());
		newFavouritePerson = true;
		return newFavouritePerson;
	}

	public int getLookupIndexByString(final String lookupSiteName) {
		int result = -1;
		for (int i=0; i<rls_list.size(); i++)
		{
			String tmpSite = rls_list.get(i).getName();
			if (tmpSite.equals(lookupSiteName)) {
				result=i;
			}
		}
		return result;
	}

	public Person getFavourite() {
		return this.mostFilledFieldsPerson;
	}
}
