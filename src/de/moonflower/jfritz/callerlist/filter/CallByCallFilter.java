/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;

public class CallByCallFilter extends CallFilter {

    private Vector filteredCallByCallProviders = new Vector();

    public CallByCallFilter(JFritz jfritz) {
        super(jfritz);
            String providers = JFritz.getProperty(
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

    }

    public boolean filterPassed(Call currentCall) {
        System.err.println(filteredCallByCallProviders);
        if (currentCall.getPhoneNumber() != null) {
            String callbycallprovider = currentCall.getPhoneNumber()
                    .getCallByCall();
            if (callbycallprovider.equals("")) { //$NON-NLS-1$
                callbycallprovider = "NONE"; //$NON-NLS-1$
            }
            if (!filteredCallByCallProviders
                    .contains(callbycallprovider)) {
                return false;
            }
        } else { // Hide calls without number
            if (!filteredCallByCallProviders.contains("NONE")) { //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    public void setFilter(int filter) {
        filteredCallByCallProviders.clear();
        try {
            String provider = ""; //$NON-NLS-1$
            int rows[] = jfritz.getJframe().getCallerTable().getSelectedRows();
            if (rows.length != 0) { // Filter only selected rows
                for (int i = 0; i < rows.length; i++) {
                    Call call = (Call) jfritz.getCallerlist()
                            .getFilteredCallVector().get(rows[i]);
                    if (call.getPhoneNumber() != null) {
                        provider = call.getPhoneNumber().getCallByCall();
                        if (provider.equals("")) { //$NON-NLS-1$
                            provider = "NONE"; //$NON-NLS-1$
                        }
                    } else {
                        provider = "NONE"; //$NON-NLS-1$
                    }
                    if (!filteredCallByCallProviders.contains(provider)) {
                        filteredCallByCallProviders.add(provider);
                    }
                }
            } else { // filter only calls with callbycall predial
                for (int i = 0; i < jfritz.getCallerlist()
                        .getFilteredCallVector().size(); i++) {
                    Call call = (Call) jfritz.getCallerlist()
                            .getFilteredCallVector().get(i);
                    if (call.getPhoneNumber() != null) {
                        provider = call.getPhoneNumber().getCallByCall();
                    }
                    if (!provider.equals("")) { //$NON-NLS-1$
                        if (!filteredCallByCallProviders.contains(provider)) {
                            filteredCallByCallProviders.add(provider);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        JFritz.setProperty("filter.callbycallProvider", filteredCallByCallProviders //$NON-NLS-1$
                .toString());
        jfritz.getCallerlist().updateFilter();
    }

}
