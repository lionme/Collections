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
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;

/**
 * {@link RemainingVacationDaysDAO} implementation for work with Spring's JdbcTeplate.
 * 
 * @author dimas
 *
 */
@Repository(value="jdbcVacationDaysDAO")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcRemainingVacationDaysDAO implements RemainingVacationDaysDAO {
	
	/**
	 *  SQL query for inserting new vacation days info to the database.
	 */
	private static final String SQL_INSERT_NEW_RECORD = "insert into remaining_vacation_days (id, two_weeks_vacations, " +
			"one_week_vacations, day_vacations, version) values (?, ?, ?, ?, 0)";
	
	/**
	 *  SQL query for updating vacation days info in the database.
	 */
	private static final String SQL_UPDATE_EXISTED_RECORD = "update remaining_vacation_days set two_weeks_vacations=?, " +
			"one_week_vacations=?, day_vacations=?, version=version+1 where id=? and version=? and status=1";
	
	/**
	 *  SQL query for deleting vacations day record by id.
	 */
	private static final String SQL_DELETE_EXISTED_RECORD = "delete from remaining_vacation_days where id=?";
	
	/**
	 * SQL query for selecting vacation days record by its id.
	 */
	private static final String SQL_GET_VACATION_DAY_BY_ID =
			"select * from remaining_vacation_days where id=? and status=1";
	
	/**
	 * SQL query for fetching all vacation days records from DB.
	 */
	private static final String SQL_GET_ALL_VACATION_DAYS = 
			"select * from remaining_vacation_days where status=1";
	
	/**
	 * SQL query for changing status of the record (status values: 1 - is used; 0 - was deleted)
	 */
	private static final String SQL_CHANGE_STATUS = "update remaining_vacation_days set status=?, version=version+1 where id=? and version=?";
	
	/**
	 * SQL query for getting version by id.
	 */
	private static final String SQL_GET_VERSION =
			"select version from remaining_vacation_days where id=? and status=1";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DBLogger<RemainingVacationDays> dbLogger;
	
	@CacheEvict(value = "remainingVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public RemainingVacationDays save(RemainingVacationDays vacationDays) {
		if (vacationDays.getId() == 0) {
			throw new IllegalArgumentException("ID property should not be zero. It should be equals to the" +
					"user's id.");
		}
		
		jdbcTemplate.update(SQL_INSERT_NEW_RECORD, 
				vacationDays.getId(),
				vacationDays.getTwoWeeksVacations(),
				vacationDays.getOneWeekVacations(),
				vacationDays.getDayVacations());
		
		// Add info about operation to log table
		dbLogger.logBasicOperation(vacationDays, OperationName.CREATE);
		
		return vacationDays;
	}

	@CacheEvict(value = "remainingVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(RemainingVacationDays vacationDays) throws StaleObjectStateException {
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_UPDATE_EXISTED_RECORD, 
					vacationDays.getTwoWeeksVacations(),
					vacationDays.getOneWeekVacations(),
					vacationDays.getDayVacations(),
					vacationDays.getId(),
					vacationDays.getVersion());

			if (affectedRowsCount == 0 && !compareVersions(vacationDays)) {
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
		
		// Add info about operation to log table
		dbLogger.logBasicOperation(vacationDays, OperationName.UPDATE);
	}

	@CacheEvict(value = "remainingVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(RemainingVacationDays vacationDays) throws StaleObjectStateException {
		try {
			int affectedRowsCount = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0,            // set status 0 - unused or deleted
					vacationDays.getId(),
					vacationDays.getVersion());

			if (affectedRowsCount == 0  && !compareVersions(vacationDays)) {
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
		
		// Add info about operation to log table
		dbLogger.logBasicOperation(vacationDays, OperationName.DELETE);
	}
	
	@CacheEvict(value = "remainingVacationDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(RemainingVacationDays vacationDays) {
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(vacationDays, OperationName.REMOVE);
		
		jdbcTemplate.update(SQL_DELETE_EXISTED_RECORD,
				vacationDays.getId());
	}

	@Cacheable(value = "remainingVacationDays")
	@Override
	public RemainingVacationDays getById(long id) {
		RemainingVacationDays vacationDays = null;
		try {
			/*vacationDays = jdbcTemplate.queryForObject(SQL_GET_VACATION_DAY_BY_ID, 
				new BeanPropertyRowMapper<VacationDays>(VacationDays.class),
				id);*/
			
			vacationDays = jdbcTemplate.queryForObject(SQL_GET_VACATION_DAY_BY_ID, 
					new ParameterizedRowMapper<RemainingVacationDays>() {

						@Override
						public RemainingVacationDays mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							RemainingVacationDays vd = new RemainingVacationDays();
							
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

	@Cacheable(value = "remainingVacationDays")
	@Override
	public List<RemainingVacationDays> getAll() {
		List<RemainingVacationDays> listOfRecords = new ArrayList<RemainingVacationDays>();

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_VACATION_DAYS);
		for (Map<String, Object> row: rows) {
			RemainingVacationDays vacationDays = new RemainingVacationDays();
			
			long id = (Long) row.get("ID");
			DaoUtil.setObjectId(vacationDays, id);
			
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
	public boolean compareVersions(RemainingVacationDays vacationDays) {
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				vacationDays.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return dbVersion==vacationDays.getVersion();
	}
	
	

}
