package org.dllearner.tools.ore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.PelletReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import com.clarkparsia.modularity.ModularityUtils;

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
		File file = new File("examples/family-benchmark/family-benchmark_rich_background.owl");
		IRI classToDescribe = IRI.create("http://www.benchmark.org/family#Father");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass father = factory.getOWLClass(classToDescribe);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		for(OWLClassExpression d : father.getSuperClasses(ontology)){
			signature.addAll(d.getSignature());
		}
		signature.addAll(father.getSignature());
		
		OWLOntology module = manager.createOntology(ModularityUtils.extractModule(ontology, signature, ModuleType.TOP_OF_BOT));
		ComponentManager cm = ComponentManager.getInstance();
		OWLAPIOntology ont = new OWLAPIOntology(module);
		ont.init();
		
		OWLFile ks = cm.knowledgeSource(OWLFile.class);
		((OWLFile)ks).getConfigurator().setUrl(file.toURI().toURL());
		ks.init();
		
		PelletReasoner reasoner = cm.reasoner(PelletReasoner.class, ont);
		reasoner.init();
		reasoner.dematerialise();
		
		ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		lp.getConfigurator().setClassToDescribe(new URL(classToDescribe.toString()));
		lp.init();
		
		
		
		final CELOE la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
		la.getConfigurator().setMaxExecutionTimeInSeconds(8);
		la.getConfigurator().setUseNegation(false);
		la.getConfigurator().setNoisePercentage(0.05);
		la.getConfigurator().setMaxNrOfResults(10);
		la.init();
		
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//			
//			@Override
//			public void run() {
//				if(la.isRunning()){
//					System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
//				} else {
//					cancel();
//				}
//				
//			}
//		}, 1000, 1000);
		la.start();
		

	}

}
