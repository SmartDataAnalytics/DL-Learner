package org.autosparql.server.search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.autosparql.shared.Example;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.InvalidFileFormatException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class TBSLSearch implements Search{
	
	private static final String OPTIONS_FILE = "org/autosparql/server/tbsl.properties";
	
	private static final int LIMIT = 10;
	private static final int OFFSET = 0;
	
	private static final String QUERY_PREFIX = "Give me all ";
	
	private SPARQLTemplateBasedLearner tbsl;
	private SparqlEndpoint endpoint;
	
	public TBSLSearch(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		try {
			tbsl = new SPARQLTemplateBasedLearner(this.getClass().getClassLoader().getResource(OPTIONS_FILE).getPath());
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getResources(String query) {
		return getResources(query, LIMIT);
	}

	@Override
	public List<String> getResources(String query, int limit) {
		return getResources(query, limit, OFFSET);
	}

	@Override
	public List<String> getResources(String query, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		
		tbsl.setEndpoint(endpoint);
		tbsl.setQuestion(QUERY_PREFIX + query);
		try {
			tbsl.learnSPARQLQueries();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		//get SPARQL query which returned result, if exists
		String learnedQuery = tbsl.getBestSPARQLQuery();
		
		
		return resources;
	}

	@Override
	public List<Example> getExamples(String query) {
		return getExamples(query, LIMIT, OFFSET);
	}

	@Override
	public List<Example> getExamples(String query, int limit) {
		return getExamples(query, limit, OFFSET);
	}

	@Override
	public List<Example> getExamples(String query, int limit, int offset) {
		List<Example> examples = new ArrayList<Example>();
		
		tbsl.setEndpoint(endpoint);
		tbsl.setQuestion(QUERY_PREFIX + query);
		try {
			tbsl.learnSPARQLQueries();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		//get SPARQL query which returned result, if exists
		String learnedQuery = tbsl.getBestSPARQLQuery();
		
		
		return examples;
	}
	
	public List<String> getLexicalAnswerType(){
		for(Template t : tbsl.getTemplates()){
			if(t.getLexicalAnswerType() != null){
				return t.getLexicalAnswerType();
			}
		}
		return null;
	}
	
	private ResultSet executeQuery(String query){
		
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}

}
