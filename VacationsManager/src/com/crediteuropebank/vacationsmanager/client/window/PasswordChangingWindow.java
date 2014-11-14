package com.crediteuropebank.vacationsmanager.client.window;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.ExtendedDialogBox;
import com.crediteuropebank.vacationsmanager.client.widget.PasswordTextField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * 
 * This class is used for creating a window for password changing.
 * 
 * @author dimas
 *
 */
public class PasswordChangingWindow {
	
	private static final int TEXT_FIELD_WIDTH = 300;
	private static final int LABEL_WIDTH = 150;
	
	private static final PasswordTextField oldPassword = new PasswordTextField("Old password");
	private static final PasswordTextField newPassword = new PasswordTextField("New password");
	private static final PasswordTextField retypedNewPussword = new PasswordTextField("Retyped new password");
	
	/**
	 * Holds single instance of this class
	 */
	private static final PasswordChangingWindow instance = new PasswordChangingWindow();
	
	/**
	 * Holds the window instance.
	 */
	private ExtendedDialogBox window = new ExtendedDialogBox();
	
	private PasswordChangingWindow() {
		window.setTitleText("Change old password");
		
		window.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		oldPassword.setWidth(TEXT_FIELD_WIDTH);
		oldPassword.setLabelWidth(LABEL_WIDTH);
		
		newPassword.setWidth(TEXT_FIELD_WIDTH);
		newPassword.setLabelWidth(LABEL_WIDTH);
		
		retypedNewPussword.setWidth(TEXT_FIELD_WIDTH);
		retypedNewPussword.setLabelWidth(LABEL_WIDTH);
		
		window.add(oldPassword);
		window.add(newPassword);
		window.add(retypedNewPussword);
		
		Button saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String oldPasswordString = oldPassword.getFieldValue();
				String newPasswordString = newPassword.getFieldValue();
				
				VacationsManager.getUsersService().updateUserPassword(oldPasswordString, 
						newPasswordString, 
						new CustomAsyncCallback<Void>() {

							@Override
							public void onSuccessExecution(Void result) {
								Dialog.showSimpleMessage("Your password was updated successfully.", 
										Dialog.TITLE_INFORMATION);
								hide();
							}
						});
			}
		});
		
		window.addButton(saveButton);
		
		Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				window.hide();
			}
		});
		
		window.addButton(closeButton);
		window.setButtonsAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		window.setButtonsSpacing(10);
		window.setButtonsWidth(80);
	}
	
	/**
	 * @return single {@link PasswordChangingWindow} instance.
	 */
	public static PasswordChangingWindow getInstance() {
		return instance;
	}
	
	/**
	 * This method clears all data in the window.
	 */
	private void clearWindow() {
		oldPassword.setFieldValue("");
		newPassword.setFieldValue("");
		retypedNewPussword.setFieldValue("");
	}
	
	/**
	 * This method shows window in the center of the screen. 
	 */
	public void show() {
		window.center();
	}
	
	/**
	 * This method clears window content and hides it if it is shown.
	 */
	public void hide() {
		clearWindow();
		window.hide();
	}
}
