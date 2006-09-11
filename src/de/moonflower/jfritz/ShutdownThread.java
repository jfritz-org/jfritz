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

		if (JFritz.getJframe() != null) {
            if (JFritz.isInstanceControlEnabled()) {
                File f = new File( JFritz.SAVE_DIR + JFritz.LOCK_FILE );
                if ( f.exists() )
                    {
                        f.delete();
                    }
                Debug.msg("Multiple instance lock: release lock."); //$NON-NLS-1$
            }
			JFritz.saveProperties();
            JFritz.getJframe().saveQuickDials();

			if (JFritz.getCallMonitor() != null) {
                JFritz.getCallMonitor().stopCallMonitor();
			}
		}
		Debug.msg("Shutdown thread done."); //$NON-NLS-1$

	}
}
