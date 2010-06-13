package org.dllearner.tools.protege;

import java.net.MalformedURLException;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ProtegeReasoner;
import org.dllearner.tools.ore.LearningManager.LearningType;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;

public class Manager implements OWLModelManagerListener, OWLSelectionModelListener{
	
	private static Manager instance;
	
	private OWLEditorKit editorKit;
	
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
	private int minInstanceCount;
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
	
	private Manager(OWLEditorKit editorKit){
		this.editorKit = editorKit;
		cm = ComponentManager.getInstance();
	}

	public boolean isReinitNecessary(){
		return reinitNecessary;
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
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
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

	public double getNoisePercentage() {
		return noisePercentage;
	}

	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public double getThreshold() {
		return threshold;
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

	public int getMinInstanceCount() {
		return minInstanceCount;
	}

	public void setMinInstanceCount(int minInstanceCount) {
		this.minInstanceCount = minInstanceCount;
	}

	public boolean isUseAllConstructor() {
		return useAllConstructor;
	}

	public void setUseAllConstructor(boolean useAllConstructor) {
		this.useAllConstructor = useAllConstructor;
	}

	public boolean isUseExistsConstructor() {
		return useExistsConstructor;
	}

	public void setUseExistsConstructor(boolean useExistsConstructor) {
		this.useExistsConstructor = useExistsConstructor;
	}

	public boolean isUseHasValueConstructor() {
		return useHasValueConstructor;
	}

	public void setUseHasValueConstructor(boolean useHasValueConstructor) {
		this.useHasValueConstructor = useHasValueConstructor;
	}

	public boolean isUseNegation() {
		return useNegation;
	}

	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}

	public boolean isUseCardinalityRestrictions() {
		return useCardinalityRestrictions;
	}

	public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
		this.useCardinalityRestrictions = useCardinalityRestrictions;
	}

	public int getCardinalityLimit() {
		return cardinalityLimit;
	}

	public void setCardinalityLimit(int cardinalityLimit) {
		this.cardinalityLimit = cardinalityLimit;
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
