package org.dllearner.algorithms.miles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.reasoning.FastInstanceChecker;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
/**
 * @author Lorenz Buehmann
 *
 */
public class DescriptionLinearClassifier {
	
	private AbstractReasonerComponent rc;
	private Set<Individual> posExamples;
	private Set<Individual> negExamples;
	
	private boolean writeArffFile = true;

	public DescriptionLinearClassifier(PosNegLP lp, AbstractReasonerComponent rc) {
		this(lp.getPositiveExamples(), lp.getNegativeExamples(), rc);
	}
	
	public DescriptionLinearClassifier(ClassLearningProblem lp, AbstractReasonerComponent rc) {
		this(rc.getIndividuals(lp.getClassToDescribe()), Sets.difference(rc.getIndividuals(),rc.getIndividuals(lp.getClassToDescribe())), rc);
	}
	
	public DescriptionLinearClassifier(Set<Individual> posExamples, Set<Individual> negExamples, AbstractReasonerComponent rc) {
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		this.rc = rc;
	}
	
	public void getLinearCombination(List<Description> descriptions){
		//get common data
		Instances data = buildData(descriptions);
		
		//compute linear regression model
		data.setClassIndex(data.numAttributes() - 1);
		AbstractClassifier model = new LinearRegression();
		model = new J48();
		try {
			model.buildClassifier(data);
			System.out.println(model);
			
//			AddExpression filter = new AddExpression();
//			filter.setExpression("a1^2");
//			FilteredClassifier filteredClassifier = new FilteredClassifier();
//			filteredClassifier.setClassifier(model);
//			filteredClassifier.setFilter(filter);
//			filteredClassifier.buildClassifier(data);
//			logger.debug(filteredClassifier.getClassifier());
			
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(model, data, 10, new Random(1));
			System.out.println(eval.toSummaryString(true));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Instances buildData(List<Description> descriptions){
		//#attributes = #descriptions + 1 for the target class
		int numAttributes = descriptions.size() + 1;
		ArrayList<Attribute> attInfo = new ArrayList<Attribute>(numAttributes);
		
		for (int i = 0; i < descriptions.size(); i++) {
			attInfo.add(new Attribute("C_" + String.valueOf(i)));
		}
		attInfo.add(new Attribute("t", Lists.newArrayList("0","1")));
			
		Instances data = new Instances("rel", attInfo, posExamples.size()+negExamples.size());
		
		//basically, we have two strategies to build the matrix:
		//1. for each example check for each concept whether it's instance of
		//2. for each concept get all instances
		
		//apply 2. strategy
		List<SortedSet<Individual>> individualsList = new ArrayList<SortedSet<Individual>>(descriptions.size());
		for (Description description : descriptions) {
			SortedSet<Individual> individuals = rc.getIndividuals(description);
			individualsList.add(individuals);
		}
		//handle pos examples
		for (Individual posEx : posExamples) {
			double[] attValues = new double[numAttributes];
			
			for (int i = 0; i < descriptions.size(); i++) {
				attValues[i] = individualsList.get(i).contains(posEx) ? 1.0 : 0.0;
			}
			
			//last attribute value is 1
			attValues[numAttributes-1] = 1;
			
			Instance instance = new DenseInstance(1.0, attValues);
			data.add(instance);
			instance.setDataset(data);
		}
		
		// handle neg examples
		for (Individual negEx : negExamples) {
			double[] attValues = new double[numAttributes];

			for (int i = 0; i < descriptions.size(); i++) {
				attValues[i] = individualsList.get(i).contains(negEx) ? 1 : 0;
			}

			//last attribute value is 0
			attValues[numAttributes-1] = 0;

			Instance instance = new DenseInstance(1.0, attValues);
			data.add(instance);
		}
		System.out.println(data.toString());
		if(writeArffFile ){
			try {
				writeArffFile(data, new File("./data/test.arff"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	private void writeArffFile(Instances dataSet, File file) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(file);
		saver.writeBatch();
	}
	
	public static void main(String[] args) throws Exception {
		KnowledgeSource ks = new OWLFile("../examples/swore/swore.rdf");
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(new NamedClass("http://ns.softwiki.de/req/CustomerRequirement"));
		lp.init();
		CELOE celoe = new CELOE(lp, rc);
		celoe.setNoisePercentage(1.0);
		celoe.setMaxExecutionTimeInSeconds(3);
		celoe.init();
		celoe.start();
		
		List<Description> descriptions = new ArrayList<Description>();
		for (EvaluatedDescription ed : celoe.getCurrentlyBestEvaluatedDescriptions(100)) {
			if(((EvaluatedDescriptionClass)ed).getAdditionalInstances().size() > 0){
				System.out.println(ed);
				System.out.println(((EvaluatedDescriptionClass)ed).getAdditionalInstances());
				descriptions.add(ed.getDescription());
			}
			if(descriptions.size() == 3) break;
		}
//		descriptions.addAll(celoe.getCurrentlyBestDescriptions(2));
//		descriptions.add(new NamedClass("http://ns.softwiki.de/req/Requirement"));
//		descriptions.add(new NamedClass("http://ns.softwiki.de/req/Customer"));
		
		DescriptionLinearClassifier dlc = new DescriptionLinearClassifier(lp, rc);
		dlc.getLinearCombination(descriptions);
	}

}
