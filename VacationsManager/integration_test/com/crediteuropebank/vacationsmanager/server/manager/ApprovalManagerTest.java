package com.crediteuropebank.vacationsmanager.server.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
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
import com.crediteuropebank.vacationsmanager.server.dao.RoleDAO;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.dao.VacationDAO;
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
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class ApprovalManagerTest {
	
	private static final BigDecimal DAY_VACATIONS_AMOUNT = BigDecimalUtil.newBigDecimal(5);

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private VacationDAO vacationDAO;
	
	@Autowired
	private ApprovalStepDAO approvalStepDAO;
	
	@Autowired
	private VacationManager vacationManager;
	
	@Autowired
	private ApprovalManager approvalManager;
	
	private Role testRole;
	
	private Role testParentRole;
	
	private User testUser;
	
	private User testDeputy1;
	
	private User testDeputy2;
	
	private Vacation testVacation;
	
	@Before
	public void setUp() {
		
		/**/
		
		testParentRole = new Role();
		testParentRole.setName("TEST_PARENT_ROLE");
		testParentRole.setDesription("Test parent role");
		testParentRole.setPrivilege(Privilege.ADMIN);
		testParentRole = roleDAO.save(testParentRole);
		
		User simpleUser = new User();
		simpleUser.setUsername("SIMPLE_USER");
		simpleUser.setPassword("12345678");
		simpleUser.setFullName("Just simple user");
		simpleUser.seteMail("simple@gmail.com");
		simpleUser.setRole(testParentRole);
		
		RemainingVacationDays vacDays = new RemainingVacationDays();
		vacDays.setTwoWeeksVacations(1);
		vacDays.setOneWeekVacations(1);
		vacDays.setDayVacations(BigDecimalUtil.newBigDecimal(5));
		
		simpleUser.setVacationDays(vacDays);
		
		userDAO.save(simpleUser);
		
		/**/
		
		testRole = new Role();
		testRole.setName("TEST_VACATIONS_ROLE");
		testRole.setDesription("test role");
		testRole.setParentRole(testParentRole);
		testRole.setPrivilege(Privilege.DEFAULT);
		
		testRole = roleDAO.save(testRole);
		
		testUser = new User();
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
		
		RemainingVacationDays vacationDays3 = new RemainingVacationDays(vacationDays1);
		
		testDeputy2.setVacationDays(vacationDays3);
		testDeputy2 = userDAO.save(testDeputy2);

		// Test saving of the vacation.
		testVacation = new Vacation();
		testVacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		testVacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		testVacation.setState(VacationState.JUST_OPENED);
		testVacation.setUser(testUser);
		//testVacation.set
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(testDeputy1);
		deputies.add(testDeputy2);
		
		testVacation.setDeputies(deputies);
		
		UsedVacationDays usedVacationDays = new UsedVacationDays();
		usedVacationDays.setTwoWeeksVacations(0);
		usedVacationDays.setOneWeekVacations(1);
		usedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(2));
		
		testVacation.setUsedVacationDays(usedVacationDays);
		
		// Note that vacation have not been saved yet!
	}

	@Test
	//@Rollback(false)
	public void testCreateAndRemoveApprovalFlow() throws CustomMessageException {
		Assert.assertNotNull(testVacation);
		
		testVacation = vacationDAO.save(testVacation);
		
		approvalManager.createApprovalFlow(testVacation);
		
		approvalManager.deleteApprovalFlow(testVacation);
		
		List<ApprovalStep> approvalSteps = approvalStepDAO.getAllApprovalStepsForVacation(testVacation);
		Assert.assertTrue(approvalSteps.size() == 0);
	}
	
	@Test
	public void testApproveAndRejectActions() throws CustomMessageException {
		Assert.assertNotNull("Test vacation should be created before any test is running.", testVacation);
		
		vacationManager.saveVacation(testVacation);
		
		ApprovalStep activeApprovalStep = approvalStepDAO.getActiveApprovalStepForVacation(testVacation.getId());
		Assert.assertNotNull("On this stage should be at least 1 active approval step.", activeApprovalStep);
		
/*		Mockito.verify(mailSender).sendMail(activeApprovalStep.getVacation().getUser().geteMail(), 
				activeApprovalStep.getApprover().geteMail(), 
				MailUtil.SUBJECT_TEMPLATE_NEXT_APPROVER, 
				MailUtil.generateMailTextForApprover(activeApprovalStep.getApprover().getFullName(),
						activeApprovalStep.getVacation().getId()));*/
		
		String TEST_COMMENT_APPROVED = "Test comment - approved";
		
		activeApprovalStep.setComments(TEST_COMMENT_APPROVED);
		
		approvalManager.approve(activeApprovalStep);
		
		ApprovalStep updatedApprovalStep = approvalStepDAO.getById(activeApprovalStep.getId());		
		Assert.assertEquals("Comments have not been saved.", TEST_COMMENT_APPROVED, updatedApprovalStep.getComments());
		
		activeApprovalStep = approvalStepDAO.getActiveApprovalStepForVacation(testVacation.getId());
		Assert.assertNotNull("On this stage should be at least 1 active approval step.", activeApprovalStep);
	
		String TEST_COMMENT_REJECTED = "Test comment - rejected";
		
		activeApprovalStep.setComments(TEST_COMMENT_REJECTED);
		
		approvalManager.reject(activeApprovalStep);
		
		updatedApprovalStep = approvalStepDAO.getById(activeApprovalStep.getId());		
		Assert.assertEquals("Comments have not been saved.", TEST_COMMENT_REJECTED, updatedApprovalStep.getComments());
		
		activeApprovalStep = approvalStepDAO.getActiveApprovalStepForVacation(testVacation.getId());
		Assert.assertNull("After rejection there should not be active approval steps.", activeApprovalStep);	
		
		Vacation fetchedVacation = vacationManager.getVacationById(testVacation.getId());
		Assert.assertEquals("Vacation's state should be REJECTED!", VacationState.REJECTED, fetchedVacation.getState());
		
		//approvalManager.getActiveApprovalsForApprover(testDeputy1);
	}
	
	@Test
	public void test_deleteApprovalStepsWithSpecifiedApproverRole() throws CustomMessageException {
		Assert.assertNotNull(testVacation);
		Assert.assertNotNull(testParentRole);
		
		testVacation = vacationManager.saveVacation(testVacation);
		
		approvalManager.deleteApprovalStepsWithSpecifiedApproverRole(testParentRole);
		
		List<ApprovalStep> approvalSteps = approvalStepDAO.getAllApprovalStepsWithSpecifiedApproverRole(testParentRole);
		Assert.assertEquals("List of approval steps should be empty!", approvalSteps.size(), 0);
		
		Vacation fetchedVacation = vacationDAO.getById(testVacation.getId());
		
		Assert.assertEquals("Vacations state should be rejected", VacationState.REJECTED, fetchedVacation.getState());
	}
	
}
