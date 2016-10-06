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
package org.dllearner.algorithms.qtl.qald;

import java.net.URL;

import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.google.common.collect.Lists;
import org.apache.jena.shared.PrefixMapping;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaKB extends KB {
	
	public DBpediaKB() throws Exception{
		id = "DBpedia";
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		endpoint = new SparqlEndpoint(
//			new URL("http://akswnc3.informatik.uni-leipzig.de:8860/sparql"), 
				new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), 
				"http://dbpedia.org");
		ks = new SparqlEndpointKS(endpoint);
		ks.setCacheDir("./cache-qtl/qtl-qald-iswc2015-cache;mv_store=false");
		ks.init();
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.setPrecomputeClassHierarchy(true);
		reasoner.setPrecomputeObjectPropertyHierarchy(true);
		reasoner.setPrecomputeDataPropertyHierarchy(true);
		reasoner.init();
		
		questionFiles = Lists.newArrayList(
				"org/dllearner/algorithms/qtl/qald-4_multilingual_train.xml",
				"org/dllearner/algorithms/qtl/qald-4_multilingual_test.xml"
				);
		
		baseIRI = "http://dbpedia.org/resource/";
		prefixMapping = PrefixMapping.Factory.create().withDefaultMappings(PrefixMapping.Standard);
		prefixMapping.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		prefixMapping.setNsPrefix("wiki", "http://wikidata.dbpedia.org/resource/");
		prefixMapping.setNsPrefix("odp-dul", "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#");
		prefixMapping.setNsPrefix("schema", "http://schema.org/");
		
		
		
	}

}
