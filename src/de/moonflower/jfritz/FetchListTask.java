/*
 *
 * Created on 13.05.2005
 *
 */
package de.moonflower.jfritz;

import java.util.TimerTask;

/**
 * Class for automatic cyclic caller list retrieval
 *
 * @author Arno Willig
 *
 */
public class FetchListTask extends TimerTask {

	JFritz jfritz;

	/**
	 * Set JFritz instance to FetchListTask
	 */
	public FetchListTask(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
	}
	/**
	 * run fetchListTask
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		System.out.println("Running FetchListTask..");
		jfritz.fetchList();
	}

}
