package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;

/**
 * 
 * This DAO interface defines CRUD and search operations for {@link Vacation} domain object.
 * 
 * @author dimas
 *
 */
public interface VacationDAO extends DAO<Vacation>{
	
	/**
	 * This method changes vacation state to new one.
	 * 
	 * @param vacation - vacation for which state should be changed.
	 * @param newState - new state
	 * @throws StaleObjectStateException if input vacation's state and record in DB versions differs
	 */
	void changeVacationState(Vacation vacation, VacationState newState) throws StaleObjectStateException;
	
	/**
	 * This method returns the actual vacations list for specified date. 
	 * By actual vacations are meant vacations for which end date >= specified date.
	 * 
	 * @returnc the list of actual vacations
	 */
	List<Vacation> getActualVacationsForDate(Date date);
	
	/**
	 * This method calculates the number of vacations for specified user in specified period. 
	 * 
	 * @param userId - id of the user.
	 * @param startDate - start date.
	 * @param endDate - end date.
	 * @return the number of vacations in the specified period for specified user.
	 */
	int calculateVacationsForPeriod(long userId, Date startDate, Date endDate);
	
	/**
	 * This method gets list of vacation's ids in specified period for which specified user is deputy.
	 * 
	 * @param userId - id of the user.
	 * @param startDate - start date.
	 * @param endDate - end date.
	 * @return the list of vacations ids in specified period for which specified user is deputy
	 */
	List<Long> getVacationsIdsWhereUserIsDeputy(long userId, Date startDate, Date endDate);
	
	/**
	 * This method calculates users with specified role which have vacation in the specified period.
	 * 
	 * @param role - user's role.
	 * @param startDate - start date.
	 * @param endDate - end date.
	 * @return the number of users with specified role who has vacation in the specified period.
	 */
	int calculateUsersWithVacationForRole(Role role, Date startDate, Date endDate);
	
	/**
	 * This method fetches the list of rejected vacations.
	 * 
	 * @return the list of rejected vacations.
	 */
	List<Vacation> getRejectedVacations();
	
	/**
	 * 
	 * This method fetches the list of all vacations for specified user.
	 * 
	 * @param user - user which is owner of the vacations to be deleted.
	 * @return the list of all vacations for user.
	 */
	List<Vacation> getAllVacationsForUser(User user);
	
	/** 
	 * This method deletes user from all deputies list.
	 * 
	 * @param user
	 */
	void deleteUserFromAllDeputiesLists(User user);
	
	/**
	 * This method gets the list of all vacations for specified period.
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @return the list of all {@link Vacation} entities for specified period.
	 */
	List<Vacation> getAllVacationsForDateRange(Date startDate, Date endDate);
	
	/**
	 * This method gets actual vacations (which end date >= current date and status of vacation is not REJECTED)
	 * for specified period.
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @return the list of actual vacations for period.
	 */
	List<Vacation> getActualVacationsForDateRange(Date startDate, Date endDate);
	
	/**
	 * This method fetches list of the rejected vacations for specified period.
	 * 
	 * @param startDate - start date of the range
	 * @param endDate - end date of the range
	 * @return the list of the {@link Vacation} entities with REJECTED status for specified period
	 */
	List<Vacation> getRejectedVacationsForDateRange(Date startDate, Date endDate);
}
