package org.dllearner.test;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.tools.ore.explanation.ExplanationGenerator;
import org.dllearner.tools.ore.explanation.PelletExplanationGenerator;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.jamonapi.Monitor;

public class IncrementalInconsistencyFinder {
	
	private static Logger logger = Logger.getRootLogger();
	
	private static final String ENDPOINT_URL = "http://dbpedia-live.openlinksw.com/sparql";
	private static String DEFAULT_GRAPH_URI = "http://dbpedia.org";
	private static int RESULT_LIMIT = 20;
	private static int RECURSION_DEPTH = 2;
	
	private static boolean BREAK_AFTER_ERROR_FOUND = true;
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private OWLReasoner reasoner;
	
	private String endpointURI;
	
	private Monitor queryMonitor = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "Query monitor");
	
	public IncrementalInconsistencyFinder() throws OWLOntologyCreationException{
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.createOntology();
		factory = manager.getOWLDataFactory();
		reasoner = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology);
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.INFO);
		
	}
	
	private void checkForUnsatisfiableClasses(String endpointURI){
		this.endpointURI = endpointURI;
		
		Monitor overallMonitor = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "Overall monitor").start();
		Monitor reasonerMonitor = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "Reasoning monitor");
		
		Set<OWLClass> visitedClasses = new HashSet<OWLClass>();
		Set<OWLClass> classesToVisit = new HashSet<OWLClass>();
		Set<OWLClass> tmp = new HashSet<OWLClass>();
		
		//we are starting with disjointClasses axioms
		Set<OWLDisjointClassesAxiom> disjointAxioms = retrieveDisjointClassAxioms();
		manager.addAxioms(ontology, disjointAxioms);
		
		for(OWLDisjointClassesAxiom ax : disjointAxioms){
			for(OWLClassExpression cl : ax.getClassExpressions()){
				if(!cl.isAnonymous()){
					classesToVisit.add(cl.asOWLClass());
				}
			}
		}
		boolean foundUnsatisfiableClasses = false;
		logger.info("Starting with " + classesToVisit.size() + " classes to visit.");
		for(int i = 1; i <= RECURSION_DEPTH; i++){
			logger.info("Recursion depth = " + (i-1));
			for(OWLClass cl : classesToVisit){
				logger.info("Starting retrieving axioms for class " + cl);
				Set<OWLAxiom> axioms = retrieveAxioms(cl);
				manager.addAxioms(ontology, axioms);
				if(!axioms.isEmpty()){
					logger.info("Checking for unsatisfiable classes");
					reasonerMonitor.start();
					foundUnsatisfiableClasses = !reasoner.getUnsatisfiableClasses().getEntities().isEmpty();
					reasonerMonitor.stop();
					
				}
				for(OWLAxiom ax : axioms){
					tmp.addAll(ax.getClassesInSignature());
				}
				if(foundUnsatisfiableClasses && BREAK_AFTER_ERROR_FOUND){
					logger.info("Found unsatisfiable classes. Aborting.");
					break;
				}
			}
			if(foundUnsatisfiableClasses && BREAK_AFTER_ERROR_FOUND){
				break;
			}
			visitedClasses.addAll(classesToVisit);
			tmp.removeAll(visitedClasses);
			classesToVisit.clear();
			classesToVisit.addAll(tmp);
			tmp.clear();
		}
		
