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
package org.dllearner.kb.sparql;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.HasProgressMonitor;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.dllearner.utilities.ProgressMonitor;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractSampleGenerator implements HasProgressMonitor<AbstractSampleGenerator.SampleGeneratorProgressMonitor> {

	public interface SampleGeneratorProgressMonitor extends ProgressMonitor {
		void sampleGenerationStarted();
		void sampleGenerationFinished();
	}
	private Collection<SampleGeneratorProgressMonitor> progressMonitors = new LinkedHashSet<>();
	@Override
	public Collection<SampleGeneratorProgressMonitor> progressMonitors() {
		return progressMonitors;
	}

	private void fireSampleGenerationStarted() {
		progressMonitors().forEach(SampleGeneratorProgressMonitor::sampleGenerationStarted);
	}

	private void fireSampleGenerationFinished() {
		progressMonitors().forEach(SampleGeneratorProgressMonitor::sampleGenerationFinished);
	}

	private ConciseBoundedDescriptionGenerator cbdGen;
	
	private int sampleDepth = 2;

	protected QueryExecutionFactory qef;
	
	protected SPARQLReasoner reasoner;
	
	private boolean loadRelatedSchema = true;


	public AbstractSampleGenerator(SparqlEndpointKS ks) {
		this(ks.getQueryExecutionFactory());
	}
	
	public AbstractSampleGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGen.setIgnoredProperties(Sets.newHashSet(OWL.sameAs.getURI()));
		
		reasoner = new SPARQLReasoner(qef);
	}
	
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
		cbdGen.setAllowedPropertyNamespaces(namespaces);
	}
	
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
		cbdGen.setAllowedObjectNamespaces(namespaces);
	}
	
	public void addIgnoredProperties(Set<String> ignoredProperties) {
		cbdGen.setIgnoredProperties(ignoredProperties);
	}

	public void addIgnoredClasses(Set<String> ignoredProperties) {
		cbdGen.setIgnoredProperties(ignoredProperties);
	}

	public void addAllowedClassNamespaces(Set<String> ignoredProperties) {
		cbdGen.setIgnoredProperties(ignoredProperties);
	}

	public void setLoadRelatedSchema(boolean loadRelatedSchema) {
		this.loadRelatedSchema = loadRelatedSchema;
	}

	/**
	 * Computes a sample of the knowledge base, i.e. it contains only facts
	 * about the positive and negative individuals.
	 * @param individuals the individuals
	 * @return a sample ontology of the knowledge bas
	 */
	public OWLOntology getSample(Set<OWLIndividual> individuals) {
		fireSampleGenerationStarted();

		// get the sample model
		Model sampleModel = getSampleModel(individuals);

		// convert to ontology
		OWLOntology sampleOntology = OwlApiJenaUtils.getOWLOntology(sampleModel);

		fireSampleGenerationFinished();

		return sampleOntology;
	}
	
	/**
	 * @param sampleDepth the maximum sample depth to set
	 */
	public void setSampleDepth(int sampleDepth) {
		this.sampleDepth = sampleDepth;
	}
	
	/**
	 * @return the maximum sample depth
	 */
	public int getSampleDepth() {
		return sampleDepth;
	}

	public void addPostProcessor(Predicate<Statement> postProcessStatementFilter) {

	}


	protected Model getSampleModel(Set<OWLIndividual> individuals) {
		Set<String> resources = individuals.stream().map(OWLIndividual::toStringID).collect(Collectors.toSet());
		Model model = cbdGen.getConciseBoundedDescription(resources, sampleDepth);

//		Model model = ModelFactory.createDefaultModel();
//
		// load instance data
//		for(OWLIndividual ind : individuals){
//			Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), sampleDepth);
//			model.add(cbd);
//		}
		
		StmtIterator iterator = model.listStatements();
		List<Statement> toAdd = new ArrayList<>();
		while(iterator.hasNext()) {
			Statement st = iterator.next();
			if(st.getObject().isLiteral()) {
				Literal lit = st.getObject().asLiteral();
				RDFDatatype datatype = lit.getDatatype();
				
				if(datatype != null) {
					if(datatype.equals(XSDDatatype.XSDdouble) && lit.getLexicalForm().equals("NAN")) {
						iterator.remove();
						toAdd.add(model.createLiteralStatement(st.getSubject(), st.getPredicate(), Double.NaN));
					} else if(datatype.equals(XSDDatatype.XSDgYear) && st.getPredicate().getURI().equals("http://dbpedia.org/ontology/birthDate")) {
						iterator.remove();
						toAdd.add(model.createStatement(st.getSubject(), st.getPredicate(), model.createTypedLiteral("2000-01-01", XSDDatatype.XSDdate)));
					} else if(datatype.equals(XSDDatatype.XSDdate)) {
						iterator.remove();
						toAdd.add(model.createStatement(st.getSubject(), st.getPredicate(), model.createTypedLiteral("2000-01-01", XSDDatatype.XSDdate)));
					} else if(datatype.equals(RDF.langString)) {
						iterator.remove();
						toAdd.add(model.createStatement(st.getSubject(), st.getPredicate(), model.createTypedLiteral("2000-01-01", new BaseDatatype(RDF.dtLangString.getURI()))));
					}
				}
			}
		}
		model.add(toAdd);
		
		// infer entity types, e.g. object or data property
		OWLEntityTypeAdder.addEntityTypes(model);
		
		// load related schema information
		if(loadRelatedSchema) {
//			loadRelatedSchema(model);
		}
		
		return model;
	}
	
	private void loadRelatedSchema(Model model) {
		String query = 
				"CONSTRUCT {" +
				"?p a owl:ObjectProperty;" +
//				"a ?type;" +
				"rdfs:domain ?domain;" +
				"rdfs:range ?range." +
				"} WHERE {" +
				"?p a owl:ObjectProperty." +
//				"?p a ?type. " +
				"OPTIONAL{?p rdfs:domain ?domain.} " +
				"OPTIONAL{?p rdfs:range ?range.}" +
				"}";
		
		QueryExecution qe = qef.createQueryExecution(query);
		qe.execConstruct(model);
		qe.close();
		
		query = 
				"CONSTRUCT {" +
				"?s a owl:Class ." +
				"?s rdfs:subClassOf ?sup ." +
				"} WHERE {\n" +
				"?s a owl:Class ." +
				"OPTIONAL{?s rdfs:subClassOf ?sup .} " +
				"}";
		qe = qef.createQueryExecution(query);
		qe.execConstruct(model);
		qe.close();
	}

}
