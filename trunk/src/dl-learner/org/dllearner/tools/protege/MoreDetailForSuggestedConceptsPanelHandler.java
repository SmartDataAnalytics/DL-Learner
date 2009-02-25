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
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JOptionPane;

import org.dllearner.algorithms.EvaluatedDescriptionClass;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;

/**
 * This class takes care of all events happening in the MoreDetailForSuggestedConceptsPanelHandler.
 * @author Christian Koetteritzsch
 *
 */
public class MoreDetailForSuggestedConceptsPanelHandler implements ActionListener{

	private static final String OLD_BUTTON_LABEL = "old";
	private static final String NEW_BUTTON_LABEL = "new";
	private final DLLearnerModel model;
	private EvaluatedDescription eval;
	
	/**
	 * This is the constructor of the MoreDetailForSuggestedConceptsPanelHandler.
	 * @param m Model of the DL-Learner
	 */
	public MoreDetailForSuggestedConceptsPanelHandler(DLLearnerModel m) {
		model = m;
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		if(a.getActionCommand().equals(OLD_BUTTON_LABEL)) {
			String toolTip = "This are the Individuals beloning to " + model.getOldConceptOWLAPI() +":\n\n";
			SortedSet<Individual> ind = model.getReasoner().getIndividuals(model.getCurrentConcept());
			for(Individual i : ind) {
				Set<String> ur = model.getOntologyURIString();
				for(String str : ur) {
					if(i.toString().contains(str)) {
						toolTip = toolTip + i.toManchesterSyntaxString(str, null) + "\n";
					}
				}
			}
			JOptionPane.showMessageDialog(null, toolTip);
		}
		
		if(a.getActionCommand().equals(NEW_BUTTON_LABEL)) {
			Set<Individual> posInd = ((EvaluatedDescriptionClass) eval).getCoveredInstances();
			posInd.addAll(((EvaluatedDescriptionClass) eval).getAdditionalInstances());
			Set<String> uri = model.getOntologyURIString();
			String toolTip = "This are the Individuals beloning to\n ";
			for(String u : uri) {
				if(eval.getDescription().toString().contains(u)) {
					toolTip = toolTip + eval.getDescription().toManchesterSyntaxString(u, null) + ":\n\n";
				}
			}
			
			for(Individual ind : posInd) {
				
				for(String u : uri) {
					if(ind.toString().contains(u)) {
						toolTip = toolTip + ind.toManchesterSyntaxString(u, null) + "\n";
					}
				}
				
			}
			JOptionPane.showMessageDialog(null, toolTip);

		}
		
	}
	
	/**
	 * This method sets the currently selected evaluated description.
	 * @param e evaluated description
	 */
	public void setEvaluadtedDescription(EvaluatedDescription e) {
		eval = e;
	}
}
