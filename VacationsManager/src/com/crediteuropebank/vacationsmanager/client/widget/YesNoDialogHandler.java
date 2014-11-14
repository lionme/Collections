package com.crediteuropebank.vacationsmanager.client.widget;

/**
 * This interface uses for handling an answer in YES/NO dialog. You need to implement method 
 * onAnswerChoosed(boolean answer) for handling an answer. 
 * 
 * @author Dimas
 *
 */
public interface YesNoDialogHandler {
	
	/**
	 * You need to implement this method for handling users answer.
	 * 
	 * @param answer - true, if user chose positive answer (YES), and false if user chose negative
	 * 					answer (NO).
	 */
	void onAnswerChoosed(boolean answer);
}
