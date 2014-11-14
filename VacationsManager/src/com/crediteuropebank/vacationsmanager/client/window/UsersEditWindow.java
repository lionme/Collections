package com.crediteuropebank.vacationsmanager.client.window;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.IntegerTextField;
import com.crediteuropebank.vacationsmanager.client.widget.ObjectListBoxField;
import com.crediteuropebank.vacationsmanager.client.widget.TextField;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * 
 * User editing window for administration roles. 
 * 
 * Is not used now.
 * 
 * @author dimas
 *
 */
@Deprecated
public class UsersEditWindow {
	
	/**
	 * 
	 * @author dimas
	 *
	 */
	private static class UserAsynchDataProvider extends AsyncDataProvider<User> {
		private HasData<User> display;
		
		@Override
		protected void onRangeChanged(HasData<User> display) {
			this.display = display;
			
			final Range range = display.getVisibleRange();
			final int start = range.getStart();
			final int end = start + range.getLength();

			AsyncCallback<List<User>> callback = new CustomAsyncCallback<List<User>>() {

				@Override
				public void onSuccessExecution(List<User> result) {
					if (result.size() < end) {
						updateRowData(start, result.subList(start, result.size())); 
					} else {
						updateRowData(start, result.subList(start, end));
					}
					
					updateRowCount(result.size(), true); 
				}
				
			};
			
			VacationsManager.getUsersService().getAllUsersList(callback);
		}
		
		/**
		 * This method force the data provider to update his data from DB.
		 */
		public void updateView() {
			if (display!=null) {
				onRangeChanged(display);
			}
		}

	}
	
	/**
	 * An instance of UsersService proxy class.
	 */
	//private final static UsersServiceAsync usersService = GWT.create(UsersService.class);
	
	/**
	 *  Width of the label of TextField
	 */
	private static final int USER_DETAIL_LABEL_WIDTH = 70;
	
	/**
	 *  Width of the label of TextField
	 */
	private static final int VACATION_DAYS_LABEL_WIDTH = 140;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private static final int USER_DETAIL_TEXTFIELD_WIDTH = 280;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private static final int VACATION_DAYS_TEXTFIELD_WIDTH = 260;
	
	/**
	 *  Width of the buttons on the user's details panel (string value)
	 */
	private static final String USER_DETAIL_BUTTONS_WIDTH = "60px";
	
	/**
	 * Width of the users table.
	 */
	private static final String USER_TABLE_WIDTH = "1000px";
	
	/**
	 * Height of the users table.
	 */
	private static final String USER_TABLE_HEIGHT = "312px";

	/**
	 * Instance of user editing window.
	 */
	private static DialogBox winModal;
	
	/**
	 * The instance of data provider.
	 */
	private static UserAsynchDataProvider dataProvider;
	
	/**
	 * Selection model for user's table.
	 */
	private static SingleSelectionModel<User> selectionModel;
	
	// TextField for user's details panel.
	private static TextField usernameTF;
	private static TextField passwordTF;
	private static TextField fullnameTF;
	private static ObjectListBoxField<Role> roleLBF;
	private static TextField eMailTF;
	//private static ObjectListBoxField<User> headerLBF;
	
	// Buttons for user's details panel.
	private static Button userDetailsCreateButton;
	private static Button userDetailsUpdateButton;
	private static Button userDetailsDeleteButton;
	private static Button userDetailsClearButton;
	
	/**
	 * This variable holds the instance of User class, which was selected in the user's table.
	 */
	private static User selectedUser;

