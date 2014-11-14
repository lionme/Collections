package com.crediteuropebank.vacationsmanager.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.dblogging.AffectedRows;
import com.crediteuropebank.vacationsmanager.server.dblogging.ComparisonCondition;
import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.server.dblogging.SearchArgument;
import com.crediteuropebank.vacationsmanager.server.dblogging.TableInfo;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;

/**
 * 
 * This is DAO class for User entity. You should use it for all CRUD and search operations 
 * under this entity.
 * 
 * @author dimas
 *
 */
@Repository(value="jdbcUserDAO")
@DependsOn(value="passwordEncoder")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcUserDAO implements UserDAO {
	
	/**
	 *  SQL query for inserting new user to the database.
	 */
	private static final String SQL_INSERT_USER = 
			"insert into users(username, password, fullname, role_id, email, version) " +
			"values (?, ?, ?, ?, ?, 0)";
	
	/**
	 * SQL query for updating user in database.
	 */
	private static final String SQL_UPDATE_USER = 
			"update users set username=?, fullname=?, role_id=?, email=?, version=version+1 " +
			"where id=? and version=? and status=1";
	
	/**
	 * SQL query for deleting user from database.
	 */
	private static final String SQL_DELETE_USER =
			"delete from users where id=?";
	
	/**
	 * SQL query for selecting single user by his id.
	 */
	private static final String SQL_GET_USER_BY_ID =
			"select * from users where id=? and status=1";
	
	/**
	 * SQL query for selecting all users from table.
	 */
	private static final String SQL_GET_ALL_USERS = 
			"select * from users where status=1 order by username";
	
	/**
	 * SQL query for selecting all users with specified role.
	 */
	private static final String SQL_GET_USERS_BY_ROLE =
			"select * from users where role_id=? and status=1 order by username";
	
	/**
	 * SQL query for selecting single user by his username.
	 */
	private static final String SQL_GET_USER_BY_NAME =
			"select * from users where username=? and status=1";
	
	/**
	 * SQL query for changing status of the record (status values: 1 - is used; 0 - was deleted)
	 */
	private static final String SQL_CHANGE_STATUS = 
			"update users set status=?, version=version+1 where id=? and version=?";
	
	/**
	 * SQL query that calculate all users in the DB.
	 */
	private static final String SQL_COUNT_ALL_USERS = 
			"select count(*) from users where status=1 and role_id=?";
	
	/**
	 * SQL query for getting version by id.
	 */
	private static final String SQL_GET_VERSION =
			"select version from users where id=? and status=1";
	
	/**
	 * SQL query for getting user's info by his id and password.
	 */
	private static final String SQL_CALCULATE_USERS_WITH_ID_AND_PASSWORD = 
			"select count(*) from users where status=1 and id=? and password=?";
	
	/**
	 * SQL query for updating user's password.
	 */
	private static final String SQL_UPDATE_USER_PASSWORD = 
			"update users set password=? where status=1 and id=?";
	
	/**
	 * Inject password encoder.
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private RemainingVacationDaysDAO vacationDaysDAO;
	
	@Autowired
	private DBLogger<User> dbLogger;
	
	@Autowired
	private KeyHolderFactory keyHolderFactory;

	/**
	 * After saving this method returns user with empty password (with security aim).
	 */
	@CacheEvict(value = "users", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public User save(final User user) {
		if (user == null || user.getRole() == null) {
			throw new IllegalArgumentException("Input User object or nested Role object is null");
		}
		
		if (this.getUserByUserName(user.getUsername()) != null) {
			throw new DublicateEntryException("User with specified username already exists in DB!");
		}
	
		KeyHolder keyHolder = keyHolderFactory.newKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection connection)
					throws SQLException {
				
				PreparedStatement ps = connection.prepareStatement(SQL_INSERT_USER, new String[]{"ID"});
				
				String encodedPassword = passwordEncoder.encodePassword(user.getPassword(), null);
				
				ps.setString(1, user.getUsername());
				ps.setString(2, encodedPassword);
				ps.setString(3, user.getFullName());
				ps.setLong(4, user.getRole().getId());
				ps.setString(5, user.geteMail());
				
				return ps;
			}
		}, keyHolder);
		
		Long id = (Long) keyHolder.getKey();
		if (id != null) {
			DaoUtil.setObjectId(user, id);
		} else {
			DaoUtil.setObjectId(user, -1);
		}
		
		// set empty password according to security aim
		user.setPassword("");
		
		RemainingVacationDays vacationDays = user.getVacationDays();
		DaoUtil.setObjectId(vacationDays, user.getId());
		
		// Create related vacation days record.
		vacationDaysDAO.save(vacationDays);
		
		user.setVacationDays(vacationDays);
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(user, OperationName.CREATE);
		
		return user;
	}
	
	@CacheEvict(value = "users", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(final User user) throws StaleObjectStateException {
		if (user == null || user.getRole() == null) {
			throw new IllegalArgumentException("Input User object or nested Role object is null");
		}
		
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_UPDATE_USER, 
					user.getUsername(),
					/*
					 * Administrator should not update user's password. This can be done only by user.
					 */
					//user.getPassword(),
					user.getFullName(),
					// add additional check on null later, for now it shouldn't be null
					user.getRole().getId(),
					user.geteMail(),
					user.getId(),
					user.getVersion());

			if (affectedRowsCount == 0  && !compareVersions(user)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(user)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Update related vacation days record
		vacationDaysDAO.update(user.getVacationDays());
		
		// Add information about operation to log table.
		dbLogger.logBasicOperation(user, OperationName.UPDATE);
	}

	@CacheEvict(value = "users", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(final User user) throws StaleObjectStateException {
		if (user == null || user.getRole() == null) {
			throw new IllegalArgumentException("Input User object or nested Role object is null");
		}
		
		vacationDaysDAO.delete(user.getVacationDays());
		
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0,   // set status to 0 - deleted
					user.getId(),
					user.getVersion());

			if (affectedRowsCount == 0  && !compareVersions(user)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(user)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table.
		dbLogger.logBasicOperation(user, OperationName.DELETE);
	}
	
	@CacheEvict(value = "users", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(final User user) {
		
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(user, OperationName.REMOVE);
		
		/*
		 *  Delete related vacation days record.	It has the same primary key as user's record.	
		 *  Vacation days record should be deleted before related user's record. In other case
		 *  there will be DB constraints violations error.
		 */
		vacationDaysDAO.removeFromDB(user.getVacationDays());
		
		jdbcTemplate.update(SQL_DELETE_USER, 
				user.getId());
	}

	/**
	 * If user with specified id is absent - this method throws EmptyResultDataAccessException.
	 * I need to change this later.
	 */
	@Cacheable(value="users")
	@Override
	public User getById(final long id) {
		User user = null;
		try {
			user = jdbcTemplate.queryForObject(SQL_GET_USER_BY_ID, 
				new ParameterizedRowMapper<User>() {

					@Override
					public User mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						
						return createUserFromResultSet(rs);
					}
				},
				id);
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
				
		return user;
	}

	@Cacheable(value = "users")
	@Override
	public List<User> getAll() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_USERS);

		return createUsersListFromListOfResultMaps(rows);
	}

	@Cacheable(value = "users")
	@Override
	public List<User> getUsersByRole(final Role role) {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null!");
		}

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_USERS_BY_ROLE,
				role.getId());
		
		return createUsersListFromListOfResultMaps(rows);
	}

	@Cacheable(value="users")
	@Override
	public User getUserByUserName(final String username) {
		User user = null;
		try{
			user = jdbcTemplate.queryForObject(SQL_GET_USER_BY_NAME, 
				new ParameterizedRowMapper<User>() {

					@Override
					public User mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						
						return createUserFromResultSet(rs);
					}
				},
				username);
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}

		return user;
	}

	@Override
	public int getTotalAmountOfUsersWithRole(final Role role) {
		return jdbcTemplate.queryForInt(SQL_COUNT_ALL_USERS, role.getId());
	}
	
	@Override
	public boolean compareVersions(final User user) {
		
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				user.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return dbVersion==user.getVersion();
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void updatePassword(final long userId, final String newPassword) {
		String encodedNewPassword = passwordEncoder.encodePassword(newPassword, null);
		
		jdbcTemplate.update(SQL_UPDATE_USER_PASSWORD, encodedNewPassword, userId);	
		
		/*
		 * Log information about updating user's password to the log table.
		 */
		
		TableInfo tableInfo = TableInfo.newTableInfo(User.class);
		
		// Add information about operation to log table.
		dbLogger.logComplexOperation(tableInfo, 
				OperationName.UPDATE, 
				AffectedRows.ONE, 
				new SearchArgument[]{new SearchArgument("ID", userId, ComparisonCondition.EQ)});
	}

	@Override
	public boolean isPasswordCorrect(final long userId, final String oldPassword) {

		String encodedOldPassword = passwordEncoder.encodePassword(oldPassword, null);
		
		int usersCount = jdbcTemplate.queryForInt(SQL_CALCULATE_USERS_WITH_ID_AND_PASSWORD, 
				userId, 
				encodedOldPassword);
		
		// Additional check
		if (usersCount > 1) {
			throw new IllegalArgumentException("Number of users with specified id and password " +
					"should not be greater then 1!");
		}
		
		return usersCount==1;
	}
	
	/**
	 * This method converts list of result rows Maps from method queryForList of JdbcTemplate 
	 * to the List of {@link User} objects.
	 * 
	 * @param rows - list of result rows returned from query and represented as Map<String, Object>
	 * @return the list of {@link USer} domain objects represented by rows
	 */
	private List<User> createUsersListFromListOfResultMaps(List<Map<String, Object>> rows) {
		List<User> users = new ArrayList<User>();
		
		for (Map<String, Object> row: rows) {
			User user = new User();
			
			long userId = (Long) row.get("ID");
			DaoUtil.setObjectId(user, userId);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(user, version);
			
			user.setUsername((String) row.get("USERNAME"));
			
			/*
			 * Instead of password always return empty String
			 */
			//user.setPassword(rs.getString("PASSWORD"));
			user.setPassword("");
			
			user.setFullName((String) row.get("FULLNAME"));
			
			// Add check on null value later.
			long fetchedRoleId = (Long) row.get("ROLE_ID");
			user.setRole(roleDAO.getById(fetchedRoleId));

			user.seteMail((String) row.get("EMAIL"));
			
			RemainingVacationDays vacationDays = vacationDaysDAO.getById(userId);
			user.setVacationDays(vacationDays);
			
			users.add(user);			
		}
		
		return users;
	}
	
	/**
	 * This method creates new {@link User} object using data from {@link ResultSet}.
	 * 
	 * @param rs - {@link ResultSet} that contains approval step's data
	 * @return the new {@link User} object
	 * @throws SQLException
	 */
	private User createUserFromResultSet(ResultSet rs) throws SQLException {
		User user = new User();
		
		long userId = rs.getLong("ID");
		DaoUtil.setObjectId(user, userId);
		
		int version = rs.getInt("VERSION");
		DaoUtil.setObjectVersion(user, version);
		
		user.setUsername(rs.getString("USERNAME"));
		
		/*
		 * Instead of password always return empty String
		 */
		//user.setPassword(rs.getString("PASSWORD"));
		user.setPassword("");
		
		user.setFullName(rs.getString("FULLNAME"));
		
		long roleId = rs.getLong("ROLE_ID");
		user.setRole(roleDAO.getById(roleId));
		
		user.seteMail(rs.getString("EMAIL"));
		
		RemainingVacationDays vacationDays = vacationDaysDAO.getById(userId);
		user.setVacationDays(vacationDays);
		
		return user;
	}
}
