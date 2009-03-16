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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;

import org.dllearner.algorithms.EvaluatedDescriptionClass;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
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
public class DLLearnerModel implements Runnable{

	// The Sting is for components that are available in the DL-Learner

	private final String[] componenten = { "org.dllearner.kb.OWLFile",
			"org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.FastInstanceChecker",
			"org.dllearner.reasoning.FastRetrievalReasoner",
			"org.dllearner.algorithms.RandomGuesser",
			"org.dllearner.algorithms.refinement.ROLearner",
			"org.dllearner.algorithms.celoe.CELOE",
			"org.dllearner.algorithms.gp.GP", "org.dllearner.learningproblems.PosOnlyLP",
			"org.dllearner.learningproblems.PosNegLPStandard", "org.dllearner.learningproblems.ClassLearningProblem"};

	// Component Manager that manages the components of the DL-Learner

	private final ComponentManager cm;

	private static final String EQUIVALENT_CLASS_AXIOM_STRING = "equivalent class";
	private static final String SUPER_CLASS_AXIOM_STRING = "Suggest super class";
	private static final String EQUIVALENT_CLASS_LEARNING = "equivalence";
	private static final String SUPER_CLASS_LEARNING = "superClass";

	// The View of the DL-Learner Plugin


	// The Learning problem that is used to learn new concepts

	private LearningProblem lp;

	// This boolean is for clearing the suggest Panel

	private boolean alreadyLearned = false;

	// This is the learning algorithm

	private LearningAlgorithm la = null;

	// Necessary to get the currently loaded Ontology

	private final OWLEditorKit editor;

	// The Reasoner which is used to learn

	private FastInstanceChecker reasoner;

	// A Set of Descriptions in OWL Syntax which the DL-Learner suggested

	private final Set<OWLDescription> owlDescription;

	// The most fitting Description in OWL Syntax which the DL-Learner suggested

	private OWLDescription desc;

	// String to distinguish between Equivalent classes and sub classes

	private String id;

	// The new Concept which is learned by the DL-Learner

	private OWLDescription newConceptOWLAPI;

	// The old concept that is chosen in Protege

	private OWLDescription oldConceptOWLAPI;

	// A Set of Descriptions in OWL Syntax which the DL-Learner suggested

	private final Set<OWLDescription> ds;

	// The model for the suggested Descriptions

	private final DefaultListModel suggestModel;

	// The Individuals of the Ontology

	private Set<Individual> individual;
	private int instancesCount;

	// The error message which is rendered when an error occured


	// This is the new axiom which will be added to the Ontology

	private OWLAxiom axiomOWLAPI;

	// This is necessary to get the details of the suggested concept

	private final Set<KnowledgeSource> sources;
	private boolean hasIndividuals;
	private NamedClass currentConcept;
	private Set<String> ontologieURI;
	private final boolean ontologyConsistent;
	private final DLLearnerView view;

	// This is a List of evaluated descriptions to get more information of the
	// suggested concept
	private List<? extends EvaluatedDescription> evalDescriptions;

	/**
	 * This is the constructor for DL-Learner model.
	 * 
	 * @param editorKit
	 *            Editor Kit to get the currently loaded Ontology
	 * @param id
	 *            String if it learns a subclass or a superclass.
	 * @param view
	 *            current view of the DL-Learner tab
	 */
	public DLLearnerModel(OWLEditorKit editorKit, DLLearnerView view) {
		editor = editorKit;
		
		this.view = view;
		ontologyConsistent = true;
		instancesCount = 0;
		owlDescription = new HashSet<OWLDescription>();
		ComponentManager.setComponentClasses(componenten);
		cm = ComponentManager.getInstance();
		ds = new HashSet<OWLDescription>();
		suggestModel = new DefaultListModel();
		ontologieURI = new HashSet<String>();
		sources = new HashSet<KnowledgeSource>();
	}
	
	public void setID(String d) {
		this.id = d;
	}
	/**
	 * This method initializes the SimpleSuggestionLearningAlgorithm and adds
	 * the suggestions to the suggest panel model.
	 */
	public void initReasoner() {
		alreadyLearned = false;
		setKnowledgeSource();
		setReasoner();

	}

	/**
	 * This Method returns a List of evaluated descriptions suggested by the
	 * DL-Learner.
	 * 
	 * @return list of evaluated descriptions
	 */
	public List<? extends EvaluatedDescription> getEvaluatedDescriptionList() {
		return evalDescriptions;
	}

	/**
	 * This method sets the knowledge source for the learning process. Only
	 * OWLAPIOntology will be available.
	 */
	public void setKnowledgeSource() {
		Set<OWLOntology> ontologieSet = editor.getModelManager().getActiveOntologies();
		for(OWLOntology onto : ontologieSet) {
			sources.add(new OWLAPIOntology(onto));
		}
	}

	/**
	 * This method sets the reasoner. Only
	 * FastInstanceChecker is available.
	 */
	public void setReasoner() {
		this.reasoner = cm.reasoner(FastInstanceChecker.class, sources);
		try {
			reasoner.init();
			reasoner.isSatisfiable();
			view.setIsInconsistent(false);
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			System.out.println("fehler!!!!!!!!!");	
			e.printStackTrace();
		} catch (InconsistentOntologyException incon) {
			view.setIsInconsistent(true);
		}
	}
	
