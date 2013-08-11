package de.robotniko.fbcrawler.exceptions;

public class WrongPasswordException extends Exception {

	private static final long serialVersionUID = -5277288034895752239L;

	public WrongPasswordException(String message) {
		super(message);
	}
	
	public WrongPasswordException(Throwable cause) {
		super(cause);
	}

	public WrongPasswordException(String message, Throwable cause) {
		super(message, cause);
	}
}
