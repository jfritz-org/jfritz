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
public abstract class CallFilter {
    private boolean invert = false;
    private boolean enabled = true;

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
}

