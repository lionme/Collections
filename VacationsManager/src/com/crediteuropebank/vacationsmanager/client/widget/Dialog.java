package com.crediteuropebank.vacationsmanager.client.widget;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This class contains a set of static methods that create different kinds of 
 * dialogs(SimpleMessage, WarningMessage, SimpleDialog...)
 * 
 * @author dimas
 * 
 */
public class Dialog {
	/* This constants represents the size of the all dialog and message windows. */
	private static final int WIDTH = 300;
	private static final int HEIGHT = 40; // height of the panel without buttons in the bottom 
	
	/* This constants represents buttons labels. */
	private static final String BUTTON_OK_TEXT = "OK";
	private static final String BUTTON_YES_TEXT = "YES";
	private static final String BUTTON_NO_TEXT = "NO";
	private static final String BUTTON_CANCEL_TEXT = "Cancel";
	
	public static final String TITLE_ERROR = "Error";
	public static final String TITLE_WARNING = "Warning";
	public static final String TITLE_INFORMATION = "Information";

	public static final String MESSAGE_SAVING_WAS_EXECUTED_SUCCESSFULLY = "Record have been successfully saved.";
	public static final String MESSAGE_UPDATING_WAS_EXECUTED_SUCCESSFULLY = "Record have been successfully updated.";
	public static final String MESSAGE_DELETING_WAS_EXECUTED_SUCCESSFULLY = "Record have been successfully deleted.";
	
	private static final String STANDARD_ERROR_MESSAGE_TEXT = "Sorry, but error on server occurs. Please, contact with your administrator.";
	
