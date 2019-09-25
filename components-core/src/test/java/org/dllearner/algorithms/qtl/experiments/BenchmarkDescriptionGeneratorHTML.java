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

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.dllearner.kb.sparql.CBDStructureTree;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Lorenz Buehmann
 *
 */
public class BenchmarkDescriptionGeneratorHTML extends BenchmarkDescriptionGenerator{
	
	String style =
			"<head>\n" + 
			"<link rel=\"stylesheet\" href=\"https://rawgit.com/twbs/bootstrap/master/dist/css/bootstrap.min.css\">\n" + 
			"<link rel=\"stylesheet\" href=\"https://rawgit.com/wenzhixin/bootstrap-table/1.8.0/dist/bootstrap-table.css\">\n" +
			"<style type=\"text/css\">\n" + 
			"   pre {\n" + 
			"	border: 0; \n" + 
			"	background-color: transparent\n" +
//			"   font-family: monospace;" +
			"	}\n"
			+ "table {\n" + 
			"    border-collapse: separate;\n" + 
			"    border-spacing: 0 5px;\n" + 
			"}\n" +
					"table th {\n" +
					"    width: auto !important;\n" +
					"}" +
			"\n" + 
			"thead th {\n" + 
			"    background-color: #006DCC;\n" + 
			"    color: white;\n" + 
			"}\n" + 
			"\n" + 
			"tbody td {\n" + 
			"    background-color: #EEEEEE;\n" + 
			"}\n" + 
			"\n" + 
			"tr td:first-child,\n" + 
			"tr th:first-child {\n" + 
			"    border-top-left-radius: 6px;\n" + 
			"    border-bottom-left-radius: 6px;\n" + 
			"}\n" + 
			"\n" + 
			"tr td:last-child,\n" + 
			"tr th:last-child {\n" + 
			"    border-top-right-radius: 6px;\n" + 
			"    border-bottom-right-radius: 6px;\n" + 
			"}\n" + 
			".fixed-table-container tbody td {\n" + 
			"    border: none;\n" + 
			"}\n" + 
			".fixed-table-container thead th {\n" + 
			"    border: none;\n" + 
			"}\n" + 
			"\n" + 
			".bootstrap-table .table {\n" + 
			"	border-collapse: inherit !important;\n" + 
			"}" + 
			"</style>\n" +
			"<script src=\"http://code.jquery.com/jquery-1.11.3.min.js\"></script>\n" + 
			"<script src=\"https://rawgit.com/twbs/bootstrap/master/dist/js/bootstrap.min.js\"></script>\n" + 
			"<script src=\"https://rawgit.com/wenzhixin/bootstrap-table/1.8.0/dist/bootstrap-table-all.min.js\"></script>\n" +
			"</head>\n";

	SparqlEndpoint endpoint;
	StringBuilder sb;
			
	public BenchmarkDescriptionGeneratorHTML(QueryExecutionFactory qef) {
		super(qef);
	}

