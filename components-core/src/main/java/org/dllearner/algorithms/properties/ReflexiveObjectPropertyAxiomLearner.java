package org.dllearner.algorithms.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ReflexiveObjectPropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtendedQueryEngineHTTP;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.OWL2;

@ComponentAnn(name="reflexive property axiom learner")
public class ReflexiveObjectPropertyAxiomLearner extends AbstractComponent implements AxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ReflexiveObjectPropertyAxiomLearner.class);
	
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
	

	public ReflexiveObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		//check if property is already declared as reflexive in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL2.ReflexiveProperty.getURI());
		boolean declaredAsReflexive = executeAskQuery(query);
		if(declaredAsReflexive) {
			logger.info("Property is already declared as reflexive in knowledge base.");
		}
		
		//get fraction of instances s with <s p o> also exists <o p s> 
		query = "SELECT (COUNT(?s)) AS ?all ,(COUNT(?o1)) AS ?reflexiv WHERE {?s <%s> ?o. OPTIONAL{?o <%s> ?o1.FILTER(?s=?o)}}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		ResultSet rs = executeQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			int all = qs.getLiteral("all").getInt();
			int reflexive = qs.getLiteral("reflexiv").getInt();
			if(all > 0){
				double frac = reflexive / (double)all;
				currentlyBestAxioms.add(new EvaluatedAxiom(new ReflexiveObjectPropertyAxiom(propertyToDescribe), new AxiomScore(frac)));
			}
			
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		return currentlyBestAxioms.isEmpty() ? Collections.<Axiom>emptyList() : Collections.singletonList(currentlyBestAxioms.get(0).getAxiom());
	}
	
	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		return currentlyBestAxioms;
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
	
	private boolean executeAskQuery(String query){
		logger.info("Sending query \n {}", query);
		
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		boolean result = queryExecution.execAsk();
		return result;
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
