package com.crediteuropebank.vacationsmanager.client.observer;

import java.util.List;

/**
 * This interface should mark "Observer" participant of the Observer pattern. In our application
 * this will be parts of the GUI that should be updated.
 * 
 * This class is typed and it requires a list of the the data to be displayed.
 * 
 * @author dimas
 *
 * @param <T> - the type of the object that contains list of new objects.
 */
public interface TypedObserver<T> {

	/**
	 * Executes necessary update actions.
	 * 
	 * @param objects - the new list of objects that should be displayed
	 */
	void update(List<T> objects);
	
}