	/**
	 * This method returns the fast instance checker reasoner.
	 * @return fast instance checker reasoner
	 */
	public FastInstanceChecker getReasoner() {
		return reasoner;
	}
	
	/**
	 * This method returns the current concept.
	 * @return current concept
	 */
	public NamedClass getCurrentConcept() {
		return currentConcept;
	}
	/**
	 * This method sets the Learning problem for the learning process.
	 * PosNegDefinitonLp for equivalent classes and PosNegInclusionLP for super
	 * classes.
	 */
	public void setLearningProblem() {
		lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		cm.applyConfigEntry(lp, "classToDescribe", currentConcept.toString());
		System.out.println("lern: " + currentConcept);
		if (id.equals(EQUIVALENT_CLASS_AXIOM_STRING)) {
			// sets the learning problem to PosNegDefinitionLP when the
			// dllearner should suggest an equivalent class
			cm.applyConfigEntry(lp, "type", EQUIVALENT_CLASS_LEARNING);
		}
		if (id.equals(SUPER_CLASS_AXIOM_STRING)) {
			// sets the learning problem to PosNegInclusionLP when the dllearner
			// should suggest a subclass
			cm.applyConfigEntry(lp, "type", SUPER_CLASS_LEARNING);
		}
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
			this.la = cm.learningAlgorithm(CELOE.class, lp,
					reasoner);
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cm.applyConfigEntry(la, "useNegation", false);
		cm.applyConfigEntry(la, "noisePercentage", 5.0);
		cm.applyConfigEntry(la, "maxExecutionTimeInSeconds", view
				.getPosAndNegSelectPanel().getOptionPanel()
				.getMaxExecutionTime());
		try {
			// initializes the learning algorithm
			la.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		alreadyLearned = true;
	}
	
	/**
	 * Starts the learning algorithm.
	 */
	public void run() {
		la.start();
	}

	/**
	 * This method sets the positive examples for learning. 
	 * @param ind Set of Individuals
	 */
	public void setIndividuals(Set<Individual> ind) {
		individual = ind;
	}
	
	/**
	 * This method sets the uri sting for the currently used
	 * for learning. 
	 * @param uri Set of uris
	 */
	public void setOntologyURIString(Set<String> uri) {
		this.ontologieURI = uri;
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
	 * Sets if the ontology has individuals.
	 * @param has boolean if concept has Individuals
	 */
	public void setHasIndividuals(boolean has) {
		this.hasIndividuals = has;
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
		oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor
		.getOWLDescription(currentConcept);
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
	 * This method stores the new concept learned by the DL-Learner in the
	 * Ontology.
	 * 
	 * @param descript
	 *            Description learn by the DL-Learner
	 */
	public void changeDLLearnerDescriptionsToOWLDescriptions(
			Description descript) {
		setNewConceptOWLAPI(descript);
		oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor
				.getOWLDescription(currentConcept);
		ds.add(oldConceptOWLAPI);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLDataFactory factory = manager.getOWLDataFactory();
		if (id.equals(EQUIVALENT_CLASS_AXIOM_STRING)) {
			axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		} else {
			axiomOWLAPI = factory.getOWLSubClassAxiom(oldConceptOWLAPI,
					newConceptOWLAPI);
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
	 * This method gets the status if the DL-Learner has already learned. It is
	 * only for reseting the suggest panel.
	 * 
	 * @return boolean if the learner has already learned
	 */
	public boolean getAlreadyLearned() {
		return alreadyLearned;
	}

	/**
	 * This Method checks if after inserting of this concept the ontology is
	 * still consistent.
	 * 
	 * @param eDescription
	 *            EvauatedDescription
	 * @return isConsistent boolean
	 */
	public boolean isConsistent(EvaluatedDescription eDescription) {
		boolean isConsistent = false;
		if (((EvaluatedDescriptionClass) eDescription).getCoveredInstances().size() < instancesCount) {
			isConsistent = false;
		} else {
			isConsistent = true;
		}
		return isConsistent;
	}

	/**
	 * This method sets the suggestion list.
	 * 
	 * @param list
	 *            List(EvaluatedDescription)
	 */
	public void setSuggestList(List<? extends EvaluatedDescription> list) {
		evalDescriptions = list;
	}
	
	/**
	 * This method returns the Strings of the Ontology uri's that are currently used.
	 * @return ontologieURI
	 */
	public Set<String> getOntologyURIString() {
		return ontologieURI;
	}
	/**
	 * This method returns a boolean if an ontology is inconsistent.
	 * @return ontologyInconsistent
	 */
	public boolean getOntologyConsistent() {
		return ontologyConsistent;
	}
	
	/**
	 * This sets the current concept.
	 * @param current currently selected class
	 */
	public void setCurrentConcept(NamedClass current) {
		this.currentConcept = current;
	}

	/**
	 * This method returns a set of individuals belonging to the
	 * currently selected class. 
	 * @return the individual
	 */
	public Set<Individual> getIndividual() {
		return individual;
	}
	
	/**
	 * This method sets the number of instances.
	 * @param i number of instances
	 */
	public void setInstancesCount(int i) {
		instancesCount = i;
	}
	
	public String getID() {
		return id;
	}
}


