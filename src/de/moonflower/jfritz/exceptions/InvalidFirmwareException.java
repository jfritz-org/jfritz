/*
 *
 * Created on 10.04.2005
 */

package de.moonflower.jfritz.exceptions;

/**
 * thrown when a firmware object is invalid
 * @author Arno Willig
 *
 */
public class InvalidFirmwareException extends Exception {
	private static final long serialVersionUID = 1;
    public InvalidFirmwareException() {
        super();
    }
    public InvalidFirmwareException(final String param) {
        super(param);
    }
}
