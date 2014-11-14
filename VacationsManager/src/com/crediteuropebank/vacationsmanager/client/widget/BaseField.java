package com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * This class is base for all custom Field widgets that represent combination of {@link Label} + some type of input 
 * widget (TextBox, ListBox, etc.)
 * 
 * @author DIMAS
 *
 * @param <T> - the type of the value that will be hold by the custom input field.
 */
public abstract class BaseField<T> extends Composite implements CustomField {
	
	/**
	 * An additional text that is always adding to label's text.
	 */
	private final static String ADDITIONAL_LABEL_TEXT = " :";
	
	/**
	 * Minimum width of the TextBox.
	 */
	private final static int MIN_TEXTBOX_WIDTH = 10;
	
	/**
	 * Shows whether inner elements have been already added to main panel.
	 * It is used to exclude possibility of adding elements twice.
	 */
	private boolean hasComponentAlreadyBeenInitialized = false;
	
	/**
	 * Main HorizontalPanel where label and input text widget are placed.
	 */
	private final HorizontalPanel mainPanel = new HorizontalPanel();
	
	private final Label label = new Label();
	private Widget textBox;
	private int width;
	private int labelWidth;
	private int spacing;
	
	/**
	 *  Set the width of the whole element (label + custom input field)
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 *  Sets the width of the label.
	 */
	public void setLabelWidth(int labelWidth) {
		this.labelWidth = labelWidth;
	}

	/**
	 * Sets spacing for this component
	 */
	public void setSpacing(int spacing) {
		this.spacing = spacing;
		this.mainPanel.setSpacing(spacing);
	}

	/**
	 * Sets the text of the label.
	 */
	public void setLabelText(String labelText) {
		this.label.setText(labelText + ADDITIONAL_LABEL_TEXT);
	}
	
	/**
	 * This function should be overridden in the subclasses.It should return widget that will be used for input information.
	 * @return the widget that will be used for entering data in the subclasses (TextBox, ListBox etc.).
	 */
	abstract Widget getCustomWidget();

	/**
	 * Default constructor.
	 */	
	public BaseField() {
		mainPanel.add(label);
		
		initWidget(mainPanel);
	}

	/**
	 * Creates a text field with default settings and specified labels text.
	 * @param labelText - label's text
	 */	
	public BaseField(String labelText) {
		this();
		this.label.setText(labelText + ADDITIONAL_LABEL_TEXT);
	}
	
	/**
	 * This method will be called when component have been attached to the HTML page.
	 * 
	 * This is very ugly. Change later. I have just took it from old project.
	 */	
	@Override
	protected void onAttach() {
				
		if (!hasComponentAlreadyBeenInitialized) {
			recalculateComponentSize();
			hasComponentAlreadyBeenInitialized = true;
		}
		
		super.onAttach();
	}

	/**
	 * This function makes recalculation of sizes of panel, label and custom input widget.
	 * 
	 */
	private void recalculateComponentSize() {
		
		this.textBox = getCustomWidget();

		if (textBox == null) {
			throw new IllegalStateException("Internal custom widget for entering information can not be null. May be you forget to set it?");
		}
		
		mainPanel.add(textBox);
		
		int textBoxWidthInt = width - labelWidth - spacing;
		if (textBoxWidthInt<MIN_TEXTBOX_WIDTH) {
			textBoxWidthInt = MIN_TEXTBOX_WIDTH;
			labelWidth = width - MIN_TEXTBOX_WIDTH - spacing;
		}
		
		String textBoxWidth = String.valueOf(textBoxWidthInt) + "px";
		
		mainPanel.setWidth(width + "px");

		label.setWidth(labelWidth + "px");
		
		textBox.setWidth(textBoxWidth);
		
		mainPanel.setCellWidth(label, labelWidth + "px");
		mainPanel.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
		mainPanel.setCellWidth(textBox, textBoxWidth);
	}
	
	/**
	 * This method sets enabled/disabled this field
	 */
	public abstract void setEnabled(boolean value);
}
