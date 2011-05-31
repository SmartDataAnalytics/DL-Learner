package org.dllearner.algorithm.tbsl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class MultithreadedSPARQLQueryExecutor {
	
	private int threadCount;
	private SparqlEndpoint endpoint;
	
	private ExecutorService es;
	
	public MultithreadedSPARQLQueryExecutor(SparqlEndpoint endpoint) {
		this(endpoint, Runtime.getRuntime().availableProcessors());
	}
	
	public MultithreadedSPARQLQueryExecutor(SparqlEndpoint endpoint, int threadCount) {
		this.endpoint = endpoint;
		this.threadCount = threadCount;
		
		es = Executors.newFixedThreadPool(threadCount);
	}
	
	public List<ResultSet> executeQueries(List<String> queries){
		List<ResultSet> result = new ArrayList<ResultSet>();
		
		Future<ResultSet>[] ret = new Future[queries.size()];
		
		for(int i = 0; i < queries.size(); i++){
			ret[i] = es.submit(new SPARQLQueryExecutionTask(queries.get(i)));
		}
		
		for (int i = 0; i < queries.size(); i++) {
            try {
            	result.add(ret[i].get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
            	e.printStackTrace();
            }
        }
		
		return result;
	}
	
	public void close(){
		es.shutdown();
	}
	
	private class SPARQLQueryExecutionTask implements Callable<ResultSet>{
		
		private String query;
		
		public SPARQLQueryExecutionTask(String query){
			this.query = query;
		}

		@Override
		public ResultSet call() throws Exception {
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				queryExecution.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				queryExecution.addNamedGraph(ngu);
			}
			
			ResultSet rs = null;
			if(query.contains("SELECT")){
				rs = queryExecution.execSelect();
			} else if(query.contains("ASK")){
				queryExecution.execAsk();
			}
			return rs;
		}
	}

}
