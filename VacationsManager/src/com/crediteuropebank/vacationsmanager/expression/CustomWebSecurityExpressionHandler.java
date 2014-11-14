package com.crediteuropebank.vacationsmanager.expression;

import org.springframework.context.annotation.DependsOn;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.stereotype.Component;

/**
 * 
 * This class is used for creating custom SpEL expression.
 * 
 * @author dimas
 *
 */
@Component(value="customExpressionHandler")
@DependsOn("passwordEncoder")
public class CustomWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {

	@Override
	protected SecurityExpressionRoot createSecurityExpressionRoot(
			Authentication authentication, FilterInvocation fi) {
		
		WebSecurityExpressionRoot expressionRoot = new CustomWebSecurityExpressionRoot(authentication, fi);
		
		return expressionRoot;
	}

	
}
