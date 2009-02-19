package org.dllearner.tools.protege;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JOptionPane;

import org.dllearner.algorithms.EvaluatedDescriptionPosNeg;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;

public class MoreDetailForSuggestedConceptsPanelHandler implements ActionListener{

	private static final String OLD_BUTTON_LABEL = "old";
	private static final String NEW_BUTTON_LABEL = "new";
	private DLLearnerModel model;
	private EvaluatedDescription eval;
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
			Set<Individual> posInd = ((EvaluatedDescriptionPosNeg)eval).getCoveredPositives();
			Set<String> uri = model.getOntologyURIString();
			String toolTip = "This are the Individuals beloning to\n ";
			for(String u : uri) {
				if(eval.getDescription().toString().contains(u)) {
					toolTip = toolTip + eval.getDescription().toManchesterSyntaxString(u, null) + ":\n\n";
				}
			}
			
			for(Individual ind : posInd ) {
				
				for(String u : uri) {
					if(ind.toString().contains(u)) {
						toolTip = toolTip + ind.toManchesterSyntaxString(u, null) + "\n";
					}
				}
				
			}
			JOptionPane.showMessageDialog(null, toolTip);

		}
		
	}
	
	public void setEvaluadtedDescription(EvaluatedDescription e) {
		eval = e;
	}
}