	public static void showWindow() {
		
		winModal = new DialogBox();  

		// set unique id for applying css rules
		winModal.getElement().setId("usersDialogBox");

		winModal.setText("Add/Update/Delete user");

		Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				winModal.hide();
				winModal = null;
			}
		});

		HTML msg = new HTML("<center>Active user's list</center>",true);

		DockPanel dock = new DockPanel();
		dock.setSpacing(4);
		//dock.setWidth("500px");

		dock.add(closeButton, DockPanel.SOUTH);
		dock.add(msg, DockPanel.NORTH);

		HorizontalPanel mainPanel = new HorizontalPanel();
		mainPanel.setSpacing(10);

		// Create user table
		DataGrid<User> usersTable = createUserTable();
		DecoratorPanel decorator = new DecoratorPanel();
		decorator.setWidget(usersTable);
		// Add user table to panel
		mainPanel.add(decorator);
		
		// Create user details panel
		VerticalPanel userDetailPanel = createUserDetailsPanel();
		DecoratorPanel userDetailsDecorator = new DecoratorPanel();
		userDetailsDecorator.setWidget(userDetailPanel);
		// Add user details panel
		mainPanel.add(userDetailsDecorator);

		dock.add(mainPanel, DockPanel.CENTER);
		dock.setCellHorizontalAlignment(closeButton, DockPanel.ALIGN_RIGHT);

		// Change the user details panel's butons states.
		changeButtonsStateToCreate();
		//clearUserDetailsPanel();
		
		winModal.setWidget(dock);
		winModal.center();
	}

	/**
	 * This method creates table (DataGrid) where list all users.
	 * @return the DataGrid for listing existed users.
	 */
	private static DataGrid<User> createUserTable() {
	    /*
	     * Define a key provider for a User. We use the unique ID as the key.
	     */
	    ProvidesKey<User> keyProvider = new ProvidesKey<User>() {
	      public Object getKey(User item) {
	        // Always do a null check.
	        return (item == null) ? null : item.getId();
	      }
	    };
		
		// Create a DataGrid.
		DataGrid<User> table = new DataGrid<User>(keyProvider);
		table.setHeight(USER_TABLE_HEIGHT);
		table.setWidth(USER_TABLE_WIDTH);
		
		// Create id column.
		Column<User, Number> idColumn = new Column<User, Number>(new NumberCell()) {
			@Override
			public Number getValue(User user) {
				return user.getId();
			}
		};

		// Create username column.
		Column<User, String> usernameColumn =
				new Column<User, String>(new TextCell()) {
			@Override
			public String getValue(User user) {
				return user.getUsername();
			}
		};
		// For AsyncDataProvider we don't need client side sorting.
		//usernameColumn.setSortable(true);

		// Create password column.
		TextColumn<User> passwordColumn = new TextColumn<User>() {
			@Override
			public String getValue(User user) {
				return user.getPassword();
			}
		};

		// Create fullname column. (was EditTextCell)
		Column<User, String> fullnameColumn =
				new Column<User, String>(new TextCell()) {
			@Override
			public String getValue(User user) {
				return user.getFullName();
			}
		};

		// Create role column.
		TextColumn<User> roleColumn = new TextColumn<User>() {
			@Override
			public String getValue(User user) {
				return user.getRole().getName();
			}
		};
		
		// Create eMail column.
		TextColumn<User> eMailColumn = new TextColumn<User>() {
			@Override
			public String getValue(User user) {
				return user.geteMail();
			}
		};
		
		// Create superviser id column.
		/*Column<User, Number> headerIdColumn = new Column<User, Number>(new NumberCell()) {
			@Override
			public Number getValue(User user) {
				// Check whether suoervisor != null, in other case return 0.
				return (user.getHeader()!=null)?user.getHeader().getId():0;
			}
		};*/

		// Add the columns.
		table.addColumn(idColumn, "ID");
		table.setColumnWidth(idColumn, 30, Unit.PX);
		table.addColumn(usernameColumn, "Username");
		table.setColumnWidth(usernameColumn, 75, Unit.PX);
		table.addColumn(passwordColumn, "Password");
		table.setColumnWidth(passwordColumn, 75, Unit.PX);
		table.addColumn(fullnameColumn, "Full name");
		table.setColumnWidth(fullnameColumn, 150, Unit.PX);
		table.addColumn(roleColumn, "Role");
		table.setColumnWidth(roleColumn, 65, Unit.PX);
		table.addColumn(eMailColumn, "EMail");
		table.setColumnWidth(eMailColumn, 110, Unit.PX);
		/*table.addColumn(headerIdColumn, "Header's ID");
		table.setColumnWidth(headerIdColumn, 70, Unit.PX);*/

		// Create a data provider.
		//ListDataProvider<User> dataProvider = new ListDataProvider<User>();
		dataProvider = new UserAsynchDataProvider();

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Add the data to the data provider, which automatically pushes it to the
		// widget.
		/*List<User> usersList = testUserList();
		List<User> dataProviderList = dataProvider.getList();
		
		dataProviderList.addAll(usersList);*/

		// Add a ColumnSortEvent.ListHandler to connect sorting to the
		// java.util.List.
		// For AsyncdataProvider I implement server side sorting.
/*		ListHandler<User> columnSortHandler = new ListHandler<User>(dataProviderList);
		
		columnSortHandler.setComparator(usernameColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				if (o1 == o2) {
					return 0;
				}

				// Compare the name columns.
				if (o1 != null) {
					return (o2 != null) ? o1.getUsername().compareTo(o2.getUsername()) : 1;
				}
				return -1;
			}
		});
		
		table.addColumnSortHandler(columnSortHandler);*/

		// We know that the data is sorted alphabetically by default. (Works wrong in my case)
		//table.getColumnSortList().push(usernameColumn);
		
		// Add a selection model to handle user selection.
		selectionModel = new SingleSelectionModel<User>();
		table.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				User selected = selectionModel.getSelectedObject();
				if (selected != null) {
					fillUserDetailsPanel(selected);
					changeButtonsStateToEdit();
				}
			}
		});

		return table;
	}

	/**
	 * This method creates VerticalPanel with user details.
	 * 
	 * @return the VerticalPanel with necessary user's details fields and buttons.
	 */
	private static VerticalPanel createUserDetailsPanel() {
		VerticalPanel userDetailsPanel = new VerticalPanel();
		userDetailsPanel.setSpacing(10);

		usernameTF = new TextField();
		//usernameTF.setNotEmpty(true);
		usernameTF.setLabelText("Username");
		usernameTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		usernameTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(usernameTF);
		
		passwordTF = new TextField();
		//passwordTF.setNotEmpty(true);
		passwordTF.setLabelText("Password");
		passwordTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		passwordTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(passwordTF);
		
		fullnameTF = new TextField();
		//fullnameTF.setNotEmpty(true);
		fullnameTF.setLabelText("Full name");
		fullnameTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		fullnameTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(fullnameTF);
		
		roleLBF = new ObjectListBoxField<Role>();
		roleLBF.setLabelText("Role");
		roleLBF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		roleLBF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		// Fills the role ListBox
		/*UserRole[] roles = UserRole.values();
		for (int i=0; i< roles.length; i++) {
			String role = roles[i].toString();
			roleLBF.addItem(role, role);
		}*/
		
		VacationsManager.getRoleService().getAllRoles(new CustomAsyncCallback<List<Role>>() {

			@Override
			public void onSuccessExecution(List<Role> roles) {
				for(Role role: roles) {
					roleLBF.addItem(role.getId(), role.getName(), role);
				}
			}
		});
		
		userDetailsPanel.add(roleLBF);
		roleLBF.setSelectedIndex(-1);
		
		/*roleLBF.addChangeHandler(new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event) {
				try {
					// Check whether DEVELOPER role is selected
					if(UserRole.DEVELOPER.toString().equals(roleLBF.getFieldValue())) {
						headerLBF.setEnabled(true);
						fillHeaderListBoxBySupervisors();
					} 
					// Check whether SUPERVISOR role is selected
					else if(UserRole.SUPERVISOR.toString().equals(roleLBF.getFieldValue())) {
						headerLBF.setEnabled(true);
						fillHeaderListBoxByHeaders();
					} else {
						headerLBF.setEnabled(false);
					}
				} catch (ClientSideValidationException e) {
					// Do nothing because this is not necessary thing;
				}
			}
			
		});*/
		
		eMailTF = new TextField();
		//eMailTF.setNotEmpty(true);
		eMailTF.setLabelText("EMail");
		eMailTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		eMailTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(eMailTF);
		
		/*headerLBF = new ObjectListBoxField<User>();
		headerLBF.setNotEmpty(true);
		headerLBF.setLabelText("Direct Header");
		headerLBF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		headerLBF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		//headerLBF.setEnabled(false);
		userDetailsPanel.add(headerLBF);
		headerLBF.addItem(0L, "None", null);
			
		// Fill ListBox by users from DB
		usersService.getAllUsersList(new CustomAsyncCallback<List<User>>() {

			@Override
			public void onSuccessExecution(List<User> users) {
				for (User user: users) {
					headerLBF.addItem(user.getId(), user.getFullName(), user);
				}
				
			}
		});
		
		headerLBF.setSelectedIndex(0);*/
		
		// Create a vacation days panel
		
		VerticalPanel vacationDaysPanel = new VerticalPanel();
		vacationDaysPanel.setSpacing(5);
		
		IntegerTextField twoWeeksVacationsAmountTF = new IntegerTextField();
		twoWeeksVacationsAmountTF.setWidth(VACATION_DAYS_TEXTFIELD_WIDTH);
		twoWeeksVacationsAmountTF.setLabelWidth(VACATION_DAYS_LABEL_WIDTH);
		twoWeeksVacationsAmountTF.setLabelText("Amount of two weeks vacations");
		vacationDaysPanel.add(twoWeeksVacationsAmountTF);
		
		IntegerTextField oneWeeksVacationsAmountTF = new IntegerTextField();
		oneWeeksVacationsAmountTF.setWidth(VACATION_DAYS_TEXTFIELD_WIDTH);
		oneWeeksVacationsAmountTF.setLabelWidth(VACATION_DAYS_LABEL_WIDTH);
		oneWeeksVacationsAmountTF.setLabelText("Amount of one weeks vacations");
		vacationDaysPanel.add(oneWeeksVacationsAmountTF);
		
		IntegerTextField dayVacationsAmountTF = new IntegerTextField();
		dayVacationsAmountTF.setWidth(VACATION_DAYS_TEXTFIELD_WIDTH);
		dayVacationsAmountTF.setLabelWidth(VACATION_DAYS_LABEL_WIDTH);
		dayVacationsAmountTF.setLabelText("Amount of a day vacations");
		vacationDaysPanel.add(dayVacationsAmountTF);
		
		DecoratorPanel vacationDaysDecorator = new DecoratorPanel();
		vacationDaysDecorator.setWidget(vacationDaysPanel);
		userDetailsPanel.add(vacationDaysDecorator);
		
		// Create panel with buttons
		HorizontalPanel buttonsPanel = new HorizontalPanel();
		buttonsPanel.setSpacing(5);
		
		userDetailsCreateButton = new Button("Create");
		userDetailsCreateButton.setWidth(USER_DETAIL_BUTTONS_WIDTH);
		userDetailsCreateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveUser();
			}
		});
		buttonsPanel.add(userDetailsCreateButton);
		
		userDetailsUpdateButton = new Button("Update");
		userDetailsUpdateButton.setWidth(USER_DETAIL_BUTTONS_WIDTH);
		userDetailsUpdateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateUser();
			}
		});
		buttonsPanel.add(userDetailsUpdateButton);
		
		userDetailsDeleteButton = new Button("Delete");
		userDetailsDeleteButton.setWidth(USER_DETAIL_BUTTONS_WIDTH);
		userDetailsDeleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteUser();
			}
		});
		buttonsPanel.add(userDetailsDeleteButton);
		
		userDetailsClearButton = new Button("Clear");
		userDetailsClearButton.setWidth(USER_DETAIL_BUTTONS_WIDTH);
		userDetailsClearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearUserDetailsPanel();
			}
		});
		buttonsPanel.add(userDetailsClearButton);
		
		DecoratorPanel buttonsDecorator = new DecoratorPanel();
		buttonsDecorator.setWidget(buttonsPanel);
		userDetailsPanel.add(buttonsDecorator);
		userDetailsPanel.setCellHorizontalAlignment(buttonsDecorator, 
				HasHorizontalAlignment.ALIGN_CENTER);

		return userDetailsPanel;
	}
	
