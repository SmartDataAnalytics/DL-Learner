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

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

/**
 * This class is the Panel for the Check boxes where the positive and negative
 * examples are chosen.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class PosAndNegSelectPanel extends JPanel {

	private static final long serialVersionUID = 23632947283479L;

	private final OptionPanel optionPanel;

	/**
	 * This is the constructor for the Panel that shows the check boxes.
	 * 
	 * @param model
	 *            DLLearnerModel
	 * @param act
	 *            ActionHandler
	 */
	public PosAndNegSelectPanel(DLLearnerModel model, ActionHandler act) {
		//set layout for parent Panel
		super();
		setLayout(new GridLayout(0, 1));
		setPreferredSize(new Dimension(490, 100));
		optionPanel = new OptionPanel();
		add(optionPanel);
	}
	
	/**
	 * This method returns the option panel.
	 * @return OptionPanel 
	 */
	public OptionPanel getOptionPanel() {
		return optionPanel;
	}

}
