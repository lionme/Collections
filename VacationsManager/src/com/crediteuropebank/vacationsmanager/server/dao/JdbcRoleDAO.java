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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.dblogging.DBLogger;
import com.crediteuropebank.vacationsmanager.server.dblogging.OperationName;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;

/**
 * A {@link RoleDAO} implementation for case of using JDBCTemplate for hitting database.
 * 
 * I made all queries text constants visibility to "package" in aim to use them in unit tests.
 * 
 * @author DIMAS
 *
 */
@Repository(value="jdbcRoleDAO")
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
public class JdbcRoleDAO implements RoleDAO {

	/**
	 *  SQL query for inserting new role to the database.
	 */
	static final String SQL_INSERT_ROLE = "insert into roles(name, description, parent_role_id, privilege, version)" +
			" values (?, ?, ?, ?, 0)";
	
	/**
	 *  SQL query for updating existed role to the database.
	 */
	static final String SQL_UPDATE_ROLE = "update roles set name=?, description=?, " +
			"parent_role_id=?, privilege=?, version=version+1 where id=? and version=? and status=1";

	/**
	 * SQL query for deleting role from database.
	 */
	static final String SQL_DELETE_ROLE =
			"delete from roles where id=?";
	
	/**
	 * SQL query for selecting single role by its id.
	 */
	static final String SQL_GET_ROLE_BY_ID =
			"select * from roles where id=? and status=1";
	
	/**
	 * SQL query for selecting all roles from table.
	 */
	static final String SQL_GET_ALL_ROLES = 
			"select * from roles where status=1 order by name";
	
	/**
	 * SQL query for changing status of the record (status values: 1 - is used; 0 - was deleted)
	 */
	static final String SQL_CHANGE_STATUS = "update roles set status = ?, version=version+1 " +
			"where id=? and version=?";
	
	/**
	 * SQL query for searching child roles for parent role.
	 */
	static final String SQL_FIND_CHILD_ROLES = "select * from roles where parent_role_id=? and status=1";
	
	/**
	 * SQL query for getting version by id.
	 */
	static final String SQL_GET_VERSION =
			"select version from roles where id=? and status=1";
	
	/**
	 * SQL query for fetching role by its name (which is unique in the context of the application).
	 */
	static final String SQL_GET_ROLE_BY_NAME =
			"select * from roles where status=1 and name=?";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DBLogger<Role> dbLogger;
	
	@Autowired
	private KeyHolderFactory keyHolderFactory;
	
