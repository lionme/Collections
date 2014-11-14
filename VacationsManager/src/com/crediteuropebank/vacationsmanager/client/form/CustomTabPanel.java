package com.crediteuropebank.vacationsmanager.client.form;

import java.util.HashMap;
import java.util.Map;

import com.crediteuropebank.vacationsmanager.client.form.tab.CustomTab;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * This class represents custom tab panel.
 * 
 * @author dimas
 *
 */
public class CustomTabPanel extends Composite {
	
	/**
	 * This class represents tab header.
	 * 
	 * @author DIMAS
	 *
	 */
	private static class TabHeader extends Composite {
		
		/**
		 * The height of the tab panel header.
		 */
		private static final int TAB_PANEL_HEADER_HEIGHT = 16;
		
		/**
		 * The image that should be displayed as update button icon.
		 */
		private static final String imagePath="images/update.png";
		
		/**
		 * Header panel that is used to keep all widgets in the header of each tab.
		 */
		private final HorizontalPanel headerPanel = new HorizontalPanel();
		
		public TabHeader(final CustomTab tab, String headerText) {
			
			headerPanel.setSpacing(2);
			headerPanel.setHeight(String.valueOf(TAB_PANEL_HEADER_HEIGHT) + "px");
			
			final Label label = new Label(headerText); 
			label.setHeight(String.valueOf(TAB_PANEL_HEADER_HEIGHT) + "px");
			headerPanel.add(label);
			
			Image image = new Image(imagePath);
			image.setWidth("16px");
			image.setHeight("16px");
			image.setPixelSize(16, 16);
			image.getElement().getStyle().setPadding(0D, Unit.PX);
			image.getElement().getStyle().setMargin(0D, Unit.PX);
			
			final PushButton refreshButton = new PushButton(new Image(imagePath));
			
			refreshButton.getElement().getStyle().setWidth(16, Unit.PX);
			refreshButton.getElement().getStyle().setHeight(16, Unit.PX);
			refreshButton.getElement().getStyle().setPadding(0D, Unit.PX);
			refreshButton.getElement().getStyle().setMargin(0D, Unit.PX);
			refreshButton.getElement().getStyle().setOpacity(0.8);
			
			refreshButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					tab.updateContent();
				}
			});
			
			headerPanel.add(refreshButton);
			headerPanel.setCellHorizontalAlignment(refreshButton, HasHorizontalAlignment.ALIGN_RIGHT);
			headerPanel.setCellVerticalAlignment(refreshButton, HasVerticalAlignment.ALIGN_MIDDLE);
			
			initWidget(headerPanel);
		}
		
		public TabHeader(final CustomTab tab, String headerText, int headerWidth) {
			this(tab, headerText);
			
			setHeaderWidth(headerWidth);
		}
		
		public void setHeaderWidth(int width) {
			headerPanel.setWidth(String.valueOf(width) + "px");
		}
	}

	/**
	 * Standard tab panel.
	 */
	private final TabLayoutPanel mainPanel = new TabLayoutPanel(2.5, Unit.EM);
	
	/**
	 * The map that contains the list of the tabs, attached to this tab panel.
	 */
	private Map<Integer, CustomTab> tabs = new HashMap<Integer, CustomTab>();
	
	/**
	 * Holds selected tab instance.
	 */
	private CustomTab selectedTab;
	
	private final int tabHeaderWidth;
	
	/**
	 * Default constructor.
	 */
	public CustomTabPanel(int tabHeaderWidth) {
		this.tabHeaderWidth = tabHeaderWidth;
		
		// Set a properties of main tab Panel.
	    mainPanel.setAnimationDuration(2000);
	    mainPanel.getElement().getStyle().setMarginBottom(10.0, Unit.PX);
	    
	    /* 
	     * In TabLayoutPanel tabs count is started from 0.
	     * 
	     * We redraw region if related tab is selected.
	     */
		mainPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
		
				updateTab(event.getSelectedItem());
			}
		});
	    
	    initWidget(mainPanel);
	}
	
	/**
	 * Adds new {@link CustomTab} to our {@link CustomTabPanel}. This method centers the 
	 * content of the tab.
	 * 
	 * @param tabId - unique id of the tab.
	 * @param tab - the {@link CustomTab} implementation.
	 * @param tabTitle - the title of the tab.
	 */
	public void addTab(int tabId, CustomTab tab, String tabTitle) {
		if (tab == null) {
			throw new IllegalArgumentException("Input tab object cannot be null!");
		} else if (tabs.containsKey(tabId)) {
			throw new IllegalArgumentException("Tab with specified id have already been added!");
		}
		
		/* Center content of the tab */
		Panel wrappedTab = wrapInCenteringPanel(tab);
		
		TabHeader tabHeader = new TabHeader(tab, tabTitle, tabHeaderWidth);
		
		mainPanel.add(wrappedTab, tabHeader);
		
		tabs.put(tabId, tab);
	}
	
	/**
	 * Adds new {@link CustomTab} to our {@link CustomTabPanel}. This overloaded version of
	 * addTab(..) method gives also a possibility to set vertical and horizontal alignment of the 
	 * tab's content.
	 * 
	 * @param tabId - unique id of the tab.
	 * @param tab - the {@link CustomTab} implementation.
	 * @param tabTitle - the title of the tab.
	 * @param horizontalAlignment - horizontal alignment of the tab's content. Should be specified
	 * 					using {@link HasHorizontalAlignment} interface.
	 * @param verticalAlignment - vertical alignment of the tab's content. Should be specified
	 * 					using {@link HasVerticalAlignment} interface.
	 */
	public void addTab(int tabId, CustomTab tab, String tabTitle, 
			HorizontalAlignmentConstant horizontalAlignment, VerticalAlignmentConstant verticalAlignment) {
		if (tab == null) {
			throw new IllegalArgumentException("Input tab object cannot be null!");
		} else if (tabs.containsKey(tabId)) {
			throw new IllegalArgumentException("Tab with specified id have already been added!");
		}
		
		/* Center content of the tab */
		Panel wrappedTab = wrapInOuterPanel(tab, horizontalAlignment, verticalAlignment);
		
		TabHeader tabHeader = new TabHeader(tab, tabTitle, tabHeaderWidth);
		
		mainPanel.add(wrappedTab, tabHeader);
		
		tabs.put(tabId, tab);
	}
	
	public void setWidth(String width) {
		mainPanel.setWidth(width);
	}
	
	public void setHeight(String height) {
		mainPanel.setHeight(height);
	}
	
	/**
	 * This function updates content of the tab with specified tab id.
	 * 
	 * @param tabId - the id of the tab.
	 */
	public void updateTab(int tabId) {
		selectedTab = tabs.get(tabId);
		
		if (selectedTab != null) {
			selectedTab.updateContent();
		}
	}
	
	/**
	 * This methods wraps input widget in Horizontal panel, that center all content.
	 * 
	 * @param panel - widget to be centered.
	 * @return the HorizontalPanel with centered content.
	 */
	private Panel wrapInCenteringPanel(IsWidget widget) {
	    // Create centering panel. It should occupy the entire space of the surrounding panel.
	    HorizontalPanel centeringPanel = new HorizontalPanel();
	    centeringPanel.setWidth("100%");
	    centeringPanel.setHeight("100%");
	    
	    // Add panel that need to be centered into the centering panel
	    centeringPanel.add(widget);
	    centeringPanel.setCellHorizontalAlignment(widget, HasHorizontalAlignment.ALIGN_CENTER);
	    centeringPanel.setCellVerticalAlignment(widget, HasVerticalAlignment.ALIGN_MIDDLE);

	    return centeringPanel;
	}
	
	/**
	 * This method wraps widget in outer panel and set's its alignment inside this wrapper panel.
	 * 
	 * @param widget
	 * @param horizontalAlignment
	 * @param verticalAlignment
	 * @return wrapper panel
	 */
	private Panel wrapInOuterPanel(IsWidget widget, HorizontalAlignmentConstant horizontalAlignment,
			VerticalAlignmentConstant verticalAlignment) {
		
		// Create centering panel. It should occupy the entire space of the surrounding panel.
	    HorizontalPanel centeringPanel = new HorizontalPanel();
	    centeringPanel.setWidth("100%");
	    centeringPanel.setHeight("100%");
	    
	    // Add panel that need to be centered into the centering panel
	    centeringPanel.add(widget);
	    centeringPanel.setCellHorizontalAlignment(widget, horizontalAlignment);
	    centeringPanel.setCellVerticalAlignment(widget, verticalAlignment);

	    return centeringPanel;
	}
}
