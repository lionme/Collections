package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author dimas
 *
 */
public interface RoleServiceAsync {

	/**
	 * This method fetches the list of all roles from DB.
	 * 
	 * @param callback
	 */
	void getAllRoles(AsyncCallback<List<Role>> callback);

	/**
	 * This method saves new role record to the DB.
	 * 
	 * @param role - entity to be saved
	 * @param callback
	 */
	void saveRole(Role role, AsyncCallback<Role> callback);

	/**
	 * This method updates existed role record in DB.
	 * 
	 * @param role - entity to be updated.
	 * @param callback
	 */
	void updateRole(Role role, AsyncCallback<Void> callback);

	/**
	 * This method deletes existed role record from DB.
	 * 
	 * @param role - entity to be deleted.
	 * @param callback
	 */
	void deleteRole(Role role, AsyncCallback<Void> callback);

}
