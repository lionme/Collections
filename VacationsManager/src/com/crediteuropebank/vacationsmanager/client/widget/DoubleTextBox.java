package com.crediteuropebank.vacationsmanager.client.widget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/**
 * This class extends {@link TextBox} and handles some of the browser events. It limits symbols that 
 * can be entered to only '0'..'9' and '.'. In such way it gives user a possibility to enter
 * only double numbers. Also max number of digits that can be entered is limited to 8.
 * 
 * @author dimas
 *
 */
public class DoubleTextBox extends TextBox {
	private static final String ERROR_TEXT = "In that field you can put only double value!";
	
	/**
	 * Max number of digits that can be entered in this field.
	 */
	private static final int MAX_NYMBER_OF_DIGITS = 8;
	
	@Override
	public void onBrowserEvent(Event event) {
		try {
			switch (DOM.eventGetType(event)) {
			case Event.ONKEYPRESS: {// OnKeyPress event ONKEYPRESS {
				int charCode = Event.getCurrentEvent().getCharCode();
				// Char codes: "." - 46; backspace - 0; "0..9" - 48..57;
				if (charCode == 46) {
					String value = this.getValue() + ".";
					Double.parseDouble(value);
				} else if ( ((charCode < 48) || (charCode > 57)) && (charCode != 0) && (charCode != 46)) {
					throw new NumberFormatException();
				}
				
				if ((this.getValue().length()+1)>MAX_NYMBER_OF_DIGITS) {
					throw new IndexOutOfBoundsException();
				}

				break;
			}
			case Event.ONBLUR: {
				if (this.getValue().trim().equals("")) {
					this.setValue("0");
				}
				
				// Throws exception if wrong value was entered.
				Double.parseDouble(this.getValue());

				break;
			}
			}
			

		} catch (NumberFormatException e) {
			Dialog.showReturnedFocusSimpleMessage(ERROR_TEXT, Dialog.TITLE_WARNING, this);
			DOM.eventPreventDefault(event);
		} catch (IndexOutOfBoundsException e) {
			DOM.eventPreventDefault(event);
		}
		
		super.onBrowserEvent(event);
	}

	public DoubleTextBox() {
		// Specify which events should be handled in obBrowserEvent method. Without this will not work!
		this.sinkEvents(Event.ONKEYPRESS);
		this.sinkEvents(Event.ONBLUR);
		this.sinkEvents(Event.ONKEYUP);	
	}

}
