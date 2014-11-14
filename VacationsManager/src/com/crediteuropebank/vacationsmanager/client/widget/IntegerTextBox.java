package  com.crediteuropebank.vacationsmanager.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/**
 * This class extends standart {@link TextBox}. In this component user can enter only digits. 
 * Validation is made when user click on the key from keyboard. Also in this text box you
 * can enter only 8 digits.
 * 
 * @author dimas
 *
 */
public final class IntegerTextBox extends TextBox {
	private static final String ERROR_TEXT = "In that field you can put only digits!";
	
	/**
	 * Max number of digits that can be entered in this field.
	 */
	private static final int MAX_NYMBER_OF_DIGITS = 8;

	@Override
	public void onBrowserEvent(Event event) {
		try {
			switch (DOM.eventGetType(event)) {
			case Event.ONKEYPRESS: {
				int charCode = Event.getCurrentEvent().getCharCode();
				if ( (charCode<48) || (charCode>57) )
					throw new NumberFormatException();

				if (this.getValue().length()>MAX_NYMBER_OF_DIGITS) {
					throw new IndexOutOfBoundsException();
				}

				break;
			}
			case Event.ONBLUR: {
				if (this.getValue().trim().equals("")) {
					this.setValue("0");
				}

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
	
	public IntegerTextBox() {
		// Specify which events should be handled in obBrowserEvent method. Without this will not work!
		this.sinkEvents(Event.ONKEYPRESS);
		this.sinkEvents(Event.ONBLUR);
		this.sinkEvents(Event.ONKEYUP);
	}


}
