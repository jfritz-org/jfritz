package de.moonflower.jfritz;
/*
 * Created on 10.04.2005
 *
 */
/**
 * thrown when the web admin password of the fritz box is invalid
 * @author Arno Willig
 *
 */
public class WrongPasswordException extends Exception {
    public WrongPasswordException() {
        super();
    }
    public WrongPasswordException(String s) {
        super(s);
    }
}
