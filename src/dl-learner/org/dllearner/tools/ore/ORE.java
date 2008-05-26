package org.dllearner.tools.ore;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.reasoning.OWLAPIReasoner;

public class ORE {
	
	private LearningAlgorithm la;
	private ReasoningService rs;
	private KnowledgeSource ks; 
	private PosNegDefinitionLP lp;
	private ComponentManager cm;
	OWLAPIReasoner reasoner;
	SortedSet<Individual> posExamples;
	SortedSet<Individual> negExamples;
	NamedClass concept;
	Description conceptToAdd;
	OntologyModifierOWLAPI modi;
	Set<NamedClass> allAtomicConcepts;
	
	
	public ORE() {

		cm = ComponentManager.getInstance();

	}
	
	// step 1: detect knowledge sources
	
	public void setKnowledgeSource(File f) {

		Class<OWLFile> owl = OWLFile.class;
		ks = cm.knowledgeSource(owl);

		cm.applyConfigEntry(ks, "url", f.toURI().toString());

		try {
			ks.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	public void detectReasoner(){
		
		reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		reasoner.init();

		rs = cm.reasoningService(reasoner);
		modi = new OntologyModifierOWLAPI(reasoner);
	}
	
	public ReasoningService getReasoningService(){
		return rs;
	}
	
	public SortedSet<Individual> getPosExamples(){
		return posExamples;
	}
	
	public void setPosNegExamples(){
		posExamples = rs.retrieval(concept);
		negExamples = rs.getIndividuals();
		
		for (Individual rem_pos : posExamples)
			negExamples.remove(rem_pos);
	}
	
	public SortedSet<Individual> getNegExamples(){
		return negExamples;
	}
	
	public void setLearningProblem(){
		lp = new PosNegDefinitionLP(rs, posExamples, negExamples);
		lp.init();
	}
	
	public void setLearningAlgorithm(){
		try {
			la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);
		} catch (LearningProblemUnsupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//la = new ROLearner(lp, rs);
		
		Set<String> t = new TreeSet<String>();
		t.add(concept.getName());
		cm.applyConfigEntry(la, "ignoredConcepts", t );
		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setConcept(NamedClass concept){
		this.concept = concept;
	}

	
	public LearningAlgorithm start(){
		this.setPosNegExamples();
		this.setLearningProblem();
		this.setLearningAlgorithm();
		la.start();
		
		return la;
		
	}
	
	public Description getLearningResult(){
		return la.getBestSolution();
	}
	
	public List<Description> getLearningResults(int anzahl){
		return la.getBestSolutions(anzahl);
	}
	
	public BigDecimal getCorrectness(Description d){
		int numberPosExamples = 0;
		int numberNegExamples = 0;
		double result_tmp = 0.0f;
		
		for(Individual ind : posExamples){
			if(rs.instanceCheck(d, ind))
				numberPosExamples++;
		}
		for(Individual ind : negExamples){
			if(!rs.instanceCheck(d, ind))
				numberNegExamples++;
		}
		
		result_tmp = ((float)(numberPosExamples) + (float)(numberNegExamples))/((float)(posExamples.size())+(float)(negExamples.size())) * 100;
		BigDecimal result = new BigDecimal( result_tmp );
		result = result.setScale( 2, BigDecimal.ROUND_HALF_UP );
		return result;	
		
		
	}
	
	public HashSet<Individual> getNegFailureExamples(){
		HashSet<Individual> negFailureExamples = new HashSet<Individual>() ;
		
		for(Individual ind : negExamples){
			if(rs.instanceCheck(conceptToAdd, ind))
				negFailureExamples.add(ind);
				
		}
		
		return negFailureExamples;
	}
	
	public SortedSet<Individual> getPosFailureExamples(){
		
		SortedSet<Individual> posFailureExamples = null ;
		
		for(Individual ind : posExamples){
			if(!rs.instanceCheck(conceptToAdd, ind))
				posFailureExamples.add(ind);
				
		}
		
		return posFailureExamples;
	}


	
		
	

	public Description getConceptToAdd() {
		return conceptToAdd;
	}

	public void setConceptToAdd(Description conceptToAdd) {
		this.conceptToAdd = conceptToAdd;
	}

	public LearningAlgorithm getLa() {
		return la;
	}

	public OntologyModifierOWLAPI getModi() {
		return modi;
	}

	public NamedClass getConcept() {
		return concept;
	}

	public void setAllAtomicConcepts(Set<NamedClass> allAtomicConcepts) {
		this.allAtomicConcepts = allAtomicConcepts;
	}
	
	public static void main(String[] args){
		
//		ORE test = new ORE();
//		
//		File owlFile = new File("src/dl-learner/org/dllearner/tools/ore/father.owl");
//		
//		test.setKnowledgeSource(owlFile);
//	
//		test.detectReasoner();
//		ReasoningService rs = test.getReasoningService();
//		System.err.println("Concepts :" + rs.getAtomicConcepts());
//		
//		
//		test.setConcept(new NamedClass("http://example.com/father#father"));
//		test.setPosNegExamples();
//		System.out.println(test.posExamples);
//		System.out.println(test.negExamples);
//		test.setLearningProblem();
//		test.setLearningAlgorithm();
//		test.start();
	}
	
	
}
