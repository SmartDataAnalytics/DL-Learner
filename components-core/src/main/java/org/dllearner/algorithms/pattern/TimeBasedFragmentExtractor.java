/**
 * 
 */
package org.dllearner.algorithms.pattern;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.pagination.core.PaginationUtils;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.OWLClass;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class TimeBasedFragmentExtractor implements FragmentExtractor{
	
	public static final FragmentExtractionStrategy extractionStrategy = FragmentExtractionStrategy.TIME;
	private SparqlEndpointKS ks;
	private QueryExecutionFactory qef;
	
	private long maxExecutionTimeInMilliseconds;
	private long startTime;
	
	public TimeBasedFragmentExtractor(SparqlEndpointKS ks, String cacheDir, int maxExecutionTimeInMilliseconds, TimeUnit timeUnit) {
		this.ks = ks;
		this.maxExecutionTimeInMilliseconds = timeUnit.toMillis(maxExecutionTimeInMilliseconds);
		
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
	}
	
	public TimeBasedFragmentExtractor(SparqlEndpointKS ks, int maxExecutionTimeInMilliseconds, TimeUnit timeUnit) {
		this(ks, null, maxExecutionTimeInMilliseconds, timeUnit);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.pattern.FragmentExtractor#extractFragment(org.dllearner.core.owl.NamedClass)
	 */
	@Override
	public Model extractFragment(OWLClass cls, int maxFragmentDepth) {
		startTime = System.currentTimeMillis();
		Model fragment = ModelFactory.createDefaultModel();
		
		Query query = buildConstructQuery(cls, maxFragmentDepth);
		
		long pageSize = PaginationUtils.adjustPageSize(qef, 10000);
		query.setLimit(pageSize);
		int offset = 0;
		while(getRemainingRuntime() > 0){
			query.setOffset(offset);System.out.println(query);
			Model model = qef.createQueryExecution(query).execConstruct();
			fragment.add(model);
			offset += pageSize;
		}
		return fragment;
	}
	
	private Query buildConstructQuery(OWLClass cls, int depth){
		StringBuilder sb = new StringBuilder();
		int maxVarCnt = 0;
		sb.append("CONSTRUCT {\n");
		sb.append("?s").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
			maxVarCnt++;
		}
		sb.append("?o").append(maxVarCnt).append(" a ?type.\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("?s a ?cls.");
		sb.append("?s").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("OPTIONAL{?o").append(maxVarCnt).append(" a ?type}.\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("}");
		}
		
		sb.append("}\n");
		ParameterizedSparqlString template = new ParameterizedSparqlString(sb.toString());
		template.setIri("cls", cls.toStringID());
		return template.asQuery();
	}
	
	private long getRemainingRuntime(){
		return maxExecutionTimeInMilliseconds - (System.currentTimeMillis() - startTime);
	}

}
