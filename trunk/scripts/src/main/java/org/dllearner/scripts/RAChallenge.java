/**
 * 
 */
package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.elcopy.ELLearningAlgorithm;
import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class RAChallenge {

	
	private static boolean useEL = false;
	
	static Map<String, String> prefixes = new HashMap<>();

	private static String baseURI = "http://bio2rdf.org/ra.challenge:";
	
	static {
		
//		prefixes.put("ra", "http://bio2rdf.org/ra.challenge:");
		prefixes.put("ra-voc", "http://bio2rdf.org/ra.challenge_vocabulary:");
		prefixes.put("dbsnp", "http://bio2rdf.org/dbsnp:");
		prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixes.put("foaf", "http://xmlns.com/foaf/0.1/");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("drug-voc", "http://bio2rdf.org/drugbank_vocabulary:");
		
		
	}

	public static void main(String[] args) throws Exception{
		//load the data
		File dataDir = new File(args[0]);
		File[] files = dataDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".nt") || name.endsWith(".ttl") || name.endsWith(".rdf") || name.endsWith(".owl");
			}
		});
		System.out.println("loading data...");
		Model model = ModelFactory.createDefaultModel();
		for (File file : files) {
			model.read(new FileInputStream(file), null, "TURTLE");
		}
		
		analyzeData(model);
		
		//get the positive and negative examples via SPARQL
		//<http://bio2rdf.org/ra.challenge:1877000> <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> "true"^^<http://www.w3.org/2001/XMLSchema#boolean>  .
		System.out.println("extracting pos/neg examples...");
		SortedSet<Individual> posExamples = new TreeSet<Individual>();
		SortedSet<Individual> negExamples = new TreeSet<Individual>();
		String query = "SELECT ?s WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>} limit 400";
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet rs = qe.execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			posExamples.add(new Individual(qs.getResource("s").getURI()));
		}
		query = "SELECT ?s WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>} limit 400";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		while(rs.hasNext()){
			qs = rs.next();
			negExamples.add(new Individual(qs.getResource("s").getURI()));
		}
		qe.close();
		System.out.println("#pos examples: " + posExamples.size());
		System.out.println("#neg examples: " + negExamples.size());
		
		//remove triples with property non-responder
		model.remove(model.listStatements(null, model.createProperty("http://bio2rdf.org/ra.challenge_vocabulary:non-responder"), (RDFNode)null));
		
		//enrich with additional data
		enrich(model);
		
		//check the LGG 
