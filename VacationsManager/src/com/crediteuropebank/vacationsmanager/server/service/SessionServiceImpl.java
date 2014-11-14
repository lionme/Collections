package com.crediteuropebank.vacationsmanager.server.service;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.crediteuropebank.vacationsmanager.client.service.SessionService;

/**
 * This class contains implementation of the RPC's remote interface methods that operates 
 * with user session.
 * 
 * @author DIMAS
 *
 */
public class SessionServiceImpl implements SessionService {

	/**
	 * This method invalidates session.
	 */
	@Override
	public void invalidateSession() {
		//SecurityContextHolder.getContext().setAuthentication(null);
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpSession session = attr.getRequest().getSession();
		
		if (session!=null) {
			session.invalidate();
		} /*else {
			throw new IllegalStateException("Session Expired");
		}*/
	}

}
