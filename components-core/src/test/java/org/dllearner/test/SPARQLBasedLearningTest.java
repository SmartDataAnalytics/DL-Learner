/**
 * 
 */
package org.dllearner.test;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ClassBasedSampleGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class SPARQLBasedLearningTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		endpoint = new SparqlEndpoint(new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), "http://dbpedia.org");
		
		Set<String> ignoredProperties = Sets.newHashSet(
				"http://dbpedia.org/ontology/wikiPageRevisionID",
				"http://dbpedia.org/ontology/wikiPageID",
				"http://dbpedia.org/ontology/abstract",
				"http://dbpedia.org/ontology/alias"
				,"http://dbpedia.org/ontology/number"
				,"http://dbpedia.org/ontology/endowment"
				);
		
		// the class to describe
		OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/SoccerPlayer"));
		
		// (optionally) provide the schema
		String ontologyURL = "http://mappings.dbpedia.org/server/ontology/dbpedia.owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology schemaOntology = man.loadOntology(IRI.create(ontologyURL));
		// OWL API does not support rdf:langString so far
		Set<OWLDataPropertyRangeAxiom> rangeAxioms = schemaOntology.getAxioms(AxiomType.DATA_PROPERTY_RANGE);
		Set<OWLAxiom> toRemove = new HashSet<OWLAxiom>();
		Set<OWLAxiom> toAdd = new HashSet<OWLAxiom>();
		for (OWLDataPropertyRangeAxiom ax : rangeAxioms) {
			OWLDatatype datatype = ax.getRange().asOWLDatatype();
			if(datatype.equals(df.getOWLDatatype(IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")))) {
				toRemove.add(ax);
				toAdd.add(df.getOWLDataPropertyRangeAxiom(ax.getProperty(), df.getOWLDatatype(IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral"))));
			}
		}
		man.removeAxioms(schemaOntology, toRemove);
		man.addAxioms(schemaOntology, toAdd);
		
		KnowledgeSource schemaKS = new OWLAPIOntology(schemaOntology);
		schemaKS.init();
		
		// setup the dataset
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint, schemaKS);
		ks.init();
		
		// extract sample of the knowledge base
		ClassBasedSampleGenerator sampleGen = new ClassBasedSampleGenerator(ks);
		sampleGen.addAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
		sampleGen.addIgnoredProperties(ignoredProperties);
		sampleGen.addAllowedObjectNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"));
		OWLOntology sampleOntology = sampleGen.getSample(cls);
		
		// add schema axioms to the sample
		man.addAxioms(sampleOntology, schemaOntology.getLogicalAxioms());
		
		// setup knowledge source
		AbstractKnowledgeSource sampleKS = new OWLAPIOntology(sampleOntology);
		ks.init();
		
		// setup reasoner
//		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(sampleKS);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
		baseReasoner.init();
		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(baseReasoner);
		reasoner.init();
		
		// setup learning problem
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		// setup refinement operator
		RhoDRDown op = new RhoDRDown();
		op.setUseTimeDatatypes(false);
		op.setReasoner(reasoner);
		op.setUseNegation(false);
		op.init();
		
		// setup learning algorithm
		CELOE celoe = new CELOE(lp, reasoner);
		celoe.setOperator(op);
		celoe.setWriteSearchTree(true);
		celoe.setSearchTreeFile("/tmp/searchtree-celoe.txt");
		celoe.setReplaceSearchTree(true);
		celoe.setMaxExecutionTimeInSeconds(10);
		celoe.setNoisePercentage(0);
		celoe.init();
		
		// run
		celoe.start();
		
//		Set<OWLClassExpression> refinements = op.refine(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Work")), 5);
//		for (OWLClassExpression ref : refinements) {
//			System.out.println(ref + ":" + lp.getAccuracy(ref, 1.0));
//		}
		
		
		NavigableSet<? extends EvaluatedDescription> solutions = celoe.getCurrentlyBestEvaluatedDescriptions();
		
		System.out.println(solutions);
	}

}
