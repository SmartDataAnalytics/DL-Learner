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
package org.dllearner.algorithms.qtl;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.algorithms.qtl.util.PrefixCCPrefixMapping;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.StopURIsSKOS;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilterDBpedia;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.SPARQLReasoner;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class QTLTest {
	String cacheDirectory = "cache";
	CacheFrontend cache;

	@Before
	public void setUp() throws Exception {
//		try {
//			long timeToLive = TimeUnit.DAYS.toMillis(30);
//			CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
//			cache = new CacheExImpl(cacheBackend);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}

	@Test
	public void testGetQuestion() throws Exception {
//		PosOnlyLP lp = new PosOnlyLP();
//		lp.setPositiveExamples(new TreeSet<Individual>(Sets.newHashSet(
//				new Individual("http://dbpedia.org/resource/Digital_Fortress"),
//				new Individual("http://dbpedia.org/resource/The_Da_Vinci_Code")
//				)));
//		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql"), "http://dbpedia.org"));
//		ks.setCache(cache);
//		QTL qtl = new QTL(lp, ks);
//		qtl.setPrefixes(PrefixCCMap.getInstance());
//		qtl.init();
//		qtl.start();
	}
	
	public static void main(String[] args) throws Exception {
		QueryTreeFactory qtf = new QueryTreeFactoryBase();
		qtf.addDropFilters(
				new PredicateDropStatementFilter(StopURIsDBpedia.get()),
				new ObjectDropStatementFilter(StopURIsDBpedia.get()),
				new PredicateDropStatementFilter(StopURIsRDFS.get()),
				new PredicateDropStatementFilter(StopURIsOWL.get()),
				new ObjectDropStatementFilter(StopURIsOWL.get()),
				new PredicateDropStatementFilter(StopURIsSKOS.get()),
				new ObjectDropStatementFilter(StopURIsSKOS.get()),
				new NamespaceDropStatementFilter(
						Sets.newHashSet(
								"http://dbpedia.org/property/", 
//								"http://purl.org/dc/terms/",
								"http://dbpedia.org/class/yago/"
//								,FOAF.getURI()
								)
								),
								new PredicateDropStatementFilter(
										Sets.newHashSet(
												"http://www.w3.org/2002/07/owl#equivalentClass", 
												"http://www.w3.org/2002/07/owl#disjointWith"))
				);
		int maxTreeDepth = 2;
		qtf.setMaxDepth(maxTreeDepth);
		
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		ks.init();
		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		qef = CacheUtilsH2.createQueryExecutionFactory(qef, "/tmp/sparql", false, TimeUnit.DAYS.toMillis(30));
		qef = new QueryExecutionFactoryPaginated(qef);
		
		PosNegLPStandard lp = new PosNegLPStandard();
		Set<OWLIndividual> posExamples = Sets.newTreeSet();
		
		String posExStr = "http://dbpedia.org/resource/Big_Star,\n" + 
				"http://dbpedia.org/resource/Falling_in_Reverse,\n" + 
				"http://dbpedia.org/resource/Kid_Canaveral,\n" + 
				"http://dbpedia.org/resource/JEFF_the_Brotherhood,\n" + 
				"http://dbpedia.org/resource/The_Terrible_Twos,\n" + 
				"http://dbpedia.org/resource/Awolnation,\n" + 
				"http://dbpedia.org/resource/Biffy_Clyro,\n" + 
				"http://dbpedia.org/resource/White_Wives,\n" + 
				"http://dbpedia.org/resource/October_Fall,\n" + 
				"http://dbpedia.org/resource/Vandaveer,\n" + 
				"http://dbpedia.org/resource/Silverchair,\n" + 
				"http://dbpedia.org/resource/Karnataka_(band),\n" + 
				"http://dbpedia.org/resource/These_Arms_Are_Snakes,\n" + 
				"http://dbpedia.org/resource/Secret_and_Whisper,\n" + 
				"http://dbpedia.org/resource/Wounded_Knees,\n" + 
				"http://dbpedia.org/resource/Zemlyane,\n" + 
				"http://dbpedia.org/resource/Beautiful_Creatures_(band),\n" + 
				"http://dbpedia.org/resource/World_Under_Blood,\n" + 
				"http://dbpedia.org/resource/Devour_the_Day,\n" + 
				"http://dbpedia.org/resource/Ghostwriters,\n" + 
				"http://dbpedia.org/resource/From_First_to_Last,\n" + 
				"http://dbpedia.org/resource/Gruntruck,\n" + 
				"http://dbpedia.org/resource/Excel_(band),\n" + 
				"http://dbpedia.org/resource/Starpool,\n" + 
				"http://dbpedia.org/resource/Phantasmagoria_(band),\n" + 
				"http://dbpedia.org/resource/Cradle_of_Filth,\n" + 
				"http://dbpedia.org/resource/Lifescreen,\n" + 
				"http://dbpedia.org/resource/I_Am_Kloot,\n" + 
				"http://dbpedia.org/resource/The_Afghan_Whigs,\n" + 
				"http://dbpedia.org/resource/Deus_(band)";
//		String posExStr = "http://dbpedia.org/resource/Ladislaus_the_Posthumous,"
//				+ " http://dbpedia.org/resource/Nga_Kor_Ming, "
//				+ "http://dbpedia.org/resource/L._M._Shaw";
		
		posExStr = "http://dbpedia.org/resource/Super_Robot_Wars_D, http://dbpedia.org/resource/Cosmic_Soldier_(video_game), http://dbpedia.org/resource/Reel_Fishing:_Angler's_Dream, http://dbpedia.org/resource/CR_Parodius_Da!, http://dbpedia.org/resource/Jikandia:_The_Timeless_Land, http://dbpedia.org/resource/Rainbow_Islands_Evolution, http://dbpedia.org/resource/Mega_Man_Battle_Chip_Challenge, http://dbpedia.org/resource/Case_Closed:_The_Mirapolis_Investigation, http://dbpedia.org/resource/Mist_of_Chaos, http://dbpedia.org/resource/Super_Robot_Wars_64, http://dbpedia.org/resource/Panorama_Cotton, http://dbpedia.org/resource/Mad_Stalker:_Full_Metal_Force_(1997_video_game), http://dbpedia.org/resource/Karous, http://dbpedia.org/resource/Gadget_Trial, http://dbpedia.org/resource/Battle_Arena_Toshinden_3, http://dbpedia.org/resource/Record_of_Agarest_War, http://dbpedia.org/resource/Metal_Gear_Solid_2:_Sons_of_Liberty, http://dbpedia.org/resource/Memories_Off:_Yubikiri_no_Kioku, http://dbpedia.org/resource/Harvest_Moon:_Hero_of_Leaf_Valley, http://dbpedia.org/resource/Harvest_Moon:_Back_to_Nature, http://dbpedia.org/resource/Harvest_Moon:_Tree_of_Tranquility, http://dbpedia.org/resource/Ultimate_Shooting_Collection, http://dbpedia.org/resource/Shantae:_Half-Genie_Hero, http://dbpedia.org/resource/Apocalypse:_Desire_Next, http://dbpedia.org/resource/Katekyo_Hitman_Reborn!_Dream_Hyper_Battle!, http://dbpedia.org/resource/Harvest_Moon_DS:_Grand_Bazaar, http://dbpedia.org/resource/Chaos_Field, http://dbpedia.org/resource/Charinko_Hero, http://dbpedia.org/resource/Blue_Flow, http://dbpedia.org/resource/Generation_of_Chaos";
		for (String uri : Splitter.on(", ").trimResults().split(posExStr)) {
			posExamples.add(new OWLNamedIndividualImpl(IRI.create(uri)));
		}
		lp.setPositiveExamples(posExamples);
		
		AbstractReasonerComponent reasoner = new SPARQLReasoner(qef);
		reasoner.setPrecomputeClassHierarchy(true);
		reasoner.setPrecomputeObjectPropertyHierarchy(true);
		reasoner.setPrecomputeDataPropertyHierarchy(true);
		reasoner.init();
		
		QTL2Disjunctive la = new QTL2Disjunctive(lp, qef);
		la.setReasoner(reasoner);
		la.setTreeFactory(qtf);
		la.setEntailment(Entailment.SIMPLE);
		la.setMaxTreeDepth(maxTreeDepth);
		la.init();
		
		la.start();
		
		List<EvaluatedRDFResourceTree> solutions = la.getSolutionsAsList();
		System.out.println(solutions.size());
		
		RDFResourceTree bestSolution = solutions.get(0).getTree();
		System.out.println(bestSolution.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(bestSolution));
		
		PredicateExistenceFilterDBpedia filter = new PredicateExistenceFilterDBpedia(null);
		System.out.println(filter.filter(bestSolution).getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(filter.filter(bestSolution)));
		
		QueryTreeUtils.asGraph(bestSolution, null, PrefixCCPrefixMapping.Full, new File("/tmp/tree.graphml"));
	}

}
