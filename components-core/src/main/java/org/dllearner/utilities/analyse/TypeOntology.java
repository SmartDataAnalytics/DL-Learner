package org.dllearner.utilities.analyse;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.kb.sparql.simple.QueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class TypeOntology {

	private static Logger log = LoggerFactory.getLogger(TypeOntology.class);

	public void addTypes(OntModel model) {
		Set<DatatypeProperty> dataProperties = model.listDatatypeProperties()
				.toSet();
		Set<ObjectProperty> objectProperties = model.listObjectProperties()
				.toSet();
		Set<OntClass> classes = model.listClasses().toSet();
		Set<Individual> individuals = model.listIndividuals().toSet();
		Set<Triple> triples = model.getGraph().find(Triple.ANY).toSet();
		Node subject;
		Node predicate;
		Node object;
		// while (!triples.isEmpty()) {
		ExtendedIterator<Triple> iterator = model.getGraph().find(Triple.ANY);
		// System.out.println(triples.size());
		for (Triple triple : triples) {
			// System.out.println(triple);
			subject = triple.getSubject();
			predicate = triple.getPredicate();
			object = triple.getObject();
			if (individuals.contains(model.getResource(subject.getURI()))) {
				log.debug("{}", triple);
				if (predicate.hasURI(RDF.type.getURI())) {
					if (!classes.contains(model.getResource(object.getURI()))
							&& !object.getURI().equals(OWL.Thing.getURI())) {
						model.getResource(subject.getURI()).addProperty(
								com.hp.hpl.jena.vocabulary.RDFS.subClassOf,
								OWL.Thing);
						classes = model.listClasses().toSet();
						log.debug("{} is a class", object);
					}
				} else if (object.isLiteral()) {
					if (!objectProperties.contains(model.getResource(predicate
							.getURI()))) {
						model.createDatatypeProperty(predicate.getURI());
						dataProperties = model.listDatatypeProperties().toSet();
						log.debug("{} is a dataproperty", predicate);
					} else {
						model.createOntProperty(predicate.getURI());
						log.info("{} is a rdf:property", predicate);
					}
				} else if (!individuals.contains(model.getResource(object
						.getURI()))) {
					model.getResource(object.getURI()).addProperty(RDF.type,
							OWL.Thing);
					individuals = model.listIndividuals().toSet();
					if (!dataProperties.contains(model.getResource(predicate
							.getURI()))) {
						model.createObjectProperty(predicate.getURI());
						objectProperties = model.listObjectProperties().toSet();
						log.debug("{} is an objectproperty", predicate);
					} else {
						model.createOntProperty(predicate.getURI());
						log.info("{} is a rdf:property", predicate);
					}
					log.debug("{} is an individual", object);
				}

			} else if (classes.contains(model.getResource(subject.getURI()))) {
				model.getResource(object.getURI()).addProperty(
						com.hp.hpl.jena.vocabulary.RDFS.subClassOf, OWL.Thing);
			}
		}
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
		model.createIndividual("http://dbpedia.org/resource/Philolaus",
				OWL.Thing);
		model.createIndividual("http://dbpedia.org/resource/Zeno_of_Elea",
				OWL.Thing);
		model.createIndividual("http://dbpedia.org/resource/Socrates",
				OWL.Thing);
		model.createIndividual("http://dbpedia.org/resource/Pytagoras",
				OWL.Thing);
		model.createIndividual("http://dbpedia.org/resource/Archytas",
				OWL.Thing);
		model.createIndividual("http://dbpedia.org/resource/Plato", OWL.Thing);
		model.createIndividual("http://dbpedia.org/resource/Democritus",
				OWL.Thing);
		QueryExecutor exec = new QueryExecutor();
		exec.executeQuery(sparql, "http://live.dbpedia.org/sparql", model,
				"http://dbpedia.org");
		System.out.println(model.listIndividuals().toSet());
		System.out.println(model.listObjectProperties().toSet());
		TypeOntology type = new TypeOntology();
		type.addTypes(model);
		System.out.println(model.listIndividuals().toSet());
		System.out.println(model.listObjectProperties().toSet());
		System.out.println(model.listDatatypeProperties().toSet());
	}
}