//		computeLGG(model, posExamples);
		
		//convert JENA model to OWL API ontology
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos , "N-TRIPLES");
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
		
		//init knowledge source
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		
		//init reasoner
		System.out.println("initializing reasoner...");
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
//		baseReasoner.setReasonerTypeString("elk");
		baseReasoner.init();
		FastInstanceChecker rc = new FastInstanceChecker(ks);
		rc.setReasonerComponent(baseReasoner);
		rc.setBaseURI(baseURI);
		rc.setPrefixes(prefixes);
		rc.init();
		
		//init learning problem
		System.out.println("initializing learning problem...");
		PosNegLPStandard lp = new PosNegLPStandard(rc, posExamples, negExamples);
		lp.setUseApproximations(true);
		lp.init();
		
		//init learning algorithm
		System.out.println("initializing learning algorithm...");
		AbstractCELA la;
		if(useEL){
			la = new ELLearningAlgorithm(lp, rc);
			((ELLearningAlgorithm) la).setNoisePercentage(30);
			((ELLearningAlgorithm) la).setMaxNrOfResults(50);
			((ELLearningAlgorithm) la).setTreeSearchTimeSeconds(10);
		} else {
			OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
			heuristic.setExpansionPenaltyFactor(0.1);
			la = new CELOE(lp, rc);
			((CELOE) la).setHeuristic(heuristic);
			((CELOE) la).setMaxExecutionTimeInSeconds(100);
			((CELOE) la).setNoisePercentage(50);
			((CELOE) la).setMaxNrOfResults(50);
			((CELOE) la).setWriteSearchTree(true);
			((CELOE) la).setReplaceSearchTree(true);
			((CELOE) la).setStartClass(new NamedClass("http://xmlns.com/foaf/0.1/Person"));
			RhoDRDown op = new RhoDRDown();
			op.setUseHasValueConstructor(true);
			op.setUseObjectValueNegation(true);
			op.setReasoner(rc);
			op.init();
//			((CELOE) la).setOperator(op);
		}
		la.init();
		
		la.start();
	}
	
	/**
	 * Do some statistical queries.
	 * @param model
	 */
	private static void analyzeData(Model model){
		String query = "SELECT (COUNT(DISTINCT ?s)AS ?cnt) WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o.} ";
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		query = "SELECT ?o (COUNT(?s) AS ?cnt)  WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
//				+ "OPTIONAL{?s_res <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>.FILTER(?s=s_res)} "
//				+ "?s_non_res <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>"
				+ "}"
				+ " GROUP BY ?o ORDER BY DESC(?cnt)";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		query = "SELECT ?o (COUNT(?s) AS ?cnt)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>."
				+ "}"
				+ " GROUP BY ?o ORDER BY DESC(?cnt)";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		query = "SELECT ?o (COUNT(?s) AS ?total) (Min(?cnt_res) as ?responder) (Min(?cnt_non_res) as ?non_responder)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "{SELECT ?o (COUNT(?s) AS ?cnt_res)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>."
				+ "} GROUP BY ?o}"
				+ "{SELECT ?o (COUNT(?s) AS ?cnt_non_res)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>."
				+ "}"
				+ " GROUP BY ?o}} GROUP BY ?o ORDER BY DESC(?total)";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
	}
	
	private static void computeLGG(Model model, SortedSet<Individual> posExamples){
		QueryTreeFactory<String> queryTreeFactory = new QueryTreeFactoryImpl();
		
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		for (Individual ex : posExamples) {
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex.getName(), model);
			posExampleTrees.add(tree);
		}
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		String lggString = lgg.getStringRepresentation(true);
		lggString = lggString.replace(baseURI, "");
		for (Entry<String, String> entry : prefixes.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			lggString = lggString.replace(value, key + ":");
		}
		System.out.println(lggString);
		
		OWLClassExpression classExpression = lgg.asOWLClassExpression();
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		ToStringRenderer.getInstance().setShortFormProvider(new SimpleShortFormProvider());
		System.out.println(classExpression);
	}
	
	private static void enrich(Model model) throws MalformedURLException, FileNotFoundException{
		System.out.println("enriching data...");
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://cu.drugbank.bio2rdf.org/sparql"));
		
//		StmtIterator stmtIterator = model.listStatements(null, RDF.type, (RDFNode)null);
//		Model classes = ModelFactory.createDefaultModel();
//		while(stmtIterator.hasNext()){
//			Statement st = stmtIterator.next();
//			classes.add(classes.createStatement(st.getObject().asResource(), RDF.type, OWL.Class));
//		}
//		classes.write(new FileOutputStream("classes.nt"), "TURTLE");
		
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, "cache/drugbank");
		List<RDFNode> drugs = model.listObjectsOfProperty(model.getProperty("http://bio2rdf.org/ra.challenge_vocabulary:drug")).toList();
		Model drugbankData = ModelFactory.createDefaultModel();
		Model cbd;
		for (RDFNode drug : drugs) {
			 cbd = cbdGen.getConciseBoundedDescription(drug.asResource().getURI(), 0, true);
			 drugbankData.add(cbd);
		}
		drugbankData.setNsPrefix("drug-voc", "http://bio2rdf.org/drugbank_vocabulary:");
		drugbankData.setNsPrefix("drug-res", "http://bio2rdf.org/drugbank_resource:");
		drugbankData.setNsPrefix("drug", "http://bio2rdf.org/drugbank:");
		
		drugbankData.write(new FileOutputStream("drugbank.ttl"), "TURTLE", null);
		model.add(drugbankData);
		
		
	}
}
