package com.crediteuropebank.vacationsmanager.server.dao;

import org.springframework.jdbc.support.KeyHolder;

/**
 * 
 * This interface is used for injecting {@link KeyHolder} instance to the necessary bean.
 * 
 * @author DIMAS
 *
 */
public interface KeyHolderFactory {

	/**
	 * This is factory method for creating new {@link KeyHolder}.
	 * 
	 * @return the new {@link KeyHolder} instance.
	 */
	KeyHolder newKeyHolder();
	
}
