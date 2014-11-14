package com.crediteuropebank.vacationsmanager.server.dao;

/**
 * This exception is similar to Hibernate's and is used for optimistic locking.
 * This exception is thrown when user try to update object, but his version of the object 
 * and version of the object in DB differs because of user has old data.
 * In such case user should refresh his data.
 * 
 * @author DIMAS
 *
 */
public class StaleObjectStateException extends Exception {
	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	public static String ERROR_MESSAGE_TEXT = "You have an old version of data. Please, update your " +
			"data on screen and try operation again.";
	
	/**
	 * Default constructor. Creates exception object with default error message.
	 */
	public StaleObjectStateException() {
		super(ERROR_MESSAGE_TEXT);
	}
	
	/**
	 * Constructor that sets error message.
	 * 
	 * @param message - error message to be set.
	 */
	public StaleObjectStateException(String message) {
		super(message);
	}
}
