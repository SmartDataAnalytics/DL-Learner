/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class EnrichmentVocabulary {
	
	private static final OWLDataFactory factory = new OWLDataFactoryImpl();
	
	//the default namespace
	public static final String NS = "http://www.dl-learner.org/ontologies/enrichment.owl#";
	
	
	//the classes
	public static final OWLClass ChangeSet = factory.getOWLClass(IRI.create(NS + "ChangeSet"));
	
	public static final OWLClass SuggestionSet = factory.getOWLClass(IRI.create(NS + "SuggestionSet"));
	
	public static final OWLClass Suggestion = factory.getOWLClass(IRI.create(NS + "Suggestion"));
	
	public static final OWLClass AddSuggestion = factory.getOWLClass(IRI.create(NS + "AddSuggestion"));
	
	public static final OWLClass DeleteSuggestion = factory.getOWLClass(IRI.create(NS + "DeleteSuggestion"));
	
	public static final OWLClass Parameter = factory.getOWLClass(IRI.create(NS + "Parameter"));
	
	public static final OWLClass Creation = factory.getOWLClass(IRI.create(NS + "Creation"));
	
	public static final OWLClass AlgorithmRun = factory.getOWLClass(IRI.create(NS + "AlgorithmRun"));
	
	public static final OWLClass SPARQLEndpoint = factory.getOWLClass(IRI.create(NS + "SPARQL_Endpoint"));
	
	public static final OWLClass OWLFile = factory.getOWLClass(IRI.create(NS + "OWL_File"));
	
	
	//the object properties
	public static final OWLObjectProperty creator = factory.getOWLObjectProperty(IRI.create(NS + "creator"));
	
//	public static final OWLObjectProperty hasAxiom = factory.getOWLObjectProperty(IRI.create(NS + "hasAxiom"));
	
	public static final OWLObjectProperty hasChange = factory.getOWLObjectProperty(IRI.create(NS + "hasChange"));
	
	public static final OWLObjectProperty hasInput = factory.getOWLObjectProperty(IRI.create(NS + "hasInput"));
	
	public static final OWLObjectProperty hasSuggestion = factory.getOWLObjectProperty(IRI.create(NS + "hasSuggestion"));
	
	public static final OWLObjectProperty hasParameter = factory.getOWLObjectProperty(IRI.create(NS + "hasParameter"));
	
	public static final OWLObjectProperty usedAlgorithm = factory.getOWLObjectProperty(IRI.create(NS + "usedAlgorithm"));
	
	public static final OWLObjectProperty defaultGraph = factory.getOWLObjectProperty(IRI.create(NS + "defaultGraph"));
	
	
	//the data properties
	public static final OWLDataProperty confidence = factory.getOWLDataProperty(IRI.create(NS + "confidence"));
	
	public static final OWLDataProperty explanation = factory.getOWLDataProperty(IRI.create(NS + "explanation"));
	
	public static final OWLDataProperty parameterName = factory.getOWLDataProperty(IRI.create(NS + "parameterName"));
	
	public static final OWLDataProperty parameterValue = factory.getOWLDataProperty(IRI.create(NS + "parameterValue"));
	
	public static final OWLDataProperty timestamp = factory.getOWLDataProperty(IRI.create(NS + "timestamp"));
	
	public static final OWLDataProperty version = factory.getOWLDataProperty(IRI.create(NS + "version"));
	
	public static final OWLDataProperty hasAxiom = factory.getOWLDataProperty(IRI.create(NS + "hasAxiom"));
	
	
	//the annotation properties
	public static final OWLAnnotationProperty belongsTo = factory.getOWLAnnotationProperty(IRI.create(NS + "belongsTo"));
	
}
