package com.crediteuropebank.vacationsmanager.server.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;

/**
 * 
 * Concrete {@link UsedVacationDaysDAO} implementation for using with spring's JdbcTemplate.
 * 
 * @author DIMAS
 *
 */
@Repository(value="jdbcUsedVacationDaysDAO")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcUsedVacationDays implements UsedVacationDaysDAO{
	
	/**
	 *  SQL query for inserting new vacation days info to the database.
	 */
	private static final String SQL_INSERT_NEW_RECORD = "insert into used_vacation_days(id, two_weeks_vacations, " +
			"one_week_vacations, day_vacations, version) values (?, ?, ?, ?, 0)";
	
	/**
	 *  SQL query for updating vacation days info in the database.
	 */
	private static final String SQL_UPDATE_EXISTED_RECORD = "update used_vacation_days " +
			"set two_weeks_vacations=?, one_week_vacations=?, day_vacations=?, version=version+1 " +
			"where id=? and version=? and status=1";
	
	/**
	 *  SQL query for deleting vacations day record by id.
	 */
	private static final String SQL_DELETE_EXISTED_RECORD = 
			"delete from used_vacation_days where id=?";
	
	/**
	 * SQL query for selecting vacation days record by its id.
	 */
	private static final String SQL_GET_VACATION_DAY_BY_ID =
			"select * from used_vacation_days where id=? and status=1";
	
	/**
	 * SQL query for fetching all vacation days records from DB.
	 */
	private static final String SQL_GET_ALL_VACATION_DAYS = 
			"select * from used_vacation_days where status=1";
	
	/**
	 * SQL query for changing status of the record (status values: 1 - is used; 0 - was deleted)
	 */
	private static final String SQL_CHANGE_STATUS = "update used_vacation_days " +
			"set status=?, version=version+1 where id=? and version=?";
	
	/**
	 * SQL query for getting version by id.
	 */
	private static final String SQL_GET_VERSION =
			"select version from used_vacation_days where id=? and status=1";	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DBLogger<UsedVacationDays> dbLogger;

	@CacheEvict(value = "usedVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public UsedVacationDays save(UsedVacationDays vacationDays) {
		if (vacationDays.getId() == 0) {
			throw new IllegalArgumentException("ID property should not be zero. It should be equals to the" +
					"vacation's id.");
		}
		
		jdbcTemplate.update(SQL_INSERT_NEW_RECORD, 
				vacationDays.getId(),
				vacationDays.getTwoWeeksVacations(),
				vacationDays.getOneWeekVacations(),
				vacationDays.getDayVacations());
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(vacationDays, OperationName.CREATE);
		
		return vacationDays;
	}

	@CacheEvict(value = "usedVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(UsedVacationDays vacationDays) throws StaleObjectStateException {
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_UPDATE_EXISTED_RECORD, 
					vacationDays.getTwoWeeksVacations(),
					vacationDays.getOneWeekVacations(),
					vacationDays.getDayVacations(),
					vacationDays.getId(),
					vacationDays.getVersion());

			if (affectedRowsNumber == 0 && !compareVersions(vacationDays)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(vacationDays)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(vacationDays, OperationName.UPDATE);
	}

	@CacheEvict(value = "usedVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(UsedVacationDays vacationDays) throws StaleObjectStateException {
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0, // set status deleted
					vacationDays.getId(),
					vacationDays.getVersion());

			if (affectedRowsNumber == 0 && !compareVersions(vacationDays)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(vacationDays)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(vacationDays, OperationName.DELETE);
	}

	@CacheEvict(value = "usedVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(UsedVacationDays vacationDays) {
		
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(vacationDays, OperationName.REMOVE);
		
		jdbcTemplate.update(SQL_DELETE_EXISTED_RECORD, 
				vacationDays.getId());
	}

	@Cacheable(value = "usedVacationDays")
	@Override
	public UsedVacationDays getById(long id) {
		UsedVacationDays vacationDays = null;
		try {
			vacationDays = jdbcTemplate.queryForObject(SQL_GET_VACATION_DAY_BY_ID, 
					new ParameterizedRowMapper<UsedVacationDays>() {

						@Override
						public UsedVacationDays mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							UsedVacationDays vd = new UsedVacationDays();
							
							DaoUtil.setObjectId(vd, rs.getLong("ID"));
							
							DaoUtil.setObjectVersion(vd, rs.getInt("VERSION"));
							
							vd.setTwoWeeksVacations(rs.getInt("TWO_WEEKS_VACATIONS"));
							vd.setOneWeekVacations(rs.getInt("ONE_WEEK_VACATIONS"));
							
							BigDecimal dayVacations = rs.getBigDecimal("DAY_VACATIONS").setScale(BigDecimalUtil.SCALE);
							vd.setDayVacations(dayVacations);
							
							return vd;
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
				
		return vacationDays;
	}

	@Cacheable(value = "usedVacationDays")
	@Override
	public List<UsedVacationDays> getAll() {
		List<UsedVacationDays> listOfRecords = new ArrayList<UsedVacationDays>();

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_VACATION_DAYS);
		for (Map<String, Object> row: rows) {
			UsedVacationDays vacationDays = new UsedVacationDays();
			
			long userId = (Long) row.get("ID");
			DaoUtil.setObjectId(vacationDays, userId);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(vacationDays, version);
			
			vacationDays.setTwoWeeksVacations((Integer) row.get("TWO_WEEKS_VACATIONS"));
			vacationDays.setOneWeekVacations((Integer) row.get("ONE_WEEK_VACATIONS"));
			
			BigDecimal dayVacations = (BigDecimal) row.get("DAY_VACATIONS");
			dayVacations = dayVacations.setScale(BigDecimalUtil.SCALE);
			vacationDays.setDayVacations(dayVacations);
			
			listOfRecords.add(vacationDays);			
		}
		
		return listOfRecords;	
	}
	
	@Override
	public boolean compareVersions(UsedVacationDays usedVacationDays) {
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				usedVacationDays.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return dbVersion==usedVacationDays.getVersion();
	}
}
