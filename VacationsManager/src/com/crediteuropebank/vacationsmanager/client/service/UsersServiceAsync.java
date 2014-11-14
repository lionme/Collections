package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author dimas
 *
 */
public interface UsersServiceAsync {

	/**
	 * This method fetches the list of all users from DB.
	 * 
	 * @param callback
	 */
	void getAllUsersList(AsyncCallback<List<User>> callback);

	/**
	 * This method creates new user's record in the DB.
	 * 
	 * @param user - {@link User} entity to be saved.
	 * @param callback
	 */
	void saveUser(User user, AsyncCallback<User> callback);

	/**
	 * This method updates existed user's record in DB.
	 * 
	 * @param user - {@link User} entity to be updated.
	 * @param callback
	 */
	void updateUser(User user, AsyncCallback<Void> callback);

	/**
	 * This method deletes existed user's record from DB.
	 * 
	 * @param user - {@link User} entity to be deleted.
	 * @param callback
	 */
	void deleteUser(User user, AsyncCallback<Void> callback);

	/**
	 * This method gets list with users that have specified role.
	 * 
	 * @param role - role that fetched users should have.
	 * @param callback
	 */
	void getUsersByRole(Role role, AsyncCallback<List<User>> callback);

	/**
	 * This method fetches {@link User} entity that corresponds to logged in user.
	 * 
	 * @param callback
	 */
	void getLoggedInUser(AsyncCallback<User> callback);

	/**
	 * This method fetches remaining vacation days that left for for user with specified id.
	 * 
	 * @param userId - id of the user
	 * @param callback
	 */
	void getRemainingVacationDaysForUser(long userId,
			AsyncCallback<RemainingVacationDays> callback);

	/**
	 * This method updates password for logged in user.
	 * 
	 * @param oldPassword - old password
	 * @param newPassword - new password
	 * @param callback
	 */
	void updateUserPassword(String oldPassword, String newPassword,
			AsyncCallback<Void> callback);

}
