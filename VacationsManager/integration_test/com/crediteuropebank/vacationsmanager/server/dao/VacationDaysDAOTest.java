package com.crediteuropebank.vacationsmanager.server.dao;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * 
 * @author DIMAS
 * 
 * @deprecated - test VacationDaysDAO with UserDAO and VacationDAO.
 */
@Deprecated
public class VacationDaysDAOTest {
	
	/**
	 * This method couldn't be run because of DB constrains
	 * @throws CustomMessageException 
	 */
	@Test
	@Ignore
	public void simpleComplexTestOfJdbcVacationDaysDAO() throws StaleObjectStateException {
		long idForTest = 777L;
		
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		RemainingVacationDaysDAO vacationDaysDAO = (RemainingVacationDaysDAO) context.getBean("jdbcVacationDaysDAO");
		
		// Test saving
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		//vacationDays.setId(idForTest);
		DaoUtil.setObjectId(vacationDays, idForTest);
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(5));
		
		vacationDaysDAO.save(vacationDays);
		
		// Test fetching
		RemainingVacationDays fetchedVacationDays = vacationDaysDAO.getById(idForTest);
		Assert.assertEquals(vacationDays, fetchedVacationDays);
		
		// Test updating
		
		fetchedVacationDays.setTwoWeeksVacations(2);
		fetchedVacationDays.setOneWeekVacations(2);
		vacationDaysDAO.update(fetchedVacationDays);
		
		RemainingVacationDays updatedVacationDays = vacationDaysDAO.getById(idForTest);
		Assert.assertEquals(fetchedVacationDays, updatedVacationDays);
				
		// Test deleting
		
		vacationDaysDAO.delete(updatedVacationDays);
		VacationDays deletedVacationDays = vacationDaysDAO.getById(idForTest);
		Assert.assertNull(deletedVacationDays);
		
		vacationDaysDAO.removeFromDB(updatedVacationDays);
	}
	
}
