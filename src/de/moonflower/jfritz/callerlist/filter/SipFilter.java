/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public class SipFilter extends CallFilter {

	private Vector filteredSipProviders = new Vector();

	public SipFilter(Vector providers) {
		this.filteredSipProviders = providers;
	}

	public boolean passInternFilter(Call currentCall) {

		if (filteredSipProviders.size() == 0)
			return true;
			String route = currentCall.getRoute();
			if (route.equals("")) { //$NON-NLS-1$
				route = "FIXEDLINE"; //$NON-NLS-1$
			}
			if (filteredSipProviders.contains(route))
				return true;
			else
				return false;
	}
    public String toString(){
    	String result="";
    	for(int i =0; i<filteredSipProviders.size();i++){
    		result +=" "+filteredSipProviders.elementAt(i);
    	}
    	return result;
    }

}
