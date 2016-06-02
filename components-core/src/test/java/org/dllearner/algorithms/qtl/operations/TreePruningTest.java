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

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * @author Lorenz Buehmann
 *
 */
public class TreePruningTest {
	
private static final String baseIRI = "http://test.org/";
	
	private static QueryTreeFactory treeFactory;
	private static Model model;
	
	@BeforeClass
	public static void init() {
		String kb = "@prefix : <http://test.org/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":a :p1 :b . "
					+ ":b a :A , :B ; :p2 \"lit\" ."
				+ ":a :p2 _:b1 . _:b1 a :C . "
				+ ":a :p2 _:b2 . _:b2 a :C . "
				+ ":a :p2 _:b3 . "
				
				+ ":t2 :p :c . :c a :A, _:b7 . "
				+ ":t2 a _:b4 . "
				+ ":t2 a _:b5 . _:b5 rdfs:subClassOf _:b6 ."
				
				+ ":t3 :p :c . :c a :D; :p _:b7 . _:b7 :p1 _:b8 ."
				+ ":t3 a _:b4 . ";
//		String kb = "@prefix : <http://test.org/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
//				+ ":a a :A, :B ; :p :b . ";
		
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
	public void testPruneTree(){
		RDFResourceTree tree = treeFactory.getQueryTree("http://test.org/a", model);
		System.out.println(tree.getStringRepresentation());
		QueryTreeUtils.prune(tree, null, Entailment.RDF);
		System.out.println(tree.getStringRepresentation());
		
		tree = treeFactory.getQueryTree("http://test.org/t2", model);
		System.out.println(tree.getStringRepresentation());
		QueryTreeUtils.prune(tree, null, Entailment.RDF);
		System.out.println(tree.getStringRepresentation());
		
		tree = treeFactory.getQueryTree("http://test.org/t3", model);
		System.out.println(tree.getStringRepresentation());
		QueryTreeUtils.removeVarLeafs(tree);
		System.out.println(tree.getStringRepresentation());
	}

}
