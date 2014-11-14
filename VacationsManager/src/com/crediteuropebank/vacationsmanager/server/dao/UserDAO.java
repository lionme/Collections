package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;

/**
 * 
 * This interface defines CRUD and search operations for {@link User} domain object.
 * 
 * @author dimas
 *
 */
public interface UserDAO extends DAO<User> {
	
	/**
	 * This method returns list of users with specified role.
	 * 
	 * @param role - role that users should have.
	 * @return the list of users with specified role.
	 */
	List<User> getUsersByRole(Role role);
	
	/**
	 * This method fetches user by his name (it is unique).
	 * 
	 * @param username - user's name
	 * @return user from DB; if no user was found - return null.
	 */
	User getUserByUserName(String username);
	
	/**
	 * This method calculates total amount of active users (with status=1) in DB.
	 * 
	 * @return total amount of the users
	 */
	int getTotalAmountOfUsersWithRole(Role role);
	
	/**
	 * This method updates password for user.
	 * 
	 * @param userId
	 * @param newPassword - <ins>not encoded</ins> new password.
	 */
	void updatePassword(long userId, String newPassword);
	
	/**
	 * This method checks whether entered password is correct.
	 * 
	 * @param userId
	 * @param oldPassword - <ins>not encoded</ins> old password.
	 * @return true if password is correct and false in other case.
	 */
	boolean isPasswordCorrect(long userId, String oldPassword);

}
