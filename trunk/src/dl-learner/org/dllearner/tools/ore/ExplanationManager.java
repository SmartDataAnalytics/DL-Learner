package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.explanation.CachedExplanationGenerator;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.explanation.RootFinder;
import org.mindswap.pellet.owlapi.Reasoner;
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

public class ExplanationManager implements OWLOntologyChangeListener, RepairManagerListener, OREManagerListener{

	private static ExplanationManager instance;
	
	private OWLOntologyManager manager;
	private OWLDataFactory dataFactory;
	private OWLOntology ontology;
	private Reasoner reasoner;

	private RootFinder rootFinder;

	private boolean ontologyChanged = true;

	private boolean isComputeAllExplanations = false;
	private int maxExplantionCount = 1;
	private boolean allExplanationWarningChecked = false;
	
	private Map<Explanation, List<Map<OWLAxiom, Integer>>> orderingMap;
	private DefaultExplanationOrderer explanationOrderer;
	
	private List<ExplanationManagerListener> listeners;
	
	
	
	private CachedExplanationGenerator gen;
	
	
	private ExplanationManager(OREManager oreMan) {
		OREManager.getInstance().addListener(this);
		this.reasoner = oreMan.getPelletReasoner().getReasoner();
		this.manager = reasoner.getManager();
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		
		manager.addOntologyChangeListener(this);
		dataFactory = manager.getOWLDataFactory();
		RepairManager.getRepairManager(oreMan).addListener(this);
		
		explanationOrderer = new DefaultExplanationOrderer();
		orderingMap = new HashMap<Explanation, List<Map<OWLAxiom, Integer>>>();

		rootFinder = new RootFinder();

		listeners = new ArrayList<ExplanationManagerListener>();
		
		gen = new CachedExplanationGenerator(ontology, reasoner);

	}
	
	public ExplanationManager() {
		// TODO Auto-generated constructor stub
	}

	public static synchronized ExplanationManager getInstance(
			OREManager oreMan) {
		if (instance == null) {
			instance = new ExplanationManager(oreMan);
		}
		return instance;
	}
	
	public static synchronized ExplanationManager getInstance(){
		if (instance == null) {
			instance = new ExplanationManager();
		}
		return instance;
	}
	
	public Set<OWLClass> getDerivedClasses(){
		return rootFinder.getDerivedClasses();
	}
	
	public Set<OWLClass> getRootUnsatisfiableClasses(){
		return rootFinder.getRootClasses();
	}
	
	public Set<Explanation> getUnsatisfiableExplanations(OWLClass unsat) {

		OWLSubClassAxiom entailment = dataFactory.getOWLSubClassAxiom(unsat,
				dataFactory.getOWLNothing());

		Set<Explanation> explanations;
		if (isComputeAllExplanations) {
			explanations = gen.getExplanations(entailment);
		} else {
			explanations = gen.getExplanations(entailment, maxExplantionCount);
		}

		return explanations;
	}
	
	public Set<Explanation> getInconsistencyExplanations(){
		OWLSubClassAxiom entailment = dataFactory.getOWLSubClassAxiom(dataFactory.getOWLThing(),
				dataFactory.getOWLNothing());

		Set<Explanation> explanations;
		if (isComputeAllExplanations) {
			explanations = gen.getExplanations(entailment);
		} else {
			explanations = gen.getExplanations(entailment, maxExplantionCount);
		}

		return explanations;
	}
	
	public List<Map<OWLAxiom, Integer>> getOrdering(Explanation exp){
		List<Map<OWLAxiom, Integer>> orderedAxioms = orderingMap.get(exp);
		if(orderedAxioms == null){
			orderedAxioms = new ArrayList<Map<OWLAxiom, Integer>>(exp.getAxioms().size());
			orderedAxioms.addAll(orderExplanation(exp));
			orderingMap.put(exp, orderedAxioms);
		}
		return orderedAxioms;
	}
	
