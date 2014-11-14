package com.crediteuropebank.vacationsmanager.server.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dao.ApprovalStepDAO;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
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
public class UserManagerTest {
	
	private static final int TWO_WEEKS_VACATIONS_DEFAULT_AMOUNT = 1;
	private static final int ONE_WEEK_VACATIONS_DEFAULT_AMOUNT = 1;
	private static final BigDecimal DAY_VACATIONS_DEFAULT_AMOUNT = BigDecimalUtil.newBigDecimal(5);
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private ApprovalStepDAO approvalStepDAO;
	
	@Autowired
	private RoleManager roleManager;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private VacationManager vacationManager;
	
	@Autowired
	private ApprovalManager approvalManager;
	
	private Role parentRole;
	
	private User parentUser;
	
	private Vacation parentUserVacation;
	
	private Vacation vacation;
	
	private User testDeputy1;
	
	@Before
	public void setUp() throws CustomMessageException {
		/* Prepare test data. */		
		parentRole = new Role();
		parentRole.setName("TEST_PARENT_ROLE");
		parentRole.setDesription("Test parent role");
		parentRole.setPrivilege(Privilege.DEFAULT);
		parentRole = roleManager.saveRole(parentRole);
		
		parentUser = new User();
		parentUser.setUsername("SIMPLE_USER");
		parentUser.setPassword("12345678");
		parentUser.setFullName("Just simple user");
		parentUser.seteMail("simple@gmail.com");
		parentUser.setRole(parentRole);
		
		RemainingVacationDays vacDays = new RemainingVacationDays();
		vacDays.setTwoWeeksVacations(TWO_WEEKS_VACATIONS_DEFAULT_AMOUNT);
		vacDays.setOneWeekVacations(ONE_WEEK_VACATIONS_DEFAULT_AMOUNT);
		vacDays.setDayVacations(DAY_VACATIONS_DEFAULT_AMOUNT);
		
		parentUser.setVacationDays(vacDays);
		
		parentUser = userManager.saveUser(parentUser);
		
		/**/
		
		Role testRole = new Role();
		testRole.setName("TEST_VACATIONS_ROLE");
		testRole.setDesription("test role");
		testRole.setParentRole(parentRole);
		testRole.setPrivilege(Privilege.DEFAULT);
		
		testRole = roleManager.saveRole(testRole);
		
		User testUser = new User();
		testUser.setUsername("TEST_VACATIONS_SUPERVISOR");
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

		User testDeputy2 = new User();
		testDeputy2.setUsername("VACATIONS_TEST_DEPUTY_2");
		testDeputy2.setPassword("12345678");
		testDeputy2.setFullName("Just simple test deputy");
		testDeputy2.seteMail("test@gmail.com");
		testDeputy2.setRole(testRole);
		
		RemainingVacationDays vacationDays1 = new RemainingVacationDays();
		vacationDays1.setTwoWeeksVacations(TWO_WEEKS_VACATIONS_DEFAULT_AMOUNT);
		vacationDays1.setOneWeekVacations(ONE_WEEK_VACATIONS_DEFAULT_AMOUNT);
		vacationDays1.setDayVacations(DAY_VACATIONS_DEFAULT_AMOUNT);

		// Create user for test
		testUser.setVacationDays(vacationDays1);
		testUser = userManager.saveUser(testUser);	
		
		RemainingVacationDays vacationDays2 = new RemainingVacationDays(vacationDays1);
		
		// Create first deputy for test
		testDeputy1.setVacationDays(vacationDays2);
		testDeputy1 = userManager.saveUser(testDeputy1);
		
		RemainingVacationDays vacationDays3 = new RemainingVacationDays(vacationDays1);
		
		testDeputy2.setVacationDays(vacationDays3);
		testDeputy2 = userManager.saveUser(testDeputy2);

		// Create and save vacation for test child user.
		vacation = new Vacation();
		vacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setState(VacationState.JUST_OPENED);
		vacation.setUser(testUser);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(testDeputy1);
		deputies.add(testDeputy2);
		
		vacation.setDeputies(deputies);
		
		UsedVacationDays usedVacationDays = new UsedVacationDays();
		usedVacationDays.setTwoWeeksVacations(0);
		usedVacationDays.setOneWeekVacations(1);
		usedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(2));
		
