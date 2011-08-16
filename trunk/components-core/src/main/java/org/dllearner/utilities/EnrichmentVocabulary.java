package org.dllearner.utilities;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class EnrichmentVocabulary {
	
	private static final OWLDataFactory factory = new OWLDataFactoryImpl();
	
	public static final String NS = "http://www.dl-learner.org/enrichment.owl#";
	
	//the classes
	public static final OWLClass ChangeSet = factory.getOWLClass(IRI.create(NS + "ChangeSet"));
	
	public static final OWLClass SuggestionSet = factory.getOWLClass(IRI.create(NS + "SuggestionSet"));
	
	public static final OWLClass Suggestion = factory.getOWLClass(IRI.create(NS + "Suggestion"));
	
	public static final OWLClass Parameter = factory.getOWLClass(IRI.create(NS + "Parameter"));
	
	public static final OWLClass Creation = factory.getOWLClass(IRI.create(NS + "Creation"));
	
	public static final OWLClass AlgorithmRun = factory.getOWLClass(IRI.create(NS + "AlgorithmRun"));
	
	
	//the object properties
	public static final OWLObjectProperty creator = factory.getOWLObjectProperty(IRI.create(NS + "creatr"));
	
//	public static final OWLObjectProperty hasAxiom = factory.getOWLObjectProperty(IRI.create(NS + "hasAxiom"));
	
	public static final OWLObjectProperty hasChange = factory.getOWLObjectProperty(IRI.create(NS + "hasChange"));
	
	public static final OWLObjectProperty hasInput = factory.getOWLObjectProperty(IRI.create(NS + "hasInput"));
	
	public static final OWLObjectProperty hasSuggestion = factory.getOWLObjectProperty(IRI.create(NS + "hasSuggestion"));
	
	public static final OWLObjectProperty hasParameter = factory.getOWLObjectProperty(IRI.create(NS + "hasParameter"));
	
	public static final OWLObjectProperty usedAlgorithm = factory.getOWLObjectProperty(IRI.create(NS + "usedAlgorithm"));
	
	
	//the data properties
	public static final OWLDataProperty confidence = factory.getOWLDataProperty(IRI.create(NS + "confidence"));
	
	public static final OWLDataProperty explanation = factory.getOWLDataProperty(IRI.create(NS + "explanation"));
	
	public static final OWLDataProperty parameterName = factory.getOWLDataProperty(IRI.create(NS + "parameterName"));
	
	public static final OWLDataProperty parameterValue = factory.getOWLDataProperty(IRI.create(NS + "parameterValue"));
	
	public static final OWLDataProperty timestamp = factory.getOWLDataProperty(IRI.create(NS + "timestamp"));
	
	public static final OWLDataProperty version = factory.getOWLDataProperty(IRI.create(NS + "version"));
	
	public static final OWLDataProperty hasAxiom = factory.getOWLDataProperty(IRI.create(NS + "hasAxiom"));
	
}
