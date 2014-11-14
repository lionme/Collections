package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

/**
 * 
 * Class that implements a part of functionality of {@link AbstractDataProvider} wit necessary changes in logic.
 * 
 * @author DIMAS
 *
 * @param <T> the data type of records in the list
 */
public abstract class CustomAbstractDataProvider<T> {
	
	private HasData<T> display;
	
	private HandlerRegistration handler;
	
	/**
	 * Sets new data display. If other display have already been attached, then old display is removed and its handler is removed too.
	 * 
	 * @param display
	 */
	public void addDataDisplay(final HasData<T> display) {
		if (display == null) {
			throw new IllegalArgumentException("Input display cannot be null!");
		} else if (handler !=null) {
			handler.removeHandler();
		}
		
		this.display = display;
		
		handler = display.addRangeChangeHandler(
		        new RangeChangeEvent.Handler() {
		          public void onRangeChange(RangeChangeEvent event) {
		        	  CustomAbstractDataProvider.this.onRangeChanged(display);
		          }
		        });
	}

	  /**
	   * Inform the displays of the total number of items that are available.
	   *
	   * @param count the new total row count
	   * @param exact true if the count is exact, false if it is an estimate
	   */
	  protected void updateRowCount(int count, boolean exact) {

	    if (display != null) {
	      display.setRowCount(count, exact);
	    }
	  }

	  /**
	   * Inform the displays of the new data.
	   *
	   * @param start the start index
	   * @param values the data values
	   */
	  protected void updateRowData(int start, List<T> values) {
	    if (display != null) {
	      updateRowData(display, start, values);
	    }
	  }
	  
	  /**
	   * Informs a single display of new data.
	   *
	   * @param display the display to be updated
	   * @param start the start index
	   * @param values the data values
	   */
	  protected void updateRowData(HasData<T> display, int start, List<T> values) {
	    int end = start + values.size();
	    Range range = display.getVisibleRange();
	    int curStart = range.getStart();
	    int curLength = range.getLength();
	    int curEnd = curStart + curLength;
	    if (start == curStart || (curStart < end && curEnd > start)) {
	      // Fire the handler with the data that is in the range.
	      // Allow an empty list that starts on the page start.
	      int realStart = curStart < start ? start : curStart;
	      int realEnd = curEnd > end ? end : curEnd;
	      int realLength = realEnd - realStart;
	      List<T> realValues = values.subList(
	          realStart - start, realStart - start + realLength);
	      display.setRowData(realStart, realValues);
	    }
	  }
	
	  /**
	   * Called when a display changes its range of interest.
	   *
	   * @param display the display whose range has changed
	   */
	  protected abstract void onRangeChanged(HasData<T> display);
	  
}
