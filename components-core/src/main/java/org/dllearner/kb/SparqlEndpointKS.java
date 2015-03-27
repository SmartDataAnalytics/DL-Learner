/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ListStringEditor;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.springframework.beans.propertyeditors.URLEditor;

/**
 * SPARQL endpoint knowledge source (without fragment extraction),
 * in particular for those algorithms which work directly on an endpoint
 * without requiring an OWL reasoner.
 *
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "SPARQL endpoint", shortName = "sparql", version = 0.2)
public class SparqlEndpointKS implements KnowledgeSource {

	private SparqlEndpoint endpoint;
	private CacheFrontend cache;
	private boolean supportsSPARQL_1_1 = false;
	private boolean isRemote = true;
	private boolean initialized = false;

	@ConfigOption(name = "url", required=true, propertyEditorClass = URLEditor.class)
	private URL url;

	@ConfigOption(name = "defaultGraphs", defaultValue="[]", required=false, propertyEditorClass = ListStringEditor.class)
	private List<String> defaultGraphURIs = new LinkedList<String>();

	@ConfigOption(name = "namedGraphs", defaultValue="[]", required=false, propertyEditorClass = ListStringEditor.class)
	private List<String> namedGraphURIs = new LinkedList<String>();

	// some parameters for the query execution
	@ConfigOption(name = "queryDelay", defaultValue = "50", description = "Use this setting to avoid overloading the endpoint with a sudden burst of queries. A value below 0 means no delay.", required = false)
	private int queryDelay = 50;
	
	private QueryExecutionFactory qef;

	public SparqlEndpointKS() {}

	public SparqlEndpointKS(SparqlEndpoint endpoint) {
		this(new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs()));
		this.endpoint = endpoint;
	}

	public SparqlEndpointKS(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	public SparqlEndpointKS(SparqlEndpoint endpoint, CacheFrontend cache) {
		this.endpoint = endpoint;
		this.cache = cache;
		this.qef = 	new QueryExecutionFactoryHttp(endpoint.getURL().toString(),
						endpoint.getDefaultGraphURIs());
		if(cache != null){
			this.qef = new QueryExecutionFactoryCacheEx(qef, cache);
		}
	}

	public SparqlEndpointKS(SparqlEndpoint endpoint, String cacheDirectory) {
		this.endpoint = endpoint;
		this.qef = 	new QueryExecutionFactoryHttp(endpoint.getURL().toString(),
				endpoint.getDefaultGraphURIs());
		if(cacheDirectory != null){
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				cache = CacheUtilsH2.createCacheFrontend(cacheDirectory, false, timeToLive);
				this.qef = new QueryExecutionFactoryCacheEx(qef, cache);
		}
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

	@Override
	public void init() throws ComponentInitException {
		if(!initialized){
			if(endpoint == null) {
				endpoint = new SparqlEndpoint(url, defaultGraphURIs, namedGraphURIs);
			}
			supportsSPARQL_1_1 = new SPARQLTasks(endpoint).supportsSPARQL_1_1();

			if(qef == null) {
				qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(),
						endpoint.getDefaultGraphURIs());
			}

			// add some delay
			qef = new QueryExecutionFactoryDelay(qef, 100);

			initialized = true;
		}
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

	@Override
	public String toString() {
		return endpoint.toString();
	}

}
