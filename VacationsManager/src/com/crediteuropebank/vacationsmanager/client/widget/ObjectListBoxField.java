package com.crediteuropebank.vacationsmanager.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class is the same as {@link ListBoxField}. Except that it holds not only String key 
 * and value pairs for ListBox, but also an instance of the object related to this value.
 * 
 * @author dimas
 *
 */
public class ObjectListBoxField<T> extends BaseField<String>{
	/**
	 * Holds the key/value pairs of the elements in the listbox.
	 */
	Map<String, T> objectsMap = new HashMap<String, T>();
		
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
	 * This method returns object that corresponds the selected record.
	 * 
	 * @return the object that corresponds selected record; if no record selected - returns null.
	 */
	public T getSelectedObject() {
		int index = listBox.getSelectedIndex();
		
		if (index == -1) {
			return null;
		} else {
			String key = listBox.getValue(index);

			return objectsMap.get(key);
		}
		
	}
	
	/**
	 * This method sets selected in list box item, related to object with specified id.
	 * @param objectId - id of the object to be selected.
	 */
	public void setSelectedObject(long objectId) {
		int itemsCount = listBox.getItemCount();
		for (int i=0; i< itemsCount; i++) {
			if (String.valueOf(objectId).equals(listBox.getValue(i))) {
				listBox.setSelectedIndex(i);
				break;
			}
		}
	}

	public ObjectListBoxField() {
		super();
	}

	/**
	 * 
	 * @param labelText - the text of the label
	 */
	public ObjectListBoxField(String labelText) {
		super(labelText);
	}
	
	/**
	 * This method adds key/value pair to the ListBoxField. Also it saves object  with specified key in the entire map. If map have already contained value with specified 
	 * key then new item will not be added to the list box.
	 * 
	 * @param key - objects id.
	 * @param value - the value that will be displayed in the ListBox.
	 * @param object - object that corresponds the given key.
	 */
	public void addItem(long key, String value, T object) {
		String stringKey = String.valueOf(key);
		Object previousValue = objectsMap.put(stringKey, object);
		
		// Put value to the list box only if objectsMap haven't contained value with the same key before. 
		if (previousValue == null) {
			listBox.addItem(value, stringKey);
		}
	}
	
	 /**
	  * This method sets selected value in list box with index, specified by value.
	  * 
	  * @param index - index of the list box item, which need to be selected.
	  */
	public void setSelectedIndex(int index) {
		listBox.setSelectedIndex(index);
	}
	
	/**
	 * Adds a {@link ChangeEvent} handler.
	 * 
	 * @param handler - the change handler.
	 */
	public void addChangeHandler(ChangeHandler handler) {
		listBox.addChangeHandler(handler);
	}

	@Override
	public void setEnabled(boolean value) {
		listBox.setEnabled(value);
	}
	
	/**
	 * This method removes all items from the list box and inner object map.
	 */
	public void removeAllItems() {
		listBox.clear();
		objectsMap.clear();
	}
	
	/**
	 * Sets the number of items that are visible. If only one item is visible, then the box will be displayed as a drop-down list. 
	 * 
	 * @param visibleItems - the visible item count
	 */
	public void setVisibleItemCount(int visibleItems) {
		listBox.setVisibleItemCount(visibleItems);
	}
	
	/**
	 * This function returns the list of objects, that was added to this list box. The order of object copy the order that is displayed.
	 * @return the list of object that this list box contains.
	 */
	public List<T> getValuesList() {
		int itemsCount = listBox.getItemCount();
		
		List<T> valuesList = new ArrayList<T>();
		
		for(int i=0; i< itemsCount; i++) {
			valuesList.add(objectsMap.get(listBox.getValue(i)));
		}
		
		return valuesList;
	}
	
	/**
	 * This method returns the number of items that have been added to list box.
	 * @return - the number of items that have been added.
	 */
	public int getNumberOfItems() {
		return objectsMap.size();
	}
	
	/**
	 * This function removes selected item from the items list. If no item is selected - then do nothing and return false.
	 * 
	 * @return true - if item was successfully removed; false - in other case.
	 */
	public boolean removeSelectedItem() {
		int selectedIndex = listBox.getSelectedIndex();
		
		if (selectedIndex != -1) {
			String key = listBox.getValue(selectedIndex);
			
			listBox.removeItem(selectedIndex);
			objectsMap.remove(key);
			
			return true;
		} else {
			return false;
		}
		
	}

	@Override
	Widget getCustomWidget() {
		return listBox;
	}
	
}
