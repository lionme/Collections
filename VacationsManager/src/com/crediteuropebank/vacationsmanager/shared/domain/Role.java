package com.crediteuropebank.vacationsmanager.shared.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;
import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;
import com.crediteuropebank.vacationsmanager.shared.Privilege;

@Domain(logTableName = "ROLES_LOG", tableName = "ROLES")
public class Role extends BaseDomain {
	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	@Column(columnName = "NAME")
	@NotNull(message="Role name should not be null.")
	@Size(min=3, max=20, message="Role name should be from 3 to 20 symbols length.")
	private String name;
	
	@Column(columnName = "DESCRIPTION")
	@NotNull(message="Role description should not be null.")
	@Size(min=4, max=100, message="Role description should be from 4 to 100 symbols length.")
	private String desription;
	
	@Column(columnName = "PARENT_ROLE_ID")
	private Role parentRole;
	
	@Column(columnName = "PRIVILEGE")
	@NotNull(message="Privilege couldn't be null")
	private Privilege privilege;
	
	public Role() {
		super();
	}
	
	/**
	 * Constructor which initialize basic fields of this class and sets default values of the id and version.
	 */
	public Role(String name, String desription,
			Role parentRole, Privilege privilege) {
		super();
		
		this.name = name;
		this.desription = desription;
		this.parentRole = parentRole;
		this.privilege = privilege;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDesription() {
		return desription;
	}
	
	public void setDesription(String desription) {
		this.desription = desription;
	}
	
	public Role getParentRole() {
		return parentRole;
	}

	public void setParentRole(Role parentRole) {
		this.parentRole = parentRole;
	}
	
	public Privilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(Privilege privilege) {
		this.privilege = privilege;
	}

	@Override
	public String toString() {
		return "Role [ id=" + id + ", version=" + version + "name=" + name + ", desription=" + desription
				+ ", parentRole=" + parentRole + ", privilege=" + privilege
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((desription == null) ? 0 : desription.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parentRole == null) ? 0 : parentRole.hashCode());
		result = prime * result
				+ ((privilege == null) ? 0 : privilege.hashCode());
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
		Role other = (Role) obj;
		if (desription == null) {
			if (other.desription != null)
				return false;
		} else if (!desription.equals(other.desription))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentRole == null) {
			if (other.parentRole != null)
				return false;
		} else if (!parentRole.equals(other.parentRole))
			return false;
		if (privilege != other.privilege)
			return false;
		return true;
	}
	
}
