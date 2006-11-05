/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public class SipFilter extends CallFilter {

	private Vector sipProviders = new Vector();

	public SipFilter(Vector providers) {
		this.sipProviders = providers;
	}

	public boolean passInternFilter(Call currentCall) {

		if (sipProviders.size() == 0)
			return true;
			String route = currentCall.getRoute();
//			Debug.msg("route: "+route);
			if (route.equals("")) { //$NON-NLS-1$
				return false;
			}
			if (sipProviders.contains(route))
				return true;
			else
				return false;
	}

	public String toString(){
    	String result="";
    	for(int i =0; i<sipProviders.size();i++){
    		result +=" "+sipProviders.elementAt(i);
    	}
    	return result;
    }

	public void setProvider(Vector sipProvider) {
		this.sipProviders = sipProvider;

	}

}
