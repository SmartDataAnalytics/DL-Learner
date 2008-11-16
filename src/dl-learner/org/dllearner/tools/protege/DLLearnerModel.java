/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.jdesktop.swingx.JXTaskPane;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * This Class provides the necessary methods to learn Concepts from the
 * DL-Learner.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class DLLearnerModel implements Runnable {

	// The Sting is for components that are available in the DL-Learner

	private String[] componenten = { "org.dllearner.kb.OWLFile",
			"org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.DIGReasoner",
			"org.dllearner.reasoning.FastRetrievalReasoner",
			"org.dllearner.learningproblems.PosNegInclusionLP",
			"org.dllearner.learningproblems.PosNegDefinitionLP",
			"org.dllearner.algorithms.RandomGuesser",
			"org.dllearner.algorithms.refinement.ROLearner",
			"org.dllearner.algorithms.refexamples.ExampleBasedROLComponent",
			"org.dllearner.algorithms.gp.GP" };


	// Component Manager that manages the components of the DL-Learner

	private ComponentManager cm;

	// The Reasoning Service for the Reasoner

	private ReasonerComponent rs;

	// The Knowledge source for the reasoner

	private KnowledgeSource source;

	// The View of the DL-Learner Plugin

	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;

	// This is the count of Concepts which you get after learning

	// A Array of Concepts which the DL-Learner suggested

	private Description[] description;

	// The Learning problem that is used to learn new concepts

	private LearningProblem lp;

	// This boolean is for clearing the suggest Panel

	private boolean alreadyLearned = false;

	// The Ontology which is currently used

	private OWLOntology ontology;

	// This is the learning algorithm

	private LearningAlgorithm la = null;

	// Necessary to get the currently loaded Ontology

	private OWLEditorKit editor;

	// Necessary to get the BaseUri of the currently loaded Ontology

	private OWLFrame<OWLClass> current;

	// The Reasoner which is used to learn

	private OWLAPIReasoner reasoner;

	// A Set of Descriptions in OWL Syntax which the DL-Learner suggested

	private Set<OWLDescription> owlDescription;

	// This set stores the positive examples.

	private Set<String> positiveExamples;

	// This set stores the negative examples that doesn't belong to the concept.

	private Set<String> negativeExamples;

	// The most fitting Description in OWL Syntax which the DL-Learner suggested

	private OWLDescription desc;

	// String to distinguish between Equivalent classes and sub classes

	private String id;

	// The new Concept which is learned by the DL-Learner

	private OWLDescription newConceptOWLAPI;

	// The old concept that is chosen in Protege

	private OWLDescription oldConceptOWLAPI;

	// A Set of Descriptions in OWL Syntax which the DL-Learner suggested

	private Set<OWLDescription> ds;

	// The model for the suggested Descriptions

	private DefaultListModel suggestModel;

	// The Individuals of the Ontology

	private Set<Individual> individual;

	// The error message which is rendered when an error occured

	private String error;

	// This is the new axiom which will be added to the Ontology

	private OWLAxiom axiomOWLAPI;

	// This is necessary to get the details of the suggested concept

	private JXTaskPane detailPane;
	private String ontologyURI;
	private Map<String, String> prefixes;
	private DefaultListModel posListModel;
	private DefaultListModel negListModel;
	private Vector<IndividualObject> individualVector;

	// This is a List of evaluated descriptions to get more information of the
	// suggested concept
	private List<EvaluatedDescription> evalDescriptions;

	/**
	 * This is the constructor for DL-Learner model.
	 * 
	 * @param editorKit
	 *            Editor Kit to get the currently loaded Ontology
	 * @param h
	 *            OWLFrame(OWLClass) to get the base uri of the Ontology
	 * @param id
	 *            String if it learns a subclass or a superclass.
	 * @param view
	 *            current view of the DL-Learner tab
	 */
	public DLLearnerModel(OWLEditorKit editorKit, OWLFrame<OWLClass> h,
			String id,
			OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view) {
		editor = editorKit;
		current = h;
		this.id = id;
		this.view = view;
		ontologyURI = editor.getModelManager().getActiveOntology().getURI().toString()+"#";
		owlDescription = new HashSet<OWLDescription>();
		posListModel = new DefaultListModel();
		negListModel = new DefaultListModel();
		ComponentManager.setComponentClasses(componenten);
		individualVector = new Vector<IndividualObject>();
		cm = ComponentManager.getInstance();
		ds = new HashSet<OWLDescription>();
		suggestModel = new DefaultListModel();
		detailPane = new JXTaskPane();
		detailPane.setTitle("Details");

	}

	/**
	 * This method initializes the SimpleSuggestionLearningAlgorithm and adds
	 * the suggestions to the suggest panel model.
	 */
	public void initReasoner() {
		alreadyLearned = false;
		setKnowledgeSource();
		setReasoner();
		prefixes = reasoner.getPrefixes();

	}

	/**
	 * This method checks which positive and negative examples are checked and
	 * puts the checked examples into a tree set.
	 */
	public void setPositiveAndNegativeExamples() {
		positiveExamples = new TreeSet<String>();
		negativeExamples = new TreeSet<String>();
		for (int i = 0; i < individualVector.size(); i++) {
			if (individualVector.get(i).isPositiveExample()) {
				positiveExamples.add(individualVector.get(i).getIndividualString());
			} else {
				negativeExamples.add(individualVector.get(i).getIndividualString());
			}
		}
	}

	/**
	 * This method returns the data for the suggest panel.
	 * 
	 * @return Model for the suggest panel.
	 */
	public DefaultListModel getSuggestList() {
		return suggestModel;
	}

	/**
	 * This method returns an array of descriptions learned by the DL-Learner.
	 * 
	 * @return Array of descriptions learned by the DL-Learner.
	 */
	public Description[] getDescriptions() {
		return description;
	}

	/**
	 * This Method returns a List of evaluated descriptions suggested by the
	 * DL-Learner.
	 * 
	 * @return list of evaluated descriptions
	 */
	public List<EvaluatedDescription> getEvaluatedDescriptionList() {
		return evalDescriptions;
	}

	/**
	 * This method sets the knowledge source for the learning process. Only
	 * OWLAPIOntology will be available.
	 */
	public void setKnowledgeSource() {
		this.source = new OWLAPIOntology(editor.getModelManager()
				.getActiveOntology());
	}

	/**
	 * This method sets the reasoner and the reasoning service Only
	 * OWLAPIReasoner is available.
	 */
	public void setReasoner() {
		this.reasoner = cm.reasoner(OWLAPIReasoner.class, source);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			System.out.println("fehler!!!!!!!!!");
			e.printStackTrace();
		}
//		rs = cm.reasoningService(reasoner);
	}

	/**
	 * This method sets the Learning problem for the learning process.
	 * PosNegDefinitonLp for equivalent classes and PosNegInclusionLP for super
	 * classes.
	 */
	public void setLearningProblem() {
		if (id.equals("equivalent classes")) {
			// sets the learning problem to PosNegDefinitionLP when the
			// dllearner should suggest an equivalent class
			lp = cm.learningProblem(PosNegDefinitionLP.class, reasoner);
		}
		if (id.equals("super classes")) {
			// sets the learning problem to PosNegInclusionLP when the dllearner
			// should suggest a subclass
			lp = cm.learningProblem(PosNegInclusionLP.class, reasoner);
		}
		// adds the positive examples
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		// adds the neagtive examples
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		try {
			lp.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method sets the learning algorithm for the learning process.
	 */
	public void setLearningAlgorithm() {
		try {
			// sets the learning algorithm to ROlearner
			this.la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp,
					reasoner);
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("TIME: "+view.getPosAndNegSelectPanel().getOptionPanel().getMaxExecutionTime());
		cm.applyConfigEntry(la, "maxExecutionTimeInSeconds", view.getPosAndNegSelectPanel().getOptionPanel().getMaxExecutionTime());
		try {
			// initializes the learning algorithm
			la.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		alreadyLearned = true;
	}

	/**
	 * This method starts the learning process.
	 */
	public void run() {
		error = "Learning succesful";
		String message = "To view details about why a class description was suggested, please doubleclick on it.";
		// start the algorithm and print the best concept found
		la.start();
		//description = new Description[la.getCurrentlyBestEvaluatedDescriptions(view.getPosAndNegSelectPanel().getOptionPanel().getNrOfConcepts())
		//		.size()];
		//addToListModel();
		// renders the errormessage
		view.renderErrorMessage(error);
		view.setHintMessage(message);
		// reenables the run button
		//view.getRunButton().setEnabled(true);
		// disables the cancel button
		//view.getPosAndNegSelectPanel().setCheckBoxesEnable(true);
		//view.getSuggestClassPanel().setSuggestList(suggestModel);
	}

	/**
	 * This method returns the Concepts from the DL-Learner.
	 * 
	 * @return Array of learned Concepts.
	 */
	public Description[] getSolutions() {
		return description;
	}

	/**
	 * This method gets an error message and storess it.
	 * 
	 * @param err
	 *            error message
	 */
	public void setErrorMessage(String err) {
		this.error = err;
	}

	/**
	 * This method sets the check boxes for the positive check boxes checked if
	 * the individuals matches the concept that is chosen in protege.
	 */
	public void setPosVector() {
		setPositiveConcept();

		for (Iterator<Individual> j = reasoner.getIndividuals().iterator(); j
				.hasNext();) {
			Individual ind = j.next();
			String indiv = ind.toString();
			// checks if individual belongs to the selected concept
			if (setPositivExamplesChecked(indiv)) {
				// when yes then it sets the positive example checked
				posListModel.add(0, ind.toManchesterSyntaxString(ontologyURI, prefixes));
				individualVector.add(new IndividualObject(indiv, ind.toManchesterSyntaxString(ontologyURI, prefixes), true));

			} else {
				// When no it unchecks the positive example
				negListModel.add(0, ind.toManchesterSyntaxString(ontologyURI, prefixes));
				individualVector.add(new IndividualObject(indiv, ind.toManchesterSyntaxString(ontologyURI, prefixes), false));
			}
		}
	}

	/**
	 * This method resets the Concepts that are learned.
	 */
	public void unsetNewConcepts() {
		while (owlDescription.iterator().hasNext()) {
			owlDescription.remove(owlDescription.iterator().next());
			
		}
	}
	
	/**
	 * This method returns the Vector of IndividualObjects.
	 * @return individualVector Vector
	 */
	public Vector<IndividualObject> getIndividualVector() {
		return individualVector;
	}
	/**
	 * This method sets the individuals that belong to the concept which is
	 * chosen in protege.
	 */
	public void setPositiveConcept() {
		SortedSet<Individual> individuals = null;
		//System.out.println("TEST: " +  rs.getNamedClasses());
		// checks if selected concept is thing when yes then it selects all
		// individuals
		if (!current.getRootObject().toString().equals("Thing")) {
			
			for (Iterator<NamedClass> i = reasoner.getAtomicConceptsList().iterator(); i
					.hasNext();) {
				// if individuals is null
				if (individuals == null) {
					NamedClass concept = i.next();
					// checks if the concept is the selected concept in protege
					if (concept.toString().endsWith(
							"#" + current.getRootObject().toString())) {
						// if individuals is not null it gets all individuals of
						// the concept
						if (reasoner.getIndividuals(concept) != null) {
							individual = reasoner.getIndividuals(concept);
							break;
						}
					}
				}
			}
		} else {
			individual = reasoner.getIndividuals();
		}
	}
	
	/**
	 * This Method checks if the selected class has any individuals.
	 * @param owlConcept OWLClass
	 * @return boolean hasIndividuals
	 */
	public boolean hasIndividuals(OWLClass owlConcept) {
		boolean hasIndividuals = false;
		NamedClass concept = null;
		NamedClass selectedConcept = null;
		Iterator<NamedClass> it = reasoner.getNamedClasses().iterator();
		while(it.hasNext()) {
			concept = it.next();
			if (concept.toManchesterSyntaxString(ontologyURI, prefixes).equals(owlConcept.toString())) {
				selectedConcept = concept;
				break;
			}
		}
		if (reasoner.getIndividuals(selectedConcept).size() > 0) {
			hasIndividuals = true;
		}
		concept = null;
		selectedConcept = null;
		return hasIndividuals;
	}
	/**
	 * This method gets an Individual and checks if this individual belongs to
	 * the concept chosen in protege.
	 * 
	 * @param indi
	 *            Individual to check if it belongs to the chosen concept
	 * @return is Individual belongs to the concept which is chosen in protege.
	 */
	public boolean setPositivExamplesChecked(String indi) {
		boolean isChecked = false;
		// checks if individuals are not empty
		if (individual != null) {
			// checks if the delivered individual belongs to the individuals of
			// the selected concept
			if (individual.toString().contains(indi)) {
				isChecked = true;
			}
		}
		return isChecked;

	}

	/**
	 * This method resets the vectors where the check boxes for positive and
	 * negative Examples are stored. It is called when the DL-Learner View is
	 * closed.
	 */
	public void clearVector() {
		individualVector.removeAllElements();
		posListModel.removeAllElements();
		negListModel.removeAllElements();
	}

	/**
	 * This method gets an array of concepts from the DL-Learner and stores it
	 * in the description array.
	 * 
	 * @param list
	 *            Array of concepts from DL-Learner
	 */
	public void setDescriptionList(Description[] list) {
		description = list;
	}
	
	/**
	 * This method returns the PosListModel.
	 * @return DefaultListModel posListModel
	 */
	public DefaultListModel getPosListModel() {
		return posListModel;
	}
	
	/**
	 * This method returns the NegListModel.
	 * @return DefaultListModel negListModel
	 */
	public DefaultListModel getNegListModel() {
		return negListModel;
	}
	/**
	 * This method returns the current learning algorithm that is used to learn
	 * new concepts.
	 * 
	 * @return Learning algorithm that is used for learning concepts.
	 */
	public LearningAlgorithm getLearningAlgorithm() {
		return la;
	}

	/**
	 * This method resets the array of concepts from the DL_Learner. It is
	 * called after the DL-Learner tab is closed.
	 */
	public void resetSuggestionList() {
		for (int i = 0; i < description.length; i++) {
			description[i] = null;
		}
	}

	/**
	 * This method resets the model for the suggest panel. It is called befor
	 * the DL-Learner learns the second time or when the DL-Learner tab is
	 * closed.
	 */
	public void unsetListModel() {
		if (suggestModel != null) {
			suggestModel.removeAllElements();
		}
	}

	/**
	 * This method gets a description from the DL-Learner and adds is to the
	 * model from the suggest panel.
	 * 
	 * @param descript
	 *            Description from the DL-Learner
	 */
	public void setSuggestModel(Description descript) {
		suggestModel.add(0, descript);
	}

	/**
	 * This method returns the current OWLOntology that is loaded in protege.
	 * 
	 * @return current ontology
	 */
	public OWLOntology getOWLOntology() {
		return ontology;
	}

	/**
	 * This method returns a set of concepts that are learned by the DL-Learner.
	 * They are already converted into the OWLDescription format.
	 * 
	 * @return Set of learned concepts in OWLDescription format
	 */
	public Set<OWLDescription> getNewOWLDescription() {
		return owlDescription;
	}

	/**
	 * This method returns the old concept which is chosen in protege in
	 * OWLDescription format.
	 * 
	 * @return Old Concept in OWLDescription format.
	 */
	public OWLDescription getOldConceptOWLAPI() {
		return oldConceptOWLAPI;
	}

	/**
	 * This method returns the currently learned description in OWLDescription
	 * format.
	 * 
	 * @return currently used description in OWLDescription format
	 */
	public OWLDescription getSolution() {
		return desc;
	}

	/**
	 * This method gets a description learned by the DL-Learner an converts it
	 * to the OWLDescription format.
	 * 
	 * @param desc
	 *            Description learned by the DL-Learner
	 */
	private void setNewConceptOWLAPI(Description des) {
		// converts DL-Learner description into an OWL API Description
		newConceptOWLAPI = OWLAPIDescriptionConvertVisitor
				.getOWLDescription(des);
		ds.add(newConceptOWLAPI);
		owlDescription.add(newConceptOWLAPI);
		this.desc = newConceptOWLAPI;
	}

	/**
	 * This methode returns the Model for the suggested Concepts.
	 * 
	 * @return DefaultListModel
	 */
	public DefaultListModel getSuggestModel() {
		return suggestModel;
	}

	/**
	 * This method gets the old concept from checking the positive examples.
	 */
	private void setOldConceptOWLAPI() {
		// gets all individuals
		SortedSet<Individual> indi = reasoner.getIndividuals();
		// Iterator of Individuals
		for (Iterator<Individual> i = indi.iterator(); i.hasNext();) {
			Individual indi2 = i.next();
			// checks if the current individual belongs to positive examples
			if (positiveExamples != null) {
				if (positiveExamples.toString().contains(indi2.toString())) {
					// if yes then get the concepts of this individuals
					Set<NamedClass> concept = reasoner.getTypes(indi2);
					// adds all concepts to old concept OWLAPI
					for (Iterator<NamedClass> k = concept.iterator(); k
							.hasNext();) {
						OWLDescription oldOWLAPI = OWLAPIDescriptionConvertVisitor
								.getOWLDescription(k.next());
						oldConceptOWLAPI = oldOWLAPI;
						ds.add(oldOWLAPI);
					}

				}
			}
		}
	}

	/**
	 * This method stores the new concept learned by the DL-Learner in the
	 * Ontology.
	 * 
	 * @param descript
	 *            Description learn by the DL-Learner
	 */
	public void changeDLLearnerDescriptionsToOWLDescriptions(
			Description descript) {
		setNewConceptOWLAPI(descript);
		setOldConceptOWLAPI();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLDataFactory factory = manager.getOWLDataFactory();
		if (id.equals("Equivalent classes")) {
			axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		} else {
			axiomOWLAPI = factory.getOWLSubClassAxiom(newConceptOWLAPI,
					oldConceptOWLAPI);
		}
		OWLOntology onto = editor.getModelManager().getActiveOntology();
		AddAxiom axiom = new AddAxiom(onto, axiomOWLAPI);
		try {
			// adds the new concept to the ontology
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method returns the currently used reasoning service.
	 * 
	 * @return current reasoning service
	 */
	public ReasonerComponent getReasonerComponent() {
		return rs;
	}

	/**
	 * This method gets the status if the DL-Learner has already learned. It is
	 * only for reseting the suggest panel.
	 * 
	 * @return boolean if the learner has already learned
	 */
	public boolean getAlreadyLearned() {
		return alreadyLearned;
	}
	
	/**
	 * This Method checks if after inserting of this concept the ontology is still
	 * consistent.
	 * @param eDescription EvauatedDescription
	 * @return isConsistent boolean
	 */
	public boolean isConsistent(EvaluatedDescription eDescription) {
		boolean isConsistent = false;
		if (eDescription.getNotCoveredPositives().isEmpty()) {
			isConsistent = true;
		} else {
			isConsistent = false;
		}
		return isConsistent;
	}

	/**
	 * This Method returns the URI of the currently loaded Ontology.
	 * 
	 * @return URI Ontology URI
	 */
	public URI getURI() {
		return editor.getModelManager().getActiveOntology().getURI();
	}
	
	/**
	 * This method sets the suggestion list.
	 * @param list List(EvaluatedDescription)
	 */
	public void setSuggestList(List<EvaluatedDescription> list) {
		evalDescriptions = list;
	}
}
