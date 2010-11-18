package org.dllearner.autosparql.server.cache;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class DBModelCacheImpl {
	
	protected SparqlEndpoint endpoint;
	
	private MessageDigest md5;
	
	public DBModelCacheImpl(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	protected int getResourcesCount(){
		String query = "SELECT COUNT(DISTINCT ?object) WHERE {?object a ?class}";
		
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}
		
		ResultSet rs = queryExecution.execSelect();
		
		int cnt = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		
		return cnt;
	}
	
	protected Set<String> getResources(int limit, int offset){
		Set<String> resources = new HashSet<String>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ?object WHERE {?object a ?class. ");
		sb.append("FILTER(regex(?object, 'http://dbpedia.org/resource/')) }");
		sb.append(" LIMIT ").append(limit);
		sb.append(" OFFSET ").append(offset);
		
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), sb.toString());
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}
		
		ResultSet rs = queryExecution.execSelect();
		
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("object").isURIResource()){
				resources.add(qs.getResource("object").getURI());
			}
		}
		
		return resources;
	}
	
	protected byte[] md5(String string) {
		md5.reset();
		md5.update(string.getBytes());
		return md5.digest();
	}
	
	protected String convertModel2String(Model model){
		String modelStr = "";
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			model.write(baos, "N-TRIPLE");
			modelStr = baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return modelStr;
	}

}
