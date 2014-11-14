package com.crediteuropebank.vacationsmanager.client.widget;

/**
 * This handler class gives a possibility to handle answer in dialogs where user can 
 * add his comments.
 * 
 * @author DIMAS
 *
 */
public interface CommentedDialogHandler {
	
	/**
	 * This method is called after user have pressed OK button.
	 * 
	 * @param comments - comments that user left.
	 */
	void onOkChoosed(String comments);
	
	/**
	 * This method is called after user have pressed Cancel button.
	 */
	void onCancelChoosed();
}
