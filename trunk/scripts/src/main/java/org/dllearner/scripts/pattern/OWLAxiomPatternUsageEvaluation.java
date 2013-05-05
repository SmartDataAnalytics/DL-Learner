package org.dllearner.scripts.pattern;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.aksw.commons.util.Pair;
import org.apache.log4j.Logger;
import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxOWLParser;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.Score;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.DLLearnerAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class OWLAxiomPatternUsageEvaluation {
	
	
	private static final Logger logger = Logger.getLogger(OWLAxiomPatternUsageEvaluation.AxiomTypeCategory.class
			.getName());
	
	enum AxiomTypeCategory{
		TBox, RBox, ABox
	}
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private Connection conn;
	
	private ExtractionDBCache cache = new ExtractionDBCache("pattern-cache");
	private SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia(), cache);//new LocalModelBasedSparqlEndpointKS(model);
	private String ns = "http://dbpedia.org/ontology/";
	
	private boolean fancyLatex = false;
	private DecimalFormat format = new DecimalFormat("00.0%");
	private long waitingTime = TimeUnit.SECONDS.toMillis(3);
	private double threshold = 0.6;
	private OWLAnnotationProperty confidenceProperty = df.getOWLAnnotationProperty(IRI.create("http://dl-learner.org/pattern/confidence"));

	public OWLAxiomPatternUsageEvaluation() {
		initDBConnection();
	}
	
	private void initDBConnection() {
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("db_settings.ini");
			Preferences prefs = new IniPreferences(is);
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, cache);
		
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		
		//get the axiom patterns to evaluate
		List<OWLAxiom> patterns = getPatternsToEvaluate();
		
		//get all classes in KB
		Set<NamedClass> classes = reasoner.getTypes(ns);
		
		//for each pattern
		for (OWLAxiom pattern : patterns) {
			if(pattern.isOfType(AxiomType.SUBCLASS_OF)){
				logger.info("Processing " + pattern + "...");
				Set<EvaluatedAxiom> evaluatedAxioms = new HashSet<EvaluatedAxiom>();
				Map<OWLAxiom, Score> axioms2Score = new LinkedHashMap<OWLAxiom, Score>();
				OWLClassExpression patternSubClass = ((OWLSubClassOfAxiom)pattern).getSubClass();
				OWLClassExpression superClass = ((OWLSubClassOfAxiom)pattern).getSuperClass();
				//for each class
				int i = 1;
				for (NamedClass cls : classes) {
					logger.info("Processing " + cls + "...");
					//set the subclass as a class from the KB
					OWLClass subClass = df.getOWLClass(IRI.create(cls.getName()));
					
					//1. count number of instances in subclass expression
					Query query = QueryFactory.create("SELECT (COUNT(DISTINCT ?x) AS ?cnt) WHERE {" + converter.convert("?x", subClass) + "}",Syntax.syntaxARQ);
					int subClassCnt = executeSelectQuery(query).next().getLiteral("cnt").getInt(); 
					
					//2. count number of instances in subclass AND superclass expression
					//we have 2 options here to evaluate the whole axiom pattern:
					//a) we replace all entities in the signature of the super class expression(except the subclass) with variables 
					//and GROUP BY them
					//b) we replace only 1 entity with a variable, thus we have to try it for several combinations
//					for (OWLEntity entity : signature) {
//						//replace current entity with variable and for the rest use existing entities in KB
//						query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(subClass, superClass), signature);
//					}
					Set<OWLEntity> signature = superClass.getSignature();
					signature.remove(subClass);
					query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(subClass, superClass), signature);
					query.setLimit(100);
					Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();System.out.println(query);
					com.hp.hpl.jena.query.ResultSet rs = executeSelectQuery(query);
					QuerySolution qs;
					while(rs.hasNext()){
						qs = rs.next();
						//get the IRIs for each variable
						Map<OWLEntity, IRI> entity2IRIMap = new HashMap<OWLEntity, IRI>();
						entity2IRIMap.put(patternSubClass.asOWLClass(), subClass.getIRI());
						for (OWLEntity entity : signature) {
							String var = variablesMapping.get(entity);
							Resource resource = qs.getResource(var);
							entity2IRIMap.put(entity, IRI.create(resource.getURI()));
						}
						//instantiate the pattern
						OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, df);
						OWLAxiom patternInstantiation = duplicator.duplicateObject(pattern);
						int patternInstantiationCnt = qs.getLiteral("cnt").getInt();
						//compute score
						Score score;
						try {
							score = computeScore(subClassCnt, patternInstantiationCnt);
							axioms2Score.put(patternInstantiation, score);
							logger.info(patternInstantiation + "(" + format.format(score.getAccuracy()) + ")");
						} catch (IllegalArgumentException e) {
							//sometimes Virtuosos returns 'wrong' cnt values such that the success number as bigger than the total number of instances
							e.printStackTrace();
						}
						
//						//convert into EvaluatedAxiom such we can serialize it as RDF with accuracy value as annotation
//						EvaluatedAxiom evaluatedAxiom = new EvaluatedAxiom(DLLearnerAxiomConvertVisitor.getDLLearnerAxiom(patternInstantiation), score);
//						evaluatedAxioms.add(evaluatedAxiom);
						
					}
					//wait some time to avoid flooding of endpoint
					try {
						Thread.sleep(waitingTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					if(i++ == 3) break;
				}
				save(axioms2Score);
			}
		}
	}
	
	private void save(Map<OWLAxiom, Score> axioms2Score){
		try {
			Set<OWLAxiom> annotatedAxioms = new HashSet<OWLAxiom>();
			for (Entry<OWLAxiom, Score> entry : axioms2Score.entrySet()) {
				OWLAxiom axiom = entry.getKey();
				Score score = entry.getValue();
				if(score.getAccuracy() >= threshold){
					annotatedAxioms.add(axiom.getAnnotatedAxiom(Collections.singleton(df.getOWLAnnotation(confidenceProperty, df.getOWLLiteral(score.getAccuracy())))));
					
				}
			}
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(annotatedAxioms);
			man.saveOntology(ontology, new TurtleOntologyFormat(), new FileOutputStream("pattern.ttl"));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void save(Set<EvaluatedAxiom> evaluatedAxioms){
		try {
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			for (EvaluatedAxiom evaluatedAxiom : EvaluatedAxiom.getBestEvaluatedAxioms(evaluatedAxioms, threshold)) {
				axioms.addAll(evaluatedAxiom.toRDF("http://dl-learner.org/pattern/").values().iterator().next());
			}
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(axioms);
			man.saveOntology(ontology, new TurtleOntologyFormat(), new FileOutputStream("pattern.ttl"));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public List<OWLAxiom> getPatternsToEvaluate(){
		List<OWLAxiom> axiomPatterns = new ArrayList<OWLAxiom>();
		
		Map<OWLAxiom, Pair<Integer, Integer>> topNAxiomPatterns = getTopNAxiomPatterns(AxiomTypeCategory.TBox, 10);
		axiomPatterns.addAll(topNAxiomPatterns.keySet());
		
		return axiomPatterns;
	}
	
	
	private String asLatex(String title, Map<OWLAxiom, Pair<Integer, Integer>> topN){
		String latexTable = "\\begin{table}\n";
		latexTable += "\\begin{tabular}{lrr}\n";
		latexTable += "\\toprule\n";
		latexTable += "Pattern & Frequency & \\#Ontologies\\\\\\midrule\n";
		
		for (Entry<OWLAxiom, Pair<Integer, Integer>> entry : topN.entrySet()) {
			OWLAxiom axiom = entry.getKey();
			Integer frequency = entry.getValue().getKey();
			Integer idf = entry.getValue().getValue();
			
			if(axiom != null){
				String axiomColumn = axiomRenderer.render(axiom);
				if(fancyLatex){
					axiomColumn = "\\begin{lstlisting}[language=manchester]" + axiomColumn + "\\end{lstlisting}";
				}
				latexTable += axiomColumn + " & " + frequency + " & " + idf + "\\\\\n";
			}
		}
		latexTable += "\\bottomrule\n\\end{tabular}\n";
		latexTable += "\\caption{" + title + "}\n";
		latexTable += "\\end{table}\n";
		return latexTable;
	}
	
	private Map<OWLAxiom, Pair<Integer, Integer>> getTopNAxiomPatterns(AxiomTypeCategory axiomType, int n){
		Map<OWLAxiom, Pair<Integer, Integer>> topN = new LinkedHashMap<OWLAxiom, Pair<Integer, Integer>>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT pattern,SUM(occurrences),COUNT(ontology_id) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, axiomType.name());
			ps.setInt(2, n);
			rs = ps.executeQuery();
			while(rs.next()){
				topN.put(asOWLAxiom(rs.getString(1)), new Pair<Integer, Integer>(rs.getInt(2), rs.getInt(3)));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	protected com.hp.hpl.jena.query.ResultSet executeSelectQuery(Query query) {
		com.hp.hpl.jena.query.ResultSet rs = null;
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtractionDBCache cache = ks.getCache();
			if(cache != null){
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
			} else {
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
						query);
				queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
				queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
				try {
					rs = queryExecution.execSelect();
					return rs;
				} catch (QueryExceptionHTTP e) {
					if(e.getCause() instanceof SocketTimeoutException){
						logger.warn("Got timeout");
					} else {
						logger.error("Exception executing query", e);
					}
				}
			}
			
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			rs = queryExecution.execSelect();
		}
		return rs;
	}
	
	protected com.hp.hpl.jena.query.ResultSet executeSelectQuery(Query query, boolean cached) {
		com.hp.hpl.jena.query.ResultSet rs = null;
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtractionDBCache cache = ks.getCache();
			if(cache != null && cached){
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
			} else {
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
						query);
				queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
				queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
				try {
					rs = queryExecution.execSelect();
					return rs;
				} catch (QueryExceptionHTTP e) {
					if(e.getCause() instanceof SocketTimeoutException){
						logger.warn("Got timeout");
					} else {
						logger.error("Exception executing query", e);
					}
				}
			}
			
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			rs = queryExecution.execSelect();
		}
		return rs;
	}
	
	private Score computeScore(int total, int success){
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		
		double accuracy = (confidenceInterval[0] + confidenceInterval[1]) / 2;
	
		double confidence = confidenceInterval[1] - confidenceInterval[0];
		
		return new AxiomScore(accuracy, confidence, total, success, total-success);
	}
	
	private OWLAxiom asOWLAxiom(String functionalSyntaxAxiomString){
		try {
			StringDocumentSource s = new StringDocumentSource("Ontology(<http://www.pattern.org> " + functionalSyntaxAxiomString + ")");
			OWLFunctionalSyntaxOWLParser p = new OWLFunctionalSyntaxOWLParser();
			OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology();
			p.parse(s, newOntology);
			if(!newOntology.getLogicalAxioms().isEmpty()){
				return newOntology.getLogicalAxioms().iterator().next();
			}
		} catch (UnloadableImportException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLParserException e) {
			System.err.println("Parsing failed for axiom " + functionalSyntaxAxiomString);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		new OWLAxiomPatternUsageEvaluation().run();
	}
	

}
