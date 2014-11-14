package com.crediteuropebank.vacationsmanager.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 
 * This class is used simply for holding necessary values loaded from .properties 
 * by PropertySourcesPlaceholderConfigurer file during context initialization.
 * 
 * @author dimas
 *
 */
@Component("propertiesBean")
public class PropertiesBean {
	
	@Value(value="${mail.program_email}")
	private String programUserEmail;
	
	public String getProgramUserEmail() {
		return programUserEmail;
	}
	
	@Value(value="${vacation.max_day_ahead}")
	private int maxDaysAhead;
	
	public int getMaxDaysAhead() {
		return maxDaysAhead;
	}
	
	public PropertiesBean() {
	}
	
}
