package org.dllearner.tools.protege;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class OptionPanelHandler implements ItemListener {

	private static final String OWL_RADIO_STRING = "OWL 2";
	private static final String EL_RADIO_STRING = "EL Profile";

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

		}
	}

	private void setToOWLProfile() {
		option.getOwlRadioButton().setSelected(true);
		option.getElProfileButton().setSelected(false);
		option.getAllBox().setSelected(true);
		option.getSomeBox().setSelected(true);
		option.getNotBox().setSelected(true);
		option.getValueBox().setSelected(true);
		option.getLessBox().setSelected(true);
		option.getCountLessBox().setEnabled(true);
		option.getMoreBox().setSelected(true);
		option.getCountMoreBox().setEnabled(true);
	}

	private void setToELProfile() {
		option.getOwlRadioButton().setSelected(false);
		option.getElProfileButton().setSelected(true);
		option.getAllBox().setSelected(false);
		option.getSomeBox().setSelected(true);
		option.getNotBox().setSelected(false);
		option.getValueBox().setSelected(false);
		option.getLessBox().setSelected(false);
		option.getCountLessBox().setEnabled(false);
		option.getMoreBox().setSelected(false);
		option.getCountMoreBox().setEnabled(false);
	}

}
