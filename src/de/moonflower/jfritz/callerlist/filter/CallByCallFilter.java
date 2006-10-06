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

    	/*String providers = JFritz.getProperty(
                    "filter.callbycallProvider", "[]"); //$NON-NLS-1$,  //$NON-NLS-2$

            providers = providers.replaceAll("\\[", ""); //$NON-NLS-1$,  //$NON-NLS-2$
            providers = providers.replaceAll("\\]", ""); //$NON-NLS-1$,  //$NON-NLS-2$
            String[] providerEntries = providers.split(","); //$NON-NLS-1$
            for (int i = 0; i < providerEntries.length; i++) {
                if (providerEntries[i].length() > 0) {
                    if (providerEntries[i].charAt(0) == 32) { // delete
                        // first SPACE
                        providerEntries[i] = providerEntries[i]
                                .substring(1);
                    }
                }
                filteredCallByCallProviders.add(providerEntries[i]);
            }
          */
    	filteredCallByCallProviders = providers;
    }

    public boolean passFilter(Call currentCall) {
        if (currentCall.getPhoneNumber() != null) {
            String currentProvider = currentCall.getPhoneNumber()
                    .getCallByCall();
            if (currentProvider.equals("")) { //$NON-NLS-1$
                currentProvider = "NONE"; //$NON-NLS-1$
            }
            if (!filteredCallByCallProviders
                    .contains(currentProvider)) {
                return false;
            }
        } else { // Hide calls without number
            if (!filteredCallByCallProviders.contains("NONE")) { //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }
}
