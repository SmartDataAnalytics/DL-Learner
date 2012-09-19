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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * used to randomize examples and split them into training and test sets
 * gets a percentage of the examples 
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class ExMakerRandomizer {
//	private static Logger logger = Logger.getLogger(ExMakerRandomizer.class);

	private final Examples examples;
	
	public ExMakerRandomizer(Examples examples ){
		this.examples = examples;
	}
	
	public static void main(String[] args) {
		Examples ex = new Examples();
		long n = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			ex.addPosTrain("p"+i);
			ex.addNegTrain("n"+i);
		}
		
		ExMakerRandomizer r = new ExMakerRandomizer(ex);
		ex = r.split(0.7d);
		System.out.println("needed: "+(System.currentTimeMillis()-n)+ " ms");
		System.out.println(ex.toString());
		
	}
	
	
	/**
	 * Not quite exact, but fast
	 * has an error of 0.1 % 
	 * @param percentageOfTrainingSet
	 * @return
	 */
	public Examples split(double percentageOfTrainingSet){
		int sizeOfPosTrainingSet = (int)Math.floor(((double)examples.sizeTotalOfPositives())*percentageOfTrainingSet);
		int sizeOfNegTrainingSet = (int)Math.floor(((double)examples.sizeTotalOfNegatives())*percentageOfTrainingSet);
		
		int sizeOfPosTestSet = examples.sizeTotalOfPositives()-sizeOfPosTrainingSet;
		int sizeOfNegTestSet = examples.sizeTotalOfNegatives()-sizeOfNegTrainingSet;
		
//		System.out.println(sizeOfPosTrainingSet);
//		System.out.println(sizeOfNegTrainingSet);
//		System.out.println(sizeOfPosTestSet);
//		System.out.println(sizeOfNegTestSet);
		
		List<String> posRemaining =  new ArrayList<String>(examples.getPositiveExamples());
		List<String> negRemaining  = new ArrayList<String>(examples.getNegativeExamples());
	
		Random r = new Random();
		Examples ret = new Examples();
		for (int i = 0; i < posRemaining.size(); i++) {
			String one = posRemaining.get(i);
			if(ret.getPosTrain().size()>sizeOfPosTrainingSet){
				ret.addPosTest(one);
				continue;
			}
			if(ret.getPosTest().size()>sizeOfPosTestSet){
				ret.addPosTrain(one);
				continue;
			}
			
			if(r.nextDouble()<percentageOfTrainingSet){
				ret.addPosTrain(one);
			}else{
				ret.addPosTest(one);
			}
			
		}
		for (int i = 0; i < negRemaining.size(); i++) {
			String one = negRemaining.get(i);
			if(ret.getNegTrain().size()>sizeOfNegTrainingSet){
				ret.addNegTest(one);
				continue;
			}
			if(ret.getNegTest().size()>sizeOfNegTestSet){
				ret.addNegTrain(one);
				continue;
			}
			
			if(r.nextDouble()<percentageOfTrainingSet){
				ret.addNegTrain(one);
			}else{
				ret.addNegTest(one);
			}
			
		}
		
//		Collections.shuffle(posRemaining);
//		Collections.shuffle(negRemaining);
//		
//		List<String> newPos = new ArrayList<String>();
//		List<String> newNeg = new ArrayList<String>();
//		
//		Examples ret = new Examples();
//		String one;
//		while (posRemaining.size()>sizeOfPosTrainingSet){
//			one = posRemaining.remove(posRemaining.size()-1);
//			newPos.add(one);
//			
//		}
//		
//		ret.addPosTest(newPos);
//		ret.addPosTrain(posRemaining);
//		
//		while (negRemaining.size()>sizeOfNegTrainingSet){
//			one = negRemaining.remove(negRemaining.size()-1);
//			newNeg.add(one);
//		}
//		
//		ret.addNegTest(newNeg);
//		ret.addNegTrain(negRemaining);
//		
//		double posPercent = ret.getPosTrain().size()/(double)examples.getPositiveExamples().size();
//		double negPercent = ret.getNegTrain().size()/(double)examples.getNegativeExamples().size();
//		
////		if there is more than a 10% error
//		if(Math.abs(posPercent - percentageOfTrainingSet)>0.1d || Math.abs(negPercent - percentageOfTrainingSet)>0.1d ){
//			logger.info("repeating, unevenly matched");
//			return split(percentageOfTrainingSet);
//		}
		return ret;
	}
	
	
	
}
