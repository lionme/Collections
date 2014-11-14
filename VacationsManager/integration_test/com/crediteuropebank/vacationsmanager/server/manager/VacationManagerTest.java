package com.crediteuropebank.vacationsmanager.server.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dao.RoleDAO;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.dao.RemainingVacationDaysDAO;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
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
public class VacationManagerTest {
	private static final int START_AMOUNT_OF_TWO_WEEKS_VACATIONS = 1;
	private static final int START_AMOUNT_OF_ONE_WEEKS_VACATIONS = 1;
	private static final BigDecimal START_AMOUNT_OF_DAY_VACATIONS = BigDecimalUtil.newBigDecimal(5);
	
	private static final int USED_TWO_WEEKS_VACATIONS = 0;
	private static final int USED_ONE_WEEK_VACATIONS = 1;
	private static final BigDecimal USED_DAY_VACATIONS = BigDecimalUtil.newBigDecimal(2);
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RemainingVacationDaysDAO userVacationDaysDAO;
	
	@Autowired
	private VacationManager vacationManager;
	
	@Autowired
	private ApprovalManager approvalManager;
	
	@Test
	public void testSaveUpdateDeleteVacation() throws CustomMessageException {
		Role testRole = new Role();
		testRole.setName("TEST_VACATIONS_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		testRole = roleDAO.save(testRole);
		
		User testUser = new User();
		testUser.setUsername("VACATIONS_SUPERVISOR");
		testUser.setPassword("12345678");
		testUser.setFullName("Just simple superviser");
		testUser.seteMail("test@gmail.com");
		testUser.setRole(testRole);
		
		User testDeputy1 = new User();
		testDeputy1.setUsername("VACATIONS_TEST_DEPUTY_1");
		testDeputy1.setPassword("12345678");
		testDeputy1.setFullName("Just simple test deputy");
		testDeputy1.seteMail("test@gmail.com");
		testDeputy1.setRole(testRole);

		User testDeputy2 = new User();
		testDeputy2.setUsername("VACATIONS_TEST_DEPUTY_2");
		testDeputy2.setPassword("12345678");
		testDeputy2.setFullName("Just simple test deputy");
		testDeputy2.seteMail("test@gmail.com");
		testDeputy2.setRole(testRole);
		
		RemainingVacationDays vacationDays1 = new RemainingVacationDays();
		vacationDays1.setTwoWeeksVacations(START_AMOUNT_OF_TWO_WEEKS_VACATIONS);
		vacationDays1.setOneWeekVacations(START_AMOUNT_OF_ONE_WEEKS_VACATIONS);
		vacationDays1.setDayVacations(START_AMOUNT_OF_DAY_VACATIONS);

		// Create user for test
		testUser.setVacationDays(vacationDays1);
		testUser = userDAO.save(testUser);	
		
		RemainingVacationDays vacationDays2 = new RemainingVacationDays(vacationDays1);
		
		// Create first deputy for test
		testDeputy1.setVacationDays(vacationDays2);
		testDeputy1 = userDAO.save(testDeputy1);
		
		RemainingVacationDays vacationDays3 = new RemainingVacationDays(vacationDays1);
		
		testDeputy2.setVacationDays(vacationDays3);
		testDeputy2 = userDAO.save(testDeputy2);

		// Test saving of the vacation.
		Vacation testVacation = new Vacation();
		testVacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		testVacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		testVacation.setState(VacationState.JUST_OPENED);
		testVacation.setUser(testUser);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(testDeputy1);
		deputies.add(testDeputy2);
		
		testVacation.setDeputies(deputies);
		
		UsedVacationDays usedVacationDays = new UsedVacationDays();
		usedVacationDays.setTwoWeeksVacations(USED_TWO_WEEKS_VACATIONS);
		usedVacationDays.setOneWeekVacations(USED_ONE_WEEK_VACATIONS);
		usedVacationDays.setDayVacations(USED_DAY_VACATIONS);
		
		testVacation.setUsedVacationDays(usedVacationDays);
		
		// Test saving vacation
		Vacation savedVacation = vacationManager.saveVacation(testVacation);
		
		// Check saved vacation
		Vacation fetchedVacation = vacationManager.getVacationById(testVacation.getId());
		Assert.assertEquals(savedVacation, fetchedVacation);
		
		
		User user = userDAO.getById(testUser.getId());
		
		/* Check that we withdraw necessary amount of vacation days from user */
		int expectedTwoWeeksVacations = START_AMOUNT_OF_TWO_WEEKS_VACATIONS - USED_TWO_WEEKS_VACATIONS;
		int expectedOneWeekvacations = START_AMOUNT_OF_ONE_WEEKS_VACATIONS - USED_ONE_WEEK_VACATIONS;
		BigDecimal expectedDayVacations = START_AMOUNT_OF_DAY_VACATIONS.subtract(USED_DAY_VACATIONS);
		
		Assert.assertEquals(expectedTwoWeeksVacations, user.getVacationDays().getTwoWeeksVacations());
		Assert.assertEquals(expectedOneWeekvacations, user.getVacationDays().getOneWeekVacations());
		Assert.assertEquals(expectedDayVacations, user.getVacationDays().getDayVacations());
		
		List<ApprovalStep> approvalSteps = approvalManager.getAllApprovalStepsForVacation(fetchedVacation);
		
		// should be 2 approval steps for 2 deputies (for test role there is no parent roles).
		Assert.assertEquals(2, approvalSteps.size());		
		
		// Test deleting vacation
		vacationManager.deleteVacation(savedVacation);
		
		// Deduct days that is used for vacation from remaining vacation days for test user.
		testVacation.getUser().getVacationDays().deduct(usedVacationDays);
		
		Vacation deletedVacation = vacationManager.getVacationById(testVacation.getId());
		
		Assert.assertNull(deletedVacation);
		
		approvalSteps = approvalManager.getAllApprovalStepsForVacation(fetchedVacation);
		
		// should be empty list
		Assert.assertEquals(0, approvalSteps.size());
		
		/* Check that we added vacation days used for deleted vacation to remaining vacation days for user */
		VacationDays remainingVacationDays = userVacationDaysDAO.getById(testUser.getId());
		Assert.assertEquals(1, remainingVacationDays.getTwoWeeksVacations());
		Assert.assertEquals(1, remainingVacationDays.getOneWeekVacations());
		Assert.assertEquals(START_AMOUNT_OF_DAY_VACATIONS, remainingVacationDays.getDayVacations());
	
	}
	
}
