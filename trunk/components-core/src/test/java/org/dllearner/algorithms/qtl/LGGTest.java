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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.examples.DBpediaExample;
import org.dllearner.algorithms.qtl.examples.LinkedGeoDataExample;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.EvaluatedQueryTree;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.algorithms.qtl.operations.lgg.NoiseSensitiveLGG;
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
		
		
		NoiseSensitiveLGG<String> lggGenerator = new NoiseSensitiveLGG<String>();
		List<EvaluatedQueryTree<String>> lggs = lggGenerator.computeLGG(posExampleTrees);
		
		int i = 0;
		for (EvaluatedQueryTree<String> evaluatedQueryTree : lggs) {
			System.out.println("Solution " + i++ + ":");
			System.out.println(evaluatedQueryTree);
		}
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
				"http://dl-learner.org/carcinogenesis#d102",
				"http://dl-learner.org/carcinogenesis#d103",
				"http://dl-learner.org/carcinogenesis#d106",
				"http://dl-learner.org/carcinogenesis#d107",
				"http://dl-learner.org/carcinogenesis#d108",
				"http://dl-learner.org/carcinogenesis#d11",
				"http://dl-learner.org/carcinogenesis#d12",
				"http://dl-learner.org/carcinogenesis#d13",
				"http://dl-learner.org/carcinogenesis#d134",
				"http://dl-learner.org/carcinogenesis#d135",
				"http://dl-learner.org/carcinogenesis#d136",
				"http://dl-learner.org/carcinogenesis#d138",
				"http://dl-learner.org/carcinogenesis#d140",
				"http://dl-learner.org/carcinogenesis#d141",
				"http://dl-learner.org/carcinogenesis#d144",
				"http://dl-learner.org/carcinogenesis#d145",
				"http://dl-learner.org/carcinogenesis#d146",
				"http://dl-learner.org/carcinogenesis#d147",
				"http://dl-learner.org/carcinogenesis#d15",
				"http://dl-learner.org/carcinogenesis#d17",
				"http://dl-learner.org/carcinogenesis#d19",
				"http://dl-learner.org/carcinogenesis#d192",
				"http://dl-learner.org/carcinogenesis#d193",
				"http://dl-learner.org/carcinogenesis#d195",
				"http://dl-learner.org/carcinogenesis#d196",
				"http://dl-learner.org/carcinogenesis#d197",
				"http://dl-learner.org/carcinogenesis#d198",
				"http://dl-learner.org/carcinogenesis#d199",
				"http://dl-learner.org/carcinogenesis#d2",
				"http://dl-learner.org/carcinogenesis#d20",
				"http://dl-learner.org/carcinogenesis#d200",
				"http://dl-learner.org/carcinogenesis#d201",
				"http://dl-learner.org/carcinogenesis#d202",
				"http://dl-learner.org/carcinogenesis#d203",
				"http://dl-learner.org/carcinogenesis#d204",
				"http://dl-learner.org/carcinogenesis#d205",
				"http://dl-learner.org/carcinogenesis#d21",
				"http://dl-learner.org/carcinogenesis#d22",
				"http://dl-learner.org/carcinogenesis#d226",
				"http://dl-learner.org/carcinogenesis#d227",
				"http://dl-learner.org/carcinogenesis#d228",
				"http://dl-learner.org/carcinogenesis#d229",
				"http://dl-learner.org/carcinogenesis#d231",
				"http://dl-learner.org/carcinogenesis#d232",
				"http://dl-learner.org/carcinogenesis#d234",
				"http://dl-learner.org/carcinogenesis#d236",
				"http://dl-learner.org/carcinogenesis#d239",
				"http://dl-learner.org/carcinogenesis#d23_2",
				"http://dl-learner.org/carcinogenesis#d242",
				"http://dl-learner.org/carcinogenesis#d245",
				"http://dl-learner.org/carcinogenesis#d247",
				"http://dl-learner.org/carcinogenesis#d249",
				"http://dl-learner.org/carcinogenesis#d25",
				"http://dl-learner.org/carcinogenesis#d252",
				"http://dl-learner.org/carcinogenesis#d253",
				"http://dl-learner.org/carcinogenesis#d254",
				"http://dl-learner.org/carcinogenesis#d255",
				"http://dl-learner.org/carcinogenesis#d26",
				"http://dl-learner.org/carcinogenesis#d272",
				"http://dl-learner.org/carcinogenesis#d275",
				"http://dl-learner.org/carcinogenesis#d277",
				"http://dl-learner.org/carcinogenesis#d279",
				"http://dl-learner.org/carcinogenesis#d28",
				"http://dl-learner.org/carcinogenesis#d281",
				"http://dl-learner.org/carcinogenesis#d283",
				"http://dl-learner.org/carcinogenesis#d284",
				"http://dl-learner.org/carcinogenesis#d288",
				"http://dl-learner.org/carcinogenesis#d29",
				"http://dl-learner.org/carcinogenesis#d290",
				"http://dl-learner.org/carcinogenesis#d291",
				"http://dl-learner.org/carcinogenesis#d292",
				"http://dl-learner.org/carcinogenesis#d30",
				"http://dl-learner.org/carcinogenesis#d31",
				"http://dl-learner.org/carcinogenesis#d32",
				"http://dl-learner.org/carcinogenesis#d33",
				"http://dl-learner.org/carcinogenesis#d34",
				"http://dl-learner.org/carcinogenesis#d35",
				"http://dl-learner.org/carcinogenesis#d36",
				"http://dl-learner.org/carcinogenesis#d37",
				"http://dl-learner.org/carcinogenesis#d38",
				"http://dl-learner.org/carcinogenesis#d42",
				"http://dl-learner.org/carcinogenesis#d43",
				"http://dl-learner.org/carcinogenesis#d44",
				"http://dl-learner.org/carcinogenesis#d45",
				"http://dl-learner.org/carcinogenesis#d46",
				"http://dl-learner.org/carcinogenesis#d47",
				"http://dl-learner.org/carcinogenesis#d48",
				"http://dl-learner.org/carcinogenesis#d49",
				"http://dl-learner.org/carcinogenesis#d5",
				"http://dl-learner.org/carcinogenesis#d51",
				"http://dl-learner.org/carcinogenesis#d52",
				"http://dl-learner.org/carcinogenesis#d53",
				"http://dl-learner.org/carcinogenesis#d55",
				"http://dl-learner.org/carcinogenesis#d58",
				"http://dl-learner.org/carcinogenesis#d6",
				"http://dl-learner.org/carcinogenesis#d7",
				"http://dl-learner.org/carcinogenesis#d84",
				"http://dl-learner.org/carcinogenesis#d85_2",
				"http://dl-learner.org/carcinogenesis#d86",
				"http://dl-learner.org/carcinogenesis#d87",
				"http://dl-learner.org/carcinogenesis#d88",
				"http://dl-learner.org/carcinogenesis#d89",
				"http://dl-learner.org/carcinogenesis#d9",
				"http://dl-learner.org/carcinogenesis#d91",
				"http://dl-learner.org/carcinogenesis#d92",
				"http://dl-learner.org/carcinogenesis#d93",
				"http://dl-learner.org/carcinogenesis#d95",
				"http://dl-learner.org/carcinogenesis#d96",
				"http://dl-learner.org/carcinogenesis#d98",
				"http://dl-learner.org/carcinogenesis#d99",
				"http://dl-learner.org/carcinogenesis#d100",
				"http://dl-learner.org/carcinogenesis#d104",
				"http://dl-learner.org/carcinogenesis#d105",
				"http://dl-learner.org/carcinogenesis#d109",
				"http://dl-learner.org/carcinogenesis#d137",
				"http://dl-learner.org/carcinogenesis#d139",
				"http://dl-learner.org/carcinogenesis#d14",
				"http://dl-learner.org/carcinogenesis#d142",
				"http://dl-learner.org/carcinogenesis#d143",
				"http://dl-learner.org/carcinogenesis#d148",
				"http://dl-learner.org/carcinogenesis#d16",
				"http://dl-learner.org/carcinogenesis#d18",
				"http://dl-learner.org/carcinogenesis#d191",
				"http://dl-learner.org/carcinogenesis#d206",
				"http://dl-learner.org/carcinogenesis#d230",
				"http://dl-learner.org/carcinogenesis#d233",
				"http://dl-learner.org/carcinogenesis#d235",
				"http://dl-learner.org/carcinogenesis#d237",
				"http://dl-learner.org/carcinogenesis#d238",
				"http://dl-learner.org/carcinogenesis#d23_1",
				"http://dl-learner.org/carcinogenesis#d24",
				"http://dl-learner.org/carcinogenesis#d240",
				"http://dl-learner.org/carcinogenesis#d241",
				"http://dl-learner.org/carcinogenesis#d243",
				"http://dl-learner.org/carcinogenesis#d244",
				"http://dl-learner.org/carcinogenesis#d246",
				"http://dl-learner.org/carcinogenesis#d248",
				"http://dl-learner.org/carcinogenesis#d250",
				"http://dl-learner.org/carcinogenesis#d251",
				"http://dl-learner.org/carcinogenesis#d27",
				"http://dl-learner.org/carcinogenesis#d273",
				"http://dl-learner.org/carcinogenesis#d274",
				"http://dl-learner.org/carcinogenesis#d278",
				"http://dl-learner.org/carcinogenesis#d286",
				"http://dl-learner.org/carcinogenesis#d289",
				"http://dl-learner.org/carcinogenesis#d3",
				"http://dl-learner.org/carcinogenesis#d39",
				"http://dl-learner.org/carcinogenesis#d4",
				"http://dl-learner.org/carcinogenesis#d40",
				"http://dl-learner.org/carcinogenesis#d41",
				"http://dl-learner.org/carcinogenesis#d50",
				"http://dl-learner.org/carcinogenesis#d54",
				"http://dl-learner.org/carcinogenesis#d56",
				"http://dl-learner.org/carcinogenesis#d57",
				"http://dl-learner.org/carcinogenesis#d8",
				"http://dl-learner.org/carcinogenesis#d85_1",
				"http://dl-learner.org/carcinogenesis#d90",
				"http://dl-learner.org/carcinogenesis#d94",
				"http://dl-learner.org/carcinogenesis#d97",
				"http://dl-learner.org/carcinogenesis#d296",
				"http://dl-learner.org/carcinogenesis#d305",
				"http://dl-learner.org/carcinogenesis#d306",
				"http://dl-learner.org/carcinogenesis#d307",
				"http://dl-learner.org/carcinogenesis#d308",
				"http://dl-learner.org/carcinogenesis#d311",
				"http://dl-learner.org/carcinogenesis#d314",
				"http://dl-learner.org/carcinogenesis#d315",
				"http://dl-learner.org/carcinogenesis#d316",
				"http://dl-learner.org/carcinogenesis#d320",
				"http://dl-learner.org/carcinogenesis#d322",
				"http://dl-learner.org/carcinogenesis#d323",
				"http://dl-learner.org/carcinogenesis#d325",
				"http://dl-learner.org/carcinogenesis#d329",
				"http://dl-learner.org/carcinogenesis#d330",
				"http://dl-learner.org/carcinogenesis#d331",
				"http://dl-learner.org/carcinogenesis#d332",
				"http://dl-learner.org/carcinogenesis#d333",
				"http://dl-learner.org/carcinogenesis#d336",
				"http://dl-learner.org/carcinogenesis#d337");
		
		List<String> negExamples = Lists.newArrayList(
				"http://dl-learner.org/carcinogenesis#d110",
				"http://dl-learner.org/carcinogenesis#d111",
				"http://dl-learner.org/carcinogenesis#d114",
				"http://dl-learner.org/carcinogenesis#d116",
				"http://dl-learner.org/carcinogenesis#d117",
				"http://dl-learner.org/carcinogenesis#d119",
				"http://dl-learner.org/carcinogenesis#d121",
				"http://dl-learner.org/carcinogenesis#d123",
				"http://dl-learner.org/carcinogenesis#d124",
				"http://dl-learner.org/carcinogenesis#d125",
				"http://dl-learner.org/carcinogenesis#d127",
				"http://dl-learner.org/carcinogenesis#d128",
				"http://dl-learner.org/carcinogenesis#d130");
		
		QueryTreeFactory<String> queryTreeFactory = new QueryTreeFactoryImpl();
		
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
//			System.out.println("TREE " + cnt);
//			tree.dump();
//			System.out.println("-----------------------------");
//			cnt++;
//			System.out.println(((QueryTreeImpl<String>)tree).toQuery());
		}
		
		
		NoiseSensitiveLGG<String> lggGenerator = new NoiseSensitiveLGG<String>();
		List<EvaluatedQueryTree<String>> lggs = lggGenerator.computeLGG(posExampleTrees);
		
		int i = 0;
		for (EvaluatedQueryTree<String> evaluatedQueryTree : lggs) {
			System.out.println("Solution " + i++ + ":");
			System.out.println(evaluatedQueryTree.getScore());
			//check how many negative examples are covered by the current solution
			int coveredNegatives = 0;
			for (QueryTree<String> negTree : negExampleTrees) {
				if(negTree.isSubsumedBy(evaluatedQueryTree.getTree())){
					coveredNegatives++;
				}
			}
			System.out.println("Covered negatives:" + coveredNegatives);
		}
	}


}