	/**
	 * Static method that creates standard error message with standard title "Error" and 
	 * standard message text.
	 */
	public static void showStandartErrorMessage() {
		final ExtendedDialogBox dialog = new ExtendedDialogBox();
		dialog.setWidth(WIDTH);
		
		dialog.setTitleText(TITLE_ERROR);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHeight(HEIGHT + "px");
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		Label errorLabel = new Label(STANDARD_ERROR_MESSAGE_TEXT);
		errorLabel.setStylePrimaryName("errorText");
		verticalPanel.add(errorLabel);
		dialog.add(verticalPanel);
		dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Button button = new Button(BUTTON_OK_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
			}
		});
		dialog.addButton(button);
		dialog.center();
	}	
	
	/**
	 * Static method that creates and shows simple message window with specified title
	 * and message.
	 * 
	 * @param messageText - the text of message that will be placed inside the window.
	 * @param titleText - the text of the window's title.
	 */
	public static void showSimpleMessage(String messageText, String titleText) {
		final ExtendedDialogBox dialog = new ExtendedDialogBox();
		dialog.setWidth(WIDTH);
		
		if (titleText!=null)
			dialog.setTitleText(titleText);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHeight(HEIGHT + "px");
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel.add(new Label(messageText));
		dialog.add(verticalPanel);
		dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Button button = new Button(BUTTON_OK_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
			}
		});
		dialog.addButton(button);
		dialog.center();
	}
	
	/**
	 * Static method that creates and shows simple message window with specified title and
	 * message which returns focus to the specified widget after closing.
	 * 
	 * @param messageText - the text of message that will be placed inside the window.
	 * @param titleText - the text of the window's title.
	 * @param widget - the widget to which focus should be returned.
	 */
	public static void showReturnedFocusSimpleMessage(String messageText, String titleText, FocusWidget widget) {
		final ExtendedDialogBox dialog = new ExtendedDialogBox();
		dialog.setWidth(WIDTH);
		
		if (titleText!=null)
			dialog.setTitleText(titleText);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHeight(HEIGHT + "px");
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel.add(new Label(messageText));
		dialog.add(verticalPanel);
		dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Button button = new Button(BUTTON_OK_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide(true);
			}
		});
		dialog.addButton(button);
		dialog.setWhomReturnFocus(widget);
		dialog.center();
	}
	
	/**
	 * <p>Static method, that creates and shows dialog window with "YES" and "NO" buttons. Title
	 * and dialog text should be specified as input parameters.</p>
	 * 
	 * <p>If you need to separate dialog text on few rows use '\n' symbol.</p>
	 * 
	 * @param dialogText - the text of message that will be placed inside the dialog window.
	 * @param titleText - the text of the window's title.
	 * @param handler - the custom implementation of the {@link YesNoDialogHandler} interface which gives
	 * 					you a possibility to handle user's answer.
	 */
	public static void showYesNoDialog(String dialogText, String titleText, final YesNoDialogHandler handler) {
		final ExtendedDialogBox dialog = new ExtendedDialogBox();
		dialog.setWidth(WIDTH);
		
		if (titleText!=null)
			dialog.setTitleText(titleText);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHeight(HEIGHT + "px");
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		String[] rows = dialogText.split("\n");
		
		for (int i=0; i< rows.length; i++) {
			verticalPanel.add(new Label(rows[i]));
		}
		
		dialog.add(verticalPanel);
		dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Button buttonYES = new Button(BUTTON_YES_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				handler.onAnswerChoosed(true);
				dialog.hide();
			}
		});
		Button buttonNO = new Button(BUTTON_NO_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				handler.onAnswerChoosed(false);
				dialog.hide();
			}
		});
		dialog.addButton(buttonYES);
		dialog.addButton(buttonNO);
		dialog.center();		
	}
	
	/**
	 * Shows the dialog window with specified title and dialog text that gives a possibility for
	 * user to enter some comments.
	 * 
	 * @param dialogText - the text that is shown before text area.
	 * @param titleText - the text of the title.
	 * @param handler - the handler that handles the user's response.
	 */
	public static void showCommentsEntryDialog(String dialogText, String titleText, 
			final CommentedDialogHandler handler) {
		
		final ExtendedDialogBox dialog = new ExtendedDialogBox();
		dialog.setWidth(WIDTH);
		
		if (titleText!=null)
			dialog.setTitleText(titleText);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHeight(HEIGHT + "px");
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		String[] rows = dialogText.split("\n");
		
		for (int i=0; i< rows.length; i++) {
			verticalPanel.add(new Label(rows[i]));
		}
		
		final TextArea textArea = new TextArea();
		textArea.setWidth(String.valueOf(WIDTH - 40) + "px");
		textArea.setHeight("80px");
		textArea.setDirectionEstimator(false);
		textArea.getElement().getStyle().setProperty("resize", "none");
		
		verticalPanel.add(textArea);
		verticalPanel.setCellWidth(textArea, "100%");
		verticalPanel.setCellHorizontalAlignment(textArea, HasHorizontalAlignment.ALIGN_CENTER);
		
		dialog.add(verticalPanel);
		dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Button buttonOK = new Button(BUTTON_OK_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				handler.onOkChoosed(textArea.getText());
				dialog.hide();
			}
		});
		Button buttonCancel = new Button(BUTTON_CANCEL_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				handler.onCancelChoosed();
				dialog.hide();
			}
		});
		dialog.addButton(buttonOK);
		dialog.addButton(buttonCancel);
		dialog.center();	
	}
	
	/**
	 * 
	 * This static method shows message window with numbered list of items.
	 * 
	 * @param introductoryText - the text that will be shown in the bottom of the message window before list of items.
	 * @param textItemsList - the list of String items that will be shown in the message window.
	 * @param titleText - the text of the window's title.
	 */
	public static void showMessageWithListOfItems(String introductoryText, List<String> textItemsList, String titleText) {
		final ExtendedDialogBox dialog = new ExtendedDialogBox();
		dialog.setWidth(WIDTH);
		
		if (titleText!=null)
			dialog.setTitleText(titleText);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHeight(HEIGHT + "px");
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel.add(new Label(introductoryText));
		
		// Add each text item to the panel
		int count = 0;
		for (String textItem: textItemsList) {
			count ++;
			Label spacingLabel = new Label("");
			spacingLabel.setWidth("10px");
			Label textLabel = new Label(count + ") " + textItem + ";");
			HorizontalPanel string = new HorizontalPanel();
			string.add(spacingLabel);
			string.add(textLabel);
			verticalPanel.add(string);
		}
		
		dialog.add(verticalPanel);
		dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Button button = new Button(BUTTON_OK_TEXT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
			}
		});
		dialog.addButton(button);
		dialog.center();
	}
}
