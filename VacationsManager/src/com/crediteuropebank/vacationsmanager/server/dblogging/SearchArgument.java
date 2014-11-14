package com.crediteuropebank.vacationsmanager.server.dblogging;

/**
 * 
 * <p>This class is used for defining of the search criteria for logging operation.</p>
 * <p>For example such definition:</p>
 * <i>{@code new SearchArgument("ID", 1, ComparisonCondition.EQ)}</i>
 * <p>is equivalent to such peace of SQL code:</p>
 * <i>{@code ID=1}</i>
 * <p>As a result all search arguments specified for log operation will be used during
 * building of the query.</p>
 * 
 * @author dimas
 *
 */
public class SearchArgument {

	private final String columnName;
	private final Object argumentValue;
	private final ComparisonCondition condition;
	
	/**
	 * Constructs new search argument with specified 
	 * 
	 * @param columnName
	 * @param argumentValue
	 * @param condition
	 */
	public SearchArgument(final String columnName, final Object argumentValue, final ComparisonCondition condition) {
		this.columnName = columnName;
		this.argumentValue = argumentValue;
		this.condition = condition;
	}

	public String getColumnName() {
		return columnName;
	}

	public Object getArgumentValue() {
		return argumentValue;
	}

	public ComparisonCondition getCondition() {
		return condition;
	}
	
}
