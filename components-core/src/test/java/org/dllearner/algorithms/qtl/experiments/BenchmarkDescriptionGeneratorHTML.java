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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.CBDStructureTree;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.TreeBasedConciseBoundedDescriptionGenerator;
import org.dllearner.utilities.ProgressBar;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class BenchmarkDescriptionGeneratorHTML {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkDescriptionGeneratorHTML.class);
	
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
			
	
	private QueryExecutionFactory qef;
	private TreeBasedConciseBoundedDescriptionGenerator cbdGen;
	private QueryUtils utils = new QueryUtils();

	private boolean useConstruct = true;


	public BenchmarkDescriptionGeneratorHTML(QueryExecutionFactory qef) {
		this.qef = qef;
		 cbdGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
	}

	private List<Query> loadQueries(File queriesFile) throws IOException {
		List<Query> queries = new ArrayList<>();
		
		for (String queryString : Files.readLines(queriesFile, Charsets.UTF_8)) {
			Query q = QueryFactory.create(queryString);
			adjustPrefixes(q);
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
		html += "<thead><tr>\n"
				+ "<th data-sortable=\"true\" data-valign='middle'>ID</th>\n"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query</th>\n"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query Type</th>\n"
				+ "<th data-valign='middle'>Query Graph</th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>Depth</th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>#Instances</th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>min</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>max</sub></th>\n"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>|CBD|<sub>avg</sub></th>\n"
				+ "</tr></thead>\n";
		
		html += "<tbody>\n";
		int id = 1;
		File graphDir = new File("/tmp/graphs/");
		graphDir.mkdirs();
		for (Query query : queries) {
//			if(!query.toString().contains("Kennedy"))continue;
			System.out.println(query);

//			exportGraph(query, new File("/tmp/graphs/graph" + id + ".png"));
			File graphFile = new File(graphDir, "graph" + id + ".png");
//			QueryToGraphExporter.exportYedGraph(query, graphFile);

			html += "<tr>\n";
			
			// 1. column: ID
			html += "<td>" + id++ + "</td>\n";
			
			// 2. column: SPARQL query
			html += "<td><pre>" + query.toString().replace("<", "&lt;").replace(">", "&gt;") + "</pre></td>\n";

			// 3. column: SPARQL query type
			html += "<td>" + SPARQLUtils.getQueryType(query) + "</td>\n";

			// query graph

			html += "<td><img src=\"" + graphFile.getPath() + "\" alt=\"query graph\"></td>\n";
			
			// 4. column: depth
			html += "<td class='number'>" + getLongestPath(query) + "</td>\n";

			List<String> result = SPARQLUtils.getResult(qef, query);

			// 5. column: #instances
			int nrOfInstances = result.size();
			html += "<td class='number'>" + nrOfInstances + "</td>\n";

			// 6. - 8. column: CBD sizes (min, max, avg)
			DescriptiveStatistics cbdSizeStats = determineCBDSizes(query, result);
			html += "<td class='number'>" + (int)cbdSizeStats.getMin() + "</td>\n";
			html += "<td class='number'>" + (int)cbdSizeStats.getMax() + "</td>\n";
			html += "<td class='number'>" + (int)cbdSizeStats.getMean() + "</td>\n";


			html += "</tr>\n";
//			break;
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

	private void adjustPrefixes(Query query) {
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
		if(query.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#") || query.toString().contains(" a ")) {
			query.getPrefixMapping().setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		}
		if(query.toString().contains("http://dbpedia.org/resource/")) {
			query.getPrefixMapping().setNsPrefix("", "http://dbpedia.org/resource/");
		}
	}

	private int getLongestPath(Query query) {
		SPARQLUtils.QueryType type = SPARQLUtils.getQueryType(query);

		int length = 0;
		if(type == SPARQLUtils.QueryType.IN) {
			Set<Triple> tmp = utils.extractIncomingTriplePatterns(query, query.getProjectVars().get(0));
			while(!tmp.isEmpty()) {
				length++;
				tmp = tmp.stream()
						.filter(tp -> tp.getSubject().isVariable())
						.map(tp -> tp.getSubject())
						.map(s -> utils.extractIncomingTriplePatterns(query, s))
						.flatMap(tps -> tps.stream())
						.collect(Collectors.toSet());
			}
		} else if(type == SPARQLUtils.QueryType.OUT) {
			Set<Triple> tmp = utils.extractOutgoingTriplePatterns(query, query.getProjectVars().get(0));
			while(!tmp.isEmpty()) {
				length++;
				tmp = tmp.stream()
						.filter(tp -> tp.getObject().isVariable())
						.map(tp -> tp.getObject())
						.map(o -> utils.extractOutgoingTriplePatterns(query, o))
						.flatMap(tps -> tps.stream())
						.collect(Collectors.toSet());
			}
		} else {
			length = -1;
		}
		return length;
	}

	private DescriptiveStatistics determineCBDSizes(Query query, List<String> resources) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		NumberFormat df = DecimalFormat.getPercentInstance();
		AtomicInteger idx = new AtomicInteger(1);

		CBDStructureTree cbdStructure = QueryUtils.getOptimalCBDStructure(query);

		ProgressBar progressBar = new ProgressBar();

		resources.forEach(r -> {
			long cnt = -1;
			if(useConstruct) {
				Model cbd = null;
				try {
					cbd = cbdGen.getConciseBoundedDescription(r, cbdStructure);
					cnt = cbd.size();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e.getCause());
				}

			} else {
				ParameterizedSparqlString template = SPARQLUtils.CBD_TEMPLATE_DEPTH3.copy();
				template.setIri("uri", r);
				try(QueryExecution qe = qef.createQueryExecution(template.toString())) {
					ResultSet rs = qe.execSelect();
					cnt = rs.next().getLiteral("cnt").getInt();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e.getCause());
				}
			}
			stats.addValue(cnt);
			progressBar.update(idx.getAndAdd(1), resources.size());

		});

		return stats;
	}

	private void exportGraph(Query query, File file) {
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		// Adds cells to the model in a single step
		graph.getModel().beginUpdate();
		try
		{
			Set<Triple> tps = utils.extractTriplePattern(query);

			Map<Node, Object> mapping = new HashMap<>();
			tps.forEach(tp -> {
				Object val1 = mapping.putIfAbsent(tp.getSubject(), graph.insertVertex(parent, null, tp.getSubject().toString(query.getPrefixMapping()), 20, 20, 40, 30));
				Object val2 = mapping.putIfAbsent(tp.getObject(), graph.insertVertex(parent, null, tp.getObject().toString(query.getPrefixMapping()), 20, 20, 40, 30));
			});
			tps.forEach(tp -> {
				graph.insertEdge(parent, null, tp.getPredicate().toString(query.getPrefixMapping()), mapping.get(tp.getSubject()), mapping.get(tp.getObject()));
			});

		}
		finally
		{
			// Updates the display
			graph.getModel().endUpdate();
		}
		mxGraphComponent graphComponent = new mxGraphComponent(graph);

		// positioning via jgraphx layouts
//		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
//		layout.setParallelEdgeSpacing(20d);
//		layout.setIntraCellSpacing(40d);
		mxGraphLayout layout = new mxOrthogonalLayout(graph);
		layout.execute(graph.getDefaultParent());

		Map<String, Object> edgeStyle = new HashMap<String, Object>();
//edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
		edgeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		edgeStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		edgeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#ffffff");

		Map<String, Object> nodeStyle = new HashMap<>();
		nodeStyle.put(mxConstants.STYLE_SHAPE,    mxConstants.SHAPE_ELLIPSE);
		nodeStyle.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_BOTTOM);

		mxStylesheet stylesheet = new mxStylesheet();
		stylesheet.setDefaultEdgeStyle(edgeStyle);
		stylesheet.setDefaultVertexStyle(nodeStyle);

		graph.setStylesheet(stylesheet);

//		JFrame frame = new JFrame();
//		frame.getContentPane().add(new mxGraphComponent(adapter));
//		frame.pack();
//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		frame.setVisible(true);



		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
		mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);


		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
			if (image != null) {
				encoder.encode(image);
			}
			outputStream.close();
//			ImageIO.write(image, "PNG", file);
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

		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.create(endpointURL, defaultGraph == null ? Collections.EMPTY_LIST : Lists.newArrayList(defaultGraph)));
		ks.setCacheDir("/tmp/qtl-eval");
		ks.init();
		BenchmarkDescriptionGeneratorHTML generator = new BenchmarkDescriptionGeneratorHTML(ks.getQueryExecutionFactory());
		generator.generateBenchmarkDescription(source, target);
	}

}