/*	private static void fillHeaderListBoxByHeaders() {
		headerLBF.removeAllItems();
		headerLBF.addItem(0L, "None", null);
		for (User user: listOfHeaders) {
			headerLBF.addItem(user.getId(), 
					user.getUsername(), 
					user);
		}
		headerLBF.setSelectedIndex(0);
	}
	
	private static void fillHeaderListBoxBySupervisors() {
		headerLBF.removeAllItems();
		headerLBF.addItem(0L, "None", null);
		for (User user: listOfSupervisors) {
			headerLBF.addItem(user.getId(), 
					user.getUsername(), 
					user);
		}
		headerLBF.setSelectedIndex(0);
	}*/

	/**
	 * Now is not used. Uses for test on first stages
	 * @return
	 */
/*	private static List<User> testUserList() {
		List<User> usersList = new ArrayList<User>();

		User user = new User();	
		user.setId(1);
		user.setUsername("VASIL");
		user.setPassword("12345678");
		user.setFullName("Prosto Vasil");
		user.setRole(Roles.HEADER.toString());

		usersList.add(user);

		user = new User();	
		user.setId(2);
		user.setUsername("TESTER");
		user.setPassword("12345678");
		user.setFullName("test superviser");
		user.setRole(Roles.SUPERVISOR.toString());

		usersList.add(user);

		user = new User();	
		user.setId(3);
		user.setUsername("IGOR");
		user.setPassword("12345678");
		user.setFullName("Igor Vorobiov");
		user.setRole(Roles.DEVELOPER.toString());

		usersList.add(user);

		return usersList;
	}*/
	
	private static void fillUserDetailsPanel(User user) {
		usernameTF.setFieldValue(user.getUsername());
		passwordTF.setFieldValue(user.getPassword());
		fullnameTF.setFieldValue(user.getFullName());
		roleLBF.setSelectedObject(user.getRole().getId());
		eMailTF.setFieldValue(user.geteMail());
		// Set supervisor field enabled only for DEVELOPER type of user.
		/*if(UserRole.DEVELOPER.toString().equals(user.getRole())) {
			headerLBF.setEnabled(true);
			fillHeaderListBoxBySupervisors();
		} else if(UserRole.SUPERVISOR.toString().equals(user.getRole())) {
			headerLBF.setEnabled(true);
			fillHeaderListBoxByHeaders();
		} else {
			headerLBF.setEnabled(false);
		}*/
		// Check whether suoervisor != null, in other case return 0.
		/*long headerId = (user.getHeader()!=null)?user.getHeader().getId():0;
		headerLBF.setSelectedObject(headerId);*/
		selectedUser = user;
	}
	
	private static void changeButtonsStateToCreate() {
		userDetailsCreateButton.setEnabled(true);
		userDetailsUpdateButton.setEnabled(false);
		userDetailsDeleteButton.setEnabled(false);
		userDetailsClearButton.setEnabled(false);
	}
	
	private static void changeButtonsStateToEdit() {
		userDetailsCreateButton.setEnabled(false);
		userDetailsUpdateButton.setEnabled(true);
		userDetailsDeleteButton.setEnabled(true);
		userDetailsClearButton.setEnabled(true);
	}
	
	private static void clearUserDetailsPanel() {
		usernameTF.setFieldValue("");
		passwordTF.setFieldValue("");
		fullnameTF.setFieldValue("");
		roleLBF.setSelectedIndex(-1);
		eMailTF.setFieldValue("");
		//headerLBF.setSelectedIndex(0);
		//headerLBF.setEnabled(false);
		
		// Release selected user instance.
		selectedUser = null;
		
		changeButtonsStateToCreate();
		
		deselectTableRow();
	}
	
	private static  void deselectTableRow() {
		if (selectionModel != null) {
			User selectedVacation = selectionModel.getSelectedObject();
			if (selectedVacation== null ) {
				return;
			}
			selectionModel.setSelected(selectedVacation, false);
		}
	}
	
	/**
	 * This method gets data from fields on the screen, creates new instance of User class and
	 * sends request to server for saving this user.
	 */
	private static void saveUser() {
		User user = new User();
		user.setUsername(usernameTF.getFieldValue());
		user.setPassword(passwordTF.getFieldValue());
		user.setFullName(fullnameTF.getFieldValue());
		user.setRole(roleLBF.getSelectedObject());
		user.seteMail(eMailTF.getFieldValue());

		// If all is OK - send request to server to create new user.
		VacationsManager.getUsersService().saveUser(user, new CustomAsyncCallback<User>(){

			@Override
			public void onSuccessExecution(User result) {
				if (dataProvider!= null) {
					dataProvider.updateView();
				}

				clearUserDetailsPanel();
			}});
	}

	private static void updateUser() {
		if (selectedUser != null) {
			selectedUser.setUsername(usernameTF.getFieldValue());
			selectedUser.setPassword(passwordTF.getFieldValue());
			selectedUser.setFullName(fullnameTF.getFieldValue());
			selectedUser.setRole(roleLBF.getSelectedObject());
			selectedUser.seteMail(eMailTF.getFieldValue());
			/*selectedUser.setHeader(headerLBF.getSelectedObject());*/

			// If all is OK - send request to server to update existed user.
			VacationsManager.getUsersService().updateUser(selectedUser, new CustomAsyncCallback<Void>() {
				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.updateView();
					}

					clearUserDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select user for updating!", 
					Dialog.TITLE_WARNING);
		}
	}
	
	private static void deleteUser() {
		if (selectedUser != null) {
			VacationsManager.getUsersService().deleteUser(selectedUser, new CustomAsyncCallback<Void>() {
				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.updateView();
					}
					
					clearUserDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select user for deleting!", 
					Dialog.TITLE_WARNING);
		}
	}

}
