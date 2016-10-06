/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.utilities.analyse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.kb.sparql.simple.QueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class TypeOntology {

	private static Logger log = LoggerFactory.getLogger(TypeOntology.class);

	private int addTypes(OntModel model) {
		int changes=0;
		Set<String> dataProperties = new HashSet<>();
		Set<String> objectProperties = new HashSet<>();
		Set<String> classes = new HashSet<>();
		Set<String> individuals = new HashSet<>();
		Set<Triple> triples = model.getGraph().find(Triple.ANY).toSet();

		ExtendedIterator<OntClass> itClass = model.listNamedClasses();
		while (itClass.hasNext()) {
			classes.add(itClass.next().getURI());
		}

		ExtendedIterator<Individual> itIndividuals = model.listIndividuals();
		while (itIndividuals.hasNext()) {
			individuals.add(itIndividuals.next().getURI());
		}

		ExtendedIterator<DatatypeProperty> itDataProperties = model
				.listDatatypeProperties();
		while (itDataProperties.hasNext()) {
			dataProperties.add(itDataProperties.next().getURI());
		}

		ExtendedIterator<ObjectProperty> itObjectProperties = model
				.listObjectProperties();
		while (itObjectProperties.hasNext()) {
			objectProperties.add(itObjectProperties.next().getURI());
		}

		String sUri;
		String pUri;
		String oUri;
		//System.out.println(individuals);

		// foreach triple in the model
		for (Triple triple : triples) {
			if(!triple.getSubject().isURI() || !triple.getPredicate().isURI() || !triple.getObject().isURI()){
				continue;
			}
			sUri = triple.getSubject().getURI();
			pUri = triple.getPredicate().getURI();
			oUri = triple.getObject().getURI();

			// if subject is an Individual
			if (individuals.contains(sUri)) {
				log.trace("Subject is an individual {}",triple);

				// if predicate is rdf:type
				if (pUri.equals(RDF.type.getURI())) {

					// if object is not in the list of class and not equals
					// owl:thing
					if (!classes.contains(oUri)
							&& !oUri.equals(OWL.Thing.getURI())) {
						model.getResource(oUri).addProperty(RDF.type,OWL.Class);
						classes.add(oUri);
						changes++;
						log.debug("{} is a class",oUri);
					}

					// object is not a class, so it can only be a literal or an
					// object
					// if object is a literal
				} else if (model.getResource(oUri).isLiteral()) {

					// if predicate is not in the list of objectproperties
					if (!objectProperties.contains(pUri)) {
						model.createDatatypeProperty(pUri);
						dataProperties.add(pUri);

						log.debug("{} is a dataproperty",pUri);

						// if predicate is in the list of objectproperties it
						// must be an rdf:property
					} else {
						model.createOntProperty(pUri);
						log.info("{} is a rdf:property", pUri);
					}
					changes++;

					// object is not a literal or a class so it must be an
					// instance
					// if object is not in the list of individuals
				} else if (!individuals.contains(oUri)) {
					model.getResource(oUri).addProperty(RDF.type, OWL.Thing);
					individuals.add(oUri);

					// subject and object are individuals so is predicate an
					// objectproperty
					// if predicate ist not in the list of dataproperties
					if (!dataProperties.contains(pUri)) {
						model.createObjectProperty(pUri);
						objectProperties.add(pUri);
						log.debug("{} is an objectproperty", pUri);

						// if predicate is in the list of dataproperties it must
						// be a rdf:property
					} else {
						model.createOntProperty(pUri);
						log.info("{} is a rdf:property", pUri);
					}
					log.debug("{} is an individual",oUri);
					changes++;
				}
				// if subject is an owl:class
			} else if (classes.contains(sUri)) {
                log.trace("Subject is a class {}",triple);

                //TODO check this assumption
                //if s is owl:class, then o is owl:class too ????
                if(!classes.contains(oUri) ){
				    model.getResource(oUri).addProperty(RDF.type, OWL.Class);
                    classes.add(oUri);
                    log.debug("{} is a class",oUri);
				    changes++;
                }
			}
		}
		return changes;
	}

	public OntModel addTypetoJena(OntModel model, List<String> individuals,
			List<String> classes) {
		if (individuals != null) {
			for (String individual : individuals) {
			    if (individual.startsWith("<")) {
			        int strlen = individual.length();
			        // assuming that there is also a closing angle bracket
			        individual = individual.substring(1, strlen-1);
			    }
				model.getResource(individual).addProperty(RDF.type, OWL.Thing);
			}
		}
		if (classes != null) {
			for (String ontClass : classes) {
				if (!ontClass.equals(OWL.Thing.getURI())) {
					model.getResource(ontClass).addProperty(RDFS.subClassOf, OWL.Thing);
				}
			}
		}
//		model.write(System.out);
		while(this.addTypes(model)!=0);

		return model;
	}

	public static void main(String... args) {
		String sparql = "CONSTRUCT {?s ?p ?o}"
				+ "{ ?s ?p ?o "
				+ "FILTER (?s IN( <http://dbpedia.org/resource/Philolaus>,"
				+ " <http://dbpedia.org/resource/Zeno_of_Elea>,"
				+ " <http://dbpedia.org/resource/Socrates>,"
				+ " <http://dbpedia.org/resource/Pythagoras>,"
				+ " <http://dbpedia.org/resource/Archytas>,"
				+ " <http://dbpedia.org/resource/Plato>,"
				+ " <http://dbpedia.org/resource/Democritus> )) ."
				+ " FILTER ( !isLiteral(?o) &&   regex(str(?o), '^http://dbpedia.org/resource/') &&"
				+ " ! regex(str(?o), '^http://dbpedia.org/resource/Category') &&"
				+ " ! regex(str(?o), '^http://dbpedia.org/resource/Template')  ) . }";
		OntModel model = ModelFactory.createOntologyModel();
		List<String> individuals = new ArrayList<>(7);
		individuals.add("http://dbpedia.org/resource/Philolaus");
		individuals.add("http://dbpedia.org/resource/Zeno_of_Elea");
		individuals.add("http://dbpedia.org/resource/Socrates");
		individuals.add("http://dbpedia.org/resource/Pytagoras");
		individuals.add("http://dbpedia.org/resource/Archytas");
		individuals.add("http://dbpedia.org/resource/Plato");
		individuals.add("http://dbpedia.org/resource/Democritus");
		QueryExecutor exec = new QueryExecutor();
		exec.executeQuery(sparql, "http://live.dbpedia.org/sparql", model,
				"http://dbpedia.org");
		System.out.println(model.listIndividuals().toSet());
		System.out.println(model.listObjectProperties().toSet());
		TypeOntology type = new TypeOntology();
		model=type.addTypetoJena(model, individuals, null);
		System.out.println(model.listIndividuals().toSet());
		System.out.println(model.listObjectProperties().toSet());
		System.out.println(model.listDatatypeProperties().toSet());
	}
}
