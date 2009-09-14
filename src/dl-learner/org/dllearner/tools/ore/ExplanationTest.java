package org.dllearner.tools.ore;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Set;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.explanation.PelletExplanation;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;


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
				OWLOntology ontology = manager.loadOntology( URI.create( file ) );
				OWLDataFactory factory = manager.getOWLDataFactory();
				
				// Create the reasoner and load the ontology
				Reasoner reasoner = new Reasoner( manager );
				reasoner.loadOntology( ontology );
				
				// Create an explanation generator
				PelletExplanation expGen = new PelletExplanation( reasoner );
				
				// Create some concepts
				OWLClass madCow = factory.getOWLClass( URI.create( NS + "mad+cow" ) );
				OWLClass animalLover = factory.getOWLClass( URI.create( NS + "animal+lover" ) );
				OWLClass petOwner = factory.getOWLClass( URI.create( NS + "pet+owner" ) );
				
				//Explain why ontology is inconsistent
				out.println( "Why is ontology inconsistent?" );	
				renderer.render(expGen.getInconsistencyExplanations());
				
				out.println( "unsatisfiable classes:" );		
				for(OWLClass cl : reasoner.getClasses()){
					if(!reasoner.isSatisfiable(cl)){
						out.println(cl);
						renderer.render(expGen.getUnsatisfiableExplanations(cl));
					}
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


