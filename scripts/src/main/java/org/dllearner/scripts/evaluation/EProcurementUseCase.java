/**
 * 
 */
package org.dllearner.scripts.evaluation;

import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SIBLING;
import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SUPERCLASS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.elcopy.ELLearningAlgorithm;
import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Buehmann
 *
 */
public class EProcurementUseCase {
	
	
	private static final Logger logger = Logger.getLogger(EProcurementUseCase.class.getName());
	static final int maxNrOfPositiveExamples = 100;
	static final int maxNrOfNegativeExamples = 200;
	static boolean posOnly = false;
	static int maxCBDDepth = 2;
	static int maxNrOfResults = 100;
	static int maxExecutionTimeInSeconds = 200;
	static double noiseInPercentage = 50;
	static boolean useNegation = false;
	static boolean useAllConstructor = false;
	static String testFolder = "logs/eprocurement";
	
	static boolean useEL = false;
	private static int maxClassExpressionDepth = 2;
	
	static Map<String, String> prefixes = new HashMap<String, String>();;
	static {
		prefixes.put("pc", "http://purl.org/procurement/public-contracts#");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("skos", "http://www.w3.org/2004/02/skos/core#");
		prefixes.put("dcterms", "http://purl.org/dc/terms/");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("activities", "http://purl.org/procurement/public-contracts-activities#");
		prefixes.put("gr", "http://purl.org/goodrelations/v1#");
		prefixes.put("schema", "http://schema.org/");
	
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		NamedClass posClass = new NamedClass("http://purl.org/procurement/public-contracts#SuccessfulTender");
		NamedClass negClass = new NamedClass("http://purl.org/procurement/public-contracts#UnsuccessfulTender");
		
		//1. setup the knowledge base
		Model model = ModelFactory.createDefaultModel();
		//the data
		model.read(new FileInputStream("../test/eprocurement/dl-learner-sample-with-classes-pco.rdf"), null);
		//the schema
		Model schema = ModelFactory.createDefaultModel();
		schema.read(new FileInputStream("../test/eprocurement/pco.rdf"), null);
		schema.add(schema.getResource("http://purl.org/procurement/public-contracts#SuccessfulTender"), 
				OWL.disjointWith, 
				schema.getResource("http://purl.org/procurement/public-contracts#UnsuccessfulTender"));
		schema.add(schema.getResource("http://purl.org/procurement/public-contracts#SuccessfulTender"), 
				RDFS.subClassOf, 
				schema.getResource("http://purl.org/procurement/public-contracts#Tender"));
		schema.add(schema.getResource("http://purl.org/procurement/public-contracts#UnsuccessfulTender"), 
				RDFS.subClassOf, 
				schema.getResource("http://purl.org/procurement/public-contracts#Tender"));
//		schema.read(new URL("http://opendata.cz/pco/public-contracts.ttl").openStream(), null, "TURTLE");
		model.add(schema);
		// get positive examples
		SortedSet<Individual> positiveExamples = getExamples(model, posClass);
		// get negative examples
//		SortedSet<Individual> negativeExamples = getNegativeExamples(model, cls, positiveExamples);
		SortedSet<Individual> negativeExamples = getExamples(model, negClass);
		//get the lgg of the pos. examples
//		showLGG(model, positiveExamples);
		// build a sample of the kb
		model = getSample(model, Sets.union(positiveExamples, negativeExamples));
		//add inferred entity types
		OWLEntityTypeAdder.addEntityTypes(model);
		//the ontology
		model.add(schema);
		//convert all into DL-Learner kb object
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "TURTLE");
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		
		
		//2. setup the reasoner
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		
		//3. setup the learning problem
		AbstractLearningProblem lp;
//		lp = new ClassLearningProblem(rc);
//		((ClassLearningProblem)lp).setClassToDescribe(cls);
//		((ClassLearningProblem)lp).setEquivalence(true);
		lp = new PosNegLPStandard(rc, positiveExamples, negativeExamples);
		lp.init();
		
		//4. setup the learning algorithm
		AbstractCELA la;
		if(useEL){
			la = new ELLearningAlgorithm(lp, rc);
			((ELLearningAlgorithm)la).setNoisePercentage(noiseInPercentage);
//			((ELLearningAlgorithm)la).setStartClass(startClass);
			((ELLearningAlgorithm)la).setIgnoredConcepts(Sets.newHashSet(posClass));
			((ELLearningAlgorithm)la).setClassToDescribe(posClass);
			((ELLearningAlgorithm)la).setTreeSearchTimeSeconds(maxExecutionTimeInSeconds);
			((ELLearningAlgorithm)la).setMaxNrOfResults(maxNrOfResults);
			((ELLearningAlgorithm)la).setMaxClassExpressionDepth(maxClassExpressionDepth );
//			la = new ELLearningAlgorithmDisjunctive(lp, reasoner);
		} else {
			//set up the refinement operator and the allowed OWL constructs
			RhoDRDown rop = new RhoDRDown();
			rop.setReasoner(rc);
			rop.setUseNegation(useNegation);
			rop.setUseAllConstructor(useAllConstructor);
			rop.init();
			//build CELOE la
			CELOE laTmp = new CELOE(lp, rc);
			laTmp.setMaxNrOfResults(maxNrOfResults);
			laTmp.setOperator(rop);
			laTmp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
//			laTmp.setStartClass(startClass);
			laTmp.setNoisePercentage(noiseInPercentage);
			new File(testFolder).mkdirs();
			laTmp.setSearchTreeFile(testFolder  + "searchTree.txt");
			laTmp.setWriteSearchTree(true);
//			isle.setTerminateOnNoiseReached(true);
			laTmp.setIgnoredConcepts(Collections.singleton(posClass));
			laTmp.setReplaceSearchTree(true);
			laTmp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
			laTmp.setExpandAccuracy100Nodes(true);
			la = laTmp;
		}
		la.init();
		
