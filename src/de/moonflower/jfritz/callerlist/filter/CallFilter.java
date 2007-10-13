/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

/**
 * Call Filter if a call should get throught tht filter the passFilter method must
 * return true.
 * A yaf-Filter will only return true, if he is disabled, or the call has the yaf-Property
 * A filter can be enabled or disabled, if he is disabled, every call will pass
 * A filter can be inverted or not, if he is inverted he will return the exact opposite
 * of the value he would normally return. If you create a fiter he is enabled and not inverted
 * @author marc
 *
 */
public abstract class CallFilter implements Cloneable {
    private boolean invert = false;
    private boolean enabled = true;

	public static final String FILTER_CALLBYCALL = "filter_callbycall";

	public static final String FILTER_CALLIN_NOTHING = "filter_callin";

	public static final String FILTER_CALLINFAILED = "filter_callinfailed";

	public static final String FILTER_CALLOUT = "filter_callout";

	public static final String FILTER_COMMENT = "filter_comment";

	public static final String FILTER_DATE = "filter_date";

	public static final String FILTER_FIXED = "filter_fixed";

	public static final String FILTER_HANDY = "filter_handy";

	public static final String FILTER_ANONYM = "filter_number";

	public static final String FILTER_SEARCH = "filter_search";

	public static final String FILTER_SEARCH_TEXT = "filter_search.text";

	public static final String FILTER_SIP = "filter_sip";

	public static final String FILTER_DATE_END = "FILTER_DATE_END";

	public static final String FILTER_DATE_START = "FILTER_DATE_START";

	public static final String THIS_DAY = "date_filter.today";

	public static final String LAST_DAY = "date_filter.yesterday";

	public static final String THIS_WEEK = "date_filter.this_week";

	public static final String LAST_WEEK = "date_filter.last_week";

	public static final String THIS_MONTH = "date_filter.this_month";

	public static final String LAST_MONTH = "date_filter.last_month";

	public static final String FILTER_DATE_SPECIAL = "date_filter.special";


    /**
     * @return true if the Filter is enabled, else false
     */
	public boolean isEnabled() {
		return enabled;
	}
/**
 *
 * @param enabled true if you want to enable the filter, false if you want to disable the filter
 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
/**
 * This method either return true, if the filter is passed or false if the call cannot pass the filter
 * @param currentCall the call to be checked
 * @return true if the call can pass the filter
 */
	public final boolean passFilter(Call currentCall){
		if(!enabled){return true;}
		if(invert){
			return !passInternFilter(currentCall);
		}
		return passInternFilter(currentCall);
	}

	/**
	 *  All filters must implement this function to determine, if the call can pass
	 *  or not.
	 * @param currentCall the call to be checked
	 * @return true if the call can pass the filter
	 */
	abstract boolean passInternFilter(Call currentCall);
	/**
	 * a inverted filter will return the exact opposite
	 * @param inv true to invert false for normal filter usage
	 */
	public void setInvert(boolean inv){
    	invert = inv;
    }
    /**
     * @return true if the filter is inverted, false if the filter is not inverted
     */
	public boolean isInvert() {
		return invert;
	}

	public abstract String getType();

	public abstract CallFilter clone();

}

