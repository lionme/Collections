package com.crediteuropebank.vacationsmanager.server.dao;


/**
 * 
 * This class was created to reduce amount of the boilerplate code and 
 * to reduce risk of forgetting to check something when optimistic locKing will be implemented.
 * 
 * In this class was implemented TemplateMethod pattern for methods where versions should be checked and StaleObjectStateException
 * should be thrown.
 * 
 * Is not used. It was a bad idea.
 * 
 * @author DIMAS
 *
 */
@Deprecated
public abstract class BaseDAOImpl<T> implements DAO<T> {

	@Override
	public void update(T object) throws StaleObjectStateException {
		int affectedRowsNumber = executeUpdate(object);
		
		if (affectedRowsNumber == 0 && !compareVersions(object)) {
			throw new StaleObjectStateException();
		}
	}
	
	/**
	 * 
	 * Executes update of the object in the DB. A template method for update(..) method in {@link DAO} interface.
	 * 
	 * @param Object - object to be updated.
	 * @return - the number of affected rows
	 */
	abstract int executeUpdate(T Object);

	@Override
	public void delete(T object) throws StaleObjectStateException {
		int affectedRowsNumber = executeDelete(object);
		
		if (affectedRowsNumber == 0 && !compareVersions(object)) {
			throw new StaleObjectStateException();
		}
	}
	
	/**
	 * 
	 * Executes delete of the object in the DB (change its status to 0). A template method for delete(..) method in {@link DAO} interface.
	 * 
	 * @param Object - object to be deleted.
	 * @return - the number of affected rows
	 */
	abstract int executeDelete(T object);

/*	@Override
	public void removeFromDB(T object) throws StaleObjectStateException {
		int affectedRowsNumber = executeDelete(object);
		
		if (affectedRowsNumber == 0 && !compareVersions(object)) {
			throw new StaleObjectStateException();
		}	
	}
	
	*//**
	 * 
	 * This method really deletes record from DB. A template method for removeFromDB(..)
	 * method in {@link DAO} interface.
	 * 
	 * @param Object - object to be removed from DB.
	 * @return - the number of affected rows.
	 *//*
	abstract int executeRemoveFromDB(T object);*/

	/**
	 * 
	 * This method checks version of the input object with last version of the DB record.
	 * 
	 * @param object - object for which version should be checked.
	 * @return true - if version of object match to the version in DB.
	 */
	//abstract boolean compareVersions(T object);
}
