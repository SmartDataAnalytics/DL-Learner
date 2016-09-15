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

import java.io.ByteArrayInputStream;
import java.io.File;

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
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
	public static void init() throws ComponentInitException {
		String kb = "@prefix : <http://test.org/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":a1 :r :A . "
				+ ":a2 :s :A . "
				+ ":a3 :r :C . :C :p :E ."
				+ ":a4 :s :D . :D :p :F ."
				+ "<_:lgg1_2> :r :A ."
				+ "<_:lgg3_4> :r _:D . _:D :p _:F ."
				+ ":r rdfs:subPropertyOf :s ."
				+ ":a5 a :A ."
				+ ":a6 a :B ."
				+ ":A rdfs:subClassOf :B ."
				+ "<_:lgg5_6> a :B ."
				;
		
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
		reasoner.init();
		
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
	public void testClassEntailment() {
		RDFResourceTree tree1 = treeFactory.getQueryTree("http://test.org/a5", model);
		RDFResourceTree tree2 = treeFactory.getQueryTree("http://test.org/a6", model);

		System.out.println("Tree 1\n" + tree1.getStringRepresentation());
		System.out.println("Tree 2\n" + tree2.getStringRepresentation());

		RDFResourceTree lggSimple = lggGenSimple.getLGG(tree1, tree2);
		System.out.println("LGG_simple(T1,T2)\n" + lggSimple.getStringRepresentation());

		assertTrue(lggSimple.isLeaf());

		RDFResourceTree targetLGG = treeFactory.getQueryTree(new ResourceImpl(AnonId.create("lgg5_6")), model);
		System.out.println("Target LGG\n" + targetLGG.getStringRepresentation());

		RDFResourceTree lggRDFS = lggGenRDFS.getLGG(tree1, tree2);
		System.out.println("LGG_RDFS(T1,T2)\n" + lggRDFS.getStringRepresentation());

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
	
//	@Test
	public void correctness() {
		treeFactory.setMaxDepth(2);
		java.util.List<Filter<Statement>> var = new DBpediaEvaluationDataset(new File("/tmp/lggtest"), SparqlEndpoint.getEndpointDBpedia()).getQueryTreeFilters();
		treeFactory.addDropFilters((Filter<Statement>[]) var.toArray(new Filter[var.size()]));
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
