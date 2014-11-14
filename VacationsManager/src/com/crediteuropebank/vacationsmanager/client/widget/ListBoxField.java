package com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class represent the custom ListBoxField widget. 
 * It consists from {@link Label} and {@link ListBox}.
 * 
 * @author dimas
 *
 */
public class ListBoxField extends BaseField<String>{

	/**
	 * Custom input widget.
	 */
	private final ListBox listBox = new ListBox();
	
	{
		listBox.setVisibleItemCount(1);
		listBox.setSelectedIndex(-1);
		listBox.setStylePrimaryName("listBoxStyle");
	}
	
	/**
	 *	This fields returns selected value.
	 *
	 * @return selected value; if nothing selected - returns null.
	 */
	public String getSelectedValue() {
		int index = listBox.getSelectedIndex();
		
		return (index!=-1)?listBox.getValue(index):null;
	}
	

	/**
	 * This method sets selected the specified value in the combobox.
	 * @param value - value of the combobox element, which need to be selected.
	 */
	public void setSelectedValue(String value) {
		
		int itemsCount = listBox.getItemCount();
		for (int i=0; i< itemsCount; i++) {
			if (value.equals(listBox.getValue(i))) {
				listBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public ListBoxField() {
		super();
	}

	/**
	 * 
	 * @param labelText - the text of the label.
	 */
	public ListBoxField(String labelText) {
		super(labelText);
	}
	
	/**
	 * This method adds key/value pair to the ListBoxField
	 * @param key
	 * @param value
	 */
	public void addItem(String key, String value) {
		listBox.addItem(value, key);
	}
	
	 /** 
	  * This method sets selected value in list box with index, specified by value.
	 *   @param index - index of the list box item, which need to be selected.
	 */
	public void setSelectedIndex(int index) {
		listBox.setSelectedIndex(index);
	}
	
	/**
	 * This method adds Change handler to list box field. 
	 * @param handler - the handler to be attached
	 */
	public void addChangeHandler(ChangeHandler handler) {
		listBox.addChangeHandler(handler);
	}

	@Override
	public void setEnabled(boolean value) {
		listBox.setEnabled(value);
	}
	
	/**
	 * Removes all items from the ListBoxField.
	 */
	public void removeAllItems() {
		listBox.clear();
	}


	@Override
	Widget getCustomWidget() {
		return listBox;
	}

		
}
