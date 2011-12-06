package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import weka.attributeSelection.ConsistencySubsetEval;

import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.owlapiv3.OntologyUtils;
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
	
	private static final Logger logger = Logger.getLogger(ApproximateDebugging.class);
	
	private PelletReasoner reasoner;
	
	private OWLOntology schema;
	private OWLOntology data;
	private OWLOntology ontology;
	
	private Model model;
	private OWLDataFactory factory;
	private OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	
	static {PelletExplanation.setup();}
	
	public ApproximateDebugging(OWLOntology schema, OWLOntology data) throws OWLOntologyCreationException {
		this.schema = schema;
		this.data = data;
		
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(schema);
		ontologies.add(data);
		ontology = man.createOntology(IRI.create("http://merged.owl"), ontologies);
		
		model = convert(ontology);
		factory = new OWLDataFactoryImpl();
	}
	
	public Set<Set<OWLAxiom>> computeInconsistencyExplanations(){
		Set<Set<OWLAxiom>> allExplanations = new HashSet<Set<OWLAxiom>>();
		
		Set<Set<OWLAxiom>> explanations = computeInconsistencyExplanationsByPellet();
		allExplanations.addAll(explanations);
		logger.info("Computed " + explanations.size() + " explanations with Pellet.");
		
		explanations = computeInconsistencyExplanationsByPattern();
		allExplanations.addAll(explanations);
		logger.info("Computed " + explanations.size() + " explanations with anti-pattern.");
		
		logger.info("Computed overall " + allExplanations.size() + " explanations.");
		return allExplanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByPellet(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
//		man.removeAxioms(ontology, ontology.getAxioms(AxiomType.DISJOINT_CLASSES));
//		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
//		System.out.println(reasoner.isConsistent());
//		
//		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create("p"));
//		OWLIndividual s = factory.getOWLNamedIndividual(IRI.create("i"));
//		OWLIndividual o1 = factory.getOWLNamedIndividual(IRI.create("o1"));
//		OWLIndividual o2 = factory.getOWLNamedIndividual(IRI.create("o2"));
//		OWLAxiom axi = factory.getOWLObjectPropertyAssertionAxiom(prop, s, o1);
//		man.addAxiom(ontology, axi);
//		axi = factory.getOWLObjectPropertyAssertionAxiom(prop, s, o2);
//		man.addAxiom(ontology, axi);
//		axi = factory.getOWLFunctionalObjectPropertyAxiom(prop);
//		man.addAxiom(ontology, axi);
//		System.out.println(reasoner.isConsistent());
		
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		
//		try {
//			OWLOntology dummy = man.createOntology();
//			OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create("p"));
//			OWLIndividual s = factory.getOWLNamedIndividual(IRI.create("i"));
//			OWLIndividual o1 = factory.getOWLNamedIndividual(IRI.create("o1"));
//			OWLIndividual o2 = factory.getOWLNamedIndividual(IRI.create("o2"));
//			OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, s, o1);
//			manager.addAxiom(dummy, ax);
//			ax = factory.getOWLObjectPropertyAssertionAxiom(prop, s, o2);
//			manager.addAxiom(dummy, ax);
//			ax = factory.getOWLFunctionalObjectPropertyAxiom(prop);
//			manager.addAxiom(dummy, ax);
//			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(dummy);
//			System.out.println(reasoner.isConsistent());
//			PelletExplanation expGen = new PelletExplanation(reasoner);
//			System.out.println(expGen.getInconsistencyExplanation());
//		} catch (OWLOntologyCreationException e) {
//			e.printStackTrace();
//		}
		
		
		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(schema);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
		
		PelletExplanation expGen = new PelletExplanation(reasoner);
		
		//we first compute the unsatisfiable classes and unsatisfiable object properties here
		logger.info("Computing unsatisfiable classes...");
		long startTime = System.currentTimeMillis();
//		Set<OWLClass> unsatClasses = Collections.emptySet();
		Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
		logger.info("#Unsatisfiable classes: " + unsatClasses.size());
		logger.info("Computing unsatisfiable object properties...");
		startTime = System.currentTimeMillis();
		Set<OWLObjectProperty> unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
		logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
		logger.info("#Unsatisfiable object properties: " + unsatObjectProperties.size());
		
		//we keep only the unsatisfiable classes which are contained in the extracted module
		unsatClasses.retainAll(data.getClassesInSignature());
		//we check if there are instances asserted to each of the unsatisfiable classes, but only 
		for(OWLClass unsatClass : unsatClasses){
			logger.info(unsatClass);
			//we compute the justifications
			logger.info("Extracting module...");
			startTime = System.currentTimeMillis();
			Set<OWLAxiom> module = ModularityUtils.extractModule(schema, Collections.singleton((OWLEntity)unsatClass), ModuleType.TOP_OF_BOT);
			logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("Module size: " + module.size());
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(OntologyUtils.getOntologyFromAxioms(module));
			expGen = new PelletExplanation(reasoner);
			logger.info("Computing explanations...");
			startTime = System.currentTimeMillis();
			Set<Set<OWLAxiom>> temp = expGen.getUnsatisfiableExplanations(unsatClass, 10);
			logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("#Explanations: " + temp.size());
			//we get all asserted instances
			Set<OWLIndividual> individuals = getAssertedIndividuals(unsatClass);
			for(Set<OWLAxiom> t : temp){
				for(OWLIndividual ind : individuals){
					Set<OWLAxiom> explanation = new HashSet<OWLAxiom>(t);
					explanation.add(factory.getOWLClassAssertionAxiom(unsatClass, ind));
					explanations.add(explanation);
				}
			}
		}
		
		//we keep only the unsatisfiable object properties which are contained in the extracted module
		unsatObjectProperties.retainAll(data.getObjectPropertiesInSignature());
		
		for(OWLObjectProperty unsatProp : unsatObjectProperties){
			logger.info(unsatProp);
			logger.info("Extracting module...");
			startTime = System.currentTimeMillis();
			Set<OWLAxiom> module = ModularityUtils.extractModule(schema, Collections.singleton((OWLEntity)unsatProp), ModuleType.TOP_OF_BOT);
			logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("Module size: " + module.size());
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(OntologyUtils.getOntologyFromAxioms(module));
			expGen = new PelletExplanation(reasoner);
			logger.info("Computing explanations...");
			startTime = System.currentTimeMillis();
			Set<Set<OWLAxiom>> temp = expGen.getUnsatisfiableExplanations(factory.getOWLObjectExactCardinality(1, unsatProp), 50);
			logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("#Explanations: " + temp.size());
			//we get all property assertions
			Set<OWLObjectPropertyAssertionAxiom> assertions = getObjectPropertyAssertions(unsatProp);
			for(Set<OWLAxiom> t : temp){
				for(OWLObjectPropertyAssertionAxiom ax : assertions){
					Set<OWLAxiom> explanation = new HashSet<OWLAxiom>(t);
					explanation.add(ax);
					explanations.add(explanation);
				}
			}
		}
		
		
		return explanations;
	}
	
	public Set<Set<OWLAxiom>> computeExplanationsDefault(int limit){
		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(schema);
		reasoner.isConsistent();
		logger.info("Computing inconsistency explanations with only Pellet...");
		long startTime = System.currentTimeMillis();
		PelletExplanation expGen = new PelletExplanation(reasoner);
		Set<Set<OWLAxiom>> explanations = expGen.getInconsistencyExplanations(limit);
		logger.info("done in " + (System.currentTimeMillis()-startTime) + "ms.");
		logger.info("#Explanations: " + explanations.size());
		return explanations;
	}
	
	private Set<OWLObjectProperty> getUnsatisfiableObjectProperties(PelletReasoner reasoner){
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(OWLObjectProperty p : reasoner.getRootOntology().getObjectPropertiesInSignature()){
//			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectExactCardinality(1, p));
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectSomeValuesFrom(p, factory.getOWLThing()));
			if(!satisfiable){
				properties.add(p);
			}
		}
		return properties;
	}
	
	private Set<OWLIndividual> getAssertedIndividuals(OWLClass cls){
		Set<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
		
		String queryString = "SELECT * WHERE {?s a <cls>}".replace("cls", cls.toStringID());
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution qs = results.next();
				OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
				individuals.add(subject);
			}
		} finally {
			qexec.close();
		}
		
		return individuals;
	}
	
	private Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertions(OWLObjectProperty property){
		Set<OWLObjectPropertyAssertionAxiom> assertions = new TreeSet<OWLObjectPropertyAssertionAxiom>();
		
		String queryString = "SELECT * WHERE {?s <prop> ?o}".replace("prop", property.toStringID());
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution qs = results.next();
				OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
				OWLIndividual object = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
				assertions.add(factory.getOWLObjectPropertyAssertionAxiom(property, subject, object));
			}
		} finally {
			qexec.close();
		}
		
		return assertions;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		explanations.addAll(computeInconsistencyExplanationsByFunctionalityPattern());
		explanations.addAll(computeInconsistencyExplanationsByIrreflexivityPattern());
		explanations.addAll(computeInconsistencyExplanationsByAsymmetryPattern());
		explanations.addAll(computeInconsistencyExplanationsByIndividualsAssertedToDisjointClassesPattern());
		
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByFunctionalityPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)){
			OWLAxiom axiom = factory.getOWLFunctionalObjectPropertyAxiom(prop);
			String queryString = "SELECT * WHERE {?s <%s> ?o1. ?s <%s> ?o2. FILTER(?o1 != ?o2)}".replace("%s", prop.toStringID());
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
			String queryString = "SELECT * WHERE {?s <%s> ?o. ?o <%s> ?s. FILTER(?o != ?s)}".replace("%s", prop.toStringID());
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
			String queryString = "SELECT * WHERE {?s <%s> ?s.}".replace("%s", prop.toStringID());
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
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByIndividualsAssertedToDisjointClassesPattern(){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
		for(OWLDisjointClassesAxiom axiom : ontology.getAxioms(AxiomType.DISJOINT_CLASSES)){
			OWLClass dis1 = axiom.getClassExpressionsAsList().get(0).asOWLClass();
			OWLClass dis2 = axiom.getClassExpressionsAsList().get(1).asOWLClass();
			
			String queryString = "SELECT * WHERE {?s a <cls1>. ?s a <cls2>}".replace("cls1", dis1.toStringID()).replace("cls2", dis2.toStringID());
			Query query = QueryFactory.create(queryString) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					Set<OWLAxiom> explanation = new HashSet<OWLAxiom>();
					explanation.add(axiom);
					QuerySolution qs = results.nextSolution();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLAxiom ax = factory.getOWLClassAssertionAxiom(dis1, subject);
					explanation.add(ax);
					ax = factory.getOWLClassAssertionAxiom(dis2, subject);
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
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		Logger.getRootLogger().addAppender(new FileAppender(new SimpleLayout(), "log/approx_debug.log"));
		
		PelletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
		String resource = "http://dbpedia.org/resource/Brad_Pitt";
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.aksw.org:8902/sparql"),
				Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, new ExtractionDBCache("cache"));
//		ConciseBoundedDescriptionGenerator cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(endpoint, new ExtractionDBCache("cache"));
		Model model = cbdGen.getConciseBoundedDescription(resource, 3);
		OWLOntology data = convert(model);
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("src/main/resources/dbpedia_0.75_no_datapropaxioms.owl"));
		
		ApproximateDebugging debug = new ApproximateDebugging(schema, data);
		Set<Set<OWLAxiom>> explanations1 = debug.computeInconsistencyExplanations();
		Set<Set<OWLAxiom>> explanations2 = debug.computeExplanationsDefault(500);
		System.out.println(explanations1.size());
		System.out.println(explanations2.size());
	}

}
