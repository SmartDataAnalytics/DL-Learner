package org.dllearner.algorithms.qtl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.DBpediaPredicateExistenceFilter;
import org.dllearner.algorithms.qtl.util.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.PredicateDropStatementFilter;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

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
				new PredicateDropStatementFilter(StopURIsRDFS.get()),
				new PredicateDropStatementFilter(StopURIsOWL.get()),
				new ObjectDropStatementFilter(StopURIsOWL.get()),
				new NamespaceDropStatementFilter(
						Sets.newHashSet(
								"http://dbpedia.org/property/", 
//								"http://purl.org/dc/terms/",
								"http://dbpedia.org/class/yago/",
								FOAF.getURI()
								)
								)
				);
		qtf.setMaxDepth(3);
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		
		qef = new QueryExecutionFactoryCacheEx(qef, CacheUtilsH2.createCacheFrontend("/tmp/cache", false, TimeUnit.DAYS.toMillis(60)));
		qef = new QueryExecutionFactoryPaginated(qef);
		
		
		PosNegLPStandard lp = new PosNegLPStandard();
		lp.setPositiveExamples(Sets.<OWLIndividual>newHashSet(
				new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Dresden")),
				new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Leipzig"))));
		
		QTL2DisjunctiveNew la = new QTL2DisjunctiveNew(lp, qef);
		la.setTreeFactory(qtf);
		la.init();
		
		la.start();
		
		List<EvaluatedRDFResourceTree> solutions = la.getSolutionsAsList();
		RDFResourceTree bestSolution = solutions.get(0).getTree();
		DBpediaPredicateExistenceFilter filter = new DBpediaPredicateExistenceFilter(null);
		System.out.println(filter.filter(bestSolution).getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(filter.filter(bestSolution)));
	}

}
