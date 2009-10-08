package org.dllearner.tools.protege;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class OptionPanelHandler implements ItemListener {

	private static final String OWL_RADIO_STRING = "OWL 2";
	private static final String EL_RADIO_STRING = "EL Profile";
	private static final String VALUE_STRING = "<=x, >=x with max.:";

	private OptionPanel option;

	public OptionPanelHandler(OptionPanel o) {
		this.option = o;

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.toString().contains(OWL_RADIO_STRING)) {
			if (option.getOwlRadioButton().isSelected()) {
				this.setToOWLProfile();
			} else {
				this.setToELProfile();
			}

		} else if (e.toString().contains(EL_RADIO_STRING)) {
			if (option.getElProfileButton().isSelected()) {
				this.setToELProfile();
			} else {
				this.setToOWLProfile();
			}

		} else if(e.toString().contains(VALUE_STRING)) {
			if(option.getMoreBox()) {
				option.setCountMoreBoxEnabled(true);
			} else {
				option.setCountMoreBoxEnabled(false);
			}
		}
	}

	private void setToOWLProfile() {
		option.getOwlRadioButton().setSelected(true);
		option.getElProfileButton().setSelected(false);
		option.setToOWLProfile();

	}

	private void setToELProfile() {
		option.getOwlRadioButton().setSelected(false);
		option.getElProfileButton().setSelected(true);
		option.setToELProfile();

	}

}
