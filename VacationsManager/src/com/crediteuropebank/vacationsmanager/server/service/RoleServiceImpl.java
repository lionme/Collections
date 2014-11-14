package com.crediteuropebank.vacationsmanager.server.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.service.RoleService;
import com.crediteuropebank.vacationsmanager.server.manager.RoleManager;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class corresponds the server side implementation of the RPC for 
 * {@link RoleService} interface.
 * It just transfer the calls to the {@link RoleManager} which represents the real 
 * "service" layer of the application.
 * 
 * @author dimas
 *
 */
public class RoleServiceImpl implements RoleService {
	
	// Should be automatically wired by type
	private RoleManager userManager;
	
	public RoleManager getUserManager() {
		return userManager;
	}

	public void setUserManager(RoleManager userManager) {
		this.userManager = userManager;
	}

	/**
	 * Default constructor.
	 */
	public RoleServiceImpl() {
		
	}
	
	@Override
	public List<Role> getAllRoles() {
		return userManager.getAllRoles();
	}

	@Override
	public Role saveRole(Role role) throws CustomMessageException {
		return userManager.saveRole(role);
	}

	@Override
	public void updateRole(Role role) throws CustomMessageException {
		userManager.updateRole(role);
	}

	@Override
	public void deleteRole(Role role) throws CustomMessageException {
		userManager.deleteRole(role);
	}

}
