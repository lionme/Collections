package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.DoubleTextField;
import com.crediteuropebank.vacationsmanager.client.widget.IntegerTextField;
import com.crediteuropebank.vacationsmanager.client.widget.ObjectListBoxField;
import com.crediteuropebank.vacationsmanager.client.widget.TextField;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Constants;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * 
 * This region contains logic of users list (table) displaying. It is a part of main 
 * form and is visible only for users which role has admin privilege. 
 * This region also contains logic of user adding/changing/deleting.
 * 
 * @author dimas
 *
 */
public class UsersListTab extends Composite implements CustomTab {
	
	/**
	 * 
	 * Data provider for user list table.
	 * 
	 * @author dimas
	 *
	 */
	private class UsersListAsynchDataProvider extends CustomAbstractDataProvider<User> implements UpdatableDataProvider<User> {
		
		@Override
		protected void onRangeChanged(HasData<User> display) {
		
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
		public void updateView(HasData<User> display) {
			if (display!=null) {
				onRangeChanged(display);
			}
		}
	}
	
	/**
	 *  Width of the label of TextField
	 */
	private final int USER_DETAIL_LABEL_WIDTH = 100;
	
	/**
	 *  Width of the label of TextField
	 */
	private final int VACATION_DAYS_LABEL_WIDTH = 140;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private final int USER_DETAIL_TEXTFIELD_WIDTH = 300;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private final int VACATION_DAYS_TEXTFIELD_WIDTH = 260;
	
	/**
	 *  Width of the buttons on the user's details panel (string value)
	 */
	private final String USER_DETAIL_BUTTONS_WIDTH = "60px";
	
	/**
	 * Width of the visible part of the table.
	 */
	private final String USER_TABLE_WIDTH = "860px";
	
	/**
	 * Width of the whole table. If it is greater then width of the visible part then 
	 * horizontal scrollbar appears.
	 */
	private final double WHOLE_USER_TABLE_WIDTH_DOUBLE = 1050;
	
	/**
	 * Height of the users table.
	 */
	private final String USER_TABLE_HEIGHT = "338px";
	
	/**
	 * Specifies the default amount of the two weeks vacations.
	 */
	private final int DEFAULT_TWO_WEEKS_VACATION_AMOUNT = 1;
	
	/**
	 * Specifies the default amount of the one week vacations.
	 */
	private final int DEFAULT_ONE_WEEK_VACATION_AMOUNT = 1;
	
	/**
	 * Specifies the default amount of the day vacations.
	 */
	private final double DEFAULT_DAY_VACATION_AMOUNT = 5;
	
	/**
	 * An instance of main panel, in which placed all other elements.
	 */
	private final HorizontalPanel mainPanel = new HorizontalPanel();

	/**
	 * The instance of data provider.
	 */
	private final UsersListAsynchDataProvider dataProvider = new UsersListAsynchDataProvider();;
	
	/**
	 * Selection model for user's table.
	 */
	private SingleSelectionModel<User> selectionModel;
	
	/**
	 * DataGrid
	 */
	private DataGrid<User> table;
	
	// Input widgets for user's details.
	private TextField usernameTF;
	private TextField passwordTF;
	private TextField fullnameTF;
	private ObjectListBoxField<Role> roleLBF;
	private TextField eMailTF;
	
	// Input widgets for vacations days
	private IntegerTextField twoWeeksVacationsAmountTF;
	private IntegerTextField oneWeeksVacationsAmountTF;
	private DoubleTextField dayVacationsAmountTF;
	
	// Buttons for user's details panel.
	private Button userDetailsCreateButton;
	private Button userDetailsUpdateButton;
	private Button userDetailsDeleteButton;
	private Button userDetailsClearButton;
	
	/**
	 * This variable holds the instance of User class, which was selected in the user's table.
	 */
	private User selectedUser;
	
	public UsersListTab() {
		mainPanel.setSpacing(10);

		// Create user table
		VerticalPanel usersTable = createTablePanel();
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

		// Change the user details panel's butons states.
		changeButtonsStateToCreate();
		
		initWidget(mainPanel);
	}
	
