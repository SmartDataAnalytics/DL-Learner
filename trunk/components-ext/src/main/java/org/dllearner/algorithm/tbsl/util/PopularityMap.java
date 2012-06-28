package org.dllearner.algorithm.tbsl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class PopularityMap {
	
	enum EntityType {
		CLASS, PROPERTY, RESOURCE
	}
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;
	private String file;
	
	private Map<String, Integer> class2Popularity = new HashMap<String, Integer>();
	private Map<String, Integer> property2Popularity = new HashMap<String, Integer>();
	private Map<String, Integer> resource2Popularity = new HashMap<String, Integer>();
	
	public PopularityMap(String file, SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.file = file;
		this.endpoint = endpoint;
		this.cache = cache;
		
	}
	
	public void init() {
		boolean deserialized = deserialize();
		if(!deserialized){
			// load popularity of classes
			for (NamedClass nc : new SPARQLTasks(endpoint).getAllClasses()) {
				System.out.println("Computing popularity for " + nc);
				String query = String.format("SELECT COUNT(?s) WHERE {?s a <%s>}", nc.getName());
				int popularity = loadPopularity(query);
				class2Popularity.put(nc.getName(), Integer.valueOf(popularity));
			}
			// load popularity of properties
			for (ObjectProperty op : new SPARQLTasks(endpoint).getAllObjectProperties()) {
				System.out.println("Computing popularity for " + op);
				String query = String.format("SELECT COUNT(*) WHERE {?s <%s> ?o}", op.getName());
				int popularity = loadPopularity(query);
				class2Popularity.put(op.getName(), Integer.valueOf(popularity));
			}
			for (DatatypeProperty dp : new SPARQLTasks(endpoint).getAllDataProperties()) {
				System.out.println("Computing popularity for " + dp);
				String query = String.format("SELECT COUNT(*) WHERE {?s <%s> ?o}", dp.getName());
				int popularity = loadPopularity(query);
				class2Popularity.put(dp.getName(), Integer.valueOf(popularity));
			}
			serialize();
		}
	}
	
	private void serialize(){
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(file)));
			oos.writeObject(class2Popularity);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private boolean deserialize(){
		File mapFile = new File(file);
		if(mapFile.exists()){
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new FileInputStream(new File(file)));
				class2Popularity = (Map<String, Integer>) ois.readObject();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if(ois != null){
					try {
						ois.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
			return true;
		} 
		return false;
	}
	
	private int loadPopularity(String query){
		int pop = 0;
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		String projectionVar;
		while(rs.hasNext()){
			qs = rs.next();
			projectionVar = qs.varNames().next();
			pop = qs.get(projectionVar).asLiteral().getInt();
		}
		return pop;
	}
	
	public int getPopularity(String uri, EntityType entityType){
		Integer popularity;
		if(entityType == EntityType.CLASS){
			popularity = class2Popularity.get(uri);
		} else if(entityType == EntityType.PROPERTY){
			popularity = property2Popularity.get(uri);
		} else {
			popularity = resource2Popularity.get(uri);
		}
		return popularity;
	}
	
	public Integer getPopularity(String uri){
		Integer popularity  = class2Popularity.get(uri);
		if(popularity == null){
			popularity = property2Popularity.get(uri);
		}
		if(popularity == null){
			popularity = resource2Popularity.get(uri);
		}
		return popularity;
	}
	
	public static void main(String[] args) {
		new PopularityMap("dbpedia_popularity.map", SparqlEndpoint.getEndpointDBpedia(), new ExtractionDBCache("cache")).init();
	}

}
