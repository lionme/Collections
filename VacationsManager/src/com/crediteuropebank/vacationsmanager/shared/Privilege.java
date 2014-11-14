package com.crediteuropebank.vacationsmanager.shared;

/**
 * 
 * This enum describes existed privileges.
 * 
 * @author dimas
 *
 */
public enum Privilege {
	
	/**
	 * This privilege gives a possibility to see only general tabs (vacations list and vacations chart).
	 */
	DEFAULT,
	
	/**
	 * This privilege gives a possibility to see admin tabs (users, holiday days, roles). 
	 */
	ADMIN
	
}
