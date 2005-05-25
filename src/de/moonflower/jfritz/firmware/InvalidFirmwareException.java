/*
 *
 * Created on 10.04.2005
 */

package de.moonflower.jfritz.firmware;

/**
 * thrown when the firmware string is invalid
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
