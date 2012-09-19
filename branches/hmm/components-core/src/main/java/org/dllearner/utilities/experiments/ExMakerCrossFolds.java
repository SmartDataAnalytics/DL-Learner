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
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class ExMakerCrossFolds {
	private static Logger logger = Logger.getLogger(ExMakerCrossFolds.class);

	private final Examples examples;
	
	public static int minElementsPerFold = 6;
	
	public ExMakerCrossFolds(Examples examples){
		this.examples = examples;
	}
	
	public static void main(String[] args) {
		Examples ex = new Examples();
		
		for (int i = 0; i < 10000; i++) {
			ex.addPosTrain("p"+i);
			ex.addNegTrain("n"+i);
		}
		long n = System.currentTimeMillis();
		 System.out.println("initial size: "+ex.size());
		 ExMakerCrossFolds r = new ExMakerCrossFolds(ex);
		 List<Examples> l = r.splitLeaveOneOut(10);
		 printFolds(l );
		 System.out.println(System.currentTimeMillis()-n);
		
		
	}
	public static void printFolds(List<Examples> l ){
		 int i = 1;
		 int totalsize = 0;
		 StringBuffer b = new StringBuffer();
		 b.append("Number of folds "+l.size()+"\n");
		 for (Examples examples : l) {
			 b.append("Fold: "+(i++)+"\n");
			 b.append(examples.toString());
			 b.append("\n");
			
			 totalsize+=examples.size();
		}
		 b.append("total size: "+totalsize);
		 logger.info(b.toString());
	}
	
	
	public List<Examples> splitLeaveOneOut(int folds){
		if( 	folds*minElementsPerFold > examples.sizeTotalOfPositives()
				|| folds*minElementsPerFold > examples.sizeTotalOfNegatives()
		){
			logger.error("Too many folds for, too few data. cant spread: ");
			logger.error(examples.sizeTotalOfPositives()+" examples over "+folds+" folds OR");
			logger.error(examples.sizeTotalOfNegatives()+" examples over "+folds+" folds");
			logger.error("each fold must have more than "+minElementsPerFold+" elements");
			return null;
		}
		
		List<Examples> foldSets = new ArrayList<Examples>();
		double foldPercentage = 1.0d/((double)folds);
		int tenPercentPos = (int)Math.floor(((double)examples.sizeTotalOfPositives())*foldPercentage);
		int tenPercentNeg = (int)Math.floor(((double)examples.sizeTotalOfNegatives())*foldPercentage);
		
		List<String> posRemaining =  new ArrayList<String>(examples.getPositiveExamples());
		List<String> negRemaining  = new ArrayList<String>(examples.getNegativeExamples());
		Collections.shuffle(posRemaining);
		Collections.shuffle(negRemaining);
		
		
		Examples tmp;
//		Examples oneFold;
		for(int i = 0; i<folds;i++){
//			logger.trace("Foldprogess: "+i+" of "+folds);
			SortedSet<String> newPos = new TreeSet<String>();
			SortedSet<String> newNeg = new TreeSet<String>();
			String one = "";

			for(int a =0; a<tenPercentPos&& !posRemaining.isEmpty();a++){
				one = posRemaining.remove(posRemaining.size()-1);
				newPos.add(one);
			}
			for(int a =0; a <tenPercentNeg&& !negRemaining.isEmpty() ; a++){
				one = negRemaining.remove(negRemaining.size()-1);
				newNeg.add(one);
			}

			tmp = new Examples();
			tmp.addPosTrain(newPos);
			tmp.addNegTrain(newNeg);
			foldSets.add(tmp);

		}
		List<Examples> ret = new ArrayList<Examples>();
		for(int i =0; i<foldSets.size();i++){
			Examples oneFold = new Examples();
			oneFold.addPosTest(foldSets.get(i).getPositiveExamples());
			oneFold.addNegTest(foldSets.get(i).getNegativeExamples());
			for(int a =0; a<foldSets.size();a++){
				if(a==i){
					continue;
				}else{
					oneFold.addPosTrain(foldSets.get(a).getPositiveExamples());
					oneFold.addNegTrain(foldSets.get(a).getNegativeExamples());
				}
				
			}
			ret.add(oneFold);
		}
		
		return ret;
	}
	
}
