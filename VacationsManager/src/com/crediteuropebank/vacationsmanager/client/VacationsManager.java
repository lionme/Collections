package com.crediteuropebank.vacationsmanager.client;

import com.crediteuropebank.vacationsmanager.client.form.ApprovalListForm;
import com.crediteuropebank.vacationsmanager.client.form.MainForm;
import com.crediteuropebank.vacationsmanager.client.form.RemainingVacationDaysForm;
import com.crediteuropebank.vacationsmanager.client.observer.GeneralObservable;
import com.crediteuropebank.vacationsmanager.client.resources.CustomApplicationResource;
import com.crediteuropebank.vacationsmanager.client.resources.CustomCellTableResources;
import com.crediteuropebank.vacationsmanager.client.resources.CustomDataGridResources;
import com.crediteuropebank.vacationsmanager.client.service.ApprovalService;
import com.crediteuropebank.vacationsmanager.client.service.ApprovalServiceAsync;
import com.crediteuropebank.vacationsmanager.client.service.HolidayDaysService;
import com.crediteuropebank.vacationsmanager.client.service.HolidayDaysServiceAsync;
import com.crediteuropebank.vacationsmanager.client.service.RoleService;
import com.crediteuropebank.vacationsmanager.client.service.RoleServiceAsync;
import com.crediteuropebank.vacationsmanager.client.service.SessionService;
import com.crediteuropebank.vacationsmanager.client.service.SessionServiceAsync;
import com.crediteuropebank.vacationsmanager.client.service.UsersService;
import com.crediteuropebank.vacationsmanager.client.service.UsersServiceAsync;
import com.crediteuropebank.vacationsmanager.client.service.VacationsService;
import com.crediteuropebank.vacationsmanager.client.service.VacationsServiceAsync;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.window.PasswordChangingWindow;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class VacationsManager implements EntryPoint {
	

	/*
	 * Create a remote service proxies to talk to the server-side services.
	 */
	private static final ApprovalServiceAsync approvalService = GWT.create(ApprovalService.class);
	private static final UsersServiceAsync usersService = GWT.create(UsersService.class);
	private static final VacationsServiceAsync vacationsService = GWT.create(VacationsService.class);
	private static final RoleServiceAsync roleService = GWT.create(RoleService.class);
	private static final SessionServiceAsync sessionService = GWT.create(SessionService.class);
	private static final HolidayDaysServiceAsync holidayDaysService = GWT.create(HolidayDaysService.class);
	
	/**
	 * Resource with custom css styles for Data Grid.
	 */
	private static final DataGrid.Resources customDataGridResources = GWT.create(CustomDataGridResources.class);

	/**
	 * Resource with custom css styles for {@link CellTable}.
	 */
	private static final CellTable.Resources customCellTableResources = GWT.create(CustomCellTableResources.class);
	
	/**
	 * Instance that holds info about logged in user. 
	 */
	private static User loggedInUser;
	
	/**
	 * Global observable for changes in approval steps data.
	 */
	private static final GeneralObservable approvalStepGeneralObservable = new GeneralObservable();
	
	/**
	 * Global observable for changes in vacations data.
	 */
	private static final GeneralObservable vacationGeneralObservable = new GeneralObservable();
	
	/**
	 * Returns the proxy of ApprovalServiceImpl
	 * 
	 * @return the proxy object
	 */
	public static ApprovalServiceAsync getApprovalService() {
		return approvalService;
	}

	/**
	 * Returns the proxy of UserServiceImpl
	 * 
	 * @return the proxy object
	 */
	public static UsersServiceAsync getUsersService() {
		return usersService;
	}

	/**
	 * Returns the proxy of VacationsServiceImpl
	 * 
	 * @return the proxy object
	 */
	public static VacationsServiceAsync getVacationsService() {
		return vacationsService;
	}
	
	/**
	 * Returns the proxy of RoleServiceImpl
	 * 
	 * @return the proxy object
	 */
	public static RoleServiceAsync getRoleService() {
		return roleService;
	}
	
	/**
	 * Returns the proxy of SessionServiceImpl
	 * 
	 * @return the proxy object
	 */
	public static SessionServiceAsync getSessionService() {
		return sessionService;
	}
	
	/**
	 * Returns the proxy of HolidayDaysServiceImpl
	 * 
	 * @return the proxy object
	 */
	public static HolidayDaysServiceAsync getHolidayDaysService() {
		return holidayDaysService;
	}

	/**
	 * Returns info about logged in user.
	 * 
	 * @return User object with logged in user's info
	 */
	public static User getLoggedInUser() {
		return loggedInUser;
	}

	/**
	 * Returns custom {@link DataGrid.Resources} implementation specially changed for application.
	 * 
	 * @return custom DataGrid.Resources
	 */
	public static DataGrid.Resources getCustomDataGridResources() {
		return customDataGridResources;
	}
	
	/**
	 * Returns custom {@link CellTable.Resources} implementation specially changed for application.
	 * 
	 * @return custom CellTable.Resources
	 */
	public static CellTable.Resources getCustomCellTableResources() {
		return customCellTableResources;
	}
	
	/**
	 * Returns global Observable for approval steps.
	 * 
	 * @return global observable for approval steps.
	 */
	public static GeneralObservable getApprovalStepGeneralObservable() {
		return approvalStepGeneralObservable;
	}

	/**
	 * Returns global Observable for vacations.
	 * 
	 * @return global observable for vacations.
	 */
	public static GeneralObservable getVacationGeneralObservable() {
		return vacationGeneralObservable;
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		/* Attach custom css resource for application */
		CustomApplicationResource.INSTANCE.css().ensureInjected();
		
		/* Add listener that listen for browser closing event. */
		Window.addWindowClosingHandler(new ClosingHandler() {
			
			@Override
			public void onWindowClosing(final ClosingEvent event) {
				//event.setMessage("Are you sure?");
				sessionService.invalidateSession(new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						
						 
						/*
						 * Check that the reason of the error isn't a StatusCodeException:0 because this
						 * error is just a notification of the browser that client has left the page. 
						 */						 
						if (!"0".equals(caught.getMessage().trim())) {
							Dialog.showSimpleMessage("Problem during session invalidating occured.", Dialog.TITLE_ERROR);						
						}
						
					}

					@Override
					public void onSuccess(Void result) {
						// Do nothing
					}
				});
			}
		});
		
		RootPanel isAdminContainer = RootPanel.get("isAdminContainer");
		
		boolean isAdmin = false;
		
		/*
		 *  Check if this container exists. It exists only if user has role with ADMIN 
		 *  permissions because in other case Spring Security will remove it.
		 *  This is done by such tag:
		 *  <security:authorize access="hasAdminRights()">
		 *  	...
		 *  </security:authorize>
		 */
		if (isAdminContainer != null) {
			isAdmin = true;
		}
		
		RootPanel vacationsListContainer = RootPanel.get("vacationsListContainer");
		MainForm vacationsDetailsForm = new MainForm(isAdmin);
		vacationsListContainer.add(vacationsDetailsForm);
		
		ApprovalListForm approvalListForm = new ApprovalListForm();
		RootPanel approvalListContainer = RootPanel.get("vaitingOnApprovalContainer");
		approvalListContainer.add(approvalListForm);
		
		RootPanel vacationDaysContainer = RootPanel.get("remainingVacationDaysContainer");
		final RemainingVacationDaysForm vacationDaysForm = new RemainingVacationDaysForm();
		vacationDaysContainer.add(vacationDaysForm);
		
		usersService.getLoggedInUser(new AsyncCallback<User>() {
			
			@Override
			public void onSuccess(User user) {
				loggedInUser = user;
				
				vacationDaysForm.setRemainingVacationDays(user.getVacationDays());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// I need to add logging.
				Dialog.showSimpleMessage("Error occured during getting logged in user's info: " + caught.getMessage(), 
						Dialog.TITLE_ERROR);
				
			}
		});
		
		/* Add change password button to the left top coner */
		RootPanel changePasswordButtonContainer = RootPanel.get("changePasswordButtonContainer");
		Button changePasswordButton = new Button("Change password");
		changePasswordButton.setHeight("20px");
		changePasswordButton.getElement().getStyle().setMargin(0, Unit.PX);
		changePasswordButton.getElement().getStyle().setPaddingTop(0, Unit.PX);
		changePasswordButton.getElement().getStyle().setPaddingBottom(0, Unit.PX);		
		
		changePasswordButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				/*Dialog.showSimpleMessage("Functionality have not been implemented yet", 
						Dialog.TITLE_INFORMATION);*/
				PasswordChangingWindow.getInstance().show();
			}
		});
		changePasswordButtonContainer.add(changePasswordButton);
		
	}
	
}
