package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.List;


/**
 * This is general interface for all DAO classes. It contains common methods.
 * 
 * @author DIMAS
 *
 * @param <T> - the type of the domain object.
 */
public interface DAO<T> {
	
	/**
	 * This method saves object to the DB. After inserting of the record to DB it set's id of the 
	 * generated record to object. If key holder returns null instead id, it sets object's id to -1.
	 * 
	 * @param object - object to be saved.
	 * @return object that was saved with generated id.
	 */
	T save(T object);
	
	/**
	 * This method updates existed object in the DB.
	 * 
	 * @param object - object to be updated.
	 * @throws StaleObjectStateException if object that you want to update and DB record have different versions.
	 */
	void update(T object) throws StaleObjectStateException;
	
	/**
	 * This method change status of the record to 0 which means that record have been deleted.
	 * 
	 * @param object - the object that should be deleted from DB.
	 * @throws StaleObjectStateException if object that you want to delete and DB record have different versions.
	 */
	void delete(T object) throws StaleObjectStateException;
	
	/**
	 * This method removes record from DB. 
	 * As opposed to deleteRole() method this method deletes record in fact.
	 * 
	 * We shouldn't check version before deleting because this method will not be called during client request.
	 * This is help method that will be used only in server logic. You shouldn't us it without
	 * serious reason.
	 *
	 * @param object - the object that should be deleted from DB.
	 */
	void removeFromDB(T object);
	
	/**
	 * This method gets object from DB by its ID. If no record was found - returns null.
	 * 
	 * @param id - id of the object to be fetched.
	 * @return the object from DB with specified ID.
	 */
	T getById(long id);
	
	/**
	 * This method fetches all record from DB.
	 * 
	 * @return the list of all objects from DB.
	 */
	List<T> getAll();

	/**
	 * 
	 * This method compares version of the object with version of the record in DB.
	 * 
	 * @return true if object's version matches DB version.
	 */
	boolean compareVersions(T object);
}
