package com.crediteuropebank.vacationsmanager.client.form.tab;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * This interface specifies custom tab that is used as a part of tab panel
 * 
 * @author dimas
 *
 */
public interface CustomTab extends IsWidget {
	
	/*void selected();
	
	void unselected();*/
	
	/**
	 * This method is called when necessary to update content of the tab. So put all logic 
	 * that updates state of the tab here.
	 */
	void updateContent();
}
