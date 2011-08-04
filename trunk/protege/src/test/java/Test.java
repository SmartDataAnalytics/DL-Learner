import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ProtegeReasoner;
import org.dllearner.tools.protege.LearningType;
import org.dllearner.tools.protege.StatusBar;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;


public class Test {
	
	private static final String[] components = { "org.dllearner.kb.OWLFile",
			"org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.FastInstanceChecker",
			"org.dllearner.reasoning.ProtegeReasoner",
			"org.dllearner.reasoning.FastRetrievalReasoner",
			"org.dllearner.algorithms.celoe.CELOE",
			"org.dllearner.learningproblems.PosNegLPStandard", "org.dllearner.learningproblems.ClassLearningProblem"};

	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws ComponentInitException 
	 * @throws LearningProblemUnsupportedException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, ComponentInitException, LearningProblemUnsupportedException, MalformedURLException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.loadOntology(IRI.create("http://dl-learner.svn.sourceforge.net/viewvc/dl-learner/trunk/examples/swore/swore.rdf"));
		OWLReasoner r = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		ComponentManager.setComponentClasses(components);
		ComponentManager cm = ComponentManager.getInstance();
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(ont);
		ks.init();
		
		ProtegeReasoner reasoner = cm.reasoner(ProtegeReasoner.class, ks);
		reasoner.setProgressMonitor(new StatusBar());
		reasoner.setOWLReasoner(r);
		reasoner.init();
		
		AbstractLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		cm.applyConfigEntry(lp, "classToDescribe", new URL("http://ns.softwiki.de/req/CustomerRequirement"));
			cm.applyConfigEntry(lp, "type", "equivalence");
		lp.init();
		
		
		
		AbstractCELA la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
		cm.applyConfigEntry(la, "useNegation", false);
		cm.applyConfigEntry(la, "noisePercentage", 0.9);
		cm.applyConfigEntry(la, "maxExecutionTimeInSeconds", 10);
		la.init();
		
		la.start();
		
		
	}

}
