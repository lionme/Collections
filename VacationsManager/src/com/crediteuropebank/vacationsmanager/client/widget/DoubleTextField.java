package com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * This widget represents combination of the {@link Label} + {@link TextBox} in which you
 * can enter only double numbers.
 * 
 * @author dimas
 *
 */
public final class DoubleTextField extends BaseField<Double> {
	
	/**
	 * Custom input widget.
	 */
	private DoubleTextBox doubleTextBox = new DoubleTextBox();
	
	{
		doubleTextBox.setValue("0");
	}

	/**
	 * This method returns double value, entered in this field.
	 * 
	 * @return double value from field.
	 * @throws NumberFormatException if the string does not contain a parsable integer.
	 */
	public Double getFieldValue() {
		String value = doubleTextBox.getValue();

		double returnedValue = Double.parseDouble(value);
		
		return returnedValue;
	}

	/**
	 * Sets the double value that will be displayed in this field.
	 * 
	 * @param double value
	 */
	public void setFieldValue(Double value) {
		
		doubleTextBox.setValue(String.valueOf(value));
	}

	public DoubleTextField(String labelText) {
		super(labelText);
	}

	public DoubleTextField() {
		super();
	}

	@Override
	public void setEnabled(boolean value) {
		doubleTextBox.setEnabled(value);
	}

	@Override
	Widget getCustomWidget() {
		return doubleTextBox;
	}

	
}
