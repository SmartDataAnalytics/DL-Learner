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

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DataPropertyEditor;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.OWL2Datatype;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtendedQueryEngineHTTP;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="dataproperty range learner")
public class DataPropertyRangeAxiomLearner extends AbstractComponent implements AxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(DataPropertyRangeAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=DataPropertyEditor.class)
	private DatatypeProperty propertyToDescribe;
	@ConfigOption(name="maxExecutionTimeInSeconds", description="", propertyEditorClass=IntegerEditor.class)
	private int maxExecutionTimeInSeconds = 10;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private SPARQLReasoner reasoner;
	private SparqlEndpointKS ks;
	
	private List<EvaluatedAxiom> currentlyBestAxioms;
	private long startTime;
	private int fetchedRows;
	
	public DataPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public DatatypeProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(DatatypeProperty propertyToDescribe) {
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
		DataRange existingRange = reasoner.getRange(propertyToDescribe);
		logger.debug("Existing range: " + existingRange);
		
		//get objects with types
		Map<Individual, Set<Datatype>> individual2Types = new HashMap<Individual, Set<Datatype>>();
		Map<Individual, Set<Datatype>> newIndividual2Types;
		boolean repeat = true;
		while(!terminationCriteriaSatisfied() && repeat){
			newIndividual2Types = getObjectsWithDatatypes(fetchedRows);
			individual2Types.putAll(newIndividual2Types);
			currentlyBestAxioms = buildBestAxioms(individual2Types);
			fetchedRows += 1000;
			repeat = !newIndividual2Types.isEmpty();
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
	
	private List<EvaluatedAxiom> buildBestAxioms(Map<Individual, Set<Datatype>> individual2Types){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Map<Datatype, Integer> result = new HashMap<Datatype, Integer>();
		for(Entry<Individual, Set<Datatype>> entry : individual2Types.entrySet()){
			for(Datatype nc : entry.getValue()){
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
		for(Entry<Datatype, Integer> entry : sortByValues(result)){
			evalAxiom = new EvaluatedAxiom(new DatatypePropertyRangeAxiom(propertyToDescribe, entry.getKey()),
					new AxiomScore(entry.getValue() / (double)individual2Types.keySet().size()));
			axioms.add(evalAxiom);
		}
		
		return axioms;
	}
	
	/*
	 * Returns the entries of the map sorted by value.
	 */
	private SortedSet<Entry<Datatype, Integer>> sortByValues(Map<Datatype, Integer> map){
		SortedSet<Entry<Datatype, Integer>> sortedSet = new TreeSet<Map.Entry<Datatype,Integer>>(new Comparator<Entry<Datatype, Integer>>() {

			@Override
			public int compare(Entry<Datatype, Integer> value1, Entry<Datatype, Integer> value2) {
				if(value1.getValue() < value2.getValue()){
					return 1;
				} else if(value2.getValue() < value1.getValue()){
					return -1;
				} else {
					return value1.getKey().getURI().compareTo(value2.getKey().getURI());
				}
			}
		});
		sortedSet.addAll(map.entrySet());
		return sortedSet;
	}
	
	private Map<Individual, Set<Datatype>> getObjectsWithDatatypes(int offset){
		Map<Individual, Set<Datatype>> individual2Datatypes = new HashMap<Individual, Set<Datatype>>();
		int limit = 1000;
		String query = String.format("SELECT ?ind, (DATATYPE(?val) AS ?datatype) WHERE {?ind <%s> ?val.} LIMIT %d OFFSET %d", propertyToDescribe.getName(), limit, offset);
		ResultSet rs = executeQuery(query);
		QuerySolution qs;
		Individual ind;
		Set<Datatype> types;
		while(rs.hasNext()){
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			types = individual2Datatypes.get(ind);
			if(types == null){
				types = new HashSet<Datatype>();
				individual2Datatypes.put(ind, types);
			}
			types.add(new Datatype(qs.getResource("datatype").getURI()));
		}
		return individual2Datatypes;
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

}
