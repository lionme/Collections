package com.crediteuropebank.vacationsmanager.client.widget;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>This component extends functionality of standard DialogBox, defined in GWT.
 * It has already contains main panel (VerticalPanel) and 2 panels, placed on
 * it:</p>
 * <ol>
 * 		<li>user's elements panel (Vertical Panel) where you can place your
 * widgets;</li>
 * 		<li>buttons panel (HorizontalPanel), placed on main panel;</li>
 * </ol>
 * <p>If you specify position of the widgets in this component by calling
 * setHorizontalAlignment - you specified only the position of added widgets,
 * not buttons on the buttons panel. To specified it's position you should use
 * function setButtonsAlignment.</p>
 * 
 */

public class ExtendedDialogBox {
	
	/**
	 * Constant spacing for main panel.
	 */
	private static final int MAIN_PANEL_SPACING = 5;
	
	/**
	 * Default spacing for content panel.
	 */
	private static final int DEFAULT_CONTENT_PANEL_SPACING = 5;
	
	/**
	 * Default spacing for buttons panel.
	 */
	private static final int DEFAULT_BUTTONS_SPACING = 10;
	
	/**
	 * Default width of the main panel of the dialog box.
	 */
	private static final int DEFAULT_WINDOW_WIDTH = 300;
	
	/**
	 * Default title text (necessary because of problem with DialogBox displaying 
	 * if we will not set text property for it).
	 */
	private static final String DEFAULT_TITLE_TEXT = " ";
	
	private static final HorizontalAlignmentConstant DEFAULT_CONTENT_ALIGNMENT = HasHorizontalAlignment.ALIGN_CENTER;
	
	private static final HorizontalAlignmentConstant DEFAULT_BUTTONS_ALIGNMENT = HasHorizontalAlignment.ALIGN_CENTER;
	
	/**
	 * Buttons height.
	 */
	private int buttonsHeight = 30;
	
	/**
	 * Buttons width.
	 */
	private int buttonsWidth = 80;
	
	/**
	 * Main panel of the dialog box where all other elements are placed. 
	 */
	private final VerticalPanel mainPanel = new VerticalPanel();
	
	/**
	 * Panel that holds all custom elements inside window.
	 */
	private final VerticalPanel contentPanel = new VerticalPanel();
	
	/**
	 * Panel that is outer for buttons panel.
	 */
	private final VerticalPanel buttonsOuterPanel = new VerticalPanel();
	
	/**
	 * Panel where placed buttons in the bottom of the window.
	 */
	private final HorizontalPanel buttonsPanel = new HorizontalPanel();
	
	/**
	 * This list holds all custom elements, placed in the window.
	 */
	private List<Widget> userWidgetsList = new LinkedList<Widget>();
	
	/**
	 * This list holds all bottom buttons.
	 */
	private List<Button> buttonsList = new LinkedList<Button>();
	
	/**
	 * Holds the element to which focus should be returned after closing dialog.
	 */
	private FocusWidget whomReturnFocus;
	
	/**
	 * The dialog box instance
	 */
	private final DialogBox dialogBox = new DialogBox();

	public String getTitleText() {
		return dialogBox.getText();
	}

	/**
	 * Sets the text of the title.
	 * 
	 * @param titleText
	 */
	public void setTitleText(String titleText) {
		dialogBox.setText(titleText);
	}

	/**
	 * Sets width in pixels
	 * 
	 * @param width
	 */
	public void setWidth(int width) {
		mainPanel.setWidth(width + "px");
		
		contentPanel.setWidth((width - 10) + "px");
	}

	/**
	 * Sets bottom buttons height (in pixels).
	 * 
	 * @param buttonsHeight
	 */
	public void setButtonsHeight(int buttonsHeight) {
		this.buttonsHeight = buttonsHeight;
		
		for (Button button: buttonsList) {
			button.setHeight(buttonsHeight + "px");
		}
	}

	/**
	 * Sets bottom buttons width (in pixels).
	 * 
	 * @param buttonsWidth
	 */
	public void setButtonsWidth(int buttonsWidth) {
		this.buttonsWidth = buttonsWidth;
		
		for (Button button: buttonsList) {
			button.setWidth(buttonsWidth + "px");
		}
	}

