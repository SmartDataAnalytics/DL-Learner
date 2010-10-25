package org.dllearner.autosparql.server;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class QueryTreeGenerator {
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	private QueryTreeFactoryImpl factory;
	
	private int maxTreeSize;
	
	private static final int MAX_TRIPLE_PER_QUERY = 1000;
	private static final int RECURSION_DEPTH = 2;
	
	public QueryTreeGenerator(ExtractionDBCache cache, SparqlEndpoint endpoint, int maxTreeSize){
		this.cache = cache;
		this.endpoint = endpoint;
		this.maxTreeSize = maxTreeSize;
		
		factory = new QueryTreeFactoryImpl();
	}
	
	public QueryTree<String> getQueryTree(String uri, Model model){
		QueryTree<String> tree = factory.getQueryTree(uri, model);
		
		return tree;
	}
	
	public QueryTree<String> getQueryTree(String uri){
		Model model = getModel(uri);
		QueryTree<String> tree = factory.getQueryTree(uri, model);
		
		return tree;
	}
	
	private Model getModel(String uri){
		Model all = ModelFactory.createDefaultModel();
		
		try {
			String query = buildConstructQuery(uri, MAX_TRIPLE_PER_QUERY, 0);
			System.out.println("Sending query: \n" + query);
			Model model = cache.executeConstructQuery(endpoint, query);
			System.out.println("Got " + model.size() + " new triple.");
			all.add(model);
			int i = 1;
			while(model.size() != 0 && all.size() < maxTreeSize){
				query = buildConstructQuery(uri, MAX_TRIPLE_PER_QUERY, i * MAX_TRIPLE_PER_QUERY);
				System.out.println("Sending query: \n" + query);
				model = cache.executeConstructQuery(endpoint, query);
				System.out.println("Got " + model.size() + " new triple.");
				all.add(model);
				i++;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return all;
	}
	
	private String buildConstructQuery(String uri, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(uri).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < RECURSION_DEPTH; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(uri).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < RECURSION_DEPTH; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		
		sb.append("FILTER (!regex (?p0, \"http://dbpedia.org/property\"))");
		sb.append("}\n");
		sb.append("ORDER BY ");
		for(int i = 0; i < RECURSION_DEPTH; i++){
			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
		}
		sb.append("\n");
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		
		return sb.toString();
	}

}
