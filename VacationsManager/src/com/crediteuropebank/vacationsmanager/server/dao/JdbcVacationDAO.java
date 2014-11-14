package com.crediteuropebank.vacationsmanager.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dblogging.AffectedRows;
import com.crediteuropebank.vacationsmanager.server.dblogging.ComparisonCondition;
import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.server.dblogging.SearchArgument;
import com.crediteuropebank.vacationsmanager.server.dblogging.TableInfo;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;

/**
 * This interface contains methods for making CRUD and select operations with
 * {@link Vacation} domain object.
 * 
 * @author dimas
 *
 */
@Repository(value="jdbcVacationDAO")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcVacationDAO implements VacationDAO{
	
	/**
	 * SQL query for inserting new vacation to the database.
	 */
	private static final String SQL_INSERT_VACATION =
			"insert into vacations(start_date, end_date, state, user_id, version) " +
			"values (?, ?, ?, ?, 0)";
	
	/**
	 * SQL query for updating existing vacation in the database.
	 */
	private static final String SQL_UPDATE_VACATION =
			"update vacations set start_date=?, end_date=?, state=?, user_id=?, version=version+1 " +
			"where id=? and version=? and status=1";
	
	/**
	 * SQL query for deleting existing vacation from DB.
	 */
	private static final String SQL_DELETE_VACATION = 
			"delete from vacations where id=?";
	
	/**
	 * SQL query for selecting vacation by its id.
	 */
	private static final String SQL_GET_VACATION_BY_ID =
			"select * from vacations where id=? and status=1";
	
	/**
	 * SQL query for selecting all vacations from DB.
	 */
	private static final String SQL_GET_ALL_VACATIONS = 
			"select * from vacations where status=1 order by start_date desc, end_date desc";
	
	/**
	 * SQL query for selecting all vacations from DB for specified date range.
	 */
	private static final String SQL_GET_ALL_VACATIONS_FOR_DATE_RANGE = 
			"select * from vacations where status=1 and end_date>? and start_date<? order by start_date desc, end_date desc";
	
	/**
	 * SQL query for selecting actual vacations (where end date >= current date and state doesn't equals rejected).
	 * <p/>Fetched records is ordered by start date.
	 */
	private static final String SQL_GET_ACTUAL_VACATIONS = 
			"select * from vacations where end_date>=? and status=1 " +
					" and state!='" + VacationState.REJECTED + "'" +
						" order by start_date desc, end_date desc";
	
	/**
	 * SQL query for selecting actual vacations (where end date >= current date and state doesn't equals rejected) for 
	 * specified period.
	 * 
	 * <p/>Fetched records is ordered by start date.
	 */
	private static final String SQL_GET_ACTUAL_VACATIONS_FOR_DATE_RANGE = 
			"select * from vacations where status=1 " +
					" and state!='" + VacationState.REJECTED + "'" +
						" and end_date>? and start_date<?" +
							" order by start_date desc, end_date desc";
	
	/** 
	 * SQL query for changing vacation's state.
	 */
	private static final String SQL_CHANGE_VACATION_STATE = 
			"update vacations set state=?, version=version+1 where id=? and version=? and status=1";
	
	/**
	 * SQL query for changing status of the concrete record in DB (status values: 1 - is used; 0 - was deleted)
	 */
	private static final String SQL_CHANGE_STATUS = "update vacations set status=?, version=version+1 where id=? and version=?";
	
	/**
	 * SQL query for changing status of the records in table which holds many-to-many relation.
	 */
	private static final String SQL_CHANGE_RELATION_TABLE_STATUS = 
			"update vacations_deputies set status=? where vacation_id=?";
	
	/**
	 * SQL query for inserting new record to vacations_users table for maintaining many-to-many relationship.
	 */
	private static final String SQL_INSERT_VACATION_DEPUTIES_RELATION = 
			"insert into vacations_deputies (vacation_id, user_id, list_index) values (?, ?, ?)";
	
	/**
	 * SQL query for deleting record from vacations_deputies table.
	 */
	private static final String SQL_DELETE_VACATION_DEPUTIES_RELATION_BY_VACATION_ID = 
			"delete from vacations_deputies where vacation_id=?";
	
	/**
	 * SQL query for fetching all deputies for specified vacation.
	 */
	private static final String SQL_GET_DEPUTIES_FOR_VACATION = "select u.ID, u.USERNAME, u.PASSWORD, u.FULLNAME, u.ROLE_ID, u.EMAIL, u.VERSION " +
			"from users u, vacations_deputies vd where vd.status=1 and vd.vacation_id=? and u.ID = vd.USER_ID and u.status=1 order by list_index asc";
	
	/**
	 * SQL query for calculating number of vacations for specified user in specified period. 
	 * Rejected vacations have not been calculated.
	 */
	private static final String SQL_CALCULATE_VACATIONS_FOR_PERIOD = 
			"select count(*) from vacations where status=1 and user_id=? " +
					"and ((start_date between ? and ?) OR (end_date between ? and ?) OR (start_date<=? and end_date>=?)) " +
						" and state!='" + VacationState.REJECTED + "'";
	
	/**
	 * SQL query that calculates for how many vacations in specified period specified user is deputy.
	 */
	private static final String SQL_CALCULATE_VACATIONS_FOR_WHICH_USER_IS_DEPUTY =
			"select v.ID from vacations v, vacations_deputies vd where vd.status=1 and vd.USER_ID=? and v.`STATUS`=1 " +
					" and ( (v.START_DATE between ? and ?) OR (v.END_DATE between ? and ?) OR (v.start_date<=? and v.end_date>=?)) " +
						" and v.ID=vd.VACATION_ID" +
							" and v.state!='" + VacationState.REJECTED + "'";
	
	/**
	 * This query calculates users with specified role which have vacation in the specified perod.
	 */
	private static final String SQL_CALCULATE_USERS_WITH_VACATION = 
			"select u.USERNAME, count(*) from users u, vacations v where u.STATUS=1 and u.ROLE_ID=? and v.STATUS=1" +
					" and ( (v.START_DATE between ? and ?) OR (v.END_DATE between ? and ?) OR (v.start_date<=? and v.end_date>=?))" +
						" and v.state!='" + VacationState.REJECTED + "'" +
							" and u.ID=v.USER_ID group by u.USERNAME";
	
	/**
	 * SQL query for getting version by id.
	 */
	private static final String SQL_GET_VERSION =
			"select version from vacations where id=? and status=1";
	
	/**
	 * SQL query for fetching rejected vacations list.
	 */
	private static final String SQL_GET_REJECTED_VACATIONS =
			"select * from vacations where status=1 and state='" + VacationState.REJECTED + "' order by start_date desc, end_date desc";
	
	/**
	 * SQL query for fetching rejected vacations list fo specified date ranje.
	 */
	private static final String SQL_GET_REJECTED_VACATIONS_FOR_DATE_RANGE =
			"select * from vacations where status=1 and state='" + VacationState.REJECTED + "'" +
					" and end_date>? and start_date<? order by start_date desc, end_date desc";
	
	/**
	 * SQL query for fetching list of all users for specified user's id.
	 */
	private static final String SQL_GET_ALL_VACATIONS_FOR_USER =
			"select * from vacations where status=1 and user_id=?";
	
	/**
	 * SQL query for deleting user from all deputies lists (change its status to 0).
	 */
	private static final String SQL_DELETE_USER_FROM_ALL_DEPUTIES_LISTS = 
			"update vacations_deputies set status=0 where status=1 and user_id=?";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoleDAO roleDAO; 
	
	@Autowired
	private RemainingVacationDaysDAO vacationDaysDAO;
	
	@Autowired
	private UsedVacationDaysDAO usedVacationDaysDAO;
	
	@Autowired
	private DBLogger<Vacation> dbLogger;
	
	@Autowired
	private KeyHolderFactory keyHolderFactory;

	@CacheEvict(value = "vacations", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public Vacation save(Vacation vacation) {
		
		// Insert vacation's record to the table.
		Vacation savedVacation = insertVacationRecord(vacation);
		
		// Set to used vacation days record the id of the saved vacation record.
		DaoUtil.setObjectId(savedVacation.getUsedVacationDays(), savedVacation.getId());
		
		// insert used vacation days record.
		saveUsedVacationDaysRecord(savedVacation.getUsedVacationDays());
		
		// Save many-to-many relationship between vacation and it's deputies.
		saveVacationAndDeputiesRelationship(savedVacation.getId(), savedVacation.getDeputies());

		return savedVacation;
	}
	
	/**
	 * This method inserts vacation's record into VACATIONS table.
	 * 
	 * @param vacation - vacation to be saved.
	 * @return the saved vacation object with generated id.
	 */
	private Vacation insertVacationRecord(final Vacation vacation) {
		// additional check
		if (vacation.getUsedVacationDays() == null) {
			throw new IllegalStateException("Vacation days used for current vacation should not be null.");
		}
		
		KeyHolder keyHolder = keyHolderFactory.newKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection connection)
					throws SQLException {
				
				PreparedStatement ps = connection.prepareStatement(SQL_INSERT_VACATION, new String[]{"ID"});
				
				ps.setDate(1, DateUtil.convertToSqlDate(vacation.getStartDate()));
				ps.setDate(2, DateUtil.convertToSqlDate(vacation.getEndDate()));
				ps.setString(3, vacation.getState().toString());
				ps.setObject(4, vacation.getUser().getId());
				
				return ps;
			}
		}, keyHolder);
		
		Long id = (Long) keyHolder.getKey();
		if (id != null) {
			DaoUtil.setObjectId(vacation, id);
		} else {
			DaoUtil.setObjectId(vacation, -1);
		}
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(vacation, OperationName.CREATE);
		
		return vacation;
	}
	
	/**
	 * This method saves info about vacation days used for concrete vacation to the table
	 * USED_VACATION_DAYS. Note, that input object should obligatory contain id which should be 
	 * equals to vacation's id.
	 * 
	 * @param usedVacationDays
	 */
	private void saveUsedVacationDaysRecord(final UsedVacationDays usedVacationDays) {
		usedVacationDaysDAO.save(usedVacationDays);
	}
	
	/**
	 * This method saves many-to-many relationship between vacations and deputies 
	 * to VACATIONS_DEPUTIES table.
	 * 
	 * @param vacationId - saved vacation's id.
	 * @param deputies - list of the deputies.
	 */
	private void saveVacationAndDeputiesRelationship(final long vacationId, final List<User> deputies) {
		jdbcTemplate.batchUpdate(SQL_INSERT_VACATION_DEPUTIES_RELATION, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, vacationId);
				ps.setLong(2, deputies.get(i).getId());
				ps.setInt(3, i);
			}
			
			@Override
			public int getBatchSize() {
				return deputies.size();
			}
		});
		
		// Add information about operation under relation table to DB.
		TableInfo tableInfo = TableInfo.newTableInfo("VACATIONS_DEPUTIES", 
				"VACATIONS_DEPUTIES_LOG", 
				new String[]{"VACATION_ID", "USER_ID", "LIST_INDEX"});
		
		dbLogger.logComplexOperation(tableInfo, 
				OperationName.CREATE, 
				AffectedRows.MANY, 
				new SearchArgument[]{new SearchArgument("VACATION_ID", vacationId, ComparisonCondition.EQ), 
						new SearchArgument("STATUS", 1, ComparisonCondition.EQ)});

	}

	/**
	 * This method updates existed vacation record and related to it used vacation days record. 
	 * Also this method updates relationship between vacation and deputies (It deletes old records and 
	 * inserts new one).
	 */
	@CacheEvict(value = "vacations", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(final Vacation vacation) throws StaleObjectStateException {
		
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_UPDATE_VACATION,
					vacation.getStartDate(),
					vacation.getEndDate(),
					vacation.getState().toString(),
					(vacation.getUser()!=null)?vacation.getUser().getId():null, // but it shouldn't be null
							vacation.getId(),
							vacation.getVersion());

			if (affectedRowsCount == 0  && !compareVersions(vacation)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(vacation)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		/* Update related used vacation days record. */
		usedVacationDaysDAO.update(vacation.getUsedVacationDays());
		
		final long vacationId = vacation.getId();
		final List<User> deputies = vacation.getDeputies();
		
		/* Delete old record with vacations and deputies relationship */
		deleteVacationsDeputiesRelationByVacationId(vacationId);
		
		/* Insert new records that holds vacation and deputies relationship */
		saveVacationAndDeputiesRelationship(vacationId, deputies);
		
		// Add information about operation to log table.
		dbLogger.logBasicOperation(vacation, OperationName.UPDATE);
	}

	@CacheEvict(value = "vacations", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(final Vacation vacation) throws StaleObjectStateException {
		usedVacationDaysDAO.delete(vacation.getUsedVacationDays());
		
		try {
			int vacationAffectedRowsNumber = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0, // saved 1 if true and 0 if false
					vacation.getId(),
					vacation.getVersion());

			int relationAffectedRowsNumber = deleteVacationsDeputiesRelationByVacationId(vacation.getId());

			if (vacationAffectedRowsNumber==0 && relationAffectedRowsNumber==0  && !compareVersions(vacation)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(vacation)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table.
		dbLogger.logBasicOperation(vacation, OperationName.DELETE);
	}
	
	/**
	 * Deletes (change status to 0) rows from vacations_deputies table that holds many-to-many relation between 
	 * vacations and deputies. As criteria of deleting uses vacation's id.
	 * 
	 * @param vacationId
	 * @return the number of affected rows.
	 */
	private int deleteVacationsDeputiesRelationByVacationId(final long vacationId) {
		/*
		 *  We should call log operation before deleting vacations and deputies relation records because
		 *  in other case we will not have criteria to filter old data from new.
		 */
		TableInfo tableInfo = TableInfo.newTableInfo("VACATIONS_DEPUTIES", 
				"VACATIONS_DEPUTIES_LOG", 
				new String[]{"VACATION_ID", "USER_ID", "LIST_INDEX"});
		
		dbLogger.logComplexOperation(tableInfo, 
				OperationName.DELETE, 
				AffectedRows.MANY, 
				new SearchArgument[]{new SearchArgument("VACATION_ID", vacationId, ComparisonCondition.EQ), 
						new SearchArgument("STATUS", 1, ComparisonCondition.EQ)});
		
		return jdbcTemplate.update(SQL_CHANGE_RELATION_TABLE_STATUS,
				0, // saved 1 if true and 0 if false
				vacationId);
	}
	
	@CacheEvict(value = "vacations", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(final Vacation vacation) {
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(vacation, OperationName.REMOVE);
		
		usedVacationDaysDAO.removeFromDB(vacation.getUsedVacationDays());
		
		jdbcTemplate.update(SQL_DELETE_VACATION_DEPUTIES_RELATION_BY_VACATION_ID,
				vacation.getId());
		
		jdbcTemplate.update(SQL_DELETE_VACATION, 
				vacation.getId());
	}

	/**
	 * This method fetches the list of deputies for vacation.
	 * 
	 * @param vacationId - the id of the vacation.
	 * @return the list of users who is a deputy for vacation with specified id.
	 */
	private List<User> getDeputiesForVacation(final long vacationId) {
		List<User> deputies = new ArrayList<User>();
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_DEPUTIES_FOR_VACATION, 
				vacationId);
		
		for (Map<String, Object> row: rows) {
			User deputy = new User();
			
			long userId = (Long) row.get("ID");
			DaoUtil.setObjectId(deputy, userId);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(deputy, version);
			
			deputy.setUsername((String) row.get("USERNAME"));
			deputy.setPassword((String) row.get("PASSWORD"));
			deputy.setFullName((String) row.get("FULLNAME"));
			
			// Add check on null value later.
			long roleId = (Long) row.get("ROLE_ID");
			deputy.setRole(roleDAO.getById(roleId));
			
			deputy.seteMail((String) row.get("EMAIL"));
			
			RemainingVacationDays vacationDays = vacationDaysDAO.getById(userId);
			deputy.setVacationDays(vacationDays);
			
			deputies.add(deputy);			
		}
		
		return deputies;
	}

	@Cacheable(value = "vacations")
	@Override
	public List<Vacation> getAll() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_VACATIONS);
			
		return createVacationsListListListOfResultMapsaps(rows);
	}
	
	@Cacheable(value = "vacations")
	@Override
	public Vacation getById(final long id) {
		Vacation vacation = null;
		try {
			vacation = jdbcTemplate.queryForObject(SQL_GET_VACATION_BY_ID, 
				new ParameterizedRowMapper<Vacation>() {

					@Override
					public Vacation mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						
						Vacation vacation = new Vacation();
						
						DaoUtil.setObjectId(vacation, rs.getLong("ID"));
						
						DaoUtil.setObjectVersion(vacation, rs.getInt("VERSION"));						
						
						vacation.setStartDate(rs.getDate("START_DATE"));
						vacation.setEndDate(rs.getDate("END_DATE"));
						vacation.setState(VacationState.valueOf(rs.getString("STATE")));
						
						long userId = rs.getLong("USER_ID");
						User user = userDAO.getById(userId);
						vacation.setUser(user);
						
						vacation.setDeputies(getDeputiesForVacation(vacation.getId()));
						
						// Load vacation days  that was used for this vacation.
						UsedVacationDays usedVacationDays = usedVacationDaysDAO.getById(vacation.getId());
						vacation.setUsedVacationDays(usedVacationDays);
						
						return vacation;
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

		return vacation;
	}

	@CacheEvict(value = "vacations", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void changeVacationState(final Vacation vacation, final VacationState newState) throws StaleObjectStateException {
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_CHANGE_VACATION_STATE,
					newState.toString(),
					vacation.getId(),
					vacation.getVersion());

			if (affectedRowsNumber == 0  && !compareVersions(vacation)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(vacation)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table.
		dbLogger.logBasicOperation(vacation, OperationName.UPDATE);
	}

	@Cacheable(value = "vacations")
	@Override
	public List<Vacation> getActualVacationsForDate(final Date date) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ACTUAL_VACATIONS, date);
		
		return createVacationsListListListOfResultMapsaps(rows);
	}

	@Override
	public int calculateVacationsForPeriod(final long userId, final Date startDate,
			final Date endDate) {
		int returnValue = 0;
		
		try {
			returnValue = jdbcTemplate.queryForInt(SQL_CALCULATE_VACATIONS_FOR_PERIOD, 
					userId,
					startDate,
					endDate,
					startDate,
					endDate,
					startDate,
					endDate);

		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		return returnValue;
	}

	@Override
	public List<Long> getVacationsIdsWhereUserIsDeputy(final long userId, final Date startDate,
			final Date endDate) {
		
		List<Long> vacationsIdsList = new ArrayList<Long>();
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_CALCULATE_VACATIONS_FOR_WHICH_USER_IS_DEPUTY, 
				userId,
				startDate,
				endDate,
				startDate,
				endDate,
				startDate,
				endDate);
		for (Map<String, Object> row: rows) {
			Long id = (Long) row.get("ID");
			vacationsIdsList.add(id);
		}
		
		return vacationsIdsList;
	}

	@Override
	public int calculateUsersWithVacationForRole(final Role role, final Date startDate,
			final Date endDate) {
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_CALCULATE_USERS_WITH_VACATION, 
				role.getId(),
				startDate,
				endDate,
				startDate,
				endDate,
				startDate,
				endDate);
		
		// return number of rows, found in DB.
		return rows.size();
	}

	@Override
	public boolean compareVersions(final Vacation vacation) {
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				vacation.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return dbVersion==vacation.getVersion();
	}

	@Override
	public List<Vacation> getRejectedVacations() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_REJECTED_VACATIONS);
		
		return createVacationsListListListOfResultMapsaps(rows);
	}

	@Cacheable(value = "vacations")
	@Override
	public List<Vacation> getAllVacationsForUser(final User inUser) {
		if (inUser == null) {
			throw new IllegalArgumentException("Input user object should not be null!");
		}

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_VACATIONS_FOR_USER,
				inUser.getId());

		return createVacationsListListListOfResultMapsaps(rows);
	}

	@CacheEvict(value = "vacations", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void deleteUserFromAllDeputiesLists(final User user) {
		/* 
		 * Add information to log table before operation will be done, in other case
		 * we will not have enough criteria to select data that have been changed.
		 */
		TableInfo tableInfo = TableInfo.newTableInfo("VACATIONS_DEPUTIES", 
				"VACATIONS_DEPUTIES_LOG", 
				new String[]{"VACATION_ID", "USER_ID", "LIST_INDEX"});
		
		dbLogger.logComplexOperation(tableInfo, 
				OperationName.DELETE, 
				AffectedRows.MANY, 
				new SearchArgument[]{new SearchArgument("USER_ID", user.getId(), ComparisonCondition.EQ), 
						new SearchArgument("STATUS", 1, ComparisonCondition.EQ)});
		
		jdbcTemplate.update(SQL_DELETE_USER_FROM_ALL_DEPUTIES_LISTS,
				user.getId());
	}

	@Override
	public List<Vacation> getAllVacationsForDateRange(Date startDate,
			Date endDate) {
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_VACATIONS_FOR_DATE_RANGE,
				startDate,
				endDate);
		
		return createVacationsListListListOfResultMapsaps(rows);
	}

	@Override
	public List<Vacation> getActualVacationsForDateRange(Date startDate,
			Date endDate) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ACTUAL_VACATIONS_FOR_DATE_RANGE,
				startDate,
				endDate);
		
		return createVacationsListListListOfResultMapsaps(rows);
	}

	@Override
	public List<Vacation> getRejectedVacationsForDateRange(Date startDate,
			Date endDate) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_REJECTED_VACATIONS_FOR_DATE_RANGE,
				startDate,
				endDate);
		
		return createVacationsListListListOfResultMapsaps(rows);
	}
	
	/**
	 * This method converts list of row Maps from method queryForList of JdbcTemplate to the List of Vacation objects.
	 * 
	 * @param rows - list of result rows returned from query and represented as Map<String, Object>
	 * @return the list of {@link Vacation} domain objects represented by rows
	 */
	private List<Vacation> createVacationsListListListOfResultMapsaps(List<Map<String, Object>> rows) {
		List<Vacation> vacationsList = new ArrayList<Vacation>();
		
		for (Map<String, Object> row: rows) {
			Vacation vacation = new Vacation();
			
			long id = (Long) row.get("ID");
			DaoUtil.setObjectId(vacation, id);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(vacation, version);
			
			vacation.setStartDate((Date) row.get("START_DATE"));
			vacation.setEndDate((Date) row.get("END_DATE"));
			vacation.setState(VacationState.valueOf((String) row.get("STATE")));
			
			long userId = (Long) row.get("USER_ID");
			User user  = userDAO.getById(userId);
			vacation.setUser(user);
			
			vacation.setDeputies(getDeputiesForVacation(vacation.getId()));
			
			// Load vacation days  that was used for this vacation.
			UsedVacationDays usedVacationDays = usedVacationDaysDAO.getById(vacation.getId());
			vacation.setUsedVacationDays(usedVacationDays);
			
			vacationsList.add(vacation);			
		}
		
		return vacationsList;
	}
}
