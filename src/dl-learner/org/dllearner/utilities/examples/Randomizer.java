/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.utilities.examples;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * used to randomize examples and split them into training and test sets
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class Randomizer {
	private static Logger logger = Logger.getLogger(Randomizer.class);

	private final Examples examples;
	
	public Randomizer(Examples examples ){
		this.examples = examples;
	}
	
	public static void main(String[] args) {
		Examples ex = new Examples();
		
		for (int i = 0; i < 20; i++) {
			ex.addPosTrain("p"+i);
			ex.addNegTrain("n"+i);
		}
		
		Randomizer r = new Randomizer(ex);
		ex = r.split(0.7d);
		System.out.println(ex.toString());
		
	}
	
	public Examples split(double percentageOfTrainingSet){
//		System.out.println(GlobalConfig.trainingDataPercentage+"");
		SortedSet<String> posTrain = new TreeSet<String>();
		SortedSet<String> negTrain = new TreeSet<String>();
		
		SortedSet<String> posTest = new TreeSet<String>();
		SortedSet<String> negTest = new TreeSet<String>();
		
		SortedSet<String> posOld = new TreeSet<String>();
		SortedSet<String> negOld = new TreeSet<String>();
		posOld.addAll(examples.getPositiveExamples());
		negOld.addAll(examples.getNegativeExamples());
		
		int posOldSize = posOld.size();
		int negOldSize = negOld.size();
		
		while (!posOld.isEmpty() && (((double)posOld.size()/(double)posOldSize)) > percentageOfTrainingSet) {
			String one = pickOneRandomly(posOld.toArray(new String[] {}));
			posOld.remove(one);
			posTest.add(one);
		}
		posTrain.addAll(posOld);
		
		while (!negOld.isEmpty() && (((double)negOld.size()/(double)negOldSize)) > percentageOfTrainingSet) {
			String one = pickOneRandomly(negOld.toArray(new String[] {}));
			negOld.remove(one);
			negTest.add(one);
		}
		negTrain.addAll(negOld);
		
		
		double posPercent = posTrain.size()/(double)posOldSize;
		double negPercent = negTrain.size()/(double)negOldSize;
		
//		if there is more than a 10% error
		if(Math.abs(posPercent - percentageOfTrainingSet)>0.1d || Math.abs(negPercent - percentageOfTrainingSet)>0.1d ){
			logger.info("repeating, unevenly matched");
			return split(percentageOfTrainingSet);
		}
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
