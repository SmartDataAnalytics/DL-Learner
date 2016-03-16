package org.dllearner.algorithms.miles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class FoldGenerator<T> {

	List<T> examples;

	private Collection<T> posExamples;
	private Collection<T> negExamples;

	Random random = new Random(123);

	public FoldGenerator(Collection<T> posExamples, Collection<T> negExamples) {
		this.posExamples = posExamples;
		this.negExamples = negExamples;

		examples = new ArrayList<>();
		examples.addAll(posExamples);
		examples.addAll(negExamples);

//		Collections.shuffle(examples, random);
		
		System.out.println(examples);
	}

	public List<T> trainCV(int numFolds, int numFold) {
		int numInstForFold, first, offset;
		List<T> train;

		if (numFolds < 2) {
			throw new IllegalArgumentException("Number of folds must be at least 2!");
		}
		if (numFolds > examples.size()) {
			throw new IllegalArgumentException("Can't have more folds than instances!");
		}
		numInstForFold = examples.size() / numFolds;
		if (numFold < examples.size() % numFolds) {
			numInstForFold++;
			offset = numFold;
		} else {
			offset = examples.size() % numFolds;
		}
		train = new ArrayList<>(examples.size() - numInstForFold);
		first = numFold * (examples.size() / numFolds) + offset;
		train.addAll(examples.subList(0, first));
		int from = first + numInstForFold;
		int size = examples.size() - first - numInstForFold;
		int to = from + size;
		train.addAll(examples.subList(from, to));
		
		return train;
	}
	
	public List<T> testCV(int numFolds, int numFold) {
		int numInstForFold, first, offset;
		List<T> test;

		if (numFolds < 2) {
			throw new IllegalArgumentException("Number of folds must be at least 2!");
		}
		if (numFolds > examples.size()) {
			throw new IllegalArgumentException("Can't have more folds than instances!");
		}
		numInstForFold = examples.size() / numFolds;
		if (numFold < examples.size() % numFolds) {
			numInstForFold++;
			offset = numFold;
		} else {
			offset = examples.size() % numFolds;
		}
		test = new ArrayList<>(numInstForFold);
		first = numFold * (examples.size() / numFolds) + offset;
		test.addAll(examples.subList(first, first + numInstForFold));
		return test;
	}
	
	public static void main(String[] args) throws Exception {
		int numPos = 4;
		int numNeg = 5;
		List<String> posExamples = Lists.newArrayList();
		List<String> negExamples = Lists.newArrayList();
		for (int i = 1; i <= numPos; i++) {
			posExamples.add("p" + i);
		}
		for (int i = 1; i <= numNeg; i++) {
			negExamples.add("n" + i);
		}
		
		FoldGenerator<String> foldGenerator = new FoldGenerator<>(posExamples, negExamples);
		
		int numFolds = 5;
		
		int numAttributes = 2;
		int num = 9;
		ArrayList<Attribute> attInfo = new ArrayList<>(numAttributes);
		attInfo.add(new Attribute("C1"));
		attInfo.add(new Attribute("t", Lists.newArrayList("0","1")));
			
		Instances data = new Instances("rel", attInfo, num);
		for (int i = 1; i <= num; i++) {
			data.add(new DenseInstance(1.0, new double[]{i,0}));
		}
//		System.out.println(data);
		for (int i = 0; i < numFolds; i++) {
			System.out.println("Fold " + i);
			System.out.println("Train:" + foldGenerator.trainCV(numFolds, i));
			System.out.println("Test:" + foldGenerator.testCV(numFolds, i));
			
//			System.out.println(data.trainCV(numFolds, i));
		}
		
		
	}

}