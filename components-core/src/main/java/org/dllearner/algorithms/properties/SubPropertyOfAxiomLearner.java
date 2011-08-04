package org.dllearner.algorithms.properties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

@ComponentAnn(name="subPropertyOf learner")
public class SubPropertyOfAxiomLearner extends AbstractComponent implements AxiomLearningAlgorithm {
	
private static final Logger logger = LoggerFactory.getLogger(PropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxExecutionTimeInSeconds", description="", propertyEditorClass=IntegerEditor.class)
	private int maxExecutionTimeInSeconds = 10;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private SPARQLReasoner reasoner;
	private SparqlEndpointKS ks;
	
	private List<EvaluatedAxiom> currentlyBestAxioms;
	private long startTime;
	private int fetchedRows;
	
	public SubPropertyOfAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public ObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	public int getMaxFetchedRows() {
		return maxFetchedRows;
	}

	public void setMaxFetchedRows(int maxFetchedRows) {
		this.maxFetchedRows = maxFetchedRows;
	}

	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		//get existing super properties
		SortedSet<ObjectProperty> existingSuperProperties = reasoner.getSuperProperties(propertyToDescribe);
		logger.debug("Existing super properties: " + existingSuperProperties);
		
		//get subjects with types
		int limit = 1000;
		int offset = 0;
		String queryTemplate = "SELECT ?p (COUNT(?s)) AS ?count WHERE {?s ?p ?o." +
		"{SELECT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d}" +
		"}";
		String query;
		Map<ObjectProperty, Integer> result = new HashMap<ObjectProperty, Integer>();
		ObjectProperty prop;
		Integer oldCnt;
		while(!terminationCriteriaSatisfied()){
			query = String.format(queryTemplate, propertyToDescribe, limit, offset);
			ResultSet rs = executeQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				prop = new ObjectProperty(qs.getResource("p").getURI());
				int newCnt = qs.getLiteral("count").getInt();
				oldCnt = result.get(prop);
				if(oldCnt == null){
					oldCnt = Integer.valueOf(newCnt);
				}
				result.put(prop, oldCnt);
				qs.getLiteral("count").getInt();
			}
			offset += 1000;
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		List<Axiom> bestAxioms = new ArrayList<Axiom>();
		
		Iterator<EvaluatedAxiom> it = currentlyBestAxioms.iterator();
		while(bestAxioms.size() < nrOfAxioms && it.hasNext()){
			bestAxioms.add(it.next().getAxiom());
		}
		
		return bestAxioms;
	}
	
	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		int max = Math.min(currentlyBestAxioms.size(), nrOfAxioms);
		
		List<EvaluatedAxiom> bestAxioms = currentlyBestAxioms.subList(0, max);
		
		return bestAxioms;
	}

	@Override
	public Configurator getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws ComponentInitException {
		reasoner = new SPARQLReasoner(ks);
		
	}
	
	private boolean terminationCriteriaSatisfied(){
		boolean timeLimitExceeded = maxExecutionTimeInSeconds == 0 ? false : (System.currentTimeMillis() - startTime) >= maxExecutionTimeInSeconds * 1000;
		boolean resultLimitExceeded = maxFetchedRows == 0 ? false : fetchedRows >= maxFetchedRows;
		return  timeLimitExceeded || resultLimitExceeded; 
	}
	
	private List<EvaluatedAxiom> buildBestAxioms(Map<Individual, Set<NamedClass>> individual2Types){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Map<NamedClass, Integer> result = new HashMap<NamedClass, Integer>();
		for(Entry<Individual, Set<NamedClass>> entry : individual2Types.entrySet()){
			for(NamedClass nc : entry.getValue()){
				Integer cnt = result.get(nc);
				if(cnt == null){
					cnt = Integer.valueOf(1);
				}
				result.put(nc, Integer.valueOf(cnt + 1));
			}
		}
		
		EvaluatedAxiom evalAxiom;
		for(Entry<NamedClass, Integer> entry : sortByValues(result)){
			evalAxiom = new EvaluatedAxiom(new ObjectPropertyDomainAxiom(propertyToDescribe, entry.getKey()),
					new AxiomScore(entry.getValue() / (double)individual2Types.keySet().size()));
			axioms.add(evalAxiom);
		}
		
		return axioms;
	}
	
	/*
	 * Returns the entries of the map sorted by value.
	 */
	private SortedSet<Entry<NamedClass, Integer>> sortByValues(Map<NamedClass, Integer> map){
		SortedSet<Entry<NamedClass, Integer>> sortedSet = new TreeSet<Map.Entry<NamedClass,Integer>>(new Comparator<Entry<NamedClass, Integer>>() {

			@Override
			public int compare(Entry<NamedClass, Integer> value1, Entry<NamedClass, Integer> value2) {
				if(value1.getValue() < value2.getValue()){
					return 1;
				} else if(value2.getValue() < value1.getValue()){
					return -1;
				} else {
					return value1.getKey().compareTo(value2.getKey());
				}
			}
		});
		sortedSet.addAll(map.entrySet());
		return sortedSet;
	}
	
	/*
	 * Executes a SELECT query and returns the result.
	 */
	private ResultSet executeQuery(String query){
		logger.info("Sending query \n {}", query);
		
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}
	
}
