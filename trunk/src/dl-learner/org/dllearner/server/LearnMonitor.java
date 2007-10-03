package org.dllearner.server;

import org.dllearner.Config;
import org.dllearner.Main;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ReasoningMethodUnsupportedException;

public class LearnMonitor extends Thread {

	
	private ClientState c;
	private boolean active = false;
	
	//private ROLearner ROL;
	
	public LearnMonitor(ClientState c){
		this.c=c;
	}
	
	
	public  void end(){
		
		System.out.println("trying to end");
		System.out.println("ROL2"+c.ROL);
		this.c.ROL.stop();
		c.setLastResult(c.ROL.getBestSolution().toString());
		c.setStatus("stopped");
	}
	
	public void learn(){
		
		this.start();
		
	}
	
	/**
	 * @override
	 */
	@Override
	public void run(){
		try{
		c.setStatus("still running");
		if(active);
		active=true;
		c.setStatus("running");
		
		//		 notwendige Vorverarbeitungsschritte für den Lernalgorithmus
		// - es müssen ein paar Konzepte, die ev. von Jena generiert wurden ignoriert
		//   werden
		// - die Subsumptionhierarchie muss erstellt werden
		// - die Subsumptionhierarchie wird verbessert um das Lernen effizienter zu machen
		Main.autoDetectConceptsAndRoles(c.getRs());
		c.getReasoner().prepareSubsumptionHierarchy();
		if (Config.Refinement.improveSubsumptionHierarchy) {
			try {
				c.getReasoner().getSubsumptionHierarchy().improveSubsumptionHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				// solange DIG-Reasoner eingestellt ist, schlägt diese Operation nie fehl
				e.printStackTrace();
			}
		}
		c.p("learning started");
		// LearningProblem learningProblem = new LearningProblem(c.getRs(), c.getPosExamples(), c.getNegExamples());
		// erstmal wird nur der Refinement-Learner als Web-Service angeboten
		//System.out.println("aaaa");
		// c.ROL = new ROLearner(learningProblem);
		
		c.ROL.start();
		//new ROLearner();
		//c.p(("ROL1"+ROL));
		
		
		
		
		 
		
		//c.setLastResult(c.ROL.getBestSolution().toString());
		c.setLastResult(c.ROL.getBestSolution().toString());
		c.setStatus("finished");
		
		}catch (Exception e) {e.printStackTrace();}
		finally{active=false;};
	}
	
}
