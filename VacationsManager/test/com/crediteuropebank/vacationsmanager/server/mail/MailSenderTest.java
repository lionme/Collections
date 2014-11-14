package com.crediteuropebank.vacationsmanager.server.mail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.crediteuropebank.vacationsmanager.server.PropertiesBean;

@RunWith(MockitoJUnitRunner.class)
public class MailSenderTest {
	
	@Mock
	private JavaMailSender javaMailSender;
	
	@Mock
	private PropertiesBean propertiesBean;
	
	@InjectMocks
	private MailSenderImpl mailSender;

	@Test
	public void testSendMail() {
		//ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		//MailSender mailSender = (MailSender) context.getBean("mailSender");
		
		Mockito.when(propertiesBean.getProgramUserEmail()).thenReturn("yermolovich1987@gmail.com");
		
		String from = "yermolovich1987@gmail.com";
		String to = "Dmitriy.IERMOLOVICH@crediteurope.com.ua";
		String title = "Test";
		String text = "Hello world!";
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(title);
		message.setText(text);
		
		mailSender.sendMail(to, title, text);
		
		Mockito.verify(javaMailSender).send(message);
		Mockito.verify(propertiesBean).getProgramUserEmail();
	}
	
}
