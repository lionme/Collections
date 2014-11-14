package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.List;

import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;

/**
 * 
 * This interface marks DAO class for operating with {@link ApprovalStep} domain object.
 * 
 * @author dimas
 *
 */
public interface ApprovalStepDAO extends DAO<ApprovalStep>{
	
	/**
	 * This method changes state (see {@link ApprovalStepState}) of the approval step.
	 * 
	 * @param approvalStep - approval step object for which state should be changed.
	 * @param newState - new state {@link ApprovalStepState}.
	 * @throws StaleObjectStateException
	 */
	void changeApprovalStepState(ApprovalStep approvalStep, ApprovalStepState newState) throws StaleObjectStateException;
	
	/**
	 * This method saves list of approval steps as one batch operation.
	 * 
	 * @param approvalSteps - list of approval steps to be saved.
	 */
	void saveListOfApprovalSteps(List<ApprovalStep> approvalSteps);
	
	/**
	 * This method deletes specified list of approval steps (change their status to 0).
	 * 
	 * @param approvalSteps - the list of approval steps to be deleted.
	 * @throws StaleObjectStateException 
	 */
	void deleteListOfApprovalSteps(List<ApprovalStep> approvalSteps) throws StaleObjectStateException;
	
	/**
	 * This method fetches the list of all approval steps for specified vacation. Approval steps is returned
	 * ordered by row number.
	 * 
	 * @param vacation - vacation for which list of approval steps should be fetched
	 * @return the list of approval steps for specified vacation.
	 */
	List<ApprovalStep> getAllApprovalStepsForVacation(Vacation vacation);
	
	/**
	 * This method fetches list of the approval steps for specified vacation that have WAITING 
	 * state (see {@link ApprovalStepState}).
	 * 
	 * @param vacationID - id of the vacation for which to get approval steops.
	 * @return
	 */
	List<ApprovalStep> getWaitingApprovalStepsForVacation(long vacationID);
	
	/**
	 * This method fetches approval step by vacation id and row number.
	 * 
	 * @param vacationID - vacation id. 
	 * @param rowNumber - row (order) number.
	 * @return the approval step
	 */
	ApprovalStep getApprovalStepsByVacationIdAndRowNumber(long vacationID, int rowNumber);
	
	/**
	 * This method fetches list of approval steps for specified approver which have ACTIVE 
	 * state (see {@link ApprovalStepState}).
	 * 
	 * @param approver - user that should approve vacation.
	 * @return the list of approval steps.
	 */
	List<ApprovalStep> getActiveApprovalStepsForApprover(User approver);
	
	/**
	 * This method gets approval step for vacation which is active at current time. (at each moment maximum
	 * one approval step should be active). If no active approval step was fount then returns null.
	 * 
	 * @param vacationId - id of the vacation.
	 * @return fetched approval step, if no active approval step was fount - return null.
	 */
	ApprovalStep getActiveApprovalStepForVacation(long vacationId);
	
	/**
	 * This method fetches list of all approval steps with specified approver role. 
	 * Related vacation should have states: JUST_OPENED and IN_PROGRESS. (look at {@link VacationState})
	 * 
	 * @param role - role.
	 * @return the list of the approval steps that corresponds necessary criteria.
	 */
	List<ApprovalStep> getAllApprovalStepsWithSpecifiedApproverRole(Role role);
	
	/**
	 * This method fetches list of all approval steps with specified approver user. 
	 * Related vacation should have states: JUST_OPENED and IN_PROGRESS. (look at {@link VacationState})
	 * 
	 * @param user - approver of the approval step.
	 * @return the list of the approval steps that corresponds necessary criteria.
	 */
	List<ApprovalStep> getAllApprovalStepsWithSpecifiedApprover(User approver);
	
	/**
	 * This method removes all approval steps with specified approver role.
	 * 
	 * @param role - role for which approval roles should be deleted.
	 */
	void deleteApprovalStepsWithSpecifiedApproverRole(Role role);
	
	/**
	 * This method removes all approval steps with specified approver user.
	 * 
	 * @param approver - user that should approve deleted approval step.
	 */
	void deleteApprovalStepsWithSpecifiedApprover(User approver);
	
	/**
	 * This method calculates the number of approval steps with specified approver role.
	 * 
	 * @param role
	 * @return the number of approval steps with specified approver role.
	 */
	int getNumberOfApprovalStepsWithSpecifiedApprovalRole(Role role);
	
	/**
	 * This method changes state and comments for all approval steps related 
	 * to specified vacation.
	 * 
	 * @param vacationId - id of the vacation.
	 * @param newState - new state of the approval steps.
	 * @param comment - comment with description why state was changed.
	 */
	void changeStateOfAllApprovalStepsForVacation(long vacationId, ApprovalStepState newState, String comment);

}
