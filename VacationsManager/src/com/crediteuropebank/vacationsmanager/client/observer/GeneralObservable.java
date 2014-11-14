package com.crediteuropebank.vacationsmanager.client.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This observable class is general for application and is used for 
 * updating elements from different forms.
 * 
 * In future if it will be necessary to use observable for different subjects 
 * -> change list on map where keys will be constants of enum ObserveSubjects (for example).
 * 
 * @author dimas
 *
 */
public class GeneralObservable {
	
	/**
	 * The list of observers.
	 */
	private final List<GeneralObserver> observers = new ArrayList<GeneralObserver>();
	
	/**
	 * Add new observer to list of observers. 
	 * 
	 * @param observer 
	 */
	public void attach(GeneralObserver observer) {
		observers.add(observer);
	}
	
	/**
	 * Remove observer from list of observers.
	 * 
	 * @param observer
	 */
	public void detach(GeneralObserver observer) {
		observers.remove(observer);
	}
	
	/**
	 * Notifies observers related to specified subject.
	 * 
	 * @param subject
	 */
	public void notifyObservers() {
		for(GeneralObserver observer: observers) {
			observer.update();
		}
	}
}
