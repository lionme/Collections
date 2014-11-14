package com.crediteuropebank.vacationsmanager.client.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class plays "Observable" role in observer pattern. It holds the list 
 * of the observers that should be notified when some operation executes.
 * 
 * This class is typed and during notification of the observers it sends the list 
 * with new data to be displayed.
 * 
 * @author dimas
 *
 *	<T> - the type of data that observer need for updating.
 *	
 */
public class TypedObservable<T> {
	
	/**
	 * The list of observers.
	 */
	private final List<TypedObserver<T>> observers = new ArrayList<TypedObserver<T>>();
	
	/**
	 * Add new observer to list of observers. 
	 * 
	 * @param observer 
	 */
	public void attach(TypedObserver<T> observer) {
		observers.add(observer);
	}
	
	/**
	 * Remove observer from list of observers.
	 * 
	 * @param observer
	 */
	public void detach(TypedObserver<T> observer) {
		observers.remove(observer);
	}
	
	/**
	 * Notifies observers related to specified subject.
	 * 
	 * @param subject
	 */
	public void notifyObservers(List<T> newData) {
		for(TypedObserver<T> observer: observers) {
			observer.update(newData);
		}
	}
	
}
