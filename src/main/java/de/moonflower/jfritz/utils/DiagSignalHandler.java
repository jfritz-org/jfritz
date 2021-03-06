package de.moonflower.jfritz.utils;

import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

// Diagnostic Signal Handler class definition
public class DiagSignalHandler implements SignalHandler {
	private final static Logger log = Logger.getLogger(DiagSignalHandler.class);

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
        	log.error(iae.toString());
        	return null;
        }
    }

    // Signal handler method
    public void handle(Signal sig) {
        log.info("Diagnostic Signal handler called for signal "+sig);
        try {
            // Output information for each thread
            Thread[] threadArray = new Thread[Thread.activeCount()];
            int numThreads = Thread.enumerate(threadArray);
            log.debug("Current threads:");
            for (int i=0; i < numThreads; i++) {
                log.debug("    "+threadArray[i]);
            }

            // Chain back to previous handler, if one exists
            if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
                oldHandler.handle(sig);
            }
        } catch (Exception e) {
            log.error("Signal handler failed, reason "+e);
        }
    }
}
