package com.crediteuropebank.vacationsmanager.server.controller;

import java.util.Map;

//import javax.inject.Inject;

//import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The example of using Spring MVC. We can combine GWT RPC and Spring MVC. Isn't used now. 
 * You can use as example in future. For example for adding some REST Service functionality.
 * @author dimas
 *
 */
@Controller
public class HomeController {
	
	@RequestMapping({"/", "/myapp/*"})
	public String showHomePage(Map<String, Object> model) {
		//model.put("spittles", spitterService.getRecentSpittles(DEFAULT_SPITTLES_PER_PAGE));
		
		//String testVariable = "TEST_TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		
		return "init";
	}

}
