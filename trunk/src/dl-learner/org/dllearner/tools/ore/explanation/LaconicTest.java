package org.dllearner.tools.ore.explanation;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;

import org.dllearner.tools.ore.explanation.laconic.LaconicExplanationGenerator;
import org.mindswap.pellet.utils.Timer;
import org.mindswap.pellet.utils.Timers;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

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
				
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI
						.create(file));
				
				PelletReasoner reasoner = resonerFact.createReasoner(ontology);
				System.out.println(reasoner.getUnsatisfiableClasses());
				PelletExplanation exp = new PelletExplanation(ontology);
				
				System.out.println(exp.getUnsatisfiableExplanations(dataFactory.getOWLClass(
						IRI.create("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#KoalaWithPhD"))));
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
			
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI
					.create(file));
			
			PelletReasonerFactory reasonerFact = new PelletReasonerFactory();
			OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
			PelletReasoner reasoner = reasonerFact.createReasoner(ontology);
			reasoner.prepareReasoner();
			
			
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(
					manager, reasonerFact, ontology);
			
//			org.semanticweb.owl.explanation.api.ExplanationGenerator<OWLAxiom> copy = org.semanticweb.owl.explanation.api.
//			ExplanationManager.createLaconicExplanationGeneratorFactory(reasonerFact).createExplanationGenerator(ontology.getAxioms());
			
			
			Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			OWLSubClassOfAxiom unsatAxiom;
			Timers timers = new Timers();
			for (OWLClass unsat : unsatClasses) {
				unsatAxiom = dataFactory.getOWLSubClassOfAxiom(unsat, dataFactory
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
			OWLClass c = factory.getOWLClass(IRI.create("c"));
			OWLClass d = factory.getOWLClass(IRI.create("d"));
			OWLClass e = factory.getOWLClass(IRI.create("e"));
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(c, 
					factory.getOWLObjectIntersectionOf(d, factory.getOWLObjectComplementOf(d), e));
			OWLOntology ontology = manager.createOntology(Collections.singleton(axiom));
			OWLReasonerFactory resonerFact = new PelletReasonerFactory();
			OWLReasoner reasoner = resonerFact.createReasoner(ontology);
			
			OWLSubClassOfAxiom unsatAxiom = factory.getOWLSubClassOfAxiom(c, factory.getOWLNothing());
			
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(manager, 
					resonerFact, ontology);
			Set<Explanation> preciseJusts = expGen.getExplanations(unsatAxiom);
//			renderer.render(unsatAxiom, preciseJusts);
			renderer.endRendering();
		
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
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

			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI
					.create(file));
			
			OWLReasonerFactory resonerFact = new PelletReasonerFactory();
			OWLDataFactory dataFactory = manager.getOWLDataFactory();

			OWLReasoner reasoner = resonerFact.createReasoner(ontology);
			
			OWLSubClassOfAxiom axiom = dataFactory
					.getOWLSubClassOfAxiom(
							dataFactory
									.getOWLClass(IRI
											.create("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Employee")),
							dataFactory
									.getOWLClass(IRI
											.create("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person")));
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(
					manager, resonerFact, ontology);
			

			 
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
