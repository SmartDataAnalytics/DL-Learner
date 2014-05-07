package org.dllearner.algorithms.qtl;

import static org.junit.Assert.fail;

import java.net.URL;
import java.sql.SQLException;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.PrefixCCMap;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class QTLTest {
	String cacheDirectory = "cache";
	CacheEx cache;

	@Before
	public void setUp() throws Exception {
		try {
			long timeToLive = TimeUnit.DAYS.toMillis(30);
			CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
			cache = new CacheExImpl(cacheBackend);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetQuestion() throws Exception {
		PosOnlyLP lp = new PosOnlyLP();
		lp.setPositiveExamples(new TreeSet<Individual>(Sets.newHashSet(
				new Individual("http://dbpedia.org/resource/Digital_Fortress"),
				new Individual("http://dbpedia.org/resource/The_Da_Vinci_Code")
				)));
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql"), "http://dbpedia.org"));
		ks.setCache(cache);
		QTL qtl = new QTL(lp, ks);
		qtl.setPrefixes(PrefixCCMap.getInstance());
		qtl.init();
		qtl.start();
	}

	@Test
	public void testStart() {
		fail("Not yet implemented");
	}

}
