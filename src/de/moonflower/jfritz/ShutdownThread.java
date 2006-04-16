/*
 * Created on 20.07.2005
 */
package de.moonflower.jfritz;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 */
public class ShutdownThread extends Thread {

	private JFritz jfritz;

	public ShutdownThread(JFritz jfritz) {
		this.jfritz = jfritz;
	}

	public void run() {

		Debug.msg("Starting shutdown thread.."); //$NON-NLS-1$

		if (jfritz.getJframe() != null) {
			/*
		  	fritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
			jfritz.getCallerlist().saveToXMLFile(JFritz.CALLS_FILE, true);
			*/

			JFritz.setProperty("jfritz.isRunning","false"); //$NON-NLS-1$, //$NON-NLS-2$
			Debug.msg("Multiple instance lock: release lock."); //$NON-NLS-1$
			jfritz.saveProperties();
			jfritz.getJframe().saveQuickDials();

			if (jfritz.getCallMonitor() != null) {
				jfritz.getCallMonitor().stopCallMonitor();
			}
		}
		Debug.msg("Shutdown thread done."); //$NON-NLS-1$

	}
}
