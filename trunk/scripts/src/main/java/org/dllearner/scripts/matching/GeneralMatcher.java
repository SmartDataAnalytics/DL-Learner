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
 *
 */
package org.dllearner.scripts.matching;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Set;

import org.aksw.commons.sparql.sparqler.SelectPaginated;
import org.aksw.commons.sparql.sparqler.Sparqler;
import org.aksw.commons.sparql.sparqler.SparqlerHttp;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosOnlyLP;

import com.hp.hpl.jena.query.QuerySolution;

/**
 * 
 * 
 * @author Jens Lehmann
 *
 */
public class GeneralMatcher {

	public GeneralMatcher(SparqlEndpoint sparqlEndpoint1, SparqlEndpoint sparqlEndpoint2) throws ComponentInitException, LearningProblemUnsupportedException {
		
		// phase 1: collect owl:sameAs links and group them by class
		// option 1: read links from file
		// option 2: use SPARQL to get links
		
		// use AKSW commons SPARQL API
        Sparqler x = new SparqlerHttp(sparqlEndpoint1.getURL().toString());
        SelectPaginated q = new SelectPaginated(x, "query", 1000);
        while(q.hasNext()) {
            QuerySolution qs = q.next();
        }
		
		// phase 2: create learning problems
		
		// phase 3: execute learning algorithms
		// option 1: OWL based (reasoning) using CELOE
		// option 2: RDF based using QTL
		
		Set<String> positiveExamples = null;
		
//		ComponentManager cm = ComponentManager.getInstance();
		SparqlEndpointKS ks = new SparqlEndpointKS(sparqlEndpoint2);
		PosOnlyLP lp = new PosOnlyLP();	
		lp.getConfigurator().setPositiveExamples(positiveExamples);
		QTL qtl = new QTL(lp, ks);
		qtl.init();
		qtl.start();
		String query = qtl.getBestSPARQLQuery();
	}
	
	
	public static void main(String args[]) throws MalformedURLException, ComponentInitException, LearningProblemUnsupportedException {
//		SparqlEndpoint endpoint1 = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		
		// test endpoint at http://lgd.aksw.org:5678/sparql with the following graphs
		// http://www.instancematching.org/oaei/di/sider/
		// http://www.instancematching.org/oaei/di/dailymed/
		// http://www.instancematching.org/oaei/di/linkedmdb/
		// http://www.instancematching.org/oaei/di/diseasome/
		// http://www.instancematching.org/oaei/di/drugbank/
		// http://spatial-data.org/un-fao/
		
		// we can also use https://github.com/LATC/24-7-platform/tree/master/link-specifications
		// as a base (with links read from files)
		
		LinkedList<String> defaultGraphs = new LinkedList<String>();
		defaultGraphs.add("http://linkedgeodata.org");
		SparqlEndpoint endpoint1 = new SparqlEndpoint(new URL("http://linkedgeodata.org/sparql"), defaultGraphs, new LinkedList<String>());		
		
		LinkedList<String> defaultGraphs2 = new LinkedList<String>();
		defaultGraphs2.add("http://dbpedia.org");
		SparqlEndpoint endpoint2 = new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql"), defaultGraphs, new LinkedList<String>());
		
		new GeneralMatcher(endpoint1, endpoint2);
	}
}
