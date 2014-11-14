package com.crediteuropebank.vacationsmanager.server.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crediteuropebank.vacationsmanager.shared.exception.CustomValidationException;

/**
 * This class developed to help in validation of the entered values for domain object
 * fields. Fields that should be validated by this class must be annotated with JSR-303 
 * annotations.
 * 
 * @author dimas
 *
 */
@Component(value="validationUtil")
public class ServerValidationUtil {
	
	@Autowired
	private Validator validator;
	
	/**
	 * This function uses for validation domain object (annotated with JSR-303 annotations).
	 * 
	 * @param object - object to be validate.
	 * @return the list of errors (if no error has been found - empty list)
	 * @throws CustomValidationException - is thrown when one or more errors were found during validation
	 */
	public <T> void validate(final T object) throws CustomValidationException {
		List<String> errorMessages = new ArrayList<String>();
		
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, Default.class);
		
		for (ConstraintViolation<T> constraintViolation: constraintViolations) {
			errorMessages.add(constraintViolation.getMessage());
		}
		
		// if at least one error was found then appropriate exception is thrown
		if (errorMessages.size()>0) {
			throw new CustomValidationException(errorMessages);
		}
		
	}
}