	public void generateBenchmarkDescription(File inputFile, File htmlOutputFile, boolean withQueryIdGivenInFile) throws Exception{
		sb = new StringBuilder();

		generateBenchmarkDescription(inputFile, withQueryIdGivenInFile);

		try {
			Files.write(sb, htmlOutputFile, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void beginDocument() {
		sb.append("<html>\n");
		sb.append(style);
		sb.append("<body>\n");
	}

	@Override
	protected void endDocument() {
		sb.append("</body>\n</html>\n");
	}

	@Override
	protected void beginTable() {
		sb.append("<table data-toggle=\"table\" data-striped='true'>\n");
		// table header
		sb.append("<thead>\n"
//				"<tr>\n"
//				+ "<th colspan=\"6\">test</th>\n"
//				+ "<th colspan=\"3\">|CBD|<sub>opt</sub></th>\n"
//				+ "<th colspan=\"3\">|CBD|<sub>gen</sub></th>\n"
//				+ "</tr>\n"
				+ "<tr>\n"
				+ "<th data-sortable=\"true\" data-valign='middle'>ID</th>\n"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query</th>\n"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query Type</th>\n"
//				+ "<th data-valign='middle'>Query Graph</th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>Depth</th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>#Instances</th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>min</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>max</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>avg</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>min</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>max</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>avg</sub></th>\n"
				+ "</tr>\n" +
				"</thead>\n");

		sb.append("<tbody>\n");
	}

	@Override
	protected void addRow(QueryData queryData) {
		sb.append("<tr>\n");

		// column: ID
		sb.append("<td>").append(queryData.id).append("</td>\n");

		// column: SPARQL query
		sb.append("<td><pre>").append(queryData.query.toString().replace("<", "&lt;").replace(">", "&gt;")).append("</pre></td>\n");

		// column: SPARQL query type
		sb.append("<td>").append(queryData.queryType).append("</td>\n");

		// query graph
//		QueryToGraphExporter.exportYedGraph(queryData.query, new File(""));
//		sb.append("<td><img src=\"" + graphFile.getPath() + "\" alt=\"query graph\"></td>\n");

		// column: depth
		sb.append("<td class='number'>").append(queryData.maxTreeDepth).append("</td>\n");

		// column: #instances
		sb.append("<td class='number'>").append(queryData.nrOfInstances).append("</td>\n");

		// columns: optimal CBD sizes (min, max, avg)
		DescriptiveStatistics optimalCBDSizeStats = queryData.optimalCBDSizeStats;
		sb.append("<td class='number'>").append((int) optimalCBDSizeStats.getMin()).append("</td>\n");
		sb.append("<td class='number'>").append((int) optimalCBDSizeStats.getMax()).append("</td>\n");
		sb.append("<td class='number'>").append((int) optimalCBDSizeStats.getMean()).append("</td>\n");

		// columns: generic CBD sizes (min, max, avg)
		DescriptiveStatistics genericCBDSizeStats = queryData.defaultCBDSizesStats;
		sb.append("<td class='number'>").append((int) genericCBDSizeStats.getMin()).append("</td>\n");
		sb.append("<td class='number'>").append((int) genericCBDSizeStats.getMax()).append("</td>\n");
		sb.append("<td class='number'>").append((int) genericCBDSizeStats.getMean()).append("</td>\n");


		sb.append("</tr>\n");
	}

	@Override
	protected void endTable() {
		sb.append("</tbody>\n</table>\n");
	}

	public static void main(String[] args) throws Exception{
		OptionParser parser = new OptionParser();
		OptionSpec<File> benchmarkDirectorySpec = parser.accepts("d", "base directory").withRequiredArg().ofType(File.class).required();
		OptionSpec<File> queriesFileSpec = parser.accepts("i", "input queries file").withRequiredArg().ofType(File.class).required();
		OptionSpec<File> outputFileSpec = parser.accepts("o", "target output file").withRequiredArg().ofType(File.class).required();
		OptionSpec<URL> endpointURLSpec = parser.accepts("e", "endpoint URL").withRequiredArg().ofType(URL.class).required();
		OptionSpec<String> defaultGraphSpec = parser.accepts("g", "default graph").withRequiredArg().ofType(String.class);
		OptionSpec<Boolean> useCacheSpec = parser.accepts("cache", "use cache").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE);
		OptionSpec<Boolean> queriesHaveIdSpec = parser.accepts("id", "input file contains ID, SPARQL query").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE);
		OptionSpec<String> cbdSpec = parser.accepts("cbd", "CBD structure tree string").withRequiredArg().ofType(String.class).required();
		OptionSpec<String> queriesToOmitTokensSpec = parser.accepts("omitTokens", "comma-separated list of tokens such that queries containing any of them will be omitted").withRequiredArg().ofType(String.class).defaultsTo("");
		OptionSpec<Boolean> workaroundSpec = parser.accepts("workaround", "Virtuoso parse error workaround enabled").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);


		OptionSet options = parser.parse(args);

		File benchmarkDirectory = options.valueOf(benchmarkDirectorySpec);
		File inputFile = options.valueOf(queriesFileSpec);
		File outputFile = options.valueOf(outputFileSpec);

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

		BenchmarkDescriptionGeneratorHTML generator = new BenchmarkDescriptionGeneratorHTML(qef);
		generator.setDefaultCbdStructure(cbdStructureTree);
		generator.setSkipQueryTokens(omitTokens);
		generator.setEndpoint(endpoint);
		generator.setWorkaroundEnabled(options.valueOf(workaroundSpec));
		generator.generateBenchmarkDescription(inputFile, outputFile, options.valueOf(queriesHaveIdSpec));
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
