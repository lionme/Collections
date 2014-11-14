package com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * This field represents combination of {@link Label} + {@link TextField} (which type is password), 
 * placed in one {@link HorizontalPanel}.
 * 
 * @author DIMAS
 *
 */
public class PasswordTextField extends BaseField<String> {

	/**
	 * Custom input widget.
	 */
	private final TextBox textBox = new TextBox();
	
	{
		textBox.getElement().setAttribute("type", "password");
	}
	
	/**
	 * Returns the text, entered in this field.
	 * 
	 * @return entered text.
	 */
	public String getFieldValue() {
		String value = textBox.getValue();
			
		return value;
	}

	/**
	 * Sets the text that will be displayed in the field.
	 * 
	 * @param value
	 */
	public void setFieldValue(String value){
		textBox.setValue(value);
	}

	/**
	 * 
	 * @param labelText - the text of the label.
	 */
	public PasswordTextField(String labelText) {
		super(labelText);
	}
	
	public PasswordTextField() {
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