	/**
	 * This method creates table (DataGrid) where list all users.
	 * @return the DataGrid for listing existed users.
	 */
	private VerticalPanel createTablePanel() {
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.setSpacing(5);
		
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
		table = new DataGrid<User>(Constants.TABLE_PAGE_SIZE, 
				VacationsManager.getCustomDataGridResources(), 
				keyProvider);
		
		table.setHeight(USER_TABLE_HEIGHT);
		table.setWidth(USER_TABLE_WIDTH);
		table.setTableWidth(WHOLE_USER_TABLE_WIDTH_DOUBLE, Unit.PX);
		
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
		
		/*
		 *  Add the columns to the table.
		 */
		table.addColumn(idColumn, "ID");
		table.setColumnWidth(idColumn, 30, Unit.PX);
		table.addColumn(usernameColumn, "Username");
		table.setColumnWidth(usernameColumn, 75, Unit.PX);
		table.addColumn(passwordColumn, "Password");
		table.setColumnWidth(passwordColumn, 75, Unit.PX);
		table.addColumn(fullnameColumn, "Full name");
		table.setColumnWidth(fullnameColumn, 150, Unit.PX);
		table.addColumn(roleColumn, "Role");
		table.setColumnWidth(roleColumn, 100, Unit.PX);
		table.addColumn(eMailColumn, "EMail");
		table.setColumnWidth(eMailColumn, 170, Unit.PX);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

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

		// Add table to the table panel.
		tablePanel.add(table);
		
		// Create a pager.
		SimplePager pager = new SimplePager();
		// Set our DataGrid table as display.
		pager.setDisplay(table);

		DecoratorPanel pagerDecorator = new DecoratorPanel();
		pagerDecorator.setWidget(pager);

		// Add pager to panel and set it's location as "center".
		tablePanel.add(pagerDecorator);
		tablePanel.setCellHorizontalAlignment(pagerDecorator, HasHorizontalAlignment.ALIGN_CENTER);
		
		return tablePanel;
	}

	/**
	 * This method creates VerticalPanel with user details.
	 * 
	 * @return the VerticalPanel with necessary user's details fields and buttons.
	 */
	private VerticalPanel createUserDetailsPanel() {
		VerticalPanel userDetailsPanel = new VerticalPanel();
		userDetailsPanel.setSpacing(5);

		usernameTF = new TextField();
		usernameTF.setLabelText("Username");
		usernameTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		usernameTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(usernameTF);
		
		passwordTF = new TextField();
		passwordTF.setLabelText("Password");
		passwordTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		passwordTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(passwordTF);
		
		fullnameTF = new TextField();
		fullnameTF.setLabelText("Full name");
		fullnameTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		fullnameTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(fullnameTF);
		
		roleLBF = new ObjectListBoxField<Role>();
		roleLBF.setLabelText("Role");
		roleLBF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		roleLBF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		
		/*
		 * Get the list of all roles and add them to the role ListBoxField.
		 */
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
		
		eMailTF = new TextField();
		eMailTF.setLabelText("EMail");
		eMailTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		eMailTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		userDetailsPanel.add(eMailTF);
		
		/*
		 *  Create a user remaining vacation days panel.
		 */
		
		VerticalPanel vacationDaysPanel = new VerticalPanel();
		vacationDaysPanel.setSpacing(5);
		
		twoWeeksVacationsAmountTF = new IntegerTextField();
		twoWeeksVacationsAmountTF.setWidth(VACATION_DAYS_TEXTFIELD_WIDTH);
		twoWeeksVacationsAmountTF.setLabelWidth(VACATION_DAYS_LABEL_WIDTH);
		twoWeeksVacationsAmountTF.setLabelText("Amount of two weeks vacations");
		// set default value
		twoWeeksVacationsAmountTF.setFieldValue(DEFAULT_TWO_WEEKS_VACATION_AMOUNT);

		vacationDaysPanel.add(twoWeeksVacationsAmountTF);
		
		oneWeeksVacationsAmountTF = new IntegerTextField();
		oneWeeksVacationsAmountTF.setWidth(VACATION_DAYS_TEXTFIELD_WIDTH);
		oneWeeksVacationsAmountTF.setLabelWidth(VACATION_DAYS_LABEL_WIDTH);
		oneWeeksVacationsAmountTF.setLabelText("Amount of one weeks vacations");
		// set default value
		oneWeeksVacationsAmountTF.setFieldValue(DEFAULT_ONE_WEEK_VACATION_AMOUNT);

		vacationDaysPanel.add(oneWeeksVacationsAmountTF);
		
		dayVacationsAmountTF = new DoubleTextField();
		dayVacationsAmountTF.setWidth(VACATION_DAYS_TEXTFIELD_WIDTH);
		dayVacationsAmountTF.setLabelWidth(VACATION_DAYS_LABEL_WIDTH);
		dayVacationsAmountTF.setLabelText("Amount of a day vacations");
		// set default value
		dayVacationsAmountTF.setFieldValue(DEFAULT_DAY_VACATION_AMOUNT);
		vacationDaysPanel.add(dayVacationsAmountTF);
		
		DecoratorPanel vacationDaysDecorator = new DecoratorPanel();
		vacationDaysDecorator.setWidget(vacationDaysPanel);
		
		userDetailsPanel.add(vacationDaysDecorator);
		// Center vacations days panel.
		userDetailsPanel.setCellHorizontalAlignment(vacationDaysDecorator, 
						HasHorizontalAlignment.ALIGN_CENTER);
		
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
		// Center buttons panel.
		userDetailsPanel.setCellHorizontalAlignment(buttonsDecorator, 
				HasHorizontalAlignment.ALIGN_CENTER);

		return userDetailsPanel;
	}
	
