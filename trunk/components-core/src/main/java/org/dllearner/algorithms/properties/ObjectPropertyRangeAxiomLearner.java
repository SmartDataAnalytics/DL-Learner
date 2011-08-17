package org.dllearner.algorithms.properties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AxiomLearningAlgorithm;
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
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtendedQueryEngineHTTP;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="objectproperty range learner", shortName="oplrange", version=0.1)
public class ObjectPropertyRangeAxiomLearner extends AbstractComponent implements AxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyRangeAxiomLearner.class);
	
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
	
	public ObjectPropertyRangeAxiomLearner(SparqlEndpointKS ks){
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
		//get existing range
		Description existingRange = reasoner.getRange(propertyToDescribe);
		logger.debug("Existing range: " + existingRange);
		
		//get objects with types
		Map<Individual, SortedSet<NamedClass>> individual2Types = new HashMap<Individual, SortedSet<NamedClass>>();
		boolean repeat = true;
		int limit = 1000;
		while(!terminationCriteriaSatisfied() && repeat){
			int ret = addIndividualsWithTypes(individual2Types, limit, fetchedRows);
			currentlyBestAxioms = buildEvaluatedAxioms(individual2Types);
			fetchedRows += 1000;
			repeat = (ret == limit);
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
	
	private List<EvaluatedAxiom> buildEvaluatedAxioms(Map<Individual, SortedSet<NamedClass>> individual2Types){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Map<NamedClass, Integer> result = new HashMap<NamedClass, Integer>();
		for(Entry<Individual, SortedSet<NamedClass>> entry : individual2Types.entrySet()){
			for(NamedClass nc : entry.getValue()){
				Integer cnt = result.get(nc);
				if(cnt == null){
					cnt = Integer.valueOf(1);
				} else {
					cnt = Integer.valueOf(cnt + 1);
				}
				result.put(nc, cnt);
			}
		}
		
		EvaluatedAxiom evalAxiom;
		for(Entry<NamedClass, Integer> entry : sortByValues(result)){
			evalAxiom = new EvaluatedAxiom(new ObjectPropertyRangeAxiom(propertyToDescribe, entry.getKey()),
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
	
	private int addIndividualsWithTypes(Map<Individual, SortedSet<NamedClass>> ind2Types, int limit, int offset){
		String query = String.format("SELECT DISTINCT ?ind ?type WHERE {?s <%s> ?ind. ?ind a ?type.} LIMIT %d OFFSET %d", propertyToDescribe.getName(), limit, offset);
		
//		String query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a ?type. {SELECT ?ind {?ind <%s> ?o.} LIMIT %d OFFSET %d}}", propertyToDescribe.getName(), limit, offset);
		
		ResultSet rs = executeQuery(query);
		Individual ind;
		NamedClass newType;
		QuerySolution qs;
		SortedSet<NamedClass> types;
		int cnt = 0;
		while(rs.hasNext()){
			cnt++;
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			newType = new NamedClass(qs.getResource("type").getURI());
			types = ind2Types.get(ind);
			if(types == null){
				types = new TreeSet<NamedClass>();
				ind2Types.put(ind, types);
			}
			types.add(newType);
		}
		return cnt;
	}
	
	/*
	 * Executes a SELECT query and returns the result.
	 */
	private ResultSet executeQuery(String query){
		logger.info("Sending query \n {}", query);
		
		ExtendedQueryEngineHTTP queryExecution = new ExtendedQueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}
	
	public static void main(String[] args) throws Exception{
		ObjectPropertyRangeAxiomLearner l = new ObjectPropertyRangeAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/aircraftElectronic"));
		l.setMaxExecutionTimeInSeconds(0);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}
