package com.crediteuropebank.vacationsmanager.server.dblogging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.logging.InjectLogger;
import com.crediteuropebank.vacationsmanager.shared.domain.BaseDomain;

/**
 * 
 * <p>This class is used for logging DB CRUD operations. The algorithm of work of all methods in this class is next:</p>
 * <ol>
 * 		<li>
 * 			Select affected row (or few rows) from DB using search criteria. This criteria can be
 * 			id of the record, the list of id's or some extra criteria specified explicitly
 * 			using {@link SearchArgument} class.
 * 		</li>
 * 		<li>
 * 			Write fetched data to log table with additional specific log data (such as operation 
 * 			user, operation name (CREATE, UPDATE, DELETE and etc.), operation type).
 * 		</li>
 * </ol>
 * <p>For more details look at the methods that execute logging (their name start from "log" 
 * prefix).</p>
 * 
 * @author DIMAS
 *
 * @param <T> - the type of the domain object
 */
@Component("dbLogger")
@Scope(value="prototype")
public class DBLogger<T extends BaseDomain> {
	
	/**
	 * 
	 * Custom row mapper that maps fetched data to the map. It is used for fetching single row.
	 * 
	 * @author DIMAS
	 *
	 */
	private class SingleRowMapper implements ParameterizedRowMapper<Map<String, Object>> {

		private final List<String> columnsNames;

		/**
		 * @return the map that holds column name/ column value pairs.
		 */
		@Override
		public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
			Map<String, Object> columnsMap = new HashMap<String, Object>(); 

			for (String columnName: columnsNames) {
				Object columnValue = rs.getObject(columnName);
				columnsMap.put(columnName, columnValue);
			}

			return columnsMap;
		}

