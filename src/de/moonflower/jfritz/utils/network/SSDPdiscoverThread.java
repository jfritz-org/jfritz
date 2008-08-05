/*
 * Created on 24.05.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.StatusBarController;

/**
 * @author Arno Willig
 *
 */
public class SSDPdiscoverThread extends Thread {

	int timeout;
	private StatusBarController statusBarController = new StatusBarController();

	Vector devices;

	/**
	 * Constructs SSDPdiscoverThread
	 *
	 * @param timeout
	 */
	public SSDPdiscoverThread(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		statusBarController.fireStatusChanged(Main.getMessage("detect_boxes")); //$NON-NLS-1$
		JFritz.getJframe().setBusy(true);

		devices = UPNPUtils.SSDP_discoverFritzBoxes(timeout);
		JFritz.getJframe().setBusy(false);
		statusBarController.fireStatusChanged("");
		Debug.msg("Discover thread");
	}

	/**
	 * @return Returns the fritz box devices.
	 */
	synchronized public final Vector getDevices() {
		return devices;
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}
}
