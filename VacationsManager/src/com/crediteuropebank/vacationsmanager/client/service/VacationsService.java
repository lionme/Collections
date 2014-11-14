package com.crediteuropebank.vacationsmanager.client.service;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.dto.NonWorkingDaysDTO;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author dimas
 *
 */
@RemoteServiceRelativePath("rpc/vacations")
public interface VacationsService extends RemoteService {

	/**
	 * This method fetches all vacations records from DB.
	 * 
	 * @return the list of all {@link Vacation} entities from DB
	 */
	List<Vacation> getAllVacations();
	
	/**
	 * This method saves new vacation record to DB.
	 * 
	 * @param vacation - {@link Vacation} entity that hold all info about new vacation
	 * @throws CustomMessageException if some problems occur and we need 
	 * 					to inform user about them
	 */
	void saveVacation(Vacation vacation) throws CustomMessageException;
	
	/**
	 * This method updates existed vacations record after its rejection. Note that this 
	 * method will update record only with REJECTED state.
	 * 
	 * @param vacation - {@link Vacation} entity that hold all info about vacation
	 * 						that should be updated
	 * @throws CustomMessageException if some problems occur and we need 
	 * 					to inform user about them
	 */
	void updateVacationAfterRejection(Vacation vacation) throws CustomMessageException;
	
	/**
	 * This method deletes information about existed vacation record from DB.
	 * 
	 * @param vacation - {@link Vacation} entity that hold all info about vacation
	 * 						that should be deleted
	 * @throws CustomMessageException if some problems occur and we need 
	 * 					to inform user about them
	 */
	void deleteVacation(Vacation vacation) throws CustomMessageException;
	
	/**
	 * This method returns the list of actual vacations (by actual means vacations that has 
	 * end date >= current date and status is not REJECTED). 
	 * 
	 * @return the list of actual vacations.
	 */
	List<Vacation> getActualVacations();
	
	/**
	 * This method is used for calculating number of holiday days + weekends days that are 
	 * included into vacation's duration.
	 * 
	 * @param vacation - vacation for which number of non working days should be calculated.
	 * @return the list of non working days (domain object - {@link HolidayDays}) for specified period.
	 * @throws CustomMessageException 
	 */
	NonWorkingDaysDTO getNonWorkingDaysForVacation(Vacation vacation) throws CustomMessageException;
	
	/**
	 * This method returns the list of rejected vacations (with VacationStatus.REJECTED).
	 * 
	 * @return rejected vacations list.
	 */
	List<Vacation> getRejectedVacations();
	
	/**
	 * This method fetches all vacations records from DB.
	 * 
	 * @param startDate - start of the date range
	 * @param endDate - end of the date range
	 * @return the list of all {@link Vacation} entities from DB for specified period
	 * @throws CustomMessageException if date range validation fails and we need to inform user about the reason
	 */
	List<Vacation> getAllVacationsForDateRange(final Date startDate, final Date endDate) throws CustomMessageException;
	
	/**
	 * This method fetches all actual vacations (for which end date >= current date) for specified period.
	 * 
	 * @param startDate - start of the date range
	 * @param endDate - end of the date range
	 * @return the list of active {@link Vacation} entities from DB for specified period
	 * @throws CustomMessageException if date range validation fails and we need to inform user about the reason
	 */
	List<Vacation> getActualVacationsForDateRange(final Date startDate, final Date endDate) throws CustomMessageException;
	
	/**
	 * This method fetches all rejected vacations for specified period.
	 * 
	 * @param startDate - start of the date range
	 * @param endDate - end of the date range
	 * @return the list of rejected vacations for specified period
	 * @throws CustomMessageException if date range validation fails and we need to inform user about the reason
	 */
	List<Vacation> getRejectedVacationsForDateRange(final Date startDate, final Date endDate) throws CustomMessageException;
}
