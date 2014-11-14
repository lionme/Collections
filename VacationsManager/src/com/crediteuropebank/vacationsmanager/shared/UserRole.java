package com.crediteuropebank.vacationsmanager.shared;

/**
 * This enum contains the list of possible user roles.
 * 
 * Is not used now.
 * 
 * @author dimas
 * @deprecated use domain Role object instead
 *
 */
@Deprecated
public enum UserRole {
	
	/**
	 * Almost the same as header role, but this user can also add new users.
	 */
	ADMIN,
	
	/**
	 *  Header role. 
	 */
	HEADER,
	
	/**
	 *  Supervisor role. 
	 */
	SUPERVISOR,
	
	/**
	 *  The usual hard working developer. ) 
	 */
	DEVELOPER
}
