package com.crediteuropebank.vacationsmanager.server.dblogging;

/**
 * 
 * This interface determine operation that each enum constant should implement. 
 * Have package visibility to be used only inside this package.
 * 
 * @author DIMAS
 *
 */
interface Condition {

	/**
	 * 
	 * Returns string representation of the operation to use in queries.
	 * 
	 * @return string representation of the operation.
	 */
	String operation();
	
}
