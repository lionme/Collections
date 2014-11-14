package com.crediteuropebank.vacationsmanager.server.dblogging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used for marking domain objects (entities) DB operations (create, update, delete) under which
 * should be logged in the log table.</p>
 * 
 * <p>This annotation is used for logging.</p>
 * 
 * @author DIMAS
 *
 */
@Target(value=ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Domain {
	
	/**
	 * The name of the table where domain object is saved.
	 */
	String tableName(); 
	
	/**
	 * The name of the log table for this domain object.
	 */
	String logTableName();
}
