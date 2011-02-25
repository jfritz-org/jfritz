/*
 * Created on 13.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class DisconnectMonitor extends CallMonitorAdaptor {

	protected PropertyProvider properties = PropertyProvider.getInstance();

    public void endOfCall(Call call) {
        if (JFritzUtils.parseBoolean(properties.getProperty(
                "option.callmonitor.fetchAfterDisconnect"))) //$NON-NLS-1$,  //$NON-NLS-2$
        {
            Debug.info("Fetch callerlist at end of call");
            JFritz.getJframe().fetchList(null, false);
        }
    }
}
