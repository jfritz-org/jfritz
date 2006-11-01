/*
 * Created on 13.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class DisconnectMonitor extends CallMonitorAdaptor {

    public void endOfCall(Call call) {
        if (JFritzUtils.parseBoolean(Main.getProperty(
                "option.callmonitor.fetchAfterDisconnect", "false"))) //$NON-NLS-1$,  //$NON-NLS-2$
        {
            Debug.msg("Fetch callerlist at end of call");
            JFritz.getJframe().fetchList();
        }
    }
}
