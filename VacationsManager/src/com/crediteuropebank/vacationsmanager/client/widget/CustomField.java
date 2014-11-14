package com.crediteuropebank.vacationsmanager.client.widget;

/**
 * 
 * Interface that marks custom fields.
 * 
 * @author dimas
 *
 */
public interface CustomField {
	
	/**
	 * Sets the width of the field.
	 * 
	 * @param width
	 */
	void setWidth(int width);
	
	/**
	 * Sets the label width.
	 * 
	 * @param labelWidth
	 */
	void setLabelWidth(int labelWidth);
	
	/**
	 * Sets the spacing.
	 * 
	 * @param spacing
	 */
	void setSpacing(int spacing);
	
	/**
	 * Sets the label's text.
	 *
	 * @param labelText
	 */
	void setLabelText(String labelText);
}
