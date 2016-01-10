package org.dllearner.algorithms.qtl.operations;

import java.io.ByteArrayInputStream;

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
