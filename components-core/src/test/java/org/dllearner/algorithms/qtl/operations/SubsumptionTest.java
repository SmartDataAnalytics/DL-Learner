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
package org.dllearner.algorithms.qtl.operations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;

import com.google.common.base.StandardSystemProperty;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;

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
				+ "<_:a1> :p1 _:b . "
				+ "<_:a2> :p1 _:s . _:s :p2 :b ."
				+ ":t1 a :A . "
				+ ":t2 a _:cls1 . _:cls1 rdfs:subClassOf :A ."
				+ ":t3 a _:cls2 . _:cls2 rdfs:subClassOf :B ."
				+ ":t4 a _:cls3 . _:cls3 rdfs:subClassOf _:cls4 . _:cls4 rdfs:subClassOf :A ."
				+ ":A rdfs:subClassOf :B ."
				;
		
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
		RDFResourceTree tree1 = treeFactory.getQueryTree(new ResourceImpl(AnonId.create("a1")), model);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree1));
		print(tree1);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree(new ResourceImpl(AnonId.create("a2")), model);
		print(tree2);
		assertTrue(QueryTreeUtils.isSubsumedBy(tree2, tree1));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree1, tree2));
		
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "tree.obj"))) {
			oos.writeObject(tree1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(System.getProperty("java.io.tmpdir") + File.separator + "tree.obj"))) {
			RDFResourceTree tree = (RDFResourceTree) ois.readObject();
			System.out.println(tree.getStringRepresentation());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSubsumptionRDFS() {
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://test.org/t1", model);
		print(tree1);
		
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://test.org/t2", model);
		print(tree2);

		RDFResourceTree tree3 = treeFactory.getQueryTree("http://test.org/t3", model);
		print(tree3);

		RDFResourceTree tree4 = treeFactory.getQueryTree("http://test.org/t4", model);
		print(tree4);

		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree1));

		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2, Entailment.RDFS));
		assertTrue(QueryTreeUtils.isSubsumedBy(tree2, tree1, Entailment.RDFS));
		

		assertTrue(QueryTreeUtils.isSubsumedBy(tree2, tree3, Entailment.RDFS));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree3, tree2, Entailment.RDFS));
	}
	
	public static void print(RDFResourceTree tree) {
		System.out.println(tree.getStringRepresentation(baseIRI));
	}
}
