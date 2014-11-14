package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.observer.GeneralObservable;
import com.crediteuropebank.vacationsmanager.client.observer.GeneralObserver;
import com.crediteuropebank.vacationsmanager.client.widget.DateField;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.YesNoDialogHandler;
import com.crediteuropebank.vacationsmanager.client.widget.ListBoxField;
import com.crediteuropebank.vacationsmanager.client.widget.ObjectListBoxField;
import com.crediteuropebank.vacationsmanager.client.widget.TextField;
import com.crediteuropebank.vacationsmanager.client.window.ApprovalFlowWindow;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Constants;
import com.crediteuropebank.vacationsmanager.shared.VacationDaysUtil;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
import com.crediteuropebank.vacationsmanager.shared.dto.NonWorkingDaysDTO;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomValidationException;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * This region contains logic of vacations list (table) displaying. It is a part of main 
 * form. This region also contains logic of vacation adding/changing/deleting.
 * 
 * @author dimas
 *
 */
public class VacationsListTab extends Composite implements CustomTab {
	
	/**
	 * This data provider is responsible for vacations list displaying.
	 * @author dimas
	 *
	 */
	private class VacationAsyncDataProvider  extends CustomAbstractDataProvider<Vacation> 
				implements GeneralObserver {
		
		private final HasData<Vacation> display;
		
		public VacationAsyncDataProvider(HasData<Vacation> display) {
			this.display = display;
		}
		
		/**
		 * Sets default vacation selection type to "actual vacations".
		 */
		private VacationSelectionType vacationSelectionType = VacationSelectionType.JUST_ACTUAL;
		
		@Override
		protected void onRangeChanged(HasData<Vacation> display) {
		
			final Range range = display.getVisibleRange();
			final int start = range.getStart();
			final int end = start + range.getLength();

			AsyncCallback<List<Vacation>> callback = new CustomAsyncCallback<List<Vacation>>() {

				@Override
				public void onSuccessExecution(List<Vacation> result) {
					if (result.size() < end) {
						updateRowData(start, result.subList(start, result.size())); 
					} else {
						updateRowData(start, result.subList(start, end));
					}
					
					updateRowCount(result.size(), true); 
				}
			};
			
			/*
			 * Check whether date range checkbox is selected. If selected - then search by date 
			 * range, if not - then search for all dates.
			 */
			
			if (dateRangeCheckbox.getValue()) {
				Date startDate = searchStartDate.getValue();
				Date endDate = searchEndDate.getValue();

				switch(vacationSelectionType) {
				case ALL: {
					VacationsManager.getVacationsService()
					.getAllVacationsForDateRange(startDate, endDate, callback);
					break;
				}
				case JUST_ACTUAL: {
					VacationsManager.getVacationsService()
					.getActualVacationsForDateRange(startDate, endDate, callback);
					break;
				}
				case REJECTED:{
					VacationsManager.getVacationsService()
					.getRejectedVacationsForDateRange(startDate, endDate, callback);
					break;
				}
				}

			} else {
				switch(vacationSelectionType) {
				case ALL: {
					VacationsManager.getVacationsService().getAllVacations(callback);
					break;
				}
				case JUST_ACTUAL: {
					VacationsManager.getVacationsService().getActualVacations(callback);
					break;
				}
				case REJECTED:{
					VacationsManager.getVacationsService().getRejectedVacations(callback);
					break;
				}
				}
			}
			
		}
		
		/**
		 * This method forces data provider to update view with selected "selection type".
		 * 
		 * @param selectionType
		 */
		public void updateViewWithNewselectionType(VacationSelectionType selectionType) {
			this.vacationSelectionType = selectionType;
			
			update();
		}

		@Override
		public void update() {
			onRangeChanged(display);
		}

	}
	
	/**
	 *  Width of the label of TextField
	 */
	private static final int USER_DETAIL_LABEL_WIDTH = 100;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private static final int USER_DETAIL_TEXTFIELD_WIDTH = 300;
	
	/**
	 *  Width of the buttons on the vacation's details panel (string value)
	 */
	private static final String VACATION_DETAIL_BUTTONS_WIDTH = "60px";
	
	/**
	 * Width of the visible part of vacations table.
	 */
	private static final String VACATIONS_TABLE_WIDTH = "800px";
	
	/**
	 * Whole width of the vacations table.
	 */
	private static final double WHOLE_VACATIONS_TABLE_WIDTH_DOUBLE = 960;
	
	/**
	 * Height of the vacations table.
	 */
	private static final String VACATIONS_TABLE_HEIGHT = "338px";
	
	/**
	 * Maximum number of deputies
	 */
	private static final int MAX_DEPUTIES_NUMBER = 4;
	
	/**
     * Date formatter instance.
     */
    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM.yyyy");
	
	/**
	 * Panel where will be placed all other elements.
	 */
	private final VerticalPanel mainPanel = new VerticalPanel();
	
	/**
	 * Selection model used in table.
	 */
	SingleSelectionModel<Vacation> selectionModel = null;
	
	/**
	 * This variable holds an instance of selected in the table vacation.
	 */
	private Vacation selectedVacation;
	
	/*
	 *  Text fields for vacation's details panel
	 */
	private DateField startDateField;
	private DateField endDateField;
	private TextField vacationStateTF;
	private TextField vacationOwnerTF;
	private ObjectListBoxField<User> deputyLBF;
	private CheckBox halfDayCheckBox;
	
	private ObjectListBoxField<User> deputiesListLBF;
	private Button addDeputyButton;
	private Button removeDeputyButton;
	
