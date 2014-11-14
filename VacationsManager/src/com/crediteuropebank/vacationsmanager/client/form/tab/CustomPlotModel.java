package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.List;

import ca.nanometrics.gflot.client.PlotModel;
import ca.nanometrics.gflot.client.SeriesHandler;

/**
 * This custom plot model class was created for giving a possibility to delete all 
 * old SeriesHandlers.
 * 
 * @author dimas
 *
 */
public class CustomPlotModel extends PlotModel {

	/**
	 * This method removes all series data from current PlotModel.
	 */
	public void removeAllSeries(){
		List<SeriesHandler> handlers = getHandlers();
		
		while (handlers.size()!=0) {
			removeSeries(handlers.get(0));
		}
	}
}
