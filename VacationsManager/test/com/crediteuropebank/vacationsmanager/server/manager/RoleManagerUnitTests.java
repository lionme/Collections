package com.crediteuropebank.vacationsmanager.server.manager;

import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.crediteuropebank.vacationsmanager.server.dao.RoleDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.validation.ServerValidationUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class contains unit tests for {@link RoleManager} class.
 * It is written mostly in educational purposes.
 * 
 * @author dimas
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleManagerUnitTests {
	
	private static final long TEST_ROLE_ID = 777L;

	private static final List<Role> rolesList = new ArrayList<Role>();

	@Mock
	private RoleDAO roleDAO;

	@Mock
	private UserDAO userDAO;

	@Mock
	private ApprovalManager approvalManager;

	@Mock
	private ServerValidationUtil validationUtil;

	@InjectMocks
	private RoleManager roleManager;

	@BeforeClass
	public static void beforeTestClass() {
		/*
		 * Create roles for test
		 */

		Role role = new Role();
		role.setName("TEST_ROLE_1");
		role.setDesription("First test role");
		role.setPrivilege(Privilege.ADMIN);

		rolesList.add(role);

		role = new Role();
		role.setName("TEST_ROLE_2");
		role.setDesription("Second test role");
		role.setPrivilege(Privilege.DEFAULT);
		role.setParentRole(role);

		rolesList.add(role);
	}

	@Test
	public void testGetAllRoles() {
		Mockito.when(roleDAO.getAll()).thenReturn(rolesList);

		List<Role> rolesList = roleManager.getAllRoles();

		Assert.assertThat(rolesList,  equalTo(rolesList));
	}

	@Test
	public void testSaveRole() throws CustomMessageException {
		// Use first element of the list for tests.
		Role testRole = rolesList.get(0);

		Mockito.when(roleDAO.save(testRole)).thenReturn(testRole);

		Role returnedRole = roleManager.saveRole(testRole);

		Assert.assertThat(returnedRole, equalTo(testRole));

		Mockito.verify(validationUtil, Mockito.times(1)).validate(testRole);
	}

	@Test
	public void testUpdateRole() throws CustomMessageException, StaleObjectStateException {
		// Use first element of the list for tests.
		Role testRole = rolesList.get(0);

		roleManager.updateRole(testRole);

		Mockito.verify(validationUtil, Mockito.times(1)).validate(testRole);
		Mockito.verify(roleDAO, Mockito.times(1)).update(testRole);
	}
	
	@Test
	public void testDeleteRole() throws CustomMessageException, StaleObjectStateException {
		// Use first element of the list for tests.
		Role mockRole = Mockito.mock(Role.class);
		
		Mockito.when(mockRole.getId()).thenReturn(TEST_ROLE_ID);
		Mockito.when(roleDAO.getChildRolesAmount(TEST_ROLE_ID)).thenReturn(0);
		Mockito.when(userDAO.getUsersByRole(mockRole)).thenReturn(new ArrayList<User>());
		Mockito.when(approvalManager.getNumberOfApprovalStepsWithApproverRole(mockRole)).thenReturn(0);
		
		roleManager.deleteRole(mockRole);
		
		Mockito.verify(roleDAO, Mockito.times(1)).delete(mockRole);
	}
	
	@Test(expected=CustomMessageException.class)
	public void testDeleteRole_exceptionWhenChildRoleExist() throws CustomMessageException {
		Role mockRole = Mockito.mock(Role.class);
		
		Mockito.when(mockRole.getId()).thenReturn(TEST_ROLE_ID);
		Mockito.when(roleDAO.getChildRolesAmount(TEST_ROLE_ID)).thenReturn(1);
		Mockito.when(userDAO.getUsersByRole(mockRole)).thenReturn(new ArrayList<User>());
		Mockito.when(approvalManager.getNumberOfApprovalStepsWithApproverRole(mockRole)).thenReturn(0);
		
		roleManager.deleteRole(mockRole);
	}

	@Test(expected=CustomMessageException.class)
	public void testDeleteRole_exceptionWhenUsersWithRoleExist() throws CustomMessageException {
		Role mockRole = Mockito.mock(Role.class);
		
		@SuppressWarnings("unchecked")
		List<User> mockList = Mockito.mock(List.class);
		Mockito.when(mockList.size()).thenReturn(1);
		
		Mockito.when(mockRole.getId()).thenReturn(TEST_ROLE_ID);
		Mockito.when(roleDAO.getChildRolesAmount(TEST_ROLE_ID)).thenReturn(0);
		Mockito.when(userDAO.getUsersByRole(mockRole)).thenReturn(mockList);
		Mockito.when(approvalManager.getNumberOfApprovalStepsWithApproverRole(mockRole)).thenReturn(0);
		
		roleManager.deleteRole(mockRole);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testDeleteRole_exceptionWhenApprovalStepsWithThisDeletedRoleExists() throws CustomMessageException {
		Role mockRole = Mockito.mock(Role.class);
		
		Mockito.when(mockRole.getId()).thenReturn(TEST_ROLE_ID);
		Mockito.when(roleDAO.getChildRolesAmount(TEST_ROLE_ID)).thenReturn(0);
		Mockito.when(userDAO.getUsersByRole(mockRole)).thenReturn(new ArrayList<User>());
		Mockito.when(approvalManager.getNumberOfApprovalStepsWithApproverRole(mockRole)).thenReturn(1);
		
		roleManager.deleteRole(mockRole);
	}
	
	@Test
	public void testGetById() {
		Role testRole = rolesList.get(0);
		
		Mockito.when(roleDAO.getById(TEST_ROLE_ID)).thenReturn(testRole);
		
		Role fetchedRole = roleDAO.getById(TEST_ROLE_ID);
		
		Assert.assertEquals(testRole, fetchedRole);
	}
}
