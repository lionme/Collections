package com.crediteuropebank.vacationsmanager.server.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.client.service.RoleService;
import com.crediteuropebank.vacationsmanager.server.dao.DublicateEntryException;
import com.crediteuropebank.vacationsmanager.server.dao.RoleDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.validation.ServerValidationUtil;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * <p>In this "manager" class starts real business logic for {@link Role}. {@link RoleService} is 
 * used only to deliver requests from user to business layer.</p>
 * 
 * <p>In this class logger is used explicitly for marking boundaries of each real "service" method call by adding
 * INFO level messages to log.</p>
 * 
 * @author Dimas
 *
 */
@Service(value="roleManager")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class RoleManager {

	@Autowired(required=true)
	private RoleDAO roleDAO;

	@Autowired(required=true)
	private UserDAO userDAO;
	
	@Autowired(required=true)
	private ApprovalManager approvalManager;
	
	@Autowired(required=true)
	private ServerValidationUtil validationUtil;

	/**
	 * This method gets all roles from DB.
	 * 
	 * @return the list of all {@link Role} entities from DB.
	 */
	public List<Role> getAllRoles() {

		return roleDAO.getAll();
	}

	/**
	 * This method saves new role.
	 * 
	 * @param role - {@link Role} entity to be saved.
	 * @return {@link Role} entity with automatically generated id and version.
	 * @throws CustomMessageException if some problems occur during role saving and we want
	 * 				to inform user about them.
	 * @throws IllegalArgumentException if input role object is null.
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public Role saveRole(final Role role) throws CustomMessageException {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null!");
		}

		validationUtil.validate(role);
		
		Role savedRole = null;
		try {
			savedRole = roleDAO.save(role);
		} catch (DublicateEntryException e) {
			throw new CustomMessageException(e);
		}

		return savedRole;
	}

	/**
	 * This method updates existed role.
	 * 
	 * @param role - {@link Role} entity to be updated.
	 * @throws CustomMessageException if some problems occur during role saving and we want
	 * 				to inform user about them.
	 * @throws IllegalArgumentException if input role object is null.
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void updateRole(final Role role) throws CustomMessageException {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null!");
		}

		validationUtil.validate(role);
		
		try {
			roleDAO.update(role);
		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}

	/**
	 * <p>This method deletes existed role.</p>
	 * 
	 * <p>Logic:</p>
	 * <p>The main problem during role deleting is that role id is used in some table as foreign key. 
	 * That’s why before deleting role I checked such things:</p>
	 * <ol>
	 *   <li>Does role has child roles? If yes – throw an exception.</li>
	 *   <li>Is there at least one user with given role? If yes – throw an exception.</li>
	 * </ol>
	 * <p>Also before deleting role we need to delete all approval steps with specified approver 
	 * role that have ApprovalStepState WAITING or ACTIVE.</p>
	 * 
	 * @param role - the {@link Role} entity that should be deleted.
	 * @throws CustomMessageException if some of the checks fails or problems during entity
	 * 				deleting occur and we want to inform user about them.
	 * @throws IllegalArgumentException if input role object is null.
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void deleteRole(final Role role) throws CustomMessageException {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null!");
		}

		try {
			
			/*
			 * Check that role doesn't have child roles.
			 */
			
			int childRolesAmount = roleDAO.getChildRolesAmount(role.getId());

			if(childRolesAmount > 0) {
				String message = "You can not delete role with id=" + role.getId() + 
						"This action violates constrains because specified role has child roles." +
						"Please, first change or delete child roles.";

				throw new CustomMessageException(message);
			}
			
			/*
			 * Check that users with specified role don't exist.
			 */

			List<User> usersWithRole = userDAO.getUsersByRole(role);

			if (usersWithRole.size() > 0) {
				String message = "You can not delete role with id=" + role.getId() + 
						"This action violates constrains because users with specified role exist." +
						"Please, first change or delete this users.";

				throw new CustomMessageException(message);
			}
			
			/* 
			 * Delete all approval steps with specified role that may 
			 * affect on further work of program. 
			 * Situation when such approach is necessary is impossible, so may be it
			 * will be better just to add check and throw an exception?
			 */
			//approvalManager.deleteApprovalStepsWithSpecifiedApproverRole(role);
			if (approvalManager.getNumberOfApprovalStepsWithApproverRole(role) > 0) {
				throw new IllegalStateException("At such stage there should not be approval steps " +
						"with role that is deleted.");
			}

			roleDAO.delete(role);

		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method fetches role by its id.
	 * 
	 * @param id - id of the role.
	 * @return the {@link Role} object fetched by id.
	 */
	public Role getRoleById(long id) {
		
		return roleDAO.getById(id);
	}
}