	/**
	 * Fills details panel using info from input {@link User} object.
	 * 
	 * @param user
	 */
	private void fillUserDetailsPanel(User user) {
		usernameTF.setFieldValue(user.getUsername());
		
		/*
		 * Set instead of password default word "hidden".
		 */
		passwordTF.setFieldValue("hidden");
		passwordTF.setEnabled(false);
		
		fullnameTF.setFieldValue(user.getFullName());
		roleLBF.setSelectedObject(user.getRole().getId());
		eMailTF.setFieldValue(user.geteMail());

		VacationDays vacationDays = user.getVacationDays();
		
		twoWeeksVacationsAmountTF.setFieldValue(vacationDays.getTwoWeeksVacations());
		oneWeeksVacationsAmountTF.setFieldValue(vacationDays.getOneWeekVacations());
		dayVacationsAmountTF.setFieldValue(vacationDays.getDayVacations().doubleValue());
		
		selectedUser = user;
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to create
	 * new user record.
	 */
	private void changeButtonsStateToCreate() {
		userDetailsCreateButton.setEnabled(true);
		userDetailsUpdateButton.setEnabled(false);
		userDetailsDeleteButton.setEnabled(false);
		userDetailsClearButton.setEnabled(false);
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to 
	 * update/delete selected user record.
	 */
	private void changeButtonsStateToEdit() {
		userDetailsCreateButton.setEnabled(false);
		userDetailsUpdateButton.setEnabled(true);
		userDetailsDeleteButton.setEnabled(true);
		userDetailsClearButton.setEnabled(true);
	}
	
	/**
	 * This method clears details panel.
	 */
	private void clearUserDetailsPanel() {
		usernameTF.setFieldValue("");
		
		passwordTF.setFieldValue("");
		passwordTF.setEnabled(true);
		
		fullnameTF.setFieldValue("");
		roleLBF.setSelectedIndex(-1);
		eMailTF.setFieldValue("");

		/*
		 * Set default values of the fields.
		 */
		twoWeeksVacationsAmountTF.setFieldValue(DEFAULT_TWO_WEEKS_VACATION_AMOUNT);
		oneWeeksVacationsAmountTF.setFieldValue(DEFAULT_ONE_WEEK_VACATION_AMOUNT);
		dayVacationsAmountTF.setFieldValue(DEFAULT_DAY_VACATION_AMOUNT);
		
		// Release selected user instance.
		selectedUser = null;
		
		changeButtonsStateToCreate();
		
		deselectTableRow();
	}
	
	/**
	 * This method makes selected row in DataGrid unselected.
	 */
	private void deselectTableRow() {
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
	private void saveUser() {
		RemainingVacationDays vacationDays = new RemainingVacationDays(twoWeeksVacationsAmountTF.getFieldValue(),
				oneWeeksVacationsAmountTF.getFieldValue(),
				BigDecimalUtil.newBigDecimal(dayVacationsAmountTF.getFieldValue()));

		User user = new User(usernameTF.getFieldValue(),
				passwordTF.getFieldValue(),
				fullnameTF.getFieldValue(),
				roleLBF.getSelectedObject(),
				eMailTF.getFieldValue(),
				vacationDays);

		// If all is OK - send request to server to create new user.
		VacationsManager.getUsersService().saveUser(user, new CustomAsyncCallback<User>(){

			@Override
			public void onSuccessExecution(User result) {
				if (dataProvider!= null) {
					dataProvider.updateView(table);
				}

				clearUserDetailsPanel();
			}});
	}
	
	/**
	 * This method updates selected user record with entered data.
	 */
	private void updateUser() {
		if (selectedUser != null) {
			selectedUser.setUsername(usernameTF.getFieldValue());
			selectedUser.setPassword(passwordTF.getFieldValue());
			selectedUser.setFullName(fullnameTF.getFieldValue());
			selectedUser.setRole(roleLBF.getSelectedObject());
			selectedUser.seteMail(eMailTF.getFieldValue());

			RemainingVacationDays vacationDays = selectedUser.getVacationDays();
			vacationDays.setTwoWeeksVacations(twoWeeksVacationsAmountTF.getFieldValue());
			vacationDays.setOneWeekVacations(oneWeeksVacationsAmountTF.getFieldValue());
			vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(dayVacationsAmountTF.getFieldValue()));

			selectedUser.setVacationDays(vacationDays);

			// If all is OK - send request to server to update existed user.
			VacationsManager.getUsersService().updateUser(selectedUser, new CustomAsyncCallback<Void>() {
				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.updateView(table);
					}

					clearUserDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select user for updating!", 
					Dialog.TITLE_WARNING);
		}
	}
	
	/**
	 * This method deletes selected record by its id.
	 */
	private void deleteUser() {
		if (selectedUser != null) {
			VacationsManager.getUsersService().deleteUser(selectedUser, new CustomAsyncCallback<Void>() {
				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.updateView(table);
					}
					
					clearUserDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select user for deleting!", 
					Dialog.TITLE_WARNING);
		}
	}

	@Override
	public void updateContent() {
		dataProvider.updateView(table);
	}

}
