package com.crediteuropebank.vacationsmanager.server.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.crediteuropebank.vacationsmanager.springconfig.TestApplicationConfig;

/**
 * 
 * @author dimas
 *
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class VacationDAOTest {
	
	private static final BigDecimal DAY_VACATIONS_AMOUNT = BigDecimalUtil.newBigDecimal(5);
	
	@Autowired
	UserDAO userDAO;
	
	@Autowired
	RoleDAO roleDAO;
	
	@Autowired
	VacationDAO vacationDAO;
	
	@Autowired
	ApprovalStepDAO approvalStepDAO;
	
	private Role testRole;
	
	private User testUser;
	
	private User testDeputy1;
	
	private User testDeputy2;
	
	private Vacation testVacation;

	@Before
	public void setUp() {
		testRole = new Role();
		testRole.setName("TEST_VACATIONS_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		testRole = roleDAO.save(testRole);
				
		// Test saving of the user.
		
		testUser = new User();
		testUser.setUsername("VACATIONS_SUPERVISOR");
		testUser.setPassword("12345678");
		testUser.setFullName("Just simple superviser");
		testUser.seteMail("test@gmail.com");
		testUser.setRole(testRole);
		
		testDeputy1 = new User();
		testDeputy1.setUsername("VACATIONS_TEST_DEPUTY_1");
		testDeputy1.setPassword("12345678");
		testDeputy1.setFullName("Just simple test deputy");
		testDeputy1.seteMail("test@gmail.com");
		testDeputy1.setRole(testRole);

		testDeputy2 = new User();
		testDeputy2.setUsername("VACATIONS_TEST_DEPUTY_2");
		testDeputy2.setPassword("12345678");
		testDeputy2.setFullName("Just simple test deputy");
		testDeputy2.seteMail("test@gmail.com");
		testDeputy2.setRole(testRole);
		
		RemainingVacationDays vacationDays1 = new RemainingVacationDays();
		vacationDays1.setTwoWeeksVacations(1);
		vacationDays1.setOneWeekVacations(1);
		vacationDays1.setDayVacations(DAY_VACATIONS_AMOUNT);

		// Create user for test
		testUser.setVacationDays(vacationDays1);
		testUser = userDAO.save(testUser);	
		
		RemainingVacationDays vacationDays2 = new RemainingVacationDays(vacationDays1);
		
		// Create first deputy for test
		testDeputy1.setVacationDays(vacationDays2);
		testDeputy1 = userDAO.save(testDeputy1);
		
		RemainingVacationDays vacationDays3 = new RemainingVacationDays(vacationDays2);
		
		testDeputy2.setVacationDays(vacationDays3);
		testDeputy2 = userDAO.save(testDeputy2);

		// Test saving of the vacation.
		testVacation = new Vacation();
		testVacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		testVacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		testVacation.setState(VacationState.JUST_OPENED);
		testVacation.setUser(testUser);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(testDeputy1);
		deputies.add(testDeputy2);
		
		testVacation.setDeputies(deputies);
		
		UsedVacationDays usedVacationDays = new UsedVacationDays();
		usedVacationDays.setTwoWeeksVacations(0);
		usedVacationDays.setOneWeekVacations(1);
		usedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(2));
		
		testVacation.setUsedVacationDays(usedVacationDays);

		testVacation = vacationDAO.save(testVacation);
	}
	
	/**
	 * I wrote one method for all tests again. This is bed approach, but I don't have enough time to write 
	 * detail tests. I need to this in future.
	 * @throws CustomMessageException 
	 */
	@Test
	public void testWholeJdbcVacationDAOClass() throws StaleObjectStateException {
		// Check init data
		Assert.assertNotNull(testRole);
		Assert.assertNotNull(testUser);
		Assert.assertNotNull(testDeputy1);
		Assert.assertNotNull(testDeputy2);
		Assert.assertNotNull(testVacation);
		
		// Test fetching vacation by id

		Vacation fetchedVacation = vacationDAO.getById(testVacation.getId());
		Assert.assertEquals(testVacation, fetchedVacation);

		// Test updating of the vacation.

		fetchedVacation.setState(VacationState.APPROVED);
		vacationDAO.update(fetchedVacation);

		Vacation updatedVacation = vacationDAO.getById(fetchedVacation.getId());
		Assert.assertEquals(fetchedVacation, updatedVacation);

		// Test fetching all vacations.

		List<Vacation> vacations = vacationDAO.getAll();
		Assert.assertTrue(vacations.contains(updatedVacation));

		// Test deleting vacation

		vacationDAO.delete(updatedVacation);
		List<Vacation> vacationsThatLeft = vacationDAO.getAll();
		Assert.assertTrue(!vacationsThatLeft.contains(updatedVacation));
		
		vacationDAO.removeFromDB(updatedVacation);

		// Delete other test data
		userDAO.removeFromDB(testUser);
		userDAO.removeFromDB(testDeputy1);
		userDAO.removeFromDB(testDeputy2);
		
		roleDAO.removeFromDB(testRole);

	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateException() throws StaleObjectStateException {
		Assert.assertNotNull(testVacation);
		
		Vacation vacation = vacationDAO.getById(testVacation.getId());
		
		vacation.setEndDate(testVacation.getEndDate());
		
		vacationDAO.update(vacation);
		
		// Here exception should be thrown
		vacationDAO.update(vacation);
	}
	
	@Test
	public void testVacationUpdating() throws StaleObjectStateException {
		Assert.assertNotNull(testVacation);
		
		testVacation.setEndDate(DateUtil.addDays(DateUtil.getCurrentDateWithoutTime(), 4));
		
		List<User> deputies = testVacation.getDeputies();
		deputies.remove(testDeputy1);
		
		User newDeputy = new User();
		newDeputy.setUsername("sdfgh");
		newDeputy.setPassword("12345678");
		newDeputy.setFullName("asdasdasda");
		newDeputy.seteMail("test@gmail.com");
		newDeputy.setRole(testRole);
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(DAY_VACATIONS_AMOUNT);
		
		newDeputy.setVacationDays(vacationDays);
		
		newDeputy = userDAO.save(newDeputy);
		
		deputies.add(newDeputy);
		
		testVacation.setDeputies(deputies);
		
		vacationDAO.update(testVacation);
		
		//approvalStepDAO.getById(testVacation.getId());
		Vacation fetchedVacation  = vacationDAO.getById(testVacation.getId());
		List<User> savedDeputies = fetchedVacation.getDeputies();
		
		Assert.assertTrue("Deputies list in updated vacation doesn't contain new deputy.", savedDeputies.contains(newDeputy));
		Assert.assertFalse("Deputies list in updated vacation contains old deputy that have been removed", savedDeputies.contains(testDeputy1));
	}

	/**
	 * Don't need this test method any more.
	 */
	@Test
	@Ignore
	public void simpleVacationSaving() {
		User user = userDAO.getUserByUserName("developer");
		User deputy = userDAO.getUserByUserName("supervisor");

		Vacation vacation = new Vacation();
		vacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setState(VacationState.JUST_OPENED);
		vacation.setUser(user);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(deputy);
		vacation.setDeputies(deputies);

		vacationDAO.save(vacation);
	}
}
