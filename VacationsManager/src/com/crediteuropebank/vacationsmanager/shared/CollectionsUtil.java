package com.crediteuropebank.vacationsmanager.shared;

import java.util.Map;
import java.util.Map.Entry;

/**
 * This class contains different static utility methods.
 * 
 * @author dimas
 *
 */
public class CollectionsUtil {
	
	/**
	 * This is utility method which give you a possibility to get key from Map by value. (Note that 
	 * relation between keys and values should be 1*1)
	 * 
	 * @param map - the map where you want to search.
	 * @param value - the value by which you want to search.
	 * @return the key from the map
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
}
