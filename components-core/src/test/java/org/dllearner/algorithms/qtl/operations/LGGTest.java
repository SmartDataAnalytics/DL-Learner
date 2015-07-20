/**
 * 
 */
package org.dllearner.algorithms.qtl.operations;

import java.io.ByteArrayInputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.experiments.DBpediaEvaluationDataset;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorRDFS;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.reasoning.SPARQLReasoner;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.util.iterator.Filter;

import static org.junit.Assert.*;

/**
 * @author Lorenz Buehmann
 *
 */
public class LGGTest {
	
private static final String baseIRI = "http://test.org/";
	
	private static QueryTreeFactory treeFactory;
	private static Model model;

	private static AbstractReasonerComponent reasoner;

	private static LGGGenerator lggGenSimple;
	private static LGGGenerator lggGenRDFS;
	
	@BeforeClass
	public static void init() {
		String kb = "@prefix : <http://test.org/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":a1 :r :A . "
				+ ":a2 :s :A . "
				+ ":a3 :r :C . :C :p :E ."
				+ ":a4 :s :D . :D :p :F ."
				+ "<_:lgg1_2> :r :A ."
				+ "<_:lgg3_4> :r _:D . _:D :p _:F ."
				+ ":r rdfs:subPropertyOf :s .";
		
		model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(kb.getBytes()), null, "TURTLE");
		
		treeFactory = new QueryTreeFactoryBase();
		
		StmtIterator statements = model.listStatements();
		while (statements.hasNext()) {
			Statement st = statements.next();
			System.out.println(st);
			
		}
		
		reasoner = new SPARQLReasoner(model);
		reasoner.setPrecomputeObjectPropertyHierarchy(false);
		reasoner.setPrecomputeDataPropertyHierarchy(false);
		
		lggGenSimple = new LGGGeneratorSimple();
		lggGenRDFS = new LGGGeneratorRDFS(reasoner);
	}
	
	@Test
	public void testPropertyEntailment() {
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://test.org/a1", model);
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://test.org/a2", model);
		
		System.out.println("Tree 1\n" + tree1.getStringRepresentation());
		System.out.println("Tree 2\n" + tree2.getStringRepresentation());
		
		RDFResourceTree lggSimple = lggGenSimple.getLGG(tree1, tree2);
		System.out.println("LGG_simple(T1,T2)\n" + lggSimple.getStringRepresentation());
		
		assertTrue(lggSimple.isLeaf());
		
		RDFResourceTree targetLGG = treeFactory.getQueryTree(new ResourceImpl(AnonId.create("lgg1_2")), model);
		System.out.println("Target LGG\n" + targetLGG.getStringRepresentation());
		
		RDFResourceTree lggRDFS = lggGenRDFS.getLGG(tree1, tree2);
		System.out.println("LGG_RDFS(T1,T2)\n" + lggRDFS.getStringRepresentation());
		
		assertTrue(QueryTreeUtils.sameTrees(lggRDFS, targetLGG));
		
		RDFResourceTree tree3 = treeFactory.getQueryTree("http://test.org/a3", model);
		RDFResourceTree tree4 = treeFactory.getQueryTree("http://test.org/a4", model);
		
		System.out.println("Tree 3\n" + tree1.getStringRepresentation());
		System.out.println("Tree 4\n" + tree2.getStringRepresentation());
		
		targetLGG = treeFactory.getQueryTree(new ResourceImpl(AnonId.create("lgg3_4")), model);
		System.out.println("Target LGG\n" + targetLGG.getStringRepresentation());
		
		lggRDFS = lggGenRDFS.getLGG(tree3, tree4);
		System.out.println("LGG_RDFS(T3,T4)\n" + lggRDFS.getStringRepresentation());
		
		assertTrue(QueryTreeUtils.sameTrees(lggRDFS, targetLGG));
	}
	
	@Test
	public void testPerformance() {
		// http://dbpedia.org/resource/Awolnation
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(
				model,
				this.getClass().getClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/dbpedia-Awolnation.ttl"), 
				Lang.TURTLE);
		
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://dbpedia.org/resource/Awolnation", model);
		
		// http://dbpedia.org/resource/Big_Star
		model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(
				model,
				this.getClass().getClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/dbpedia-Big_Star.ttl"), 
				Lang.TURTLE);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://dbpedia.org/resource/Big_Star", model);
		long start = System.currentTimeMillis();
		RDFResourceTree lggSimple = lggGenSimple.getLGG(tree1, tree2);
		long end = System.currentTimeMillis();
		System.out.println("Operation took " + (end - start) + "ms");
		
//		System.out.println(lggSimple.getStringRepresentation());
	}
	
	@Test
	public void testCorrectness() {
		treeFactory.setMaxDepth(2);
		treeFactory.addDropFilters((Filter<Statement>[]) new DBpediaEvaluationDataset().getQueryTreeFilters().toArray(new Filter[]{}));
		// http://dbpedia.org/resource/Battle_Arena_Toshinden_3
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(
				model,
				this.getClass().getClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/dbpedia-Battle_Arena_Toshinden_3.ttl"), 
				Lang.TURTLE);
		
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://dbpedia.org/resource/Battle_Arena_Toshinden_3", model);
		
		// http://dbpedia.org/resource/Metal_Gear_Solid_2:_Sons_of_Liberty
		model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(
				model,
				this.getClass().getClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/dbpedia-Metal_Gear_Solid_2:_Sons_of_Liberty.ttl"), 
				Lang.TURTLE);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://dbpedia.org/resource/Metal_Gear_Solid_2:_Sons_of_Liberty", model);
		long start = System.currentTimeMillis();
		RDFResourceTree lggSimple = lggGenSimple.getLGG(tree1, tree2);
		long end = System.currentTimeMillis();
		System.out.println("Operation took " + (end - start) + "ms");
		
		System.out.println(lggSimple.getStringRepresentation());
	}

}
