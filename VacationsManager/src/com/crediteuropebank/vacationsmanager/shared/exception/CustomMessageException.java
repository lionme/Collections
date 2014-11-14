package com.crediteuropebank.vacationsmanager.shared.exception;

/**
 * This error is thrown when we need to show some message to user on client side. 
 * This message is handled in CustomAsyncCallback class. 
 * The message that we put to exception will be shown to user.
 * 
 * @author DIMAS
 *
 */
public class CustomMessageException extends Exception {

	/**
	 * Default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	public CustomMessageException() {
		super();
	}

	/**
	 * Creates an exception with specified message.
	 * 
	 * @param message
	 */
	public CustomMessageException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a new exception object with specified cause. Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in this throwable's detail message.
     * (This behavior differs from behavior of the same constructor in {@link Throwable}).
	 * 
	 * @param cause - the cause of the current exception
	 */
	public CustomMessageException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
}
