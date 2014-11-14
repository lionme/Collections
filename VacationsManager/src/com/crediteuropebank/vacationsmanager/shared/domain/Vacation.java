package com.crediteuropebank.vacationsmanager.shared.domain;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;
import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;
import com.crediteuropebank.vacationsmanager.shared.VacationState;

/**
 * Domain object that represents the vacation. Validation annotations are added.
 * @author dimas
 *
 */
@Domain(logTableName = "VACATIONS_LOG", tableName = "VACATIONS")
public class Vacation extends BaseDomain {

	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(columnName = "START_DATE")
	@NotNull(message="Start date could not be null")
	private Date startDate;
	
	@Column(columnName = "END_DATE")
	@NotNull(message="End date could not be null")
	private Date endDate;
	
	@Column(columnName = "STATE")
	@NotNull(message="State couldn't be null")
	private VacationState state;
	
	@Column(columnName = "USER_ID")
	@NotNull(message="Vacation's owner could not be null")
	@Valid
	private User user;
	
	// We don't need to annotate this field by column annotation because we have separate table for vacations and deputies relation.
	@Size(min=1, max=4, message="Vacation should have min 1 and max 4 deputies")
	private List<User> deputies;
	
	@Valid
	private UsedVacationDays usedVacationDays;
	
	/**
	 * Default constructor.
	 */
	public Vacation() {
		super();
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

	public VacationState getState() {
		return state;
	}

	public void setState(VacationState state) {
		this.state = state;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<User> getDeputies() {
		return deputies;
	}

	public void setDeputies(List<User> deputies) {
		this.deputies = deputies;
	}

	public UsedVacationDays getUsedVacationDays() {
		return usedVacationDays;
	}

	public void setUsedVacationDays(UsedVacationDays usedVacationDays) {
		this.usedVacationDays = usedVacationDays;
	}

	@Override
	public String toString() {
		return "Vacation [id=" + id + ", version=" + version + ", startDate="
				+ startDate + ", endDate=" + endDate + ", state=" + state
				+ ", user=" + user + ", deputies=" + deputies
				+ ", usedVacationDays=" + usedVacationDays + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((deputies == null) ? 0 : deputies.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime
				* result
				+ ((usedVacationDays == null) ? 0 : usedVacationDays.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		Vacation other = (Vacation) obj;
		if (deputies == null) {
			if (other.deputies != null)
				return false;
		} else if (!deputies.equals(other.deputies))
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
		if (state != other.state)
			return false;
		if (usedVacationDays == null) {
			if (other.usedVacationDays != null)
				return false;
		} else if (!usedVacationDays.equals(other.usedVacationDays))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

}
