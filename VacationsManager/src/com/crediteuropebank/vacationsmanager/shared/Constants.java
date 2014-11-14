package com.crediteuropebank.vacationsmanager.shared;

/**
 * 
 * Class with constants that can be used on client and server side. 
 * 
 * @author dimas
 *
 */
public final class Constants { // Remove some constants in properties file if necessary.
	
	/**
	 * Represents the date format, used for displaying dates in tables.
	 */
	public static final String DATE_FORMAT_STRING="yyyy-MM-dd";
	
	/**
	 * Constant that represents the number of days in two weeks vacation.
	 */
	//public static final int NUMBER_OF_DAYS_IN_TWO_WEEKS_VACATION = 14;
	
	/**
	 * Constant that represents the number of days in one week vacation.
	 */
	//public static final int NUMBER_OF_DAYS_IN_ONE_WEEK_VACATION = 5;
	
	/**
	 * Constant that represents max number of days in vacation (should be lower then 14 + 5 + 5 days 
	 * for correct work of program)
	 */
	public static final int MAX_VACATION_DAYS = 14;
	
	/**
	 * This constant specifies the number of elements per page in Data Grid.
	 */
	public static final int TABLE_PAGE_SIZE = 50;
}
