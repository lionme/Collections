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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.dblogging.ComparisonCondition;
import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.server.dblogging.SearchArgument;
import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;

/**
 * {@link ApprovalStepDAO} implementation for work with Spring's JdbcTeplate.
 * 
 * @author DIMAS
 *
 */
@Repository(value="jdbcApprovalStepDAO")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcApprovalStepDAO implements ApprovalStepDAO {
	
	/**
	 *  SQL query for inserting new approval step to the database.
	 */
	private static final String SQL_INSERT_APPROVAL_STEP = 
			"insert into approval_steps (state, vacation_id, role_id, approver_id, row_number, comments, version) " +
			"values (?, ?, ?, ?, ?, ?, 0)";
	
	/**
	 * SQL query for updating approval step in database.
	 */
	private static final String SQL_UPDATE_APPROVAL_STEP = 
			"update approval_steps " +
			"set state=?, vacation_id=?, role_id=?, approver_id=?, row_number=?, comments=?, version=version+1 " +
			"where id=? and version=?";
	
	/**
	 * SQL query for deleting approval step from database.
	 */
	private static final String SQL_DELETE_APPROVAL_STEP =
			"delete from approval_steps where id=?";
	
	/**
	 * SQL query for updating approval in database.
	 */
	private static final String SQL_UPDATE_APPROVAL_STEP_STATE = 
			"update approval_steps set state=?, version=version+1 where id=? and version=?";
	
	/**
	 * SQL query for getting sorted in ascending order active approvals for specified vacation.
	 */
	private static final String SQL_GET_WAITING_APPROVALS_FOR_VACATION= 
			"select * from approval_steps where state='" + ApprovalStepState.WAITING + "' " +
					"and vacation_id=? and status=1 order by row_number asc";
	

	/**
	 * SQL query for getting approval by vacation id and row number (order number).
	 */
	private static final String SQL_GET_APPROVAL_STEP_BY_VACATION_AND_ROWNUMBER = 
			"select * from approval_steps where vacation_id=? and row_number=? and status=1";
	
	/**
	 * SQL query for changing status of the record (status values: 1 - is used; 0 - was deleted)
	 */
	private static final String SQL_CHANGE_STATUS = "update approval_steps set status=?, version=version+1 where id=? and version=?";
	
	/**
	 * SQL query for getting all active approvals that are waiting for approve from specified approver.
	 */
	private static final String SQL_GET_ACTIVE_APPROVAL_STEPS_FOR_APPROVER = 
			"select * from approval_steps where status=1 and state='" + ApprovalStepState.ACTIVE + 
			"' and (role_id=? or approver_id=?) order by row_number asc";
			
	/**
	 * SQL query for fetching approval step record by its id.
	 */
	private static final String SQL_GET_APPROVAL_STEP_BY_ID = 
			"select * from approval_steps where status=1 and id=?";
	
	/**
	 * SQL query for fetching all approval steps records.
	 */
	private static final String SQL_GET_ALL_APPROVAL_STEPS = 
			"select * from approval_steps where status=1";
	
	/**
	 * SQL query for getting version by id.
	 */
	private static final String SQL_GET_VERSION =
			"select version from approval_steps where id=? and status=1";
	
	/**
	 * SQL query for fetching approval steps list by vacation id. List is ordered by row_number.
	 */
	private static final String SQL_GET_ALL_APPROVAL_STEPS_FOR_VACATION =
			"select * from approval_steps where status=1 and vacation_id=? order by row_number";
	
	/**
	 * SQL query for fetching active approval step step for vacation. (should be one for non rejected vacation)
	 */
	private static final String SQL_GET_ACTIVE_APPROVAL_STEP_FOR_VACATION = 
			"select * from approval_steps where status=1 and vacation_id=? and state='" + ApprovalStepState.ACTIVE + "'";
	
	/**
	 * SQL query for deleting (changing status to 0) of the records with specified approver role.
	 */
	private static final String SQL_DELETE_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER_ROLE =
			"update approval_steps set status=0 where status=1 and role_id=?";
	
	/**
	 * SQL query for deleting (changing status to 0) of the records with specified approver user.
	 */
	private static final String SQL_DELETE_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER =
			"update approval_steps set status=0 where status=1 and approver_id=?";
	
	/**
	 * SQL query that fetches list of approval steps that have specified approver role and
	 * related to vacation with states: JUST_OPENED; IN_PROGRESS.
	 */
	private static final String SQL_GET_ALL_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER_ROLE =
			"select appr.* " + 
			"from approval_steps appr, vacations vac " +
			"where appr.status=1 " +
			"and   appr.ROLE_ID=? " + 
			"and   vac.STATUS=1 " +
			"and	vac.STATE in ('" + VacationState.JUST_OPENED + "', '" + VacationState.IN_PROGRESS + "') " +
			"and	vac.ID = appr.VACATION_ID";
	
	/**
	 * SQL query that fetches list of approval steps that have specified approver user and
	 * related to vacation with states: JUST_OPENED; IN_PROGRESS.
	 */
	private static final String SQL_GET_ALL_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER =
			"select appr.* " + 
			"from approval_steps appr, vacations vac " +
			"where appr.status=1 " +
			"and   appr.APPROVER_ID=? " + 
			"and   vac.STATUS=1 " +
			"and	vac.STATE in ('" + VacationState.JUST_OPENED + "', '" + VacationState.IN_PROGRESS + "') " +
			"and	vac.ID = appr.VACATION_ID";
	
	/**
	 * SQL query that calculates the number of the approval steps with specified 
	 * approver role.
	 */
	private static final String SQL_CALCULATE_NUMBER_OF_APPROVAL_STEPS_WITH_APPROVER_ROLE = 
			"select count(*) from approval_steps where STATUS=1 and role_id=?";
	
	/**
	 * SQL query for updating approval step's state with comment.
	 */
	private static final String SQL_CHANGE_STATES_FOR_VACATION_WITH_COMMENT = 
			"update approval_steps set state=?, comments=? where status=1 and vacation_id=?";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private VacationDAO vacationDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private DBLogger<ApprovalStep> dbLogger;
	
	@Autowired
	private KeyHolderFactory keyHolderFactory;

	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public ApprovalStep save(final ApprovalStep approvalStep) {
		KeyHolder keyHolder = keyHolderFactory.newKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection connection)
					throws SQLException {
				
				PreparedStatement ps = connection.prepareStatement(SQL_INSERT_APPROVAL_STEP, new String[]{"ID"});
				
				ps.setString(1, approvalStep.getState().toString());
				ps.setLong(2, approvalStep.getVacation().getId());
				ps.setObject(3, (approvalStep.getApproverRole()!=null)?approvalStep.getApproverRole().getId():null);
				ps.setObject(4, (approvalStep.getApprover()!=null)?approvalStep.getApprover().getId():null);
				ps.setInt(5, approvalStep.getRowNumber());
				ps.setString(6, approvalStep.getComments());
				
				return ps;
			}
		}, keyHolder);
		
		Long id = (Long) keyHolder.getKey();
		if (id != null) {
			DaoUtil.setObjectId(approvalStep, id);
		} else {
			DaoUtil.setObjectId(approvalStep, -1);
		}
				
		// Add record about performed operation to log table.
		dbLogger.logBasicOperation(approvalStep, OperationName.CREATE);
		
		return approvalStep;
	}

	/**
	 * Note that this method doesn't update state. For changing state use changeApprovalState(..) method.
	 * @throws StaleObjectStateException 
	 */
	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(final ApprovalStep approvalStep) throws StaleObjectStateException {
		
		try {
			int affectedRowsCaunt = jdbcTemplate.update(SQL_UPDATE_APPROVAL_STEP,
					approvalStep.getState().toString(),
					approvalStep.getVacation().getId(),
					(approvalStep.getApproverRole()!=null)?approvalStep.getApproverRole().getId():null,
							(approvalStep.getApprover()!=null)?approvalStep.getApprover().getId():null,
									approvalStep.getRowNumber(),
									approvalStep.getComments(),
									approvalStep.getId(),
									approvalStep.getVersion()); // path current object's version

			if (affectedRowsCaunt == 0 && !compareVersions(approvalStep)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(approvalStep)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add record about performed operation to log table.
		dbLogger.logBasicOperation(approvalStep, OperationName.UPDATE);
	}

	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(final ApprovalStep approvalStep) throws StaleObjectStateException {
		
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0, // set status 0 - deleted
					approvalStep.getId(),
					approvalStep.getVersion());

			if (affectedRowsCount == 0  && !compareVersions(approvalStep)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(approvalStep)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add record about performed operation to log table.
		dbLogger.logBasicOperation(approvalStep, OperationName.DELETE);
	}

	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(final ApprovalStep approvalStep) {
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(approvalStep, OperationName.REMOVE);
		
		jdbcTemplate.update(SQL_DELETE_APPROVAL_STEP, 
				approvalStep.getId());
		
	}

	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void changeApprovalStepState(final ApprovalStep approvalStep, final ApprovalStepState newState) throws StaleObjectStateException {
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_UPDATE_APPROVAL_STEP_STATE,
					newState.toString(),
					approvalStep.getId(),
					approvalStep.getVersion());

			if (affectedRowsCount == 0  && !compareVersions(approvalStep)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(approvalStep)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Write information about performed operation to log table
		dbLogger.logBasicOperation(approvalStep, OperationName.UPDATE);
	}

	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void saveListOfApprovalSteps(final List<ApprovalStep> approvalSteps) {
		if (approvalSteps == null) {
			throw new IllegalArgumentException("Input approvalSteps list should not be null!");
		}
		
		jdbcTemplate.batchUpdate(SQL_INSERT_APPROVAL_STEP, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ApprovalStep approvalStep = approvalSteps.get(i);
				ps.setString(1, approvalStep.getState().toString());
				ps.setLong(2, approvalStep.getVacation().getId());
				if (approvalStep.getApproverRole()!=null) {
					ps.setLong(3, approvalStep.getApproverRole().getId());
				} else {
					ps.setObject(3, null);
				}
				if (approvalStep.getApprover()!=null) {
					ps.setLong(4, approvalStep.getApprover().getId());
				} else {
					ps.setObject(4, null);
				}
				ps.setInt(5, approvalStep.getRowNumber());
				ps.setString(6, approvalStep.getComments());
			}
			
			@Override
			public int getBatchSize() {
				return approvalSteps.size();
			}
		});
		
		// Write information about performed operation to log table
		ApprovalStep firstApprovalStep = approvalSteps.get(0);
		if (firstApprovalStep != null) {
			dbLogger.logMultipleRowOperation(ApprovalStep.class, OperationName.CREATE, 
					new SearchArgument[]{new SearchArgument("VACATION_ID", firstApprovalStep.getVacation().getId(), ComparisonCondition.EQ),
							new SearchArgument("STATUS", 1, ComparisonCondition.EQ)});
		}
	}
	
	@CacheEvict(value="approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void deleteListOfApprovalSteps(final List<ApprovalStep> approvalSteps) throws StaleObjectStateException {
		int[] affectedRowsCount = jdbcTemplate.batchUpdate(SQL_CHANGE_STATUS, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ApprovalStep approvalStep = approvalSteps.get(i);
				ps.setInt(1, 0);
				ps.setLong(2, approvalStep.getId());
				ps.setInt(3, approvalStep.getVersion());
			}
			
			@Override
			public int getBatchSize() {
				return approvalSteps.size();
			}
		});
		
		for (int i=0; i < affectedRowsCount.length; i++) {
			if (affectedRowsCount[i] == 0) {
				throw new StaleObjectStateException();
			}
		}
		
		// write information about performed operation to log table	
		dbLogger.logMultipleRowOperation(approvalSteps, OperationName.DELETE);
	}

	/* Do not cache to avoid conflicts */
	//@Cacheable(value="approvalSteps")
	@Override
	public List<ApprovalStep> getWaitingApprovalStepsForVacation(final long vacationID) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_WAITING_APPROVALS_FOR_VACATION,
																	vacationID);
		return createApprovalStepListFromListOfResultMaps(rows);
		
	}

	@Cacheable(value="approvalSteps")
	@Override
	public ApprovalStep getApprovalStepsByVacationIdAndRowNumber(final long vacationID,
			final int rowNumber) {
		ApprovalStep approvalStep = jdbcTemplate.queryForObject(SQL_GET_APPROVAL_STEP_BY_VACATION_AND_ROWNUMBER, 
				new ParameterizedRowMapper<ApprovalStep>(){

					@Override
					public ApprovalStep mapRow(ResultSet rs, int rowNumber)
							throws SQLException {
						
						return createApprovalStepFromResultSet(rs);
					}},
				vacationID,
				rowNumber);
		return approvalStep;
	}

	@Cacheable(value="approvalSteps")
	@Override
	public List<ApprovalStep> getActiveApprovalStepsForApprover(final User user) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ACTIVE_APPROVAL_STEPS_FOR_APPROVER,
				user.getRole().getId(),
				user.getId());
		
		return createApprovalStepListFromListOfResultMaps(rows);
	}

	@Cacheable(value="approvalSteps")
	@Override
	public ApprovalStep getById(final long id) {
		ApprovalStep approvalStep = jdbcTemplate.queryForObject(SQL_GET_APPROVAL_STEP_BY_ID, 
				new ParameterizedRowMapper<ApprovalStep>(){

					@Override
					public ApprovalStep mapRow(ResultSet rs, int rowNumber)
							throws SQLException {
						
						return createApprovalStepFromResultSet(rs);
					}},
				id);

		return approvalStep;
	}

	@Cacheable(value="approvalSteps")
	@Override
	public List<ApprovalStep> getAll() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_APPROVAL_STEPS);
		
		return createApprovalStepListFromListOfResultMaps(rows);
	}

	@Override
	public boolean compareVersions(final ApprovalStep approvalStep) {
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				approvalStep.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}

		return dbVersion == approvalStep.getVersion();
	}

	@Cacheable(value="approvalSteps")
	@Override
	public List<ApprovalStep> getAllApprovalStepsForVacation(final Vacation inVacation) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_APPROVAL_STEPS_FOR_VACATION,
				inVacation.getId());
		
		return createApprovalStepListFromListOfResultMaps(rows);
	}

	@Override
	public ApprovalStep getActiveApprovalStepForVacation(final long vacationId) {
		ApprovalStep approvalStep = null;

		try {
			approvalStep = jdbcTemplate.queryForObject(SQL_GET_ACTIVE_APPROVAL_STEP_FOR_VACATION, 
					new ParameterizedRowMapper<ApprovalStep>(){

				@Override
				public ApprovalStep mapRow(ResultSet rs, int rowNumber)
						throws SQLException {

					return createApprovalStepFromResultSet(rs);
				}},
				vacationId);
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return approvalStep;
	}
	
	@Cacheable(value="approvalSteps")
	@Override
	public List<ApprovalStep> getAllApprovalStepsWithSpecifiedApproverRole(final Role inRole) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER_ROLE,
				inRole.getId());
		
		return createApprovalStepListFromListOfResultMaps(rows);		
	}

	@CacheEvict(value = "approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void deleteApprovalStepsWithSpecifiedApproverRole(final Role role) {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null.");
		}
		
		/*
		 *  We should call log operation before deleting approval steps because
		 *  in other case we will not have criteria to filter old data from new.
		 */
		dbLogger.logMultipleRowOperation(ApprovalStep.class, 
				OperationName.DELETE, 
				new SearchArgument[]{new SearchArgument("role_id", role.getId(), ComparisonCondition.EQ), 
				new SearchArgument("STATUS", 1, ComparisonCondition.EQ)});
		
		jdbcTemplate.update(SQL_DELETE_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER_ROLE, 
				role.getId());
	}

	@Override
	public int getNumberOfApprovalStepsWithSpecifiedApprovalRole(final Role role) {
		if (role == null) {
			throw new IllegalArgumentException("Input role object should not be null.");
		}
		
		return jdbcTemplate.queryForInt(SQL_CALCULATE_NUMBER_OF_APPROVAL_STEPS_WITH_APPROVER_ROLE,
				role.getId());
	}

	@CacheEvict(value = "approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void changeStateOfAllApprovalStepsForVacation(final long vacationId,
			final ApprovalStepState newState, final String comment) {
		
		jdbcTemplate.update(SQL_CHANGE_STATES_FOR_VACATION_WITH_COMMENT,
				newState.toString(),
				comment,
				vacationId);	
	}

	@Override
	public List<ApprovalStep> getAllApprovalStepsWithSpecifiedApprover(final User inApprover) {
		if (inApprover == null) {
			throw new IllegalArgumentException("Input approver object should not be null!");
		}

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER,
				inApprover.getId());
		
		return createApprovalStepListFromListOfResultMaps(rows);	
	}

	@CacheEvict(value = "approvalSteps", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void deleteApprovalStepsWithSpecifiedApprover(final User approver) {
		if (approver == null) {
			throw new IllegalArgumentException("Input approver object should not be null.");
		}
		
		/*
		 *  We should call log operation before deleting approval steps because
		 *  in other case we will not have criteria to filter old data from new.
		 */
		dbLogger.logMultipleRowOperation(ApprovalStep.class, 
				OperationName.DELETE, 
				new SearchArgument[]{new SearchArgument("approver_id", approver.getId(), ComparisonCondition.EQ), 
				new SearchArgument("STATUS", 1, ComparisonCondition.EQ)});
		
		jdbcTemplate.update(SQL_DELETE_APPROVAL_STEPS_WITH_SPECIFIED_APPROVER, 
				approver.getId());
	}
	
	/**
	 * This method converts list of result rows Maps from method queryForList of JdbcTemplate 
	 * to the List of {@link ApprovalStep} objects.
	 * 
	 * @param rows - list of result rows returned from query and represented as Map<String, Object>
	 * @return the list of {@link ApprovalStep} domain objects represented by rows
	 */
	private List<ApprovalStep> createApprovalStepListFromListOfResultMaps(List<Map<String, Object>> rows) {
		List<ApprovalStep> approvalSteps = new ArrayList<ApprovalStep>();
		
		for (Map<String, Object> row: rows) {
			ApprovalStep approvalStep = new ApprovalStep();
			
			long id = (Long) row.get("ID");
			DaoUtil.setObjectId(approvalStep, id);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(approvalStep, version);
			
			approvalStep.setRowNumber((Integer) row.get("ROW_NUMBER"));
			approvalStep.setState(ApprovalStepState.valueOf((String) row.get("STATE")));
			
			Long vacationId = (Long) row.get("VACATION_ID");
			Vacation vacation = null;
			if (vacationId!=null) {
				vacation = vacationDAO.getById(vacationId);
			}
			approvalStep.setVacation(vacation);
			
			Long roleId = (Long) row.get("ROLE_ID");
			Role role = null;
			if (roleId!=null) {
				role = roleDAO.getById(roleId);
			}
			approvalStep.setApproverRole(role);
			
			Long approverId = (Long) row.get("APPROVER_ID");
			User approver = null;
			if (approverId!=null) {
				approver = userDAO.getById(approverId);
			}	
			approvalStep.setApprover(approver);
			
			approvalStep.setComments((String) row.get("COMMENTS"));
			
			approvalSteps.add(approvalStep);
		}
		
		return approvalSteps;
	}
	
	/**
	 * This method creates new {@link ApprovalStep} object using data from {@link ResultSet}.
	 * 
	 * @param rs - {@link ResultSet} that contains approval step's data
	 * @return the new {@link ApprovalStep} object
	 * @throws SQLException
	 */
	private ApprovalStep createApprovalStepFromResultSet(final ResultSet rs) throws SQLException {
		ApprovalStep apprStep = new ApprovalStep();
		
		DaoUtil.setObjectId(apprStep, rs.getLong("ID"));
		
		DaoUtil.setObjectVersion(apprStep, rs.getInt("VERSION"));
		
		apprStep.setState(ApprovalStepState.valueOf(rs.getString("STATE")));
		
		long vacationId = rs.getLong("VACATION_ID");
		Vacation vacation = vacationDAO.getById(vacationId);
		apprStep.setVacation(vacation);
		
		long roleId = rs.getLong("ROLE_ID");
		Role role = roleDAO.getById(roleId);
		apprStep.setApproverRole(role);
		
		long approverId = rs.getLong("APPROVER_ID");
		User approver = userDAO.getById(approverId);
		apprStep.setApprover(approver);
		
		apprStep.setRowNumber(rs.getInt("ROW_NUMBER"));
		
		apprStep.setComments(rs.getString("COMMENTS"));
		
		return apprStep;
	}

}
