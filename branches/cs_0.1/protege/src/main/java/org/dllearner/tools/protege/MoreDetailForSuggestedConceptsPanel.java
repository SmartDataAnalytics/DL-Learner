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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.dllearner.core.EvaluatedDescription;
import org.protege.editor.owl.OWLEditorKit;



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
	 
	private EvaluatedDescription eval;
	private static final int HEIGHT = 300;
	private static final int WIDTH = 540;
	private GraphicalCoveragePanel graphicalPanel;
	private GraphicalCoverageTextField graphicalText;

	/**
	 * This is the constructor for the Panel.
	 * @param model DLLearnerModel
	 */
	public MoreDetailForSuggestedConceptsPanel(OWLEditorKit editorKit) {
		super();
		setLayout(new GridLayout(1, 2));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		graphicalPanel = new GraphicalCoveragePanel(eval);
		graphicalText = new GraphicalCoverageTextField(eval, editorKit);
		graphicalPanel.setBounds(5, 0, 300, 370);
		add(graphicalPanel, BorderLayout.CENTER);
		add(graphicalText.getTextScroll(), BorderLayout.EAST);
	}


	private void unsetEverything() {
		removeAll();
	}
	
	public void setDescription(EvaluatedDescription desc){
		graphicalText.setDescription(desc);
		graphicalPanel.setDescription(desc);
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
