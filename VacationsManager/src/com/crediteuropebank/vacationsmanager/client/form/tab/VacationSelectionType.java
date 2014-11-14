package com.crediteuropebank.vacationsmanager.client.form.tab;

/**
 * This enum holds the type of vacation selection.
 * 
 * @author dimas
 *
 */
enum VacationSelectionType {
	
	/**
	 * Select all vacations from DB. 
	 */
	ALL,
	
	/**
	 * Select just actual vacations from DB (which start date >= then today).
	 */
	JUST_ACTUAL,
	
	/**
	 * Select rejected vacations from DB.
	 */
	REJECTED
}
