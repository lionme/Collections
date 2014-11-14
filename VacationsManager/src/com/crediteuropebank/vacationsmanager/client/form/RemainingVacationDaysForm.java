package com.crediteuropebank.vacationsmanager.client.form;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.observer.GeneralObserver;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.TextField;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.VacationDays;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * This form uses just for displaying remaining vacation days for logged in user.
 * 
 * @author dimas
 *
 */
public class RemainingVacationDaysForm extends Composite implements GeneralObserver{
	
	/**
	 * Text field width.
	 */
	private static final int TEXT_FIELD_WIDTH = 300;
	
	/**
	 * Text field label width.
	 */
	private static final int TEXT_FIELD_LABEL_WIDTH = 200;
	
	/**
	 * Main panel spacing
	 */
	private static final int MAIN_PANEL_SPACING = 5;
	
	private final TextField twoWeeksVacationsTF = new TextField("Two weeks vacations.");
	
	private final TextField oneWeekVacationsTF = new TextField("One week vacations.");
	
	private final TextField dayVacationsTF = new TextField("Day vacations.");
	
	public RemainingVacationDaysForm() {
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setSpacing(MAIN_PANEL_SPACING);
		
		twoWeeksVacationsTF.setEnabled(false);
		twoWeeksVacationsTF.setWidth(TEXT_FIELD_WIDTH);
		twoWeeksVacationsTF.setLabelWidth(TEXT_FIELD_LABEL_WIDTH);
		mainPanel.add(twoWeeksVacationsTF);
		
		oneWeekVacationsTF.setEnabled(false);
		oneWeekVacationsTF.setWidth(TEXT_FIELD_WIDTH);
		oneWeekVacationsTF.setLabelWidth(TEXT_FIELD_LABEL_WIDTH);
		mainPanel.add(oneWeekVacationsTF);
		
		dayVacationsTF.setEnabled(false);
		dayVacationsTF.setWidth(TEXT_FIELD_WIDTH);
		dayVacationsTF.setLabelWidth(TEXT_FIELD_LABEL_WIDTH);
		mainPanel.add(dayVacationsTF);
		
	    DisclosurePanel disclosurePanel = new DisclosurePanel("Your remaining vacation days...");
	    disclosurePanel.setAnimationEnabled(true);
	    disclosurePanel.setContent(mainPanel);
	    
	    initWidget(disclosurePanel);
	    
	    VacationsManager.getVacationGeneralObservable().attach(this);
	}
	
	/**
	 * Updates fields that display user's remaining vacation days info.
	 * 
	 * @param vacationDays
	 */
	public void setRemainingVacationDays(final VacationDays vacationDays) {
		twoWeeksVacationsTF.setFieldValue(String.valueOf(vacationDays.getTwoWeeksVacations()));
		oneWeekVacationsTF.setFieldValue(String.valueOf(vacationDays.getOneWeekVacations()));
		dayVacationsTF.setFieldValue(String.valueOf(vacationDays.getDayVacations()));
	}

	@Override
	public void update() {
		User loggedInUser = VacationsManager.getLoggedInUser();
		
		if (loggedInUser == null) {
			// Add client side log
			
			Dialog.showSimpleMessage("Problem with loading logged in user info occurs. Please, refresh browser and inform administrator!", 
					Dialog.TITLE_ERROR);
		}
		
		VacationsManager.getUsersService().getRemainingVacationDaysForUser(loggedInUser.getId(), 
				new CustomAsyncCallback<RemainingVacationDays>() {

			@Override
			public void onSuccessExecution(RemainingVacationDays result) {
				setRemainingVacationDays(result);
			}
		});
	}
}
