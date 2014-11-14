package com.crediteuropebank.vacationsmanager.server.service;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.client.service.VacationsService;
import com.crediteuropebank.vacationsmanager.server.manager.VacationManager;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.dto.NonWorkingDaysDTO;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class corresponds the server side implementation of the RPC's remote service for 
 * {@link VacationService} interface.
 * It just transfer the calls to the {@link VacationManager} which represents the real "service" 
 * layer of the application.
 * 
 * @author dimas
 *
 */
public class VacationsServiceImpl implements VacationsService {
	private VacationManager vacationManager;

	public VacationManager getVacationManager() {
		return vacationManager;
	}

	public void setVacationManager(VacationManager vacationManager) {
		this.vacationManager = vacationManager;
	}

	public VacationsServiceImpl() {
		
	}

	@Override
	public List<Vacation> getAllVacations() {
		return vacationManager.getAllVacations();
	}

	@Override
	public void deleteVacation(Vacation vacation) throws CustomMessageException {
		vacationManager.deleteVacation(vacation);
	}

	@Override
	public void saveVacation(Vacation vacation) throws CustomMessageException {
		vacationManager.saveVacation(vacation);
	}

	@Override
	public void updateVacationAfterRejection(Vacation vacation) throws CustomMessageException {
		vacationManager.updateVacationAfterRejection(vacation);
	}

	@Override
	public List<Vacation> getActualVacations() {
		return vacationManager.getActualVacations();
	}

	@Override
	public NonWorkingDaysDTO getNonWorkingDaysForVacation(Vacation vacation) throws CustomMessageException {
		return vacationManager.getNonWorkingDaysForVacation(vacation);
	}

	@Override
	public List<Vacation> getRejectedVacations() {
		return vacationManager.getRejectedVacations();
	}

	@Override
	public List<Vacation> getAllVacationsForDateRange(Date startDate,
			Date endDate) throws CustomMessageException {
		return vacationManager.getAllVacationsForDateRange(startDate, endDate);
	}

	@Override
	public List<Vacation> getActualVacationsForDateRange(Date startDate,
			Date endDate) throws CustomMessageException {
		return vacationManager.getActualVacationsForDateRange(startDate, endDate);
	}

	@Override
	public List<Vacation> getRejectedVacationsForDateRange(Date startDate,
			Date endDate) throws CustomMessageException {
		return vacationManager.getRejectedVacationsForDateRange(startDate, endDate);
	}
	
}
