package org.dllearner.utilities.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class ExMakerCrossFolds {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExMakerCrossFolds.class);

	private final Examples examples;
	
	public static int minElementsPerFold = 6;
	
	public ExMakerCrossFolds(Examples examples){
		this.examples = examples;
	}
	
	public static void main(String[] args) {
		Examples ex = new Examples();
		
		for (int i = 0; i < 30000; i++) {
			ex.addPosTrain("p"+i);
			ex.addNegTrain("n"+i);
		}
		long n = System.currentTimeMillis();
		 System.out.println("initial size: "+ex.size());
		 ExMakerCrossFolds r = new ExMakerCrossFolds(ex);
		 List<Examples> l = r.split(10, 0.9d);
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
	
	
	public List<Examples> split(int folds, double percentageOfTrainingSet){
		if( 	folds*minElementsPerFold > examples.sizeTotalOfPositives()
				|| folds*minElementsPerFold > examples.sizeTotalOfNegatives()
		){
			logger.error("Too many folds for, too few data. cant spread: ");
			logger.error(examples.sizeTotalOfPositives()+" examples over "+folds+" folds OR");
			logger.error(examples.sizeTotalOfNegatives()+" examples over "+folds+" folds");
			logger.error("each fold must have more than "+minElementsPerFold+" elements");
			return null;
		}
		
		List<Examples> ret = new ArrayList<Examples>();
		double foldPercentage = 1.0d/((double)folds);
		int tenPercentPos = (int)Math.floor(((double)examples.sizeTotalOfPositives())*foldPercentage);
		int tenPercentNeg = (int)Math.floor(((double)examples.sizeTotalOfNegatives())*foldPercentage);
		
		List<String> posRemaining =  new ArrayList<String>(examples.getPositiveExamples());
		List<String> negRemaining  = new ArrayList<String>(examples.getNegativeExamples());
		Collections.shuffle(posRemaining);
		Collections.shuffle(negRemaining);
		
		
		Examples tmp;
		Examples oneFold;
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

			oneFold = new ExMakerRandomizer(tmp).split(percentageOfTrainingSet);
			ret.add(oneFold);
			
		}
		return ret;
	}
	
}
