package com.crediteuropebank.vacationsmanager.shared.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;
import com.crediteuropebank.vacationsmanager.server.dblogging.Domain;
import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;

/**
 * 
 * Domain class which represents approval step.
 * 
 * @author DIMAS
 *
 */
@Domain(tableName="APPROVAL_STEPS", logTableName="APPROVAL_STEPS_LOG")
public class ApprovalStep extends BaseDomain {
	
	/**
	 * Automatically generated serial version id.
	 */
	private static final long serialVersionUID = -3986854129454905841L;

	@Column(columnName = "STATE")
	@NotNull(message = "Approval state could not be null")
	private ApprovalStepState state;
	
	@Column(columnName = "VACATION_ID")
	@NotNull(message = "Vaction couldn't be null")
	private Vacation vacation;
	
	/**
	 * User, who should approve request on current step of approval.
	 */
	@Column(columnName = "ROLE_ID")
	private Role approverRole;
	
	@Column(columnName = "APPROVER_ID")
	private User approver;
	
	@Column(columnName = "ROW_NUMBER")
	@Min(1)
	private int rowNumber;
	
	@Column(columnName = "COMMENTS")
	@Size(max=255, message="Comments should contain maximum 255 symbols.")
	private String comments;
	
	public ApprovalStep() {
		super();
	}

	public ApprovalStepState getState() {
		return state;
	}

	public void setState(ApprovalStepState state) {
		this.state = state;
	}

	public Vacation getVacation() {
		return vacation;
	}

	public void setVacation(Vacation vacation) {
		this.vacation = vacation;
	}
	
	public Role getApproverRole() {
		return approverRole;
	}

	public void setApproverRole(Role approverRole) {
		this.approverRole = approverRole;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	
	public User getApprover() {
		return approver;
	}

	public void setApprover(User approver) {
		this.approver = approver;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	@Override
	public String toString() {
		return "ApprovalStep [id=" + id + ", version=" + version + ", state="
				+ state + ", vacation=" + vacation + ", approverRole="
				+ approverRole + ", approver=" + approver + ", rowNumber="
				+ rowNumber + ", comments=" + comments + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((approver == null) ? 0 : approver.hashCode());
		result = prime * result
				+ ((approverRole == null) ? 0 : approverRole.hashCode());
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime * result + rowNumber;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result
				+ ((vacation == null) ? 0 : vacation.hashCode());
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
		ApprovalStep other = (ApprovalStep) obj;
		if (approver == null) {
			if (other.approver != null)
				return false;
		} else if (!approver.equals(other.approver))
			return false;
		if (approverRole == null) {
			if (other.approverRole != null)
				return false;
		} else if (!approverRole.equals(other.approverRole))
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (rowNumber != other.rowNumber)
			return false;
		if (state != other.state)
			return false;
		if (vacation == null) {
			if (other.vacation != null)
				return false;
		} else if (!vacation.equals(other.vacation))
			return false;
		return true;
	}
	
}
