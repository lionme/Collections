package com.crediteuropebank.vacationsmanager.server.dao;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

/**
 * 
 * Simple implementation of the {@link KeyHolderFactory}.
 * 
 * @author DIMAS
 *
 */
@Component
public class KeyHolderFactoryImpl implements KeyHolderFactory {

	@Override
	public KeyHolder newKeyHolder() {		
		return new GeneratedKeyHolder();
	}

}
