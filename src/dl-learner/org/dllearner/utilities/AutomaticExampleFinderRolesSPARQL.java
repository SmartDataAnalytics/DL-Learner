package org.dllearner.utilities;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.ResultSet;

public class AutomaticExampleFinderRolesSPARQL {

	private static Logger logger = Logger
			.getLogger(ComponentManager.class);
	
	private Cache c;
	private SparqlEndpoint se;
	private SortedSet<String> posExamples;
	private SortedSet<String> negExamples;
	private int roleLimit=1000;
	
	
	public AutomaticExampleFinderRolesSPARQL(SparqlEndpoint se){
		this.c=new Cache("cachetemp");
		this.se=se;
		posExamples = new TreeSet<String>();
		negExamples = new TreeSet<String>();
	}
	
	public void initDomainRange(String role, int poslimit, int neglimit) { 
		makePositiveExamplesAsDomain( role);
		SortedSet<String> keepForClean = new TreeSet<String>();
		keepForClean.addAll(this.posExamples);
		this.posExamples = SetManipulation.fuzzyShrink(this.posExamples, poslimit);
		logger.trace("shrinking: pos Example size: "+posExamples.size());
		
		makeNegativeExamplesAsRange( role);
		
		
		//clean
		negExamples.removeAll(keepForClean);
		logger.trace("neg Example size after cleaning: "+negExamples.size());
		this.negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);
		logger.debug("pos Example size after shrinking: "+posExamples.size());
		logger.debug("neg Example size after shrinking: "+negExamples.size());
		logger.debug("Finished examples for role: "+role);
	}
	
	
	
	
	private void makePositiveExamplesAsDomain(String role){
		logger.debug("making Positive Examples from Role as Domain: "+role);
		this.posExamples.addAll(getDomain( role, roleLimit));
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	private void makeNegativeExamplesAsRange(String role){
		logger.debug("making Negative Examples from Role as Range: "+role);
		this.negExamples.addAll(getRange( role, roleLimit));
		logger.debug("   neg Example size: "+negExamples.size());
	}
	
	private SortedSet<String> getDomain(String role,int limit){
		
		String query = "" +
		"SELECT DISTINCT ?domain " +
		"WHERE { \n" + 
		"?domain <" + role + "> " + " ?o. \n" +
		"?domain a []\n." +
		"FILTER (!isLiteral(?domain))." +
		"}\n" +
		"LIMIT "+limit;
	

		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return rsc.getStringListForVariable("domain");
	
	}
	
	private SortedSet<String> getRange(String role,int limit){
		
		String query = "" +
		"SELECT DISTINCT ?range " +
		"WHERE { \n" + 
		"?s <" + role + "> " + " ?range. \n" +
		"?range a [].\n" +
		"FILTER (!isLiteral(?range))." +
		"}\n" +
		"LIMIT "+limit;
	

		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return rsc.getStringListForVariable("range");
	
	}
	
	
	
	
	

	public SortedSet<String> getPosExamples() {
		return posExamples;
	}

	public SortedSet<String> getNegExamples() {
		return negExamples;
	}
	
	
	
	
	
	
}
