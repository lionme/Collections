package com.crediteuropebank.vacationsmanager.shared;

/**
 * This enum represents approval states.
 * @author dimas
 *
 */
public enum ApprovalStepState {
	
	/**
	 * This state means that current approval step is waiting for approval (but should't be approved next).
	 */
	WAITING,
	
	/**
	 * This state means that current approval step should be approved next.
	 */
	ACTIVE,
	
	/**
	 * This state means that current approval step was approved.
	 */
	APPROVED,
	
	/**
	 * This means that some of the approvers rejected vacation request on this step.
	 */
	REJECTED
}
