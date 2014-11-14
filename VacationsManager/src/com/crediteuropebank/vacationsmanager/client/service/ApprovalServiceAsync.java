package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author DIMAS
 *
 */
public interface ApprovalServiceAsync {

	/**
	 * This method returns list of approval steps with state ACTIVE (look at {@link ApprovalStepState}) 
	 * for which specified user is approver.
	 * 
	 * @param approver - user who should approve approval steps.
	 * @param callback
	 */
	void getActiveApprovalStepsForApprover(User approver,
			AsyncCallback<List<ApprovalStep>> callback);

	/**
	 * This method returns list of approval steps with state ACTIVE (look at {@link ApprovalStepState})
	 * for which logged in user is approver.
	 * 
	 * @param callback
	 * @return the list active approval steps for which logged in user is approver.
	 */
	void getActiveApprovalStepsForLoggedInUser(
			AsyncCallback<List<ApprovalStep>> callback);

	/**
	 * Executes approve logic for specified approval step.
	 * 
	 * @param approvalStep - approval step to be approved.
	 * @param callback
	 */
	void approve(ApprovalStep a, AsyncCallback<Void> callback);

	/**
	 * Executes reject logic for specified approval step.
	 * 
	 * @param approvalStep - approval step to be rejected.
	 * @param callback
	 */
	void reject(ApprovalStep approvalStep, AsyncCallback<Void> callback);

	/**
	 * This method returns list of all approval steps for specified vacation.
	 * 
	 * @param vacation - vacation for which approval steps list should be fetched.
	 * @param callback
	 */
	void getApprovalStepsForVacation(Vacation vacation,
			AsyncCallback<List<ApprovalStep>> callback);

}
