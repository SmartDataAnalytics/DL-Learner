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
package org.dllearner.algorithms.qtl.experiments.datasets;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.experiments.SPARQLUtils;
import org.dllearner.algorithms.qtl.util.filters.AbstractTreeFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains a knowledge base and a set of SPARQL queries.
 * @author Lorenz Buehmann
 *
 */
public abstract class EvaluationDataset {

	protected String name;

	protected SparqlEndpointKS examplesKS;
	protected SparqlEndpointKS ks;
	protected String baseIRI;
	protected PrefixMapping prefixMapping;

	protected AbstractReasonerComponent reasoner;

	protected Map<String, Query> sparqlQueries = new TreeMap<>();

	protected List<Predicate<Statement>> queryTreeFilters = new ArrayList<>();
	protected Set<AbstractTreeFilter<RDFResourceTree>> treeFilters = new HashSet<>();

	protected  PredicateExistenceFilter predicateFilter;

	public EvaluationDataset(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public SparqlEndpointKS getKS() {
		return ks;
	}

	public SparqlEndpointKS getExamplesKS() {
		return examplesKS;
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
	
	public Map<String, Query> getSparqlQueries() {
		return sparqlQueries;
	}
	
	public List<Predicate<Statement>> getQueryTreeFilters() {
		return queryTreeFilters;
	}

	public PredicateExistenceFilter getPredicateFilter() {
		return predicateFilter;
	}

	public void setPredicateFilter(PredicateExistenceFilter predicateFilter) {
		this.predicateFilter = predicateFilter;
	}

	public Set<AbstractTreeFilter<RDFResourceTree>> getTreeFilters() {
		return treeFilters;
	}

	public abstract boolean usesStrictOWLTypes();

	/**
	 * Writes the ID and SPARQL queries line-wise to file.
	 *
	 * @param file the file
	 */
	public void saveToDisk(File file) throws IOException {
		// adjust the PREFIX declarations
		sparqlQueries.entrySet().stream().forEach(entry -> adjustPrefixes(entry.getValue()));

		// create directory and file if not exist
		java.nio.file.Path pathToFile = file.toPath();
		Files.createDirectories(pathToFile.getParent());
		Files.createFile(pathToFile);

		// write ID + queries to disk
		Files.write(pathToFile,
				sparqlQueries.entrySet().stream()
						.map(entry -> entry.getKey() + ", " + entry.getValue().toString().replace("\n", " "))
						.collect(Collectors.toList()));
	}

	protected void adjustPrefixes(Query query) {
		query.getPrefixMapping().removeNsPrefix("owl");
		query.getPrefixMapping().removeNsPrefix("rdfs");
		query.getPrefixMapping().removeNsPrefix("foaf");
		query.getPrefixMapping().removeNsPrefix("rdf");

		prefixMapping.getNsPrefixMap().forEach((key, value) -> {
            if (query.toString().contains(value)) {
                query.getPrefixMapping().setNsPrefix(key, value);
            }
        });

//		if(query.toString().contains("http://dbpedia.org/ontology/")) {
//			query.getPrefixMapping().setNsPrefix("dbo", "http://dbpedia.org/ontology/");
//		}
//		if(query.toString().contains("http://dbpedia.org/property/")) {
//			query.getPrefixMapping().setNsPrefix("dbp", "http://dbpedia.org/property/");
//		}
//		if(query.toString().contains("http://xmlns.com/foaf/0.1/")) {
//			query.getPrefixMapping().setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
//		}
//		if(query.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#") || query.toString().contains(" a ")) {
//			query.getPrefixMapping().setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
//		}
//		if(query.toString().contains("http://dbpedia.org/resource/")) {
//			query.getPrefixMapping().setNsPrefix("", "http://dbpedia.org/resource/");
//		}
	}

	public void analyze() {
		ConciseBoundedDescriptionGenerator cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());

		String separator = "\t";
		String tsv = sparqlQueries.entrySet().stream().map(entry -> {
			StringBuilder sb = new StringBuilder();

			// ID
			String id = entry.getKey();
			sb.append(id).append(separator);

			// query
			Query q = entry.getValue();
			sb.append(q.toString().replace("\n", " "));
			try {
				// get query result
				List<String> result = SPARQLUtils.getResult(ks.getQueryExecutionFactory(), q);
				sb.append(separator).append(result.size());

				// query type
				SPARQLUtils.QueryType queryType = SPARQLUtils.getQueryType(q);
				sb.append(separator).append(queryType.name());

				// check CBD sizes and time
				Monitor mon = MonitorFactory.getTimeMonitor("CBD");
				mon.reset();
				DescriptiveStatistics sizeStats = new DescriptiveStatistics();
				result.stream()
						.map(r -> {
							System.out.println(r);
							mon.start();
							Model cbd = cbdGen.getConciseBoundedDescription(r, 2);
							mon.stop();
							return cbd;
						})
						.map(Model::size)
						.forEach(sizeStats::addValue);

				// show min., max. and avg. size
				sb.append(separator).append(sizeStats.getMin());
				sb.append(separator).append(sizeStats.getMax());
				sb.append(separator).append(sizeStats.getMean());

				// show min., max. and avg. CBD time
				sb.append(separator).append(mon.getTotal());
				sb.append(separator).append(mon.getMin());
				sb.append(separator).append(mon.getMax());
				sb.append(separator).append(mon.getAvg());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return sb;
		}).collect(Collectors.joining("\n"));

		System.out.println(tsv);
	}
}