	/**
	 * Sets the spacing between the buttons.
	 */
	public void setButtonsSpacing(int buttonsSpacing) {
		buttonsPanel.setSpacing(buttonsSpacing);
	}

	/**
	 * Sets horizontal alignment of the content.
	 * 
	 * @param horizontalAlignment
	 */
	public void setHorizontalAlignment(HorizontalAlignmentConstant horizontalAlignment) {
		contentPanel.setHorizontalAlignment(horizontalAlignment);
	}

	/**
	 * Sets bottom buttons alignment (horizontal).
	 * 
	 * @param buttonsAlignment
	 */
	public void setButtonsAlignment(HorizontalAlignmentConstant buttonsAlignment) {
		buttonsOuterPanel.setHorizontalAlignment(buttonsAlignment);
		
		mainPanel.setCellHorizontalAlignment(buttonsPanel,
				buttonsAlignment);
	}

	/**
	 * This method return widget, whom focus will be returned after dialogBox
	 * have been closed.
	 * 
	 * @return whomReturnFocus
	 */
	public FocusWidget getWhomReturnFocus() {
		return whomReturnFocus;
	}

	/**
	 * This method sets widget, whom focus will be returned after dialogBox have
	 * been closed.
	 * 
	 * @param whomReturnFocus
	 */
	public void setWhomReturnFocus(FocusWidget whomReturnFocus) {
		this.whomReturnFocus = whomReturnFocus;
	}

	/**
	 * Sets the spacing between content elements. Default = 5.
	 * 
	 * @param contentPanelSpacing
	 */
	public void setContentPanelSpacing(int contentPanelSpacing) {
		contentPanel.setSpacing(contentPanelSpacing);
	}

	/**
	 * This is constructor without arguments in which set's default parameters
	 * of the component
	 */
	public ExtendedDialogBox() {
		super();
		
		// Set constant main panel spacing.
		mainPanel.setSpacing(MAIN_PANEL_SPACING);
		mainPanel.setWidth(DEFAULT_WINDOW_WIDTH + "px");
		mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		contentPanel.setSpacing(DEFAULT_CONTENT_PANEL_SPACING);
		contentPanel.setHorizontalAlignment(DEFAULT_CONTENT_ALIGNMENT);
		contentPanel.setWidth( (DEFAULT_WINDOW_WIDTH - 10) + "px");
		
		DecoratorPanel contentDecorator = new DecoratorPanel();
		contentDecorator.setWidget(contentPanel);
		
		mainPanel.add(contentDecorator);
		//mainPanel.setCellWidth(contentPanel, "100%");
		
		buttonsPanel.setSpacing(DEFAULT_BUTTONS_SPACING);	
		buttonsOuterPanel.add(buttonsPanel);
		buttonsOuterPanel.setHorizontalAlignment(DEFAULT_BUTTONS_ALIGNMENT);
		
		mainPanel.setCellHorizontalAlignment(buttonsPanel,
				DEFAULT_BUTTONS_ALIGNMENT);
		
		mainPanel.add(buttonsOuterPanel);
		mainPanel.setCellWidth(buttonsOuterPanel, "100%");
		
		dialogBox.setText(DEFAULT_TITLE_TEXT);
		
		dialogBox.add(mainPanel);
	}

	/**
	 * This method adds new widget to main panel of ExtendedDialogBox
	 */
	public void add(Widget widget) {
		contentPanel.add(widget);
		
		userWidgetsList.add(widget);
	}

	/**
	 * This method add buttons to the bottom of the component.
	 * 
	 * @param button
	 */
	public void addButton(Button button) {
		button.setWidth(buttonsWidth + "px");
		button.setHeight(buttonsHeight + "px");
		
		buttonsPanel.add(button);
		
		buttonsList.add(button);
	}
	
	/**
	 * Hides the popup and detaches it from the page. 
	 * 
	 * @param autoClosed
	 */
	public void hide(boolean autoClosed) {
			
		dialogBox.hide(autoClosed);
		if (whomReturnFocus!=null)
			whomReturnFocus.setFocus(true);
	}

	/**
	 * Hides the popup and detaches it from the page. 
	 */
	public void hide() {
		
		dialogBox.hide();
		if (whomReturnFocus!=null)
			whomReturnFocus.setFocus(true);
	}
	
	/**
	 * Centers the window in the browser window and shows it. 
	 */
	public void center() {
		dialogBox.center();
	}
}
