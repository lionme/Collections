package com.crediteuropebank.vacationsmanager.shared.domain;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;
import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;

/**
 * Domain User object with defined validation parameters.
 * 
 * We don't use password in any method (equals(..), hashCode(), toString())
 * 
 * @author dimas
 *
 */
@Domain(logTableName = "USERS_LOG", tableName = "USERS")
public class User extends BaseDomain {
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(columnName = "USERNAME")
	@NotNull(message="Username couldn't be null")
	@Size(min=4, max=50, message="Username should be from 4 to 50 symbols length")
	@Pattern(regexp="^[a-zA-Z_0-9]+$", message="Username must be alphanumeric with no spaces")
	private String username;
	
	@Column(columnName = "PASSWORD")
	@NotNull(message="Password couldn't be null")
	
	/* We cannot use size validation for password any more because of encoding */
	/*@Size(min=4, max=20, message="Password should be from 8 to 50 symbols length")*/
	//@Pattern(regexp = "[0-9]+")
	private String password;
	
	@Column(columnName = "FULLNAME")
	@NotNull(message="Full name could not be null")
	@Size(min=4, max=255, message="Full name should be from 4 to 255 symbols length")
	private String fullName;
	
	@Column(columnName = "ROLE_ID")
	@NotNull(message="Role coudn't be null")
	@Valid
	private Role role;
	
	@Column(columnName = "EMAIL")
	@NotNull(message="email couldn't be null")
	//@NotEmpty(message="eMail could not be empty")	
	@Pattern(regexp="^[A-Za-z0-9.%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}",	message="Invalid eMail address")
	private String eMail;
	
	@Valid
	private RemainingVacationDays vacationDays;
	
	/**
	 * Default constructor.
	 */
	public User() {
		super();
	}
	
	/**
	 * This constructor initialize basic fields of the class and sets default versions of id and version.
	 * 
	 * @param username
	 * @param password
	 * @param fullName
	 * @param role
	 * @param eMail
	 * @param vacationDays
	 */
	public User(String username, String password,
			String fullName, Role role, String eMail,
			RemainingVacationDays vacationDays) {
		super();
		
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.role = role;
		this.eMail = eMail;
		this.vacationDays = vacationDays;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String geteMail() {
		return eMail;
	}

	public void seteMail(String eMail) {
		this.eMail = eMail;
	}

	public RemainingVacationDays getVacationDays() {
		return vacationDays;
	}

	/**
	 * Vacation days id that you have said will not play role. It will be automatically taken from user.
	 * @param vacationDays
	 */
	public void setVacationDays(RemainingVacationDays vacationDays) {
		this.vacationDays = vacationDays;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", version=" + version + ", username="
				+ username + ", fullName="
				+ fullName + ", role=" + role + ", eMail=" + eMail
				+ ", vacationDays=" + vacationDays + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eMail == null) ? 0 : eMail.hashCode());
		result = prime * result
				+ ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		result = prime * result
				+ ((vacationDays == null) ? 0 : vacationDays.hashCode());
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
		User other = (User) obj;
		if (eMail == null) {
			if (other.eMail != null)
				return false;
		} else if (!eMail.equals(other.eMail))
			return false;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (vacationDays == null) {
			if (other.vacationDays != null)
				return false;
		} else if (!vacationDays.equals(other.vacationDays))
			return false;
		return true;
	}

	
	
}


