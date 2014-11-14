package com.crediteuropebank.vacationsmanager.server.dblogging;

/**
 * 
 * This enum defines names for the CRUD operations and used in logging.
 * 
 * @author DIMAS
 *
 */
public enum OperationName {

	/**
	 * Means that operation creates one or more records in DB.
	 */
	CREATE,
	
	/**
	 * Means that operation updates one or more records in DB.
	 */
	UPDATE,
	
	/**
	 * Means that operation deletes one or more records in DB.
	 */
	DELETE,
	
	/**
	 * Means that operation remove single row from DB (not change it's status).
	 */
	REMOVE
}
