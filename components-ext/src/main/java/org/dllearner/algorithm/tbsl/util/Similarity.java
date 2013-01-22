package org.dllearner.algorithm.tbsl.util;

import org.dllearner.algorithms.qtl.filters.I_Sub;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

public class Similarity {
	
	private static AbstractStringMetric qGramMetric = new QGramsDistance();
	private static AbstractStringMetric levensteinMetric = new Levenshtein();
	private static I_Sub substringMetric = new I_Sub();
	
	public static double getSimilarity(String s1, String s2){
		float qGramSim = qGramMetric.getSimilarity(s1, s2);
		float levensteinSim = levensteinMetric.getSimilarity(s1, s2);
		double subStringSim = substringMetric.score(s1, s2, true);
		
		return (qGramSim + levensteinSim + subStringSim) / 3;
	}

}
