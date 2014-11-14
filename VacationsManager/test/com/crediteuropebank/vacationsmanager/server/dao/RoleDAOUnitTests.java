package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.KeyHolder;

import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;

/**
 * 
 * This is a class with unit tests to learn how to use mockito in testing purposes (applied to DAO 
 * layer).
 * 
 * @author dimas
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleDAOUnitTests {
	
	private static final long TEST_GENERATED_ID = 777L; 
	private static final long TEST_ROLE_ID = 1L;
	private static final int TEST_ROLE_VERSION = 7;
	
	@Mock
	private DBLogger<Role> mockDbLogger;
	
	@Mock
	private JdbcTemplate mockJdbcTemplate;
	
	@Mock
	private KeyHolderFactory mockKeyHolderFactory;
	
	@InjectMocks
	private JdbcRoleDAO roleDAO;
	
	/**
	 * test role that is used in all methods.
	 */
	private Role role;
	
	@Before
	public void setUp(){
		// Create a test role object
		role = new Role();
		//role.setId(TEST_ROLE_ID);
		DaoUtil.setObjectId(role, TEST_ROLE_ID);
		DaoUtil.setObjectVersion(role, TEST_ROLE_VERSION);
		role.setName("TEST");
		role.setDesription("Test role");
		role.setPrivilege(Privilege.DEFAULT);
		//role.setVersion(TEST_ROLE_VERSION);
		
		// Return correct version of the object
		Mockito.when(mockJdbcTemplate.queryForInt(JdbcRoleDAO.SQL_GET_VERSION, TEST_ROLE_ID))
			.thenReturn(TEST_ROLE_VERSION);
	}
	
	/*
	 * I didn't find the way how to verify that key is generated and don't find the way how 
	 * to mock KeyHolder. 
	 */
	@Test
	public void testSave() {
		Assert.assertNotNull(role);
		
		KeyHolder mockKeyHolder = Mockito.mock(KeyHolder.class);
		
		Mockito.when(mockKeyHolder.getKey()).thenReturn(TEST_GENERATED_ID);
		
		Mockito.when(mockKeyHolderFactory.newKeyHolder()).thenReturn(mockKeyHolder);
	
		roleDAO.save(role);
		
		InOrder inOrder = Mockito.inOrder(mockJdbcTemplate, mockKeyHolder, mockDbLogger);
		
		// Verify execution of the conditions.
		inOrder.verify(mockJdbcTemplate, Mockito.times(1)).update(Mockito.any(PreparedStatementCreator.class), 
				Mockito.any(KeyHolder.class));
		
		inOrder.verify(mockKeyHolder, Mockito.times(1)).getKey();
		
		inOrder.verify(mockDbLogger, Mockito.times(1)).logBasicOperation(role, OperationName.CREATE);
		
		/* 
		 * Expected -1 because I can't mock KeyHolder and should be returned -1 value because 
		 * will be returned null key by KeyHolder
		 */	
		Assert.assertEquals("Generated id is wrong!", TEST_GENERATED_ID, role.getId());
	}
	
	@Test
	public void testUpdate() throws StaleObjectStateException {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.update(JdbcRoleDAO.SQL_UPDATE_ROLE, 
				role.getName(),
				role.getDesription(),
				(role.getParentRole()!=null)?role.getParentRole().getId():null,
				role.getPrivilege().toString(),
				role.getId(),
				role.getVersion())).thenReturn(1);
		
		roleDAO.update(role);
		
		InOrder inOrder = Mockito.inOrder(mockJdbcTemplate, mockDbLogger);
		
		inOrder.verify(mockJdbcTemplate, Mockito.times(1)).update(JdbcRoleDAO.SQL_UPDATE_ROLE, 
				role.getName(),
				role.getDesription(),
				(role.getParentRole()!=null)?role.getParentRole().getId():null,
				role.getPrivilege().toString(),
				role.getId(),
				role.getVersion());
		
		inOrder.verify(mockDbLogger, Mockito.times(1)).logBasicOperation(role, OperationName.UPDATE);

	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateExceptionDuringUpdating() throws StaleObjectStateException {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.update(JdbcRoleDAO.SQL_UPDATE_ROLE, 
				role.getName(),
				role.getDesription(),
				(role.getParentRole()!=null)?role.getParentRole().getId():null,
				role.getPrivilege().toString(),
				role.getId(),
				role.getVersion())).thenReturn(0);
		
		Mockito.when(mockJdbcTemplate.queryForInt(JdbcRoleDAO.SQL_GET_VERSION, role.getId()))
		  .thenReturn(6);
		
		roleDAO.update(role);	
	}
	
	@Test
	public void testDelete() throws StaleObjectStateException {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.update(JdbcRoleDAO.SQL_CHANGE_STATUS, 
				0,   // set 0 status - deleted
				role.getId(),
				role.getVersion())).thenReturn(1);
		
		roleDAO.delete(role);
		
		InOrder inOrder = Mockito.inOrder(mockJdbcTemplate, mockDbLogger);
		
		inOrder.verify(mockJdbcTemplate, Mockito.times(1)).update(JdbcRoleDAO.SQL_CHANGE_STATUS, 
				0,   // set 0 status - deleted
				role.getId(),
				role.getVersion());
		
		inOrder.verify(mockDbLogger, Mockito.times(1)).logBasicOperation(role, OperationName.DELETE);

	}
	
	@Test(expected=StaleObjectStateException.class)
	public void testStaleObjectStateExceptionDuringDeleting() throws StaleObjectStateException {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.update(JdbcRoleDAO.SQL_CHANGE_STATUS, 
				0,   // set 0 status - deleted
				role.getId(),
				role.getVersion())).thenReturn(0);
		
		Mockito.when(mockJdbcTemplate.queryForInt(JdbcRoleDAO.SQL_GET_VERSION, role.getId()))
		  .thenReturn(6);
		
		roleDAO.delete(role);	
	}
	
	@Test
	public void testRemoveFromDB() throws StaleObjectStateException {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.update(JdbcRoleDAO.SQL_DELETE_ROLE, 
				role.getId())).thenReturn(1);
		
		roleDAO.removeFromDB(role);
		
		InOrder inOrder = Mockito.inOrder(mockDbLogger, mockJdbcTemplate);
		
		inOrder.verify(mockDbLogger, Mockito.times(1)).logBasicOperation(role, OperationName.REMOVE);
		
		inOrder.verify(mockJdbcTemplate, Mockito.times(1)).update(JdbcRoleDAO.SQL_DELETE_ROLE, 
				role.getId());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetById() {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.queryForObject(Mockito.eq(JdbcRoleDAO.SQL_GET_ROLE_BY_ID), 
				Mockito.any(ParameterizedRowMapper.class), 
				Mockito.eq(role.getId())))
				.thenReturn(role)
				.thenThrow(new EmptyResultDataAccessException(0));
		
		Role fetchedRole = roleDAO.getById(role.getId());
		
		Assert.assertEquals("Role object should be equal", role, fetchedRole);
		
		Mockito.verify(mockJdbcTemplate, Mockito.times(1)).queryForObject(Mockito.eq(JdbcRoleDAO.SQL_GET_ROLE_BY_ID), 
				Mockito.any(ParameterizedRowMapper.class), 
				Mockito.eq(role.getId()));
		
		// Check on null value in case if object wasn't find in DB		
		fetchedRole = roleDAO.getById(role.getId());
		
		Assert.assertNull("Fetched role should be null", fetchedRole);
		
		Mockito.verify(mockJdbcTemplate, Mockito.times(2)).queryForObject(Mockito.eq(JdbcRoleDAO.SQL_GET_ROLE_BY_ID), 
				Mockito.any(ParameterizedRowMapper.class), 
				Mockito.eq(role.getId()));		
	}
	
	@Test
	public void testGetAll() {
		Assert.assertNotNull(role);
		
		List<Role> expectedRolesList = prepareTestRolesList();
		
		List<Map<String, Object>> testMapsList = prepareTestRolesDataMapsList(expectedRolesList);
		
		// Define when condition for jdbcTemplate.
		Mockito.when(mockJdbcTemplate.queryForList(JdbcRoleDAO.SQL_GET_ALL_ROLES))
				.thenReturn(testMapsList);
		
		List<Role> fetchedList = roleDAO.getAll();
		
		Assert.assertEquals("Expected and returned lists should be equals", 
				expectedRolesList, 
				fetchedList);
		
		Mockito.verify(mockJdbcTemplate, Mockito.times(1)).queryForList(JdbcRoleDAO.SQL_GET_ALL_ROLES);
	}
	
	private List<Role> prepareTestRolesList() {
		
		List<Role> testRolesList = new ArrayList<Role>();
		
		Role someDefaultRole = new Role();
		//someDefaultRole.setId(3);
		//someDefaultRole.setVersion(2);
		DaoUtil.setObjectId(someDefaultRole, 3);
		DaoUtil.setObjectVersion(someDefaultRole, 2);
		someDefaultRole.setName("TEST_ROLE");
		someDefaultRole.setDesription("unit test");
		someDefaultRole.setParentRole(null);
		someDefaultRole.setPrivilege(Privilege.DEFAULT);
		
		testRolesList.add(someDefaultRole);
		
		Assert.assertNotNull(role);
		testRolesList.add(role);
		
		return testRolesList;
	}
	
	private List<Map<String, Object>> prepareTestRolesDataMapsList(final List<Role> rolesList) {
		List<Map<String, Object>> testMapsList = new ArrayList<Map<String, Object>>();
		
		for (Role testRole: rolesList) {
			Map<String, Object> testRoleMap = new HashMap<String, Object>();
			
			// default role data
			testRoleMap.put("ID", testRole.getId());
			testRoleMap.put("VERSION", testRole.getVersion());
			testRoleMap.put("NAME", testRole.getName());
			testRoleMap.put("DESCRIPTION", testRole.getDesription());
			testRoleMap.put("PARENT_ROLE_ID", testRole.getParentRole());
			testRoleMap.put("PRIVILEGE", testRole.getPrivilege().toString());
			
			testMapsList.add(testRoleMap);
		}
		
		return testMapsList;
	}
	
	@Test
	public void testGetChildRolesAmount() {
		
		List<Map<String, Object>> testMapsList = 
				prepareTestRolesDataMapsList(prepareTestRolesList());
		
		Mockito.when(mockJdbcTemplate.queryForList(JdbcRoleDAO.SQL_FIND_CHILD_ROLES, TEST_ROLE_ID)).thenReturn(testMapsList);
	
		int resultAmount = roleDAO.getChildRolesAmount(TEST_ROLE_ID);
	
		Assert.assertEquals(testMapsList.size(), resultAmount);
		
		Mockito.verify(mockJdbcTemplate, Mockito.times(1)).queryForList(JdbcRoleDAO.SQL_FIND_CHILD_ROLES, TEST_ROLE_ID);
	}
	
	@Test
	public void testCompareVersions() {
		Assert.assertNotNull(role);
		
		// This condition defined in setUp() method
		Mockito.when(mockJdbcTemplate.queryForInt(JdbcRoleDAO.SQL_GET_VERSION,
				role.getId())).thenReturn(role.getVersion()).thenThrow(new EmptyResultDataAccessException(0));
		
		boolean result = roleDAO.compareVersions(role);
		
		Assert.assertTrue("Versions should be the same!", result);
		
		result = roleDAO.compareVersions(role);
		
		Assert.assertFalse("Version should be different because during reading from db exception was " +
				"thrown and for verification was used default version (-1)!", result);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetByName() {
		Assert.assertNotNull(role);
		
		Mockito.when(mockJdbcTemplate.queryForObject(Mockito.eq(JdbcRoleDAO.SQL_GET_ROLE_BY_NAME), 
				Mockito.any(ParameterizedRowMapper.class), 
				Mockito.eq(role.getName())))
				.thenReturn(role)
				.thenThrow(new EmptyResultDataAccessException(0));
		
		Role fetchedRole = roleDAO.getRoleByName(role.getName());
		
		Assert.assertEquals("Role object should be equal", role, fetchedRole);
		
		Mockito.verify(mockJdbcTemplate, Mockito.times(1)).queryForObject(Mockito.eq(JdbcRoleDAO.SQL_GET_ROLE_BY_NAME), 
				Mockito.any(ParameterizedRowMapper.class), 
				Mockito.eq(role.getName()));
		
		// Check on null value in case if object wasn't find in DB		
		fetchedRole = roleDAO.getRoleByName(role.getName());
		
		Assert.assertNull("Fetched role should be null", fetchedRole);
		
		Mockito.verify(mockJdbcTemplate, Mockito.times(2)).queryForObject(Mockito.eq(JdbcRoleDAO.SQL_GET_ROLE_BY_NAME), 
				Mockito.any(ParameterizedRowMapper.class), 
				Mockito.eq(role.getName()));	
	}
	
}
