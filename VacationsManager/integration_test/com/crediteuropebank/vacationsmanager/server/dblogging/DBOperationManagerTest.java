package com.crediteuropebank.vacationsmanager.server.dblogging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dao.ApprovalStepDAO;
import com.crediteuropebank.vacationsmanager.server.manager.ApprovalManager;
import com.crediteuropebank.vacationsmanager.server.manager.RoleManager;
import com.crediteuropebank.vacationsmanager.server.manager.UserManager;
import com.crediteuropebank.vacationsmanager.server.manager.VacationManager;
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
 * Functionality, that this test case should test was deleted
 * 
 * @author dimas
 *
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class DBOperationManagerTest {
	
	/**
	 * SQL query for calculating number of rows in users log table for specified user's id.
	 */
	private static final String SQL_CALCULATE_NUMBER_OF_USER_LOG_RECORDS_BY_USER_ID = 
			"select count(*) from users_log where id=?";
	
	/**
	 * SQL query for fetching user log record by user's id.
	 */
	private static final String SQL_USER_LOG_TABLE_RECORD_BY_USER_ID =
			"select * from users_log where id=?";
	
	/**
	 * SQL query for calculating number of rows in approval steps log table for specified approval step's id.
	 */
	/*private static final String SQL_CALCULATE_NUMBER_OF_APPROVAL_STEPS_LOG_RECORDS_BY_VACATION_ID = 
			"select count(*) from approval_steps_log where vacation_id=?";*/
	
	/**
	 * SQL query for fetching log record by user's id.
	 */
	private static final String SQL_APROVAL_STEP_LOG_TABLE_RECORD_BY_VACATION_ID_AND_OPERATION =
			"select * from approval_steps_log where vacation_id=? and operation=?";
	
	/**
	 * SQL query for fetching record by vacation id from relation table.
	 */
	private static final String SQL_GET_DEPUTIES_LIST_BY_VACATION_ID_AND_OPERATION =
			"select * from vacations_deputies_log where vacation_id=? and operation=?";
	
	@Autowired
	private ApprovalStepDAO approvalStepDAO;
	
	@Autowired
	private RoleManager roleManager;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private VacationManager vacationManager;
	
	@Autowired
	private ApprovalManager approverManager;
	
	/*@Autowired
	private ApprovalStepDAO approvalStepDAO;*/
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * Test {@link DBOperaionManager} on the example of operations with User domain object and single row operation.
	 * @throws CustomMessageException 
	 */
	@Test
	public void testSingleRowLogOperation() throws CustomMessageException {
		Role role = new Role();
		role.setName("TEST_LOG_ROLE");
		role.setDesription("Role for testing log operation.");
		role.setPrivilege(Privilege.DEFAULT);

		role = roleManager.saveRole(role);
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(5));
		
		User user = new User();
		user.setUsername("test_log_user");
		user.setPassword("12345678");
		user.setFullName("User for testing logging in the table");
		user.seteMail("test123nottrue@eee.com");
		user.setRole(role);
		user.setVacationDays(vacationDays);
		
		user = userManager.saveUser(user);
		
		int numOfRows = jdbcTemplate.queryForInt(SQL_CALCULATE_NUMBER_OF_USER_LOG_RECORDS_BY_USER_ID, 
				user.getId());
		
		Assert.assertEquals("In log table should be only one record!", 1, numOfRows);
		
		Map<String, Object> resultsMap = jdbcTemplate.queryForMap(SQL_USER_LOG_TABLE_RECORD_BY_USER_ID,
				user.getId());
		
		/*
		 * Check returned data.
		 */
		Assert.assertNotNull("Log id column should not be null!", resultsMap.get("LOG_ID"));
		
		String operationName = (String) resultsMap.get("OPERATION");
		Assert.assertEquals("Operation name should be CREATE", "CREATE", operationName);
		
		Assert.assertNotNull("Operation time should not be null!", resultsMap.get("OPERATION_TIME"));
		
		Assert.assertNotNull("Operation user should not be null, it can be empty string!", resultsMap.get("OPERATION_USER"));
		
		long userId = (Long) resultsMap.get("ID");
		Assert.assertEquals("User id saved to log is wrong!", user.getId(), userId);
		
		String username = (String) resultsMap.get("USERNAME");
		Assert.assertEquals("Username saved to log is wrong!", user.getUsername(), username);
		
		String password = (String) resultsMap.get("PASSWORD");
		Assert.assertNotNull("Saved password should not be null!", password);
		
		String fullname = (String) resultsMap.get("FULLNAME"); 
		Assert.assertEquals("User's full name saved to log is wrong!", user.getFullName(), fullname);
		
		long roleID = (Long) resultsMap.get("ROLE_ID");
		Assert.assertEquals("Role's id saved to log is wrong!", user.getRole().getId(), roleID);
		
		String email = (String) resultsMap.get("EMAIL"); 
		Assert.assertEquals("Email saved to log is wrong!", user.geteMail(), email);

		int version = (Integer) resultsMap.get("VERSION");
		Assert.assertEquals("Version saved to log is wrong!", user.getVersion(), version);
	}
	
	/**
	 * Test {@link DBOperaionManager} on the example of operations with ApprovalStep domain object and batch operation.
	 * 
	 * Also in this test check logging to the relation table vacations_deputies.
	 * 
	 * @throws CustomMessageException 
	 */
	@Rollback(true)
	@Test
	public void testBatchLogOperation() throws CustomMessageException {
		Role testRole = new Role();
		testRole.setName("TEST_VACATIONS_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		testRole = roleManager.saveRole(testRole);
		
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
		vacationDays1.setTwoWeeksVacations(1);
		vacationDays1.setOneWeekVacations(1);
		vacationDays1.setDayVacations(BigDecimalUtil.newBigDecimal(5));

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

		// Test saving of the vacation.
		Vacation vacation = new Vacation();
		vacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setState(VacationState.JUST_OPENED);
		vacation.setUser(testUser);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(testDeputy1);
		deputies.add(testDeputy2);
		
		vacation.setDeputies(deputies);
		
		UsedVacationDays usedVacationDays = new UsedVacationDays();
		usedVacationDays.setTwoWeeksVacations(1);
		usedVacationDays.setOneWeekVacations(1);
		usedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(5));
		
		vacation.setUsedVacationDays(usedVacationDays);
		
		// Test saving vacation
		vacation = vacationManager.saveVacation(vacation);
		
		
		List<Map<String, Object>> approvalStepsLogRows = jdbcTemplate.queryForList(SQL_APROVAL_STEP_LOG_TABLE_RECORD_BY_VACATION_ID_AND_OPERATION,
				vacation.getId(),
				OperationName.CREATE.toString());
		
		Assert.assertEquals("In log table should be two records!", 2, approvalStepsLogRows.size());
		
		Map<String, Object> resultsMap = approvalStepsLogRows.get(0);
		
		/*
		 * Check returned data.
		 */
		Assert.assertNotNull("Log id column should not be null!", resultsMap.get("LOG_ID"));
		
		String operationName = (String) resultsMap.get("OPERATION");
		Assert.assertEquals("Operation name should be CREATE", "CREATE", operationName);
		
		Assert.assertNotNull("Operation time should not be null!", resultsMap.get("OPERATION_TIME"));
		
		Assert.assertNotNull("Operation user should not be null, it can be empty string!", resultsMap.get("OPERATION_USER"));
		
		//long userId = (Long) resultsMap.get("ID");
		//Assert.assertEquals("User id saved to log is wrong!", user.getId(), userId);
		Assert.assertNotNull("Approval step id should not be null!", resultsMap.get("ID"));
		
		String state = (String) resultsMap.get("STATE");
		Assert.assertEquals("State saved to log is wrong!", "WAITING", state);
		
		long vacationId = (Long) resultsMap.get("VACATION_ID");
		Assert.assertEquals("Vacation id saved to log is wrong!", vacation.getId(), vacationId);
		
		long approverId = (Long) resultsMap.get("APPROVER_ID"); 
		Assert.assertEquals("Approver id saved to log is wrong!", testDeputy1.getId(), approverId);
		
		int rowNumber = (Integer) resultsMap.get("ROW_NUMBER");
		Assert.assertEquals("Row number saved to log is wrong!",1, rowNumber);
		
		/* Should br logged one update operation in approval_steps_log table. Check this. */
		approvalStepsLogRows = jdbcTemplate.queryForList(SQL_APROVAL_STEP_LOG_TABLE_RECORD_BY_VACATION_ID_AND_OPERATION,
				vacation.getId(),
				OperationName.UPDATE.toString());
		
		Assert.assertEquals("In log table should be one record for update operation!", 1, approvalStepsLogRows.size());

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_DEPUTIES_LIST_BY_VACATION_ID_AND_OPERATION, 
				vacation.getId(),
				OperationName.CREATE.toString());
		
		Assert.assertEquals("Should be two rows in relation table!", 2, rows.size());
		
		// Check that returned data is not empty:
		for (Map<String, Object> row : rows) {
			Long vacation_id = (Long) row.get("VACATION_ID");
			Assert.assertNotNull(vacation_id);
			Assert.assertTrue(vacation_id.longValue() > 0);
			
			Long user_id = (Long) row.get("USER_ID");
			Assert.assertNotNull(user_id);
			Assert.assertTrue(user_id.longValue() > 0);
			
			Integer list_Index = (Integer) row.get("LIST_INDEX");
			Assert.assertNotNull(list_Index);
			Assert.assertTrue(list_Index.intValue() >= 0);
		}
		
		List<ApprovalStep> approvalSteps = approverManager.getAllApprovalStepsForVacation(vacation);
		
		approverManager.reject(approvalSteps.get(0));
		
		vacation = vacationManager.getVacationById(vacation.getId());
		
		vacationManager.updateVacationAfterRejection(vacation);
		
		vacation = vacationManager.getVacationById(vacation.getId());
		
		vacationManager.deleteVacation(vacation);
		
		approvalStepsLogRows = jdbcTemplate.queryForList(SQL_APROVAL_STEP_LOG_TABLE_RECORD_BY_VACATION_ID_AND_OPERATION,
				vacation.getId(),
				OperationName.DELETE.toString());
		
		Assert.assertEquals("In log table should be two records!", 4, approvalStepsLogRows.size());
		
		resultsMap = approvalStepsLogRows.get(0);
		
		/*
		 * Check returned data.
		 */
		Assert.assertNotNull("Log id column should not be null!", resultsMap.get("LOG_ID"));
		
		operationName = (String) resultsMap.get("OPERATION");
		Assert.assertEquals("Operation name should be CREATE", "DELETE", operationName);
		
		Assert.assertNotNull("Operation time should not be null!", resultsMap.get("OPERATION_TIME"));
		
		Assert.assertNotNull("Operation user should not be null, it can be empty string!", resultsMap.get("OPERATION_USER"));
		
		//long userId = (Long) resultsMap.get("ID");
		//Assert.assertEquals("User id saved to log is wrong!", user.getId(), userId);
		Assert.assertNotNull("Approval step id should not be null!", resultsMap.get("ID"));
		
		state = (String) resultsMap.get("STATE");
		//Assert.assertEquals("State saved to log is wrong!", "REJECTED", state);
		Assert.assertNotNull(state);
		
		vacationId = (Long) resultsMap.get("VACATION_ID");
		Assert.assertEquals("Vacation id saved to log is wrong!", vacation.getId(), vacationId);
		
		approverId = (Long) resultsMap.get("APPROVER_ID"); 
		Assert.assertEquals("Approver id saved to log is wrong!", testDeputy1.getId(), approverId);
		
		rowNumber = (Integer) resultsMap.get("ROW_NUMBER");
		Assert.assertEquals("Row number saved to log is wrong!",1, rowNumber);
		
		rows = jdbcTemplate.queryForList(SQL_GET_DEPUTIES_LIST_BY_VACATION_ID_AND_OPERATION, 
				vacation.getId(),
				OperationName.DELETE.toString());
		
		Assert.assertEquals("Should be two rows in relation table!", 4, rows.size());
		
		// Check that returned data is not empty:
		for (Map<String, Object> row : rows) {
			Long vacation_id = (Long) row.get("VACATION_ID");
			Assert.assertNotNull(vacation_id);
			Assert.assertTrue(vacation_id.longValue() > 0);
			
			Long user_id = (Long) row.get("USER_ID");
			Assert.assertNotNull(user_id);
			Assert.assertTrue(user_id.longValue() > 0);
			
			Integer list_Index = (Integer) row.get("LIST_INDEX");
			Assert.assertNotNull(list_Index);
			Assert.assertTrue(list_Index.intValue() >= 0);
		}
	}
	
	@Ignore
	@Test
	@Rollback(false)
	public void testSingleOperation() {
		Role role = roleManager.getRoleById(2);
		
		approvalStepDAO.deleteApprovalStepsWithSpecifiedApproverRole(role);
	}
}
