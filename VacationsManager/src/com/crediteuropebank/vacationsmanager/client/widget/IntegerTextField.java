package com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.user.client.ui.Widget;

/**
 * This custom text field represents combination of the {@link Label} and {@link TextBox}}
 * that can contain only Integer values.
 * 
 * @author dimas
 *
 */
public final class IntegerTextField extends  BaseField<Integer> {
	
	/**
	 * Custom input widget.
	 */
	private IntegerTextBox digitTextBox = new IntegerTextBox();
	
	{
		digitTextBox.setValue("0");
	}

	/**
	 * Default constructor.
	 */
	public IntegerTextField() {
		super();
	}

	/**
	 * Creates new IntegerTextField with specified label's text.
	 * @param labelText
	 */
	public IntegerTextField(String labelText) {
		super(labelText);
	}

	/**
	 * This method returns the filed's value.
	 * 
	 * @return integer value entered in the combobox.
	 * @throws NumberFormatException if the string does not contain a parsable integer.
	 */
	public Integer getFieldValue() {
		String value = digitTextBox.getValue();
		
		return Integer.parseInt(value);
	}

	/**
	 * Sets new integer value to the text field.
	 * 
	 * @param value
	 */
	public  void setFieldValue(Integer value) {
		
		digitTextBox.setValue(String.valueOf(value));
	}

	@Override
	public void setEnabled(boolean value) {
		digitTextBox.setEnabled(value);
	}

	@Override
	Widget getCustomWidget() {
		return digitTextBox;
	}


}
