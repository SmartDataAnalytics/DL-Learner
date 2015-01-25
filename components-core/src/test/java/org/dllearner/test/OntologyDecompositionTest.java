/**
 * 
 */
package org.dllearner.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposerOWLAPITOOLS;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;
import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * Test class to play around with modules and Atomic Decomposition(AD), a method to 
 * partition an ontology into pieces called atoms. An atom is a set of axioms that
 * appears in every module all together (or none of them). They could be viewed
 * as a smallest pieces of modules.
 * @author Lorenz Buehmann
 *
 */
public class OntologyDecompositionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
//		renderer.setUseWrapping(false);
//		renderer.setUseTabbing(false);
		ToStringRenderer.getInstance().setRenderer(renderer);
		
		String ontologyURL = "../examples/swore/swore.rdf";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = man.getOWLDataFactory();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(ontologyURL));
		OWLReasonerFactory reasonerFactory = new JFactFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		
		SyntacticLocalityModuleExtractor moduleExtractor = new SyntacticLocalityModuleExtractor(
				man, ontology, (Set) ontology.<OWLAxiom>getLogicalAxioms(), ModuleType.BOT);
		AtomicDecomposition ad = new AtomicDecomposerOWLAPITOOLS(ontology);
		Set<Atom> atoms = ad.getAtoms();
		for (Atom atom : atoms) {
			System.out.println(atom);
		}
		
		// the target class of the learning problem
		OWLClass customerRequirement  = dataFactory.getOWLClass(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));
		
		// get all instances which are the positive examples
		Set<OWLNamedIndividual> instances = reasoner.getInstances(customerRequirement, false).getFlattened();
		
		// get the signature of the examples
		Set<OWLEntity> signature = new HashSet<>();
		for (OWLNamedIndividual individual : instances) {
			signature.add(individual);
		}
		
		// extract a locality based module
		Set<OWLAxiom> module = moduleExtractor.extract(signature);
		System.out.println(module.size() + "/" + ontology.getLogicalAxiomCount());
		
		ontology = man.createOntology(module);
		
		System.out.println(ontology.getClassesInSignature());
		
		OWLAPIReasoner rc = new OWLAPIReasoner(reasoner);
		rc.setReasonerImplementation(ReasonerImplementation.PELLET);
		rc.init();
		
		ClosedWorldReasoner cwr = new ClosedWorldReasoner();
		cwr.setReasonerComponent(rc);
		cwr.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(cwr);
		lp.setClassToDescribe(customerRequirement);
		lp.init();
		
		CELOE la = new CELOE(lp, cwr);
		la.init();
		
		la.start();
	}

}