	/*
	 *  Buttons for vacation's details panel
	 */
	private static Button vacationDetailsCreateButton;
	private static Button vacationDetailsUpdateButton;
	private static Button vacationDetailsDeleteButton;
	private static Button vacationDetailsClearButton;
	
	/**
	 * This is a button that shows approval flow details window.
	 */
	private Button showApprovalFlowButton;
	
	/**
	 * Data provider instance
	 */
	private VacationAsyncDataProvider dataProvider = null;
	
	/**
	 * Vacations table.
	 */
	private DataGrid<Vacation> table;
	
	private final GeneralObservable tabUpdateObservable = new GeneralObservable();
	
	// Widgets that specifies search by dates range
	private CheckBox dateRangeCheckbox;
	private DateBox searchStartDate;
	private DateBox searchEndDate;
	
	public VacationsListTab() {
		mainPanel.setSpacing(5);

		// Add search criteria panel.
		mainPanel.add(createSearchCriteriaPanel());
		
		// Create panel with table + details panel
		HorizontalPanel vacationsPanel = new HorizontalPanel();
		
		//vacationsPanel.setSpacing(10);
		Label spacingLabel = new Label("");
		spacingLabel.setWidth("10px");
		
		/* Create vacations table panel */
		VerticalPanel vacationTablePanel = createTablePanel();
		DecoratorPanel tableDecorator = new DecoratorPanel();
		tableDecorator.setWidget(vacationTablePanel);
		/* Add vacations table to panel */
		vacationsPanel.add(tableDecorator);
		
		/* Add spacing label */
		vacationsPanel.add(spacingLabel);
		
		/* Create vacation details panel */
		VerticalPanel vacationDetailPanel = createVacationDetailsPanel();
		DecoratorPanel vacationDetailsDecorator = new DecoratorPanel();
		vacationDetailsDecorator.setWidget(vacationDetailPanel);
		
		/* Add vacation details outer panel */
		vacationsPanel.add(vacationDetailsDecorator);
		
		mainPanel.add(vacationsPanel);
		
		initWidget(mainPanel);
	}
	
	private VerticalPanel createTablePanel() {
		
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.setSpacing(5);
		
	    /*
	     * Define a key provider for a Vacation. We use the unique ID as the key.
	     */
	    ProvidesKey<Vacation> keyProvider = new ProvidesKey<Vacation>() {
	      public Object getKey(Vacation item) {
	        // Always do a null check.
	        return (item == null) ? null : item.getId();
	      }
	    };
	    
		// Create a DataGrid with custom styles.
		table = new DataGrid<Vacation>(Constants.TABLE_PAGE_SIZE, 
				VacationsManager.getCustomDataGridResources(), 
				keyProvider);
		
		// Create id column.
		Column<Vacation, Number> idColumn = new Column<Vacation, Number>(new NumberCell()) {
			@Override
			public Number getValue(Vacation user) {
				return user.getId();
			}
		};
		
		// Create startDate column.
		Column<Vacation, Date> startDateColumn = 
				new Column<Vacation, Date>(new DateCell(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING))) {
			@Override
			public Date getValue(Vacation vacation) {
				return vacation.getStartDate();
			}
		};
		
