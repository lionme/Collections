package com.crediteuropebank.vacationsmanager.shared.domain;

import java.io.Serializable;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;

/**
 * This is abstract base class for all domain objects. It has default visibility to be used only in domain objects.
 * 
 * @author DIMAS
 *
 */
public abstract class BaseDomain  implements Serializable {
	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	@Column(columnName = "ID")
	protected long id;
	
	@Column(columnName = "VERSION")
	protected int version;
	
	public long getId() {
		return id;
	}

	/**
	 *  We don't need public setter for id. It should be set only during object creation 
	 *  using reflection (look at DaoUtil class) and private setter.
	 *  
	 *  @param id
	 */
	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	/**
	 *  We don't need public setter for id. It should be set only during object creation 
	 *  using reflection (look at DaoUtil class) and private setter.
	 *  
	 *  @param version
	 */
	@SuppressWarnings("unused")
	private void setVersion(int version) {
		this.version = version;
	}
	
	protected BaseDomain(long id, int version) {
		this.id = id;
		this.version = version;
	}
	
	/**
	 * This constructor sets default values of the id and version (each one equals 0). 
	 */
	public BaseDomain() {
		this.id = 0L;
		this.version = 0;
	}
}
