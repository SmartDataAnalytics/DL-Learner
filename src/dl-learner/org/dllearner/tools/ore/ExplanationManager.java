package org.dllearner.tools.ore;

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
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import uk.ac.manchester.cs.bhig.util.Tree;
import uk.ac.manchester.cs.owl.explanation.ordering.DefaultExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import com.clarkparsia.explanation.PelletExplanation;

public class ExplanationManager implements OWLOntologyChangeListener, ImpactManagerListener{

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
	private Set<OWLClass> unsatClasses;
	private Set<OWLClass> rootClasses;
	boolean ontologyChanged = true;
	
	
	
	private ExplanationManager(Reasoner reasoner) {
		regularExplanationCache = new HashMap<OWLClass, Set<Set<OWLAxiom>>>();
		laconicExplanationCache = new HashMap<OWLClass, Set<Set<OWLAxiom>>>();

		this.reasoner = reasoner;
		this.manager = reasoner.getManager();
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		
		manager.addOntologyChangeListener(this);
		manager.addOntologyChangeListener(reasoner);
		dataFactory = manager.getOWLDataFactory();
		ImpactManager.getImpactManager(reasoner).addListener(this);
		reasonerFactory = new PelletReasonerFactory();

		rootFinder = new RootFinder(manager, reasoner, reasonerFactory);

		regularExpGen = new PelletExplanation(manager, Collections
				.singleton(ontology));
		laconicExpGen = new LaconicExplanationGenerator(manager,
				reasonerFactory, Collections.singleton(ontology));

		rootClasses = new HashSet<OWLClass>();
		unsatClasses = new HashSet<OWLClass>();

	}
	
	public static synchronized ExplanationManager getExplanationManager(
			Reasoner reasoner) {
		if (instance == null) {
			instance = new ExplanationManager(reasoner);
		}
		return instance;
	}
	
	public static synchronized ExplanationManager getExplanationManager(){
	
		return instance;
	}
	
	public Set<OWLClass> getUnsatisfiableClasses(){
		computeRootUnsatisfiableClasses();
		return unsatClasses;
	}
	
	public Set<OWLClass> getRootUnsatisfiableClasses(){
		computeRootUnsatisfiableClasses();
		return rootClasses;
	}
	
		
	public void computeRootUnsatisfiableClasses(){
		if(ontologyChanged){
			rootClasses.clear();
			unsatClasses.clear();
			unsatClasses.addAll(reasoner.getInconsistentClasses());
			rootClasses.addAll(rootFinder.getRootClasses());
			ontologyChanged = false;
		}
		
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

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {System.out.println(changes);
		ontologyChanged = true;
		
		
		
	}
	
	public int getArity(OWLClass cl, OWLAxiom ax) {
		int arity = 0;
		Set<Set<OWLAxiom>> explanations = regularExplanationCache.get(cl);
		
		if(explanations != null){
			
			for (Set<OWLAxiom> explanation : explanations) {
				if (explanation.contains(ax)) {
					arity++;
				}
			}
		}
		return arity;
	}

	@Override
	public void axiomForImpactChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted() {
		reasoner.refresh();
		ontologyChanged = true;
		regularExplanationCache.clear();
		laconicExplanationCache.clear();
	}
	
	
}
