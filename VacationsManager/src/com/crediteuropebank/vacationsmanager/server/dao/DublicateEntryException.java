package com.crediteuropebank.vacationsmanager.server.dao;

/**
 * 
 * This exception means that record with unique for application field (in database it can't be unique because of
 * status column) already exists in DB.
 * 
 * @author dimas
 *
 */
public class DublicateEntryException extends RuntimeException {

	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@link DublicateEntryException} with null as its detail message. 
	 */
	public DublicateEntryException() {
		super();
	}

	/**
	 * Constructs a new {@link DublicateEntryException} with specified detailed message.
	 * 
	 * @param message
	 */
	public DublicateEntryException(String message) {
		super(message);
	}	
	
}
