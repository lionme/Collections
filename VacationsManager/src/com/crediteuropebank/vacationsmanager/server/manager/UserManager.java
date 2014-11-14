package com.crediteuropebank.vacationsmanager.server.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.dao.DublicateEntryException;
import com.crediteuropebank.vacationsmanager.server.dao.RemainingVacationDaysDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.dao.VacationDAO;
import com.crediteuropebank.vacationsmanager.server.validation.ServerValidationUtil;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * <p>In this "manager" class starts real business logic. Service layer is used only to deliver 
 * requests from user to business layer.</p>
 * 
 * <p>In this class logger is used explicitly for marking boundaries of each "service" method 
 * call by adding INFO level messages to log.</p>
 * 
 * @author Dimas
 *
 */
@Service(value="userManager")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class UserManager {
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private VacationDAO vacationDAO;
	
	@Autowired
	private RemainingVacationDaysDAO remainingVacationDaysDAO;
	
	@Autowired
	private ServerValidationUtil validationUtil;
	
	@Autowired
	private VacationManager vacationManager;
	
	@Autowired
	private ApprovalManager approvalManager;
	
	/*@InjectLogger
	private Logger logger;*/
	
	/**
	 * This method gets all existed users from DB.
	 * 
	 * @return the list of all {@link USer} entities from DB.
	 */
	public List<User> getAllUsersList() {
		List<User> usersList = userDAO.getAll();
		
		return usersList;
	}

	/**
	 * This method adds new user.
	 * 
	 * @param user - {@link User} entity to be added to DB
	 * @return the {@link User} entity with generated id and version field.
	 * @throws CustomMessageException if error during users object validation occurs or if we get
	 * 				DublicateEntryException and want to inform about this client. 
	 * @throws IllegalArgumentException if input role object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public User saveUser(final User user) throws CustomMessageException {
		if (user == null) {
			throw new IllegalArgumentException("Input user object should not be null!");
		}
		
		validationUtil.validate(user);
		
		/* Validate password separately of all user object because of password encoding. */
		
		String password = user.getPassword();
		String passwordWithoutSpaces = password.replaceAll(" ", "");
		if (password.length() > passwordWithoutSpaces.length()) {
			throw new CustomMessageException("Password should not contain spaces!");
		}
		if ( (password.length() < 8) || (password.length() > 20) ) {
			throw new CustomMessageException("Password should be from 8 to 20 symbols length!");
		}
		
		User savedUser = null;
		try {
			savedUser = userDAO.save(user);
		} catch (DublicateEntryException e) {
			throw new CustomMessageException(e);
		}
		
		return savedUser;
	}

	/**
	 * This method updates existed user.
	 * 
	 * @param user - user to be updated
	 * @throws CustomMessageException if some problem connected with client's input data 
	 * 				validation occurs or if problem with user object version occurs.
	 * @throws IllegalArgumentException if input role object is null
	 * 
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void updateUser(final User user) throws CustomMessageException {
		if (user == null) {
			throw new IllegalArgumentException("Input user object should not be null!");
		}
		
		try {
			validationUtil.validate(user);
			userDAO.update(user);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}

	/**
	 * This method deletes existed user.
	 * 
	 * @param user - {@link User} entity to be deleted
	 * @throws CustomMessageException if some problems occur during deleting and we need
	 * 				to inform user about them.
	 * @throws IllegalArgumentException if input role object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void deleteUser(final User user) throws CustomMessageException {
		if (user == null) {
			throw new IllegalArgumentException("Input user object should not be null!");
		}
		
		try {
			/* Get the list of vacation for user and delete each of them separately. */
			List<Vacation> vacations = vacationManager.getListOfAllVacationsForUser(user);
			for (Vacation vacation: vacations) {
				//vacationManager.deleteVacation(vacation);
				/* First remove approval flow for vacation. */
				approvalManager.deleteApprovalFlow(vacation);

				/* Then remove specified vacation. */
				vacationDAO.delete(vacation);
			}

			/*
			 * Now delete all approval steps for which user's role is used as approver role.
			 * If related to approval step vacation has state JUST_OPENED or WAITING then
			 * before deleting approval step it will have been rejected together with vacation.
			 * All the approval steps of the related vacation is changed to REJECTED.
			 */
			approvalManager.deleteApprovalStepsWithSpecifiedApproverRole(user.getRole());			
			
			/*
			 * Delete approval steps for which specified user defined as approver. If some of 
			 * the ApprovalSteps have ACTIVE state then their corresponding vacations are rejected
			 * before deleting.
			 */
			approvalManager.deleteApprovalStepsWithSpecifiedApprover(user);

			/*
			 * After all we delete specified user from the list of deputies of all vacation.
			 */
			vacationDAO.deleteUserFromAllDeputiesLists(user);

			userDAO.delete(user);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}

	/**
	 * This method fetches users which have specified role.
	 * 
	 * @param role - role by which we want to search user.
	 * @return the list of users which have specified role. 
	 * @throws IllegalArgumentException if input role object is null.
	 */
	public List<User> getUsersByRole(final Role role) {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null!");
		}
	
		List<User> usersList = userDAO.getUsersByRole(role);
				
		return usersList;
	}

	/**
	 * This method gets logged in user's data.
	 * 
	 * @return the user object that corresponds logged in user.
	 * @throws CustomMessageException 
	 */
	public User getLoggedInUser() throws CustomMessageException {
		//Get username from Spring Security
		Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentification == null) {
			// for first time
			return null;
			//throw new UsernameNotFoundException("Cannot get info about logged in user");
		}
		
		Object principals = authentification.getPrincipal();
		
		/*
		 * This problem is necessary because sometimes Spring Security return principal as
		 * "anonymousUser". I can't find the reason but because of this many problems occurs.
		 */
		if (principals.toString().equals("anonymousUser")) {
			throw new CustomMessageException("Problem with loading info about logged in user. " +
					"Please, reload web page.");
		}
		
		org.springframework.security.core.userdetails.User user = 
				(org.springframework.security.core.userdetails.User)principals;

		User loggedInUser = userDAO.getUserByUserName(user.getUsername());
		
		return loggedInUser;
	}
	
	/**
	 * Returns number of users with specified role
	 * 
	 * @param role
	 * @return the number of users with role
	 */
	int getNumberOfUsersWithRole(final Role role){
		return userDAO.getTotalAmountOfUsersWithRole(role);
	}

	/**
	 * This method returns remaining vacation days for specified user.
	 * 
	 * @param userId - user's id.
	 * @return remaining users vacation days for user by its id.
	 */
	public RemainingVacationDays getRemainingVacationDaysForUser(long userId) {
		/* Remaining vacations day's id = user's id */
		RemainingVacationDays remainingVacationDays = remainingVacationDaysDAO.getById(userId);
		
		return remainingVacationDays;
	}
	
	/**
	 * This method updates password for logged in user.
	 * 
	 * @param oldPassword - old password
	 * @param newPassword - new password
	 * @throws CustomMessageException if some problem during updating password occurs and we need
	 * 				to inform user about it.
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void updateUserPassword(final String oldPassword, final String newPassword) throws CustomMessageException {
		User loggedInUser = getLoggedInUser();
		
		if (loggedInUser == null) {
			throw new CustomMessageException("Failed to load logged in user's info. Please, refresh browser!");
		}
		
		if (!userDAO.isPasswordCorrect(loggedInUser.getId(), oldPassword)) {
			throw new CustomMessageException("You have entered wrong old password!");
		}
		
		userDAO.updatePassword(loggedInUser.getId(), newPassword);
	}

}
