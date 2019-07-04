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
import com.google.common.io.Files;
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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.dllearner.kb.sparql.CBDStructureTree;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.TreeBasedConciseBoundedDescriptionGenerator;
import org.dllearner.utilities.ProgressBar;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class BenchmarkDescriptionGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkDescriptionGenerator.class);

	private QueryExecutionFactory qef;
	private TreeBasedConciseBoundedDescriptionGenerator cbdGen;
	private QueryUtils utils = new QueryUtils();

	private boolean useConstruct = true;

	protected Set<String> skipQueryTokens = new HashSet<>();
	private CBDStructureTree defaultCbdStructure;

	public BenchmarkDescriptionGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
		 cbdGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
	}

	public void setWorkaroundEnabled(boolean enabled) {
		cbdGen.setWorkaround(enabled);
	}

	public void setEndpoint(SparqlEndpoint endpoint) {
		cbdGen.setEndpoint(endpoint);
	}

	protected abstract void beginDocument();
	protected abstract void endDocument();
	protected abstract void beginTable();
	protected abstract void addRow(QueryData queryData);
	protected abstract void endTable();

	public void generateBenchmarkDescription(File benchmarkQueriesFile, boolean withQueryIdGivenInFile) throws Exception{
		Map<String, Query> id2Query = new HashMap<>();
		int id = 1;
		for (String line : Files.readLines(benchmarkQueriesFile, Charsets.UTF_8)) {
			String queryString = line;
			String idString = String.valueOf(id);
			if(withQueryIdGivenInFile) {
				idString = queryString.substring(0, queryString.indexOf(","));
				queryString = queryString.substring(queryString.indexOf(",") + 1);
			}
			Query query = QueryFactory.create(queryString);
			id2Query.put(idString, query);
		}
		generateBenchmarkDescription(id2Query);
	}

	public void generateBenchmarkDescription(Map<String, Query> id2Query) throws Exception{
		beginDocument();
		beginTable();

//		File graphDir = new File("/tmp/graphs/");
//		graphDir.mkdirs();
		for (Map.Entry<String, Query> entry : id2Query.entrySet()) {
			String id = entry.getKey();
			Query query = entry.getValue();
			if (skipQueryTokens.stream().anyMatch(t -> query.toString().contains(t))){
				continue;
			}

			System.out.println(query);

//			exportGraph(query, new File("/tmp/graphs/graph" + id + ".png"));
//			File graphFile = new File(graphDir, "graph" + id + ".png");
//			QueryToGraphExporter.exportYedGraph(query, graphFile, true);

			// column: SPARQL query type
			SPARQLUtils.QueryType queryType = SPARQLUtils.getQueryType(query);

			// column: depth
			int maxDepth = getLongestPath(query);

			List<String> result = SPARQLUtils.getResult(qef, query);

			// column: #instances
			int nrOfInstances = result.size();

			// columns: optimal CBD sizes (min, max, avg)
			DescriptiveStatistics optimalCBDSizeStats = determineOptimalCBDSizes(query, result);

			// columns: generic CBD sizes (min, max, avg)
			DescriptiveStatistics genericCBDSizeStats = determineDefaultCBDSizes(query, result);

			addRow(new QueryData(id, query, queryType, maxDepth, nrOfInstances, optimalCBDSizeStats, genericCBDSizeStats));
		}

		endTable();
		endDocument();
	}

	public void setSkipQueryTokens(Collection<String> skipQueryTokens) {
		this.skipQueryTokens.addAll(skipQueryTokens);
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
						.map(Triple::getSubject)
						.map(s -> utils.extractIncomingTriplePatterns(query, s))
						.flatMap(Collection::stream)
						.collect(Collectors.toSet());
			}
		} else if(type == SPARQLUtils.QueryType.OUT) {
			Set<Triple> tmp = utils.extractOutgoingTriplePatterns(query, query.getProjectVars().get(0));
			while(!tmp.isEmpty()) {
				length++;
				tmp = tmp.stream()
						.filter(tp -> tp.getObject().isVariable())
						.map(Triple::getObject)
						.map(o -> utils.extractOutgoingTriplePatterns(query, o))
						.flatMap(Collection::stream)
						.collect(Collectors.toSet());
			}
		} else {
			length = -1;
		}
		return length;
	}

	public void setDefaultCbdStructure(CBDStructureTree defaultCbdStructure) {
		this.defaultCbdStructure = defaultCbdStructure;
	}

	private CBDStructureTree getDefaultCBDStructureTree() {
		return defaultCbdStructure;
	}

	private DescriptiveStatistics determineDefaultCBDSizes(Query query, List<String> resources) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		NumberFormat df = DecimalFormat.getPercentInstance(Locale.ROOT);
		AtomicInteger idx = new AtomicInteger(1);

		CBDStructureTree cbdStructure = getDefaultCBDStructureTree();
		System.out.println(cbdStructure.toStringVerbose());

		ProgressBar progressBar = new ProgressBar();

		resources.forEach(r -> {
			long cnt = -1;
			if(useConstruct) {
				Model cbd = null;
				try {
//					cbd = cbdGen.getConciseBoundedDescription(r, cbdStructure);
//					cnt = cbd.size();
//					System.out.println(r + ":" + cnt);
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

	private DescriptiveStatistics determineOptimalCBDSizes(Query query, List<String> resources) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		NumberFormat df = DecimalFormat.getPercentInstance(Locale.ROOT);
		AtomicInteger idx = new AtomicInteger(1);

		CBDStructureTree cbdStructure = QueryUtils.getOptimalCBDStructure(query);
		System.out.println(cbdStructure.toStringVerbose());

		ProgressBar progressBar = new ProgressBar();

		resources.forEach(r -> {
			long cnt = -1;
			if(useConstruct) {
				Model cbd = null;
				try {
//					cbd = cbdGen.getConciseBoundedDescription(r, cbdStructure);
//					cnt = cbd.size();
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
			tps.forEach(tp -> graph.insertEdge(parent, null, tp.getPredicate().toString(query.getPrefixMapping()), mapping.get(tp.getSubject()), mapping.get(tp.getObject())));

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

		Map<String, Object> edgeStyle = new HashMap<>();
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

	class QueryData {
		final String id;
		final Query query;
		final SPARQLUtils.QueryType queryType;
		final int maxTreeDepth;
		final int nrOfInstances;
		final DescriptiveStatistics optimalCBDSizeStats;
		final DescriptiveStatistics defaultCBDSizesStats;

		public QueryData(String id, Query query, SPARQLUtils.QueryType queryType, int maxTreeDepth, int nrOfInstances,
						 DescriptiveStatistics optimalCBDSizeStats, DescriptiveStatistics determineDefaultCBDSizes) {
			this.id = id;
			this.query = query;
			this.queryType = queryType;
			this.maxTreeDepth = maxTreeDepth;
			this.nrOfInstances = nrOfInstances;
			this.optimalCBDSizeStats = optimalCBDSizeStats;
			this.defaultCBDSizesStats = determineDefaultCBDSizes;
		}


	}


}


