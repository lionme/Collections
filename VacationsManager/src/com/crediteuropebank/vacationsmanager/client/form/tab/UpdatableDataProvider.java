package com.crediteuropebank.vacationsmanager.client.form.tab;

import com.google.gwt.view.client.HasData;

import ca.nanometrics.gflot.client.PlotWithOverviewModel.AsyncDataProvider;

/**
 * 
 * This interface should be implemented by all custom implementations of {@link AsyncDataProvider} to give a possibility to
 *  update them programmatically.
 * 
 * @author DIMAS
 * @param <T>
 *
 */
public interface UpdatableDataProvider<T> {

	/**
	 * This method force the data provider to update his data from DB.
	 */
	void updateView(HasData<T> data);
	
}
