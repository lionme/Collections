package com.crediteuropebank.vacationsmanager.shared.domain;

import java.math.BigDecimal;

import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;

/**
 * 
 * This class is specially created for holding vacation days used by user for his vacation and for saving
 * results to the USED_VACATION_DAYS table.
 * 
 * @author DIMAS
 *
 */
@Domain(logTableName = "USED_VACATION_DAYS_LOG", tableName = "USED_VACATION_DAYS")
public class UsedVacationDays extends VacationDays {

	/**
	 * Default serial version ID.
	 */
	private static final long serialVersionUID = 1L;
	
	public UsedVacationDays() {
		super();
	}

	/**
	 * This constructor sets basic properties of this class and default values of the id and version.
	 * 
	 * @param twoWeeksVacations
	 * @param oneWeekVacations
	 * @param dayVacations
	 */
	public UsedVacationDays(int twoWeeksVacations,
			int oneWeekVacations, BigDecimal dayVacations) {
		super(twoWeeksVacations, oneWeekVacations, dayVacations);
	}
	
	/**
	 * Copy constructor. Creates exact copy of the input {@link VacationDays} object.
	 * 
	 * @param vacationDays
	 */
	public UsedVacationDays(VacationDays vacationDays) {
		super(vacationDays);
	}
	
}
