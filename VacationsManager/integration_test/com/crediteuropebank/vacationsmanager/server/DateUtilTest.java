package com.crediteuropebank.vacationsmanager.server;

import java.text.ParseException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * Simple test class for testing {@link DateUtil} class.
 * 
 * @author dimas
 *
 */
public class DateUtilTest {
	
	@Test
	public void testCalculateNumberOfWeekendsInRange() throws ParseException {
		Date startDate = DateUtil.parseString("2012-05-06");
		Date endDate = DateUtil.parseString("2012-05-19");
		
		int weekendDays = DateUtil.calculateNumberOfWeekendsInRange(startDate, endDate);
		
		Assert.assertEquals(4, weekendDays);
	}
}
