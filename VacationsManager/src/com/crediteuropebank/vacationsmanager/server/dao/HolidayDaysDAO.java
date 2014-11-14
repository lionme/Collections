package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;

/**
 * 
 * This interface holds methods for making CRUD and search operations with 
 * {@link NonWorkingDays} domain object.
 * 
 * @author DIMAS
 *
 */
public interface HolidayDaysDAO extends DAO<HolidayDays>{
	
	/**
	 * This function is used for fetching a list of non working days for specified period.
	 * 
	 * @param startDate - start date
	 * @param endDate - end date
	 * @return the list of non working days (domain object - {@link HolidayDays}) for specified period
	 */
	List<HolidayDays> getForPeriod(Date startDate, Date endDate);

}
