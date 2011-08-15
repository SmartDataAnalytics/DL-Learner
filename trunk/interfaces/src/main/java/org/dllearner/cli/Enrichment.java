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
 *
 */
package org.dllearner.cli;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.aksw.commons.jena_owlapi.Conversion;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.CommonPrefixMap;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Command Line Interface for Enrichment.
 * 
 * @author Jens Lehmann
 * 
 */
public class Enrichment {

	private static Logger logger = Logger.getLogger(Enrichment.class);
	private DecimalFormat df = new DecimalFormat("##0.0");
	
	// enrichment parameters
	private SparqlEndpoint se;
	private Entity resource;
	private boolean verbose;

	// max. execution time for each learner for each entity
	private int maxExecutionTimeInSeconds = 10;

	// number of axioms which will be learned/considered (only applies to
	// some learners)
	private int nrOfAxiomsToLearn = 10;	
	
	// lists of algorithms to apply
	private List<Class<? extends AxiomLearningAlgorithm>> objectPropertyAlgorithms;
	private List<Class<? extends AxiomLearningAlgorithm>> dataPropertyAlgorithms;
	private List<Class<? extends LearningAlgorithm>> classAlgorithms;
	
	private CommonPrefixMap prefixes = new CommonPrefixMap();
	
	public Enrichment(SparqlEndpoint se, Entity resource, boolean verbose) {
		this.se = se;
		this.resource = resource;
		this.verbose = verbose;
		
		objectPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		objectPropertyAlgorithms.add(DisjointObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(EquivalentObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(FunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(InverseFunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyDomainAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyRangeAxiomLearner.class);
		objectPropertyAlgorithms.add(SubObjectPropertyOfAxiomLearner.class);
		objectPropertyAlgorithms.add(SymmetricObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(TransitiveObjectPropertyAxiomLearner.class);

		dataPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		dataPropertyAlgorithms.add(DisjointDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(EquivalentDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(FunctionalDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyDomainAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyRangeAxiomLearner.class); 
		dataPropertyAlgorithms.add(SubDataPropertyOfAxiomLearner.class);
		
		classAlgorithms = new LinkedList<Class<? extends LearningAlgorithm>>();
		classAlgorithms.add(DisjointClassesLearner.class);
		classAlgorithms.add(SimpleSubclassLearner.class);
		classAlgorithms.add(CELOE.class);		
	}
	
	@SuppressWarnings("unchecked")
	public void start() throws ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException, MalformedURLException {
		
		// sanity check that endpoint/graph returns at least one triple
		String query = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
		SparqlQuery sq = new SparqlQuery(query, se);
		ResultSet q = sq.send();
		while (q.hasNext()) {
			q.next();
		}
				
		// instantiate SPARQL endpoint wrapper component
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		ks.init();
		
		// common helper objects
		SPARQLTasks st = new SPARQLTasks(se);
		
		if(resource == null) {
			// TODO: automatically run over all resources if no specific resource was specified
			st.getAllClasses();
			st.getAllDataProperties();
			st.getAllObjectProperties();
		} else {
			if(resource instanceof ObjectProperty) {
				for (Class<? extends AxiomLearningAlgorithm> algorithmClass : objectPropertyAlgorithms) {
					applyLearningAlgorithm(algorithmClass, ks);
				}
			} else if(resource instanceof DatatypeProperty) {
				for (Class<? extends AxiomLearningAlgorithm> algorithmClass : dataPropertyAlgorithms) {
					applyLearningAlgorithm(algorithmClass, ks);
					
				}
			} else if(resource instanceof NamedClass) {
				for (Class<? extends LearningAlgorithm> algorithmClass : classAlgorithms) {
					if(algorithmClass == CELOE.class) {
						applyCELOE(ks, false);
						applyCELOE(ks, true);
					} else {
//						applyLearningAlgorithm((Class<AxiomLearningAlgorithm>)algorithmClass, ks);
					}
				}				
			} else {
				throw new Error("The type " + resource.getClass() + " of resource " + resource + " cannot be handled by this enrichment tool.");
			}
		}
	}
	
	private List<EvaluatedAxiom> applyCELOE(SparqlEndpointKS ks, boolean equivalence) throws ComponentInitException, LearningProblemUnsupportedException, MalformedURLException {
		SPARQLTasks st = new SPARQLTasks(se);
		
		// get instances of class as positive examples
		SPARQLReasoner sr = new SPARQLReasoner(ks);
		SortedSet<Individual> posExamples = sr.getIndividuals((NamedClass)resource, 50);
		SortedSet<String> posExStr = Helper.getStringSet(posExamples);
		
		// get negative examples via various strategies
		AutomaticNegativeExampleFinderSPARQL finder = new AutomaticNegativeExampleFinderSPARQL(posExStr, st, null);
		finder.makeNegativeExamplesFromNearbyClasses(posExStr, 50);
		finder.makeNegativeExamplesFromParallelClasses(posExStr, 50);
		finder.makeNegativeExamplesFromRelatedInstances(posExStr, "http://dbpedia.org/resource/");
		finder.makeNegativeExamplesFromSuperClasses(resource.getName(), 50);
//		finder.makeNegativeExamplesFromRandomInstances();
		SortedSet<String> negExStr = finder.getNegativeExamples(50, false);
		SortedSet<Individual> negExamples = Helper.getIndividualSet(negExStr);
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples, negExamples);
		
        ComponentManager cm = ComponentManager.getInstance();

        SparqlKnowledgeSource ks2 = cm.knowledgeSource(SparqlKnowledgeSource.class);
        ks2.getConfigurator().setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
        ks2.getConfigurator().setUrl(ks.getEndpoint().getURL());
        ks2.getConfigurator().setDefaultGraphURIs(new TreeSet<String>(ks.getEndpoint().getDefaultGraphURIs()));
        ks2.getConfigurator().setUseLits(false);
        ks2.getConfigurator().setUseCacheDatabase(true);
        ks2.getConfigurator().setRecursionDepth(1);
        ks2.getConfigurator().setCloseAfterRecursion(true);
//        ks2.getConfigurator().setSaveExtractedFragment(true);
        ks2.init();

        AbstractReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, ks2);
        rc.init();

        // TODO: super class learning
        ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, rc);
//        lp.setPositiveExamples(posExamples);
//        lp.setNegativeExamples(negExamples);
        lp.getConfigurator().setClassToDescribe(resource.getURI().toURL());
        lp.getConfigurator().setType("equivalence");
        lp.getConfigurator().setAccuracyMethod("fmeasure");
        lp.getConfigurator().setUseApproximations(false);
        lp.init();


        CELOE la = cm.learningAlgorithm(CELOE.class, lp, rc);
        CELOEConfigurator cc = la.getConfigurator();
        cc.setMaxExecutionTimeInSeconds(100);
        cc.setNoisePercentage(20);
        la.init();
        la.start();

        // convert the result to axioms (to make it compatible with the other algorithms)
        TreeSet<? extends EvaluatedDescription> learnedDescriptions = la.getCurrentlyBestEvaluatedDescriptions();
        List<EvaluatedAxiom> evaluatedAxioms = new LinkedList<EvaluatedAxiom>();
        for(EvaluatedDescription learnedDescription : learnedDescriptions) {
        	Axiom axiom;
        	if(equivalence) {
        		axiom = new EquivalentClassesAxiom((NamedClass) resource, learnedDescription.getDescription());
        	} else {
        		axiom = new SubClassAxiom((NamedClass) resource, learnedDescription.getDescription());
        	}
        	Score score = lp.computeScore(learnedDescription.getDescription());
        	evaluatedAxioms.add(new EvaluatedAxiom(axiom, score)); 
        }
        
        toRDF(evaluatedAxioms, la, ks);
        cm.freeAllComponents();		
		return evaluatedAxioms;
	}
	
	private List<EvaluatedAxiom> applyLearningAlgorithm(Class<? extends AxiomLearningAlgorithm> algorithmClass, SparqlEndpointKS ks) throws ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		AxiomLearningAlgorithm learner = algorithmClass.getConstructor(
				SparqlEndpointKS.class).newInstance(ks);
		if(classAlgorithms.contains(algorithmClass)) {
			ConfigHelper.configure(learner, "classToDescribe", resource);
		} else {
			ConfigHelper.configure(learner, "propertyToDescribe", resource);
		}
		ConfigHelper.configure(learner, "maxExecutionTimeInSeconds",
				maxExecutionTimeInSeconds);
		learner.init();
		String algName = ComponentManager.getName(learner);
		System.out.print("Applying " + algName + " on " + resource + " ... ");
		long startTime = System.currentTimeMillis();
		try {
			learner.start();
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getCause() instanceof SocketTimeoutException){
				System.out.println("Query timed out (endpoint possibly too slow).");
			}						
		}
		long runtime = System.currentTimeMillis() - startTime;
		System.out.println("done in " + runtime + "ms");
		List<EvaluatedAxiom> learnedAxioms = learner
				.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn);
		System.out.println(prettyPrint(learnedAxioms));	
		
		toRDF(learnedAxioms, learner, ks);
		return learnedAxioms;
	}
	
