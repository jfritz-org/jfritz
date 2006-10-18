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
 * @author marc
 *
 */
public abstract class CallFilter {
    private boolean invert = false;
    private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public final boolean passFilter(Call currentCall){
		if(!enabled){return true;}
		if(invert){
			return !passInternFilter(currentCall);
		}
		return passInternFilter(currentCall);
	}

	abstract boolean passInternFilter(Call currentCall);

	public void setInvert(boolean inv){
    	invert = inv;
    }

	public boolean isInvert() {
		return invert;
	}
}

