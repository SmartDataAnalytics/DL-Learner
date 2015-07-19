/**
 * 
 */
package org.dllearner.algorithms.qtl.operations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.Entailment;
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
	
	private static QueryTreeFactory treeFactory;
	private static Model model;

	@BeforeClass
	public static void init() {
		String kb = "@prefix : <http://test.org/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":a1 :p1 _:b . "
				+ ":a2 :p1 _:s . _:s :p2 :b ."
				+ ":a3 a :A . "
				+ ":a4 a _:cls . _:cls rdfs:subClassOf :A ."
				+ ":a5 a _:cls2 . _:cls2 rdfs:subClassOf _:cls3 . _:cls3 rdfs:subClassOf :A .";
		
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
		
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/tmp/tree.obj"))) {
			oos.writeObject(tree1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/tmp/tree.obj"))) {
			RDFResourceTree tree = (RDFResourceTree) ois.readObject();
			System.out.println(tree.getStringRepresentation());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSubsumptionRDFS() {
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://test.org/a3", model);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree1));
		print(tree1);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://test.org/a4", model);
		print(tree2);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2, Entailment.RDFS));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree2, tree1, Entailment.RDFS));
		
		RDFResourceTree tree3 = treeFactory.getQueryTree("http://test.org/a5", model);
		print(tree3);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree2, tree3, Entailment.RDFS));
		assertTrue(QueryTreeUtils.isSubsumedBy(tree3, tree2, Entailment.RDFS));
	}
	
	public static void print(RDFResourceTree tree) {
		System.out.println(tree.getStringRepresentation(baseIRI));
	}
}
