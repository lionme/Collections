package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.Date;
import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.widget.DateField;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.TextField;
import com.crediteuropebank.vacationsmanager.shared.Constants;
import com.crediteuropebank.vacationsmanager.shared.domain.HolidayDays;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * 
 * This tab is used to display a list of all holiday days and to add/update/delete 
 * holiday days records.
 * 
 * @author dimas
 *
 */
public class HolidayDaysListTab extends Composite implements CustomTab  {

	/**
	 * 
	 * Data provider for holiday days list table.
	 * 
	 * @author dimas
	 *
	 */
	private class HolidayDaysAsynchDataProvider extends CustomAbstractDataProvider<HolidayDays> implements UpdatableDataProvider<HolidayDays> {
		
		@Override
		protected void onRangeChanged(HasData<HolidayDays> display) {
		
			final Range range = display.getVisibleRange();
			final int start = range.getStart();
			final int end = start + range.getLength();

			AsyncCallback<List<HolidayDays>> callback = new CustomAsyncCallback<List<HolidayDays>>() {

				@Override
				public void onSuccessExecution(List<HolidayDays> result) {
					if (result.size() < end) {
						updateRowData(start, result.subList(start, result.size())); 
					} else {
						updateRowData(start, result.subList(start, end));
					}
					
					updateRowCount(result.size(), true); 
				}
				
			};
			
			VacationsManager.getHolidayDaysService().getAllHolidayDays(callback);
		}
		
		/**
		 * This method force the data provider to update his data from DB.
		 */
		public void updateView(HasData<HolidayDays> display) {
			if (display!=null) {
				onRangeChanged(display);
			}
		}
	}
	
	/**
	 *  Width of the label of TextField
	 */
	private final int LABEL_WIDTH = 100;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private final int TEXTFIELD_WIDTH = 250;
	
	/**
	 *  Width of the buttons on the user's details panel (string value)
	 */
	private final String HOLIDAY_DAYS_DETAIL_BUTTONS_WIDTH = "60px";
	
	/**
	 * Width of the holiday days table.
	 */
	private final String HOLIDAY_DAYS_TABLE_WIDTH = "650px";
	
	/**
	 * Width of the holiday days table represented as double value.
	 */
	private final double HOLIDAY_DAYS_TABLE_WIDTH_DOUBLE = 650;
	
	/**
	 * Height of the users table.
	 */
	private final String HOLIDAY_DAYS_TABLE_HEIGHT = "340px";
	
	/**
	 * An instance of main panel, in which placed all other elements.
	 */
	private final HorizontalPanel mainPanel = new HorizontalPanel();
	
	/**
	 * The instance of data provider.
	 */
	private final HolidayDaysAsynchDataProvider dataProvider = new HolidayDaysAsynchDataProvider();
	
	/**
	 * Selection model for user's table.
	 */
	private SingleSelectionModel<HolidayDays> selectionModel;
	
	/**
	 * DataGrid
	 */
	private DataGrid<HolidayDays> table;
	
	// Input widgets for holiday days details.
	private DateField startDateField;
	private DateField endDateField;
	private TextField descriptionTF;
	
	// Buttons for user's details panel.
	private Button holidayDaysCreateButton;
	private Button holidayDaysUpdateButton;
	private Button holidayDaysDeleteButton;
	private Button holidayDaysClearButton;
	
	/**
	 * This variable holds the instance of {@link HolidayDays} class, which was selected in the table.
	 */
	private HolidayDays selectedHolidayDays;
	
	public HolidayDaysListTab() {
		mainPanel.setSpacing(10);
		
		// Create user table
		VerticalPanel usersTable = createTablePanel();
		DecoratorPanel decorator = new DecoratorPanel();
		decorator.setWidget(usersTable);
		// Add user table to panel
		mainPanel.add(decorator);
		
		// Create user details panel
		VerticalPanel userDetailPanel = createDetailsPanel();
		DecoratorPanel userDetailsDecorator = new DecoratorPanel();
		userDetailsDecorator.setWidget(userDetailPanel);
		// Add user details panel
		mainPanel.add(userDetailsDecorator);

		// Change the user details panel's butons states.
		changeButtonsStateToCreate();
		
		initWidget(mainPanel);
	}
	