	private String prettyPrint(List<EvaluatedAxiom> learnedAxioms) {
		String str = "suggested axioms and their score in percent:\n";
		if(learnedAxioms.isEmpty()) {
			return "  no axiom suggested";
		} else {
			for (EvaluatedAxiom learnedAxiom : learnedAxioms) {
				str += " " + prettyPrint(learnedAxiom) + "\n";
			}		
		}
		return str;
	}
	
	private String prettyPrint(EvaluatedAxiom axiom) {
		double acc = axiom.getScore().getAccuracy() * 100;
		String accs = df.format(acc);
		if(acc<10d) { accs = " " + accs; }
		if(acc<100d) { accs = " " + accs; }
		String str =  accs + "%\t" + axiom.getAxiom().toManchesterSyntaxString(null, prefixes);
		return str;
	}
	
	/*
	 * Generates list of OWL axioms.
	 */
	private void toRDF(List<EvaluatedAxiom> evalAxioms, LearningAlgorithm algorithm, SparqlEndpointKS ks){
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
		
		OWLDataFactory f = new OWLDataFactoryImpl();
		
		String id = "http://dl-learner.org#enrichment";//TODO create a unique identifier for a particular suggestion
		OWLIndividual ind = f.getOWLNamedIndividual(IRI.create(id));
		
		OWLAxiom ax1 = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.Suggestion, ind);
		axioms.add(ax1);
		
