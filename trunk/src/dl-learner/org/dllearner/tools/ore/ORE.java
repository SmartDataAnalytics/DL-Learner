package org.dllearner.tools.ore;

import java.io.File;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGReasoner;

public class ORE {
	
	private LearningAlgorithm la;
	private ReasoningService rs;
	private KnowledgeSource ks; 
	private PosNegDefinitionLP lp;
	private ComponentManager cm;
	SortedSet<Individual> posExamples;
	SortedSet<Individual> negExamples;
	Description concept;
	
	public ORE(){
		
		cm = ComponentManager.getInstance();
	
		
	}
	
	// step 1: detect knowledge sources
	
	public void setKnowledgeSource(File f){
	
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
		
		ReasonerComponent reasoner = cm.reasoner(
				DIGReasoner.class, ks);
		
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = cm.reasoningService(reasoner);
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
		lp = new PosNegDefinitionLP(rs,posExamples, negExamples);
		lp.init();
	}
	
	public void setLearningAlgorithm(){
		try {
			la = cm.learningAlgorithm(ROLearner.class, lp, rs);
		} catch (LearningProblemUnsupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//la = new ROLearner(lp, rs);
		
		Set<String> t = new TreeSet<String>();
		t.add(concept.toString());
		//cm.applyConfigEntry(la, "ignoredConcepts", t );
		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void setConcept(String conceptStr){
		try {
			concept = KBParser.parseConcept(conceptStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void startLearningThread(){
		Thread laThread = new Thread()
		{
			@Override
			public void run(){
				la.start();
				
			}
		};
		laThread.start();
	}

	public void start(){
		this.setPosNegExamples();
		this.setLearningProblem();
		this.setLearningAlgorithm();
		la.start();
		this.startLearningThread();
	}
	
	public Description getLearningResult(){
		return la.getBestSolution();
	}

		
	public static void main(String[] args){
		
		ORE test = new ORE();
		//File owlFile = new File("examples/family/father.owl");
		File owlFile = new File("src/dl-learner/org/dllearner/tools/ore/father.owl");
		
		test.setKnowledgeSource(owlFile);
	
		test.detectReasoner();
		ReasoningService rs = test.getReasoningService();
		System.err.println("Concepts :" + rs.getAtomicConcepts());
		
		
		
		test.setConcept("http://example.com/father#father");
		test.setPosNegExamples();
		System.out.println(test.posExamples);
		System.out.println(test.negExamples);
		test.setLearningProblem();
		test.setLearningAlgorithm();
		test.startLearningThread();
	}
	
	
}
