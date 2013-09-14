/**
 * 
 */
package org.dllearner.algorithms.pattern;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class IndividualBasedFragmentExtractor implements FragmentExtractor{
	
	public static final FragmentExtractionStrategy extractionStrategy = FragmentExtractionStrategy.INDIVIDUALS;
	private QueryExecutionFactory qef;
	
	private long maxNrOfIndividuals;
	private long startTime;
	
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	public IndividualBasedFragmentExtractor(SparqlEndpointKS ks, String cacheDir, int maxNrOfIndividuals) {
		this.maxNrOfIndividuals = maxNrOfIndividuals;
		
		SparqlEndpoint endpoint = ks.getEndpoint();
		
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDir != null){
			try {
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDir, timeToLive, true);
				CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, cacheDir);
	}
	
	public IndividualBasedFragmentExtractor(SparqlEndpointKS ks, int maxNrOfIndividuals) {
		this(ks, null, maxNrOfIndividuals);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.pattern.FragmentExtractor#extractFragment(org.dllearner.core.owl.NamedClass)
	 */
	@Override
	public Model extractFragment(NamedClass cls, int maxFragmentDepth) {
		startTime = System.currentTimeMillis();
		Model fragment = ModelFactory.createDefaultModel();
		
		//get some random individuals
		Set<Individual> individuals = getRandomIndividuals(cls);
		
		//get for each individual the CBD
		Model cbd;
		for (Individual ind : individuals) {
			cbd = cbdGen.getConciseBoundedDescription(ind.getName(), maxFragmentDepth);
			fragment.add(cbd);
		}
		return fragment;
	}
	
	private Set<Individual> getRandomIndividuals(NamedClass cls){
		Set<Individual> individuals = new HashSet<Individual>();
		
		String query = "SELECT ?s WHERE {?s a <" + cls.getName() + ">} LIMIT " + maxNrOfIndividuals;
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("s").isURIResource()){
				individuals.add(new Individual(qs.getResource("s").getURI()));
			}
			
		}
		qe.close();
		
		return individuals;
	}
}
