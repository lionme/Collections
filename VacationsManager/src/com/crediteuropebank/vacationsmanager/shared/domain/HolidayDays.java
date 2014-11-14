package com.crediteuropebank.vacationsmanager.shared.domain;

import java.util.Date;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;
import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;

/**
 * Entity that holds information about non working days associated with the holidays.
 * 
 * @author DIMAS
 *
 */
@Domain(logTableName = "HOLIDAY_DAYS_LOG", tableName = "HOLIDAY_DAYS")
public class HolidayDays extends BaseDomain {
	/**
	 * Default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Column(columnName = "START_DATE")
	private Date startDate;
	
	@Column(columnName = "END_DATE")
	private Date endDate;
	
	@Column(columnName = "DESCRIPTION")
	private String description;
	
	public HolidayDays() {
		super();
	}
	
	/**
	 * This constructor initialize basic fields of this class and sets default values of 
	 * id and version.
	 * 
	 * @param startDate
	 * @param endDate
	 * @param description
	 */
	public HolidayDays(Date startDate, Date endDate,
			String description) {
		super();
		
		this.startDate = startDate;
		this.endDate = endDate;
		this.description = description;
	}



	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "HolidayDays [id=" + id + ", version=" + version
				+ ", startDate=" + startDate + ", endDate=" + endDate
				+ ", description=" + description + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HolidayDays other = (HolidayDays) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}
	
	
}
