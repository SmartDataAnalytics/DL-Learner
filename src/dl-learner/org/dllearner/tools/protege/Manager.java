package org.dllearner.tools.protege;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.ProtegeReasoner;
import org.dllearner.tools.ore.LearningManager.LearningType;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

public class Manager implements OWLModelManagerListener, OWLSelectionModelListener{
	
	private static Manager instance;
	
	private OWLEditorKit editorKit;
	private ReasonerProgressMonitor progressMonitor;
	
	private boolean reinitNecessary = true;
	
	private ComponentManager cm;
	private LearningProblem lp;
	private LearningAlgorithm la;
	private ProtegeReasoner reasoner;
	private KnowledgeSource ks;
	
	private LearningType learningType;
	private int maxExecutionTimeInSeconds;
	private double noisePercentage;
	private double threshold;
	private int maxNrOfResults;
	private boolean useAllConstructor;
	private boolean useExistsConstructor;
	private boolean useHasValueConstructor;
	private boolean useNegation;
	private boolean useCardinalityRestrictions;
	private int cardinalityLimit;
	
	public static synchronized Manager getInstance(OWLEditorKit editorKit){
		if(instance == null){
			instance = new Manager(editorKit);
		}
		return instance;
	}
	
	public static synchronized Manager getInstance(){
		return instance;
	}
	
	private Manager(OWLEditorKit editorKit){
		this.editorKit = editorKit;
		cm = ComponentManager.getInstance();
	}
	
	public void setOWLEditorKit(OWLEditorKit editorKit){
		this.editorKit = editorKit;
	}

	public boolean isReinitNecessary(){
		return reinitNecessary;
	}
	
	public void init(){
		initKnowledgeSource();
		if(reinitNecessary){
			initReasoner();
		}
		initLearningProblem();
		initLearningAlgorithm();
		reinitNecessary = false;
	}
	
