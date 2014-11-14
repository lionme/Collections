package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author dimas
 *
 */
public interface HolidayDaysServiceAsync {

	/**
	 * This method saves new holidays record to DB.
	 * 
	 * @param holidayDays - holiday days entity that should be saved.
	 * @param callback
	 */
	void saveHolidayDays(HolidayDays holidayDays, AsyncCallback<Void> callback);

	/**
	 * This method updates existed holidays record in DB.
	 * 
	 * @param holidayDays - holiday days entity that should be updated.
	 * @param callback
	 */
	void updateHolidayDays(HolidayDays holidayDays, AsyncCallback<Void> callback);

	/**
	 * This method deletes existed record from DB.
	 * 
	 * @param holidayDays - holiday days entity that should be deleted. 
	 * @param callback
	 */
	void deleteHolidayDays(HolidayDays holidayDays, AsyncCallback<Void> callback);

	/**
	 * This method gets record by its id.
	 * 
	 * @param id - id of the record.
	 * @param callback
	 */
	void getHolidayDaysById(long id, AsyncCallback<HolidayDays> callback);

	/**
	 * This method gets all holiday days records from DB.
	 * 
	 * @param callback
	 */
	void getAllHolidayDays(AsyncCallback<List<HolidayDays>> callback);

}
