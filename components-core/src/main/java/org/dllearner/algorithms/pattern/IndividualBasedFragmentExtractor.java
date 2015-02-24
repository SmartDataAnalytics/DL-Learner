/**
 * 
 */
package org.dllearner.algorithms.pattern;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

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
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	public IndividualBasedFragmentExtractor(SparqlEndpointKS ks, String cacheDir, int maxNrOfIndividuals) {
		this.maxNrOfIndividuals = maxNrOfIndividuals;
		
		SparqlEndpoint endpoint = ks.getEndpoint();
		
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDir != null){
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(cacheDir, true, timeToLive);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
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
	public Model extractFragment(OWLClass cls, int maxFragmentDepth) {
		startTime = System.currentTimeMillis();
		Model fragment = ModelFactory.createDefaultModel();
		
		//get some random individuals
		Set<OWLIndividual> individuals = getRandomIndividuals(cls);
		
		//get for each individual the CBD
		Model cbd;
		for (OWLIndividual ind : individuals) {
			cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), maxFragmentDepth);
			fragment.add(cbd);
		}
		return fragment;
	}
	
	private Set<OWLIndividual> getRandomIndividuals(OWLClass cls){
		Set<OWLIndividual> individuals = new HashSet<OWLIndividual>();
		
		String query = "SELECT ?s WHERE {?s a <" + cls.toStringID() + ">} LIMIT " + maxNrOfIndividuals;
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("s").isURIResource()){
				individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI())));
			}
		}
		qe.close();
		
		return individuals;
	}
}
