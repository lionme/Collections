package com.crediteuropebank.vacationsmanager.client.window;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.shared.Constants;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

/**
 * 	This class represents window where displayed approval flow details.
 * 
 *  This class implements Singleton pattern
 *  
 *  @author DIMAS
 *
 */
public class ApprovalFlowWindow {
	
	/* Constants that represents approval flow table size */
	private static final String APPROVAL_FLOW_TABLE_HEIGHT = "350px";	
	private static final String APPROVAL_FLOW_TABLE_WIDTH = "1000px";
	
	/* Constants that represents approval flow table's columns width */
	private static final int ID_COLUMN_WIDTH = 30;
	private static final int STATE_COLUMN_WIDTH = 100;
	private static final int APPROVER_COLUMN_WIDTH = 150;
	private static final int APPROVER_ROLE_COLUMN_WIDTH = 150;
	private static final int ORDER_NUMBER_COLUMN_WIDTH = 50;
	private static final int COMMENTS_COLUMN_WIDTH = 250;
	
	/**
	 * Holds single instance of this class.
	 */
	private static final ApprovalFlowWindow instance = new ApprovalFlowWindow();
	
	private DialogBox window = new DialogBox();
	
	private DataGrid<ApprovalStep> table;

	/**
	 * Default constructor that creates dialog
	 */
	private ApprovalFlowWindow() {
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.setSpacing(5);
		
		window.setText("Vacation's approval flow");
		
	    /*
	     * Define a key provider for a ApprovalStep. We use the unique ID as the key.
	     */
	    ProvidesKey<ApprovalStep> keyProvider = new ProvidesKey<ApprovalStep>() {
	      public Object getKey(ApprovalStep item) {
	        // Always do a null check.
	        return (item == null) ? null : item.getId();
	      }
	    };
	    
		/*
		 *  Create a DataGrid.
		 */
		table = new DataGrid<ApprovalStep>(Constants.TABLE_PAGE_SIZE, 
				VacationsManager.getCustomDataGridResources(), 
				keyProvider);
		
		table.setHeight(APPROVAL_FLOW_TABLE_HEIGHT);
		table.setWidth(APPROVAL_FLOW_TABLE_WIDTH);
		
		// Create id column.
		Column<ApprovalStep, Number> idColumn = new Column<ApprovalStep, Number>(new NumberCell()) {
			@Override
			public Number getValue(ApprovalStep approvalStep) {
				return approvalStep.getId();
			}
		};
		
		// Create approval step state column.
		Column<ApprovalStep, String> stateColumn =
				new Column<ApprovalStep, String>(new TextCell()) {
			@Override
			public String getValue(ApprovalStep approvalStep) {
				return approvalStep.getState().toString();
			}
		};

		// Create approver column
		Column<ApprovalStep, String> approverNameColumn =
				new Column<ApprovalStep, String>(new TextCell()) {
			@Override
			public String getValue(ApprovalStep approvalStep) {
				return (approvalStep.getApprover()!=null)?approvalStep.getApprover().getFullName():"";
			}
		};
		
		// Create approver role column
		Column<ApprovalStep, String> approverRoleNameColumn =
				new Column<ApprovalStep, String>(new TextCell()) {
			@Override
			public String getValue(ApprovalStep approvalStep) {
				return (approvalStep.getApproverRole()!=null)?approvalStep.getApproverRole().getName():"";
			}
		};
		
		// Create order number column
		Column<ApprovalStep, Number> orderNumberColumn = new Column<ApprovalStep, Number>(new NumberCell()) {
			@Override
			public Number getValue(ApprovalStep approvalStep) {
				return approvalStep.getRowNumber();
			}
		};
		
		// Create comments column
		Column<ApprovalStep, String> commentsColumn =
				new Column<ApprovalStep, String>(new TextCell()) {
			@Override
			public String getValue(ApprovalStep approvalStep) {
				return (approvalStep.getComments()!=null)?approvalStep.getComments():"";
			}
		};
		
		/*
		 * Add the columns to the table.
		 */
		table.addColumn(idColumn, "ID");
		table.setColumnWidth(idColumn, ID_COLUMN_WIDTH, Unit.PX);
		table.addColumn(stateColumn, "State");
		table.setColumnWidth(stateColumn, STATE_COLUMN_WIDTH, Unit.PX);
		table.addColumn(approverNameColumn, "Approver");
		table.setColumnWidth(approverNameColumn, APPROVER_COLUMN_WIDTH, Unit.PX);
		table.addColumn(approverRoleNameColumn, "Approver role");
		table.setColumnWidth(approverRoleNameColumn, APPROVER_ROLE_COLUMN_WIDTH, Unit.PX);
		table.addColumn(orderNumberColumn, "Order");
		table.setColumnWidth(orderNumberColumn, ORDER_NUMBER_COLUMN_WIDTH, Unit.PX);
		table.addColumn(commentsColumn, "Comments");
		table.setColumnWidth(commentsColumn, COMMENTS_COLUMN_WIDTH, Unit.PX);
		
		/* Create decorator for table */
		DecoratorPanel tableDecorator = new DecoratorPanel();
		tableDecorator.setWidget(table);
		
		/* Add table to table's panel */
		tablePanel.add(tableDecorator);
		
		/* Create buttons pannel */
		HorizontalPanel buttonsPanel = new HorizontalPanel();
		buttonsPanel.setSpacing(5);
		
		/* Create close button */
		Button closeButton = new Button("Close");
		closeButton.setWidth("80px");
		closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		buttonsPanel.add(closeButton);
		
		/* Add buttons panel to the table panel and set it's alignment */
		tablePanel.add(buttonsPanel);
		tablePanel.setCellHorizontalAlignment(buttonsPanel, HasHorizontalAlignment.ALIGN_RIGHT);
		
		window.add(tablePanel);
	}
	
	/**
	 * Returns instance of the {@link ApprovalFlowWindow} class. If instance have already been created - returns existed one, 
	 * if no - create it (Singleton pattern).
	 * 
	 * We don't need to think about synchronization because in GWT client is always executed in one thread.
	 * 
	 * @return single instance of the class. 
	 */
	public static ApprovalFlowWindow getInstance() {
		return instance;
	}
	
	/**
	 * This method updates data, displayed in the table on the screen.
	 * 
	 * @param vacation - vacation for which approval flow is displayed.
	 */
	private void updateTableData(Vacation vacation) {
		VacationsManager.getApprovalService().getApprovalStepsForVacation(vacation, new CustomAsyncCallback<List<ApprovalStep>>() {

			@Override
			public void onSuccessExecution(List<ApprovalStep> approvalSteps) {
				//table.setPageSize(approvalSteps.size());
				table.setRowCount(approvalSteps.size(), true);
				table.setVisibleRange(0, approvalSteps.size());
				table.setRowData(approvalSteps);
			}
		});
	}
	
	/**
	 * This method cleans data displayed in table.
	 */
	private void cleanTableData() {
		Range range = new Range(0, 0);
		
		table.setVisibleRangeAndClearData(range, false);
	}
	
	/**
	 * Shows the window at the center of the screen.
	 * 
	 * @param vacation - vacation for which data should be displayed in the window.
	 */
	public void show(Vacation vacation) {
		if (vacation == null) {
			throw new IllegalArgumentException("Input vacation object is null!");
		}
		
		updateTableData(vacation);
		window.center();
	}
	
	/**
	 * Hides the window and cleans data that was displayed in the window.
	 */
	public void hide() {
		cleanTableData();
		window.hide();
	}
	
}