	/**
	 * @throws DublicateEntryException if object with specified unique for application field already 
	 * 				exists in DB.
	 */
	@CacheEvict(value = "roles", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public Role save(final Role role) {
		
		if (this.getRoleByName(role.getName()) != null) {
			throw new DublicateEntryException("Record with specified role name already exists in application.");
		}
		
		KeyHolder keyHolder =keyHolderFactory.newKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection connection)
					throws SQLException {
				
				PreparedStatement ps = connection.prepareStatement(SQL_INSERT_ROLE, new String[]{"ID"});
				
				ps.setString(1, role.getName());
				ps.setString(2, role.getDesription());
				ps.setObject(3, (role.getParentRole()!=null)?role.getParentRole().getId():null);
				ps.setString(4, role.getPrivilege().toString());
				
				return ps;
			}
		}, keyHolder);
		
		Long id = (Long) keyHolder.getKey();
		if (id != null) {
			DaoUtil.setObjectId(role, id);
		} else {
			DaoUtil.setObjectId(role, -1);
		}
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(role, OperationName.CREATE);
		
		return role;
	}

	@CacheEvict(value = "roles", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void update(final Role role) throws StaleObjectStateException {
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_UPDATE_ROLE, 
					role.getName(),
					role.getDesription(),
					(role.getParentRole()!=null)?role.getParentRole().getId():null,
					role.getPrivilege().toString(),
					role.getId(),
					role.getVersion());

			if (affectedRowsNumber == 0 && !compareVersions(role)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(role)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(role, OperationName.UPDATE);
	}

	@CacheEvict(value = "roles", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void delete(final Role role) throws StaleObjectStateException {
		
		try {
			int affectedRowsNumber = jdbcTemplate.update(SQL_CHANGE_STATUS,
					0,   // set 0 status - deleted
					role.getId(),
					role.getVersion());

			if (affectedRowsNumber == 0 && !compareVersions(role)) {
				throw new StaleObjectStateException();
			}
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no approval steps with active state was fount.
			if (e.getActualSize() == 0 && !compareVersions(role)) {
				throw new StaleObjectStateException();
			} else {
				throw e;
			}
		}
		
		// Add information about operation to log table
		dbLogger.logBasicOperation(role, OperationName.DELETE);
	}
	
	@CacheEvict(value = "roles", allEntries = true)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	@Override
	public void removeFromDB(final Role role) {
		
		/* 
		 * Execute logging to DB before performing the operation because after operation data
		 * will be removed from DB. Make last snapshot of the DB row.
		 */
		dbLogger.logBasicOperation(role, OperationName.REMOVE);
		
		jdbcTemplate.update(SQL_DELETE_ROLE, 
				role.getId());
	}

	@Cacheable(value = "roles")
	@Override
	public Role getById(final long id) {
		Role role = null;
		
		try {
			role = jdbcTemplate.queryForObject(SQL_GET_ROLE_BY_ID, 
					new ParameterizedRowMapper<Role>() {

						@Override
						public Role mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							Role role = new Role();
							
							DaoUtil.setObjectId(role, rs.getLong("ID"));
							
							DaoUtil.setObjectVersion(role, rs.getInt("VERSION"));
							
							role.setName(rs.getString("NAME"));
							role.setDesription(rs.getString("DESCRIPTION"));
							
							long parentRoleId = rs.getLong("PARENT_ROLE_ID");
							role.setParentRole((parentRoleId!=0)?getById(parentRoleId):null);
							
							Privilege privilege = Privilege.valueOf(rs.getString("PRIVILEGE"));
							role.setPrivilege(privilege);
							
							return role;
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
		
		return role;
	}

	@Cacheable(value = "roles")
	@Override
	public List<Role> getAll() {
		List<Role> roles = new ArrayList<Role>();
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_GET_ALL_ROLES);
		for (Map<String, Object> row: rows) {
			Role role = new Role();
			
			long id = (Long) row.get("ID");
			DaoUtil.setObjectId(role, id);
			
			int version = (Integer) row.get("VERSION");
			DaoUtil.setObjectVersion(role, version);
			
			role.setName((String) row.get("NAME"));
			role.setDesription((String) row.get("DESCRIPTION"));
			
			Long parentRoleId = (Long) row.get("PARENT_ROLE_ID");
			role.setParentRole((parentRoleId!=null)?getById(parentRoleId):null);
			
			Privilege privilege = Privilege.valueOf((String) row.get("PRIVILEGE"));
			role.setPrivilege(privilege);
			
			roles.add(role);
		}
		
		return roles;
	}

	@Override
	public int getChildRolesAmount(final long parentRoleId) {

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_FIND_CHILD_ROLES, parentRoleId);

		return rows.size();
	}

	@Override
	public boolean compareVersions(final Role role) {
		int dbVersion = -1;
		
		try {
			dbVersion = jdbcTemplate.queryForInt(SQL_GET_VERSION,
				role.getId());
		}catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return dbVersion==role.getVersion();
	}

	@Cacheable(value = "roles")
	@Override
	public Role getRoleByName(final String roleName) {
		Role role = null;
		
		try {
			role = jdbcTemplate.queryForObject(SQL_GET_ROLE_BY_NAME, 
					new ParameterizedRowMapper<Role>() {

						@Override
						public Role mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							Role role = new Role();
							
							DaoUtil.setObjectId(role, rs.getLong("ID"));
							
							DaoUtil.setObjectVersion(role, rs.getInt("VERSION"));
							
							role.setName(rs.getString("NAME"));
							role.setDesription(rs.getString("DESCRIPTION"));
							
							long parentRoleId = rs.getLong("PARENT_ROLE_ID");
							role.setParentRole((parentRoleId!=0)?getById(parentRoleId):null);
							
							Privilege privilege = Privilege.valueOf(rs.getString("PRIVILEGE"));
							role.setPrivilege(privilege);
							
							return role;
						}
					},
					roleName);
		} catch(EmptyResultDataAccessException e) {
			// Check whether the reason of error is that no users with specified id not found,
			// in other case throw exception further
			if (e.getActualSize()>0) {
				throw e;
			}
		}
		
		return role;
	}

	
}
