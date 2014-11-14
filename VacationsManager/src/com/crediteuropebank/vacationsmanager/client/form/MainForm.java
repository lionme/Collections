package com.crediteuropebank.vacationsmanager.client.form;

import com.crediteuropebank.vacationsmanager.client.form.tab.HolidayDaysListTab;
import com.crediteuropebank.vacationsmanager.client.form.tab.RolesListTab;
import com.crediteuropebank.vacationsmanager.client.form.tab.UsersListTab;
import com.crediteuropebank.vacationsmanager.client.form.tab.VacationsChartTab;
import com.crediteuropebank.vacationsmanager.client.form.tab.VacationsListTab;
import com.google.gwt.user.client.ui.Composite;

/**
 * This class contains all logic about vacations details displaying (chart, table...).
 * It contains {@link CustomTabPanel} where contains almost all functionality of application.
 * 
 * @author dimas
 *
 */
public class MainForm extends Composite{
	
	/**
	 * The width of the tab panel.
	 */
	private static final String TAB_PANEL_WIDTH = "1260px";
	
	/**
	 * The height of the tab panel.
	 */
	private static final String TAB_PANEL_HEIGHT = "560px";
	
	/**
	 * The width of the tab's header.
	 */
	private static final int TAB_HEADER_WIDTH = 140;
	
	/**
	 * Creates MainForm instance. If logged in user is admin then additional
	 * tabs are created to which have access only users with administrator rights.
	 * 
	 * @param isLoggedInUserAdmin - define whether logged in user has administrator rights or not.
	 */
	public MainForm(boolean isLoggedInUserAdmin) {
		CustomTabPanel tabPanel = new CustomTabPanel(TAB_HEADER_WIDTH);
		
		// Set size of the main panel.
	    tabPanel.setWidth(TAB_PANEL_WIDTH);
	    tabPanel.setHeight(TAB_PANEL_HEIGHT);
	    
	    // Create a region with vacation list
	    VacationsListTab vacationsListTab = new VacationsListTab();
	    tabPanel.addTab(0, vacationsListTab, "Vacations");
	    
	    // Crate a region with vacation chart
	    VacationsChartTab vacationsChartRegion = new VacationsChartTab();
	    tabPanel.addTab(1, vacationsChartRegion, "Vacations Chart");
	    
	    /*
	     *  If logged in user is admin, then show additional admins tabs:
	     */
	    if (isLoggedInUserAdmin) {
	    	UsersListTab usersEditingTab = new UsersListTab();
		    tabPanel.addTab(2, usersEditingTab, "Users");
		    
		    RolesListTab rolesListTab = new RolesListTab();
		    tabPanel.addTab(3, rolesListTab, "Roles");	   
		    
		    HolidayDaysListTab holidayDaysTab = new HolidayDaysListTab();
		    tabPanel.addTab(4, holidayDaysTab, "Holiday Days");
	    }
	    
	    tabPanel.updateTab(0);
	    
	    initWidget(tabPanel);
	}
	
}
