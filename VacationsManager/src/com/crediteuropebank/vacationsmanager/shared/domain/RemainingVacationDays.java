package com.crediteuropebank.vacationsmanager.shared.domain;

import java.math.BigDecimal;

import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;

/**
 * 
 * This class is specially created for holding user's remaining vacation days and for saving
 * results to the REMAINING_VACATION_DAYS table.
 * 
 * @author DIMAS
 *
 */
@Domain(logTableName = "REMAINING_VACATION_DAYS_LOG", tableName = "REMAINING_VACATION_DAYS")
public class RemainingVacationDays extends VacationDays {

	/**
	 * Default serial version ID.
	 */
	private static final long serialVersionUID = 1L;
	
	public RemainingVacationDays() {
		super();
	}

	/**
	 * This constructor initialize basic fields of the class and sets default 0 id and version.
	 * 
	 * @param twoWeeksVacations
	 * @param oneWeekVacations
	 * @param dayVacations
	 */
	public RemainingVacationDays(int twoWeeksVacations,
			int oneWeekVacations, BigDecimal dayVacations) {
		super(twoWeeksVacations, oneWeekVacations, dayVacations);
	}

	/**
	 * Copy constructor. Creates exact copy of the input {@link VacationDays} object.
	 * 
	 * @param vacationDays
	 */
	public RemainingVacationDays(VacationDays vacationDays) {
		super(vacationDays);
	}
	
}
