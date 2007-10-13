/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public class CallByCallFilter extends CallFilter {

    private static final String type = FILTER_CALLBYCALL;

	private Vector<String> filteredCallByCallProviders = new Vector<String>();

    public CallByCallFilter(Vector<String> providers) {
    	filteredCallByCallProviders = providers;
    }

    public boolean passInternFilter(Call currentCall) {
        if (currentCall.getPhoneNumber() != null) {
            String currentProvider = currentCall.getPhoneNumber()
                    .getCallByCall();
//            Debug.msg("currentProvider: "+currentProvider);
            if (currentProvider.equals("")) { //$NON-NLS-1$
                return false;
            }
            if (filteredCallByCallProviders
                    .contains(currentProvider)) {
                return true;
            }
        }
        return false;
    }
    public String toString(){
    	String result="";
    	for(int i =0; i<filteredCallByCallProviders.size();i++){
    		result +=" "+filteredCallByCallProviders.elementAt(i);
    	}
    	return result;
    }

    public Vector<String> getCallbyCallProviders(){
    	return this.filteredCallByCallProviders;
    }

	public void setCallbyCallProvider(Vector<String> callByCallProvider) {
		filteredCallByCallProviders = callByCallProvider;
	}

	public String getType(){
		return type;
	}

	public CallByCallFilter clone(){
		CallByCallFilter cbcf = new CallByCallFilter((Vector<String>) this.filteredCallByCallProviders.clone());
		cbcf.setEnabled(this.isEnabled());
		cbcf.setInvert(this.isInvert());
		return cbcf;

	}

}
