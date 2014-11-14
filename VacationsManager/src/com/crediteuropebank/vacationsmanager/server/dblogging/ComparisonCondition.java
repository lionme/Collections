package com.crediteuropebank.vacationsmanager.server.dblogging;

/**
 * 
 * This enum determine conditions for SQL statement.
 * 
 * @author DIMAS
 *
 */
public enum ComparisonCondition implements Condition {
	
	/**
	 * Equals condition.
	 */
	EQ {
		@Override
		public String operation() {
			return "=";
		}

	},

	/**
	 * Greater or equals condition.
	 */
	GE {
		@Override
		public String operation() {
			return ">=";
		}

	},
	
	/**
	 * Greater condition.
	 */
	GR {

		@Override
		public String operation() {
			return ">";
		}
		
	},
	
	/**
	 * Lower or equals condition.
	 */
	LE {
		@Override
		public String operation() {
			return "<=";
		}
	},
	
	/**
	 * Lower condition.
	 */
	LW {

		@Override
		public String operation() {
			return "<";
		}
		
	},
	
	/**
	 * SQL "IN" condition
	 */
	IN {

		@Override
		public String operation() {
			return " IN ";
		}
		
	}
	
}
