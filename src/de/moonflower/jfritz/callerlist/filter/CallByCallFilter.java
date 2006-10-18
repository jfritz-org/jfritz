/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public class CallByCallFilter extends CallFilter {

    private Vector filteredCallByCallProviders = new Vector();

    public CallByCallFilter(Vector providers) {
    	filteredCallByCallProviders = providers;
    }

    public boolean passInternFilter(Call currentCall) {
        if (currentCall.getPhoneNumber() != null) {
            String currentProvider = currentCall.getPhoneNumber()
                    .getCallByCall();
            if (currentProvider.equals("")) { //$NON-NLS-1$
                currentProvider = "NONE"; //$NON-NLS-1$
            }
            if (filteredCallByCallProviders
                    .contains(currentProvider)) {
                return true;
            }
        } else { // Hide calls without number
            if (filteredCallByCallProviders.contains("NONE")) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }
}
