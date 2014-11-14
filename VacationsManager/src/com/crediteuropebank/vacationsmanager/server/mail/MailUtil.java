package com.crediteuropebank.vacationsmanager.server.mail;

/**
 * 
 * This class contains a set of static methods and constants that simplifies sending of the mails.
 * 
 * @author DIMAS
 *
 */
public class MailUtil {
	
	/**
	 *  Subject's template for message sending to approver. 
	 */
	public static final String SUBJECT_TEMPLATE_NEXT_APPROVER = 
			"Vacation request is waiting for your approval.";
	
	/**
	 * Subject's template for the case when vacation request was successfully approved.
	 */
	public static final String SUBJECT_TEMPLATE_REQUEST_APPROVED = 
			"Vacation request was succcessfully approved.";

	/**
	 * Subject's template for the case when vacation request was rejected.
	 */
	public static final String SUBJECT_TEMPLATE_REQUEST_REJECTED = 
			"Vacation request was rejected.";
	
	/**
	 * Subject's template for the case when vacation was deleted.
	 */
	public static final String SUBJECT_TEMPLATE_VACATION_WAS_DELETED = 
			"Vacation that has been waiting for your approval, was deleted.";
	
	/**
	 * This method generates template message text for approver. 
	 * It is used during automatic mail sending.
	 * 
	 * @param userFullName
	 * @param vacationId
	 * @return the template mail text.
	 */
	public static String generateMailTextForApprover(String userFullName, long vacationId) {
		String mailText = "Dear " + userFullName + ", \n" +
					"This is automatic message from Vacation Manager application.\n" +
					"Vacation with id=" + vacationId + " is waiting for your approval. Please, approve it.";
		
		return mailText;
	}
	
	/**
	 * This method generates template mail text for vacation's owner that his vacation has been approved.
	 * It is used during automatic mail sending.
	 * 
	 * @param userFullName
	 * @param vacationId
	 * @return the template mail text
	 */
	public static String generateMailTextIfVacationApproved(String userFullName, long vacationId) {
		String mailText = "Dear " + userFullName + ", \n" +
					"This is automatic message from Vacation Manager application.\n" +
					"Your vacation request with id=" + vacationId + " has been successfully approved.";
		
		return mailText;
	}
	
	/**
	 * This method generates template mail text for vacation's owner that his vacation has been rejected.
	 * It is used during automatic mail sending.
	 * 
	 * @param userFullName
	 * @param vacationId
	 * @return the template mail text
	 */
	public static String generateMailTextIfVacationRejected(String userFullName, long vacationId, String rejectionComments) {
		String mailText = "Dear " + userFullName + ", \n\n" +
					"This is automatic message from Vacation Manager application.\n" +
					"Your vacation request with id=" + vacationId + " was rejected.\n" + 
					((rejectionComments!=null)?("The reason of rejection is:" + rejectionComments):"");
		
		return mailText;
	}
	
	/**
	 * This method generates template mail text for approver to inform him that vacation was deleted.
	 * 
	 * @param userFullName
	 * @param vacationId
	 * @return the template mail text
	 */
	public static String generateMailTextIfVacationDeleted(String userFullName, long vacationId) {
		String mailText = "Dear " + userFullName + ", \n" +
				"This is automatic message from Vacation Manager application.\n" +
				"Vacation that has been waiting for your approval, was rejected (id=" + vacationId + ").";
	
		return mailText;
	}
}
