package org.dllearner.tools.ore.explanation;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.dllearner.tools.ore.explanation.laconic.LaconicExplanationGenerator;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.Timer;
import org.mindswap.pellet.utils.Timers;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import com.clarkparsia.explanation.PelletExplanation;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

public class LaconicTest {
	
	
	
	public static void main(String[] args) {
	
//		test();
//		miniTest();
		miniEconomyTest();
//		universityTest();
	}
	

	public static void test(){
		String	file	= "file:/home/lorenz/neu.owl";
		
		
			
			try {
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
				PrintWriter pw = new PrintWriter(System.out);
				renderer.startRendering(pw);
				OWLDataFactory dataFactory = manager.getOWLDataFactory();
				PelletReasonerFactory resonerFact = new PelletReasonerFactory();
				
				OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI
						.create(file));
				
				Reasoner reasoner = resonerFact.createReasoner(manager);
				reasoner.loadOntologies(Collections.singleton(ontology));
				System.out.println(reasoner.getInconsistentClasses());
				PelletExplanation exp = new PelletExplanation(manager, Collections.singleton(ontology));
				
				System.out.println(exp.getUnsatisfiableExplanations(dataFactory.getOWLClass(
						URI.create("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#KoalaWithPhD"))));
				renderer.endRendering();
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void miniEconomyTest() {
		String	file	= "file:examples/ore/koala.owl";
		
		try {
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI
					.create(file));
			
			PelletReasonerFactory reasonerFact = new PelletReasonerFactory();
			OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
			Reasoner reasoner = reasonerFact.createReasoner(manager);
			reasoner.loadOntology(ontology);
			
			reasoner.classify();
			
			
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(
					manager, reasonerFact, Collections.singleton(ontology));
			
//			org.semanticweb.owl.explanation.api.ExplanationGenerator<OWLAxiom> copy = org.semanticweb.owl.explanation.api.
//			ExplanationManager.createLaconicExplanationGeneratorFactory(reasonerFact).createExplanationGenerator(ontology.getAxioms());
			
			
			Set<OWLClass> unsatClasses = reasoner.getInconsistentClasses();
			OWLSubClassAxiom unsatAxiom;
			Timers timers = new Timers();
			for (OWLClass unsat : unsatClasses) {
				unsatAxiom = dataFactory.getOWLSubClassAxiom(unsat, dataFactory
						.getOWLNothing());
				Timer t1 = timers.createTimer("t1");
				Timer t2 =timers.createTimer("t2");
				t1.start();
				Set<Explanation> explanations = expGen
						.getExplanations(unsatAxiom);
				t1.stop();
//				System.out.println(explanations);
				t2.start();
//				Set<org.semanticweb.owl.explanation.api.Explanation<OWLAxiom>> expl = copy.getExplanations(unsatAxiom);
				t2.stop();
//				System.out.println(expl);
				
				for(Timer timer : timers.getTimers()){
					System.out.println(timer.getTotal());
				}
			}


		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} 
	}
	
	public static void miniTest(){
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
			PrintWriter pw = new PrintWriter(System.out);
			renderer.startRendering(pw);
			OWLClass c = factory.getOWLClass(URI.create("c"));
			OWLClass d = factory.getOWLClass(URI.create("d"));
			OWLClass e = factory.getOWLClass(URI.create("e"));
			OWLAxiom axiom = factory.getOWLSubClassAxiom(c, 
					factory.getOWLObjectIntersectionOf(d, factory.getOWLObjectComplementOf(d), e));
			OWLOntology ontology = manager.createOntology(Collections.singleton(axiom));
			OWLReasonerFactory resonerFact = new PelletReasonerFactory();
			OWLReasoner reasoner = resonerFact.createReasoner(manager);
			reasoner.loadOntologies(Collections.singleton(ontology));
			
			OWLSubClassAxiom unsatAxiom = factory.getOWLSubClassAxiom(c, factory.getOWLNothing());
			
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(manager, resonerFact, Collections.singleton(ontology));
			Set<Explanation> preciseJusts = expGen.getExplanations(unsatAxiom);
//			renderer.render(unsatAxiom, preciseJusts);
			renderer.endRendering();
		
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExplanationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void universityTest(){
		String	file	= "file:examples/ore/university.owl";
		
		try {

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
			PrintWriter pw = new PrintWriter(System.out);
			renderer.startRendering(pw);

			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI
					.create(file));
			
			OWLReasonerFactory resonerFact = new PelletReasonerFactory();
			OWLDataFactory dataFactory = manager.getOWLDataFactory();

			OWLReasoner reasoner = resonerFact.createReasoner(manager);
			reasoner.loadOntologies(Collections.singleton(ontology));
			
			OWLSubClassAxiom axiom = dataFactory
					.getOWLSubClassAxiom(
							dataFactory
									.getOWLClass(URI
											.create("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Employee")),
							dataFactory
									.getOWLClass(URI
											.create("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person")));
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(
					manager, resonerFact, Collections.singleton(ontology));
			

			 
			 Set<Explanation> regularJusts = expGen.getRegularExplanations(axiom);
			 System.out.println("Regular explanations:");
//			 renderer.render(axiom, regularJusts);
			 
			Set<Explanation> preciseJusts = expGen.getExplanations(axiom);
			System.out.println("Precise explanations:");
//			renderer.render(axiom, preciseJusts);
			

			renderer.endRendering();

		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}
