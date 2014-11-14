package com.crediteuropebank.vacationsmanager.server.dao;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.springconfig.TestApplicationConfig;


/**
 * 
 * @author DIMAS
 *
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:war/WEB-INF/applicationContext.xml"})
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class HolidayDaysDAOTest {
	
	@Autowired
	HolidayDaysDAO dao;

	@Test
	public void complexTest() throws StaleObjectStateException {
	
		// Test saving
		HolidayDays nonWorkingDays = new HolidayDays();
		try {
			nonWorkingDays.setStartDate(DateUtil.parseString("2012-07-14"));
			nonWorkingDays.setEndDate(DateUtil.parseString("2012-07-15"));
			nonWorkingDays.setDescription("Test holidays");
		} catch (ParseException e) {} // Won't happen.
		
		HolidayDays savedNonWorkingDaysDAO = dao.save(nonWorkingDays);
		
		HolidayDays fetchedNonWorkingDaysDAO = dao.getById(savedNonWorkingDaysDAO.getId());
		Assert.assertEquals(savedNonWorkingDaysDAO, fetchedNonWorkingDaysDAO);
		
		// Test updating
		try {
			fetchedNonWorkingDaysDAO.setEndDate(DateUtil.parseString("2012-07-16"));
			fetchedNonWorkingDaysDAO.setDescription("Changed description");
		} catch (ParseException e) {} // Won't happen.
		
		dao.update(fetchedNonWorkingDaysDAO);
		
		HolidayDays updatedNonWorkingDays = dao.getById(fetchedNonWorkingDaysDAO.getId());
		Assert.assertEquals(fetchedNonWorkingDaysDAO, updatedNonWorkingDays);
		
		// Reload entity after saving to get right version
		fetchedNonWorkingDaysDAO = dao.getById(fetchedNonWorkingDaysDAO.getId());
		
		// Just to test transaction's rollback
		//int i = 1/0;
				
		// Test deleting
		dao.delete(fetchedNonWorkingDaysDAO);
		HolidayDays deletedNonWorkingDays = dao.getById(fetchedNonWorkingDaysDAO.getId());
		Assert.assertNull(deletedNonWorkingDays);
		
		dao.removeFromDB(fetchedNonWorkingDaysDAO);
	}
	
	@Ignore
	@Test
	@Rollback(value=false)
	public void createTestRecord() {
	
		// Test saving
		HolidayDays nonWorkingDays = new HolidayDays();
		try {
			nonWorkingDays.setStartDate(DateUtil.parseString("2012-06-14"));
			nonWorkingDays.setEndDate(DateUtil.parseString("2012-06-15"));
			nonWorkingDays.setDescription("Test holidays");
		} catch (ParseException e) {} // Won't happen.
		
		dao.save(nonWorkingDays);
	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateException() throws StaleObjectStateException {
		// Test saving
		HolidayDays nonWorkingDays = new HolidayDays();
		try {
			nonWorkingDays.setStartDate(DateUtil.parseString("2012-07-14"));
			nonWorkingDays.setEndDate(DateUtil.parseString("2012-07-15"));
			nonWorkingDays.setDescription("Test holidays");
		} catch (ParseException e) {} // Won't happen.

		HolidayDays savedHolidayDays = dao.save(nonWorkingDays);
		
		savedHolidayDays.setDescription("UPDATED");
		
		dao.update(savedHolidayDays);
		
		// Should cause an error because after updating in database should be grater version.
		dao.update(savedHolidayDays);
	}
	
}
