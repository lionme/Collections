package com.crediteuropebank.vacationsmanager.server.dblogging;

/**
 * 
 * This exception is thrown when user forget to put annotation where it is required.
 * 
 * @author DIMAS
 * 
 */
public class MissedAnnotationException extends RuntimeException {

	/**
	 * default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs new {@link MissedAnnotationException} with specified message.
	 * 
	 * @param message
	 */
	public MissedAnnotationException(String message) {
		super(message);
	}
}
