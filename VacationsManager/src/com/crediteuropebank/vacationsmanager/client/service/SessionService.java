package com.crediteuropebank.vacationsmanager.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This remote service interface contains methods that operate with session.
 * 
 * @author DIMAS
 *
 */
@RemoteServiceRelativePath("rpc/session")
public interface SessionService extends RemoteService {
	
	/**
	 * This method invalidates session.
	 */
	void invalidateSession();
}