		//5. run
		la.start();
		

	}
	
	private static void showLGG(Model model, SortedSet<Individual> positiveExamples){
		LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGen.getLGG(buildTrees(model, positiveExamples));
		String s = lgg.getStringRepresentation();
		for (Entry<String, String> entry : prefixes.entrySet()) {
			s = s.replace(entry.getValue(), entry.getKey() + ":");
		}
		System.out.println(s);
		((QueryTreeImpl<String>) lgg).asGraph();
	}
	
	private static SortedSet<Individual> getExamples(Model model, NamedClass cls){
		logger.info("Generating examples...");
		SortedSet<Individual> individuals = new SPARQLReasoner(new LocalModelBasedSparqlEndpointKS(model)).getIndividuals(cls, 1000);
		List<Individual> individualsList = new ArrayList<>(individuals);
//		Collections.shuffle(individualsList, new Random(1234));
		individuals.clear();
		individuals.addAll(individualsList.subList(0, Math.min(maxNrOfPositiveExamples, individualsList.size())));
		logger.info("Done. Got " + individuals.size() + ": " + individuals);
		return individuals;
	}
	
	private static SortedSet<Individual> getPositiveExamples(Model model, NamedClass cls){
		logger.info("Generating positive examples...");
		SortedSet<Individual> individuals = new SPARQLReasoner(new LocalModelBasedSparqlEndpointKS(model)).getIndividuals(cls, 1000);
		List<Individual> individualsList = new ArrayList<>(individuals);
//		Collections.shuffle(individualsList, new Random(1234));
		individuals.clear();
		individuals.addAll(individualsList.subList(0, Math.min(maxNrOfPositiveExamples, individualsList.size())));
		logger.info("Done. Got " + individuals.size() + ": " + individuals);
		return individuals;
	}
	
	private static SortedSet<Individual> getNegativeExamples(Model model, NamedClass classToDescribe, Set<Individual> positiveExamples){
		logger.info("Generating positive examples...");
		SortedSet<Individual> individuals = new AutomaticNegativeExampleFinderSPARQL2(new SPARQLReasoner(new LocalModelBasedSparqlEndpointKS(model))).getNegativeExamples(classToDescribe, positiveExamples, Arrays.asList(SIBLING, SUPERCLASS), maxNrOfNegativeExamples);
		logger.info("Done. Got " + individuals.size() + ": " + individuals);
		return individuals;
	}
	
	private static Model getSample(Model model, Individual individual){
		logger.info("Generating sample...");
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(model, maxCBDDepth);
		Model sample = cbdGen.getConciseBoundedDescription(individual.getName(), maxCBDDepth, true);
		logger.info("Done. Got " + sample.size() + " triples.");
		return sample;
	}
	
	private static Model getSample(Model model, Set<Individual> individuals){
		logger.info("Generating sample...");
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(model, maxCBDDepth);
		Model sample = ModelFactory.createDefaultModel();
		Model cbd;
		for (Individual individual : individuals) {
//			System.out.println("##########################");
//			System.out.println(individual);
			try {
				cbd = cbdGen.getConciseBoundedDescription(individual.getName(), maxCBDDepth, true);
//				showTree(individual, model);
				sample.add(cbd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("Done. Got " + sample.size() + " triples.");
		return sample;
	}
	
	private static QueryTree<String> buildTree(Individual ind, Model model){
		QueryTreeFactory<String> qf = new QueryTreeFactoryImpl();
		QueryTreeImpl<String> queryTree = qf.getQueryTree(ind.getName(), model);
		return queryTree;
	}
	
	private static List<QueryTree<String>> buildTrees(Model model, Collection<Individual> individuals){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		for (Individual individual : individuals) {
			trees.add(buildTree(individual, getSample(model, individual)));
		}
		return trees;
	}
	
	private static void showTree(Individual ind, Model model){
		QueryTree<String> tree = buildTree(ind, model);
		String s = tree.getStringRepresentation();
		
		for (Entry<String, String> entry : prefixes.entrySet()) {
			s = s.replace(entry.getValue(), entry.getKey() + ":");
		}
		System.out.println(s);
	}
	

}