//		 for each disjointClasses axiom we retrieve all subClassOf axioms, domain and range axioms of the disjoint classes
//		 after each time we added axioms to the ontology, we check for unsatisfiable classes
//		for(OWLDisjointClassesAxiom ax : disjointAxioms){
//			for(OWLClassExpression cl : ax.getClassExpressions()){
//				//retrieve subClassOf axioms
//				Set<OWLSubClassOfAxiom> subClassOfAxioms = retrieveSubClassAxioms(cl.asOWLClass());
//				manager.addAxioms(ontology, subClassOfAxioms);
//				if(!subClassOfAxioms.isEmpty()){
//					reasoner.getUnsatisfiableClasses();
//				}
//				//retrieve objectPropertyDomain axioms
//				Set<OWLObjectPropertyDomainAxiom> propertyDomainAxioms = retrievePropertyDomainAxioms(cl.asOWLClass());
//				manager.addAxioms(ontology, propertyDomainAxioms);
//				if(!propertyDomainAxioms.isEmpty()){
//					reasoner.getUnsatisfiableClasses();
//				}
//				//retrieve objectPropertyRange axioms
//				Set<OWLObjectPropertyRangeAxiom> propertyRangeAxioms = retrievePropertyRangeAxioms(cl.asOWLClass());
//				manager.addAxioms(ontology, propertyRangeAxioms);
//				if(!propertyRangeAxioms.isEmpty()){
//					reasoner.getUnsatisfiableClasses();
//				}
//				//retrieve classAssertion axioms
//				Set<OWLClassAssertionAxiom> classAssertionAxioms = retrieveClassAssertionAxioms(cl.asOWLClass());
//				manager.addAxioms(ontology, classAssertionAxioms);
//			}
//		}
		overallMonitor.stop();
		logger.info("Overall execution time: " + overallMonitor.getTotal() + " ms");
		logger.info("Overall query time: " + queryMonitor.getTotal() + " ms");
		logger.info("Average query time: " + queryMonitor.getAvg() + " ms");
		logger.info("Longest query time: " + queryMonitor.getMax() + " ms");
		logger.info("Shortest query time: " + queryMonitor.getMin() + " ms");
		logger.info("Overall reasoning time: " + reasonerMonitor.getTotal() + " ms");
		logger.info("Average reasoning time: " + reasonerMonitor.getAvg() + " ms");
		logger.info("Longest reasoning time: " + reasonerMonitor.getMax() + " ms");
		logger.info("Shortest reasoning time: " + reasonerMonitor.getMin() + " ms");
		System.out.println(ontology.getLogicalAxiomCount() + " logical axioms in the ontology:");
		System.out.println(ontology.getLogicalAxioms());
		System.out.println("Ontology is " + (reasoner.isConsistent() ? "consistent" : "inconsistent"));
		Node<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses();
		System.out.println("Found " + unsatisfiableClasses.getSize() + " unsatisfiable class(es):");
		System.out.println(unsatisfiableClasses.getEntities());
		if(!unsatisfiableClasses.getEntities().isEmpty()){
			ExplanationGenerator expGen = new PelletExplanationGenerator(ontology);
			System.out.println(expGen.getExplanation(factory.getOWLSubClassOfAxiom(unsatisfiableClasses.getRepresentativeElement(), factory.getOWLNothing())));
		}
		
		
	}
	
	private Set<OWLDisjointClassesAxiom> retrieveDisjointClassAxioms(){
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?x", OWLVocabulary.OWL_DISJOINT_WITH, "?y",
				"regex(?x,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLDisjointClassesAxiom> axioms = new HashSet<OWLDisjointClassesAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		RDFNode rdfNodeObject;
		OWLClass disjointClass1;
		OWLClass disjointClass2;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?x");
			rdfNodeObject = solution.getResource("?y");
			
			disjointClass1 = factory.getOWLClass(IRI.create(rdfNodeSubject.toString()));
			disjointClass2 = factory.getOWLClass(IRI.create(rdfNodeObject.toString()));
			
			axioms.add(factory.getOWLDisjointClassesAxiom(disjointClass1, disjointClass2));
		}
		return axioms;
	}
	
	private Set<OWLSubClassOfAxiom> retrieveSubClassAxioms(OWLClass cl){
		logger.info("Retrieving subClassOf axioms for class " + cl);
		queryMonitor.start();
		
		Set<OWLSubClassOfAxiom> axioms = new HashSet<OWLSubClassOfAxiom>();
		QuerySolution solution;
		RDFNode rdfNode;
		
		//we retrieve first all axioms, where the class is the subClass
		Query sparqlQuery = createSimpleSelectSPARQLQuery(cl.toStringID(), OWLVocabulary.RDFS_SUBCLASS_OF,
				"?y", "regex(?y,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		OWLClass superClass;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNode = solution.getResource("?y");
			
			superClass = factory.getOWLClass(IRI.create(rdfNode.toString()));
			
			axioms.add(factory.getOWLSubClassOfAxiom(cl, superClass));
		}
		
		//this time we retrieve  all axioms, where the class is the superClass
		sparqlQuery = createSimpleSelectSPARQLQuery("?x", OWLVocabulary.RDFS_SUBCLASS_OF,
				cl.toStringID(), "regex(?x,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		sparqlResults = sparqlQueryExec.execSelect();
		OWLClass subClass;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNode = solution.getResource("?x");
			
			subClass = factory.getOWLClass(IRI.create(rdfNode.toString()));
			
			axioms.add(factory.getOWLSubClassOfAxiom(subClass, cl));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLObjectPropertyDomainAxiom> retrievePropertyDomainAxioms(OWLClass cl){
		logger.info("Retrieving objectPropertyDomain axioms for class " + cl);
		queryMonitor.start();
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?s", OWLVocabulary.RDFS_domain, cl.toStringID(),
				"regex(?s,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLObjectPropertyDomainAxiom> axioms = new HashSet<OWLObjectPropertyDomainAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		OWLObjectProperty property;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?s");
			
			property = factory.getOWLObjectProperty(IRI.create(rdfNodeSubject.toString()));
			
			axioms.add(factory.getOWLObjectPropertyDomainAxiom(property, cl));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLObjectPropertyRangeAxiom> retrievePropertyRangeAxioms(OWLClass cl){
		logger.info("Retrieving objectPropertyRange axioms for class " + cl);
		queryMonitor.start();
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?s", OWLVocabulary.RDFS_range, cl.toStringID(),
				"regex(?s,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLObjectPropertyRangeAxiom> axioms = new HashSet<OWLObjectPropertyRangeAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		OWLObjectProperty property;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?s");
			
			property = factory.getOWLObjectProperty(IRI.create(rdfNodeSubject.toString()));
			
			axioms.add(factory.getOWLObjectPropertyRangeAxiom(property, cl));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLClassAssertionAxiom> retrieveClassAssertionAxioms(OWLClass cl){
		logger.info("Retrieving classAssertion axioms for class " + cl);
		queryMonitor.start();
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?s", OWLVocabulary.RDF_TYPE, cl.toStringID(),
				null, RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLClassAssertionAxiom> axioms = new HashSet<OWLClassAssertionAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		OWLNamedIndividual individual;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?s");
			
			individual = factory.getOWLNamedIndividual(IRI.create(rdfNodeSubject.toString()));
			
			axioms.add(factory.getOWLClassAssertionAxiom(cl, individual));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLAxiom> retrieveAxioms(OWLClass cl){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(retrieveSubClassAxioms(cl));
		axioms.addAll(retrievePropertyDomainAxioms(cl));
		axioms.addAll(retrievePropertyRangeAxioms(cl));
		
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
			sb.append(" FILTER ");
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
