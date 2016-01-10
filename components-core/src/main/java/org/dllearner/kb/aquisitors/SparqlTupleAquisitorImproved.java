package org.dllearner.kb.aquisitors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlTupleAquisitorImproved extends SparqlTupleAquisitor {
	
	private static Logger logger = Logger.getLogger(SparqlTupleAquisitorImproved.class);
	private Map<String,SortedSet<RDFNodeTuple>> resources = new HashMap<>();
	int recursionDepth;
	

	public SparqlTupleAquisitorImproved(SparqlQueryMaker sparqlQueryMaker, SPARQLTasks sparqlTasks, int recursionDepth) {
		super(sparqlQueryMaker, sparqlTasks);
		this.recursionDepth = recursionDepth;
		
	}
	
	@Override
	public SortedSet<RDFNodeTuple> retrieveTupel(String uri){
			
		SortedSet<RDFNodeTuple> cachedSet = resources.get(uri);
		if(cachedSet!=null) {
			return cachedSet;
			}
		
		//SortedSet<RDFNodeTuple> tmp = new TreeSet<RDFNodeTuple>();
		String sparqlQueryString = sparqlQueryMaker.makeSubjectQueryLevel(uri, recursionDepth);
		//System.out.println(sparqlQueryString);
		ResultSetRewindable rsw=null;
		try{
			rsw = sparqlTasks.queryAsResultSet(sparqlQueryString);
		}catch (Exception e) {
			return super.retrieveTupel(uri);
		}
			@SuppressWarnings("unchecked")
		List<QuerySolution> l = ResultSetFormatter.toList(rsw);
		rsw.reset();
		
		
		
		int resultsetcount = 0;
		int i = 0;
		for (QuerySolution binding : l) {
			i = 0;
			RDFNode nextOBJ = binding.get(OBJECT+i);
			RDFNode nextPRED = binding.get(PREDICATE+i);
			RDFNodeTuple tmptuple =  new RDFNodeTuple(nextPRED, nextOBJ );
			addToLocalCache(uri,tmptuple);
			
			boolean cont = !nextOBJ.isLiteral();
			for (i=1; (i < recursionDepth) && cont; i++) {
				RDFNode tmpPREDURI = binding.get(PREDICATE+i);
				RDFNode tmpOBJURI = binding.get(OBJECT+i);
				if(tmpOBJURI==null) {
					cont=false;
				}else if (tmpOBJURI.isLiteral()) {
					tmptuple =  new RDFNodeTuple(tmpPREDURI, tmpOBJURI );
					addToLocalCache(nextOBJ.toString(), tmptuple);
					//logger.trace(tmptuple);
					//logger.trace("For: "+nextOBJ.toString()+ " added :"+resources.get(nextOBJ.toString()));
					cont=false;
				}else {
					tmptuple =  new RDFNodeTuple(tmpPREDURI, tmpOBJURI );
					addToLocalCache(nextOBJ.toString(), tmptuple);
					//logger.trace(tmptuple);
					//logger.trace("For: "+nextOBJ.toString()+ " added :"+resources.get(nextOBJ.toString()));
					nextOBJ = tmpOBJURI;
					cont = true;
				}
			}//end for
			
			resultsetcount++;
		}
		
		//System.out.println("original count "+count);
		//logger.warn("SparqlTupelAquisitor retrieved : "+resultsetcount);
		if(resultsetcount>999) {
			logger.warn("SparqlTupelAquisitor retrieved more than 1000 results, there might some be missing");
		}
		return ((cachedSet=resources.get(uri))==null)?new TreeSet<RDFNodeTuple>():cachedSet;
	}
	
	@Override
	public SortedSet<RDFNodeTuple> retrieveTuplesForClassesOnly(String uri){
		int tmp = recursionDepth;
		recursionDepth=4;
		
		SortedSet<RDFNodeTuple> tmpSet = retrieveTupel(uri);
		recursionDepth = tmp;
		return tmpSet;
		//getQuery
		//String sparqlQueryString = sparqlQueryMaker.makeSubjectQueryUsingFilters(uri);
		//return  sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, PREDICATE, OBJECT);
	}
	
	
	
	
	@Override
	public SortedSet<RDFNodeTuple> retrieveClassesForInstances(String uri){
		// getQuery
		return super.retrieveClassesForInstances(uri);
		
	}
	
	private void addToLocalCache(String uri, RDFNodeTuple tuple){
		SortedSet<RDFNodeTuple> set = resources.get(uri);
	
		
		if(set==null){
			set = new TreeSet<>();
			set.add(tuple);
			resources.put(uri, set );
			
		}else {
			set.add(tuple);
		}
	}
	
	public void removeFromCache(String uri){
		resources.remove(uri);
	}



}
