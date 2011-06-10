package org.dllearner.autosparql.client.widget.autocomplete;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AutoCompleteTextBox extends TextBox implements KeyboardListener, ChangeListener {

	protected PopupPanel choicesPopup = new PopupPanel(true);
	protected ListBox choices = new ListBox();
	protected CompletionItems items = new SimpleAutoCompletionItems(new String[] {"Soccer", "Test", "Book", "Books"});
	protected boolean popupAdded = false;
	protected boolean visible = false;

	/**
	 * Default Constructor
	 * 
	 */
	public AutoCompleteTextBox() {
		super();
		this.addKeyboardListener(this);
		choices.addChangeListener(this);
		this.setStyleName("AutoCompleteTextBox");

		choicesPopup.add(choices);
		choicesPopup.addStyleName("AutoCompleteChoices");

		choices.setStyleName("list");
	}

	/**
	 * Sets an "algorithm" returning completion items You can define your own
	 * way how the textbox retrieves autocompletion items by implementing the
	 * CompletionItems interface and setting the according object
	 * 
	 * @see SimpleAutoCompletionItem
	 * @param items
	 *            CompletionItem implementation
	 */
	public void setCompletionItems(CompletionItems items) {
		this.items = items;
	}

	/**
	 * Returns the used CompletionItems object
	 * 
	 * @return CompletionItems implementation
	 */
	public CompletionItems getCompletionItems() {
		return this.items;
	}

	/**
	 * Not used at all
	 */
	public void onKeyDown(Widget arg0, char arg1, int arg2) {
	}

	/**
	 * Not used at all
	 */
	public void onKeyPress(Widget arg0, char arg1, int arg2) {
	}

	/**
	 * A key was released, start autocompletion
	 */
	public void onKeyUp(Widget arg0, char arg1, int arg2) {
		if (arg1 == KEY_DOWN) {
			int selectedIndex = choices.getSelectedIndex();
			selectedIndex++;
			if (selectedIndex > choices.getItemCount()) {
				selectedIndex = 0;
			}
			choices.setSelectedIndex(selectedIndex);

			return;
		}

		if (arg1 == KEY_UP) {
			int selectedIndex = choices.getSelectedIndex();
			selectedIndex--;
			if (selectedIndex < 0) {
				selectedIndex = choices.getItemCount();
			}
			choices.setSelectedIndex(selectedIndex);

			return;
		}

		if (arg1 == KEY_ENTER) {
			if (visible) {
				complete();
			}

			return;
		}

		if (arg1 == KEY_ESCAPE) {
			choices.clear();
			choicesPopup.hide();
			visible = false;

			return;
		}

		String text = this.getText();
		String[] matches = new String[] {};
		if (text.length() > 0) {
			matches = items.getCompletionItems(text);
		}

		if (matches.length > 0) {
			choices.clear();

			for (int i = 0; i < matches.length; i++) {
				choices.addItem((String) matches[i]);
			}

			// if there is only one match and it is what is in the
			// text field anyways there is no need to show autocompletion
			if (matches.length == 1 && matches[0].compareTo(text) == 0) {
				choicesPopup.hide();
			} else {
				choices.setSelectedIndex(0);
				choices.setVisibleItemCount(matches.length + 1);

				if (!popupAdded) {
					RootPanel.get().add(choicesPopup);
					popupAdded = true;
				}
				choicesPopup.show();
				visible = true;
				choicesPopup.setPopupPosition(this.getAbsoluteLeft(), this.getAbsoluteTop() + this.getOffsetHeight());
				// choicesPopup.setWidth(this.getOffsetWidth() + "px");
				choices.setWidth(this.getOffsetWidth() + "px");
			}

		} else {
			visible = false;
			choicesPopup.hide();
		}
	}

	/**
	 * A mouseclick in the list of items
	 */
	public void onChange(Widget arg0) {
		complete();
	}

	public void onClick(Widget arg0) {
		complete();
	}

	// add selected item to textbox
	protected void complete() {
		if (choices.getItemCount() > 0) {
			this.setText(choices.getItemText(choices.getSelectedIndex()));
		}

		choices.clear();
		choicesPopup.hide();
	}
}
