package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author dimas
 *
 */
@RemoteServiceRelativePath("rpc/role")
public interface RoleService extends RemoteService {

	/**
	 * This method fetches the list of all roles from DB.
	 * 
	 * @return the list of all {@link Role}} entities from DB.
	 */
	List<Role> getAllRoles();

	/**
	 * This method saves new role record to the DB.
	 * 
	 * @param role - entity to be saved
	 * @return saved {@link Role}} entity with generated id.
	 * @throws CustomMessageException if some problem occur during saving and we need to
	 * 				inform user about it.
	 */
	Role saveRole(Role role) throws CustomMessageException;

	/**
	 * This method updates existed role record in DB.
	 * 
	 * @param role - entity to be updated.
	 * @throws CustomMessageException if some problem occur during saving and we need to
	 * 				inform user about it.
	 */
	void updateRole(Role role) throws CustomMessageException;

	/**
	 * This method deletes existed role record from DB.
	 * 
	 * @param role - entity to be deleted.
	 * @throws CustomMessageException if some problem occur during saving and we need to
	 * 				inform user about it.
	 */
	void deleteRole(Role role) throws CustomMessageException;

}
