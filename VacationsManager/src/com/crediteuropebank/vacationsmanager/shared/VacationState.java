package com.crediteuropebank.vacationsmanager.shared;

/**
 * This enum contains the list of vacation states.
 * @author dimas
 *
 */
public enum VacationState {

	/**
	 * This status means that vacation is fully approved by all approvers.
	 */
	APPROVED,
	
	/**
	 * This status means that approval process have been started (some of approvers have already 
	 * approved request), but not finished and waiting for one or more approvals.
	 */
	IN_PROGRESS,
	
	/**
	 * This means that nobody have yet approved the vacation. It is just opened.
	 */
	JUST_OPENED,
	
	/**
	 * This means that somebody of the approvers rejected vacation.
	 */
	REJECTED
}
