package com.crediteuropebank.vacationsmanager.server.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.service.UsersService;
import com.crediteuropebank.vacationsmanager.server.manager.UserManager;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class corresponds the server side implementation of the RPC for {@link UserService} 
 * interface.
 * It just transfer the calls to the {@link UserManager} class which represents the real 
 * "service" layer of the application.
 * 
 * @author DIMAS
 *
 */
public class UsersServiceImpl implements UsersService{
	
	private UserManager userManager;
	
	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public UsersServiceImpl() {
		
	}

	@Override
	public List<User> getAllUsersList() {
		return userManager.getAllUsersList();
	}

	@Override
	public User saveUser(User user) throws CustomMessageException {
		return userManager.saveUser(user);
	}

	@Override
	public void updateUser(User user) throws CustomMessageException {
		userManager.updateUser(user);
	}

	@Override
	public void deleteUser(User user) throws CustomMessageException {
		userManager.deleteUser(user);
	}

	@Override
	public List<User> getUsersByRole(Role role) {
		return userManager.getUsersByRole(role);
	}

	@Override
	public User getLoggedInUser() throws CustomMessageException {
		return userManager.getLoggedInUser();
	}

	@Override
	public RemainingVacationDays getRemainingVacationDaysForUser(long userId) {
		return userManager.getRemainingVacationDaysForUser(userId);
	}

	@Override
	public void updateUserPassword(String oldPassword, String newPassword) throws CustomMessageException {
		userManager.updateUserPassword(oldPassword, newPassword);
	}

}
