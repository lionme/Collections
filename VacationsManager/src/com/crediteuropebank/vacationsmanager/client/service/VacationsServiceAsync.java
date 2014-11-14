package com.crediteuropebank.vacationsmanager.client.service;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.dto.NonWorkingDaysDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author dimas
 *
 */
public interface VacationsServiceAsync {

	/**
	 * This method fetches all vacations records from DB.
	 * 
	 * @param callback
	 */
	void getAllVacations(AsyncCallback<List<Vacation>> callback);

	/**
	 * This method saves new vacation record to DB.
	 * 
	 * @param vacation - {@link Vacation} entity that hold all info about new vacation
	 * @param callback
	 */
	void deleteVacation(Vacation vacation, AsyncCallback<Void> callback);

	/**
	 * This method updates existed vacations record after its rejection. Note that this 
	 * method will update record only with REJECTED state.
	 * 
	 * @param vacation - {@link Vacation} entity that hold all info about vacation
	 * 						that should be updated
	 * @param callback
	 */
	void saveVacation(Vacation vacation, AsyncCallback<Void> callback);

	/**
	 * This method deletes information about existed vacation record from DB.
	 * 
	 * @param vacation - {@link Vacation} entity that hold all info about vacation
	 * 						that should be deleted
	 * @param callback
	 */
	void updateVacationAfterRejection(Vacation vacation, AsyncCallback<Void> callback);

	/**
	 * This method returns the list of actual vacations (by actual means vacations that has 
	 * end date >= current date and status is not REJECTED).
	 * 
	 * @param callback
	 */
	void getActualVacations(AsyncCallback<List<Vacation>> callback);

	/**
	 * This method is used for calculating number of holiday days + weekends days that are 
	 * included into vacation's duration.
	 * 
	 * @param vacation - vacation for which number of non working days should be calculated.
	 * @param callback
	 */
	void getNonWorkingDaysForVacation(Vacation vacation,
			AsyncCallback<NonWorkingDaysDTO> callback);

	/**
	 * This method returns the list of rejected vacations (with VacationStatus.REJECTED).
	 * 
	 * @param callback
	 */
	void getRejectedVacations(AsyncCallback<List<Vacation>> callback);

	/**
	 * This method fetches all vacations records from DB.
	 * 
	 * @param startDate - start of the date range.
	 * @param endDate - end of the date range.
	 * @param callback
	 */
	void getAllVacationsForDateRange(Date startDate, Date endDate,
			AsyncCallback<List<Vacation>> callback);

	/**
	 * This method fetches all actual vacations (for which end date >= current date) for specified period.
	 * 
	 * @param startDate - start of the date range
	 * @param endDate - end of the date range
	 * @param callback
	 */
	void getActualVacationsForDateRange(Date startDate, Date endDate,
			AsyncCallback<List<Vacation>> callback);

	/**
	 * This method fetches all rejected vacations for specified period.
	 * 
	 * @param startDate - start of the date range
	 * @param endDate - end of the date range
	 * @param callback
	 */
	void getRejectedVacationsForDateRange(Date startDate, Date endDate,
			AsyncCallback<List<Vacation>> callback);

}
