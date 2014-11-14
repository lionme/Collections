package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.List;

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

import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
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
public class RoleDAOTest {
	
	@Autowired
	RoleDAO roleDAO;

	@Test
	public void simpleComplexTestForRoleDAOTest() throws StaleObjectStateException {

		// Test saving
		
		Role parentRole = new Role();
		parentRole.setName("MAIN_HEADER_TEST");
		parentRole.setDesription("test role");
		parentRole.setPrivilege(Privilege.ADMIN);
		
		parentRole = roleDAO.save(parentRole);
				
		// Test fetching
		Role fetchedRole = roleDAO.getById(parentRole.getId());
		Assert.assertEquals(parentRole, fetchedRole);
		
		// Test updating
		
		fetchedRole.setName("HEADER_TEST");
		fetchedRole.setDesription("changed role");
		fetchedRole.setPrivilege(Privilege.DEFAULT);
		
		roleDAO.update(fetchedRole);
		
		Role updatedRole = roleDAO.getById(parentRole.getId());
		Assert.assertEquals(fetchedRole, updatedRole);
		
		parentRole = updatedRole;
		
		// Test parent/child saving
		
		Role childRole = new Role();
		//childRole.setId(childRoleId);
		childRole.setName("SUPERVISOR_TEST");
		childRole.setDesription("test role");
		childRole.setParentRole(parentRole);
		childRole.setPrivilege(Privilege.DEFAULT);
		
		childRole = roleDAO.save(childRole);
		
		long childRoleId = childRole.getId();
		Assert.assertNotSame(0, childRoleId);	
		//Assert.assertEquals(childRoleId, childRole.getId());
		
		Role fetchedChildRole = roleDAO.getById(childRoleId);
		
		Assert.assertEquals(parentRole, fetchedChildRole.getParentRole());
		
		// Test fetching all records
		
		List<Role> rolesList = roleDAO.getAll();
		Assert.assertTrue(rolesList.contains(parentRole));
		Assert.assertTrue(rolesList.contains(childRole));
		
		// Test deleting
		
		roleDAO.delete(childRole);
		roleDAO.delete(parentRole);
		List<Role> existedRolesList = roleDAO.getAll();
		Assert.assertFalse(existedRolesList.contains(parentRole));
		Assert.assertFalse(existedRolesList.contains(childRole));
		
		roleDAO.removeFromDB(childRole);
		roleDAO.removeFromDB(parentRole);
	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateException() throws StaleObjectStateException {
		Role testRole = new Role();
		testRole.setName("EXCEPTION_TEST_ROLE");
		testRole.setDesription("Role for testing exception");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		roleDAO.save(testRole);
		
		testRole.setDesription("changed");
		
		roleDAO.update(testRole);
		
		// Exception should be thrown here
		roleDAO.update(testRole);
	}
	
	@Test
	@Ignore // This test is necessary to check how will be displayed in table many roles
	@Rollback(false)
	public void crateALotOfRoles() throws CustomMessageException {
		/*ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		RoleDAO roleDAO = (RoleDAO) context.getBean("jdbcRoleDAO");*/

		for (int i=70; i<=190; i++) {
			Role testRole = new Role();
			testRole.setName("TEST_ROLE_" + i);
			testRole.setDesription("test role" + i);
			//testRole.setParentRole(parentRole);
			roleDAO.save(testRole);
		}
	}
}
