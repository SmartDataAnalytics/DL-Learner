package org.dllearner.algorithms.qtl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.jena.riot.Lang;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree.Rendering;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.junit.Test;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



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
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2));
	}
	
	@Test
	public void test2(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
		tree1.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
		child.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		tree2.addChild(child, "r");
		
		assertFalse(QueryTreeUtils.isSubsumedBy(tree1, tree2));
	}
	
	@Test
	public void test3(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		tree1.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		tree1.addChild(new QueryTreeImpl<String>("A", NodeType.RESOURCE), "s");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		tree2.addChild(new QueryTreeImpl<String>("A", NodeType.RESOURCE), "r");
		tree2.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		tree2.addChild(new QueryTreeImpl<String>("C", NodeType.RESOURCE), "s");
		
		assertFalse(QueryTreeUtils.isSubsumedBy(tree2, tree1));
	}
	
	@Test
	public void test4(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("?", NodeType.LITERAL);
		tree1.addChild(child, "r");
		child.addChild(new QueryTreeImpl<String>("?", NodeType.LITERAL), "s");
		QueryTreeImpl<String> subChild = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		child.addChild(subChild, "t");
		subChild.addChild(new QueryTreeImpl<String>("A", NodeType.RESOURCE), "u");
		tree1.dump();
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		child = new QueryTreeImpl<String>("?");
		tree2.addChild(child, "r");
		child.addChild(new QueryTreeImpl<String>("?", NodeType.LITERAL), "s");
		subChild = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
		child.addChild(subChild, "t");
		subChild.addChild(new QueryTreeImpl<String>("?", NodeType.VARIABLE), "u");
		tree2.dump();
		
		assertTrue(QueryTreeUtils.isSubsumedBy(tree1, tree2));
		assertFalse(QueryTreeUtils.isSubsumedBy(tree2, tree1));
	}

}
