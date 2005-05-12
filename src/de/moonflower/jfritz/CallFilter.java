/*
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz;

/**
 * Manages a CallType Filter
 *
 * @author Arno Willig
 */
public class CallFilter {

	boolean filterCallIn,filterCallInFailed,filterCallOut;


	/**
	 * Creates new CallType Filter
	 *
	 * @param filterCallIn
	 * @param filterCallInFailed
	 * @param filterCallOut
	 */
	public CallFilter(boolean filterCallIn, boolean filterCallInFailed,
			boolean filterCallOut) {
		super();
		this.filterCallIn = filterCallIn;
		this.filterCallInFailed = filterCallInFailed;
		this.filterCallOut = filterCallOut;
	}

	public boolean isCallFiltered(Call c) {
		int ct = c.getCalltype().toInt();
		if ((ct == CallType.CALLIN) && (filterCallIn)) return false;
		else if ((ct == CallType.CALLIN_FAILED) && (filterCallInFailed)) return false;
		else if ((ct == CallType.CALLOUT) && (filterCallOut)) return false;
		else return true;
	}


}
