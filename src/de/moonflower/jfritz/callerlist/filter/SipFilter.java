/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public class SipFilter extends CallFilter {

	private Vector<String> sipProviders = new Vector<String>();

	private static final String type = FILTER_SIP;

	private boolean allSelected = false;

	public SipFilter() {
	}

	public boolean passInternFilter(Call currentCall) {

		if ((sipProviders.size() == 0)
			|| allSelected)
		{
			return true;
		}
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
    		result +=";"+sipProviders.elementAt(i);
    	}
    	if (result.startsWith(";"))
    	{
    		result = result.substring(1);
    	}
    	result = result.trim();
    	return result;
    }

	public void setProvider(Vector<String> sipProvider) {
		this.sipProviders = sipProvider;
		allSelected = false;
	}

	public String getType(){
		return type;
	}

	public SipFilter clone(){
		SipFilter sf = new SipFilter();
		sf.setProvider(this.sipProviders);
		sf.setEnabled(this.isEnabled());
		sf.setInvert(this.isInvert());
		return sf;
	}

	public void setAllSelected() {
		allSelected = true;
		sipProviders.clear();
		sipProviders.add("$ALL$");
	}

	public boolean isAllSelected() {
		return allSelected;
	}
}