		// Create endDate column.
		Column<Vacation, Date> endDateColumn = 
				new Column<Vacation, Date>(new DateCell(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING))) {
			@Override
			public Date getValue(Vacation vacation) {
				return vacation.getEndDate();
			}
		};
		
		// Create state column.
		Column<Vacation, String> stateColumn = new Column<Vacation, String>(new TextCell()) {
			@Override
			public String getValue(Vacation vacation) {
				return vacation.getState().toString();
			}
		};
		
		// Create user column (displayed users full name).
		Column<Vacation, String> userColumn = new Column<Vacation, String>(new TextCell()) {
			@Override
			public String getValue(Vacation vacation) {
				return (vacation.getUser()!=null)?vacation.getUser().getFullName():"NULL";
			}
		};
		
		// Create user column (displayed users full name).
		Column<Vacation, String> deputiesColumn = new Column<Vacation, String>(new TextCell()) {
			@Override
			public String getValue(Vacation vacation) {
				StringBuilder returnValue = new StringBuilder();
				List<User> deputies = vacation.getDeputies();
				
				for (User deputy: deputies) {
					returnValue.append(deputy.getFullName());
					returnValue.append("; ");
				}
				
				return returnValue.toString();
			}
		};
		
		// Add columns to table
		table.addColumn(idColumn, "ID");
		table.setColumnWidth(idColumn, 30, Unit.PX);
		table.addColumn(startDateColumn, "Start Date");
		table.setColumnWidth(startDateColumn, 75, Unit.PX);
		table.addColumn(endDateColumn, "End Date");
		table.setColumnWidth(endDateColumn, 75, Unit.PX);
		table.addColumn(stateColumn, "Vacation State");
		table.setColumnWidth(stateColumn, 120, Unit.PX);
		table.addColumn(userColumn, "User");
		table.setColumnWidth(userColumn, 150, Unit.PX);
		table.addColumn(deputiesColumn, "Deputies");
		table.setColumnWidth(deputiesColumn, 250, Unit.PX);
		
		/* Set sizes for table */
		table.setHeight(VACATIONS_TABLE_HEIGHT);
		table.setWidth(VACATIONS_TABLE_WIDTH);
		table.setMinimumTableWidth(WHOLE_VACATIONS_TABLE_WIDTH_DOUBLE, Unit.PX);
		
		// Connect the table to the data provider.
		dataProvider = new VacationAsyncDataProvider(table);
		dataProvider.addDataDisplay(table);
		
		VacationsManager.getApprovalStepGeneralObservable().attach(dataProvider);
		
		tabUpdateObservable.attach(dataProvider);
		
	    // Add a selection model to handle user selection.
		selectionModel = new SingleSelectionModel<Vacation>();
		table.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				Vacation selected = selectionModel.getSelectedObject();
				if (selected != null) {
					fillVacationDetailPanel(selected);
				}
			}
		});

		// Add table to the table panel
		tablePanel.add(table);
		
		// Create a pager
		SimplePager pager = new SimplePager();
		// Set our DataGrid table as display
		pager.setDisplay(table);

		DecoratorPanel pagerDecorator = new DecoratorPanel();
		pagerDecorator.setWidget(pager);

		// Set pager's location at center
		tablePanel.add(pagerDecorator);
		tablePanel.setCellHorizontalAlignment(pagerDecorator, HasHorizontalAlignment.ALIGN_CENTER);

		return tablePanel;
	}
	
	/**
	 * This method creates VerticalPanel with vacation's details.
	 * 
	 * @return the VerticalPanel with necessary vacation's details fields and buttons.
	 */
	private VerticalPanel createVacationDetailsPanel() {
		VerticalPanel vacationDetailsPanel = new VerticalPanel();
		vacationDetailsPanel.setSpacing(5);
		
		/*Label vacationDetailsLabel = new Label("Vacation details:");
		vacationDetailsLabel.setWidth("100%");
		vacationDetailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vacationDetailsPanel.add(vacationDetailsLabel);*/
		
		vacationOwnerTF = new TextField("Vacation owner");
		vacationOwnerTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		vacationOwnerTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		// This field should always be disabled
		vacationOwnerTF.setEnabled(false);
		vacationDetailsPanel.add(vacationOwnerTF);
		
		vacationStateTF = new TextField("Vacation state");
		vacationStateTF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		vacationStateTF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		// This field should always be disabled
		vacationStateTF.setEnabled(false);
		vacationDetailsPanel.add(vacationStateTF);
		
		
		/* Create label for adding indent before checkbox */
		Label indentLabel = new Label("");
		indentLabel.setWidth(String.valueOf(USER_DETAIL_LABEL_WIDTH) + "px");
		
		/* Crate checkbox */
		halfDayCheckBox = new CheckBox("Half Day Vacation");
		halfDayCheckBox.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (halfDayCheckBox.getValue()) {
					endDateField.setEnabled(false);
					endDateField.setFieldValue(null);
				} else {
					endDateField.setEnabled(true);
				}
			}
		});
		
		/* Create horizontal panel with spacing label and checkbox */
		HorizontalPanel checkBoxPanel = new HorizontalPanel();
		checkBoxPanel.add(indentLabel);
		checkBoxPanel.add(halfDayCheckBox);
		
		vacationDetailsPanel.add(checkBoxPanel);
		
		startDateField = new DateField("Start Date");
		startDateField.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		startDateField.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		startDateField.setDateFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		vacationDetailsPanel.add(startDateField);
		
		endDateField = new DateField("End Date");
		endDateField.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		endDateField.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		endDateField.setDateFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		vacationDetailsPanel.add(endDateField);
		
		// This label is used to add spacing between elements
		Label emptyLabel1 = new Label("");
		emptyLabel1.setWidth("5px");
		
		// Create panel for adding new deputy to list.
		HorizontalPanel newDeputyPanel = new HorizontalPanel();
		
		deputyLBF = new ObjectListBoxField<User>();
		deputyLBF.setLabelText("Select deputy");
		deputyLBF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		deputyLBF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		newDeputyPanel.add(deputyLBF);
		
		newDeputyPanel.add(emptyLabel1);
		
		// Create button for adding new deputy.
		addDeputyButton = new Button("Add");
		addDeputyButton.setHeight("25px");
		addDeputyButton.setWidth("70px");
		addDeputyButton.getElement().getStyle().setPadding(1, Unit.PX);
		newDeputyPanel.add(addDeputyButton);
		addDeputyButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				User deputy = deputyLBF.getSelectedObject();
				if ( deputy!=null && (deputiesListLBF.getNumberOfItems()< MAX_DEPUTIES_NUMBER) ) {
					deputiesListLBF.addItem(deputy.getId(), deputy.getFullName(), deputy);
				} 
				if (deputiesListLBF.getNumberOfItems() >= MAX_DEPUTIES_NUMBER) {
					addDeputyButton.setEnabled(false);
				}
			}
		});
		
		vacationDetailsPanel.add(newDeputyPanel);
				
		GeneralObserver deputiesComboboxObserver = new GeneralObserver() {
			
			@Override
			public void update() {
				deputyLBF.removeAllItems();
				
				// Fill the list box with deputies.
				VacationsManager.getUsersService().getAllUsersList(new CustomAsyncCallback<List<User>>() {

					@Override
					public void onSuccessExecution(List<User> result) {
						User loggedInUser = VacationsManager.getLoggedInUser();
						//Remove user that is currently logged in from user's list.
						if(loggedInUser!=null) {
							result.remove(loggedInUser);
						}
						
						for (User user: result) {
							deputyLBF.addItem(user.getId(), user.getFullName(), user);
						}
						deputyLBF.setSelectedIndex(-1);
					}
				});
			}
		};

		tabUpdateObservable.attach(deputiesComboboxObserver);
		
		Label emptyLabel2 = new Label("");
		emptyLabel2.setWidth("5px");
		
		// Create panel with deputies list
		HorizontalPanel deputiesListPanel = new HorizontalPanel();
		
		deputiesListLBF = new ObjectListBoxField<User>();
		deputiesListLBF.setVisibleItemCount(MAX_DEPUTIES_NUMBER);
		deputiesListLBF.setWidth(USER_DETAIL_TEXTFIELD_WIDTH);
		deputiesListLBF.setLabelWidth(USER_DETAIL_LABEL_WIDTH);
		deputiesListPanel.add(deputiesListLBF);
		
		deputiesListPanel.add(emptyLabel2);
		
		// Create button which give a possibility to delete deputy from the list.
		removeDeputyButton = new Button("Remove");
		removeDeputyButton.setHeight("25px");
		removeDeputyButton.setWidth("70px");
		removeDeputyButton.getElement().getStyle().setPadding(1, Unit.PX);
		deputiesListPanel.add(removeDeputyButton);		
		removeDeputyButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				boolean isItemRemoved = deputiesListLBF.removeSelectedItem();
				
				if (isItemRemoved && !addDeputyButton.isEnabled()) {
					addDeputyButton.setEnabled(true);
				}
			}
		});
		
		vacationDetailsPanel.add(deputiesListPanel);
		
		/*
		 * This is outer panel where placed Add/Update/Delete/Clear buttons and ShowapprovalFlow button.
		 */
		VerticalPanel buttonsOuterPannel = new VerticalPanel();
		
		/*
		 *  Create panel with buttons
		 */
		
		HorizontalPanel buttonsPanel = new HorizontalPanel();
		buttonsPanel.setSpacing(5);

		vacationDetailsCreateButton = new Button("Create");
		vacationDetailsCreateButton.setWidth(VACATION_DETAIL_BUTTONS_WIDTH);
		vacationDetailsCreateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveVacation();
			}
		});
		buttonsPanel.add(vacationDetailsCreateButton);

		vacationDetailsUpdateButton = new Button("Update");
		vacationDetailsUpdateButton.setWidth(VACATION_DETAIL_BUTTONS_WIDTH);
		vacationDetailsUpdateButton.setEnabled(false); // disable by default
		vacationDetailsUpdateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateVacation();
			}
		});
		buttonsPanel.add(vacationDetailsUpdateButton);

		vacationDetailsDeleteButton = new Button("Delete");
		vacationDetailsDeleteButton.setWidth(VACATION_DETAIL_BUTTONS_WIDTH);
		vacationDetailsDeleteButton.setEnabled(false); // disable by default
		vacationDetailsDeleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteVacation();
			}
		});
		buttonsPanel.add(vacationDetailsDeleteButton);

		vacationDetailsClearButton = new Button("Clear");
		vacationDetailsClearButton.setWidth(VACATION_DETAIL_BUTTONS_WIDTH);
		vacationDetailsClearButton.setEnabled(false); // disable by default
		vacationDetailsClearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearVacationDetailsPanel(true);
			}
		});
		buttonsPanel.add(vacationDetailsClearButton);
		
		buttonsOuterPannel.add(buttonsPanel);
		
		/* Create an empty label for adding spacing between components */
		Label spacingLabel = new Label("");
		spacingLabel.setHeight("5px");
		
		buttonsOuterPannel.add(spacingLabel);
		
		showApprovalFlowButton = new Button("Show approval flow");
		showApprovalFlowButton.setEnabled(false);
		showApprovalFlowButton.setWidth("150px");
		showApprovalFlowButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//Dialog.showSimpleMessage("Functionality have not been implemented yet", Dialog.TITLE_INFORMATION);
				ApprovalFlowWindow approvalflowWinsow = ApprovalFlowWindow.getInstance();
				
				if (selectedVacation != null) {
					approvalflowWinsow.show(selectedVacation);
				} else {
					Dialog.showSimpleMessage("You don't select vacation.", Dialog.TITLE_WARNING);
				}
			}
		});
		
		buttonsOuterPannel.add(showApprovalFlowButton);
		buttonsOuterPannel.setCellHorizontalAlignment(showApprovalFlowButton, 
				HasHorizontalAlignment.ALIGN_CENTER);

		DecoratorPanel buttonsDecorator = new DecoratorPanel();
		buttonsDecorator.setWidget(buttonsOuterPannel);
		
		vacationDetailsPanel.add(buttonsDecorator);
		vacationDetailsPanel.setCellHorizontalAlignment(buttonsDecorator, 
				HasHorizontalAlignment.ALIGN_CENTER);
		
		return vacationDetailsPanel;
	}
	
	/**
	 * This method creates panel for search criteria chose.
	 * 
	 * @return {@link HorizontalPanel} with all necessary elements.
	 */
	private HorizontalPanel createSearchCriteriaPanel() {
		// The panel with search criteria.
		HorizontalPanel searchCriteriaPanel = new HorizontalPanel();
		searchCriteriaPanel.setSpacing(10);
		
		// Create list box field for vacation selection type choosing.
		final ListBoxField vacationSelectionTypeLBF = new ListBoxField();
		
		vacationSelectionTypeLBF.setLabelText("Selection type");
		vacationSelectionTypeLBF.setLabelWidth(100);
		vacationSelectionTypeLBF.setWidth(250);		
		
		// Add values for vacation selection listbox.
		vacationSelectionTypeLBF.addItem(VacationSelectionType.JUST_ACTUAL.toString(), "Actual vacations");
		vacationSelectionTypeLBF.addItem(VacationSelectionType.ALL.toString(), "All vacations");
		vacationSelectionTypeLBF.addItem(VacationSelectionType.REJECTED.toString(), "Rejected vacations");
		vacationSelectionTypeLBF.setSelectedIndex(0);
		
		vacationSelectionTypeLBF.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String selectedValue = vacationSelectionTypeLBF.getSelectedValue();
				if (selectedValue!=null) {
					VacationSelectionType selectedType = VacationSelectionType.valueOf(selectedValue);
					dataProvider.updateViewWithNewselectionType(selectedType);
				} else {
					Dialog.showSimpleMessage("Selection type combobox is empty", Dialog.TITLE_WARNING);
				}
			}
			
		});
		
		//mainPanel.add(vacationSelectionTypeLBF);
		searchCriteriaPanel.add(vacationSelectionTypeLBF);
		
		Label spaceLabel = new Label("");	
		spaceLabel.setWidth("40px");
		searchCriteriaPanel.add(spaceLabel);
		
		dateRangeCheckbox = new CheckBox("Date range search");
		searchCriteriaPanel.add(dateRangeCheckbox);
			
		/*Label dateRangeLabel = new Label("Date range: ");	
		dateRangeLabel.setHeight("15px");
		searchCriteriaPanel.add(dateRangeLabel);*/
		
		searchStartDate = new DateBox();
		searchStartDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		searchStartDate.setWidth("70px");
		searchStartDate.setHeight("12px");		
		searchStartDate.setVisible(false);
		
		// Set current date as default value of the startDate
		searchStartDate.setValue(new Date());
		
		searchCriteriaPanel.add(searchStartDate);
		
		final Label divideLabel = new Label(" - ");	
		divideLabel.setVisible(false);
		searchCriteriaPanel.add(divideLabel);
		
		searchEndDate = new DateBox();
		searchEndDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		searchEndDate.setWidth("70px");
		searchEndDate.setHeight("12px");
		searchEndDate.setVisible(false);
		
		// Set current date as default value of the startDate
		searchEndDate.setValue(new Date());
		
		searchCriteriaPanel.add(searchEndDate);
		
		dateRangeCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()) { // if checkbox is selected
					searchStartDate.setVisible(true);
					divideLabel.setVisible(true);
					searchEndDate.setVisible(true);
				} else {
					searchStartDate.setVisible(false);
					divideLabel.setVisible(false);
					searchEndDate.setVisible(false);
				}
			}
		});
		
		return searchCriteriaPanel;
	}
	
	/**
	 * Fills vacations details fields with data from input {@link Vacation} object. 
	 * 
	 * @param vacation - vacation object.
	 */
	private void fillVacationDetailPanel(final Vacation vacation) {
		User loggedInUser = VacationsManager.getLoggedInUser();
		
		if (vacation.getUser() == null) {
			Dialog.showSimpleMessage("Problem with DB record. Vacation's owner is empty (NULL). " +
					"Please, contact to administrator.", Dialog.TITLE_WARNING);
			// And simple finish method processing
			return;
		}
		
		// Clear vacation detail before passing new data.
		clearVacationDetailsPanel(false);
		
		vacationOwnerTF.setFieldValue(vacation.getUser().getFullName());
		vacationStateTF.setFieldValue(vacation.getState().toString());
		
		boolean isHalfDayVacation = vacation.getUsedVacationDays().getDayVacations().equals(BigDecimalUtil.newBigDecimal(0.5d));
		
		halfDayCheckBox.setEnabled(false);
		startDateField.setFieldValue(vacation.getStartDate());
		// Check if chosen vacation is half day vacation.
		if (isHalfDayVacation){
			halfDayCheckBox.setValue(true);
		} else {
			endDateField.setFieldValue(vacation.getEndDate());
		}
		
		List<User> deputies = vacation.getDeputies();
		for(User deputy: deputies) {
			deputiesListLBF.addItem(deputy.getId(), deputy.getFullName(), deputy);
		}
		if (deputiesListLBF.getNumberOfItems() >= MAX_DEPUTIES_NUMBER) {
			addDeputyButton.setEnabled(false);
		}
		
		selectedVacation = vacation;
		
		// Set all input elements that can modify vacation disabled.
		addDeputyButton.setEnabled(false);
		removeDeputyButton.setEnabled(false);
		startDateField.setEnabled(false);
		endDateField.setEnabled(false);
		deputyLBF.setEnabled(false);
		
		if ( (loggedInUser != null) && (loggedInUser.getId()==vacation.getUser().getId())) {
			changeButtonsStateToEdit();
			
			// If vacation's state is REJECTED we give user a possibility to update his vacation.
			if (vacation.getState() == VacationState.REJECTED) {
				addDeputyButton.setEnabled(true);
				removeDeputyButton.setEnabled(true);
				startDateField.setEnabled(true);
				if (isHalfDayVacation) {
					endDateField.setEnabled(false);
				} else {
					endDateField.setEnabled(true);
				}
				deputyLBF.setEnabled(true);
				
				vacationDetailsUpdateButton.setEnabled(true);
			}
			
		} else {
			vacationDetailsCreateButton.setEnabled(false);
			vacationDetailsClearButton.setEnabled(true);
		}
		
		showApprovalFlowButton.setEnabled(true);
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to create
	 * new vacation record.
	 */
	private void changeButtonsStateToCreate() {
		vacationDetailsCreateButton.setEnabled(true);
		vacationDetailsUpdateButton.setEnabled(false);
		vacationDetailsDeleteButton.setEnabled(false);
		vacationDetailsClearButton.setEnabled(false);
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to 
	 * update/delete selected vacation record.
	 */
	private void changeButtonsStateToEdit() {
		vacationDetailsCreateButton.setEnabled(false);
		vacationDetailsUpdateButton.setEnabled(false);
		vacationDetailsDeleteButton.setEnabled(true);
		vacationDetailsClearButton.setEnabled(true);
	}
	
	/**
	 * Clears vacations details panel.
	 * 
	 * @param deselectTableRow determines is it necessary to make currently selected row an unselected.
	 */
	private void clearVacationDetailsPanel(boolean deselectTableRow) {
		vacationOwnerTF.setFieldValue("");
		vacationStateTF.setFieldValue("");
		
		halfDayCheckBox.setValue(false);
		halfDayCheckBox.setEnabled(true);
		
		startDateField.setFieldValue(null);
		endDateField.setFieldValue(null);
		deputyLBF.setSelectedIndex(-1);
		
		deputiesListLBF.removeAllItems();
		
		startDateField.setEnabled(true);
		endDateField.setEnabled(true);
		deputyLBF.setEnabled(true);
		
		changeButtonsStateToCreate();
		
		addDeputyButton.setEnabled(true);
		removeDeputyButton.setEnabled(true);
		
		if (deselectTableRow){
			deselectTableRow();
		}
		
		showApprovalFlowButton.setEnabled(false);
	}
	
	/**
	 * This method makes selected row in the DataGrid unselected.
	 */
	private void deselectTableRow() {
		if (selectionModel != null) {
			Vacation selectedVacation = selectionModel.getSelectedObject();
			if (selectedVacation== null ) {
				return;
			}
			selectionModel.setSelected(selectedVacation, false);
		}
	}
	
	/**
	 * This creates message that will be shown to user in dialog.
	 * 
	 * @param holidaysDaysList - list of holiday days which will be celebrated during vacation.
	 * @param vacationDuration - total vacation duration.
	 * @param numberOfWeekendDays - number of weekend days that will take place during vacation.
	 * @param usedVacationDays - vacation days that will be used for vacation.
	 * @return the String with prepared message for user.
	 */
	private String createUsersConfirmMessage(List<HolidayDays> holidaysDaysList, int vacationDuration, 
			int numberOfWeekendDays, VacationDays usedVacationDays) {
		StringBuilder message = new StringBuilder();
		
		
		int numberOfHolidayDays = 0;
		
		if (!holidaysDaysList.isEmpty()) {
			message.append("In the period of your vacation there will be such holidays and weekend days:");
			message.append("\n");
		}
		
		for (HolidayDays item: holidaysDaysList) {
			numberOfHolidayDays = numberOfHolidayDays + calculateNumberOfDaysBetween(item.getStartDate(), item.getEndDate());
			message.append(" * ");
			message.append(dateFormatter.format(item.getStartDate()));
			message.append(" - ");
			message.append(dateFormatter.format(item.getEndDate()));
			message.append(" - ");
			message.append(item.getDescription());
			message.append(";");
			message.append("\n");
		}
		
		message.append("\n");
		
		message.append("Total vacation duration");
		if (!holidaysDaysList.isEmpty()) {
			message.append(" (exclude holiday days)");
		}
		message.append(" is ");
		message.append(vacationDuration - numberOfHolidayDays);
		message.append(" days. This period included ");
		message.append(numberOfWeekendDays);
		message.append(" weekend days.");
		
		message.append("\n");
		
		message.append("For this vacation will be used:");
		message.append("\n");
		
		if (usedVacationDays.getTwoWeeksVacations() > 0) {
			message.append("   - ");
			message.append(usedVacationDays.getTwoWeeksVacations());
			message.append(" two weeks vacation(s);");
			message.append("\n");
		}
		if (usedVacationDays.getOneWeekVacations() > 0) {
			message.append("   - ");
			message.append(usedVacationDays.getOneWeekVacations());
			message.append(" one week vacation(s);");
			message.append("\n");
		}
		if (usedVacationDays.getDayVacations().doubleValue() > 0) {
			message.append("   - ");
			/* 
			 * We can't take half day vacation together with few days vacation.
			 *  
			 */
			message.append(usedVacationDays.getDayVacations().intValue());
			message.append(" day vacation(s);");
			message.append("\n");
		}
		
		message.append("\n");
		message.append("Do you agree?");
		
		return message.toString();
	}
	
	/**
	 * This method is used for calculation the number of days between two dates.
	 * 
	 * @param startDate - start date
	 * @param endDate - end date
	 * @return the number of days between two dates.
	 * @throws IllegalArgumentException if start date is grater then end date
	 */
	private int calculateNumberOfDaysBetween(Date startDate, Date endDate) {
		if (startDate.after(endDate)) {
			throw new IllegalArgumentException("End date should be grater or equals to start date");
		}
		
		long startDateTime = startDate.getTime();
		long endDateTime = endDate.getTime();
		long milPerDay = 1000*60*60*24; 
		
		int numOfDays = (int) ((endDateTime - startDateTime) / milPerDay); // calculate vacation duration in days
		
		return ( numOfDays + 1); // add one day to include start date in interval
	}
	
	/**
	 * This method returns current date without time part.
	 * 
	 * @return current date without time part
	 */
	private Date getCurrentDateWithoutTimePart() {
		return dateFormatter.parse(dateFormatter.format(new Date()));
	}
	
	/**
	 * This method validates entered vacation's info.
	 * 
	 * @param vacation - vacation to be validated.
	 * @throws CustomMessageException if some wrong data was found.
	 */
	private void validateVacationDates(Vacation vacation) throws CustomMessageException {
		if (vacation.getStartDate() == null || vacation.getEndDate() == null) {
			throw new CustomMessageException("Vacation's start and end dates cannot be empty");
		}
		
		// Validate if start date >= current date.
		if (vacation.getStartDate().before(getCurrentDateWithoutTimePart())) {
			throw new CustomMessageException("Vacation's start date shouldn't be lower then current date!");
		}
		
		// Validate whether vacation's end date >= start date
		if (vacation.getStartDate().after(vacation.getEndDate())) {
			throw new CustomMessageException("Vacation's end date should not be lower then start date!");
		}
	}
	
	/**
	 * 
	 * This method validates vacation and if all entered data is correct - shows to user confirm message, 
	 * if not - shows warning message. The further actions depends on the user's response and is specified 
	 * by {@link YesNoDialogHandler} which is taken as one of the input parameters.
	 * 
	 * Note. In this method used vacation days for vacation are set. So if you want to change something, don't forget
	 * about this.
	 * 
	 * @param vacation - vacation to be saved.
	 * @param handler - response handler (see {@link YesNoDialogHandler}})
	 */
	/* This method looks ugly. Think about how to change it */
	private void validateVacationAndShowUserConfirmMessage(final Vacation vacation, 
			final YesNoDialogHandler handler, 
			final boolean isHalfDayVacation) {
		
		if (!isHalfDayVacation) { // if user take usual vacation (not half day!)

			// Async callback for fetching non working days.
			AsyncCallback<NonWorkingDaysDTO> callback = new CustomAsyncCallback<NonWorkingDaysDTO>() {

				@Override
				public void onSuccessExecution(NonWorkingDaysDTO nonWorkingDaysDTO) {
					try {
						// Validate vacation dates.
						validateVacationDates(vacation);

						// Calculate vacation's duration
						int vacationDuration = calculateNumberOfDaysBetween(vacation.getStartDate(), vacation.getEndDate());

						// Calculate number of non working days
						int numberOfHolidayDays = 0;
						for (HolidayDays item: nonWorkingDaysDTO.holidayDays) {
							numberOfHolidayDays = numberOfHolidayDays + calculateNumberOfDaysBetween(item.getStartDate(), item.getEndDate());
						}
						
						if ((numberOfHolidayDays + nonWorkingDaysDTO.numberOfWeekendDays) == vacationDuration) {
							Dialog.showSimpleMessage("Specified vacation's period contains only holiday and weekend days!", 
									Dialog.TITLE_WARNING);
							return;
						}

						// Check if max vacation duration is exceeded
						if ( (vacationDuration - numberOfHolidayDays) > Constants.MAX_VACATION_DAYS) {
							throw new CustomMessageException("You exceeded limit of maximum vacation days. Maximum vacation length should be " + Constants.MAX_VACATION_DAYS);
						}

						VacationDays userVacationDays = null;

						/*
						 *  Check whether we update existed vacation (If vacation has vacation days then 
						 *  it have been already created). If this is update operation we have to add 
						 *  vacation days that was withdrawn previously for vacation.
						 */
						if (vacation.getUsedVacationDays() != null) {
							// Create new object for not affecting on existed one.
							userVacationDays = new RemainingVacationDays(vacation.getUser().getVacationDays());
							userVacationDays.add(vacation.getUsedVacationDays());
						} else {
							userVacationDays = vacation.getUser().getVacationDays();
						}

						// Calculate used vacation days
						UsedVacationDays usedVacationDays = VacationDaysUtil.calculateVacationDays(vacationDuration, 
								numberOfHolidayDays, 
								nonWorkingDaysDTO.numberOfWeekendDays,
								userVacationDays);

						String messageText = createUsersConfirmMessage(nonWorkingDaysDTO.holidayDays, 
								vacationDuration,
								nonWorkingDaysDTO.numberOfWeekendDays,
								usedVacationDays);

						/**
						 * if this is update operation for vacation - then update existed UsedVacationDays object,
						 * if no - create new one and set it to vacation.
						 */
						UsedVacationDays oldUsedVacationDays = vacation.getUsedVacationDays();
						if (oldUsedVacationDays != null)  {
							oldUsedVacationDays.setDayVacations(usedVacationDays.getDayVacations());
							oldUsedVacationDays.setOneWeekVacations(usedVacationDays.getOneWeekVacations());
							oldUsedVacationDays.setTwoWeeksVacations(usedVacationDays.getTwoWeeksVacations());

							// make this operation to be sure that new object was assigned
							vacation.setUsedVacationDays(oldUsedVacationDays);
						} else {
							// set vacation days that will be used for this vacation.
							vacation.setUsedVacationDays(usedVacationDays);
						}

						// Validate entered vacation data.
						//ClientValidationUtil.validate(vacation);

						Dialog.showYesNoDialog(messageText, 
								Dialog.TITLE_INFORMATION, 
								handler);

					} catch (CustomValidationException e) {
						Dialog.showMessageWithListOfItems("Wrong values have been entered:", 
								e.getErrorMessages(), 
								Dialog.TITLE_WARNING);
					} catch (CustomMessageException ex) {
						Dialog.showSimpleMessage(ex.getMessage(), Dialog.TITLE_WARNING);
					}
				}
			};

			// Make RPC for fetching list of non working days
			VacationsManager.getVacationsService().getNonWorkingDaysForVacation(vacation, callback);

		} else { // made validation and create confirm YES/NO dialog in case of half day vacation.
			
			RemainingVacationDays remainingVacationDays = null;
			/*
			 *  Check whether we update existed vacation (If vacation has vacation days then 
			 *  it have been already created). If this is update operation we have to add 
			 *  vacation days that was withdrawn previously for vacation.
			 */
			if (vacation.getUsedVacationDays() != null) {
				// Create new object for not affecting on existed one.
				remainingVacationDays = new RemainingVacationDays(vacation.getUser().getVacationDays());
				remainingVacationDays.add(vacation.getUsedVacationDays());
			} else {
				remainingVacationDays = vacation.getUser().getVacationDays();
			}
			
			if (remainingVacationDays.getDayVacations()
					.compareTo(BigDecimalUtil.newBigDecimal(0.5d)) > 0) {
				
				/* Set used vacation days */
				
				UsedVacationDays usedVacationDays = vacation.getUsedVacationDays();
				if (usedVacationDays == null) {
					usedVacationDays = new UsedVacationDays();
					usedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(0.5D));
					vacation.setUsedVacationDays(usedVacationDays);
				} else {
					usedVacationDays.setTwoWeeksVacations(0);
					usedVacationDays.setOneWeekVacations(0);
					usedVacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(0.5D));
					vacation.setUsedVacationDays(usedVacationDays);
				}
			
				String messageText = "You enter half day vacation request. Do you want to proceed?";
				
				Dialog.showYesNoDialog(messageText, 
						Dialog.TITLE_INFORMATION, 
						handler);
			} else {
				Dialog.showSimpleMessage("You din't have enough amount of day vacations for" +
						" taking half day vacation!", Dialog.TITLE_WARNING);
			}
			
		}
	}
	
	/**
	 * This method is used for saving new vacation to DB. Before saving vacation is validated. If 
	 * validation finishes without errors then necessary amount of vacation days is calculated and
	 * confirm message to user is shown.
	 */
	private void saveVacation() {
		if (VacationsManager.getLoggedInUser() != null) {
			final Vacation vacation = new Vacation();
			
			if (halfDayCheckBox.getValue()) { // if user chose half day vacation
				Date vacationDate = startDateField.getFieldValue();
				vacation.setStartDate(vacationDate);
				vacation.setEndDate(vacationDate);
			} else {
				vacation.setStartDate(startDateField.getFieldValue());
				vacation.setEndDate(endDateField.getFieldValue());	
			}
			
			vacation.setState(VacationState.JUST_OPENED);
			vacation.setUser(VacationsManager.getLoggedInUser());
			vacation.setDeputies(deputiesListLBF.getValuesList());
			
			boolean isHalfDayVacation = halfDayCheckBox.getValue();

			// Handler for processing user answer. Does he want to continue processing.
			final YesNoDialogHandler handler = new YesNoDialogHandler() {

				@Override
				public void onAnswerChoosed(boolean answer) {
					if (answer) { // If user pressed yes
						VacationsManager.getVacationsService().saveVacation(vacation, new CustomAsyncCallback<Void>() {

							@Override
							public void onSuccessExecution(Void result) {
								if (dataProvider!= null) {
									dataProvider.update();
								}

								clearVacationDetailsPanel(true);
								
								VacationsManager.getVacationGeneralObservable().notifyObservers();
							}
						});
					}
				}
			};

			/*
			 *  Validate vacation, calculate vacation days that will be used for vacation and show to user 
			 *  confirm message.
			 */
			validateVacationAndShowUserConfirmMessage(vacation, handler, isHalfDayVacation);

		} else {
			Dialog.showSimpleMessage("Logged in user's info is crashed. Please, relogin.", 
					Dialog.TITLE_ERROR);
		}
	} 
	
	// Not needed for now
	@Deprecated
	private void updateVacation() {
		if (selectedVacation != null) {
		
			if (halfDayCheckBox.getValue()) { // if user chose half day vacation
				Date vacationDate = startDateField.getFieldValue();
				selectedVacation.setStartDate(vacationDate);
				selectedVacation.setEndDate(vacationDate);
			} else {
				selectedVacation.setStartDate(startDateField.getFieldValue());
				selectedVacation.setEndDate(endDateField.getFieldValue());	
			}
			
			selectedVacation.setDeputies(deputiesListLBF.getValuesList());
			
			boolean isHalfDayVacation = halfDayCheckBox.getValue();

			// Handler for processing user answer. Does he want to continue processing.
			final YesNoDialogHandler handler = new YesNoDialogHandler() {

				@Override
				public void onAnswerChoosed(boolean answer) {
					if (answer) { // If user pressed yes
						VacationsManager.getVacationsService().updateVacationAfterRejection(selectedVacation, new CustomAsyncCallback<Void>() {

							@Override
							public void onSuccessExecution(Void result) {
								if (dataProvider!= null) {
									dataProvider.update();
								}

								clearVacationDetailsPanel(true);
								
								VacationsManager.getVacationGeneralObservable().notifyObservers();
							}
						});
					}
				}
			};

			
			 /* Validate vacation, calculate vacation days that will be used for vacation and show to user 
			 confirm message. */
			 
			validateVacationAndShowUserConfirmMessage(selectedVacation, handler, isHalfDayVacation);

		} else {
			Dialog.showSimpleMessage("You don't select vacation for updating!", 
					Dialog.TITLE_WARNING);
		}
	}
	
	/**
	 * Deletes selected row from BD by its id.
	 */
	private void deleteVacation() {
		if (selectedVacation != null) {
			VacationsManager.getVacationsService().deleteVacation(selectedVacation, new CustomAsyncCallback<Void>() {

				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.update();
					}
					
					clearVacationDetailsPanel(true);
					
					VacationsManager.getVacationGeneralObservable().notifyObservers();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select user for deleting!", 
					Dialog.TITLE_WARNING);
		}
	}

	@Override
	public void updateContent() {
		tabUpdateObservable.notifyObservers();
	}

}
