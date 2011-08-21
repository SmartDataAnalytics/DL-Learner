/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.utilities.experiments;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * used to randomize examples and split them into training and test sets
 * gets a fixed number of examples
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class ExMakerFixedSize {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExMakerFixedSize.class);

	private final Examples examples;
	private final boolean randomize;
	
	public ExMakerFixedSize(Examples examples ){
		this(examples, true) ;
	}
	
	public ExMakerFixedSize(Examples examples, boolean randomize ){
		this.examples = examples;
		this.randomize = randomize;
	}
	
	public static void main(String[] args) {
		Examples ex = new Examples();
		
		for (int i = 0; i < 20; i++) {
			ex.addPosTrain("p"+i);
			ex.addNegTrain("n"+i);
		}
		
		ExMakerFixedSize r = new ExMakerFixedSize(ex);
		ex = r.select(5, 5);
		System.out.println(ex.toString());
		
	}
	
	/**
	 * same as select(int,int)
	 * uses both times the same number
	 * @param both
	 * @return
	 */
	public Examples select(int both){
		return select( both,  both);
	}
	
	/**
	 * returns a new example object based on ALL (train and test) examples in the old set
	 * picks a fixed number of examples, puts them into training sets, rest to test set
	 * @param nrOfPos
	 * @param nrOfNeg
	 * @return
	 */
	public Examples select(int nrOfPos, int nrOfNeg){

		SortedSet<String> posTrain = new TreeSet<String>();
		SortedSet<String> negTrain = new TreeSet<String>();
		
		SortedSet<String> posTest = new TreeSet<String>();
		SortedSet<String> negTest = new TreeSet<String>();
		
		SortedSet<String> posOld = new TreeSet<String>();
		SortedSet<String> negOld = new TreeSet<String>();
		posOld.addAll(examples.getPositiveExamples());
		negOld.addAll(examples.getNegativeExamples());
		
		while (!posOld.isEmpty() && posTrain.size()< nrOfPos) {
			String one;
			if(randomize){
				one = pickOneRandomly(posOld.toArray(new String[] {}));
			}else{
				one = posOld.first();
			}
			posOld.remove(one);
			posTrain.add(one);
		}
		posTest.addAll(posOld);
		
		while (!negOld.isEmpty() && negTrain.size()< nrOfNeg) {
			String one;
			if(randomize){
				one = pickOneRandomly(negOld.toArray(new String[] {}));
			}else{
				one = negOld.first();
			}
			negOld.remove(one);
			negTrain.add(one);
		}
		negTest.addAll(negOld);
		
		return new Examples(posTrain, negTrain, posTest, negTest);
	}
	
	
	public static String pickOneRandomly(String[] from){
		Random r = new Random();
		int index = Math.round((float)(from.length*r.nextFloat()));
		try{
			return from[index];
		}catch (Exception e) {
			return pickOneRandomly(from);
		}
	}
	
}
