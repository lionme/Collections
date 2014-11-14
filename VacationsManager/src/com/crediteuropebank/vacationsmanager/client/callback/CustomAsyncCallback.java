package com.crediteuropebank.vacationsmanager.client.callback;

import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomValidationException;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is a custom implementation of AsyncCallback to hold onFailure(...) in one place.
 * For implementation of the onSuccess case use TemplateMethod.
 * @author dimas
 *
 * @param <T> - type of returned result
 */
public abstract class CustomAsyncCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof CustomValidationException) {
			CustomValidationException ex = (CustomValidationException)caught;
			
			Dialog.showMessageWithListOfItems("Wrong values have been entered:", 
					ex.getErrorMessages(), 
					Dialog.TITLE_WARNING);
		} else if(caught instanceof CustomMessageException){
			Dialog.showSimpleMessage(caught.getMessage(), Dialog.TITLE_WARNING);
		} else {
			// add logging here if necessary
			Dialog.showSimpleMessage("Error occured: " + caught.getMessage(), 
					Dialog.TITLE_ERROR);
		}
	}

	@Override
	public void onSuccess(T result) {
		onSuccessExecution(result);
	}

	/**
	 * Called when an asynchronous call completes successfully.
	 * @param result - result which has been returned from Async. call 
	 */
	public abstract void onSuccessExecution(T result);
}