		String algorithmName = "http://dl-learner.org#" + algorithm.getClass().getAnnotation(ComponentAnn.class).name().replace(" ", "_");
		OWLAxiom ax2 = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.usedAlgorithm,
				ind, f.getOWLNamedIndividual(IRI.create(algorithmName)));
		axioms.add(ax2);
		
		try {
			OWLAxiom ax3 = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.hasInput,
					ind, f.getOWLNamedIndividual(IRI.create(ks.getEndpoint().getURL())));
			axioms.add(ax3);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		for(EvaluatedAxiom evAx : evalAxioms){
			axioms.addAll(evAx.toRDF());
		}
		
		
		printManchesterOWLSyntax(axioms);
//		printTurtleSyntax(axioms);
//		printNTriplesSyntax(axioms);
	}
	
	/*
	 * Write axioms in Manchester OWL Syntax.
	 */
	private void printManchesterOWLSyntax(List<OWLAxiom> axioms){
		System.out.println("ENRICHMENT[");
		StringWriter sw = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer r = new ManchesterOWLSyntaxObjectRenderer(
				sw, new ManchesterOWLSyntaxPrefixNameShortFormProvider(new DefaultPrefixManager()));
		
		for(OWLAxiom ax : axioms){
			sw.getBuffer().setLength(0);
			ax.accept(r);
			System.out.println(sw.toString());
		}
		System.out.println("]");
	}
	
	/*
	 * Write axioms in Turtle syntax.
	 */
	private void printTurtleSyntax(List<OWLAxiom> axioms){
		try {
			System.out.println("ENRICHMENT[");
			Model model = ModelFactory.createDefaultModel();
			Conversion.OWLAPIOntology2JenaModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms)), model);
			model.write(System.out, "TURTLE");
			System.out.println("]");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Write axioms in Turtle syntax.
	 */
	private void printNTriplesSyntax(List<OWLAxiom> axioms){
		try {
			System.out.println("ENRICHMENT[");
			Model model = ModelFactory.createDefaultModel();
			Conversion.OWLAPIOntology2JenaModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms)), model);
			model.write(System.out, "N-TRIPLES");
			System.out.println("]");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException {
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.dllearner").setLevel(Level.WARN); // seems to be needed for some reason (?)
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(consoleAppender);
		
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("h", "?", "help"), "Show help.");
		parser.acceptsAll(asList("v", "verbose"), "Verbosity level.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
		parser.acceptsAll(asList("e", "endpoint"), "SPARQL endpoint URL to be used.")
				.withRequiredArg().ofType(URL.class);
		parser.acceptsAll(asList("g", "graph"),
				"URI of default graph for queries on SPARQL endpoint.").withOptionalArg()
				.ofType(URI.class);
		parser.acceptsAll(asList("r", "resource"),
				"The resource for which enrichment axioms should be suggested.").withOptionalArg().ofType(URI.class);
		parser.acceptsAll(asList("o", "output"), "Specify a file where the output can be written.")
				.withOptionalArg();
		parser.acceptsAll(asList("f", "format"),
				"Format of the generated output (plain, html, rdf).").withOptionalArg()
				.ofType(String.class).defaultsTo("plain");

		// parse options and display a message for the user in case of problems
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage() + ". Use -? to get help.");
			System.exit(0);
		}

		// print help screen
		if (options.has("?")) {
			parser.printHelpOn(System.out);
			String addHelp = "Additional explanations: The resource specified should " +
			"be a class, object \nproperty or data property. DL-Learner will try to " +
			"automatically detect its \ntype. If no resource is specified, DL-Learner will " +
			"generate enrichment \nsuggestions for all detected classes and properties in " + 
			"the given endpoint \nand graph. This can take several hours.";
			System.out.println();
			System.out.println(addHelp);
			// main script
		} else {			
			// create SPARQL endpoint object
			URL endpoint = (URL) options.valueOf("endpoint");
			URI graph = (URI) options.valueOf("graph");
			LinkedList<String> defaultGraphURIs = new LinkedList<String>();
			if(graph != null) {
				defaultGraphURIs.add(graph.toString());
			}
			SparqlEndpoint se = new SparqlEndpoint(endpoint, defaultGraphURIs, new LinkedList<String>());
			
			// map resource to correct type
			Entity resource = null;
			if(options.valueOf("resource") != null) {
				resource = new SPARQLTasks(se).guessResourceType(((URI)options.valueOf("resource")).toString());
				if(resource == null) {
					throw new IllegalArgumentException("Could not determine the type (class, object property or data property) of input resource " + options.valueOf("resource"));
				}
			}
			
			if(!options.hasArgument("endpoint")) {
				System.out.println("Please specify a SPARQL endpoint (using the -e option).");
			}
			
			boolean verbose = (Boolean) options.valueOf("v");
			
			Enrichment e = new Enrichment(se, resource, verbose);
			e.start();

			// TODO: print output in correct format
			
		}

	}
	

}
