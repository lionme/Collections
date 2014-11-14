package com.crediteuropebank.vacationsmanager.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * 
 * This interface is used for loading custom css resources for whole application
 * 
 * @author dimas
 *
 */
public interface CustomApplicationResource extends ClientBundle {
	
	public static final CustomApplicationResource INSTANCE = 
			GWT.create(CustomApplicationResource.class); 
	
	 @Source("resources/CustomApplicationStyles.css")
     @CssResource.NotStrict
     CssResource css();

}
