package com.crediteuropebank.vacationsmanager.server.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.logging.InjectLogger;
import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dao.ApprovalStepDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.server.dao.VacationDAO;
import com.crediteuropebank.vacationsmanager.server.mail.MailSender;
import com.crediteuropebank.vacationsmanager.server.mail.MailUtil;
import com.crediteuropebank.vacationsmanager.server.service.ApprovalServiceImpl;
import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * <p>In this "manager" class starts real business logic for {@link ApprovalStep}} domain object. 
 * Remote service implementation class {@link ApprovalServiceImpl} is used only to deliver 
 * requests from user to business layer using RPC call.</p>
 * 
 * <p>In this class logger is used explicitly for marking boundaries of each "service" method call by adding
 * INFO level messages to log.</p>
 * 
 * @author dimas
 *
 */
@Service(value="approvalManager")
@Transactional(propagation=Propagation.SUPPORTS, rollbackFor=Exception.class)
public class ApprovalManager {
	
	/**
	 * This string is used as comment during rejection operation.
	 */
	public static final String AUTOMATIC_REJECTION_COMMENT = 
			"This was automatic rejetion because one of the approver steps was deleted.";
	
	@Autowired
	private ApprovalStepDAO approvalStepDAO;
	
	@Autowired
	private MailSender mailSender;
	
	@Autowired
	private VacationDAO vacationDAO;
	
	@Autowired
	private UserManager userManager;
	
	@InjectLogger
	private Logger logger;
	
	
	/**
	 * This method creates approval flow for vacation request and save it to APPROVAL_STEPS 
	 * table in DB.
	 * 
	 * @param vacation - {@link Vacation} entity for which it is necessary to generate 
	 * 						approval flow
	 * @throws CustomMessageException if some error occurs during creating of the approval flow
	 * 						and we need to inform user about it
	 * @throws IllegalArgumentException if input vacation object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	void createApprovalFlow(final Vacation vacation) throws CustomMessageException {
		if (vacation == null) {
			throw new IllegalArgumentException("Input vacation object should not be null!");
		}
		
		List<ApprovalStep> approvalSteps = new ArrayList<ApprovalStep>();
		
		int rowNumber = 1;
		
		/*
		 * Prepare the list of approval steps for vacation.
		 */
		
		// Add an approval steps for deputies.
		List<User> deputies = vacation.getDeputies();
		for(User deputy: deputies) {
			ApprovalStep approval = new ApprovalStep();
			approval.setState(ApprovalStepState.WAITING);
			approval.setVacation(vacation);
			approval.setApprover(deputy);
			approval.setRowNumber(rowNumber);
			approvalSteps.add(approval);
			rowNumber ++;
		}
		
		// Add an approval steps for parent roles 
		Role role = vacation.getUser().getRole();
		while(role.getParentRole() != null) {
			Role parentRole = role.getParentRole();
			
			/* Check that there is at least one user with specified role. */
			int numOfUsersWithRole = userManager.getNumberOfUsersWithRole(parentRole);
			if (numOfUsersWithRole>0) {
				ApprovalStep approvalStep = new ApprovalStep();
				approvalStep.setState(ApprovalStepState.WAITING);
				approvalStep.setVacation(vacation);
				approvalStep.setApproverRole(parentRole);
				approvalStep.setRowNumber(rowNumber);
				approvalSteps.add(approvalStep);
			}
			
			role = role.getParentRole();
			
			rowNumber ++;
		}
		
		approvalStepDAO.saveListOfApprovalSteps(approvalSteps);
		
		ApprovalStep firstApproval = approvalStepDAO.getApprovalStepsByVacationIdAndRowNumber(vacation.getId(), 1);
		
