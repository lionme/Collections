package com.crediteuropebank.vacationsmanager.client.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author DIMAS
 *
 */
@RemoteServiceRelativePath("rpc/approval")
public interface ApprovalService extends RemoteService{

	/**
	 * This method returns list of approval steps with state ACTIVE (look at {@link ApprovalStepState}) 
	 * for which specified user is approver.
	 * 
	 * @param approver - user who should approve approval steps.
	 * @return the list active approval steps for specified approver.
	 */
	List<ApprovalStep> getActiveApprovalStepsForApprover(User approver);
	
	/**
	 * This method returns list of approval steps with state ACTIVE (look at {@link ApprovalStepState})
	 * for which logged in user is approver.
	 * 
	 * @return the list active approval steps for which logged in user is approver.
	 * @throws CustomMessageException 
	 */
	List<ApprovalStep> getActiveApprovalStepsForLoggedInUser() throws CustomMessageException;
	
	/**
	 * Executes approve logic for specified approval step.
	 * 
	 * @param approvalStep - approval step to be approved.
	 * @throws CustomMessageException if we need to inform client about some problem.
	 */
	void approve(ApprovalStep approvalStep) throws CustomMessageException;
	
	/**
	 * Executes reject logic for specified approval step.
	 * 
	 * @param approvalStep - approval step to be rejected.
	 * @throws CustomMessageException if we need to inform client about some problem.
	 */
	void reject(ApprovalStep approvalStep) throws CustomMessageException;
	
	/**
	 * This method returns list of all approval steps for specified vacation.
	 * 
	 * @param vacation - vacation for which approval steps list should be fetched.
	 * @return the list of approval steps for vacation.
	 */
	List<ApprovalStep> getApprovalStepsForVacation(Vacation vacation);
}
