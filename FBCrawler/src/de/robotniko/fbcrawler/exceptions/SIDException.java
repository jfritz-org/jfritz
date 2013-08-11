package de.robotniko.fbcrawler.exceptions;

public class SIDException extends Exception {

	private static final long serialVersionUID = -5277288034895752239L;

	public SIDException(String message) {
		super(message);
	}
	
	public SIDException(Throwable cause) {
		super(cause);
	}

	public SIDException(String message, Throwable cause) {
		super(message, cause);
	}
}
