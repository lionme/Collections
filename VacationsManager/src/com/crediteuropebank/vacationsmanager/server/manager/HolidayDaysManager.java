package com.crediteuropebank.vacationsmanager.server.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.client.service.HolidayDaysService;
import com.crediteuropebank.vacationsmanager.server.dao.HolidayDaysDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * <p>In this "manager" class starts real business logic for {@link HolidayDays} domain 
 * object.
 * In this "manager" class starts real business logic. {@link HolidayDaysService} is used only 
 * to deliver requests from user to business layer.</p>
 * 
 * <p>In this class logger is used explicitly for marking boundaries of each "service" method call 
 * by adding INFO level messages to log.</p>
 * 
 * @author dimas
 *
 */
@Service(value="holidayDaysManager")
@Transactional(propagation=Propagation.SUPPORTS, rollbackFor=Exception.class)
public class HolidayDaysManager {
	
	@Autowired
	private HolidayDaysDAO holidayDaysDAO;
	
	/**
	 * This method saves new holiday days.
	 * 
	 * @param holidayDays - the {@link HolidayDays} entity to be saved
	 * @return the same {@link HolidayDays} object with generated id
	 * @throws CustomMessageException if some problem occurs during saving and we need to inform
	 * 				user about it
	 * @throws IllegalArgumentException if input holidayDays object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public HolidayDays saveHolidayDays(final HolidayDays holidayDays) throws CustomMessageException {
		if (holidayDays == null) {
			throw new IllegalArgumentException("Input holdayDays record should not be null!");
		}
		
		validateHolidayDaysPeriod(holidayDays);
		
		HolidayDays outHolidayDays = holidayDaysDAO.save(holidayDays);
		
		return outHolidayDays;
	}
	
	/**
	 * This method updates existed holiday days.
	 * 
	 * @param holidayDays - the {@link HolidayDays} entity to be updated
	 * @throws CustomMessageException if some problem occurs during updating and we need to inform
	 * 				user about it
	 * @throws IllegalArgumentException if input holidayDays object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void updateHolidayDays(final HolidayDays holidayDays) throws CustomMessageException {
		if (holidayDays == null) {
			throw new IllegalArgumentException("Input holdayDays record should not be null!");
		}
		
		validateHolidayDaysPeriod(holidayDays);
		
		try {
			holidayDaysDAO.update(holidayDays);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method deletes existed holiday.
	 * 
	 * @param holidayDays - the {@link HolidayDays} entity to be deleted
	 * @throws CustomMessageException if some problem occurs during deleting and we need to inform
	 * 				user about it
	 * @throws IllegalArgumentException if input holidayDays object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void deleteHolidayDays(final HolidayDays holidayDays) throws CustomMessageException {
		if (holidayDays == null) {
			throw new IllegalArgumentException("Input holdayDays record should not be null!");
		}
		
		try {
			holidayDaysDAO.delete(holidayDays);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method fetches holidays days record by its id.
	 * 
	 * @param id
	 * @return the holiday days record by its id.
	 */
	public HolidayDays getHolidayDaysById(long id) {
		return holidayDaysDAO.getById(id);
	}
	
	/**
	 * This method fetches list of all holiday days records from DB.
	 * 
	 * @return the list of all holiday days records from DB.
	 */
	public List<HolidayDays> getAllHolidayDays() {
		return holidayDaysDAO.getAll();
	}
	
	/**
	 * 
	 * <p>This method checks holiday days period on such conditions:</p>
	 * <ul>
	 * 		<li>start date <= end date;</li>
	 * 		<li>there is no another holiday days defined in the specified period with status 1;</li>
	 * </ul>
	 * 
	 * @param holidayDays - {@link HolidayDays} entity object that represents holidays info.
	 * @throws CustomMessageException if some of the checks fail
	 */
	private void validateHolidayDaysPeriod(final HolidayDays holidayDays) throws CustomMessageException {
		
		if (holidayDays.getEndDate().before(holidayDays.getStartDate())) {
			throw new CustomMessageException("Holiday days end date could not be less then start date!");
		}
		
		List<HolidayDays> existedHolidayDays = holidayDaysDAO.getForPeriod(holidayDays.getStartDate(), 
				holidayDays.getEndDate());
		
		if (existedHolidayDays.size() > 0) {
			throw new CustomMessageException("You cannot add holiday days because there is nother one " +
					"in the same period!");
		}
	}
}
