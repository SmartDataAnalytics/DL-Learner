package org.dllearner.tools.ore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
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
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

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
		
		String NS = "http://ns.softwiki.de/req/";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass customerRequirement = factory.getOWLClass(URI.create(NS +"CustomerRequirement"));
		OWLClass comment = factory.getOWLClass(URI.create(NS + "Comment"));
		OWLObjectProperty idDefinedBy = factory.getOWLObjectProperty(URI.create(NS + "isDefinedBy"));
		OWLObjectProperty defines = factory.getOWLObjectProperty(URI.create(NS + "defines"));
		
		OWLObjectSomeRestriction some = factory.getOWLObjectSomeRestriction(defines, comment);
		OWLObjectAllRestriction all = factory.getOWLObjectAllRestriction(idDefinedBy, some);
		OWLSubClassAxiom sub = factory.getOWLSubClassAxiom(customerRequirement, all);
		
//		System.out.println(reasoner.remainsSatisfiableImpl(sub));
		
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
