package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ApproximateDebugging {
	
	private PelletReasoner reasoner;
	private OWLOntology ontology;
	private Model model;
	private OWLDataFactory factory;
	
	static {PelletExplanation.setup();}
	
	public ApproximateDebugging(OWLOntology ontology) {
		this.ontology = ontology;System.out.println(ontology.getLogicalAxiomCount());
		
		model = convert(ontology);
		factory = new OWLDataFactoryImpl();
	}
	
	public void computeInconsistencyExplanations(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		explanations.addAll(computeInconsistencyExplanationsByPellet());
		explanations.addAll(computeInconsistencyExplanationsByPattern());
		
		System.out.println(explanations);
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByPellet(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.removeAxioms(ontology, ontology.getAxioms(AxiomType.DISJOINT_CLASSES));
		
		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
		reasoner.isConsistent();
		
		PelletExplanation expGen = new PelletExplanation(reasoner);
		System.out.println(expGen.getInconsistencyExplanation());
		
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		explanations.addAll(computeInconsistencyExplanationsByFunctionalityPattern());
		explanations.addAll(computeInconsistencyExplanationsByIrreflexivityPattern());
		explanations.addAll(computeInconsistencyExplanationsByAsymmetryPattern());
		
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByFunctionalityPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)){
			OWLAxiom axiom = factory.getOWLFunctionalObjectPropertyAxiom(prop);
			String queryString = "SELECT * WHERE {?s <%s> ?o1. ?s <%s> ?o2. FILTER(?o1 != ?o2)} LIMIT 1000".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					Set<OWLAxiom> explanation = new HashSet<OWLAxiom>();
					explanation.add(axiom);
					QuerySolution qs = results.next();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLIndividual object1 = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o1").getURI()));
					OWLIndividual object2 = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o2").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object1);
					explanation.add(ax);
					ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object2);
					explanation.add(ax);
					explanations.add(explanation);
				}
			} finally {
				qexec.close();
			}
		}
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByAsymmetryPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.ASYMMETRIC_OBJECT_PROPERTY)){
			OWLAxiom axiom = factory.getOWLAsymmetricObjectPropertyAxiom(prop);
			String queryString = "SELECT * WHERE {?s <%s> ?o. ?o <%s> ?s. FILTER(?o != ?s)} LIMIT 1000".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					Set<OWLAxiom> explanation = new HashSet<OWLAxiom>();
					explanation.add(axiom);
					QuerySolution qs = results.nextSolution();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLIndividual object = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object);
					explanation.add(ax);
					ax = factory.getOWLObjectPropertyAssertionAxiom(prop, object, subject);
					explanation.add(ax);
					explanations.add(explanation);
				}
			} finally {
				qexec.close();
			}
		}
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByIrreflexivityPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY)){
			OWLAxiom axiom = factory.getOWLIrreflexiveObjectPropertyAxiom(prop);
			String queryString = "SELECT * WHERE {?s <%s> ?s.} LIMIT 1000".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					Set<OWLAxiom> explanation = new HashSet<OWLAxiom>();
					explanation.add(axiom);
					QuerySolution qs = results.nextSolution();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, subject);
					explanation.add(ax);
					explanations.add(explanation);
				}
			} finally {
				qexec.close();
			}
		}
		return explanations;
	}
	
	private SortedSet<OWLObjectProperty> extractObjectProperties(AxiomType<? extends OWLAxiom> axiomType){
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		for(OWLAxiom ax : ontology.getAxioms(axiomType)){
			properties.add(((OWLObjectPropertyCharacteristicAxiom)ax).getProperty().asOWLObjectProperty());
		}
		return properties;
	}
	
	public static OWLOntology convert(Model model) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N-TRIPLE");
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology retOnt = null;
		try {
			retOnt = manager.loadOntologyFromOntologyDocument(bais);
		} catch (OWLOntologyCreationException e) {

		}
		return retOnt;
	}
	
	public static Model convert(OWLOntology ontology) {
		Model model = ModelFactory.createDefaultModel();
		ByteArrayInputStream bais = null;
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			man.saveOntology(ontology, new RDFXMLOntologyFormat(), baos);
			bais = new ByteArrayInputStream(baos.toByteArray());
			model.read(bais, null);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bais != null){
					bais.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return model;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PelletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
		String resource = "http://dbpedia.org/resource/Leipzig";
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.aksw.org:8902/sparql"),
				Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		
		ModelGenerator modelGen = new ModelGenerator(endpoint, new ExtractionDBCache("cache"));
		Model model = modelGen.createModel(resource, Strategy.CHUNKS, 4);
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File("src/main/resources/dbpedia_0.75_no_datapropaxioms.owl"));
		System.out.println(ontology.getLogicalAxiomCount());
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(ontology);
		ontologies.add(convert(model));
		ontology = man.createOntology(IRI.create("http://merged.owl"), ontologies, true);//man.addAxioms(ontology, convert(model).getLogicalAxioms());
		
		ApproximateDebugging debug = new ApproximateDebugging(ontology);
		debug.computeInconsistencyExplanations();
	}

}
