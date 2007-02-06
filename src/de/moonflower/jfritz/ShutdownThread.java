/*
 * Created on 20.07.2005
 */
package de.moonflower.jfritz;

import java.io.File;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 */
public class ShutdownThread extends Thread {

	public void run() {

		Debug.msg("Starting shutdown thread.."); //$NON-NLS-1$
		Debug.msg("Shutdown thread done."); //$NON-NLS-1$

	}
}
