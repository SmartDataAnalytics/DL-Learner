package org.dllearner.algorithms.qtl;

import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.junit.Before;
import org.junit.Test;

public class QTLTest {
	String cacheDirectory = "cache";
	CacheEx cache;

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

}