	/**
	 * This method creates table (DataGrid) where list all holiday days.
	 * @return the DataGrid for listing existed holiday days.
	 */
	private VerticalPanel createTablePanel() {
		
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.setSpacing(5);
		
	    /*
	     * Define a key provider for a User. We use the unique ID as the key.
	     */
	    ProvidesKey<HolidayDays> keyProvider = new ProvidesKey<HolidayDays>() {
	      public Object getKey(HolidayDays item) {
	        // Always do a null check.
	        return (item == null) ? null : item.getId();
	      }
	    };
		
		// Create a DataGrid.
		table = new DataGrid<HolidayDays>(Constants.TABLE_PAGE_SIZE, 
				VacationsManager.getCustomDataGridResources(), 
				keyProvider);
		
		table.setHeight(HOLIDAY_DAYS_TABLE_HEIGHT);
		table.setWidth(HOLIDAY_DAYS_TABLE_WIDTH);
		table.setTableWidth(HOLIDAY_DAYS_TABLE_WIDTH_DOUBLE, Unit.PX);

		// Create id column.
		Column<HolidayDays, Number> idColumn = new Column<HolidayDays, Number>(new NumberCell()) {
			@Override
			public Number getValue(HolidayDays holidayDays) {
				return holidayDays.getId();
			}
		};
		
		// Create startDate column.
		Column<HolidayDays, Date> startDateColumn = 
				new Column<HolidayDays, Date>(new DateCell(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING))) {
			@Override
			public Date getValue(HolidayDays holidayDays) {
				return holidayDays.getStartDate();
			}
		};
		
