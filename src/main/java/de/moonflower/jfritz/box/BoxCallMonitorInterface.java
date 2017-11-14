package de.moonflower.jfritz.box;

import java.util.Vector;

import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;

public interface BoxCallMonitorInterface {

	public static final int CALLMONITOR_STARTED = 0;

	public static final int CALLMONITOR_FIRMWARE_INCOMPATIBLE = 1;

	public static final int CALLMONITOR_NOT_CONFIGURED = 2;

	/**
	 * Starts selected call monitor.
	 * @param listener Vector of state listeners.
	 * @return Return value indicating success or failure.
	 * @return CALLMONITOR_STARTED if call monitor could be started successfully.
	 * @return CALLMONITOR_FIRMWARE_INCOMPATIBLE if firmware is incompatible.
	 * @return CALLMONITOR_NOT_CONFIGURED if call monitor has not been configured properly.
	 */
	public int startCallMonitor(Vector<CallMonitorStatusListener> listener);

	/**
	 * Stop call monitor
	 * @param listener Vector of state listeners.
	 */
	public void stopCallMonitor(Vector<CallMonitorStatusListener> listener);

	/**
	 * Returns true if call monitor is running, false otherwise.
	 * @return
	 */
	public boolean isCallMonitorConnected();
}
