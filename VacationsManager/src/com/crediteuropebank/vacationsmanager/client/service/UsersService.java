package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *	This remote service interface contains methods to operate with User and Role entities.
 *
 * @author dimas
 *
 */
@RemoteServiceRelativePath("rpc/users")
public interface UsersService extends RemoteService{
	
	/**
	 * This method fetches the list of all users from DB.
	 * 
	 * @return the list of all {@link User} entities from DB.
	 */
	List<User> getAllUsersList();
	
	/**
	 * This method creates new user's record in the DB.
	 * 
	 * @param user - {@link User} entity to be saved.
	 * @return saved {@link User} entity with generated id.
	 * @throws CustomMessageException if some problem occurs during saving and
	 * 					we need to inform user about it.
	 */
	User saveUser(User user) throws CustomMessageException;
	
	/**
	 * This method updates existed user's record in DB.
	 * 
	 * @param user - {@link User} entity to be updated.
	 * @throws CustomMessageException if some problem occurs during saving and
	 * 					we need to inform user about it.
	 */
	void updateUser(User user) throws CustomMessageException;
	
	/**
	 * This method deletes existed user's record from DB.
	 * 
	 * @param user - {@link User} entity to be deleted.
	 * @throws CustomMessageException if some problem occurs during saving and
	 * 					we need to inform user about it.
	 */
	void deleteUser(User user) throws CustomMessageException;
	
	/**
	 * This method gets list with users that have specified role.
	 * 
	 * @param role - role that fetched users should have.
	 * @return the list of fetched users that have specified role.
	 */
	List<User> getUsersByRole(Role role);
	
	/**
	 * This method fetches {@link User} entity that corresponds to logged in user.
	 * 
	 * @return the {@link User} entity that represents logged in user's info.
	 * @throws CustomMessageException 
	 */
	User getLoggedInUser() throws CustomMessageException;
	
	/**
	 * This method fetches remaining vacation days that left for for user with specified id.
	 * 
	 * @param userId - id of the user
	 * @return the {@link RemainingVacationDays} entity that represents information about vacation
	 * 						days left for user with specified id.
	 */
	RemainingVacationDays getRemainingVacationDaysForUser(long userId);
	
	/**
	 * This method updates password for logged in user.
	 * 
	 * @param oldPassword - old password
	 * @param newPassword - new password
	 * @throws CustomMessageException if some problems occur during saving and we need to inform
	 * 						user about them
	 */
	void updateUserPassword(String oldPassword, String newPassword) throws CustomMessageException;
}
