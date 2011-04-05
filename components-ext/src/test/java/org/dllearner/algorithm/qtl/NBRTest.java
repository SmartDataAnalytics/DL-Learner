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

import java.util.List;

import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.examples.DBpediaExample;
import org.dllearner.algorithm.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithm.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.algorithm.qtl.operations.nbr.NBRGenerator;
import org.dllearner.algorithm.qtl.operations.nbr.NBRGeneratorImpl;
import org.dllearner.algorithm.qtl.operations.nbr.strategy.BruteForceNBRStrategy;
import org.dllearner.algorithm.qtl.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.algorithm.qtl.operations.nbr.strategy.TagNonSubsumingPartsNBRStrategy;
import org.junit.Test;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class NBRTest {
	
	@Test
	public void computeSingleNBRBruteForce(){
		
		List<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		List<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
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
		List<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		List<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
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
		List<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		List<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
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
		List<QueryTree<String>> posExampleTrees = DBpediaExample.getPosExampleTrees();
		List<QueryTree<String>> negExampleTrees = DBpediaExample.getNegExampleTrees();
		
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
