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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;

/**
 * 
 * {@link HolidayDaysDAO} implementation for work with Spring's JdbcTeplate.
 * 
 * @author dimas
 *
 */
@Repository(value="jdbcHolidayDaysDAO")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcHolidayDaysDAO implements HolidayDaysDAO {
	
	/**
	 * SQL query for inserting new non working days record.
	 */
	private static final String SQL_INSERT_RECORD = "insert into holiday_days " +
			"(start_date, end_date, description, version) " +
			"values (?, ?, ?, 0)";

	/**
	 * SQL query for updating non working days record.
	 */
	private static final String SQL_UPDATE_RECORD = "update holiday_days " +
			"set start_date=?, end_date=?, description=?, version=version+1 " +
			"where id=? and version=? and status=1";
	
	/**
	 * SQL query for deleting non working days record.
	 */
	private static final String SQL_DELETE_RECORD = "delete from holiday_days where id=?";
	
	/**
	 * SQL query for fetching non working days record by its id.
	 */
	private static final String SQL_GET_BY_ID = "select * from holiday_days where id=? and status=1";
	
	/**
	 * SQL query for fetching all non working days record.
	 */
	private static final String SQL_GET_ALL_RECORDS = "select * from holiday_days where status=1 order by start_date asc";
	
	/**
	 * SQL query for changing status of the record (status values: 1 - is used; 0 - was deleted)
	 */
	private static final String SQL_CHANGE_STATUS = "update holiday_days set status=?, " +
			"version=version+1 where id=? and version=?";
	
	/**
	 * SQL query for fetching list of records for specified time period.
	 */
	private static final String SQL_GET_RECORDS_FOR_SPECIFIED_PERIOD = "select * from holiday_days where status=1 " +
			"and ((start_date between ? and ?) or (end_date between ? and ?) or (start_date<=? and end_date>=?))";

	/**
	 * SQL query for getting version by id.
	 */
	private static final String SQL_GET_VERSION =
			"select version from holiday_days where id=? and status=1";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DBLogger<HolidayDays> dbLogger;
	
	@Autowired
	private KeyHolderFactory keyHolderFactory;
	
	/**
	 * Saves new non working days record. Id is always generated automatically. If you will set id
	 * by yourself it will have no effect.
	 *  
	 * @param nonWorkingDays - object to be saved.
	 * @return the NonWorkingDays with automatically generated id. 
	 */
	@CacheEvict(value = "holidayDays", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public HolidayDays save(final HolidayDays nonWorkingDays) {
		
		KeyHolder keyHolder = keyHolderFactory.newKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection connection)
					throws SQLException {
				
				PreparedStatement ps = connection.prepareStatement(SQL_INSERT_RECORD, new String[]{"ID"});
				
				ps.setDate(1, DateUtil.convertToSqlDate(nonWorkingDays.getStartDate()));
				ps.setDate(2, DateUtil.convertToSqlDate(nonWorkingDays.getEndDate()));
				ps.setString(3, nonWorkingDays.getDescription());
				
				return ps;
			}
		}, keyHolder);
		
		Long id = (Long) keyHolder.getKey();
		if (id != null) {
			DaoUtil.setObjectId(nonWorkingDays, id);
		} else {
			DaoUtil.setObjectId(nonWorkingDays, -1);
		}
		
		// Add information about this operation to log table.
		dbLogger.logBasicOperation(nonWorkingDays, OperationName.CREATE);
		
		return nonWorkingDays;
	}

	@CacheEvict(value = "holidayDays", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(HolidayDays holidayDays) throws StaleObjectStateException {
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_UPDATE_RECORD, 
					holidayDays.getStartDate(),
					holidayDays.getEndDate(),
					holidayDays.getDescription(),
					holidayDays.getId(),
					holidayDays.getVersion());

			if (affectedRowsNumber == 0 && !compareVersions(holidayDays)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(holidayDays)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about this operation to log table.
		dbLogger.logBasicOperation(holidayDays, OperationName.UPDATE);
	}

	@CacheEvict(value = "holidayDays", allEntries=true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(HolidayDays holidayDays) throws StaleObjectStateException {
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0, // set status 0 - deleted
					holidayDays.getId(),
					holidayDays.getVersion());

			if (affectedRowsNumber == 0 && !compareVersions(holidayDays)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(holidayDays)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about this operation to log table.
		dbLogger.logBasicOperation(holidayDays, OperationName.DELETE);
	}
	
	@CacheEvict(value = "holidayDays", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(HolidayDays holidayDays) {		
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(holidayDays, OperationName.REMOVE);
		
		jdbcTemplate.update(SQL_DELETE_RECORD, 
				holidayDays.getId());
	}

	@Cacheable(value = "holidayDays")
	@Override
	public HolidayDays getById(long id) {
		HolidayDays nonWorkingDays = null;
		try {
			/*nonWorkingDays = jdbcTemplate.queryForObject(SQL_GET_BY_ID, 
				new BeanPropertyRowMapper<HolidayDays>(HolidayDays.class),
				id);*/
			
			nonWorkingDays = jdbcTemplate.queryForObject(SQL_GET_BY_ID, 
					new ParameterizedRowMapper<HolidayDays>() {
						@Override
						public HolidayDays mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							HolidayDays holidayDays = new HolidayDays();
							
							long userId = rs.getLong("ID");
							DaoUtil.setObjectId(holidayDays, userId);
							
							int version = rs.getInt("VERSION");
							DaoUtil.setObjectVersion(holidayDays, version);
							
							holidayDays.setStartDate(rs.getDate("START_DATE"));
							holidayDays.setEndDate(rs.getDate("END_DATE"));
							holidayDays.setDescription(rs.getString("DESCRIPTION"));
							
							return holidayDays;
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
				
		return nonWorkingDays;
	}

	@Cacheable(value = "holidayDays")
	@Override
	public List<HolidayDays> getAll() {
		List<HolidayDays> fetchedItems = new ArrayList<HolidayDays>();
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_RECORDS);
		
		for (Map<String, Object> row: rows) {
			HolidayDays nonWorkingDays = new HolidayDays();
			
			long id = (Long) row.get("ID");
			DaoUtil.setObjectId(nonWorkingDays, id);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(nonWorkingDays, version);
			
			nonWorkingDays.setStartDate((Date) row.get("START_DATE"));
			nonWorkingDays.setEndDate((Date) row.get("END_DATE"));
			nonWorkingDays.setDescription((String) row.get("DESCRIPTION"));
			
			fetchedItems.add(nonWorkingDays);
		}
		
		return fetchedItems;
	}

	@Cacheable(value = "holidayDays")
	@Override
	public List<HolidayDays> getForPeriod(Date startDate, Date endDate) {
		List<HolidayDays> fetchedItems = new ArrayList<HolidayDays>();
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_RECORDS_FOR_SPECIFIED_PERIOD,
																	DateUtil.removeTimePart(startDate),
																	DateUtil.removeTimePart(endDate),
																	DateUtil.removeTimePart(startDate),
																	DateUtil.removeTimePart(endDate),
																	DateUtil.removeTimePart(startDate),
																	DateUtil.removeTimePart(endDate));
		
		for (Map<String, Object> row: rows) {
			HolidayDays holidayDays = new HolidayDays();
			
			long id = (Long) row.get("ID");
			DaoUtil.setObjectId(holidayDays, id);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(holidayDays, version);
			
			holidayDays.setStartDate((Date) row.get("START_DATE"));
			holidayDays.setEndDate((Date) row.get("END_DATE"));
			holidayDays.setDescription((String) row.get("DESCRIPTION"));
			
			fetchedItems.add(holidayDays);
		}
		
		return fetchedItems;
	}

	@Override
	public boolean compareVersions(HolidayDays holidayDays) {
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				holidayDays.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return dbVersion==holidayDays.getVersion();
	}

}
