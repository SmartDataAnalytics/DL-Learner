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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles the all actions in the option panel.
 * @author Christian Koetteritzsch
 *
 */
public class OptionPanelHandler implements ActionListener {

	private static final String OPTION_PROFILE_OWL = "OWL 2";
	private static final String OPTION_PROFILE_EL = "EL Profile";
	private static final String OPTION_PROFILE_DEFAULT = "Default";
	private static final String OPTION_CARDINALITY = "Cardinality";

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
	public void actionPerformed(ActionEvent e) {
		if (e.toString().contains(OPTION_PROFILE_OWL)) {
				this.setToOWLProfile();
		} 
		if (e.toString().contains(OPTION_PROFILE_EL)) {
				this.setToELProfile();
		}
		if (e.toString().contains(OPTION_PROFILE_DEFAULT)) {
				this.setToDefaultProfile();
		}
		if(e.getActionCommand().equals(OPTION_CARDINALITY)) {
			option.setCountMoreBoxEnabled(option.isUseCardinalityRestrictions());
		}
	}

	private void setToOWLProfile() {
		option.setToOWLProfile();

	}

	private void setToELProfile() {
		option.setToELProfile();

	}
	
	private void setToDefaultProfile() {
		option.setToDefaultProfile();
	}

	

}