		// Create first approval an active and send message to him.
		try {
			makeApprovalActive(firstApproval);
		} catch (StaleObjectStateException e) { 
			// Here should not occur this error. And we don't need to inform client about it if it will occur.
			logger.error("Problem with versions during making first approval step active. " +
					"Such kind of errors should not occur in this place!");
			throw new RuntimeException(e);
			//throw new CustomMessageException(e);
		}
	}

	/**
	 * This method makes specified approval step an active and send message to 
	 * approver who should approve it.
	 * 
	 * @param approval - approval that should be approved next.
	 * @throws StaleObjectStateException if version of one of the domain objects is lower then version of record in DB.
	 */
	private void makeApprovalActive(final ApprovalStep approval) throws StaleObjectStateException {
		// Change status of the next approval to ACTIVE
		approvalStepDAO.changeApprovalStepState(approval, ApprovalStepState.ACTIVE);
		
		// send mail to one approver if exists or to all users with specified approver role
		if (approval.getApprover() !=  null) {
			mailSender.sendMail(approval.getApprover().geteMail(), 
					MailUtil.SUBJECT_TEMPLATE_NEXT_APPROVER, 
					MailUtil.generateMailTextForApprover(approval.getApprover().getFullName(), approval.getVacation().getId()));
		} else if (approval.getApproverRole()!= null) {
			List<User> approvers = userManager.getUsersByRole(approval.getApproverRole());
			
			for (User approver: approvers) {
				mailSender.sendMail(approver.geteMail(), 
						MailUtil.SUBJECT_TEMPLATE_NEXT_APPROVER, 
						MailUtil.generateMailTextForApprover(approver.getFullName(), approval.getVacation().getId()));
			}
		} else {
			throw new IllegalStateException("For each approval step approver and approverRole can't be null at the same time!");
		}
	}
	
	/**
	 * Executes approver logic for specified approval step. 
	 * 
	 * @param approvalStep - approval step that should be approved.
	 * @throws CustomMessageException if any problem with input data occurred and we need to inform client
	 * 				about it
	 * @throws IllegalArgumentException if input approvalStep object is null or its inner 
	 * 				vacation object is null 
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void approve(final ApprovalStep approvalStep) throws CustomMessageException  {		
		/* Check input data on null values */
		if (approvalStep == null || approvalStep.getVacation() == null) {
			throw new IllegalArgumentException("Input ApprovalStep object " +
					"or nested Vacation object should not be null.");
		} 
		
		if (approvalStep.getVacation().getStartDate()
				.before(DateUtil.getCurrentDateWithoutTime())) {
			throw new CustomMessageException("You cannot approve vacation because its " +
					"start day is lower then today!");
		}
		
		try {
			approvalStep.setState(ApprovalStepState.APPROVED);
			approvalStepDAO.update(approvalStep);

			// If this is a first approval then change the state of vacation.
			if (approvalStep.getVacation().getState() == VacationState.JUST_OPENED) {
				// May be necessary in future move this logic to separated class (VacationManager)
				vacationDAO.changeVacationState(approvalStep.getVacation(), VacationState.IN_PROGRESS);
			}

			// Get the list of approvals that necessary to approve
			List<ApprovalStep> approvals = approvalStepDAO.getWaitingApprovalStepsForVacation(approvalStep.getVacation().getId());

			if (approvals.size()>0) { // If there still another approval steps for this vacation - go to next approval step.
				makeApprovalActive(approvals.get(0));
			} else { // If this was the last approver - change status of the vacation to approved.
				
				// Update info about vacation (necessary because we can update vacation earlier during this request):
				Vacation vacation = vacationDAO.getById(approvalStep.getVacation().getId());
				
				// May be necessary in future move this logic to separated class (VacationManager)
				vacationDAO.changeVacationState(vacation, VacationState.APPROVED);
				// send mail to user that his vacation has been approved.
				mailSender.sendMail(approvalStep.getVacation().getUser().geteMail(), 
						MailUtil.SUBJECT_TEMPLATE_REQUEST_APPROVED, 
						MailUtil.generateMailTextIfVacationApproved(approvalStep.getVacation().getUser().getFullName(), approvalStep.getVacation().getId()));
			}

		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method executes reject logic for specified approval step. 
	 * It also changes state of the vacation to REJECTED.
	 * 
	 * @param approvalStep - approval step on which approver decided to reject vacation's request.
	 * @throws IllegalArgumentException if input data are null.
	 * @throws CustomMessageException if some problem with input data occurrs and we want to inform user 
	 * 				about them
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void reject(final ApprovalStep approvalStep) throws CustomMessageException {
		/* Check input data on null values */
		if (approvalStep == null || approvalStep.getVacation() == null) {
			throw new IllegalArgumentException("Input ApprovalStep object " +
					"or nested Vacation object should not be null.");
		} 

		// I left a possibility to reject "old" vacations.
		/*if (approvalStep.getVacation().getStartDate()
				.before(DateUtil.getCurrentDateWithoutTime())) {
			throw new CustomMessageException("You cannot reject vacation because its " +
					"start day is lower then today!");
		}*/
		
		try {
			/* change state of the current approval step to rejected */
			approvalStep.setState(ApprovalStepState.REJECTED);
			approvalStepDAO.update(approvalStep);
			
			/* change state of the vacation to REJECTED */
			vacationDAO.changeVacationState(approvalStep.getVacation(), VacationState.REJECTED);
			
			/* send mail to user to inform him that his vacation request has been rejected. */
			mailSender.sendMail(approvalStep.getVacation().getUser().geteMail(), 
					MailUtil.SUBJECT_TEMPLATE_REQUEST_REJECTED, 
					MailUtil.generateMailTextIfVacationRejected(approvalStep.getVacation().getUser().getFullName(), 
							approvalStep.getVacation().getId(),
							approvalStep.getComments()));

		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method deletes (changes status to 0) all approval steps for specified vacation.
	 * Also this method sends mail to the approvers who should approve vacation.
	 * 
	 * @param vacation - Vacation for which it is necessary to remove generated approval flow.
	 * @throws CustomMessageException if some problem occurs during deleting of the approval flow
	 * 						and we need to inform user about it
	 * @throws IllegalArgumentException if input vacation object is null
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void deleteApprovalFlow(final Vacation vacation) throws CustomMessageException {
		if (vacation == null) {
			throw new IllegalArgumentException("Input vacation object should not be null!");
		}
		
		try {
			ApprovalStep approvalStep = approvalStepDAO.getActiveApprovalStepForVacation(vacation.getId());
			
			if (approvalStep != null) {
				// send mail to one approver if exists or to all users with specified approver role
				if (approvalStep.getApprover() !=  null) {
					mailSender.sendMail(approvalStep.getApprover().geteMail(), 
							MailUtil.SUBJECT_TEMPLATE_VACATION_WAS_DELETED, 
							MailUtil.generateMailTextIfVacationDeleted(approvalStep.getApprover().getFullName(), 
									approvalStep.getVacation().getId()));
				} else if (approvalStep.getApproverRole()!= null) {
					List<User> approvers = userManager.getUsersByRole(approvalStep.getApproverRole());

					for (User approver: approvers) {
						mailSender.sendMail(approver.geteMail(), 
								MailUtil.SUBJECT_TEMPLATE_VACATION_WAS_DELETED, 
								MailUtil.generateMailTextIfVacationDeleted(approver.getFullName(), 
										approvalStep.getVacation().getId()));
					}
				} else {
					throw new IllegalStateException("For each approval step approver and approverRole can't be null at the same time!");
				}
			}
			
			List<ApprovalStep> approvalStepsForVacation = approvalStepDAO.getAllApprovalStepsForVacation(vacation);
			approvalStepDAO.deleteListOfApprovalSteps(approvalStepsForVacation);

		} catch (StaleObjectStateException e) {
			throw new CustomMessageException(e);
		}
	}
	
	/**
	 * This method gets active approval steps for specified approver.
	 * @param approver - the user who should approve approval steps.
	 * @return the list of approval steps that should be approved by specified user.
	 */
	public List<ApprovalStep> getActiveApprovalsForApprover(final User approver) {
		return approvalStepDAO.getActiveApprovalStepsForApprover(approver);
	}
	
	/**
	 * This method gets active approval steps for logged in user.
	 * @return the list of approval steps that should be approved by logged in user.
	 * @throws CustomMessageException 
	 */
	public List<ApprovalStep> getActiveApprovalsForLoggedInUser() throws CustomMessageException {
		User loggedInUser = userManager.getLoggedInUser();
		
		List<ApprovalStep> approvalStepsList = approvalStepDAO.getActiveApprovalStepsForApprover(loggedInUser);
		
		return approvalStepsList;
	}
	
	/**
	 * This method returns the list of all approval steps for specified vacation.
	 * 
	 * @param vacation
	 * @return
	 */
	public List<ApprovalStep> getAllApprovalStepsForVacation(final Vacation vacation) {
		return approvalStepDAO.getAllApprovalStepsForVacation(vacation);
	}
	
	/**
	 * This method should be called within another public method of interface with transaction
	 * propagation REQUIRED.<br/>
	 * 
	 * This method deletes (changes status to 0) all approval steps with specified approver role.<br/><br/>
	 * 
	 * The sequence of operations:<br/>
	 * 		1) Get all ApprovalSteps with specified ROLE_ID (approver role) that are related 
	 * to vacations with state IN_PROGRESS or JUST_OPENED.<br/>
	 * 		2) Reject that approval steps.<br/>
	 * 		3) Change state of the all approval steps for related vacation to REJECTED.<br/>
	 * 		4) Then delete all approval steps with specified ROLE_ID. (Don’t care about states).
	 * 
	 * @param role - role for which approval steps should be deleted.
	 * @throws CustomMessageException if any error occurs during rejection.
	 */
	void deleteApprovalStepsWithSpecifiedApproverRole(final Role role) throws CustomMessageException {
		if (role == null) {
			throw new IllegalArgumentException("Input role object cannot be null!");
		}
		
		List<ApprovalStep> approvalSteps = 
				approvalStepDAO.getAllApprovalStepsWithSpecifiedApproverRole(role);
		
		for (ApprovalStep approvalStep: approvalSteps) {
			approvalStep.setComments(AUTOMATIC_REJECTION_COMMENT);
			this.reject(approvalStep);
			
			/*
			 * Change state of all other approval steps for vacation to REJECTED and set
			 * standard comments for automatic rejection operation. 
			 */
			approvalStepDAO.changeStateOfAllApprovalStepsForVacation(approvalStep.getVacation().getId(), 
					ApprovalStepState.REJECTED, 
					ApprovalManager.AUTOMATIC_REJECTION_COMMENT);
		}
		
		approvalStepDAO.deleteApprovalStepsWithSpecifiedApproverRole(role);
	}
	
	/**
	 * <p>This method is used for deleting all approval steps with specified approver.</p>
	 * 
	 * <p>Before deleting we reject all corresponding vacations for approval steps with specified 
	 * approver that have ACTIVE state.</p>
	 * 
	 * @param approver - user that should approval step.
	 * @throws CustomMessageException if some problems occur and we need to 
	 * 					inform user about them.
	 */
	void deleteApprovalStepsWithSpecifiedApprover(final User approver) throws CustomMessageException {
		if (approver == null) {
			throw new IllegalArgumentException("Input approver object should not be null!");
		}
		
		List<ApprovalStep> approvalSteps = 
				approvalStepDAO.getActiveApprovalStepsForApprover(approver);
		
		for (ApprovalStep approvalStep: approvalSteps) {
			approvalStep.setComments(AUTOMATIC_REJECTION_COMMENT);
			this.reject(approvalStep);
			
			/*
			 * Change state of all other approval steps for vacation to REJECTED and set
			 * standard comments for automatic rejection operation. 
			 */
			approvalStepDAO.changeStateOfAllApprovalStepsForVacation(approvalStep.getVacation().getId(), 
					ApprovalStepState.REJECTED, 
					ApprovalManager.AUTOMATIC_REJECTION_COMMENT);
		}
		
		approvalStepDAO.deleteApprovalStepsWithSpecifiedApprover(approver);
	}
	
	/**
	 * 
	 * This method returns the number of the approval steps with specified approver role.
	 * 
	 * @param role
	 * @return the number of the approval steps with specified approver role.
	 */
	int getNumberOfApprovalStepsWithApproverRole(Role role) {
		return approvalStepDAO.getNumberOfApprovalStepsWithSpecifiedApprovalRole(role);
	}
	
}
