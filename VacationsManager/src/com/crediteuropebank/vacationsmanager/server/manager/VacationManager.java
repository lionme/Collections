package com.crediteuropebank.vacationsmanager.server.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.PropertiesBean;
import com.crediteuropebank.vacationsmanager.server.dao.HolidayDaysDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.server.dao.UsedVacationDaysDAO;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.dao.VacationDAO;
import com.crediteuropebank.vacationsmanager.server.dao.RemainingVacationDaysDAO;
import com.crediteuropebank.vacationsmanager.server.validation.ServerValidationUtil;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
import com.crediteuropebank.vacationsmanager.shared.dto.NonWorkingDaysDTO;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * <p>In this "manager" class starts real business logic. Service layer is used only to deliver 
 * requests from user to business layer.</p>
 * 
 * <p>In this class logger is used explicitly for marking boundaries of each "service" method call 
 * by adding INFO level messages to log.</p>
 *  
 * @author dimas
 *
 */
@Service(value="vacationManager")
@Transactional(propagation=Propagation.SUPPORTS, rollbackFor=Exception.class, readOnly=true)
public class VacationManager {
	
	@Autowired
	private VacationDAO vacationDAO;
	
	@Autowired
	private HolidayDaysDAO holidayDaysDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RemainingVacationDaysDAO userVacationDaysDAO;
	
	@Autowired
	private UsedVacationDaysDAO usedVacationDaysDAO;
	
	@Autowired
	private ApprovalManager approvalManager;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private ServerValidationUtil validationUtil;
	
	@Autowired
	private PropertiesBean propertiesBean;
	
	public VacationManager() {
		
	}
	
	/**
	 * This method gets the list of all vacations.
	 * 
	 * @return the list of all {@link Vacation} entities.
	 */
	public List<Vacation> getAllVacations() {

		return vacationDAO.getAll();
	}

	/**
	 * This method saves new vacation.
	 * 
	 * @param vacation - {@link Vacation} entity to be saved.
	 * @return saved {@link Vacation} entity with generated id. 
	 * @throws CustomMessageException if we want to inform user about some problems with input data
	 * @throws IllegalArgumentException if input vacation object is null, if it has wrong state 
	 * 				or if used vacation days is null.
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public Vacation saveVacation(final Vacation vacation) throws CustomMessageException {
		if (vacation == null) {
			throw new IllegalArgumentException("Input vacation should not be null");
		}

		if (vacation.getState() != VacationState.JUST_OPENED) {
			throw new IllegalArgumentException("You can save only vacation with state JUST_OPENED");
		}
		
		// Check that vacations list is not empty.
		if ( (vacation.getDeputies() == null) && (vacation.getDeputies().size() != 0) ) {
			throw new IllegalArgumentException("Vacation's deputies list should not be null. " +
					"It should contain at least 1 deputy.");
		}

		Vacation savedVacation = null;

		try {
			// validate entered values
			validationUtil.validate(vacation);

			// validate vacation's period; check if user can go into vacation in this period.
			validateVacationPeriod(vacation);

			//VacationDays remainingVacationDays = vacation.getUser().getVacationDays();
			// Fetch remaining vacation days from DB for associating them with current transaction
			RemainingVacationDays remainingVacationDays = userVacationDaysDAO.getById(vacation.getUser().getVacationDays().getId());
			//VacationDays remainingVacationDays = vacation.getUser().getVacationDays();
			VacationDays usedForVacationDays = vacation.getUsedVacationDays();
			
			if (usedForVacationDays == null) {
				throw new IllegalArgumentException("Vacation days that is used for vacation cannot be null!");
			}

			/* 
			 * Check that user still have enough vacation days for current vacation. 
			 * If not - throw a message exception.
			 */
			if (!checkVacationDays(remainingVacationDays, usedForVacationDays)) {
				throw new CustomMessageException("You don't have enogh vacation days for this vacation. Please," +
						"refresh page and try again.");
			}

			// execute insert to DB
			savedVacation = vacationDAO.save(vacation);

			// Deduct vacation days that is necessary for vacation from user's remaining vacation days.
			remainingVacationDays.deductVacationDays(usedForVacationDays.getTwoWeeksVacations(), 
					usedForVacationDays.getOneWeekVacations(), 
					usedForVacationDays.getDayVacations());

