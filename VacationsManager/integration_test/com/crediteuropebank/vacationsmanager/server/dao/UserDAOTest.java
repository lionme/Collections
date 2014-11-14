package com.crediteuropebank.vacationsmanager.server.dao;

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

import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
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
public class UserDAOTest {

	@Autowired
	UserDAO userDAO;
	
	@Autowired
	RoleDAO roleDAO;
	
	private User savedUser;
	
	private Role savedRole;
	
	@Before
	public void setUp() {
		// Test saving.

		Role testRole = new Role();
		testRole.setName("TEST_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);

		savedRole = roleDAO.save(testRole);

		// Test saving of the user.

		User testUser = new User();
		testUser.setUsername("TestSuperviser");
		testUser.setPassword("12345678");
		testUser.setFullName("Just simple superviser");
		testUser.seteMail("test@gmail.com");
		testUser.setRole(testRole);

		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(5));

		testUser.setVacationDays(vacationDays);

		savedUser = userDAO.save(testUser);
	}

	/**
	 * I know that writing one method for testing whole class is a bad thing, but I don't want to
	 * waste my time by writing a lot of different tests for each method in the class
	 * and preparing data for them. So I just wrote a simple test case for testing main functionality.
	 * In future may be I will rewrite it.
	 * @throws CustomMessageException 
	 */
	@Test
	public void testWholeJdbcUserDAOClass() throws StaleObjectStateException {
		
		/*if (savedUser == null && savedRole == null) {
			throw new IllegalStateException();
		}*/
		
		Assert.assertNotNull(savedRole);
		Assert.assertNotNull(savedUser);
		
		// Test fetching user by id.
		
		User fetchedUser = userDAO.getById(savedUser.getId());
		Assert.assertEquals(fetchedUser, savedUser);
		
		// Test fetching user by username.
		
		fetchedUser = userDAO.getUserByUserName(savedUser.getUsername());
		Assert.assertEquals(savedUser, fetchedUser);
		
		// Test updating user
		
		fetchedUser.setFullName("test_updating");
		//fetchedUser.setPassword("777");
		fetchedUser.seteMail("changedMail@rambler.ru");
		userDAO.update(fetchedUser);
		
		User updatedUser = userDAO.getById(fetchedUser.getId());
		Assert.assertEquals(fetchedUser, updatedUser);
		
		//testUser = updatedUser;
		
		// Test fetching all users (very simple)
		
		List<User> usersList = userDAO.getAll();
		Assert.assertTrue(usersList.contains(updatedUser));
		
		// Test fetching users by role
		
		List<User> listOfUsersWithTestRole = userDAO.getUsersByRole(savedRole);

		Assert.assertTrue(listOfUsersWithTestRole.contains(updatedUser));

		// Check that list contains only supervisors
		for (User user: listOfUsersWithTestRole) {
			Assert.assertEquals(user.getRole(), savedRole);
		}
		
		// Test deleting user
		
		userDAO.delete(updatedUser);
		List<User> usersListNew = userDAO.getAll();
		Assert.assertTrue(!usersListNew.contains(updatedUser));
		
		userDAO.removeFromDB(updatedUser);
		
		// delete test role
		//roleDAO.deleteRole(testRoleId);
		roleDAO.removeFromDB(savedRole);
	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateException() throws StaleObjectStateException {
		/*if (savedUser == null && savedRole == null) {
			throw new IllegalStateException();
		}*/
		Assert.assertNotNull(savedRole);
		Assert.assertNotNull(savedUser);
		
		User testUser = userDAO.getById(savedUser.getId());
				
		testUser.setFullName("Changed full name");
		
		userDAO.update(testUser);
		
		// Here exception should be thrown.
		userDAO.update(testUser);
		
	}
	
/*	@Test
	//@Ignore
	public void testGetUsersByRole() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		UserDAO userDAO = (UserDAO) context.getBean("jdbcUserDAO");
		
		User test_user= new User();
		test_user.setUsername("dummy2");
		test_user.setPassword("12345678");
		test_user.setFullName("dummy user");
		test_user.seteMail("dummy@gmail.com");
		test_user.setRole(UserRole.SUPERVISOR);
		
		userDAO.createUser(test_user);
		
		try {

			List<User> supervisersList = userDAO.getUsersByRole(UserRole.SUPERVISOR);

			Assert.assertTrue(supervisersList.contains(test_user));

			// Check that list contains only supervisors
			for (User user: supervisersList) {
				Assert.assertEquals(user.getRole(), UserRole.SUPERVISOR);
			}

		}finally {
			userDAO.deleteUser(test_user.getId());
		}
	}*/
	
/*	@Test
	public void testGetUserByIdInCaseOfAbsentUser() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		UserDAO userDAO = (UserDAO) context.getBean("jdbcUserDAO");
		
		// If we give id of absent user should return null
		User user = userDAO.getUserById(0);
		Assert.assertNull(user);
	}
	
	@Test
	public void testGetUserByUserNameInCaseOfAbsentUser() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		UserDAO userDAO = (UserDAO) context.getBean("jdbcUserDAO");
		
		// If we give id of absent user should return null
		User user = userDAO.getUserByUserName("NotExisted");
		Assert.assertNull(user);
	}*/
	
	/*@Test
	public void testSelectingByMultipleRows() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		UserDAO userDAO = (UserDAO) context.getBean("jdbcUserDAO");
		
		List<User> users = userDAO.getUserByRoles(new UserRole[]{UserRole.HEADER, UserRole.SUPERVISOR});
		Assert.assertTrue(users.size()>0);
		System.out.println(users);
	}*/
	
	/**
	 * Test case for creating few users in DB. (For don't doing this manual)
	 */
/*	@Test
	@Ignore
	public void createTestUser() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		UserDAO userDAO = (UserDAO) context.getBean("jdbcUserDAO");
		
		User test_user = new User();
		test_user.setUsername("developer");
		test_user.setPassword("12345678");
		test_user.setFullName("Just simple developer");
		test_user.seteMail("developer@gmail.com");
		test_user.setRole(UserRole.DEVELOPER);
		
		userDAO.createUser(test_user);	
		
		test_user = new User();
		test_user.setUsername("supervisor");
		test_user.setPassword("12345678");
		test_user.setFullName("Just simple superviser");
		test_user.seteMail("supervisor@gmail.com");
		test_user.setRole(UserRole.SUPERVISOR);
		
		userDAO.createUser(test_user);	
		
		test_user = new User();
		test_user.setUsername("admin");
		test_user.setPassword("12345678");
		test_user.setFullName("Just simple header");
		test_user.seteMail("aadmin@gmail.com");
		test_user.setRole(UserRole.HEADER);
		
		userDAO.createUser(test_user);	
	}*/
	
}
