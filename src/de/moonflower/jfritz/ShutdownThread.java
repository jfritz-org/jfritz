/*
 * Created on 20.07.2005
 */
package de.moonflower.jfritz;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 */
public class ShutdownThread extends Thread {
	Main jfritzMain;

	public ShutdownThread(Main main) {
		super("ShutdownThread");
		this.setDaemon(true);
		jfritzMain = main;
	}

	public void run() {
		Debug.info("Starting shutdown thread.."); //$NON-NLS-1$
		jfritzMain.prepareShutdown(true, false);
		Debug.info("Shutdown thread done."); //$NON-NLS-1$
	}
}
