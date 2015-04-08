package org.dllearner.kb.sparql;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
//import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
//import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * {@inheritDoc}
 * @author Lorenz Buehmann
 *
 */
public class ConciseBoundedDescriptionGeneratorImpl implements ConciseBoundedDescriptionGenerator{
	
	private static final Logger logger = LoggerFactory.getLogger(ConciseBoundedDescriptionGeneratorImpl.class);
	
	private Model baseModel;
	
	private List<String> namespaces;
	private static final int MAX_RECURSION_DEPTH_DEFAULT = 1;
	private int maxRecursionDepth = 1;
	private QueryExecutionFactory qef;
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, CacheFrontend cache) {
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cache != null){
			qef = new QueryExecutionFactoryCacheEx(qef, cache);
		}
		qef = new QueryExecutionFactoryPaginated(qef, 10000);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, String cacheDir, int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
		
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDir != null){
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(cacheDir, true, timeToLive);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		}
		qef = new QueryExecutionFactoryPaginated(qef, 10000);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, String cacheDir) {
		this(endpoint, cacheDir, MAX_RECURSION_DEPTH_DEFAULT);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint) {
		this(endpoint, (String)null);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(Model model) {
		this.baseModel = model;
		
		qef = new QueryExecutionFactoryModel(baseModel);
	}
	
	public Model getConciseBoundedDescription(String resourceURI){
		return getConciseBoundedDescription(resourceURI, maxRecursionDepth);
	}
	
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		return getConciseBoundedDescription(resourceURI, depth, false);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		logger.trace("Computing CBD for {} ...", resourceURI);
		long start = System.currentTimeMillis();
		String query = generateQuery(resourceURI, depth, withTypesForLeafs);
		QueryExecution qe = qef.createQueryExecution(query);
		Model model = qe.execConstruct();
		qe.close(); 
		long end = System.currentTimeMillis();
		logger.trace("Got {} triples in {} ms.", model.size(), (end - start));
		return model;
	}
	
	@Override
	public void setRestrictToNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}
	
	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private String generateQuery(String resource, int depth, boolean withTypesForLeafs){
		int lastIndex = Math.max(0, depth - 1);
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		if(withTypesForLeafs){
			sb.append("?o").append(lastIndex).append(" a ?type.\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		sb.append(createNamespacesFilter("?p0"));
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
			sb.append(createNamespacesFilter("?p" + i));
		}
		if(withTypesForLeafs){
			sb.append("OPTIONAL{?o").append(lastIndex).append(" a ?type.}\n");
		}
		for(int i = 1; i < depth; i++){
			sb.append("}");
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	private String createNamespacesFilter(String targetVar){
		String filter = "";
		if(namespaces != null && !namespaces.isEmpty()){
			filter += "FILTER(";
			for(Iterator<String> iter = namespaces.iterator(); iter.hasNext();){
				String ns = iter.next();
				filter += "(REGEX(STR(" + targetVar + "),'" + ns + "'))";
				if(iter.hasNext()){
					filter += " || ";
				}
			}
			filter += ")";
		}
		return filter;
	}
	
	public static void main(String[] args) {
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		cbdGen = new CachingConciseBoundedDescriptionGenerator(cbdGen);
//		cbdGen.setRestrictToNamespaces(Arrays.asList(new String[]{"http://dbpedia.org/ontology/", RDF.getURI(), RDFS.getURI()}));
		Model cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Leipzig", 3);
		System.out.println(cbd.size());
	}

	

}
