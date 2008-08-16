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
		super();
		jfritzMain = main;
	}

	public void run() {
		Debug.msg("Starting shutdown thread.."); //$NON-NLS-1$
		jfritzMain.prepareShutdown(true, false);
		Debug.msg("Shutdown thread done."); //$NON-NLS-1$
	}
}
