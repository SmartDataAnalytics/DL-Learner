/**
 * Copyright (C) 2007-2009, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.tools.protege;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This class handles the all actions in the option panel.
 * @author Christian Koetteritzsch
 *
 */
public class OptionPanelHandler implements ItemListener {

	private static final String OWL_RADIO_STRING = "OWL 2";
	private static final String EL_RADIO_STRING = "EL Profile";
	private static final String VALUE_STRING = "<=x, >=x with max.:";

	private OptionPanel option;

	/**
	 * This is the Constructor of the class.
	 * @param o Option Panel
	 */
	public OptionPanelHandler(OptionPanel o) {
		this.option = o;

	}

	@Override
	/**
	 * This method handles the actions to be taken if a 
	 * radio button is selected/deselected.
	 */
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
