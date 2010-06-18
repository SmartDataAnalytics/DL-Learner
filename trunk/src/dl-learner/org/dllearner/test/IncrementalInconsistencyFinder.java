package org.dllearner.test;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class IncrementalInconsistencyFinder {
	
	private static final String ENDPOINT_URL = "http://dbpedia-live.openlinksw.com/sparql";
	private static String DEFAULT_GRAPH_URI = "http://dbpedia.org";
	private static int RESULT_LIMIT = 10;
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private OWLReasoner reasoner;
	
	private String endpointURI;
	
	public IncrementalInconsistencyFinder() throws OWLOntologyCreationException{
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.createOntology();
		factory = manager.getOWLDataFactory();
		reasoner = new IncrementalClassifier(ontology);
	}
	
	private void checkForUnsatisfiableClasses(String endpointURI){
		this.endpointURI = endpointURI;
		
		Set<OWLDisjointClassesAxiom> disjointAxioms = retrieveDisjointClassAxioms();
		manager.addAxioms(ontology, disjointAxioms);
		
		for(OWLDisjointClassesAxiom ax : disjointAxioms){
			for(OWLClassExpression cl : ax.getClassExpressions()){
				Set<OWLSubClassOfAxiom> subClassOfAxioms = retrieveSubClassAxioms(cl.asOWLClass());
				manager.addAxioms(ontology, subClassOfAxioms);	
			}
		}
		
		System.out.println(ontology.getLogicalAxioms());
		reasoner.prepareReasoner();
		Node<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses();
		System.out.println("Found " + unsatisfiableClasses.getSize() + " unsatisfiable classes:");
		System.out.println(unsatisfiableClasses.getEntities());
	}
	
	private Set<OWLDisjointClassesAxiom> retrieveDisjointClassAxioms(){
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?x", OWLVocabulary.OWL_DISJOINT_WITH, "?y",
				"regex(?x,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLDisjointClassesAxiom> axioms = new HashSet<OWLDisjointClassesAxiom>();
		while(sparqlResults.hasNext()){
			QuerySolution solution = sparqlResults.nextSolution();
			
			RDFNode rdfNodeSubject = solution.getResource("?x");
			RDFNode rdfNodeObject = solution.getResource("?y");
			
			OWLClass disjointClass1 = factory.getOWLClass(IRI.create(rdfNodeSubject.toString()));
			OWLClass disjointClass2 = factory.getOWLClass(IRI.create(rdfNodeObject.toString()));
			
			axioms.add(factory.getOWLDisjointClassesAxiom(disjointClass1, disjointClass2));
		}
		return axioms;
	}
	
	private Set<OWLSubClassOfAxiom> retrieveSubClassAxioms(OWLClass subClass){
		Query sparqlQuery = createSimpleSelectSPARQLQuery(subClass.toStringID(), OWLVocabulary.RDFS_SUBCLASS_OF,
				"?y", "regex(?y,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();

		Set<OWLSubClassOfAxiom> axioms = new HashSet<OWLSubClassOfAxiom>();
		while(sparqlResults.hasNext()){
			QuerySolution solution = sparqlResults.nextSolution();
			
			RDFNode rdfNodeObject = solution.getResource("?y");
			
			OWLClass superClass = factory.getOWLClass(IRI.create(rdfNodeObject.toString()));
			
			axioms.add(factory.getOWLSubClassOfAxiom(subClass, superClass));
		}
		
		sparqlQuery = createSimpleSelectSPARQLQuery("?x", OWLVocabulary.RDFS_SUBCLASS_OF,
				subClass.toStringID(), "regex(?x,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		sparqlResults = sparqlQueryExec.execSelect();
		while(sparqlResults.hasNext()){
			QuerySolution solution = sparqlResults.nextSolution();
			
			RDFNode rdfNodeObject = solution.getResource("?x");
			
			OWLClass superClass = factory.getOWLClass(IRI.create(rdfNodeObject.toString()));
			
			axioms.add(factory.getOWLSubClassOfAxiom(subClass, superClass));
		}
		return axioms;
	}
	
	private Query createSimpleSelectSPARQLQuery(String subject, String predicate, String object, String filter, int limit){
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * WHERE {");
		if(!subject.startsWith("?")){
			sb.append("<").append(subject).append(">");
		} else {
			sb.append(subject);
		}
		sb.append(" ");
		if(!predicate.startsWith("?")){
			sb.append("<").append(predicate).append(">");
		} else {
			sb.append(predicate);
		}
		sb.append(" ");
		if(!object.startsWith("?")){
			sb.append("<").append(object).append(">");
		} else {
			sb.append(object);
		}
		if(filter != null){
			sb.append(".FILTER ");
			sb.append(filter);
		}
		sb.append("}");
		sb.append(" LIMIT ");
		sb.append(limit);
		Query query = QueryFactory.create(sb.toString());
		return query;
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException{
		IncrementalInconsistencyFinder incFinder = new IncrementalInconsistencyFinder();
		incFinder.checkForUnsatisfiableClasses(ENDPOINT_URL);
		
	}
	
	

}
