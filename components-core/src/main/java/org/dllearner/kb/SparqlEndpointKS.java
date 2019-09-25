/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SPARQL endpoint knowledge source (without fragment extraction),
 * in particular for those algorithms which work directly on an endpoint
 * without requiring an OWL reasoner.
 *
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "SPARQL endpoint", shortName = "sparql", version = 0.2)
public class SparqlEndpointKS extends AbstractKnowledgeSource {

	private static final Logger logger = LoggerFactory.getLogger(SparqlEndpointKS.class);

	private SparqlEndpoint endpoint;
	@NoConfigOption
	private CacheFrontend cache;
	@NoConfigOption // auto-detected
	private boolean supportsSPARQL_1_1 = false;
	private boolean isRemote = true;

	@ConfigOption(description="URL of the SPARQL endpoint", required=true)
	private URL url;

	@ConfigOption(description="a list of default graph URIs", defaultValue="{}", required=false)
	private List<String> defaultGraphURIs = new LinkedList<>();

	@ConfigOption(description="a list of named graph URIs", defaultValue="{}", required=false)
	private List<String> namedGraphURIs = new LinkedList<>();

	// some parameters for the query execution
	@ConfigOption(defaultValue = "50", description = "Use this setting to avoid overloading the endpoint with a sudden burst of queries. A value below 0 means no delay.", required = false)
	private long queryDelay = 50;

	// caching options
	@ConfigOption(defaultValue = "true", description = "Use this setting to enable caching of SPARQL queries in a local database.", required = false)
	private boolean useCache = true;

	@ConfigOption(defaultValue = "tmp folder of the system", description = "The base directory of the SPARQL query cache.", required = false)
	protected String cacheDir = System.getProperty("java.io.tmpdir") + "/sparql-cache;COMPRESS=TRUE";

	@ConfigOption(defaultValue = "86400", description = "The time to live in milliseconds for cached SPARQL queries, if enabled. The default value is 86400s(=1 day).", required = false)
	protected long cacheTTL = TimeUnit.DAYS.toMillis(1);

	@ConfigOption(defaultValue = "3", description = "The maximum number of retries for the execution of a particular SPARQL query.", required = false)
	protected int retryCount = 3;

	protected QueryExecutionFactory qef;

	@ConfigOption(defaultValue = "10 000", description = "page size", exampleValue = "10000")
	private long pageSize = 10000;
	
	private KnowledgeSource schema;

	public SparqlEndpointKS() {}

	public SparqlEndpointKS(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public SparqlEndpointKS(SparqlEndpoint endpoint, KnowledgeSource schema) {
		this.endpoint = endpoint;
		this.schema = schema;
	}

	public SparqlEndpointKS(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	public SparqlEndpointKS(SparqlEndpoint endpoint, String cacheDirectory) {
		this.endpoint = endpoint;
		this.cacheDir = cacheDirectory;
	}

	public CacheFrontend getCache() {
		return cache;
	}

	public QueryExecutionFactory getQueryExecutionFactory() {
		return qef;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(CacheFrontend cache) {
		this.cache = cache;
	}

	public void setQueryExecutionFactory(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	@Override
	public void init() throws ComponentInitException {
		if(!initialized){
			if(isRemote()) {
				if(endpoint == null) {
					endpoint = new SparqlEndpoint(url, defaultGraphURIs, namedGraphURIs);
				}
				supportsSPARQL_1_1 = new SPARQLTasks(endpoint).supportsSPARQL_1_1();
			}

			if(qef == null) {
				qef = buildQueryExecutionFactory();
			}

			initialized = true;
		}
		
		initialized = true;
		logger.info("SPARQL KB setup:\n" + toString());
	}

	protected QueryExecutionFactory buildQueryExecutionFactory() {
		/*QueryExecutionFactory qef = new org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp(
				endpoint.getURL().toString(),
				endpoint.getDefaultGraphURIs());*/
		QueryExecutionFactory qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
						.setModelContentType(WebContent.contentTypeRDFXML))
				.end()
				.create();

		if(useCache) {
			qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir, false, cacheTTL );
		} else {
			// use in-memory cache
			qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir, true, cacheTTL);
		}

		// add some delay
		qef = new QueryExecutionFactoryDelay(qef, queryDelay);

		if(retryCount > 0) {
			qef = new QueryExecutionFactoryRetry(qef, retryCount, 1, TimeUnit.SECONDS);
		}

		// add pagination to avoid incomplete result sets due to limitations of the endpoint
//		qef = new QueryExecutionFactoryPaginated(qef, pageSize);

		return qef;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public SparqlEndpoint getEndpoint() {
		return endpoint;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public boolean isRemote() {
		return isRemote;
	}

	public List<String> getDefaultGraphURIs() {
		return defaultGraphURIs;
	}

	public void setDefaultGraphURIs(List<String> defaultGraphURIs) {
		this.defaultGraphURIs = defaultGraphURIs;
	}

	public List<String> getNamedGraphURIs() {
		return namedGraphURIs;
	}

	public void setNamedGraphURIs(List<String> namedGraphURIs) {
		this.namedGraphURIs = namedGraphURIs;
	}

	public boolean supportsSPARQL_1_1() {
		return supportsSPARQL_1_1;
	}

	public void setSupportsSPARQL_1_1(boolean supportsSPARQL_1_1) {
		this.supportsSPARQL_1_1 = supportsSPARQL_1_1;
	}

	/**
	 * Set a delay between each sent SPARQL query to avoid overloading of the
	 * endpoint. Note that this does only make sense for remote endpoints and
	 * will be ignored for local files.
	 * @param queryDelay the delay in milliseconds
	 */
	public void setQueryDelay(int queryDelay) {
		this.queryDelay = queryDelay;
	}

	/**
	 * @param useCache the useCache to set
	 */
	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	/**
	 * Set the file-based cache directory. Default is the temporary
	 * folder of the operating system retrieved by using java.io.tmpdir,
	 * i.e. in most cases
	 * <table>
	 * <tr><th>OS</th><th>Directory</th></tr>
	 * <tr><td>Linux</td><td>/tmp/</td></tr>
	 * <tr><td>Windows</td><td>C:\temp</td></tr>
	 * </table>
	 *
	 * @param cacheDir the absolute cache directory path
	 */
	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

	/**
	 * Set the time-to-live for the file-based SPARQL cache.
	 * @param cacheTTL the time-to-live value in milliseconds
	 */
	public void setCacheTTL(long cacheTTL) {
		this.cacheTTL = cacheTTL;
	}
	
	/**
	 * @return if exists, a knowledge source which contains the schema
	 */
	public KnowledgeSource getSchema() {
		return schema;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	@Override
	public String toString() {
		String out = String.format("%-15s %-25s%n", "Endpoint:", "Remote");
		if (qef != null) {
			out += String.format("%-15s %-25s%n", "URL:", qef.getId());
		} else {
			out += String.format("%-15s %-25s%n", "URL:", "null");
		}
		out += String.format("%-15s %-25s%n", "Cache:", cacheDir);
		out += String.format("%-15s %dms%n", "Delay:", queryDelay);
		return out;
	}

}
