package org.dllearner.tools.ore.explanation;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.tools.ore.explanation.laconic.LaconicExplanationGenerator;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.progress.SwingProgressMonitor;
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
		miniTest();
//		miniEconomyTest();
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
				
				Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
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
			} catch (OWLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void miniEconomyTest() {
		String	file	= "file:examples/ore/miniEconomy.owl";
		
		try {
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
			PrintWriter pw = new PrintWriter(System.out);
			renderer.startRendering(pw);
			
			

			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI
					.create(file));
			Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
			ontologies.add(ontology);
			PelletReasonerFactory resonerFact = new PelletReasonerFactory();
			OWLDataFactory dataFactory = manager.getOWLDataFactory();
			
			////////////////////HermiT test
//			HermiTReasonerFactory f = new HermiTReasonerFactory();
//			HermitReasoner re = (HermitReasoner) f.createReasoner(manager);
//			re.loadOntologies(ontologies);
//			Timer t1 = new Timer("classifying");
//			t1.start();
//			re.classify();
//			t1.stop();
//			re.realise();
//			System.out.println("HermiT" + re.getInconsistentClasses());
			//////////////////////////////
		
			Reasoner reasoner = resonerFact.createReasoner(manager);
			reasoner.loadOntologies(ontologies);
			SwingProgressMonitor monitor = new SwingProgressMonitor();
			reasoner.getKB().getTaxonomyBuilder().setProgressMonitor(monitor);
			reasoner.classify();
			System.out.println(reasoner.getInconsistentClasses());
		
			
			LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(
					manager, resonerFact, ontologies);
			

						
			
			
			Set<OWLClass> unsatClasses = reasoner.getInconsistentClasses();
			OWLSubClassAxiom unsatAxiom;
			unsatAxiom = dataFactory.getOWLSubClassAxiom(dataFactory.getOWLClass(URI.create("http://reliant.teknowledge.com/DAML/Economy.owl#Cassava")),
					dataFactory.getOWLNothing());
//			for (OWLClass unsat : unsatClasses) {
//				unsatAxiom = dataFactory.getOWLSubClassAxiom(unsat, dataFactory
//						.getOWLNothing());
//				Set<Set<OWLAxiom>> preciseJusts = expGen
//						.getExplanations(unsatAxiom);
//				renderer.render(unsatAxiom, preciseJusts);
//			}
			Set<Set<OWLAxiom>> preciseJusts = expGen.getExplanations(unsatAxiom);
			renderer.render(unsatAxiom, preciseJusts);
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

		} catch (IOException e) {
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
			Set<Set<OWLAxiom>> preciseJusts = expGen.getExplanations(unsatAxiom);
			renderer.render(unsatAxiom, preciseJusts);
			renderer.endRendering();
			expGen.returnSourceAxioms(preciseJusts);
			
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
		} catch (IOException e) {
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
			

			 
			 Set<Set<OWLAxiom>> regularJusts = expGen.getRegularExplanations(axiom);
			 System.out.println("Regular explanations:");
			 renderer.render(axiom, regularJusts);
			 
			Set<Set<OWLAxiom>> preciseJusts = expGen.getExplanations(axiom);
			System.out.println("Precise explanations:");
			renderer.render(axiom, preciseJusts);
			

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

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
