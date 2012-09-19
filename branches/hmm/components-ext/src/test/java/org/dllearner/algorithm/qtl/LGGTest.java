/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
 *
 */
package org.dllearner.algorithm.qtl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithm.qtl.examples.DBpediaExample;
import org.dllearner.algorithm.qtl.examples.LinkedGeoDataExample;
import org.dllearner.algorithm.qtl.exception.TimeOutException;
import org.dllearner.algorithm.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithm.qtl.operations.NBR;
import org.dllearner.algorithm.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithm.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGTest {
	
	private static final Logger logger = Logger.getLogger(LGGTest.class);
	
	@Test
	public void testOxfordData(){
		OntModel model = ModelFactory.createOntologyModel();
		int depth = 3;
		try {
			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/ontology.ttl")), null, "TURTLE");
			System.out.println(model.size());
			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/finders.ttl")), "http://diadem.cs.ox.ac.uk/ontologies/real-estate#", "TURTLE");
			System.out.println(model.size());
//			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/martinco.ttl")), null, "TURTLE");
//			System.out.println(model.size());
//			model.write(new FileOutputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/all.ttl")), "TURTLE", null);
//			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/all.ttl")), null, "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		for(Statement s : model.listStatements().toList()){
//			System.out.println(s);
//		}
//		
//		ResultSet rs1 = QueryExecutionFactory.create("SELECT * WHERE {?s <http://diadem.cs.ox.ac.uk/ontologies/real-estate#rooms> ?o. ?o ?p ?o1}", model).execSelect();
//		System.out.println(ResultSetFormatter.asText(rs1));
		
		LocalModelBasedSparqlEndpointKS ks = new LocalModelBasedSparqlEndpointKS(model);
		
		ConciseBoundedDescriptionGenerator cbd = new ConciseBoundedDescriptionGeneratorImpl(model);
		QueryTreeFactory<String> qtf = new QueryTreeFactoryImpl();
		
		List<String> posExamples = Arrays.asList("http://diadem.cs.ox.ac.uk/ontologies/real-estate#inst004",
				"http://diadem.cs.ox.ac.uk/ontologies/real-estate#inst005");
		
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		
		//get the trees for the positive examples of depth 3
		QueryTree<String> tree;
		for(String ex : posExamples){
			tree = qtf.getQueryTree(ex, cbd.getConciseBoundedDescription(ex, depth));
			trees.add(tree);
			System.out.println(tree.getStringRepresentation());
		}
		
		//compute the LGG
		LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGen.getLGG(trees);
		System.out.println("LGG:\n" + lgg.getStringRepresentation());
		Query q = lgg.toSPARQLQuery();
		System.out.println("Query:\n" + q);
		
		//run the SPARQL query against the data - should be returned at least the positive examples
		List<String> result = new ArrayList<String>();
		ResultSet rs = QueryExecutionFactory.create(q, model).execSelect();
		while(rs.hasNext()){
			result.add(rs.next().getResource("x0").getURI());
		}
		System.out.println(result);
		Assert.assertTrue(result.containsAll(posExamples));
		
		NBR<String> nbr = new NBR<String>(model);
		try {
			nbr.setLGGInstances(new HashSet<String>(posExamples));
			String question = nbr.getQuestion(lgg, new ArrayList<QueryTree<String>>(), posExamples);
			System.out.println(question);
		} catch (TimeOutException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testLGGWithDBpediaExample(){
		QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
		
		List<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("TREE " + cnt);
			tree.dump();
			System.out.println("-----------------------------");
			cnt++;
			System.out.println(((QueryTreeImpl<String>)tree).toQuery());
		}
		
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		
		System.out.println("LGG");
		lgg.dump();
		
		QueryTreeImpl<String> tree = factory.getQueryTree("?");
		QueryTreeImpl<String> subTree1 = new QueryTreeImpl<String>("?");
		subTree1.addChild(new QueryTreeImpl<String>("?"), "leaderParty");
		subTree1.addChild(new QueryTreeImpl<String>("?"), "population");
		subTree1.addChild(new QueryTreeImpl<String>("Germany"), "locatedIn");
		tree.addChild(subTree1, "birthPlace");
		tree.addChild(new QueryTreeImpl<String>("?"), RDFS.label.toString());
		QueryTreeImpl<String> subTree2 = new QueryTreeImpl<String>("Person");
		subTree2.addChild(new QueryTreeImpl<String>(OWL.Thing.toString()), RDFS.subClassOf.toString());
		tree.addChild(subTree2, RDF.type.toString());
		QueryTreeImpl<String> subTree3 = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> subSubTree = new QueryTreeImpl<String>("Person");
		subSubTree.addChild(new QueryTreeImpl<String>(OWL.Thing.toString()), RDFS.subClassOf.toString());
		subTree3.addChild(subSubTree, RDFS.subClassOf.toString());
		tree.addChild(subTree3, RDF.type.toString());
		
		Assert.assertTrue(lgg.isSameTreeAs(tree));
		
		System.out.println(tree.toSPARQLQueryString());
		
	}
	
	@Test
	public void testLGGWithLinkedGeoDataExample(){
		QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
		
		List<QueryTree<String>> posExampleTrees = LinkedGeoDataExample.getPosExampleTrees();
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("TREE " + cnt);
			tree.dump();
			System.out.println("-----------------------------");
			cnt++;
		}
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		
		System.out.println("LGG");
		lgg.dump();
		
		QueryTreeImpl<String> tree = factory.getQueryTree("?");
		QueryTreeImpl<String> subTree = new QueryTreeImpl<String>("lgdo:Aerodome");
		subTree.addChild(new QueryTreeImpl<String>("lgdo:Aeroway"), RDFS.subClassOf.toString());
		tree.addChild(subTree, RDF.type.toString());
		tree.addChild(new QueryTreeImpl<String>("?"), RDFS.label.toString());
		tree.addChild(new QueryTreeImpl<String>("?"), "geo:long");
		tree.addChild(new QueryTreeImpl<String>("?"), "geo:lat");
		tree.addChild(new QueryTreeImpl<String>("?"), "georss:point");
		tree.addChild(new QueryTreeImpl<String>("?"), "lgdp:icao");
		
		Assert.assertTrue(lgg.isSameTreeAs(tree));
		
		System.out.println(tree.toSPARQLQueryString());
		
	}
	


}
