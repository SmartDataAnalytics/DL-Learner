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

import com.google.common.base.Joiner;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.utilities.QueryUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains a knowledge base and a set of SPARQL queries.
 * @author Lorenz Buehmann
 *
 */
public abstract class EvaluationDataset {



	String name;

	SparqlEndpointKS ks;
	String baseIRI;
	PrefixMapping prefixMapping;
	
	AbstractReasonerComponent reasoner;
	
	List<String> sparqlQueries;
	List<Predicate<Statement>> queryTreeFilters = new ArrayList<>();

	public EvaluationDataset(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public SparqlEndpointKS getKS() {
		return ks;
	}
	
	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}
	
	public String getBaseIRI() {
		return baseIRI;
	}
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}
	
	public List<String> getSparqlQueries() {
		return sparqlQueries;
	}
	
	public List<Predicate<Statement>> getQueryTreeFilters() {
		return queryTreeFilters;
	}

	/**
	 * Writes the SPARQL queries line-wise to file.
	 *
	 * @param file the file
	 */
	public void saveToDisk(File file) throws IOException {
		Files.write(file.toPath(), sparqlQueries.stream().map(q -> q.replace("\n", " ")).collect(Collectors.toList()));
	}

	public void analyze() {
		ConciseBoundedDescriptionGenerator cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());

		String tsv = sparqlQueries.stream().map(QueryFactory::create).map(q -> {
			StringBuilder sb = new StringBuilder();
			sb.append(q.toString().replace("\n", " "));
			try {
				// get query result
				List<String> result = SPARQLUtils.getResult(ks.getQueryExecutionFactory(), q);
				sb.append("\t").append(result.size());

				// query type
				SPARQLUtils.QueryType queryType = SPARQLUtils.getQueryType(q);
				sb.append("\t").append(queryType.name());


//				// check CBD sizes and time
//				Monitor mon = MonitorFactory.getTimeMonitor("CBD");
//				mon.reset();
//				DescriptiveStatistics sizeStats = new DescriptiveStatistics();
//				result.stream()
//						.map(r -> {
//							System.out.println(r);
//							mon.start();
//							Model cbd = cbdGen.getConciseBoundedDescription(r, 2);
//							mon.stop();
//							return cbd;
//						})
//						.map(Model::size)
//						.forEach(sizeStats::addValue);
//
//				// show min., max. and avg. size
//				sb.append("\t").append(sizeStats.getMin());
//				sb.append("\t").append(sizeStats.getMax());
//				sb.append("\t").append(sizeStats.getMean());
//
//				// show min., max. and avg. CBD time
//				sb.append("\t").append(mon.getTotal());
//				sb.append("\t").append(mon.getMin());
//				sb.append("\t").append(mon.getMax());
//				sb.append("\t").append(mon.getAvg());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return sb;
		}).collect(Collectors.joining("\n"));

		System.out.println(tsv);
	}
}
