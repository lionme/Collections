package com.crediteuropebank.vacationsmanager.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author DIMAS
 *
 */
public interface SessionServiceAsync {

	/**
	 * This method invalidates session.
	 * 
	 * @param callback
	 */
	void invalidateSession(AsyncCallback<Void> callback);

}
