package com.crediteuropebank.vacationsmanager.client.form;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.widget.CommentedDialogHandler;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.shared.domain.ApprovalStep;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * This class contains logic of waiting on approval list displaying.
 * 
 * @author dimas
 *
 */
public class ApprovalListForm extends Composite {

	/**
	 * A custom {@link Cell} used to render a {@link ApprovalStep}.
	 */
	private static class ApprovalCell extends AbstractCell<ApprovalStep> {
		@Override
		public void render(Context context, ApprovalStep approval, SafeHtmlBuilder sb) {
			// do a null check
			if (approval == null) {
				return;
			}
			
			//String text;
			if (approval.getVacation()==null || approval.getVacation().getUser()==null) {
				sb.appendEscaped("NULL");
				return;
			} 

			// Add the vacation id.
			sb.appendHtmlConstant("<table>");
			sb.appendHtmlConstant("<tr><td rowspan='3' width='50'><b>");
			sb.appendEscaped("Id:" + String.valueOf(approval.getVacation().getId()));
			sb.appendHtmlConstant("</b></td>");

			// Add the name and address.
			sb.appendHtmlConstant("<td style='font-size:95%;'>");
			sb.appendEscaped(approval.getVacation().getUser().getFullName());
			sb.appendHtmlConstant("</td></tr><tr><td>");
			sb.appendEscaped("From: " + approval.getVacation().getStartDate().toString());
			sb.appendEscaped(" To: " + approval.getVacation().getEndDate().toString());
			sb.appendHtmlConstant("</td></tr></table>");
		}
	}
	
	/**
	 * Constant that represents the width of the buttons in approval form
	 */
	private static final String APPROVAL_BUTTONS_WIDTH = "100px";
	
	/**
	 * Constants that represents the width of the approval CellList.
	 */
	private static final String APPROVAL_CELL_LIST_WIDTH = "280px";
	
	/**
	 * Constants that holds width of the surrounding panel for approval cell list in purpose to hide 
	 * horizontal scroll bar. 
	 */
	private static final String APPROVAL_SURROUNDING_PANEL_WIDTH = "300px";
	
	/**
	 * Constants that represents the height of the approval CellList.
	 */
	private static final String APPROVAL_CELL_LIST_HEIGHT = "90px";

	/**
	 * Panel that will be bind.
	 */
	private final HorizontalPanel mainPanel = new HorizontalPanel();

	/**
	 * This variable holds an instance of selected in the CallList approval.
	 */
	private ApprovalStep selectedApprovalStep;
	
	/**
	 * Approval CellList instance
	 */
	private CellList<ApprovalStep> approvalCellList;

	public ApprovalListForm() {
		HorizontalPanel approvalPanel = new HorizontalPanel();
		approvalPanel.setSpacing(10);

		// Create approval CellList
		VerticalPanel approvalCellListPanel = createApprovalCellListPanel();
		// Wrap approval panel in decorator panel
		DecoratorPanel decorator = new DecoratorPanel();
		decorator.setWidget(approvalCellListPanel);
		approvalPanel.add(decorator);
		
		// Create approval buttons panel
		VerticalPanel buttonsPanel = createApprovalButtonsPanel();
		approvalPanel.add(buttonsPanel);
		
		// Add advanced options to form in a disclosure panel
	    DisclosurePanel advancedDisclosure = new DisclosurePanel("Vacation that are waiting for your approval...");
	    advancedDisclosure.setAnimationEnabled(true);
	    advancedDisclosure.setContent(approvalPanel);
		
		// Add vacations table to panel
		mainPanel.add(advancedDisclosure);
		
		initWidget(mainPanel);
	}

