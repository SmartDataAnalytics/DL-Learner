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

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.reasoning.FastInstanceChecker;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;

/**
 * This class reads the ontologie in a separate thread.
 * @author Christian Koetteritzsch
 *
 */
public class ReadingOntologyThread extends Thread {

	
	private boolean hasIndividuals;
	private FastInstanceChecker reasoner;
	private NamedClass currentConcept;
	private Set<Individual> individual;
	private final Set<String> ontologieURI;
	private final OWLEditorKit editor;
	private final DLLearnerModel model;
	private boolean isInconsistent;
	private final OWLClass current;
	private final DLLearnerView view;
	
	/**
	 * This is the constructor of the ReadingOntologyThread.
	 * @param editorKit OWLEditorKit
	 * @param frame OWLFrame
	 * @param v DL-Learner view
	 * @param m DL-Learner model
	 */
	public ReadingOntologyThread(OWLEditorKit editorKit, OWLFrame<OWLClass> frame, DLLearnerView v, DLLearnerModel m) {
		ontologieURI = new HashSet<String>();
		this.editor = editorKit;
		current =  editor.getOWLWorkspace().getOWLComponentFactory().getOWLClassSelectorPanel().getSelectedObject();
		this.view = v;
		this.model = m;
		
	}
	/**
	 * This method sets the individuals that belong to the concept which is
	 * chosen in protege.
	 */
	private void setPositiveConcept() {
		if(current != null) {
			SortedSet<Individual> individuals = null;
			hasIndividuals = false;
			// checks if selected concept is thing when yes then it selects all
			// individuals
			if (!(current instanceof Thing)) {
				List<NamedClass> classList = reasoner.getAtomicConceptsList();
				for(NamedClass concept : classList) {
					// if individuals is null
					if (individuals == null) {
						// checks if the concept is the selected concept in protege
						for(String onto : ontologieURI) {
							if (concept.toString().contains(onto)) {
								if (concept.toString().equals(
										onto + current.toString())) {
									// if individuals is not null it gets all
									// individuals of
									// the concept
									currentConcept = concept;
									if (reasoner.getIndividuals(concept) != null) {
										if (reasoner.getIndividuals(concept).size() > 0) {
											model.setInstancesCount(reasoner.getIndividuals(concept).size());
											hasIndividuals = true;
										}
										individual = reasoner.getIndividuals(concept);
										model.setIndividuals(individual);
										model.setCurrentConcept(currentConcept);
										break;
									}
								}
							}
						}
					}
				}
			} else {
				if (reasoner.getIndividuals().size() > 0) {
					hasIndividuals = true;
				
				}
				individual = reasoner.getIndividuals();
				model.setIndividuals(individual);
				model.setHasIndividuals(hasIndividuals);
			}
		}
	}
	
	/**
	 * This Method checks if the selected class has any individuals.
	 * 
	 * @return boolean hasIndividuals
	 */
	public boolean hasIndividuals() {
		return hasIndividuals;
	}
	
	/**
	 * Checks the URI if a "#" is in it.
	 */
	private void checkURI() {
		Set<OWLOntology> ont = editor.getModelManager().getActiveOntologies();
		Set<Individual> indi = reasoner.getIndividuals();
		for(OWLOntology onto : ont) {
			String ontURI = onto.getURI().toString();
			for(Individual ind : indi) {
				if(ind.toString().contains(ontURI)) {
					if(ind.toString().contains("#")) {
						ontologieURI.add(onto.getURI().toString()+"#");
						break;
					} else {
						ontologieURI.add(onto.getURI().toString());
						break;
					}
				}
			}
		}
		model.setOntologyURIString(ontologieURI);
	}
	
	///**
	// * This method sets the check boxes for the positive check boxes checked if
	// * the individuals matches the concept that is chosen in protege.
	// */
	//private void setPosVector() {
	//	setPositiveConcept();
	//	SortedSet<Individual> reasonerIndi = reasoner.getIndividuals();
	//	for(Individual ind : reasonerIndi) {
	//		Set<String> onto = ontologieURI;
	//		for(String ont : onto) {
	//			String indiv = ind.toString();
	//			// checks if individual belongs to the selected concept
	//				if (setPositivExamplesChecked(indiv)) {
	//					if (indiv.contains(ont)) {
	//						// when yes then it sets the positive example checked
	//
	//						// OWLExpressionCheckerFactory
	//						posListModel.add(0, ind.toManchesterSyntaxString(ont, null));
	//						individualVector.add(new IndividualObject(indiv, true));
	//						break;
	//					}
    //
	//				} else {
	//					// When no it unchecks the positive example
	//					if (indiv.contains(ont)) {
	//						individualVector
	//								.add(new IndividualObject(indiv, false));
	//						negListModel.add(0, ind.toManchesterSyntaxString(ont, null));
	//						break;
	//					}
	//				}
	//			}
	//	}
	//	//view.getPosAndNegSelectPanel().setExampleList(posListModel, negListModel);
	//	model.setPosListModel(posListModel);
	//	model.setNegListModel(negListModel);
	//	model.setIndividualVector(individualVector);
	//}
	
	///**
	// * This method gets an Individual and checks if this individual belongs to
	// * the concept chosen in protege.
	// * 
	//* @param indi
	// *            Individual to check if it belongs to the chosen concept
	// * @return is Individual belongs to the concept which is chosen in protege.
	// */
	//private boolean setPositivExamplesChecked(String indi) {
	//	boolean isChecked = false;
	//	// checks if individuals are not empty
	//	if (individual != null) {
	//		// checks if the delivered individual belongs to the individuals of
	//		// the selected concept
	//		if (individual.toString().contains(indi)) {
	//			isChecked = true;
	//		}
	//	}
	//	return isChecked;
    //
	//}
	
	@Override
	public void run() {
		model.unsetListModel();
		model.initReasoner();
		reasoner = model.getReasoner();
		isInconsistent = false;
		if(!isInconsistent) {
			
			this.checkURI();
			this.setPositiveConcept();
			//this.setPosVector();
			if (this.hasIndividuals()) {
				view.getRunButton().setEnabled(true);
			} else {
				view.getRunButton().setEnabled(false);
				view.getHintPanel().setVisible(false);
				String message ="There are no Instances for  available. Please insert some Instances.";
				view.renderErrorMessage(message);
			}
			//view.getPosAndNegSelectPanel().setExampleList(model.getPosListModel(), model.getNegListModel());
		} else {
			view.getHintPanel().setForeground(Color.RED);
			view.getRunButton().setEnabled(false);
			view.setHintMessage("The ontology is inconsistent and suggestions for class descriptions can only \nbe computed on consistent ontologies. Please repair the ontology first");
		}
	}
}
