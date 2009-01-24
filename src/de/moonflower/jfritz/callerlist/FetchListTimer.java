package de.moonflower.jfritz.callerlist;

import java.util.Timer;
import java.util.TimerTask;

public class FetchListTimer extends Timer {

	public final static int STATE_INITIALIZED = 0;

	public final static int STATE_SCHEDULED = 1;

	public final static int STATE_CANCELED = 2;

	private int state;

    public FetchListTimer(String name, boolean isDaemon) {
    	super(name, isDaemon);
    	state = STATE_INITIALIZED;
    }

    public void schedule(TimerTask task, long delay, long period) {
        state = STATE_SCHEDULED;
        super.schedule(task, delay, period);
    }

    public void cancel()
    {
    	state = STATE_CANCELED;
    	super.cancel();
    }

    public int getState()
    {
    	return state;
    }
}
