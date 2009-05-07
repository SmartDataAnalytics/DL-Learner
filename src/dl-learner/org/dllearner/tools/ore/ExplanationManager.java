package org.dllearner.tools.ore;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.explanation.ExplanationException;
import org.dllearner.tools.ore.explanation.LaconicExplanationGenerator;
import org.dllearner.tools.ore.explanation.RootFinder;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import uk.ac.manchester.cs.bhig.util.Tree;
import uk.ac.manchester.cs.owl.explanation.ordering.DefaultExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import com.clarkparsia.explanation.PelletExplanation;

public class ExplanationManager {

	private static ExplanationManager instance;
	
	private OWLOntologyManager manager;
	private OWLDataFactory dataFactory;
	private PelletReasonerFactory reasonerFactory;
	private OWLOntology ontology;
	private Reasoner reasoner;
	private PelletExplanation regularExpGen;
	private LaconicExplanationGenerator laconicExpGen;
	private RootFinder rootFinder;
	private Map<OWLClass, Set<Set<OWLAxiom>>> regularExplanationCache;
	private Map<OWLClass, Set<Set<OWLAxiom>>> laconicExplanationCache;
	
	
	private ExplanationManager(String ontPath){
		regularExplanationCache = new HashMap<OWLClass, Set<Set<OWLAxiom>>>();
		laconicExplanationCache = new HashMap<OWLClass, Set<Set<OWLAxiom>>>();
		
		try {
			manager = OWLManager.createOWLOntologyManager();
			dataFactory = manager.getOWLDataFactory();
			ontology = manager.loadOntology(URI.create(ontPath));
			reasonerFactory = new PelletReasonerFactory();
			reasoner = reasonerFactory.createReasoner(manager);
			reasoner.loadOntology(ontology);
			reasoner.classify();
			rootFinder = new RootFinder(manager, reasoner, reasonerFactory);
			
			regularExpGen = new PelletExplanation(manager, Collections.singleton(ontology));
			laconicExpGen = new LaconicExplanationGenerator(manager, reasonerFactory, Collections.singleton(ontology));
		} catch (OWLOntologyCreationException e) {
			
			e.printStackTrace();
		}
	}
	
	public static synchronized ExplanationManager getExplanationManager(String ontPath){
		if(instance == null){
			instance = new ExplanationManager(ontPath);
		}
		return instance;
	}
	
	public Set<OWLClass> getUnsatisfiableClasses(){
		return reasoner.getInconsistentClasses();
	}
	
		
	public Set<OWLClass> getRootUnsatisfiableClasses(){
		return rootFinder.getRootClasses();
	}
	
	public Set<Set<OWLAxiom>> getUnsatisfiableExplanations(OWLClass unsat){
		Set<Set<OWLAxiom>> explanations = regularExplanationCache.get(unsat);
		if(explanations == null){
			explanations = regularExpGen.getUnsatisfiableExplanations(unsat);
			regularExplanationCache.put(unsat, explanations);
		} 
		
		return explanations;
	}
	
	public Set<Set<OWLAxiom>> getLaconicUnsatisfiableExplanations(OWLClass unsat){
		Set<Set<OWLAxiom>> explanations = laconicExplanationCache.get(unsat);
		OWLSubClassAxiom unsatAxiom;
		if(explanations == null){
			unsatAxiom = dataFactory.getOWLSubClassAxiom(unsat, dataFactory.getOWLNothing());
			try {
				explanations = laconicExpGen.getExplanations(unsatAxiom);
			} catch (ExplanationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			laconicExplanationCache.put(unsat, explanations);
		} 
		return explanations;
		
	}
	
	public Set<List<OWLAxiom>> getOrderedUnsatisfiableExplanations(OWLClass unsat){
		
		return getOrderedExplanations(dataFactory.getOWLSubClassAxiom(unsat, dataFactory.getOWLNothing()),
				getUnsatisfiableExplanations(unsat));
		
	}
	
	public Set<List<OWLAxiom>> getOrderedLaconicUnsatisfiableExplanations(OWLClass unsat){
		
		return getOrderedExplanations(dataFactory.getOWLSubClassAxiom(unsat, dataFactory.getOWLNothing()),
				getLaconicUnsatisfiableExplanations(unsat));
		
	}
	
	public ArrayList<OWLAxiom> getTree2List(Tree<OWLAxiom> tree){
		ArrayList<OWLAxiom> ordering = new ArrayList<OWLAxiom>();
		ordering.add((OWLAxiom)tree.getUserObject());
		for(Tree<OWLAxiom> child : tree.getChildren()){
			ordering.addAll(getTree2List(child));
		}
		return ordering;
	}
	
	public Set<List<OWLAxiom>> getOrderedExplanations(OWLAxiom entailment, Set<Set<OWLAxiom>> explanations){
		DefaultExplanationOrderer orderer = new DefaultExplanationOrderer();
		Set<List<OWLAxiom>> orderedExplanations = new HashSet<List<OWLAxiom>>();
		ArrayList<OWLAxiom> ordering;
		for(Set<OWLAxiom> explanation : explanations){
			ordering = new ArrayList<OWLAxiom>();
			ExplanationTree tree = orderer.getOrderedExplanation(entailment, explanation);
			
//			ordering.add(tree.getUserObject());
			for(Tree<OWLAxiom> child : tree.getChildren()){
				ordering.addAll(getTree2List(child));
			}
			orderedExplanations.add(ordering);
		}
		return orderedExplanations;
	}
}
