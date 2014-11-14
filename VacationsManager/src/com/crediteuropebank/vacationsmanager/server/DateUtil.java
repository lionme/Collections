package com.crediteuropebank.vacationsmanager.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;

/**
 * This utility class helps to work with Date class. It has numerous methods to work with 
 * {@link Date} class.
 * 
 * @author dimas
 *
 */
public class DateUtil {
	/**
	 * Date format, that should be used in application.
	 */
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	/**
	 * {@link DateFormat} instance used in this class.
	 */
	private static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
	
	/**
	 * Returns current {@link Date} without time part.
	 * @return the current date without time part
	 */
	public static Date getCurrentDateWithoutTime() {
		Calendar currentDate = Calendar.getInstance();
		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		
		return currentDate.getTime();
	}
	
	/**
	 * Returns number of milliseconds that corresponds to current date.
	 * 
	 * @return the number of milliseconds that corresponds to current date.
	 */
	public static long getCurrentDateMilliseconds() {
		Date currentDate = new Date();
		
		return currentDate.getTime();
	}
	
	/**
	 *  
	 * @param date
	 * @return
	 * 
	 * @deprecated - because this function have not been tested and is not used.
	 */
	@Deprecated
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}
	
	/**
	 * This method parses input string and return {@link Date} object which corresponds to it.
	 * String with date representation should be in format: yyyy-MM-dd
	 *
	 * @param dateString - string representation of {@link Date} object
	 * @return the {@link Date} object which corresponds to input string.
	 * @throws ParseException if input string has wrong format.
	 */
	public static Date parseString(String dateString) throws ParseException {
		return dateFormat.parse(dateString);
	}
	
	/**
	 * This method returns copy of input date object with removed Time part.
	 * 
	 * @param date - the Date for which Time part should be removed.
	 * @return copy of the input Date object with removed Time part.
	 */
	public static Date removeTimePart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar.getTime();
	}
	
	/**
	 * This function get weekends in the date range as {@link Holidaydays} domain object.
	 * 
	 * @param startDate - start date.
	 * @param endDate - end date.
	 * @return the number of weekends in the date range.
	 * 
	 * @deprecated - bad implementation
	 */
	@Deprecated
	public static List<HolidayDays> getWeekendsInRange(Date startDate, Date endDate) {
		List<HolidayDays> listOfNonWorkingDays = new ArrayList<HolidayDays>();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		
		while(!calendar.getTime().after(endDate)) {
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			
			switch(dayOfWeek) {
			case 1:{ // if Sunday
				HolidayDays nonWorkingDays = new HolidayDays();
				nonWorkingDays.setStartDate(calendar.getTime());
				nonWorkingDays.setEndDate(calendar.getTime());
				nonWorkingDays.setDescription("SUNDAY");
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				listOfNonWorkingDays.add(nonWorkingDays);
				break;
			}
			case 7: {// if Saturday
				HolidayDays nonWorkingDays = new HolidayDays();
				if (!calendar.getTime().equals(endDate)) { // if Sunday is included into vacation duration
					nonWorkingDays.setStartDate(calendar.getTime());
					calendar.add(Calendar.DAY_OF_MONTH, 1);
					nonWorkingDays.setEndDate(calendar.getTime());
					nonWorkingDays.setDescription("Weekends");
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				} else { // if Sunday is not included into vacation duration.
					nonWorkingDays.setStartDate(calendar.getTime());
					nonWorkingDays.setEndDate(calendar.getTime());
					nonWorkingDays.setDescription("SATURDAY");
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				}
				listOfNonWorkingDays.add(nonWorkingDays);
				break;
			}
			default:{
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				break;
			}
			}
		}
		
		return listOfNonWorkingDays;
	}
	
	/**
	 * Calculates number of weekend days in specified dates range.
	 * 
	 * @param startDate - start date.
	 * @param endDate - end date.
	 * @return the number of weekend days.
	 */
	public static int calculateNumberOfWeekendsInRange(Date startDate, Date endDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		
		int counter = 0;
		while(!calendar.getTime().after(endDate)) {
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek==1 || dayOfWeek==7) {
				counter++;
			}
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		return counter;
	}
	
	/**
	 * This method converts java.util.Date object to java.sql.Date object.
	 * 
	 * @param date - date that should be converted from java.util.Date type to java.sql.Date type.
	 * @return the java.sql.Date object
	 */
	public static java.sql.Date convertToSqlDate(Date date) {
		return new java.sql.Date(date.getTime());
	}
	
	/**
	 * This method adds specified number of days to the specified date.
	 * 
	 * @param date - the date to what days will be added.
	 * @param numberOfDays - number of days to add.
	 * @return the Date object with added days.
	 */
	public static Date addDays(Date date, int numberOfDays) {
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(date);
		
		calendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
		
		return calendar.getTime();
	}
	
}
