/**
 * 
 */
package org.dllearner.algorithms.qtl.operations;

import java.io.ByteArrayInputStream;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author Lorenz Buehmann
 *
 */
public class SubsumptionTest {
	
	private static final String baseIRI = "http://test.org/";
	
	private static QueryTreeFactoryBase treeFactory;
	private static Model model;

	@BeforeClass
	public static void init() {
		String kb = "@prefix : <http://test.org/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":a1 :p1 _:b . "
				+ ":a2 :p1 _:s . _:s :p2 :b ."
				+ ":a3 a :A . "
				+ ":a4 a _:cls . _:cls rdfs:subClassOf :A .";
		
		model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(kb.getBytes()), null, "TURTLE");
		
		treeFactory = new QueryTreeFactoryBase();
		
		StmtIterator statements = model.listStatements();
		while (statements.hasNext()) {
			Statement st = statements.next();
			System.out.println(st);
			
		}
	}

	@Test
	public void testSubsumptionSimple() {
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://test.org/a1", model);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree1));
		print(tree1);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://test.org/a2", model);
		print(tree2);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree2, tree1));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree1, tree2));
	}
	
	@Test
	public void testSubsumptionRDFS() {
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://test.org/a3", model);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree1));
		print(tree1);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://test.org/a4", model);
		print(tree2);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree2, tree1));
	}
	
	public static void print(RDFResourceTree tree) {
		System.out.println(tree.getStringRepresentation(baseIRI));
	}
}
