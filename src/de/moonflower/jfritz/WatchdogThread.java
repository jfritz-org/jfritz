/*
 * Created on 06.03.2006
 *
 */
package de.moonflower.jfritz;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class WatchdogThread extends Thread {
	private final static Logger log = Logger.getLogger(WatchdogThread.class);

    private int interval = 1; // in seconds
    private int factor = 10;

    private Date now, lastTimestamp;

    private Calendar cal;

    private boolean standbyDetected = false;

    private int watchdogCalls = 0;

    protected PropertyProvider properties = PropertyProvider.getInstance();
    /**
     *
     * @param interval
     *            in seconds
     */
    public WatchdogThread(int interval, int factor) {
        cal = Calendar.getInstance();
        this.interval = interval;
        this.factor = factor;
        lastTimestamp = cal.getTime();
    }

    public void run() {
    	if (JFritz.getJframe().isCallMonitorStarted())
    	{
    		if (watchdogCalls == factor)
    		{
    			watchdogCalls = 0;
    			checkCallMonitor();
    		}
    		watchdogCalls++;
    	}
    }

    private void checkCallMonitor() {
//    	Debug.debug("Checking STANDBY");
        cal = Calendar.getInstance();
        now = cal.getTime();

        if (now.getTime() - lastTimestamp.getTime() > 3 * interval * factor * 1000
        		|| lastTimestamp.getTime() - now.getTime() > 3 * interval * factor * 1000) {
            // Mind. ein Interval wurde ausgelassen.
            // Computer wahrscheinlich im Ruhezustand gewesen.
            // Starte den Anrufmonitor neu.

            Debug.info(log, "STANDBY or SUSPEND TO RAM detected"); //$NON-NLS-1$
			JFritz.getJframe().setBoxDisconnected("");
            standbyDetected = true;
        }

        if (standbyDetected)
        {
        	JFritz.getBoxCommunication().refreshLogin(null);
        	
        	Debug.debug(log, "Restarting call monitor due to STANDBY/SUSPEND TO RAM");
			restartCallMonitor(true);
    		if (JFritzUtils.parseBoolean(properties.getProperty("option.watchdog.fetchAfterStandby"))) //$NON-NLS-1$, //$NON-NLS-2$
    		{
    			Timer timer = new Timer("Standby-Timer: Fetch-List", true);
    			timer.schedule(new TimerTask() {

					@Override
					public void run() {
						Debug.debug(log, "Fetching caller list due to STANDBY/SUSPEND TO RAM");
	        			JFritz.getJframe().fetchList(null, false);
					}

    			}, 10000);
    		}
    		standbyDetected = false; // reset flag, because we have successfully restarted the call monitor
        }
        setTimestamp();
    }

    private void setTimestamp() {
        lastTimestamp = now;
    }

    private void restartCallMonitor(boolean showErrorMessage) {
    	JFritz.getBoxCommunication().stopCallMonitor();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        JFritz.getBoxCommunication().startCallMonitor();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }
}
