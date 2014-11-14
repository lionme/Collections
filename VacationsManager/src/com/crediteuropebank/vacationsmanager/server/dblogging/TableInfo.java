package com.crediteuropebank.vacationsmanager.server.dblogging;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 
 * <p>This class is used to hold information about table for logging purposes. Such information 
 * is hold:</p>
 * <ul>
 * 		<li>base table name;</li>
 * 		<li>log table name;</li>
 * 		<li>the list of base table columns name (columns with same names should exist in 
 * 			log table);</li>
 * </ul>
 * 
 * @author dimas
 *
 */
public class TableInfo {
	private final String tableName;
	private final String logTableName;
	private final List<String> columnsNames;

	private TableInfo(String tableName, String logTableName, List<String> columnNames) {
		this.tableName = tableName;
		this.logTableName = logTableName;
		this.columnsNames = columnNames;
	}

	public String getTableName() {
		return tableName;
	}

	public String getLogTableName() {
		return logTableName;
	}

	public List<String> getColumnsNames() {
		return columnsNames;
	}

	/**
	 * Factory method that creates new {@link TableInfo} object.
	 * 
	 * @param tableName
	 * @param logTableName
	 * @param columnNames
	 * @return new {@link TableInfo} object.
	 */
	public static TableInfo newTableInfo(String tableName, String logTableName, String[] columnNames) {
		List<String> columnsNamesList = new ArrayList<String>(Arrays.asList(columnNames));
		
		TableInfo tableInfo = new TableInfo(tableName, logTableName, columnsNamesList);
		
		return tableInfo;
	}
	
	/**
	 * 
	 * This method creates and returns TableInfo object using class object of the domain class.
	 * 
	 * But note that in case of using this method domainClass should be annotated using {@link Domain} annotation and
	 * it's fields should be annotated using {@link Column} annotation (annotate only fields that will be logged in
	 * the log table). If annotation is missed - {@link MissedAnnotationException} exception is thrown.
	 * 
	 * @param domainObjectClass
	 * @return new {@link TableInfo} object.
	 * @throws MissedAnnotationException if domain class have not been annotated using {@link Domain} annotation.
	 */
	public static TableInfo newTableInfo(Class<?> domainObjectClass) {
		
		Domain domainAnnotation = domainObjectClass.getAnnotation(Domain.class);
		
		if (domainAnnotation == null) {
			throw new MissedAnnotationException("You forget to annotate domain class for logging purposes.");
		}
		
		/*
		 * Get table and log table names.
		 */
		String tableName = domainAnnotation.tableName();
		String logTableName = domainAnnotation.logTableName();
		
		Field[] fields = domainObjectClass.getDeclaredFields();
		Class<?> superClass = domainObjectClass.getSuperclass();

		while ( (superClass != null) && (!superClass.equals(Object.class)) ) {
			Field[] superclassFields = superClass.getDeclaredFields();

			int sumLength = fields.length + superclassFields.length;

			int baseLength = fields.length;

			// Create one array that contains fields for class and its superclass. 
			fields = Arrays.copyOf(fields, sumLength);  
			System.arraycopy(superclassFields, 0, fields, baseLength, superclassFields.length);
			
			superClass = superClass.getSuperclass();
		}

		final List<String> columnsNamesList = new ArrayList<String>();

		for (int i=0; i<fields.length; i++) {
			Field field = fields[i];

			Column columnAnnotation = field.getAnnotation(Column.class);

			if (columnAnnotation != null) {
				columnsNamesList.add(columnAnnotation.columnName());
			}
		}

		
		TableInfo tableInfo = new TableInfo(tableName, logTableName, columnsNamesList);
		
		return tableInfo;		
	}
}
