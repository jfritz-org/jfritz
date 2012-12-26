package de.moonflower.jfritz.box.fritzbox.query;

import java.util.Vector;

import de.moonflower.jfritz.box.fritzbox.FritzBox;

public class QueryFactory {

	private static final String QUERY_GET_VERSION = "logic:status/nspver";

	public static IQuery getQueryMethodForFritzBox(FritzBox fritzBox) {

		Vector<String> query = new Vector<String>();
		query.add(QUERY_GET_VERSION);

		Vector<String> response = new Vector<String>();
		IQuery queryOld = new QueryOld(fritzBox);
		IQuery queryNew = new QueryNew(fritzBox);

		if (((response = queryOld.getQuery(query)).size() != 0)
				&& (!"".equals(response.get(0)))) {
			return queryOld;
		} else if (((response = queryNew.getQuery(query)).size() != 0)
				&& (!"".equals(response.get(0)))) {
			return queryNew;
		} else {
			return queryNew;
		}
	}
}