	private List<Map<OWLAxiom, Integer>> orderExplanation(Explanation exp){
		explanationOrderer = new DefaultExplanationOrderer();
		List<Map<OWLAxiom, Integer>> ordering = new ArrayList<Map<OWLAxiom, Integer>>(exp.getAxioms().size());
		ExplanationTree tree = explanationOrderer.getOrderedExplanation(exp.getEntailment(), exp.getAxioms());
		for(Tree<OWLAxiom> child : tree.getChildren()){
			ordering.addAll(getTree2List(child));
		}
		return ordering;
	}
	
	
	
	private ArrayList<Map<OWLAxiom, Integer>> getTree2List(Tree<OWLAxiom> tree){
		ArrayList<Map<OWLAxiom, Integer>> ordering = new ArrayList<Map<OWLAxiom, Integer>>();
		Map<OWLAxiom, Integer> axiom2Depth = new HashMap<OWLAxiom, Integer>();
		axiom2Depth.put((OWLAxiom)tree.getUserObject(), Integer.valueOf(tree.getUserObjectPathToRoot().size()));
		ordering.add(axiom2Depth);
		for(Tree<OWLAxiom> child : tree.getChildren()){
			ordering.addAll(getTree2List(child));
		}
		return ordering;
	}

	
	
	public int getArity(OWLClass cl, OWLAxiom ax) {
		int arity = 0;
		
		Set<Explanation> explanations = gen.getExplanations(dataFactory.getOWLSubClassAxiom(cl, dataFactory.getOWLNothing()));
		
		if(explanations != null){
			
			for (Explanation explanation : explanations) {
				if (explanation.getAxioms().contains(ax)) {
					arity++;
				}
			}
		}
		return arity;
	}
	
	public void setLaconicMode(boolean laconic){
		gen.setComputeLaconicExplanations(laconic);
		fireExplanationTypeChanged();
		
	}
	
	public boolean isLaconicMode(){
		return gen.isLaconicMode();
	}
	
	public void setComputeAllExplanationsMode(boolean value){
		isComputeAllExplanations = value;
		fireExplanationLimitChanged();
	}

	public boolean isComputeAllExplanationsMode(){
		return isComputeAllExplanations;
	}
	
	public void setMaxExplantionCount(int limit){
		maxExplantionCount = limit;
		fireExplanationLimitChanged();
	}
	
	public int getMaxExplantionCount(){
		return maxExplantionCount;
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		reasoner.refresh();
		ontologyChanged = true;
	}

	@Override
	public void repairPlanChanged() {
		// TODO Auto-generated method stub
		
	}
	
	public void addListener(ExplanationManagerListener l){
		listeners.add(l);
	}
	
	public void removeListener(ExplanationManagerListener l){
		listeners.remove(l);
	}
	
	public void fireExplanationLimitChanged(){
		for(ExplanationManagerListener listener : listeners){
			listener.explanationLimitChanged();
		}
	}
	
	public void fireExplanationTypeChanged(){
		for(ExplanationManagerListener listener : listeners){
			listener.explanationTypeChanged();
		}
	}
	
	public void setAllExplanationWarningChecked(){
		allExplanationWarningChecked = true;
	}
	
	public boolean isAllExplanationWarningChecked(){
		return allExplanationWarningChecked;
	}
	
	public Set<OWLAxiom> getSourceAxioms(OWLAxiom ax){
		return gen.getSourceAxioms(ax);
	}
	
	public Set<OWLAxiom> getRemainingAxioms(OWLAxiom source, OWLAxiom part){
		return gen.getRemainingAxioms(source, part);
	}
	
	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		ontologyChanged = true;
	}

	@Override
	public void activeOntologyChanged() {
		ontology = OREManager.getInstance().getPelletReasoner().getOWLAPIOntologies();
		reasoner = OREManager.getInstance().getPelletReasoner().getReasoner();
		gen = new CachedExplanationGenerator(ontology, reasoner);
	}
	
}
