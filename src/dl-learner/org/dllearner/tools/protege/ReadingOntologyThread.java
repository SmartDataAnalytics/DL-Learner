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
import org.dllearner.reasoning.FastInstanceChecker;
import org.protege.editor.owl.OWLEditorKit;
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
	private Set<String> ontologieURI;
	private final OWLEditorKit editor;
	private DLLearnerModel model;
	private boolean isInconsistent;
	private OWLClass current;
	private DLLearnerView view;
	
	/**
	 * This is the constructor of the ReadingOntologyThread.
	 * @param editorKit OWLEditorKit
	 * @param v DL-Learner view
	 * @param m DL-Learner model
	 */
	public ReadingOntologyThread(OWLEditorKit editorKit, DLLearnerView v, DLLearnerModel m) {
		this.editor = editorKit;
		this.view = v;
		this.model = m;
	}
	
	/**
	 * This method sets the view of the DL-Learner plugin.
	 * @param v DLLearnerView
	 */
	public void setDLLearnerView(DLLearnerView v) {
		this.view = v;
	}
	
	/**
	 * This method sets the model of the DL-Learner plugin.
	 * @param m DLLearnerModel
	 */
	public void setDLLearnerModel(DLLearnerModel m) {
		this.model = m;
	}
	/**
	 * This method sets the individuals that belong to the concept which is
	 * chosen in protege.
	 */
	private void setPositiveConcept() {
		current =  editor.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
		if(current != null) {
			SortedSet<Individual> individuals = null;
			hasIndividuals = false;
			System.out.println("hier: " + editor.getOWLModelManager().getActiveOntology().getAxioms(current));
			// checks if selected concept is thing when yes then it selects all
			// individuals
			if (!(current.toString().equals("Thing"))) {
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
											hasIndividuals = true;
										}
										individual = reasoner.getIndividuals(concept);
										model.setIndividuals(individual);
										model.setHasIndividuals(hasIndividuals);
										model.setCurrentConcept(currentConcept);
										view.getRunButton().setEnabled(true);
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
		ontologieURI = new HashSet<String>();
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
	
	@Override
	public void run() {
		String loading ="loading instances...";
		view.getHintPanel().setForeground(Color.RED);
		view.setHintMessage(loading);
		if(!model.isReasonerSet() || model.getIsKnowledgeSourceIsUpdated() == true) {
			model.setKnowledgeSource();
			model.setReasoner();
		}
		reasoner = model.getReasoner();
		isInconsistent = view.getIsInconsistent();
		if(!isInconsistent) {
			this.checkURI();
			this.setPositiveConcept();
			if (this.hasIndividuals()) {
				view.getRunButton().setEnabled(true);
				view.getHintPanel().setForeground(Color.BLACK);
				view.setHintMessage("To get suggestions for class descriptions, please click the button above.");
				
			} else {
				view.getRunButton().setEnabled(false);
				view.getHintPanel().setVisible(true);
				String message ="There are no Instances for " + current + " available. Please insert some Instances.";
				view.getHintPanel().setForeground(Color.RED);
				view.setHintMessage(message);
			}
		} else {
			view.getHintPanel().setForeground(Color.RED);
			view.getRunButton().setEnabled(false);
			view.setHintMessage("The ontology is inconsistent and suggestions for class descriptions can only \nbe computed on consistent ontologies. Please repair the ontology first");
		}
	}
	
	/**
	 * This method returns the NamedClass for the currently selected class.
	 * @return NamedClass of the currently selected class
	 */
	public NamedClass getCurrentConcept() {
		return currentConcept;
	}
}
