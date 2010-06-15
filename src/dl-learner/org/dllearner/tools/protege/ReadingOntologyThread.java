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
import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.reasoning.ProtegeReasoner;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * This class reads the ontology in a separate thread.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ReadingOntologyThread extends Thread {

	private boolean hasIndividuals;
	private ProtegeReasoner reasoner;
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
	 * 
	 * @param editorKit
	 *            OWLEditorKit
	 * @param v
	 *            DL-Learner view
	 * @param m
	 *            DL-Learner model
	 */
	public ReadingOntologyThread(OWLEditorKit editorKit, DLLearnerView v,
			DLLearnerModel m) {
		this.editor = editorKit;
		this.view = v;
		this.model = m;
	}

	/**
	 * This method sets the view of the DL-Learner plugin.
	 * 
	 * @param v
	 *            DLLearnerView
	 */
	public void setDLLearnerView(DLLearnerView v) {
		this.view = v;
	}

	/**
	 * This method sets the model of the DL-Learner plugin.
	 * 
	 * @param m
	 *            DLLearnerModel
	 */
	public void setDLLearnerModel(DLLearnerModel m) {
		this.model = m;
	}

	/**
	 * This method sets the individuals that belong to the concept which is
	 * chosen in protege.
	 */
	private void setPositiveConcept() {
		current = editor.getOWLWorkspace().getOWLSelectionModel()
				.getLastSelectedClass();
		if (current != null) {
			hasIndividuals = false;
			// checks if selected concept is thing when yes then it selects all
			// individuals
			if (!current.isOWLThing()) {
				Description desc = OWLAPIConverter.convertClass(current);
				individual = reasoner.getIndividuals(desc);
				model.setIndividuals(individual);
				model.setHasIndividuals(hasIndividuals);
				model.setCurrentConcept(new NamedClass(desc.toString()));
				view.getRunButton().setEnabled(true);
				if (reasoner.getIndividuals(desc)
						.size() > 0) {
					hasIndividuals = true;
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
	 * Puts every base uri in a HashSet.
	 */
	private void checkURI() {
		ontologieURI = new HashSet<String>();
		Set<Individual> indi = reasoner.getIndividuals();
		for (Individual ind : indi) {
			int ontURI = ind.toString().lastIndexOf("/");
			int ontURI2 = ind.toString().lastIndexOf("#");
			String uriNeu = "";
			String uriAlt = "";
			if (ontURI2 != -1) {
				uriNeu = ind.toString().substring(0, ontURI2 + 1);
				if (uriNeu != uriAlt) {
					ontologieURI.add(uriNeu);
					uriAlt = uriNeu;
					uriNeu = "";
					String uriTest = indi.toString().replace(uriAlt, "");
					if(!uriTest.contains("/") && !uriTest.contains("#")) {
						break;
					}
				}
				} else { 
					uriNeu = ind.toString().substring(0, ontURI + 1);
					if (uriNeu != uriAlt) {
						ontologieURI.add(uriNeu);
						uriAlt = uriNeu;
						uriNeu = "";
						String uriTest = indi.toString().replace(uriAlt, "");
						if(!uriTest.contains("/") && !uriTest.contains("#")) {
							break;
						}
						
					}
				}
		}
		model.setOntologyURIString(ontologieURI);
	}

	@Override
	public void run() {
		view.setStatusBarVisible(true);
		view.setBusy(true);
		Manager.getInstance().initKnowledgeSource();
		Manager.getInstance().initReasoner();
		view.setStatusBarVisible(false);
		view.setBusy(false);
		if(Manager.getInstance().canLearn()){
			view.getRunButton().setEnabled(true);
			view
			.setHintMessage("<html><font size=\"3\">To get suggestions for class descriptions, please click the button above.</font></html>");
		} else {
			String message = "<html><font size=\"3\" color=\"red\">There are no instances for "
				+ current
				+ " available. Please insert some instances.</font></html>";
		view.getHintPanel().setForeground(Color.RED);
		view.setHintMessage(message);
		}
		
//		String loading = "<html><font size=\"3\">loading instances...</font></html>";
//		view.getHintPanel().setForeground(Color.RED);
//		view.setHintMessage(loading);
//		if (!model.isReasonerSet()
//				|| model.getIsKnowledgeSourceIsUpdated() == true) {
//			model.setKnowledgeSource();
//			model.setReasoner();
//		}
//		reasoner = model.getReasoner();
//		isInconsistent = view.getIsInconsistent();
//		if (!isInconsistent) {
//			this.checkURI();
//			this.setPositiveConcept();
//			if (this.hasIndividuals()) {
//				view.getRunButton().setEnabled(true);
//				view.getHintPanel().setForeground(Color.BLACK);
//				view
//						.setHintMessage("<html><font size=\"3\">To get suggestions for class descriptions, please click the button above.</font></html>");
//
//			} else {
//				view.getRunButton().setEnabled(false);
//				view.getHintPanel().setVisible(true);
//				String message = "<html><font size=\"3\" color=\"red\">There are no Instances for "
//						+ current
//						+ " available. Please insert some Instances.</font></html>";
//				view.getHintPanel().setForeground(Color.RED);
//				view.setHintMessage(message);
//			}
//		} else {
//			view.getHintPanel().setForeground(Color.RED);
//			view.getRunButton().setEnabled(false);
//			view
//					.setHintMessage("The ontology is inconsistent and suggestions for class descriptions can only \nbe computed on consistent ontologies. Please repair the ontology first");
//		}
	}

	/**
	 * This method returns the NamedClass for the currently selected class.
	 * 
	 * @return NamedClass of the currently selected class
	 */
	public NamedClass getCurrentConcept() {
		return currentConcept;
	}
}
