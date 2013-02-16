package org.dllearner.algorithm.tbsl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class PopularityMap {
	
	public enum EntityType {
		CLASS, PROPERTY, RESOURCE
	}
	
	private SparqlEndpoint endpoint;
	private Model model;
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
	
	public PopularityMap(String file, Model model) {
		this.file = file;
		this.model = model;
	}
	
	public void init() {
		boolean deserialized = deserialize();
		if(!deserialized){
			// load popularity of classes
			for (NamedClass nc : new SPARQLTasks(endpoint).getAllClasses()) {
				System.out.println("Computing popularity for " + nc);
				int popularity = loadPopularity(nc.getName(), EntityType.CLASS);
				class2Popularity.put(nc.getName(), Integer.valueOf(popularity));
			}
			// load popularity of properties
			for (ObjectProperty op : new SPARQLTasks(endpoint).getAllObjectProperties()) {
				System.out.println("Computing popularity for " + op);
				int popularity = loadPopularity(op.getName(), EntityType.PROPERTY);
				property2Popularity.put(op.getName(), Integer.valueOf(popularity));
			}
			for (DatatypeProperty dp : new SPARQLTasks(endpoint).getAllDataProperties()) {
				System.out.println("Computing popularity for " + dp);
				int popularity = loadPopularity(dp.getName(), EntityType.PROPERTY);
				property2Popularity.put(dp.getName(), Integer.valueOf(popularity));
			}
			serialize();
		}
	}
	
	private void serialize(){
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(file)));
			List<Map<String, Integer>> mapList = new ArrayList<Map<String,Integer>>();
			mapList.add(class2Popularity);
			mapList.add(property2Popularity);
			mapList.add(resource2Popularity);
			oos.writeObject(mapList);
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
				List<Map<String, Integer>> mapList = (List<Map<String, Integer>>) ois.readObject();
				class2Popularity = mapList.get(0);
				property2Popularity = mapList.get(1);
				resource2Popularity = mapList.get(2);
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
			System.out.println("Loaded popularity map.");
			return true;
		} 
		return false;
	}
	
	private int loadPopularity(String uri, EntityType entityType){
		String query;
		if(entityType == EntityType.CLASS){
			query = String.format("SELECT COUNT(?s) WHERE {?s a <%s>}", uri);
		} else if(entityType == EntityType.PROPERTY){
			query = String.format("SELECT COUNT(*) WHERE {?s <%s> ?o}", uri);
		} else {
			query = String.format("SELECT COUNT(*) WHERE {?s ?p <%s>}", uri);
		}
		int pop = 0;
		ResultSet rs;
		if(endpoint != null){
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		} else {
			rs = QueryExecutionFactory.create(query, model).execSelect();
		}
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
			if(popularity == null){
				popularity = loadPopularity(uri, entityType);
				class2Popularity.put(uri, popularity);
			}
		} else if(entityType == EntityType.PROPERTY){
			popularity = property2Popularity.get(uri);
			if(popularity == null){
				popularity = loadPopularity(uri, entityType);
				property2Popularity.put(uri, popularity);
			}
		} else {
			popularity = resource2Popularity.get(uri);
			if(popularity == null){
				popularity = loadPopularity(uri, entityType);
				resource2Popularity.put(uri, popularity);
			}
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
		PopularityMap map = new PopularityMap("dbpedia_popularity.map", SparqlEndpoint.getEndpointDBpediaLiveAKSW(), new ExtractionDBCache("cache"));
		map.init();
		System.out.println(map.getPopularity("http://dbpedia.org/ontology/Book"));
	}

}
