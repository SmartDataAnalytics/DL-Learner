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

import org.dllearner.core.EvaluatedDescription;



/**
 * This class shows more details of the suggested concepts. It shows the positive and negative examples
 * that are covered and that are not covered by the suggested concepts. It also shows the accuracy of the 
 * selected concept.
 * @author Christian Koetteritzsch
 *
 */

public class MoreDetailForSuggestedConceptsPanel extends JPanel {

	private static final long serialVersionUID = 785272797932584581L;
	
	 // Model of the dllearner
	 
	private final DLLearnerModel model;
	private EvaluatedDescription eval;
	private static final int HEIGHT = 230;
	private static final int WIDTH = 540;
	private GraphicalCoveragePanel graphicalPanel;
	private GraphicalCoverageTextField graphicalText;

	/**
	 * This is the constructor for the Panel.
	 * @param model DLLearnerModel
	 */
	public MoreDetailForSuggestedConceptsPanel(DLLearnerModel model) {
		super();
		setLayout(new GridLayout(1, 2));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.model = model;
	}

	/**
	 * This method renders the output for the detail panel.
	 * @param desc selected description
	 */
	public void renderDetailPanel(EvaluatedDescription desc) {
		eval = desc;

		//panel for the informations of the selected concept
		//this method adds the informations for the selected concept to the panel
		graphicalPanel = new GraphicalCoveragePanel(eval, model);
		graphicalText = new GraphicalCoverageTextField(eval, model);
		graphicalPanel.setBounds(5, 0, 300, 350);
		//adds all information to the example panel
		unsetEverything();
		this.add(graphicalPanel, "Center");
		this.add(graphicalText.getTextScroll(), "East");
	}

	private void unsetEverything() {
		removeAll();
	}


	/**
	 * Returns the graphical coverage panel.
	 * @return graphical coverage panel
	 */
	public GraphicalCoveragePanel getGraphicalCoveragePanel() {
		return graphicalPanel;
	}
	
	/**
	 * Unsets the panel after plugin is closed.
	 */
	public void unsetPanel() {
		unsetEverything();
		if(graphicalPanel != null) {
			graphicalPanel.unsetPanel();
		}
	}
}
