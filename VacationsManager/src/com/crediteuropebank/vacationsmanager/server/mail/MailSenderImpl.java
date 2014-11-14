package com.crediteuropebank.vacationsmanager.server.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.logging.InjectLogger;
import com.crediteuropebank.vacationsmanager.server.PropertiesBean;

/**
 * This class contains methods that helps you to send mails. 
 * 
 * @author DIMAS
 *
 */
@Component("mailSender")
@Transactional(propagation=Propagation.NOT_SUPPORTED)
public class MailSenderImpl implements MailSender{
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private PropertiesBean propertiesBean;
	
	@InjectLogger
	private Logger logger;
	
	public void sendMail(final String toAddress, final String subject, final String messageText) {
		try {
			String fromAddress = propertiesBean.getProgramUserEmail();
			if (fromAddress == null) {		
				throw new MailException("Failed to load program user's email!") {
					private static final long serialVersionUID = 1L;
					};
			}
			
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromAddress);
			message.setTo(toAddress);
			message.setSubject(subject);
			message.setText(messageText);

			javaMailSender.send(message); 
		} catch(MailException e) {
			logger.error("Failed to send email. Reason: ", e);
		}
	}
	
}
