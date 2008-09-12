/*
 * Created on 06.03.2006
 *
 */
package de.moonflower.jfritz;

import java.util.Calendar;
import java.util.Date;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class WatchdogThread extends Thread {

    private int interval = 1; // in seconds

    private Date now, lastTimestamp, startWatchdogTimestamp;

    private Calendar cal;

    private boolean standbyDetected = false;

    private JFritz jfritz;

    /**
     *
     * @param interval
     *            in minutes
     */
    public WatchdogThread(int interval, JFritz jfritz) {
        cal = Calendar.getInstance();
        this.interval = interval;
        startWatchdogTimestamp = cal.getTime();
        lastTimestamp = cal.getTime();
        this.jfritz = jfritz;
    }

    public void run() {
        if (JFritz.getJframe().isCallMonitorStarted()) {
//            Debug.msg("Watchdog: Check call monitor state");
            checkCallmonitor();
//            Debug.msg("Watchdog: Check done");
        }
    }

    private void checkCallmonitor() {
    	Debug.msg("Checking call monitor");
        cal = Calendar.getInstance();
        now = cal.getTime();

        if (now.getTime() - lastTimestamp.getTime() > 3 * interval * 1000
        		|| lastTimestamp.getTime() - now.getTime() > 3 * interval * 1000) {
            // Mind. ein Interval wurde ausgelassen.
            // Computer wahrscheinlich im Ruhezustand gewesen.
            // Starte den Anrufmonitor neu.

            Debug.msg("STANDBY or SUSPEND TO RAM detected"); //$NON-NLS-1$
            Debug.msg("Watchdog: Restarting call monitor"); //$NON-NLS-1$
            standbyDetected = true;
        }

        if ((JFritz.getCallMonitor() != null) && (!JFritz.getCallMonitor().isConnected()))
        {
        	restartCallMonitor(false);
        }

        if (standbyDetected)
        {
        	restartCallMonitor(true);
        	if ((JFritz.getCallMonitor() != null) && (JFritz.getCallMonitor().isConnected()))
        	{
        		if (JFritzUtils.parseBoolean(Main.getProperty("option.watchdog.fetchAfterStandby", "true"))) //$NON-NLS-1$, //$NON-NLS-2$
        			JFritz.getJframe().fetchList(JFritzUtils.parseBoolean(Main.getProperty("option.deleteAfterFetch", "true"))); //$NON-NLS-1$, //$NON-NLS-2$
        		standbyDetected = false; // reset flag, because we have successfully restarted the call monitor
        	}
        }

//        else if (now.getTime() - startWatchdogTimestamp.getTime() > 5*60000) {
////        	Debug.msg("Watchdog: 5 Minuten vorbei. Restarte CallMonitor");
//        	restartCallMonitor();
//        	startWatchdogTimestamp = now;
//        }
        setTimestamp();
    }

    private void setTimestamp() {
        lastTimestamp = now;
    }

    private void restartCallMonitor(boolean showErrorMessage) {
        JFritz.stopCallMonitor();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        jfritz.startChosenCallMonitor(showErrorMessage);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }
}
