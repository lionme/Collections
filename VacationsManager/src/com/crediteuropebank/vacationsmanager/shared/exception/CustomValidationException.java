package com.crediteuropebank.vacationsmanager.shared.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of validation exceptions. This exception should be caught on client side and 
 * translated to warning message for user.
 * 
 * @author dimas
 *
 */
public class CustomValidationException extends CustomMessageException {
	private List<String> errorMessages;

	/**
	 * Default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	public CustomValidationException() {
		this.errorMessages = new ArrayList<String>();
	}
	
	public CustomValidationException(List<String> errorMessages) {
		super(errorMessages.toString());
		this.errorMessages = errorMessages;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

}
