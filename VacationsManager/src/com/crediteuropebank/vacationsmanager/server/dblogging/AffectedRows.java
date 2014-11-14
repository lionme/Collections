package com.crediteuropebank.vacationsmanager.server.dblogging;

/**
 * 
 * This enum specifies number of affected by operation rows. It's used for DB logging purposes.
 * 
 * @author DIMAS
 *
 */
public enum AffectedRows {

	/**
	 * Specifies that operation affects one row.
	 */
	ONE,
	
	/**
	 * Specifies that operation affects many rows.
	 */
	MANY
	
}
