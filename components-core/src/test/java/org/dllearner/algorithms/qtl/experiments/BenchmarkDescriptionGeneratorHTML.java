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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.dllearner.utilities.QueryUtils;

/**
 * @author Lorenz Buehmann
 *
 */
public class BenchmarkDescriptionGeneratorHTML {
	
	String style = 
			"<head>\n" + 
			"<style>\n" + 
			"table.sortable thead {\n" + 
			"    background-color:#eee;\n" + 
			"    color:#666666;\n" + 
			"    font-weight: bold;\n" + 
			"    cursor: pointer;\n" + 
			"}\n" + 
			"table.sortable tbody tr:nth-child(2n) td {\n" + 
			"    color: #000000;\n" + 
			"    background-color: #EAF2D3;\n" + 
			"}\n" + 
			"table.sortable tbody tr:nth-child(2n+1) td {\n" + 
			"  background: #ccfffff;\n" + 
			"}" + 
			"#benchmark {\n" + 
			"    font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" + 
			"    width: 100%;\n" + 
			"    border-collapse: collapse;\n" + 
			"}\n" + 
			"\n" + 
			"#benchmark td, #benchmark th {\n" + 
			"    font-size: 1em;\n" + 
			"    border: 1px solid #98bf21;\n" + 
			"    padding: 3px 7px 2px 7px;\n" + 
			"}\n" + 
			"\n" + 
			"#benchmark th {\n" + 
			"    font-size: 1.1em;\n" + 
			"    text-align: left;\n" + 
			"    padding-top: 5px;\n" + 
			"    padding-bottom: 4px;\n" + 
			"    background-color: #A7C942;\n" + 
			"    color: #ffffff;\n" + 
			"}\n"
			+ "#benchmark td.number {\n" + 
			"  text-align: right;\n" + 
			"}\n" + 
			"</style>\n"
			+ "<script type='text/javascript' src='http://www.kryogenix.org/code/browser/sorttable/sorttable.js'></script>" + 
			"</head>\n" + 
			"<body>";
	
	String style2 = 
			"<head>\n" + 
			"<link rel=\"stylesheet\" href=\"https://rawgit.com/twbs/bootstrap/master/dist/css/bootstrap.min.css\">\n" + 
			"<link rel=\"stylesheet\" href=\"https://rawgit.com/wenzhixin/bootstrap-table/1.8.0/dist/bootstrap-table.css\">\n" +
			"<style type=\"text/css\">\n" + 
			"   pre {\n" + 
			"	border: 0; \n" + 
			"	background-color: transparent\n"
			+ "font-family: monospace;" + 
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
			
	
	private QueryExecutionFactory qef;

	public BenchmarkDescriptionGeneratorHTML(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	private List<Query> loadQueries(File queriesFile) throws IOException {
		List<Query> queries = new ArrayList<>();
		
		for (String queryString : Files.readLines(queriesFile, Charsets.UTF_8)) {
			Query q = QueryFactory.create(queryString);
			queries.add(q);
		}
		return queries;
	}
	
	public void generateBenchmarkDescription(File benchmarkQueriesFile, File htmlOutputFile) throws Exception{
		List<Query> queries = loadQueries(benchmarkQueriesFile);
		
		Var var = Var.alloc("s");
		String html = "<html>\n";
		html += style2;
		html += "<body>\n";
		html += "<table data-toggle=\"table\" data-striped='true'>\n";
		// table header
		html += "<thead><tr>"
				+ "<th data-sortable=\"true\" data-valign='middle'>ID</th>"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query</th>"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query Type</th>"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>Depth</th>"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>#Instances</th>"
				+ "</tr></thead>\n";
		
		html += "<tbody>\n";
		int id = 1;
		for (Query query : queries) {
			query.getPrefixMapping().removeNsPrefix("owl");
			query.getPrefixMapping().removeNsPrefix("rdfs");
			query.getPrefixMapping().removeNsPrefix("foaf");
			query.getPrefixMapping().removeNsPrefix("rdf");

			if(query.toString().contains("http://dbpedia.org/ontology/")) {
				query.getPrefixMapping().setNsPrefix("dbo", "http://dbpedia.org/ontology/");
			}
			if(query.toString().contains("http://dbpedia.org/property/")) {
				query.getPrefixMapping().setNsPrefix("dbp", "http://dbpedia.org/property/");
			}
			if(query.toString().contains("http://xmlns.com/foaf/0.1/")) {
				query.getPrefixMapping().setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
			}
			QueryUtils.exportAsGraph(query, new File("/tmp/test.png"));


			query.setBaseURI("http://dbpedia.org/resource/");
			html += "<tr>\n";
			
			// 1. column: ID
			html += "<td>" + id++ + "</td>\n";
			
			// 2. column: SPARQL query
			html += "<td><pre>" + query.toString().replace("<", "&lt;").replace(">", "&gt;") + "</pre></td>\n";

			// 3. column: SPARQL query type
			html += "<td>" + SPARQLUtils.getQueryType(query) + "</td>\n";
			
			// 4. column: depth
			int depth = org.dllearner.utilities.QueryUtils.getSubjectObjectJoinDepth(query, var) + 1;
			html += "<td class='number'>" + depth + "</td>\n";
			
			// 5. column: #instances
			int nrOfInstances = SPARQLUtils.getResult(qef, query).size();
			html += "<td class='number'>" + nrOfInstances + "</td>\n";
			
			html += "</tr>\n";
			break;
		}
		html += "</tbody>\n";
		html += "</table>\n";
		html += "</body>\n";
		html += "</html>\n";
		
		try {
			Files.write(html, htmlOutputFile, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception{
		if(args.length < 3) {
			System.out.println("Usage: BenchmarkDescriptionGeneratorHTML <source> <target> <endpointURL> <defaultGraphURI>");
			System.exit(0);
		}
		File source = new File(args[0]);
		File target = new File(args[1]);
		String endpointURL = args[2];
		String defaultGraph = null;
		if(args.length == 4)
			defaultGraph = args[3];
		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpointURL, defaultGraph);
		qef = new QueryExecutionFactoryPaginated(qef);
		BenchmarkDescriptionGeneratorHTML generator = new BenchmarkDescriptionGeneratorHTML(qef);
		generator.generateBenchmarkDescription(source, target);
	}

}
