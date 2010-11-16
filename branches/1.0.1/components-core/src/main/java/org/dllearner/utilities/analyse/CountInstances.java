package org.dllearner.utilities.analyse;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.EndpointBasedSPARQLTasks;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class CountInstances {
	
		SPARQLTasks t;
		
		public CountInstances( String url , List<String> defaultGraphs){
			try{
			 t = new EndpointBasedSPARQLTasks(Cache.getDefaultCache(),
					 new SparqlEndpoint(new URL(url), defaultGraphs, new ArrayList<String>()));
			}catch (Exception e) {
				e.printStackTrace();	
			}
			
		}
		
		public class Count implements Comparable<Count>{
			public Count(String uri, int count) {
				super();
				this.uri = uri;
				this.count = count;
			}
			String uri;
			int count;
			@Override
			public String toString(){
				return count+"\t"+uri;
			}
			@Override
			public int compareTo(Count o) {
				if(this.count == o.count) {
					return this.uri.compareTo(o.uri);
				}
				return o.count -this.count ;
			}

		}
		
		

		public static void main(String[] args) {
		}

		@SuppressWarnings("unchecked")
		public List<Count> countProperties() {
			List<Count> ret = new ArrayList<Count>();
			String query = "SELECT ?p count(?p) as ?count {" + 
						"?s ?p ?o" +
						"} GROUP BY ?p " +
						"ORDER by DESC(?count)" + "";
			ResultSetRewindable rsw = t.queryAsResultSet(query);
			final List<QuerySolution> l = ResultSetFormatter.toList(rsw);
			for (QuerySolution resultBinding : l) {
				Resource res = (Resource)resultBinding.get("p");
				Literal lit =  (Literal) resultBinding.get("count");
				
				ret.add(new Count(res.getURI(), lit.getInt()));
			}
			return ret;
		}
		
		@SuppressWarnings("unchecked")
		public List<Count> countInstances(String property, String namespace) {
			List<Count> ret = new ArrayList<Count>();
			String query = 	"SELECT ?class count(?instance) as ?count {" 
				+ "?instance <"+property+"> ?class ." +
				"FILTER (?class LIKE <"+namespace+"%>) " 
				+ "}" +
				"GROUP BY ?class "
				+ "ORDER by DESC(?count)" + "";
			ResultSetRewindable rsw = t.queryAsResultSet(query);
			final List<QuerySolution> l = ResultSetFormatter.toList(rsw);
			for (QuerySolution resultBinding : l) {
				String res = resultBinding.get("class").toString();
				Literal lit =  (Literal) resultBinding.get("count");
				
				ret.add(new Count(res, lit.getInt()));
			}
			System.out.println("retrieved: "+ret.size());
			return ret;
		}
	}