		// Create endDate column.
		Column<HolidayDays, Date> endDateColumn = 
				new Column<HolidayDays, Date>(new DateCell(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING))) {
			@Override
			public Date getValue(HolidayDays holidayDays) {
				return holidayDays.getEndDate();
			}
		};
		
		// Create description column.
		Column<HolidayDays, String> descriptionColumn =
				new Column<HolidayDays, String>(new TextCell()) {
			@Override
			public String getValue(HolidayDays holidayDays) {
				return holidayDays.getDescription();
			}
		};
		
		// Add columns to table
		table.addColumn(idColumn, "ID");
		table.setColumnWidth(idColumn, 30, Unit.PX);
		table.addColumn(startDateColumn, "Start Date");
		table.setColumnWidth(startDateColumn, 75, Unit.PX);
		table.addColumn(endDateColumn, "End Date");
		table.setColumnWidth(endDateColumn, 75, Unit.PX);
		table.addColumn(descriptionColumn, "Description");
		table.setColumnWidth(descriptionColumn, 300, Unit.PX);
		
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);
		
		// Add a selection model to handle user selection.
		selectionModel = new SingleSelectionModel<HolidayDays>();
		table.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				HolidayDays selected = selectionModel.getSelectedObject();
				if (selected != null) {
					fillDetailsPanel(selected);
					changeButtonsStateToEdit();
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
	 * This method creates VerticalPanel with holiday days details.
	 * 
	 * @return the VerticalPanel with necessary holiday days details fields and buttons.
	 */
	private VerticalPanel createDetailsPanel() {
		VerticalPanel detailsPanel = new VerticalPanel();
		detailsPanel.setSpacing(5);
		
		startDateField = new DateField();
		startDateField.setLabelText("Start date");
		startDateField.setWidth(TEXTFIELD_WIDTH);
		startDateField.setLabelWidth(LABEL_WIDTH);
		startDateField.setDateFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		detailsPanel.add(startDateField);
		
		endDateField = new DateField();
		endDateField.setLabelText("End date");
		endDateField.setWidth(TEXTFIELD_WIDTH);
		endDateField.setLabelWidth(LABEL_WIDTH);
		endDateField.setDateFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		detailsPanel.add(endDateField);
		
		descriptionTF = new TextField();
		descriptionTF.setLabelText("Description");
		descriptionTF.setWidth(TEXTFIELD_WIDTH);
		descriptionTF.setLabelWidth(LABEL_WIDTH);
		detailsPanel.add(descriptionTF);
		
		/*
		 *  Create panel with buttons
		 */
		
		HorizontalPanel buttonsPanel = new HorizontalPanel();
		buttonsPanel.setSpacing(5);

		holidayDaysCreateButton = new Button("Create");
		holidayDaysCreateButton.setWidth(HOLIDAY_DAYS_DETAIL_BUTTONS_WIDTH);
		holidayDaysCreateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveHolidayDays();
			}
		});
		buttonsPanel.add(holidayDaysCreateButton);

		holidayDaysUpdateButton = new Button("Update");
		holidayDaysUpdateButton.setWidth(HOLIDAY_DAYS_DETAIL_BUTTONS_WIDTH);
		holidayDaysUpdateButton.setEnabled(false); // disable by default
		holidayDaysUpdateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateHolidayDays();
			}
		});
		buttonsPanel.add(holidayDaysUpdateButton);

		holidayDaysDeleteButton = new Button("Delete");
		holidayDaysDeleteButton.setWidth(HOLIDAY_DAYS_DETAIL_BUTTONS_WIDTH);
		holidayDaysDeleteButton.setEnabled(false); // disable by default
		holidayDaysDeleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteHolidayDays();
			}
		});
		buttonsPanel.add(holidayDaysDeleteButton);

		holidayDaysClearButton = new Button("Clear");
		holidayDaysClearButton.setWidth(HOLIDAY_DAYS_DETAIL_BUTTONS_WIDTH);
		holidayDaysClearButton.setEnabled(false); // disable by default
		holidayDaysClearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearDetailsPanel();
			}
		});
		buttonsPanel.add(holidayDaysClearButton);

		DecoratorPanel buttonsDecorator = new DecoratorPanel();
		buttonsDecorator.setWidget(buttonsPanel);
		
		detailsPanel.add(buttonsDecorator);
		
		// Center buttons table in relation to outer panel
		detailsPanel.setCellHorizontalAlignment(buttonsDecorator, 
				HasHorizontalAlignment.ALIGN_CENTER);
		
		return detailsPanel;
	}
	
	/**
	 * Fills details panel using info from input {@link HolidayDays} object.
	 * 
	 * @param holidayDays
	 */
	private void fillDetailsPanel(HolidayDays holidayDays) {
		startDateField.setFieldValue(holidayDays.getStartDate());
		endDateField.setFieldValue(holidayDays.getEndDate());
		descriptionTF.setFieldValue(holidayDays.getDescription());
		
		selectedHolidayDays = holidayDays;
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to create
	 * new holiday days record.
	 */
	private void changeButtonsStateToCreate() {
		holidayDaysCreateButton.setEnabled(true);
		holidayDaysUpdateButton.setEnabled(false);
		holidayDaysDeleteButton.setEnabled(false);
		holidayDaysClearButton.setEnabled(false);
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to 
	 * update/delete selected holiday days record.
	 */
	private void changeButtonsStateToEdit() {
		holidayDaysCreateButton.setEnabled(false);
		holidayDaysUpdateButton.setEnabled(true);
		holidayDaysDeleteButton.setEnabled(true);
		holidayDaysClearButton.setEnabled(true);
	}
	
	/**
	 * This method clears details panel.
	 */
	private void clearDetailsPanel() {
		startDateField.setFieldValue(null);
		endDateField.setFieldValue(null);
		descriptionTF.setFieldValue("");
		
		// Release selected user instance.
		selectedHolidayDays = null;
		
		changeButtonsStateToCreate();
		
		deselectTableRow();
	}
	
	/**
	 * This method makes selected row in DataGrid unselected.
	 */
	private void deselectTableRow() {
		if (selectionModel != null) {
			HolidayDays selectedObj = selectionModel.getSelectedObject();
			if (selectedObj== null ) {
				return;
			}
			selectionModel.setSelected(selectedObj, false);
		}
	}
	
	/**
	 * This method saves holiday days record with entered data.
	 */
	private void saveHolidayDays() {
		HolidayDays holidayDays = new HolidayDays(startDateField.getFieldValue(),
				endDateField.getFieldValue(),
				descriptionTF.getFieldValue());
		
		// If all is ok - send request to server for creating new record in DB.
		VacationsManager.getHolidayDaysService().saveHolidayDays(holidayDays, 
				new CustomAsyncCallback<Void>() {

			@Override
			public void onSuccessExecution(Void result) {
				if (dataProvider!= null) {
					dataProvider.updateView(table);
				}

				clearDetailsPanel();
			}
		});
	}
	
	/**
	 * This method updates selected record with entered data.
	 */
	private void updateHolidayDays() {
		if (selectedHolidayDays != null) {
			selectedHolidayDays.setStartDate(startDateField.getFieldValue());
			selectedHolidayDays.setEndDate(endDateField.getFieldValue());
			selectedHolidayDays.setDescription(descriptionTF.getFieldValue());
			
			// if all is ok - send request to server for updating existed row in DB.
			VacationsManager.getHolidayDaysService().updateHolidayDays(selectedHolidayDays, 
					new CustomAsyncCallback<Void>() {

				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.updateView(table);
					}

					clearDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select holiday days row for updating!", 
					Dialog.TITLE_WARNING);
		}
	}
	
	/**
	 * This method deletes selected record from DB by it's id.
	 */
	private void deleteHolidayDays() {
		if (selectedHolidayDays != null) {
			VacationsManager.getHolidayDaysService().deleteHolidayDays(selectedHolidayDays, new CustomAsyncCallback<Void>() {
				@Override
				public void onSuccessExecution(Void result) {
					if (dataProvider!= null) {
						dataProvider.updateView(table);
					}
					
					clearDetailsPanel();
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
