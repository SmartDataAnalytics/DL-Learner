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
package org.dllearner.algorithms.qtl;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.examples.DBpediaExample;
import org.dllearner.algorithms.qtl.examples.LinkedGeoDataExample;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
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
	public void testNoiseLGGWithDBpediaExample(){
		List<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("TREE " + cnt);
			tree.dump();
			System.out.println("-----------------------------");
			cnt++;
			System.out.println(((QueryTreeImpl<String>)tree).toQuery());
		}
		
		
//		NoiseSensitiveLGG<String> lggGenerator = new NoiseSensitiveLGG<String>();
//		List<EvaluatedQueryTree<String>> lggs = lggGenerator.computeLGG(posExampleTrees);
//		
//		int i = 0;
//		for (EvaluatedQueryTree<String> evaluatedQueryTree : lggs) {
//			System.out.println("Solution " + i++ + ":");
//			System.out.println(evaluatedQueryTree);
//		}
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
	
	@Test
	public void testLGGEarlyTermination(){
		QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
		
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		posExampleTrees.add(DBpediaExample.getPosExampleTrees().get(0));
		posExampleTrees.add(DBpediaExample.getPosExampleTrees().get(0));
		
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
	}
	
	@Test
	public void testNoiseLGGWithCarcinogenesis() throws Exception{
		Model model = ModelFactory.createDefaultModel();
		model.read(new FileInputStream(new File("../examples/carcinogenesis/carcinogenesis.owl")), null, "RDF/XML");
		
		List<String> posExamples = Lists.newArrayList(
				"http://dl-learner.org/carcinogenesis#d1",
				"http://dl-learner.org/carcinogenesis#d10",
				"http://dl-learner.org/carcinogenesis#d101",
				"http://dl-learner.org/carcinogenesis#d102"
				);
		
		List<String> negExamples = Lists.newArrayList(
				"http://dl-learner.org/carcinogenesis#d110",
				"http://dl-learner.org/carcinogenesis#d111",
//				"http://dl-learner.org/carcinogenesis#d114",
//				"http://dl-learner.org/carcinogenesis#d116",
//				"http://dl-learner.org/carcinogenesis#d117",
//				"http://dl-learner.org/carcinogenesis#d119",
//				"http://dl-learner.org/carcinogenesis#d121",
//				"http://dl-learner.org/carcinogenesis#d123",
//				"http://dl-learner.org/carcinogenesis#d124",
//				"http://dl-learner.org/carcinogenesis#d125",
//				"http://dl-learner.org/carcinogenesis#d127",
//				"http://dl-learner.org/carcinogenesis#d128",
				"http://dl-learner.org/carcinogenesis#d130");
		
		QueryTreeFactory<String> queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(3);
		
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		for (String ex : posExamples) {
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex, model);
			posExampleTrees.add(tree);
		}
		
		List<QueryTree<String>> negExampleTrees = new ArrayList<QueryTree<String>>();
		for (String ex : negExamples) {
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex, model);
			negExampleTrees.add(tree);
		}
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("TREE " + cnt);
			tree.dump();
			
			System.out.println("-----------------------------");
			cnt++;
//			System.out.println(((QueryTreeImpl<String>)tree).toQuery());
		}
		
//		NoiseSensitiveLGG<String> lggGenerator = new NoiseSensitiveLGG<String>();
//		List<EvaluatedQueryTree<String>> lggs = lggGenerator.computeLGG(posExampleTrees);
//		
//		OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
//		ShortFormProvider sfp = new SimpleShortFormProvider();
//		renderer.setShortFormProvider(sfp);
//		ToStringRenderer.getInstance().setRenderer(renderer);
//		int i = 1;
//		QueryTree<String> lgg;
//		for (EvaluatedQueryTree<String> evaluatedQueryTree : lggs) {
//			lgg = evaluatedQueryTree.getTree();
//			System.out.println("Solution " + i++ + ":");
//			System.out.println(lgg.getStringRepresentation(true));
//			System.out.println(lgg.asOWLClassExpression());
//			//check how many negative examples are covered by the current solution
//			int coveredNegatives = 0;
//			for (QueryTree<String> negTree : negExampleTrees) {
//				if(negTree.isSubsumedBy(evaluatedQueryTree.getTree())){
//					coveredNegatives++;
//				}
//			}
//			System.out.println("Covered negatives:" + coveredNegatives);
//		}
	}
	
	@Test
	public void testTCGA() throws Exception{
		URL url = new URL("http://vmlion14.deri.ie/node43/8080/sparql");
		SparqlEndpoint endpoint = new SparqlEndpoint(url);
		List<String> posExamples = Lists.newArrayList("http://tcga.deri.ie/TCGA-BI-A0VS","http://tcga.deri.ie/TCGA-BI-A20A");
		
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint);
		
		QueryTreeFactory<String> queryTreeFactory = new QueryTreeFactoryImpl();
		
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		for (String ex : posExamples) {
			Model cbd = cbdGen.getConciseBoundedDescription(ex, 0);
			System.out.println(cbd.size());
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex, cbd);
			posExampleTrees.add(tree);
		}
		
//		NoiseSensitiveLGG<String> lggGenerator = new NoiseSensitiveLGG<String>();
//		List<EvaluatedQueryTree<String>> lggs = lggGenerator.computeLGG(posExampleTrees);
//		for (EvaluatedQueryTree<String> lgg : lggs) {
//			System.out.println(lgg);
//		}
	}


}
