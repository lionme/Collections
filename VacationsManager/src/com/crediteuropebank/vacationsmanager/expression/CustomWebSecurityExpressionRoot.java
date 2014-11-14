package com.crediteuropebank.vacationsmanager.expression;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

import com.crediteuropebank.vacationsmanager.shared.Privilege;

/**
 * 
 * A custom WebSecurityExpressionRoot which is necessary for adding new custom SpEL function.
 * 
 * @author dimas
 *
 */
public class CustomWebSecurityExpressionRoot extends WebSecurityExpressionRoot {
	private final Authentication a;
	
	public CustomWebSecurityExpressionRoot(Authentication a, FilterInvocation fi) {
		super(a, fi);
		
		this.a = a;
	}

	public boolean hasAdminRights() {
		Collection<? extends GrantedAuthority> authorities = a.getAuthorities();
		
		GrantedAuthority adminAuthority = new SimpleGrantedAuthority(Privilege.ADMIN.toString());
		
		return authorities.contains(adminAuthority);		
	}
}
