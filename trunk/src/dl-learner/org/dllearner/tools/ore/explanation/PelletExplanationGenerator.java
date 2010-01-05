package org.dllearner.tools.ore.explanation;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.reasonerfactory.pellet.PelletReasonerFactory;

import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.GlassBoxExplanation;
import com.clarkparsia.explanation.SatisfiabilityConverter;
import com.clarkparsia.explanation.TransactionAwareSingleExpGen;
import com.clarkparsia.explanation.util.ExplanationProgressMonitor;

public class PelletExplanationGenerator implements ExplanationGenerator{
	static {
		setup();
	}

	/**
	 * Very important initialization step that needs to be called once before a
	 * reasoner is created. This function will be called automatically when
	 * GlassBoxExplanation is loaded by the class loader. This function simply
	 * calls the {@link GlassBoxExplanationGenerator#setup()} function.
	 */
	public static void setup() {
		GlassBoxExplanation.setup();
	}

	
	private HSTExplanationGenerator expGen;
	
	private SatisfiabilityConverter converter;
	
	public PelletExplanationGenerator(OWLOntologyManager manager, Set<OWLOntology> ontologies) {
		this( manager, ontologies, null, true );
	}
	
	public PelletExplanationGenerator(OWLOntologyManager manager, Set<OWLOntology> ontologies, boolean useGlassBox) {
		this( manager, ontologies, null, useGlassBox );
	}
	
	private PelletExplanationGenerator(OWLOntologyManager manager, Set<OWLOntology> ontologies, Reasoner reasoner, boolean useGlassBox) {

		
		// If there is no existing reasoner create a new one and load the ontologies 
		if( reasoner == null ) {
			reasoner = new Reasoner( manager );
			reasoner.loadOntologies( manager.getImportsClosure(ontologies.iterator().next()));
		}

		// Create a single explanation generator
		TransactionAwareSingleExpGen singleExp = useGlassBox 
			? new GlassBoxExplanation( manager )
			: new BlackBoxExplanation( manager );
		
		// Create multiple explanation generator
		expGen = new HSTExplanationGenerator( singleExp );
		
		// Set the necessary fields		
		expGen.setReasonerFactory( new PelletReasonerFactory() );
		expGen.setReasoner( reasoner );
		expGen.setOntologies( manager.getImportsClosure(ontologies.iterator().next()) );
		
		// Create the converter that will translate axioms into class expressions
		converter = new SatisfiabilityConverter( manager.getOWLDataFactory() );
		setup();
	}
	
	public PelletExplanationGenerator(Reasoner reasoner) {
		this( reasoner.getManager(), reasoner.getLoadedOntologies(), reasoner, true );
	}
	
	public PelletExplanationGenerator(Reasoner reasoner, boolean useGlassBox) {
		this( reasoner.getManager(), reasoner.getLoadedOntologies(), reasoner, useGlassBox );
	}

	@Override
	public Explanation getExplanation(OWLAxiom entailment) {
		OWLDescription unsatClass = converter.convert( entailment );
    	return new Explanation( entailment, expGen.getExplanation( unsatClass ));
	}

	@Override
	public Set<Explanation> getExplanations(OWLAxiom entailment) {
		OWLDescription unsatClass = converter.convert( entailment );
		Set<Explanation> explanations = new HashSet<Explanation>();
		for(Set<OWLAxiom> axioms : expGen.getExplanations( unsatClass )){
			explanations.add(new Explanation(entailment, axioms));
		}
    	return explanations;
	}

	@Override
	public Set<Explanation> getExplanations(OWLAxiom entailment, int limit) {
		OWLDescription unsatClass = converter.convert( entailment );
		Set<Explanation> explanations = new HashSet<Explanation>();
		for(Set<OWLAxiom> axioms : expGen.getExplanations( unsatClass, limit)){
			explanations.add(new Explanation(entailment, axioms));
		}
    	return explanations;
	}
	
	public void setProgressMonitor(ExplanationProgressMonitor progressMonitor){
		expGen.setProgressMonitor(progressMonitor);
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI.create("file:examples/ore/tambis.owl"));
		Reasoner reasoner = new org.mindswap.pellet.owlapi.PelletReasonerFactory().createReasoner(manager);
		reasoner.loadOntology(ontology);
		PelletExplanationGenerator expGen = new PelletExplanationGenerator(reasoner);
		
		
		
		OWLClass unsat = factory.getOWLClass(URI.create("http://krono.act.uji.es/Links/ontologies/tambis.owl#metal"));
		OWLAxiom entailment = factory.getOWLSubClassAxiom(unsat, factory.getOWLNothing());
		System.out.println(expGen.getExplanation(entailment));
	}
    

}
