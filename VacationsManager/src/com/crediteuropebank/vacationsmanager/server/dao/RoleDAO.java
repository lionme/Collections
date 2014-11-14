package com.crediteuropebank.vacationsmanager.server.dao;

import com.crediteuropebank.vacationsmanager.shared.domain.Role;

/**
 * DAO interface which contains methods for operating with {@link Role} domain object.
 *
 * @author DIMAS
 *
 */
public interface RoleDAO extends DAO<Role>{
	
	/**
	 * This method gets amount of child roles.
	 * 
	 * @param parentRoleId - id of the parent role.
	 * @return the amount of child roles that was calculated.
	 */
	int getChildRolesAmount(long parentRoleId);
	
	/**
	 * This method fetches role by its name.
	 * 
	 * @param roleName - the name of the role.
	 * @return the {@link Role} entity which has  specified name.
	 */
	Role getRoleByName(String roleName);
	
}
