package com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A text field component. It consists from {@link Label} and {@link TextBox}, 
 * placed on {@link HorizontalPanel}. 
 * 
 * @author Dimas 
 * 
 */

public final class TextField extends BaseField<String> {

	private final TextBox textBox = new TextBox();

	/**
	 * Returns the string that was entered in the field.
	 * 
	 * @return entered string.
	 */
	public String getFieldValue() {
		String value = textBox.getValue();

		return value;
	}
	
	/**
	 * Sets the string that will be displayed in the field.
	 * 
	 * @param value
	 */
	public void setFieldValue(String value) {
		textBox.setValue(value);
	}	

	/**
	 * 
	 * @param labelText - the text of the label.
	 */
	public TextField(String labelText) {
		super(labelText);
	}
	
	public TextField() {
		super();
	}

	@Override
	public void setEnabled(boolean value) {
		textBox.setEnabled(value);
	}

	@Override
	Widget getCustomWidget() {
		return textBox;
	}

	
}
