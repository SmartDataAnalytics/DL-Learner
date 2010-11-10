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
package org.dllearner.sparqlquerygenerator;

import java.util.List;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.examples.DBpediaExample;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGenerator;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.BruteForceNBRStrategy;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.TagNonSubsumingPartsNBRStrategy;
import org.junit.Test;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class NBRTest {
	
	@Test
	public void computeSingleNBRBruteForce(){
		
		Set<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		Set<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		NBRGenerator<String> nbrGenerator = new NBRGeneratorImpl<String>(new BruteForceNBRStrategy<String>());
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("POSITIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		
		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		
		System.out.println("-----------------------------------------------");
		
		cnt = 1;
		for(QueryTree<String> tree : negExampleTrees){
			System.out.println("NEGATIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> nbr = nbrGenerator.getNBR(lgg, negExampleTrees);
		
		System.out.println("NBR");
		System.out.println(nbr.getStringRepresentation());
		
	}
	
	@Test
	public void computeAllNBRsBruteForce(){
		Set<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		Set<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		NBRGenerator<String> nbrGenerator = new NBRGeneratorImpl<String>(new BruteForceNBRStrategy<String>());
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("POSITIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		
		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		
		System.out.println("-----------------------------------------------");
		
		cnt = 1;
		for(QueryTree<String> tree : negExampleTrees){
			System.out.println("NEGATIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		List<QueryTree<String>> nbrs = nbrGenerator.getNBRs(lgg, negExampleTrees);
		cnt = 1;
		for(QueryTree<String> tree : nbrs){
			System.out.println("NBR " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
	}
	
	@Test
	public void computeSingleNBRWithTaggingNonSubsumingParts(){
		Set<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		Set<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		NBRGenerator<String> nbrGenerator = new NBRGeneratorImpl<String>(new TagNonSubsumingPartsNBRStrategy<String>());
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("POSITIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		
		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		
		System.out.println("-----------------------------------------------");
		
		cnt = 1;
		for(QueryTree<String> tree : negExampleTrees){
			System.out.println("NEGATIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> nbr = nbrGenerator.getNBR(lgg, negExampleTrees);
		
		System.out.println("NBR");
		System.out.println(nbr.getStringRepresentation());
	}
	
	@Test
	public void computeSingleNBRGreedy(){
		Set<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		Set<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		NBRGenerator<String> nbrGenerator = new NBRGeneratorImpl<String>(new GreedyNBRStrategy<String>());
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
			System.out.println("POSITIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		
		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		
		System.out.println("-----------------------------------------------");
		
		cnt = 1;
		for(QueryTree<String> tree : negExampleTrees){
			System.out.println("NEGATIVE EXAMPLE TREE " + cnt);
			System.out.println(tree.getStringRepresentation());
			System.out.println("-----------------------------------------------");
			cnt++;
		}
		
		QueryTree<String> nbr = nbrGenerator.getNBR(lgg, negExampleTrees);
		
		System.out.println("NBR");
		System.out.println(nbr.getStringRepresentation());
	}

}
