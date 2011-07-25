package org.autosparql.server.search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.InvalidFileFormatException;

public class TBSLSearch implements Search{
	
	private static final String OPTIONS_FILE = "org/autosparql/server/tbsl.properties";
	private static final int LIMIT = 10;
	private static final int OFFSET = 0;
	
	private SPARQLTemplateBasedLearner tbsl;
	private SparqlEndpoint endpoint;
	
	public TBSLSearch(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		System.out.println(this.getClass().getClassLoader().getResource(OPTIONS_FILE).getPath());
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
		tbsl.setQuestion(query);
		System.out.println("set question");
		try {
			tbsl.learnSPARQLQueries();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		
		return resources;
	}

	@Override
	public List<String> getExamples(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getExamples(String query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getExamples(String query, int limit, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

}
