/*
 *
 * Created on 10.04.2005
 */

package de.moonflower.jfritz.exceptions;

/**
 * thrown when the web admin password of the fritz box is invalid
 * @author Arno Willig
 *
 */
public class WrongPasswordException extends Exception {
	private static final long serialVersionUID = 1;

	private int waitSeconds = 0;

	private String affectedBox = "";

    public WrongPasswordException() {
        super();
    }

    public WrongPasswordException(int a, final String affectedBox, final String param, final int waitSeconds) {
        super(param);
        this.waitSeconds = waitSeconds;
        this.affectedBox = affectedBox;
    }

    public int getRetryTime()
    {
    	return waitSeconds;
    }

    public String getAffectedBox()
    {
    	return affectedBox;
    }
}
