package org.dllearner.sparqlquerygenerator;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.operations.Generalisation;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class GeneralisationTest {
	
	private static final int RECURSION_DEPTH = 2;
    private int maxModelSizePerExample = 3000;
	private final static int LIMIT = 1000;
	private final static int OFFSET = 1000;
	private static final String ENDPOINT_URL = "http://dbpedia.org/sparql";
	
	@Test
	public void generalisationTest1(){
		String resource = "http://dbpedia.org/resource/Leipzig";
		
		Generalisation<String> gen = new Generalisation<String>();
		Model model = getModelForExample(resource, maxModelSizePerExample);
		QueryTree<String> tree = new QueryTreeFactoryImpl().getQueryTree(resource, model);
		System.out.println(tree.toSPARQLQueryString());
		QueryTree<String> genTree = gen.generalise(tree);
		String query = genTree.toSPARQLQueryString();
		System.out.println(query);
	}
	
	private Model getModelForExample(String example, int maxSize){
		Query query = makeConstructQuery(example, LIMIT, 0);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
		Model all = ModelFactory.createDefaultModel();
		Model model = qexec.execConstruct();
		all.add(model);
		qexec.close();
		int i = 1;
		while(model.size() != 0 && all.size() < maxSize){
			query = makeConstructQuery(example, LIMIT, i * OFFSET);
			qexec = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
			model = qexec.execConstruct();
			all.add(model);
			qexec.close();
			i++;
		}
		return all;
	}
	
	private Query makeConstructQuery(String example, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(example).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < RECURSION_DEPTH; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(example).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < RECURSION_DEPTH; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		
		sb.append("FILTER (!regex (?p0, \"http://dbpedia.org/property/wikiPage\") && !regex(?p1, \"http://dbpedia.org/property/wikiPage\"))");
		sb.append("}\n");
		sb.append("ORDER BY ");
		for(int i = 0; i < RECURSION_DEPTH; i++){
			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
		}
		sb.append("\n");
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		Query query = QueryFactory.create(sb.toString());
		System.out.println(sb.toString());
		return query;
	}

}
