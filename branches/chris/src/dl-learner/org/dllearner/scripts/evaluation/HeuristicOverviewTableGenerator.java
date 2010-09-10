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
package org.dllearner.scripts.evaluation;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates a table for comparing different heuristics for class learning.
 * 
 * @author Jens Lehmann
 *
 */
public class HeuristicOverviewTableGenerator {

	// simple helper data structure to keep values
	public static class Input {

		// nr. of instances of the class for which we want to learn a definition / super class
		private int classInstances;
		// nr. of instances of the class expression to test 
		private int suggestionInstances;
		// nr. of instances of the knowledge base
		private int kbInstances;
		// nr. of instances of the class to describe and the class expression
		private int intersectionSuggestionClass;

		public Input(int classInstances, int suggestionInstances, int kbInstances, int intersectionSuggestionClass) {
			this.classInstances = classInstances;
			this.suggestionInstances = suggestionInstances;
			this.kbInstances = kbInstances;
			this.intersectionSuggestionClass = intersectionSuggestionClass;
		}
		
		public int getClassInstances() {
			return classInstances;
		}

		public int getSuggestionInstances() {
			return suggestionInstances;
		}

		public int getKbInstances() {
			return kbInstances;
		}
		
		public int getIntersectionSuggestionClass() {
			return intersectionSuggestionClass;
		}
		
		@Override
		public String toString() {
			return "A has " + classInstances + " instances, C has " + suggestionInstances + " instances, A and C have " + intersectionSuggestionClass + " instances and the whole KB has " + kbInstances + " instances";
		}
	}
	
	private List<Input> inputs;
	private DecimalFormat df = new DecimalFormat( "0.000000000000" );
	
	public HeuristicOverviewTableGenerator(List<Input> inputs) {
		this.inputs = inputs;
	}
	
	public void printFullTable() {
		for(Input input : inputs) {
			double precision = getPrecision(input);
			double recall = getRecall(input);
			double fMeasureEq = getFMeasure(recall, precision, 1);
			double fMeasureSc = getFMeasure(recall, precision, 3);
			double aMeasureEq = getAMeasure(recall, precision, 1);
			double aMeasureSc = getAMeasure(recall, precision, 3);
			double predAccEq = getPredAcc(input, 1);
			double predAccSc = getPredAcc(input, 3);
			double jaccard = getJaccard(input);
			
			System.out.println("input: " + input);
			System.out.println("recall: " + df.format(recall) + " precision: " + df.format(precision));
			System.out.println("FMeasure: eq. " + df.format(fMeasureEq) + " sc. " + df.format(fMeasureSc));
			System.out.println("AMeasure: eq. " + df.format(aMeasureEq) + " sc. " + df.format(aMeasureSc));
			System.out.println("pred.acc.: eq. " + df.format(predAccEq) + " sc. " + df.format(predAccSc));
			System.out.println("Jaccard: " + df.format(jaccard));
			System.out.println();
			
		}
	}
	
	private double getPrecision(Input input) {
		return input.intersectionSuggestionClass / (double) input.suggestionInstances;
	}
	
	private double getRecall(Input input) {
		return input.intersectionSuggestionClass / (double) input.classInstances;
	}
	
	private double getFMeasure(double recall, double precision, double factor) {
		return (precision + recall == 0) ? 0 :
			  ( (1+Math.sqrt(factor)) * (precision * recall)
					/ (Math.sqrt(factor) * precision + recall) ); 		
	}
	
	private double getAMeasure(double recall, double precision, double factor) {
		return (factor * recall + precision) / (factor + 1);
	}
	
	@SuppressWarnings("unused")
	private double getOwnMeasure(double recall, double precision, double factor) {
//		return (factor * recall + Math.sqrt(precision)) / (factor + 1);
		return (factor * recall + Math.pow(precision, 0.8)) / (factor + 1);
	}
	
	private double getPredAcc(Input input, double factor) {
		return (factor*input.intersectionSuggestionClass + input.kbInstances - input.classInstances - input.suggestionInstances + input.intersectionSuggestionClass) / (double) (input.kbInstances + (factor-1)*input.classInstances);
	}
	
	private double getJaccard(Input input) {
		return input.intersectionSuggestionClass / (double) (input.classInstances + input.suggestionInstances - input.intersectionSuggestionClass);
	}
	
	/**
	 * @param args none
	 */
	public static void main(String[] args) {
		List<Input> inputs = new LinkedList<Input>();
		inputs.add(new Input(100, 100, 1000, 0));
		inputs.add(new Input(100, 200, 1000, 100));
		inputs.add(new Input(100, 400, 1000, 100));
		inputs.add(new Input(100, 100, 1000, 90));
		inputs.add(new Input(100, 50, 1000, 50));
		inputs.add(new Input(20, 20, 100, 10));
		inputs.add(new Input(20, 40, 100, 10));
		
		HeuristicOverviewTableGenerator gen = new HeuristicOverviewTableGenerator(inputs);
		gen.printFullTable();
	}

}