	public void initLearningAlgorithm(){
		try {
			la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			cm.applyConfigEntry(la, "useAllConstructor", useAllConstructor);
			cm.applyConfigEntry(la, "useExistsConstructor", useExistsConstructor);
			cm.applyConfigEntry(la, "useHasValueConstructor", useHasValueConstructor);
			cm.applyConfigEntry(la, "useNegation", useNegation);
			cm.applyConfigEntry(la, "useCardinalityRestrictions", useCardinalityRestrictions);
			if(useCardinalityRestrictions) {
				cm.applyConfigEntry(la, "cardinalityLimit", cardinalityLimit);
			}
			cm.applyConfigEntry(la, "noisePercentage", noisePercentage);
			cm.applyConfigEntry(la, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
			
			la.init();
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initLearningProblem(){
		lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		try {
			cm.applyConfigEntry(lp, "classToDescribe", editorKit.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass().getIRI().toURI().toURL());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		if (learningType == LearningType.EQUIVALENT) {
			cm.applyConfigEntry(lp, "type", "equivalence");
		} else if(learningType == LearningType.SUPER){
			cm.applyConfigEntry(lp, "type", "superClass");
		}
		try {
			lp.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}
	
	public void initKnowledgeSource(){
		ks = new OWLAPIOntology(editorKit.getOWLModelManager().getActiveOntology());
		try {
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}
	
	public void initReasoner(){
		reasoner = cm.reasoner(ProtegeReasoner.class, ks);
		reasoner.setOWLReasoner(editorKit.getOWLModelManager().getReasoner());
		reasoner.setProgressMonitor(progressMonitor);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}
	
	public void initReasonerAsynchronously(){
		reasoner = cm.reasoner(ProtegeReasoner.class, ks);
		reasoner.setOWLReasoner(editorKit.getOWLModelManager().getReasoner());
		reasoner.setProgressMonitor(progressMonitor);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					reasoner.init();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	public void addAxiom(OWLClassExpression description){
		OWLClass selectedClass = editorKit.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
		if(learningType == LearningType.EQUIVALENT){
			addEquivalentClassesAxiom(selectedClass, description);
		} else {
			addSuperClassAxiom(selectedClass, description);
		}
	}
	
	private void addSuperClassAxiom(OWLClassExpression subClass, OWLClassExpression superClass){
		OWLOntology ontology = editorKit.getOWLModelManager().getActiveOntology();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLAxiom subClassAxiom = manager.getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
		OWLOntologyChange change = new AddAxiom(ontology, subClassAxiom);
		manager.applyChange(change);
	}
	
	private void addEquivalentClassesAxiom(OWLClassExpression equiv1, OWLClassExpression equiv2){
		OWLOntology ontology = editorKit.getOWLModelManager().getActiveOntology();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLAxiom equivalentClassesAxiom = manager.getOWLDataFactory().getOWLEquivalentClassesAxiom(equiv1, equiv2);
		OWLOntologyChange change = new AddAxiom(ontology, equivalentClassesAxiom);
		manager.applyChange(change);
	}
	
	public void setProgressMonitor(ReasonerProgressMonitor progressMonitor){
		this.progressMonitor = progressMonitor;
	}
	
	public void startLearning(){
		la.start();
	}
	
	public void stopLearning(){
		la.stop();
	}
	
	public boolean isLearning(){
		return la != null && la.isRunning();
	}
	
	public LearningType getLearningType() {
		return learningType;
	}

	public void setLearningType(LearningType learningType) {
		this.learningType = learningType;
	}

	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getMaxNrOfResults() {
		return maxNrOfResults;
	}

	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}

	public void setUseAllConstructor(boolean useAllConstructor) {
		this.useAllConstructor = useAllConstructor;
	}

	public void setUseExistsConstructor(boolean useExistsConstructor) {
		this.useExistsConstructor = useExistsConstructor;
	}

	public void setUseHasValueConstructor(boolean useHasValueConstructor) {
		this.useHasValueConstructor = useHasValueConstructor;
	}

	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}

	public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
		this.useCardinalityRestrictions = useCardinalityRestrictions;
	}

	public void setCardinalityLimit(int cardinalityLimit) {
		this.cardinalityLimit = cardinalityLimit;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized List<EvaluatedDescriptionClass> getCurrentlyLearnedDescriptions() {
		List<EvaluatedDescriptionClass> result;
		if (la != null) {
			result = Collections.unmodifiableList((List<EvaluatedDescriptionClass>) la
					.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults, threshold, true));
		} else {
			result = Collections.emptyList();
		}
		return result;
	}
	
	public int getMinimumHorizontalExpansion(){
		return ((CELOE)la).getMinimumHorizontalExpansion();
	}
	
	public int getMaximumHorizontalExpansion(){
		return ((CELOE)la).getMaximumHorizontalExpansion();
	}
	
	public boolean isConsistent(){
		return reasoner.isConsistent();
	}
	
	public SortedSet<Individual> getIndividuals(){
		OWLClass selectedClass = editorKit.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
		return reasoner.getIndividuals(OWLAPIConverter.convertClass(selectedClass));
	}
	
	public boolean canLearn(){
		OWLClass selectedClass = editorKit.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
		boolean canLearn = reasoner.getIndividuals(OWLAPIConverter.convertClass(selectedClass)).size() > 0;
		return canLearn;
	}
	
	public String getRendering(Description desc){
		String rendering = editorKit.getModelManager().getRendering(
				OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc));
		return rendering;
	}
	
	public String getRendering(OWLClass cl){
		String rendering = editorKit.getModelManager().getRendering(cl);
		return rendering;
	}
	
	public String getRendering(Individual ind){
		String rendering = editorKit.getModelManager().getRendering(
				OWLAPIConverter.getOWLAPIIndividual(ind));
		return rendering;
	}
	
	public ProtegeReasoner getReasoner(){
		return reasoner;
	}
	
	public OWLOntology getActiveOntology(){
		return editorKit.getOWLModelManager().getActiveOntology();
	}
	
	public OWLClass getCurrentlySelectedClass(){
		return editorKit.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
	}
	
	public String getCurrentlySelectedClassRendered(){
		return getRendering(getCurrentlySelectedClass());
	}
	
	@Override
	public void handleChange(OWLModelManagerChangeEvent event) {
		if(event.isType(EventType.REASONER_CHANGED) || event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)){
			reinitNecessary = true;
		}
	}

	@Override
	public void selectionChanged() throws Exception {
	}

}
