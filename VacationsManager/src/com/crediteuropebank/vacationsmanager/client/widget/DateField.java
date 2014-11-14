package com.crediteuropebank.vacationsmanager.client.widget;

import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

/**
 * 
 * This field represents combination of {@link Label} + {@link DateBox}, 
 * placed in one {@link HorizontalPanel}
 * 
 * @author DIMAS
 *
 */
public class DateField extends BaseField<Date>{
	
	/**
	 * Custom input widget.
	 */
	private DateBox dateBox = new DateBox();
	
	public DateField() {
		super();
	}

	public DateField(String labelText) {
		super(labelText);
	}

	/**
	 * This method returns Date value that was entered into this field.
	 * 
	 * @return the date that was entered into field, null if nothing was entered.
	 */
	public Date getFieldValue() {
		Date date = dateBox.getValue();
		
		return date;
	}

	/**
	 * Sets the value of the field.
	 * 
	 * @param date - Date to be set as value of the field.
	 */
	public void setFieldValue(Date date) {
		
		dateBox.setValue(date);
	}
 
	@Override
	public void setEnabled(boolean value) {
		dateBox.setEnabled(value);
	}
	
	/**
	 * This function sets format in which Date will be displayed in this field.
	 * @param format - format object
	 */
	public void setDateFormat(Format format) {
		dateBox.setFormat(format);
	}

	@Override
	Widget getCustomWidget() {
		return dateBox;
	}

}
