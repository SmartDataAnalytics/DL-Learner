/**
 * 
 */
package org.dllearner.algorithms.qtl.operations;

import java.io.ByteArrayInputStream;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorRDFS;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.reasoning.SPARQLReasoner;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
				+ ":lgg3_4 :r _:D . _:D :p _:F ."
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
		
		RDFResourceTree lggSimple = lggGenSimple.getLGG(tree1, tree2);
		System.out.println(lggSimple.getStringRepresentation());
		assertTrue(lggSimple.isLeaf());
		
		RDFResourceTree lggRDFS = lggGenRDFS.getLGG(tree1, tree2);
		System.out.println(lggRDFS.getStringRepresentation());
		assertTrue(QueryTreeUtils.sameTrees(lggRDFS, tree1));
		
		RDFResourceTree tree3 = treeFactory.getQueryTree("http://test.org/a3", model);
		RDFResourceTree tree4 = treeFactory.getQueryTree("http://test.org/a4", model);
		
		lggRDFS = lggGenRDFS.getLGG(tree3, tree4);
		System.out.println(lggRDFS.getStringRepresentation());
		RDFResourceTree targetLGG = treeFactory.getQueryTree("http://test.org/lgg3_4", model);
		System.out.println(targetLGG.getStringRepresentation());
		assertTrue(QueryTreeUtils.sameTrees(lggRDFS, targetLGG));
	}

}
