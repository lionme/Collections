package com.crediteuropebank.vacationsmanager.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This utility class contains a set of static methods that simplify work
 * with {@link BigDecimal} class.
 * 
 * Use only this class for creating new big decimal. In other case you can get 
 * problem with scales (especially when you will use equals method).
 * 
 * 
 * @author dimas
 *
 */
public class BigDecimalUtil {
	
	/**
	 * This constant holds the number of the digits at the right part after dot.
	 */
	public static final int SCALE = 1;
	
	/**
	 * Creates new {@link BigDecimal} instance with standard for application scale and 
	 * rounding mode.
	 * 
	 * @param value - value of the object.
	 * @return new {@link BigDecimal} that corresponds specified value.
	 */
	public static BigDecimal newBigDecimal(double value) {
		BigDecimal instance = new BigDecimal(value);
		instance = instance.setScale(SCALE, RoundingMode.HALF_UP);
		
		return instance;
	}
	
	/**
	 * Creates new {@link BigDecimal} instance with standard for application scale and 
	 * rounding mode.
	 * 
	 * @param value - value of the object.
	 * @return new {@link BigDecimal} that corresponds specified value.
	 */
	public static BigDecimal newBigDecimal(int value) {
		BigDecimal instance = new BigDecimal(value);
		instance = instance.setScale(SCALE, RoundingMode.HALF_UP);
		
		return instance;
	}
}
