package com.crediteuropebank.vacationsmanager.server.dblogging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * <p>This annotation is used for marking fields of the domain object that should be logged into log table.
 * As argument this annotation takes name of the column in which value of the field should be written.</p>
 * 
 * <p>This annotation is used for logging purposes.</p>
 * 
 * @author dimas
 * 
 */
@Target(value=ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Column {
	
	/**
	 * The name of the column in log table.
	 */
	String columnName();
}
