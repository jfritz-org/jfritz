/*
 * Created on 24.05.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.utils.StatusBarController;

/**
 * @author Arno Willig
 *
 */
public class SSDPdiscoverThread extends Thread {
	private final static Logger log = Logger.getLogger(SSDPdiscoverThread.class);

	int timeout;
	private StatusBarController statusBarController = new StatusBarController();
	protected MessageProvider messages = MessageProvider.getInstance();

	Vector<SSDPPacket> devices;

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
		statusBarController.fireStatusChanged(messages.getMessage("detect_boxes")); //$NON-NLS-1$
		JFritzWindow jframe = JFritz.getJframe();
		if (jframe != null) {
			jframe.setBusy(true);
		}

		devices = UPNPUtils.SSDP_discoverFritzBoxes(timeout);
		if (jframe != null) {
			jframe.setBusy(false);
		}
		statusBarController.fireStatusChanged("");
		log.info("Discover thread");
	}

	/**
	 * @return Returns the fritz box devices.
	 */
	synchronized public final Vector<SSDPPacket> getDevices() {
		return devices;
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}
}
