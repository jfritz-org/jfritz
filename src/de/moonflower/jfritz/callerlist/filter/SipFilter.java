/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;

public class SipFilter extends CallFilter {

    private Vector filteredSipProviders = new Vector();

    public SipFilter() {
        filteredSipProviders.clear();
        String providers = JFritz.getProperty("filter.sipProvider", //$NON-NLS-1$
                "[]"); //$NON-NLS-1$
        providers = providers.replaceAll("\\[", ""); //$NON-NLS-1$,  //$NON-NLS-2$
        providers = providers.replaceAll("\\]", ""); //$NON-NLS-1$,  //$NON-NLS-2$
        String[] providerEntries = providers.split(","); //$NON-NLS-1$
        for (int i = 0; i < providerEntries.length; i++) {
            if (providerEntries[i].length() > 0) {
                if (providerEntries[i].charAt(0) == 32) { // delete
                    // first SPACE
                    providerEntries[i] = providerEntries[i].substring(1);
                }
            }
            filteredSipProviders.add(providerEntries[i]);
        }
    }

    public boolean filterPassed(Call currentCall) {
        if ( filteredSipProviders.size() != 0 ) {
            String route = currentCall.getRoute();
            if (route.equals("")) { //$NON-NLS-1$
                route = "FIXEDLINE"; //$NON-NLS-1$
            }
            if (filteredSipProviders.contains(route))
                return true;
            else
                return false;
        } else
            return true;
    }

    public void setFilter(int filter) {

        filteredSipProviders.clear();
        try {
            int rows[] = JFritz.getJframe().getCallerTable().getSelectedRows();
            for (int i = 0; i < rows.length; i++) {
                Call call = (Call) JFritz.getCallerlist()
                        .getFilteredCallVector().get(rows[i]);
                String route = call.getRoute();
                if (route.equals("")) { //$NON-NLS-1$
                    route = "FIXEDLINE"; //$NON-NLS-1$
                }
                if (!filteredSipProviders.contains(route)) {
                    filteredSipProviders.add(route);
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        JFritz.setProperty(
                "filter.sipProvider", filteredSipProviders.toString()); //$NON-NLS-1$
        JFritz.getCallerlist().updateFilter();
    }

}
