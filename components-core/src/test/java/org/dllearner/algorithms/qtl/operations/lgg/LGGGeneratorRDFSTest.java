package org.dllearner.algorithms.qtl.operations.lgg;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.reasoning.SPARQLReasoner;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

/**
 * @author Lorenz Buehmann
 */
public class LGGGeneratorRDFSTest {

	ConciseBoundedDescriptionGenerator cbdGenerator;
	QueryTreeFactory treeFactory;
	LGGGeneratorRDFS lggGen;

	String NS = "http://dl-learner.org/test/";
	private int maxDepth = 2;

	@Before
	public void setUp() throws Exception {
		String kb = "" +
				"@prefix : <http://dl-learner.org/test/> ." +
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." +
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." +
				":x1 :r :y1 ." +
				":x2 rdf:type :A ." +
				":r rdfs:domain :A ." +

				":x3 :s :y2 ." +
				":x4 rdf:type :B ." +
				":s rdfs:domain :C ." +
				":B rdfs:subClassOf :C ." +

				":x5 :t :y3 ." +
				":y3 rdf:type :B ." +
				":x6 :t :y4 ." +
				":t rdfs:range :C ." +
				":B rdfs:subClassOf :C .";

		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, new StringReader(kb), null, Lang.TURTLE);

		SparqlEndpointKS ks = new LocalModelBasedSparqlEndpointKS(model);
		ks.init();

		AbstractReasonerComponent reasoner = new SPARQLReasoner(ks);
		reasoner.setPrecomputeClassHierarchy(true);
		reasoner.init();

		cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());

		treeFactory = new QueryTreeFactoryBase();

		lggGen = new LGGGeneratorRDFS(reasoner);

	}

	private RDFResourceTree getTree(String uri) {
		return treeFactory.getQueryTree(uri, cbdGenerator.getConciseBoundedDescription(uri, maxDepth), maxDepth);
	}

	@Test
	public void getLGG() throws Exception {
		compute(NS + "x1", NS + "x2");
		compute(NS + "x3", NS + "x4");
		compute(NS + "x5", NS + "x6");
	}

	private void compute(String tree1URI, String tree2URI) {
		RDFResourceTree tree1 = getTree(tree1URI);
		RDFResourceTree tree2 = getTree(tree2URI);

		System.out.println(tree1.getStringRepresentation());
		System.out.println(tree2.getStringRepresentation());

		RDFResourceTree lgg = lggGen.getLGG(tree1, tree2);
		System.out.println(lgg.getStringRepresentation());
	}

}