package com.crediteuropebank.vacationsmanager.shared.dto;

import java.io.Serializable;
import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;

/**
 * 
 * This DTO class is used to transfer data about holiday days and weekend days 
 * between client and server.
 * 
 * @author dimas
 *
 */
public class NonWorkingDaysDTO implements Serializable {
	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * List of holiday days.
	 */
	public List<HolidayDays> holidayDays;
	
	/**
	 * Number of weekend days.
	 */
	public int numberOfWeekendDays;

	/**
	 * Creates new non working days DTO.
	 * 
	 * @param holidayDays - the {@link HolidayDays} domain object which holds information about holiday days
	 * 							for specified period.
	 * @param numberOfWeekendDays - number of weekend days during vacation's duration.
	 */
	public NonWorkingDaysDTO(List<HolidayDays> holidayDays,
			int numberOfWeekendDays) {
		super();
		this.holidayDays = holidayDays;
		this.numberOfWeekendDays = numberOfWeekendDays;
	}
	
	public NonWorkingDaysDTO() {
		super();
	}

	public List<HolidayDays> getHolidayDays() {
		return holidayDays;
	}

	public void setHolidayDays(List<HolidayDays> holidayDays) {
		this.holidayDays = holidayDays;
	}

	public int getNumberOfWeekendDays() {
		return numberOfWeekendDays;
	}

	public void setNumberOfWeekendDays(int numberOfWeekendDays) {
		this.numberOfWeekendDays = numberOfWeekendDays;
	}
	
}
