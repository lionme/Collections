package com.crediteuropebank.vacationsmanager.server.service;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.service.ApprovalService;
import com.crediteuropebank.vacationsmanager.server.manager.ApprovalManager;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class corresponds the server side implementation of the RPC's remote service for 
 * {@link ApprovalService} interface.
 * It just transfer the calls to the {@link ApprovalManager} which represents the real 
 * "business" layer of the application.
 * 
 * @author DIMAS
 *
 */
public class ApprovalServiceImpl implements ApprovalService {
	private ApprovalManager approvalManager;

	public ApprovalManager getApprovalManager() {
		return approvalManager;
	}
	
	public void setApprovalManager(ApprovalManager approvalManager) {
		this.approvalManager = approvalManager;
	}
	
	public ApprovalServiceImpl() {
		
	}
	
	@Override
	public List<ApprovalStep> getActiveApprovalStepsForApprover(User approver) {
		return approvalManager.getActiveApprovalsForApprover(approver);
	}
	
	@Override
	public List<ApprovalStep> getActiveApprovalStepsForLoggedInUser() throws CustomMessageException {
		return approvalManager.getActiveApprovalsForLoggedInUser();
	}

	@Override
	public void approve(ApprovalStep approval) throws CustomMessageException {
		approvalManager.approve(approval);
	}

	@Override
	public void reject(ApprovalStep approvalStep) throws CustomMessageException {
		approvalManager.reject(approvalStep);
	}

	@Override
	public List<ApprovalStep> getApprovalStepsForVacation(Vacation vacation) {
		return approvalManager.getAllApprovalStepsForVacation(vacation);
	}
	
	
}
