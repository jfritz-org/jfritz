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
    public WrongPasswordException() {
        super();
    }
    public WrongPasswordException(final String param) {
        super(param);
    }
}
