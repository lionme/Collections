package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author dimas
 *
 */
@RemoteServiceRelativePath("rpc/holidayDays")
public interface HolidayDaysService extends RemoteService {
	
	/**
	 * This method saves new holidays record to DB.
	 * 
	 * @param holidayDays - holiday days entity that should be saved.
	 * @throws CustomMessageException if some error occurs during saving.
	 */
	void saveHolidayDays(HolidayDays holidayDays) throws CustomMessageException;
	
	/**
	 * This method updates existed holidays record in DB.
	 * 
	 * @param holidayDays - holiday days entity that should be updated.
	 * @throws CustomMessageException if some error occurs during updating.
	 */
	void updateHolidayDays(HolidayDays holidayDays) throws CustomMessageException;
	
	/**
	 * This method deletes existed record from DB.
	 * 
	 * @param holidayDays - holiday days entity that should be deleted. 
	 * @throws CustomMessageException if some error occurs during deleting
	 */
	void deleteHolidayDays(HolidayDays holidayDays) throws CustomMessageException;
	
	/**
	 * This method gets record by its id.
	 * 
	 * @param id - id of the record.
	 * @return fetched {@link HolidayDays}} entity.
	 */
	HolidayDays getHolidayDaysById(long id);
	
	/**
	 * This method gets all holiday days records from DB.
	 * 
	 * @return the list of all {@link HolidayDays}} entities fetched from DB.
	 */
	List<HolidayDays> getAllHolidayDays();
}
