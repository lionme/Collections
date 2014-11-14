package com.crediteuropebank.vacationsmanager.server.dao;

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
import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
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
 * @author DIMAS
 *
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:war/WEB-INF/applicationContext.xml"})
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class ApprovalStepDAOTest {
	
	private static final BigDecimal DAY_VACATIONS_AMOUNT = BigDecimalUtil.newBigDecimal(5);
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private VacationDAO vacationDAO;
	
	@Autowired
	private ApprovalStepDAO approvalStepDAO;
	
	private Role testRole;
	
	private Role testParentRole;
	
	private User testUser;
	
	private User testDeputy1;
	
	private User testDeputy2;
	
	private Vacation testVacation;
	
	private ApprovalStep testApprovalStep;
	
	@Before
	public void setUp() {		
		testParentRole = new Role();
		testParentRole.setName("TEST_PARENT_ROOLE");
		testParentRole.setDesription("Parent role for testing");
		testParentRole.setPrivilege(Privilege.ADMIN);
		testParentRole = roleDAO.save(testParentRole);
		
		testRole = new Role();
		testRole.setName("TEST_VACATIONS_ROLE");
		testRole.setDesription("test role");
		testRole.setParentRole(testParentRole);
		testRole.setPrivilege(Privilege.DEFAULT);
		
		testRole = roleDAO.save(testRole);
		
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

		vacationDAO.save(testVacation);
		
		testApprovalStep = new ApprovalStep();
		//testApprovalStep.setApprover(testDeputy1);
		testApprovalStep.setApproverRole(testParentRole);
		testApprovalStep.setState(ApprovalStepState.ACTIVE);
		testApprovalStep.setVacation(testVacation);
		testApprovalStep.setRowNumber(1);
		//savedApprovalStep.setComments("");
		
		/* test saving operation */
		testApprovalStep = approvalStepDAO.save(testApprovalStep);
	}

	/**
	 * I wrote one method for all tests again. This is bed approach, but I don't have enough time to write 
	 * detail tests. I need to this in future.
	 * @throws CustomMessageException 
	 */
	@Test
	public void testWholeJdbcApprovalStepDAOClass() throws StaleObjectStateException {
		Assert.assertNotNull(testRole);
		//Assert.assertNotNull(testParentRole);
		Assert.assertNotNull(testUser);
		Assert.assertNotNull(testDeputy1);
		Assert.assertNotNull(testDeputy2);
		Assert.assertNotNull(testVacation);
		Assert.assertNotNull(testApprovalStep);	
		
		// Test fetching vacation by id

		ApprovalStep fetchedApprovalStep = approvalStepDAO.getById(testApprovalStep.getId());
		Assert.assertEquals(testApprovalStep, fetchedApprovalStep);

		// Test updating of the vacation.

		fetchedApprovalStep.setState(ApprovalStepState.APPROVED);
		fetchedApprovalStep.setComments("Test comments");
		approvalStepDAO.update(fetchedApprovalStep);

		ApprovalStep updatedApprovalStep = approvalStepDAO.getById(fetchedApprovalStep.getId());
		Assert.assertEquals(fetchedApprovalStep, updatedApprovalStep);

		// Test fetching all vacations.

		List<ApprovalStep> vacations = approvalStepDAO.getAll();
		Assert.assertTrue(vacations.contains(updatedApprovalStep));

		// Test deleting vacation

		approvalStepDAO.delete(updatedApprovalStep);
		List<ApprovalStep> approvalStepsThatLeft = approvalStepDAO.getAll();
		Assert.assertTrue(!approvalStepsThatLeft.contains(updatedApprovalStep));
		
		approvalStepDAO.removeFromDB(updatedApprovalStep);

		// Delete other test data
		vacationDAO.removeFromDB(testVacation);
		
		userDAO.removeFromDB(testUser);
		userDAO.removeFromDB(testDeputy1);
		userDAO.removeFromDB(testDeputy2);
		
		roleDAO.removeFromDB(testRole);
	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateException() throws StaleObjectStateException {
		Assert.assertNotNull(testApprovalStep);	
		
		ApprovalStep approvalStep = approvalStepDAO.getById(testApprovalStep.getId());
		
		approvalStepDAO.update(approvalStep);
		
		// Exception should be thrown
		approvalStepDAO.update(approvalStep);
	}
	
	@Test
	public void test_getAllApprovalStepsWithSpecifiedApproverRole() {
		Assert.assertNotNull(testParentRole);
		Assert.assertNotNull(testApprovalStep);
		
		List<ApprovalStep> approvalSteps = 
				approvalStepDAO.getAllApprovalStepsWithSpecifiedApproverRole(testParentRole);
		
		Assert.assertTrue("List should contain saved approval step", approvalSteps.contains(testApprovalStep));
	}
	
	@Test
	public void test_deleteApprovalStepsWithSpecifiedApproverRole() {
		Assert.assertNotNull(testParentRole);
		
		approvalStepDAO.deleteApprovalStepsWithSpecifiedApproverRole(testParentRole);
		
		List<ApprovalStep> approvalSteps = 
				approvalStepDAO.getAllApprovalStepsWithSpecifiedApproverRole(testParentRole);
		
		Assert.assertTrue("List should not contain saved approval step", !approvalSteps.contains(testApprovalStep));

	}
}
