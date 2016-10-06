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
package org.dllearner.algorithms.qtl.experiments;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.dllearner.kb.sparql.CBDStructureTree;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Lorenz Buehmann
 *
 */
public class BenchmarkDescriptionGeneratorDatabase extends BenchmarkDescriptionGenerator{

	SparqlEndpoint endpoint;
	Connection conn;
	PreparedStatement ps_insert;
	private String databaseName = "";

	public BenchmarkDescriptionGeneratorDatabase(QueryExecutionFactory qef) {
		super(qef);
	}


	public void generateBenchmarkDescription(File inputFile, String databaseName, boolean queriesWithIDs) throws Exception {
		this.databaseName = databaseName;
		generateBenchmarkDescription(inputFile, queriesWithIDs);
	}

	@Override
	protected void beginDocument() {

		try {
			Properties properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("org/dllearner/algorithms/qtl/qtl-eval-config.properties"));

			conn = DriverManager.getConnection(
					properties.getProperty("url"),
					properties.getProperty("username"),
					properties.getProperty("password"));

			// create database
			String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
			conn.createStatement().executeUpdate(sql);

			// switch to database
			conn.setCatalog(databaseName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void endDocument() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void beginTable() {
		try {
			String tableName = "benchmark_data";
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE Table " + tableName + " (" +
					"id VARCHAR(20) NOT NULL," +
					"query VARCHAR(500) NOT NULL," +
					"query_type VARCHAR(50) NOT NULL," +
					"query_depth SMALLINT NOT NULL," +
					"instance_count INT NOT NULL," +
					"cbd_size_opt_min INT NOT NULL," +
					"cbd_size_opt_max INT NOT NULL," +
					"cbd_size_opt_avg INT NOT NULL," +
					"cbd_size_def_min INT NOT NULL," +
					"cbd_size_def_max INT NOT NULL," +
					"cbd_size_def_avg INT NOT NULL," +
					"PRIMARY KEY (id)" +
					")");

			ps_insert = conn.prepareStatement("INSERT INTO " + tableName + "" +
					"(id, query, query_type, query_depth, instance_count," +
					"cbd_size_opt_min, cbd_size_opt_max, cbd_size_opt_avg," +
					"cbd_size_def_min, cbd_size_def_max, cbd_size_def_avg)" +
					" VALUES " +
					"(?,?,?,?,?,?,?,?,?,?,?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void addRow(QueryData queryData) {
		try {
			ps_insert.setString(1, queryData.id);
			ps_insert.setString(2, queryData.query.toString());
			ps_insert.setString(3, queryData.queryType.toString());
			ps_insert.setInt(4, queryData.maxTreeDepth);
			ps_insert.setInt(5, queryData.nrOfInstances);
			ps_insert.setInt(6, (int) queryData.optimalCBDSizeStats.getMin());
			ps_insert.setInt(7, (int) queryData.optimalCBDSizeStats.getMax());
			ps_insert.setInt(8, (int) queryData.optimalCBDSizeStats.getMean());
			ps_insert.setInt(9, (int) queryData.defaultCBDSizesStats.getMin());
			ps_insert.setInt(10, (int) queryData.defaultCBDSizesStats.getMax());
			ps_insert.setInt(11, (int) queryData.defaultCBDSizesStats.getMean());

			ps_insert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void endTable(){}

	public static void main(String[] args) throws Exception{
		OptionParser parser = new OptionParser();
		OptionSpec<File> benchmarkDirectorySpec = parser.accepts("d", "base directory").withRequiredArg().ofType(File.class).required();
		OptionSpec<File> queriesFileSpec = parser.accepts("i", "input queries file").withRequiredArg().ofType(File.class).required();
		OptionSpec<String> tableNameSpec = parser.accepts("db", "database name").withRequiredArg().ofType(String.class).required();
		OptionSpec<URL> endpointURLSpec = parser.accepts("e", "endpoint URL").withRequiredArg().ofType(URL.class).required();
		OptionSpec<String> defaultGraphSpec = parser.accepts("g", "default graph").withRequiredArg().ofType(String.class);
		OptionSpec<Boolean> useCacheSpec = parser.accepts("cache", "use cache").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE);
		OptionSpec<Boolean> queriesHaveIdSpec = parser.accepts("id", "input file contains ID, SPARQL query").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE);
		OptionSpec<String> cbdSpec = parser.accepts("cbd", "CBD structure tree string").withOptionalArg().ofType(String.class).required();
		OptionSpec<String> queriesToOmitTokensSpec = parser.accepts("omitTokens", "comma-separated list of tokens such that queries containing any of them will be omitted").withRequiredArg().ofType(String.class).defaultsTo("");
		OptionSpec<Boolean> workaroundSpec = parser.accepts("workaround", "Virtuoso parse error workaround enabled").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);


		OptionSet options = parser.parse(args);

		File benchmarkDirectory = options.valueOf(benchmarkDirectorySpec);
		File inputFile = options.valueOf(queriesFileSpec);
		String tableName = options.valueOf(tableNameSpec);

		URL endpointURL = options.valueOf(endpointURLSpec);
		List<String> defaultGraphs = options.has(defaultGraphSpec) ? Lists.newArrayList(options.valueOf(defaultGraphSpec)) : Collections.emptyList();
		SparqlEndpoint endpoint = SparqlEndpoint.create(endpointURL.toString(), defaultGraphs);

//		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
//		ks.setUseCache(options.valueOf(useCacheSpec));
//		ks.setCacheDir(benchmarkDirectory.getPath());
//		ks.setQueryDelay(1000);
//		ks.setRetryCount(0);
//		ks.init();

		QueryExecutionFactory qef = buildQueryExecutionFactory(endpoint,
				options.valueOf(useCacheSpec), benchmarkDirectory.getPath(), TimeUnit.DAYS.toMillis(30),
				0, 60);


		CBDStructureTree cbdStructureTree = CBDStructureTree.fromTreeString(options.valueOf(cbdSpec).trim());

		List<String> omitTokens = Splitter
				.on(",")
				.omitEmptyStrings()
				.trimResults()
				.splitToList(options.valueOf(queriesToOmitTokensSpec));

		BenchmarkDescriptionGeneratorDatabase generator = new BenchmarkDescriptionGeneratorDatabase(qef);
		generator.setDefaultCbdStructure(cbdStructureTree);
		generator.setSkipQueryTokens(omitTokens);
		generator.setEndpoint(endpoint);
		generator.setWorkaroundEnabled(options.valueOf(workaroundSpec));
		generator.generateBenchmarkDescription(inputFile, tableName, options.valueOf(queriesHaveIdSpec));
	}

	private static QueryExecutionFactory buildQueryExecutionFactory(SparqlEndpoint endpoint, boolean useCache,
																	String cacheDir, long cacheTTL, int retryCount, long queryDelay) {
		QueryExecutionFactory qef = new org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp(
				endpoint.getURL().toString(),
				endpoint.getDefaultGraphURIs());
		qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
						.setModelContentType(WebContent.contentTypeRDFXML))
				.end()
				.create();

		if(useCache) {
			qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir, false, cacheTTL);
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

}
