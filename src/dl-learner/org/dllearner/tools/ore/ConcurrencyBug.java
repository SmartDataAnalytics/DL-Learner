package org.dllearner.tools.ore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.PelletReasoner;
import org.semanticweb.owl.model.OWLOntologyCreationException;

public class ConcurrencyBug {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws ComponentInitException 
	 * @throws URISyntaxException 
	 * @throws OWLOntologyCreationException 
	 * @throws LearningProblemUnsupportedException 
	 */
	public static void main(String[] args) throws MalformedURLException, ComponentInitException, OWLOntologyCreationException, URISyntaxException, LearningProblemUnsupportedException {
		File file = new File("examples/swore/swore.rdf");
		URL classToDescribe = new URL("http://ns.softwiki.de/req/CustomerRequirement");
		
		ComponentManager cm = ComponentManager.getInstance();
		OWLFile ks = cm.knowledgeSource(OWLFile.class);
		((OWLFile)ks).getConfigurator().setUrl(file.toURI().toURL());
		ks.init();
		
		PelletReasoner reasoner = cm.reasoner(PelletReasoner.class, ks);
		reasoner.init();
		reasoner.loadOntologies();
		reasoner.dematerialise();
		
		ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		lp.getConfigurator().setClassToDescribe(classToDescribe);
		lp.init();
		
		
		
		final CELOE la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
		la.getConfigurator().setMaxExecutionTimeInSeconds(8);
		la.getConfigurator().setUseNegation(false);
		la.getConfigurator().setNoisePercentage(0.05);
		la.getConfigurator().setMaxNrOfResults(10);
		la.init();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if(la.isRunning()){
					System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
				} else {
					cancel();
				}
				
			}
		}, 1000, 1000);
		la.start();
		

	}

}