		vacation.setUsedVacationDays(usedVacationDays);
		
		vacation = vacationManager.saveVacation(vacation);
		
		// Create and save vacation for test parent user.		
		parentUserVacation = new Vacation();
		parentUserVacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		parentUserVacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		parentUserVacation.setState(VacationState.JUST_OPENED);
		parentUserVacation.setUser(parentUser);
		List<User> deputiesForParent = new ArrayList<User>();
		deputiesForParent.add(testDeputy1);
		parentUserVacation.setDeputies(deputiesForParent);
		
		UsedVacationDays parentUsedVacationDays = new UsedVacationDays();
		parentUsedVacationDays.setTwoWeeksVacations(0);
		parentUsedVacationDays.setOneWeekVacations(0);
		parentUsedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(3));
		
		parentUserVacation.setUsedVacationDays(parentUsedVacationDays);
		parentUserVacation = vacationManager.saveVacation(parentUserVacation);
		
		int numberOfApprovalSteps = approvalManager.getNumberOfApprovalStepsWithApproverRole(parentRole);
		Assert.assertEquals("wrong number of approval steps with specified approval role!", 
				1,
				numberOfApprovalSteps);
		
		List<ApprovalStep> approvalSteps = approvalStepDAO.getAllApprovalStepsWithSpecifiedApproverRole(parentRole);
		Assert.assertEquals("List of approval steps should contain one item!", 1, approvalSteps.size());

	}
	
	/**
	 * In this test case we test how is working deleting of the user and approval steps for 
	 * which user's role is approver role.
	 * 
	 * @throws CustomMessageException
	 */
	@Test
	public void testUserDeleting_FirstCase() throws CustomMessageException {
		
		Assert.assertNotNull(parentUser);
		Assert.assertNotNull(parentUserVacation);
		Assert.assertNotNull(vacation);
		Assert.assertNotNull(parentRole);
				
		/* Update data for parent user. */
		parentUser = userDAO.getById(parentUser.getId());
		
		userManager.deleteUser(parentUser);
		
		User fetchedUser = userDAO.getById(parentUser.getId());
		
		Assert.assertNull("Fetched user should be null because it was deleted!", fetchedUser);
		
		Vacation fetchedVacation = vacationManager.getVacationById(parentUserVacation.getId());
		
		Assert.assertNull("Vacation should be null because it was deleted!", fetchedVacation);
		
		fetchedVacation = vacationManager.getVacationById(vacation.getId());
		
		Assert.assertEquals("Vacation's state should be REJETED!", 
				VacationState.REJECTED, fetchedVacation.getState());
		
		int numberOfApprovalSteps = approvalManager.getNumberOfApprovalStepsWithApproverRole(parentRole);
		Assert.assertEquals("wrong number of approval steps with specified approval role!", 
				0,
				numberOfApprovalSteps);
	}
	
	@Test
	public void testUserDeleting_SecondCase() throws CustomMessageException {
		
		Assert.assertNotNull(parentUser);
		Assert.assertNotNull(parentUserVacation);
		Assert.assertNotNull(vacation);
		Assert.assertNotNull(parentRole);
		Assert.assertNotNull(testDeputy1);
		
		testDeputy1 = userDAO.getById(testDeputy1.getId());
		
		userManager.deleteUser(testDeputy1);
		
		Vacation fetchedVacation = vacationManager.getVacationById(parentUserVacation.getId());
		Assert.assertEquals("Vacation's state should be REJETED!", 
				VacationState.REJECTED, fetchedVacation.getState());
		
		fetchedVacation = vacationManager.getVacationById(vacation.getId());
		Assert.assertEquals("Vacation's state should be REJETED!", 
				VacationState.REJECTED, fetchedVacation.getState());

		int numberOfApprovalSteps = approvalManager.getActiveApprovalsForApprover(testDeputy1).size();
		Assert.assertEquals("wrong number of approval steps with specified approval role!", 
				0,
				numberOfApprovalSteps);
	}
	
}
