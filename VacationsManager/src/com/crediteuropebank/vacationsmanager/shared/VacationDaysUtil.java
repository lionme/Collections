package com.crediteuropebank.vacationsmanager.shared;

import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * 
 * Class with utility methods for operating with {@link VacationDays} domain object.
 * 
 * @author dimas
 *
 */
public class VacationDaysUtil {

	/**
	 * Calculates how vacation with specified duration can be splitted  on vacation days.
	 * 
	 * @param vacationDuration - the duration of vacation (days).
	 * @param numberOfHolidayDays - the amount of holiday days.
	 * @param userVacationDays - the amount of vacation days that is left for user.
	 * @return the VacationDays object that represents how many vacation days can be used for specified 
	 * 				vacation's duration.
	 * @throws ClientSideValidationException - if user don't have enough vacation days. 
	 * @throws IllegalArgumentException if specified vacation's duration > 24.
	 */
	// I don't like how it looks like. Change this in future.
	public static UsedVacationDays calculateVacationDays(int vacationDuration, int numberOfHolidayDays, 
						int numberOfWeekendsDays, VacationDays userVacationDays) throws CustomMessageException {
		
		UsedVacationDays vacationDays = new UsedVacationDays();
		
		vacationDuration = vacationDuration - numberOfHolidayDays;
		
		// add additional check for correct work of program.
		if (vacationDuration > 24) {
			throw new IllegalArgumentException("Total vacation duration should be lower then 24");
		}
		
		if ( (vacationDuration >= 14) && (userVacationDays.getTwoWeeksVacations()>0) ) {
			vacationDuration = vacationDuration - 14;
			numberOfWeekendsDays = numberOfWeekendsDays - 4;
			vacationDays.setTwoWeeksVacations(1);
		}
		
		// For now calculate only in such way. In future - may be change.
		
		if ( (vacationDuration >= 7) && (userVacationDays.getOneWeekVacations()>0) ) {
			vacationDuration = vacationDuration - 7;
			numberOfWeekendsDays = numberOfWeekendsDays -2;
			vacationDays.setOneWeekVacations(1);
		}
		
		// Exclude remaining weekend days from remaining vacation days.
		vacationDuration = vacationDuration - numberOfWeekendsDays;
		if ( (vacationDuration > 0) && (userVacationDays.getDayVacations().intValue() >= vacationDuration) ) {
			vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(vacationDuration)); 
			vacationDuration = 0;
		} else {
			vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(0));
		}
		
		if (vacationDuration > 0) {
			throw new CustomMessageException("You don't have enough vacation days to take this vacation.");
		}
		
		return vacationDays;
	}
}
