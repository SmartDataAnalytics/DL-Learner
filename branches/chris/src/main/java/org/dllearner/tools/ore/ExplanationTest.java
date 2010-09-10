package org.dllearner.tools.ore;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;


public class ExplanationTest {

//	private static final String	file	= "file:examples/ore/inconsistent.owl";
	private static final String	file	= "file:examples/ore/koala.owl";
	private static final String	NS		= "http://cohse.semanticweb.org/ontologies/people#";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
			try {
				PelletExplanation.setup();
				
				// The renderer is used to pretty print explanation
				ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
				// The writer used for the explanation rendered
				PrintWriter out = new PrintWriter( System.out );
				renderer.startRendering( out );

				// Create an OWLAPI manager that allows to load an ontology file and
				// create OWLEntities
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				OWLOntology ontology = manager.loadOntology( IRI.create( file ) );
				OWLDataFactory factory = manager.getOWLDataFactory();
				
				// Create the reasoner and load the ontology
				PelletReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);
				
				// Create an explanation generator
				PelletExplanation expGen = new PelletExplanation( reasoner );
				
				// Create some concepts
				OWLClass madCow = factory.getOWLClass( IRI.create( NS + "mad+cow" ) );
				OWLClass animalLover = factory.getOWLClass( IRI.create( NS + "animal+lover" ) );
				OWLClass petOwner = factory.getOWLClass( IRI.create( NS + "pet+owner" ) );
				
				//Explain why ontology is inconsistent
				out.println( "Why is ontology inconsistent?" );	
				renderer.render(expGen.getInconsistencyExplanations());
				
				out.println( "unsatisfiable classes:" );		
				for(OWLClass cl : reasoner.getUnsatisfiableClasses().getEntities()){
						out.println(cl);
						renderer.render(expGen.getUnsatisfiableExplanations(cl));
				}
				
				
				
				
				// Explain why mad cow is an unsatisfiable concept
				Set<Set<OWLAxiom>> exp = expGen.getUnsatisfiableExplanations( madCow );
				out.println( "Why is " + madCow + " concept unsatisfiable?" );		
				renderer.render( exp );

				// Now explain why animal lover is a sub class of pet owner
				exp = expGen.getSubClassExplanations( animalLover, petOwner );
				out.println( "Why is " + animalLover + " subclass of " + petOwner + "?" );
				renderer.render( exp );
				
				renderer.endRendering();
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedOperationException e) {
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
		
}


