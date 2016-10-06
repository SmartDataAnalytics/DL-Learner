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
package org.dllearner.algorithms.qtl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;

import org.apache.jena.riot.Lang;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.junit.Test;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class TreeSubsumptionTest{
	
	@Test
	public void treeGenerationTest() throws Exception {
		QueryTreeFactory factory = new QueryTreeFactoryBase();
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read(new FileInputStream("../examples/carcinogenesis/carcinogenesis.owl"), null, Lang.RDFXML.getLabel());
		
		ExtendedIterator<Individual> it = model.listIndividuals();
		while(it.hasNext()) {
			Individual ind = it.next();
//			RDFResourceTree tree = factory.getQueryTree(ind.getURI(), model);
//			
//			String treeString = tree.getStringRepresentation(Rendering.BRACES);
		}
		
	}
	
	@Test
	public void test1(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<>("A", NodeType.RESOURCE);
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2));
	}
	
	@Test
	public void test2(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<>("A", NodeType.RESOURCE);
		tree1.addChild(new QueryTreeImpl<>("B", NodeType.RESOURCE), "r");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		QueryTreeImpl<String> child = new QueryTreeImpl<>("A", NodeType.RESOURCE);
		child.addChild(new QueryTreeImpl<>("B", NodeType.RESOURCE), "r");
		tree2.addChild(child, "r");
		
		assertFalse(QueryTreeUtils.isSubsumedBy(tree1, tree2));
	}
	
	@Test
	public void test3(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		tree1.addChild(new QueryTreeImpl<>("B", NodeType.RESOURCE), "r");
		tree1.addChild(new QueryTreeImpl<>("A", NodeType.RESOURCE), "s");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<>("?");
		tree2.addChild(new QueryTreeImpl<>("A", NodeType.RESOURCE), "r");
		tree2.addChild(new QueryTreeImpl<>("B", NodeType.RESOURCE), "r");
		tree2.addChild(new QueryTreeImpl<>("C", NodeType.RESOURCE), "s");
		
		assertFalse(QueryTreeUtils.isSubsumedBy(tree2, tree1));
	}
	
	@Test
	public void test4(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		QueryTreeImpl<String> child = new QueryTreeImpl<>("?", NodeType.LITERAL);
		tree1.addChild(child, "r");
		child.addChild(new QueryTreeImpl<>("?", NodeType.LITERAL), "s");
		QueryTreeImpl<String> subChild = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		child.addChild(subChild, "t");
		subChild.addChild(new QueryTreeImpl<>("A", NodeType.RESOURCE), "u");
		tree1.dump();
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		child = new QueryTreeImpl<>("?");
		tree2.addChild(child, "r");
		child.addChild(new QueryTreeImpl<>("?", NodeType.LITERAL), "s");
		subChild = new QueryTreeImpl<>("?", NodeType.VARIABLE);
		child.addChild(subChild, "t");
		subChild.addChild(new QueryTreeImpl<>("?", NodeType.VARIABLE), "u");
		tree2.dump();
		
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree2, tree1));
	}

}