		public SingleRowMapper(List<String> columnsNames) {
			this.columnsNames = columnsNames;			
		}

	}
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 
	 * This method is used to log simple single row CRUD operation.
	 * 
	 * @param domainObject - domain object under which operation is made.
	 * @param operationName - enum constant that characterized operation.
	 */
	@Transactional(propagation=Propagation.MANDATORY, readOnly=false, rollbackFor=Exception.class)
	public void logBasicOperation(T domainObject, OperationName operationName) {

		try {
			long id = domainObject.getId();
			
			SearchArgument[] searchArguments = new SearchArgument[] {new SearchArgument("ID", id, ComparisonCondition.EQ)};
			TableInfo tableInfo = TableInfo.newTableInfo(domainObject.getClass());
			
			buildAndExecuteLogQuery(tableInfo, operationName, AffectedRows.ONE, searchArguments);
		} catch (Exception e) {
			/*
			 * If any error occurs during logging of the DB operation just log the error but
			 * don't throw it. In other case all operations will be rolled back because
			 * of error during logging.
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("The logging of the DB ");
			sb.append(operationName.toString());
			sb.append(" operation under ");
			sb.append(domainObject.getClass().getName());
			sb.append(" domain object was failed. Reason:");
			
			logger.error(sb.toString(), e);
			
			// From log method no one error should be thrown
			// throw new RuntimeException(e);
		}
	}

	/**
	 * This method is used to log CRUD operation that affects few rows. It gives a possibility 
	 * to define search criteria using which operation will be logged.
	 * 
	 * @param domainClass - domain object's class for which DB operation is made
	 * @param operationName - enum constant that specifies operation name (CREATE, DELETE and etc.).
	 * @param searchArguments - the array of select criteria necessary for fetching info 
	 * 							from base table after operation have been done.
	 */
	@Transactional(propagation=Propagation.MANDATORY, readOnly=false, rollbackFor=Exception.class)
	public void logMultipleRowOperation(final Class<?> domainClass, final OperationName operationName,
			final SearchArgument[] searchArguments) {
		try {
			TableInfo tableInfo = TableInfo.newTableInfo(domainClass);
			buildAndExecuteLogQuery(tableInfo, operationName, AffectedRows.MANY, searchArguments);
		} catch (Exception e) {
			/*
			 * If any error occurs during logging of the DB operation just log the error but
			 * don't throw it. In other case all operations will be rolled back because
			 * of error during logging.
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("The logging of the DB ");
			sb.append(operationName.toString());
			sb.append(" operation under ");
			sb.append(domainClass.getName());
			sb.append(" domain object was failed. Reason:");
			
			logger.error(sb.toString(), e);
			
			// From log method no one error should be thrown
			// throw new RuntimeException(e);
		}
	}
	
	/**
	 * This method is used to log CRUD operation that affects few rows. This method searches by 
	 * the list of domain objects id's.
	 * 
	 * @param domainClass
	 * @param operationName
	 * @param searchArguments
	 * 
	 * @throws IllegalArgumentException if input domain objects list is empty or one of the domain object's ids is 0.
	 */
	@Transactional(propagation=Propagation.MANDATORY, readOnly=false, rollbackFor=Exception.class)
	public void logMultipleRowOperation(final List<T> domainObjectsList, final OperationName operationName) {
		Class<? extends BaseDomain> domainClass = null;
		try {
			if (domainObjectsList == null || domainObjectsList.size() == 0) {
				throw new IllegalArgumentException("Domain objects list is empty!");
			}
			
			domainClass = domainObjectsList.get(0).getClass();
			
			StringBuilder idsString = new StringBuilder();
			idsString.append("(");
			for (BaseDomain domainObj: domainObjectsList) {
				if (domainObj.getId() == 0) {
					throw new IllegalArgumentException("One of the domain object's ids is 0 (not set)!");
				}
				
				idsString.append(domainObj.getId());
				idsString.append(", ");
			}
			idsString.delete(idsString.length() - 2, idsString.length());
			idsString.append(")");
			
			SearchArgument searchArgument = new SearchArgument("ID", idsString.toString(), ComparisonCondition.IN);
			
			TableInfo tableInfo = TableInfo.newTableInfo(domainClass);
			buildAndExecuteLogQuery(tableInfo, operationName, AffectedRows.MANY, new SearchArgument[]{searchArgument});
		} catch (Exception e) {
			/*
			 * If any error occurs during logging of the DB operation just log the error but
			 * don't throw it. In other case all operations will be rolled back because
			 * of error during logging.
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("The logging of the DB ");
			sb.append(operationName.toString());
			sb.append(" operation under ");
			sb.append(domainClass.getName());
			sb.append(" domain object was failed. Reason:");
			
			logger.error(sb.toString(), e);
			
			// From log method no one error should be thrown
			// throw new RuntimeException(e);
		}
	}

	/**
	 * This method should be used in case in the case of complicated operation if all other 
	 * method doesn't suit your requirements.
	 * 
	 * @param tableInfo
	 * @param operationName
	 * @param affectedrows
	 * @param searchArguments
	 */
	@Transactional(propagation=Propagation.MANDATORY, readOnly=false, rollbackFor=Exception.class)
	public void logComplexOperation(final TableInfo tableInfo,
			final OperationName operationName, final AffectedRows affectedrows, final SearchArgument[] searchArguments) {
		try {
			buildAndExecuteLogQuery(tableInfo, operationName, affectedrows, searchArguments);
		} catch (Exception e) {
			/*
			 * If any error occurs during logging of the DB operation just log the error but
			 * don't throw it. In other case all operations will be rolled back because
			 * of error during logging.
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("The logging of the DB ");
			sb.append(operationName.toString());
			sb.append(" operation for relation table ");
			sb.append(tableInfo.getTableName());
			sb.append(" was failed. Reason:");
			
			logger.error(sb.toString(), e);
			
			// From log method no one error should be thrown
			//throw new RuntimeException(e);
		}
	}
	
	/**
	 * This method builds select query for base table, prepares some data for logging table and 
	 * writes data to log table.
	 * 
	 * @param tableInfo
	 * @param operationName
	 * @param affectedRows
	 * @param searchArguments
	 */
	private void buildAndExecuteLogQuery(final TableInfo tableInfo, final OperationName operationName,
				final AffectedRows affectedRows, SearchArgument[] searchArguments) {
		
		if (tableInfo.getColumnsNames().size() == 0) {
			throw new IllegalArgumentException("No columns where defined for logging!");
		}
		
		StringBuilder columnsString = new StringBuilder();
		
		for (String columnName: tableInfo.getColumnsNames()) {
			columnsString.append(columnName);
			columnsString.append(", ");
		}
		
		/* Remove superfluous dot and space at the end of the string.  */
		if (columnsString.length() > 2) {
			columnsString.delete(columnsString.length()-2, columnsString.length());
		}
		
		/* Create select query for fetching last chnages. */
		StringBuilder selectQueryText = new StringBuilder();
		selectQueryText.append("select ");
		selectQueryText.append(columnsString);
		selectQueryText.append(" from ");
		selectQueryText.append(tableInfo.getTableName());
		// Create where condition if necessary
		if (searchArguments.length >0) {
			selectQueryText.append(" where ");

			for (int i=0; i< searchArguments.length; i++) {
				selectQueryText.append(searchArguments[i].getColumnName());
				selectQueryText.append(searchArguments[i].getCondition().operation());
				selectQueryText.append(searchArguments[i].getArgumentValue());
				selectQueryText.append(" and ");
			}

			selectQueryText.delete(selectQueryText.length()-5, selectQueryText.length());
		}
		
		Map<String, Object> baseColumnsMap = new HashMap<String, Object>();
		baseColumnsMap.put("OPERATION", operationName.toString());
		baseColumnsMap.put("OPERATION_TIME", new Date());
		baseColumnsMap.put("OPERATION_USER", getLoggedInUserName());
		
		if (affectedRows == AffectedRows.ONE) {
			selectSingleRowAndWriteToLog(tableInfo, selectQueryText.toString(), baseColumnsMap);
		} else if (affectedRows == AffectedRows.MANY) {
			selectMultipleRowsAndWriteToLog(tableInfo, selectQueryText.toString(), baseColumnsMap);
		}else {
			throw new IllegalArgumentException("OPeration is not supported.");
		}
	}
	
	/**
	 * Executes select statement and write data to log. This method should be used for logging 
	 * only operations that affects single row in DB.
	 * 
	 * @param tableInfo
	 * @param selectQueryText
	 * @param baseColumnsMap
	 */
	private void selectSingleRowAndWriteToLog(final TableInfo tableInfo, final String selectQueryText, 
			final Map<String, Object> baseColumnsMap) {
		
		/*
		 * Create row mapper.
		 */
		SingleRowMapper rowMapper = new SingleRowMapper(tableInfo.getColumnsNames());
		
		/*
		 * Executes select query for fetching last changes in DB.
		 */
		Map<String, Object> columnsMap = jdbcTemplate.queryForObject(selectQueryText.toString(),
				rowMapper);
		
		columnsMap.putAll(baseColumnsMap);
		
		/* Prepare columns list string and values list string. */
		StringBuilder insertColumnList = new StringBuilder();
		StringBuilder valuesList = new StringBuilder();
		
		Object[] insertedValues = new Object[columnsMap.size()];
		int j=0;
		Set<String> columnsNamesSet = columnsMap.keySet();
		for (String columnName: columnsNamesSet) {
			insertColumnList.append(columnName);
			insertColumnList.append(", ");
			
			insertedValues[j] = columnsMap.get(columnName);
					
			valuesList.append("?");
			valuesList.append(", ");
			
			j++;
		}

		/* Remove superfluous dot and space at the end.  */
		insertColumnList.delete(insertColumnList.length()-2, insertColumnList.length());
		valuesList.delete(valuesList.length()-2, valuesList.length());
		
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("insert into ");
		insertQuery.append(tableInfo.getLogTableName());
		insertQuery.append(" (");
		insertQuery.append(insertColumnList);
		insertQuery.append(") values (");
		insertQuery.append(valuesList);
		insertQuery.append(")");
		
		jdbcTemplate.update(insertQuery.toString(), 
				insertedValues);
		
	}
	
	/**
	 * Executes select statement and write data to log. This method should be used for logging 
	 * operations that affects multiple rows in DB.
	 * 
	 * @param tableInfo
	 * @param selectQueryText
	 * @param baseColumnsMap
	 */
	private void selectMultipleRowsAndWriteToLog(final TableInfo tableInfo, final String selectQueryText, 
			final Map<String, Object> baseColumnsMap) {
		
		final List<Map<String, Object>> listOfColumnsMaps = jdbcTemplate.queryForList(selectQueryText);
		
		// For some operations it is possible do not affect any row.
		/*if (listOfColumnsMaps.size() == 0) {
			throw new IllegalStateException("No data for logging was found.");
		}*/
		
		Set<String> baseColumnsSet = baseColumnsMap.keySet();
		String[] baseColumnsArray = baseColumnsSet.toArray(new String[]{});
		
		String[] additionalColumnsArray = tableInfo.getColumnsNames().toArray(new String[]{});
		
		// calculate sum length of the both arrays.
		int length = baseColumnsArray.length + additionalColumnsArray.length;
		final String[] columnsArray = Arrays.copyOf(baseColumnsArray, length);
		
		System.arraycopy(additionalColumnsArray, 0, columnsArray, baseColumnsArray.length, additionalColumnsArray.length);
		
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("insert into ");
		insertQuery.append(tableInfo.getLogTableName());
		insertQuery.append(" (");
		
		StringBuilder columnsString = new StringBuilder();
		StringBuilder valuesString = new StringBuilder();
		
		for (int i=0; i<columnsArray.length; i++) {
			columnsString.append(columnsArray[i]);
			columnsString.append(", ");
			
			valuesString.append("?, ");
		}
		// Remove superfluous comma and space at the end of the strings
		columnsString.delete(columnsString.length()-2, columnsString.length());
		valuesString.delete(valuesString.length()-2, valuesString.length());
		
		insertQuery.append(columnsString);
		insertQuery.append(") values (");
		insertQuery.append(valuesString);
		insertQuery.append(")");
		
		jdbcTemplate.batchUpdate(insertQuery.toString(), new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map<String, Object> fetchedRow = listOfColumnsMaps.get(i);
				
				// Add to current columns map another map with columns base for all log tables
				fetchedRow.putAll(baseColumnsMap);
				
				for (int j=0; j<columnsArray.length; j++) {
					Object columnValue = fetchedRow.get(columnsArray[j]);
					ps.setObject(j+1, columnValue);
				}
			}
			
			@Override
			public int getBatchSize() {
				return listOfColumnsMaps.size();
			}
		});
	}
	
	/**
	 * This method gets the name of the logged in user using Spring Security.
	 * 
	 * @return the username of the logged in user.
	 */
	private String getLoggedInUserName() {
		//Get username from Spring Security
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		/*List<Object> principals = sessionRegistry.getAllPrincipals();
		
		List<String> usersNamesList = new ArrayList<String>();
		
		for (Object principal: principals) {
			if (principal instanceof User) {
				usersNamesList.add(((User) principal).getUsername());
			}
		}*/

		String username = "";
		if (authentication != null) {
			username = ((User)authentication.getPrincipal()).getUsername();
		}
		
		return username;
	}
	
}