	/**
	 * This method creates vertical panel which contains CellList with vacations
	 * @return the panel with vacations CellList.
	 */
	private VerticalPanel createApprovalCellListPanel() {
		VerticalPanel returnedPanel = new VerticalPanel();
		
		FlowPanel surroundingPanel = new FlowPanel();

		final ProvidesKey<ApprovalStep> keyProvider = new ProvidesKey<ApprovalStep>() {
			public Object getKey(ApprovalStep item) {
				// Always do a null check.
				return (item == null) ? null : item.getId();
			}
		};

		approvalCellList = new CellList<ApprovalStep>(new ApprovalCell(), keyProvider);

		approvalCellList.setWidth(APPROVAL_CELL_LIST_WIDTH);
		approvalCellList.setHeight(APPROVAL_CELL_LIST_HEIGHT);

		updateApprovalStepsList();

		// Add a selection model using the same keyProvider.
		final SingleSelectionModel<ApprovalStep> selectionModel = new SingleSelectionModel<ApprovalStep>(
				keyProvider);
		approvalCellList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				//contactForm.setContact(selectionModel.getSelectedObject());
				selectedApprovalStep = selectionModel.getSelectedObject();			
			}
		});

		surroundingPanel.add(approvalCellList);
		surroundingPanel.setWidth(APPROVAL_SURROUNDING_PANEL_WIDTH);
		
		ScrollPanel scrollPanel = new ScrollPanel(surroundingPanel);
		
		// Hide horizontal scrollbar
		scrollPanel.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
		
		returnedPanel.add(scrollPanel);

		return returnedPanel;
	}
	
	/**
	 * This function makes RPC call to server and updates data in list
	 */
	private void updateApprovalStepsList() {
		VacationsManager.getApprovalService().getActiveApprovalStepsForLoggedInUser(new CustomAsyncCallback<List<ApprovalStep>>() {

			@Override
			public void onSuccessExecution(List<ApprovalStep> approvals) {
				approvalCellList.setRowCount(approvals.size());
				approvalCellList.setRowData(approvals);
			}
		});

		/* Notify all observers that need to update their vacations data */
		VacationsManager.getApprovalStepGeneralObservable().notifyObservers();
	}
	
	/**
	 * Creates panel with buttons for approval/rejection of the approval step.
	 * 
	 * @return VerticalPanel with placed inside elements.
	 */
	private VerticalPanel createApprovalButtonsPanel() {
		VerticalPanel buttonsPanel = new VerticalPanel();
		buttonsPanel.setSpacing(5);
		
		Button approveButton = new Button("Approve");
		approveButton.setWidth(APPROVAL_BUTTONS_WIDTH);
		approveButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (selectedApprovalStep == null) {
					Dialog.showSimpleMessage("You don't select vacation that you want to approve", 
							Dialog.TITLE_WARNING);
					// stop method execution and return from it
					return;
				}
				
				CommentedDialogHandler handler = new CommentedDialogHandler() {
					
					@Override
					public void onOkChoosed(String comments) {
						selectedApprovalStep.setComments(comments);
						
						VacationsManager.getApprovalService().approve(selectedApprovalStep, new CustomAsyncCallback<Void>() {

							@Override
							public void onSuccessExecution(Void result) {
								Dialog.showSimpleMessage("Selected approval step has been successfully approved.", 
										Dialog.TITLE_INFORMATION);
								updateApprovalStepsList();
								clearOldData();
							}
						});
					}

					@Override
					public void onCancelChoosed() {
						// Do nothing
						
					}
				};
				
				Dialog.showCommentsEntryDialog("Please, entry comments for approval:",
						"Approve approval step", 
						handler);
			}
		});
		buttonsPanel.add(approveButton);
		
		Button rejectButton = new Button("Reject");
		rejectButton.setWidth(APPROVAL_BUTTONS_WIDTH);
		rejectButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (selectedApprovalStep == null) {
					Dialog.showSimpleMessage("You don't select vacation that you want to approve", 
							Dialog.TITLE_WARNING);
					// stop method execution and return from it
					return;
				}
				
				//Dialog.showSimpleMessage("Functionality is not suppoerted", Dialog.TITLE_INFORMATION);
				CommentedDialogHandler handler = new CommentedDialogHandler() {
					
					@Override
					public void onOkChoosed(String comments) {
						selectedApprovalStep.setComments(comments);
						
						VacationsManager.getApprovalService().reject(selectedApprovalStep, new CustomAsyncCallback<Void>() {

							@Override
							public void onSuccessExecution(Void result) {
								Dialog.showSimpleMessage("Vacation request have been successfully rejected", 
										Dialog.TITLE_INFORMATION);
								updateApprovalStepsList();
								clearOldData();
							}
						});
					}

					@Override
					public void onCancelChoosed() {
						// Do nothing
					}
				};
				
				Dialog.showCommentsEntryDialog("Please, entry comments with reason of rejection:",
						"Reject approval step", 
						handler);
			}
		});
		buttonsPanel.add(rejectButton);
		
		return buttonsPanel;
	}

	/**
	 * This method clears all data in the form.
	 */
	private void clearOldData() {
		selectedApprovalStep = null;
	}

}
