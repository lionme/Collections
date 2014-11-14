package com.crediteuropebank.vacationsmanager.server.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.service.HolidayDaysService;
import com.crediteuropebank.vacationsmanager.server.manager.HolidayDaysManager;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class corresponds the server side implementation of the RPC for 
 * {@link HolidayDaysService} interface.
 * It just transfer the calls to the {@link HolidayDaysManager} which represents the real 
 * "service" layer of the application.
 * 
 * @author dimas
 *
 */
public class HolidayDaysServiceImpl implements HolidayDaysService{
	
	private HolidayDaysManager holidayDaysManager;
	
	public HolidayDaysManager getHolidayDaysManager() {
		return holidayDaysManager;
	}

	public void setHolidayDaysManager(HolidayDaysManager holidayDaysManager) {
		this.holidayDaysManager = holidayDaysManager;
	}

	/**
	 * Default constructor.
	 */
	public HolidayDaysServiceImpl() {
		
	}

	@Override
	public void saveHolidayDays(HolidayDays holidayDays) throws CustomMessageException {
		holidayDaysManager.saveHolidayDays(holidayDays);
	}

	@Override
	public void updateHolidayDays(HolidayDays holidayDays) throws CustomMessageException {
		holidayDaysManager.updateHolidayDays(holidayDays);
	}

	@Override
	public void deleteHolidayDays(HolidayDays holidayDays) throws CustomMessageException {
		holidayDaysManager.deleteHolidayDays(holidayDays);
	}

	@Override
	public HolidayDays getHolidayDaysById(long id) {
		return holidayDaysManager.getHolidayDaysById(id);
	}

	@Override
	public List<HolidayDays> getAllHolidayDays() {
		return holidayDaysManager.getAllHolidayDays();
	}

}
