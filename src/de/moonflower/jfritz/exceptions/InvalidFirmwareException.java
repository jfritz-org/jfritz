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
    public InvalidFirmwareException() {
        super();
    }
    public InvalidFirmwareException(String s) {
        super(s);
    }
}
