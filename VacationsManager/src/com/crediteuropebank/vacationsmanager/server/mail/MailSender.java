package com.crediteuropebank.vacationsmanager.server.mail;

/**
 * 
 * Defines set of methods for sending mails.
 * 
 * @author DIMAS
 *
 */
public interface MailSender {

	/**
	 * This method send mails to specified address.
	 * 
	 * @param toAddress - address of the people to which mail is sending.
	 * @param subject - subject (title) of the email.
	 * @param messageText - text of the email.
	 */
	public void sendMail(String toAddress, String subject, String messageText);
	
}
