package de.moonflower.jfritz.utils;

import sun.misc.Signal;
import sun.misc.SignalHandler;

// Diagnostic Signal Handler class definition
public class DiagSignalHandler implements SignalHandler {

    private SignalHandler oldHandler;


    // Static method to install the signal handler
    public static DiagSignalHandler install(String signalName) {
        try {
        	Signal diagSignal = new Signal(signalName);
        	DiagSignalHandler diagHandler = new DiagSignalHandler();
        	diagHandler.oldHandler = Signal.handle(diagSignal,diagHandler);
        	return diagHandler;
        } catch (IllegalArgumentException iae)
        {
        	Debug.error(iae.toString());
        	return null;
        }
    }

    // Signal handler method
    public void handle(Signal sig) {
        Debug.info("Diagnostic Signal handler called for signal "+sig);
        try {
            // Output information for each thread
            Thread[] threadArray = new Thread[Thread.activeCount()];
            int numThreads = Thread.enumerate(threadArray);
            Debug.debug("Current threads:");
            for (int i=0; i < numThreads; i++) {
                Debug.debug("    "+threadArray[i]);
            }

            // Chain back to previous handler, if one exists
            if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
                oldHandler.handle(sig);
            }
        } catch (Exception e) {
            Debug.error("Signal handler failed, reason "+e);
        }
    }
}