			// Save changes in user's vacation days to DB.
			userVacationDaysDAO.update(remainingVacationDays);

			// Create approval flow for vacation.
			approvalManager.createApprovalFlow(vacation);
			
			/*
			 *  Update remaining user's vacation days data (version was changed after updating).
			 *  This is necessary because we returned saved vacation entity back and it should 
			 *  contain only actual data.
			 */
			remainingVacationDays = userVacationDaysDAO.getById(vacation.getUser().getId());
			savedVacation.getUser().setVacationDays(remainingVacationDays);

		} catch(StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}

		return savedVacation;
	}
	
	/**
	 * This method checks does user have enough vacation days for requested vacation.
	 * 
	 * @param remainingVacationDays - vacations days that remain for user.
	 * @param usedVacationDays - vacation days that will be used for vacation.
	 * @return true - if user has enough vacation days; false - if not.
	 */
	private boolean checkVacationDays(final VacationDays remainingVacationDays, 
			final VacationDays usedVacationDays) {
		if ( remainingVacationDays.getTwoWeeksVacations() >= usedVacationDays.getTwoWeeksVacations() 
				&& remainingVacationDays.getOneWeekVacations() >= usedVacationDays.getOneWeekVacations()
				&& remainingVacationDays.getDayVacations().compareTo(usedVacationDays.getDayVacations()) >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method checks that user can go to vacation in the specified period.
	 * 
	 * @param vacation - vacation to be checked.
	 * @throws CustomMessageException if we need to inform user that he cannot go to vacation in the
	 * 									chosen period.
	 * 
	 */
	private void validateVacationPeriod(final Vacation vacation) throws CustomMessageException {
		
		// We cannot take vacation for past date
		if (vacation.getStartDate().before(DateUtil.getCurrentDateWithoutTime())) {
			throw new CustomMessageException("You cannot take vacation for past date!");
		}
		
		// Limit user so that he cannot take a vacation for more then 90 days ahead.
		int maxDaysAhead = propertiesBean.getMaxDaysAhead();
		Date lastPossibleDate = DateUtil.addDays(DateUtil.getCurrentDateWithoutTime(), maxDaysAhead);
		if (vacation.getEndDate().after(lastPossibleDate)) {
			throw new CustomMessageException("You can take a vacation no more than " + maxDaysAhead + " days forward!");
		}
		
		// Check if user has another vacation at the same period.
		int existedVacations = vacationDAO.calculateVacationsForPeriod(vacation.getUser().getId(), 
				vacation.getStartDate(), 
				vacation.getEndDate());
		
		if (existedVacations > 0) {
			throw new CustomMessageException("You cannot take vacation at specified period because you " +
					"has another vacation at the same period");
		}
		
		// Check that no one of the deputies has vacation at the same period.
		for(User deputy: vacation.getDeputies()) {
			int existedDeputyVacations = vacationDAO.calculateVacationsForPeriod(vacation.getUser().getId(), 
					vacation.getStartDate(), 
					vacation.getEndDate());
			
			if (existedDeputyVacations > 0) {
				throw new CustomMessageException("Deputy " + deputy.getFullName() +
						" has vacation at the same period. He cannot be your deputy.");
			}
		}
		
		// Check if user will not be deputy for another vacations in the same period.
		List<Long> listOfVacationsIDForWhichUserIsDeputy = 
				vacationDAO.getVacationsIdsWhereUserIsDeputy(vacation.getUser().getId(), 
						vacation.getStartDate(), 
						vacation.getEndDate());
		if (listOfVacationsIDForWhichUserIsDeputy.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("You cannot take vacation in specified period because you will be " +
					"deputy for another vacation(s) with id: ");
			
			for (Long id: listOfVacationsIDForWhichUserIsDeputy) {
				sb.append(id);
				sb.append("; ");
			}
			
			throw new CustomMessageException(sb.toString());
		}
		
		/*
		 *  Check that half of all users with vacation owner's role will stay at 
		 *  their work places during this vacation.
		 */
		int numberOfExistedUsers = userDAO.getTotalAmountOfUsersWithRole(vacation.getUser().getRole());
		int numberOfUsersWithVacation = vacationDAO.calculateUsersWithVacationForRole(vacation.getUser().getRole(), 
				vacation.getStartDate(), vacation.getEndDate());
		
		double ratio = ((double)(numberOfUsersWithVacation + 1))/numberOfExistedUsers;
		
		if ( (numberOfExistedUsers>=2) && (ratio > 0.5D) ) {
			throw new CustomMessageException("You cannot go to vacation because too many people with the same role " +
					"have already entered vacation on requested period.");
		}
	}

	/**
	 * This method updates vacation that have been rejected.
	 * 
	 * <p/>Note that this method can be used only for vacation with status REJECTED (look at {@link VacationState}).
	 * 
	 * @param vacation - vacation to be updated.
	 * @throws CustomMessageException if we want to inform user about some problem that occur 
	 * 					during updating.
	 * @throws IllegalArgumentException if input vacation object is null
	 * 
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void updateVacationAfterRejection(final Vacation vacation) throws CustomMessageException {
		if (vacation == null) {
			throw new IllegalArgumentException("Input vacation object should not be null!");
		}
		
		try {

			// Validate vacation's data using validator.
			validationUtil.validate(vacation);

			/*
			 *  Check whether vacation's owner is logged in user. For test purposes this
			 *  validation was removed. Think about is it necessary to enable it again.
			 */
			/*User loggedInUser = userManager.getLoggedInUser();
			if (!vacation.getUser().equals(loggedInUser)) {
				throw new CustomMessageException("You cannot update vacation because it is not yours.");
			}*/

			// Check that vacation have been already rejected.
			if (vacation.getState() != VacationState.REJECTED) {
				throw new CustomMessageException("You cannot update this vacation because it have not been rejected." +
						"Only vacations that have been rejected can be updated.");
			}

			/* validate vacation's period; check if user can go into vacation in this period. */
			validateVacationPeriod(vacation);

			/*
			 *  Fetch vacation days that is used for current vacation before updating (used vacation days id == 
			 *  vacation's id.
			 */
			VacationDays oldVacationDays = usedVacationDaysDAO.getById(vacation.getId());
			VacationDays newVacationDays = vacation.getUsedVacationDays();

			/*
			 *  Check whether new vacation days not equals to old one.
			 *  If yes - recalculate user's remaining vacation days and update them in DB.
			 */
			if (!oldVacationDays.equals(newVacationDays)) {
				int twoWeeksVacationsDifference = newVacationDays.getTwoWeeksVacations() - oldVacationDays.getTwoWeeksVacations();
				int oneWeekVacationDifference = newVacationDays.getOneWeekVacations() - oldVacationDays.getOneWeekVacations();
				BigDecimal dayVacationDifference = newVacationDays.getDayVacations().subtract(oldVacationDays.getDayVacations());

				// Get vacation days that remains for user.
				//VacationDays remainingVacationDays = userVacationDaysDAO.getById(vacation.getUser().getVacationDays().getId());
				RemainingVacationDays userVacationDays = vacation.getUser().getVacationDays();
				userVacationDays.deductVacationDays(twoWeeksVacationsDifference, oneWeekVacationDifference, dayVacationDifference);
				userVacationDaysDAO.update(userVacationDays);
			}
			
			vacation.setState(VacationState.JUST_OPENED);

			// Update vacation.
			vacationDAO.update(vacation);

			// First we remove old approval flow (all approval steps related to vacation).
			approvalManager.deleteApprovalFlow(vacation);
			
			// Now creates new approval flow for vacation.
			approvalManager.createApprovalFlow(vacation);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}	
	
	/**
	 * This method updates vacation duration (start and end days).
	 * 
	 * For correct updating it validates such things:
	 * 	- entered vacation's data (using validation util);
	 * 	- if logged in user is owner of input vacation;
	 * 	- if vacation state is JUST_OPENED
	 * 	- validates vacation period (look at method validateVacationPeriod(..)) 
	 * 
	 * @param vacation
	 * @throws CustomMessageException
	 * 
	 * @deprecated works wrong
	 */
	@Deprecated
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void updateVacationDuration(Vacation vacation) throws CustomMessageException {
		try {

			// Validate vacation's data using validator.
			validationUtil.validate(vacation);

			// Check whether vacation's user is logged in user.
			User loggedInUser = userManager.getLoggedInUser();
			if (!loggedInUser.equals(vacation.getUser())) {
				throw new CustomMessageException("You cannot update vacation because it is not yours.");
			}

			// Check that vacation have not been yet approved by anyone.
			if (vacation.getState() != VacationState.JUST_OPENED) {
				throw new CustomMessageException("You cannot update this vacation because at least one person " +
						"have already approved it.");
			}

			// validate vacation's period; check if user can go into vacation in this period.
			validateVacationPeriod(vacation);

			/*
			 *  Fetch vacation days that is used for current vacation before updating (used vacation days id == 
			 *  vacation's id.
			 */
			VacationDays oldVacationDays = usedVacationDaysDAO.getById(vacation.getId());
			VacationDays newVacationDays = vacation.getUsedVacationDays();

			/*
			 *  Check whether new vacation days not equals to old one.
			 *  If yes - recalculate user's remaining vacation days and update them in DB.
			 */
			if (!oldVacationDays.equals(newVacationDays)) {
				int twoWeeksVacationsDifference = newVacationDays.getTwoWeeksVacations() - oldVacationDays.getTwoWeeksVacations();
				int oneWeekVacationDifference = newVacationDays.getOneWeekVacations() - oldVacationDays.getOneWeekVacations();
				BigDecimal dayVacationDifference = newVacationDays.getDayVacations().subtract(oldVacationDays.getDayVacations());

				// Get vacation days that remains for user.
				//VacationDays remainingVacationDays = userVacationDaysDAO.getById(vacation.getUser().getVacationDays().getId());
				RemainingVacationDays userVacationDays = vacation.getUser().getVacationDays();
				userVacationDays.addVacationDays(twoWeeksVacationsDifference, oneWeekVacationDifference, dayVacationDifference);
				userVacationDaysDAO.update(userVacationDays);
			}

			vacationDAO.update(vacation);

		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method deletes vacation (Changes it's status to 0). Also this method removes all approval steps, 
	 * related to this vacation.
	 * 
	 * @param vacation - vacation that should be deleted.
	 * @throws CustomMessageException if we need to inform user about some problem.
	 * @throws IllegalArgumentException if input vacation object is null.
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void deleteVacation(final Vacation vacation) throws CustomMessageException {
		if (vacation == null) {
			throw new IllegalArgumentException("Input vacation should not be null");
		}
		
		/*
		 * Check whether vacations state is APPROVED and its start date < then today.
		 * In such case you cannot delete vacation.
		 */
		if (vacation.getState() == VacationState.APPROVED &&
				vacation.getStartDate().before(DateUtil.getCurrentDateWithoutTime())) {
			throw new CustomMessageException("You cannot delete vacation because it is already started (its start date " +
					"is lower then current date).");
		}
		
		/*if (vacation.getState() == VacationState.APPROVED) {
			throw new CustomMessageException("You cannot delete vacation that have already been approved.");
		}*/
		
		try {
			/* First remove approval flow for vacation. */
			approvalManager.deleteApprovalFlow(vacation);

			/* Then remove specified vacation. */
			vacationDAO.delete(vacation);
			
			/* Update remaining user's vacation days. Add to them days that was previously used for vacation. */
			RemainingVacationDays remainingVacationDays = vacation.getUser().getVacationDays();		
			remainingVacationDays.add(vacation.getUsedVacationDays());
			userVacationDaysDAO.update(remainingVacationDays);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method gets actual vacations (which end date >= current date and status of vacation is not REJECTED).
	 * 
	 * @return the list of actual vacations.
	 */
	public List<Vacation> getActualVacations() {
		List<Vacation> actualVacationsList = vacationDAO.getActualVacationsForDate(DateUtil.getCurrentDateWithoutTime());
		
		return actualVacationsList;
	}

	/**
	 * This function is used for fetching a list of non working days for specified period.
	 * 
	 * @param startDate - start date
	 * @param endDate - end date
	 * @return the list of non working days (domain object - {@link HolidayDays}) for specified period
	 * @throws CustomMessageException 
	 */
	public NonWorkingDaysDTO getNonWorkingDaysForVacation(final Vacation vacation) throws CustomMessageException {
		validationUtil.validate(vacation);
		
		List<HolidayDays> holidayDaysList = holidayDaysDAO.getForPeriod(vacation.getStartDate(), 
				vacation.getEndDate());
		
		for (HolidayDays holidayDays: holidayDaysList) {
			if (holidayDays.getStartDate().before(vacation.getStartDate())) {
				holidayDays.setStartDate(vacation.getStartDate());
			}
			if (holidayDays.getEndDate().after(vacation.getEndDate())) {
				holidayDays.setEndDate(vacation.getEndDate());
			}
		}
		
		// Add weekend days to the total list of no working days.
		//nonWorkingDaysList.addAll(DateUtil.getWeekendsInRange(startDate, endDate));
		
		// Calculate the number of weekend days in the requested vacation's period
		int numberOfWeekends = DateUtil.calculateNumberOfWeekendsInRange(vacation.getStartDate(), 
				vacation.getEndDate());
		
		NonWorkingDaysDTO nonWorkingDaysDTO = new NonWorkingDaysDTO(holidayDaysList, numberOfWeekends);
		
		return nonWorkingDaysDTO;
	}
	
	/**
	 * This method gets vacation by its id.
	 * 
	 * @param id - id of the vacation.
	 * @return vacation with specified id.
	 */
	public Vacation getVacationById(long id) {
		Vacation vacation = vacationDAO.getById(id);
		
		return vacation;
	}

	/**
	 * This method fetches list of the rejected vacations.
	 * 
	 * @return the list of the rejected vacations
	 */
	public List<Vacation> getRejectedVacations() {
		List<Vacation> rejectedVacations = vacationDAO.getRejectedVacations();
		
		return rejectedVacations;
	}
	
	/**
	 * This method returns the list of all vacations for specified user.
	 * 
	 * @param user - the owner of vacation
	 * @return the list of vacations for specified user
	 * @throws IllegalArgumentException if input user object is null
	 */
	List<Vacation> getListOfAllVacationsForUser(final User user) {
		if (user == null) {
			throw new IllegalArgumentException("Input user object should not be null!");
		}
		
		return vacationDAO.getAllVacationsForUser(user);
	}
	
	/**
	 * This method gets the list of all vacations for specified period.
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @return the list of all {@link Vacation} entities for specified period.
	 * @throws CustomMessageException if date range validation fails
	 */
	public List<Vacation> getAllVacationsForDateRange(final Date startDate, final Date endDate) throws CustomMessageException {
		checkDateRange(startDate, endDate);
		
		return vacationDAO.getAllVacationsForDateRange(startDate, endDate);
	}
	
	/**
	 * This method gets actual vacations (which end date >= current date and status of vacation is not REJECTED)
	 * for specified period.
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @return the list of actual vacations for period.
	 * @throws CustomMessageException if date range validation fails
	 */
	public List<Vacation> getActualVacationsForDateRange(final Date startDate, final Date endDate) throws CustomMessageException {
		Date currentDate = DateUtil.getCurrentDateWithoutTime();
		
		checkDateRange(startDate, endDate);
		
		if (startDate.before(currentDate)) {
			throw new CustomMessageException("Start date of the specified date range should not be lower" +
					" then today for actual vacation searching!");
		}
		
		return vacationDAO.getActualVacationsForDateRange(startDate, endDate);
	}
	
	/**
	 * This method fetches list of the rejected vacations for specified period.
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @return the list of the {@link Vacation} entities with REJECTED status for specified period
	 * @throws CustomMessageException if date range validation fails
	 */
	public List<Vacation> getRejectedVacationsForDateRange(final Date startDate, final Date endDate) throws CustomMessageException {
		checkDateRange(startDate, endDate);
		
		return vacationDAO.getRejectedVacationsForDateRange(startDate, endDate);
	}
	
	/**
	 * This method validates date range (checks whether both dates aren't null 
	 * and start date <= end date).
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @throws CustomMessageException if some of the validation rules fails
	 */
	private void checkDateRange(final Date startDate, final Date endDate) throws CustomMessageException {
		if (startDate == null) {
			throw new CustomMessageException("Start date of the range should not be empty!");
		}
		if (endDate == null) {
			throw new CustomMessageException("End date of the range should not be empty!");
		}
		if (startDate.after(endDate)) {
			throw new CustomMessageException("Start date of the range should not be greater then end date");
		}
	}
}
