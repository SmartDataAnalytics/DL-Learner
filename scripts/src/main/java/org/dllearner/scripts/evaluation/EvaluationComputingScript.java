package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.owl.ConceptComparator;


public class EvaluationComputingScript {
	
	private AbstractReasonerComponent reasoner;
	private OWLFile ks;
	
	private String baseURI;
	private Map<String, String> prefixes;
	
	private static double minAccuracy = 0.85;
	
	private static double noisePercent = 5.0;

	private static int minInstanceCount = 5;

	private static int algorithmRuntimeInSeconds = 10;
	
	private static DecimalFormat df = new DecimalFormat();

	private static boolean useApproximations = false;
//	private static ThreeValuedLogic testReuseExistingDescription = ThreeValuedLogic.False;
//	private static ThreeValuedLogic testFilterDescriptionsFollowingFromKB = ThreeValuedLogic.False;
	
	private static boolean reuseExistingDescription = false;
	private static boolean filterDescriptionsFollowingFromKB = false;
	private static boolean checkExistingDefinitions = true;
	
	private final ConceptComparator comparator = new ConceptComparator();
	private URI ontologyURI;
	
	
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceStandardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalencePredaccMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceGenFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceJaccardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceStandardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalencePredaccMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceGenFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceJaccardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> defaultEquivalenceMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	
	
	public EvaluationComputingScript(URI ontologyURI) throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException, URISyntaxException{
		new EvaluationComputingScript(ontologyURI, false, false);
	}
	
	public EvaluationComputingScript(URI ontologyURI, boolean reuseExistingDescription, boolean filterDescriptionsFollowingFromKB) 
	throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException, URISyntaxException{
		EvaluationComputingScript.filterDescriptionsFollowingFromKB = filterDescriptionsFollowingFromKB;
		EvaluationComputingScript.reuseExistingDescription = reuseExistingDescription;
		System.out.println("Reusing existing descriptions: " + EvaluationComputingScript.reuseExistingDescription);
		System.out.println("Filtering descriptions following from KB: " + EvaluationComputingScript.filterDescriptionsFollowingFromKB);
		loadOntology(ontologyURI);
		computeWithApproximation();
		computeSuggestions();
		computeGenFMeasureWithoutDefaultNegation();
		evaluateInconsistencies();
		saveResults();
	}
	
	
	private void evaluateInconsistencies(){
		List<Map<NamedClass, List<EvaluatedDescriptionClass>>> equivalenceMapList = new ArrayList<Map<NamedClass,List<EvaluatedDescriptionClass>>>();
		
		equivalenceMapList.add(defaultEquivalenceMap);
		equivalenceMapList.add(owlEquivalenceFMeasureMap);
		equivalenceMapList.add(owlEquivalenceGenFMeasureMap);
		equivalenceMapList.add(owlEquivalenceJaccardMap);
		equivalenceMapList.add(owlEquivalencePredaccMap);
		equivalenceMapList.add(owlEquivalenceStandardMap);
		equivalenceMapList.add(fastEquivalenceFMeasureMap);
		equivalenceMapList.add(fastEquivalenceGenFMeasureMap);
		equivalenceMapList.add(fastEquivalenceJaccardMap);
		equivalenceMapList.add(fastEquivalencePredaccMap);
		equivalenceMapList.add(fastEquivalenceStandardMap);
		
		Set<EvaluatedDescriptionClass> evaluatedDescriptions = new TreeSet<EvaluatedDescriptionClass>(
				new Comparator<EvaluatedDescriptionClass>() {

					public int compare(EvaluatedDescriptionClass o1, EvaluatedDescriptionClass o2) {
						return comparator.compare(o1.getDescription(), o2.getDescription());

					};
				});
		
		Axiom axiom;
		boolean followsFromKB;
		boolean isConsistent;
		for(NamedClass nc : defaultEquivalenceMap.keySet()){
			for(Map<NamedClass, List<EvaluatedDescriptionClass>> map : equivalenceMapList){
				evaluatedDescriptions.addAll(map.get(nc));
			}
			for(EvaluatedDescriptionClass ec : evaluatedDescriptions){
				System.out.println("Checking " + ec.getDescription() + "...");
				axiom = new EquivalentClassesAxiom(nc, ec.getDescription());
				followsFromKB = reasoner.isEquivalentClass(ec.getDescription(), nc);
				isConsistent = followsFromKB || reasoner.remainsSatisfiable(axiom);
				ec.setConsistent(isConsistent);
				ec.setFollowsFromKB(followsFromKB);
				System.out.println("Consistent: " +isConsistent);
				System.out.println("Follows from KB: " + followsFromKB);
			}
			for(Map<NamedClass, List<EvaluatedDescriptionClass>> map : equivalenceMapList){
				for(EvaluatedDescriptionClass ec : map.get(nc)){
					for(EvaluatedDescriptionClass ec2 : evaluatedDescriptions){
						if(comparator.compare(ec.getDescription(), ec2.getDescription()) == 0){
							ec.setConsistent(ec2.isConsistent());
							ec.setFollowsFromKB(ec2.followsFromKB());
						}
					}
				}
			}
			evaluatedDescriptions.clear();
			
		}
		
	}
	
	
	private void loadOntology(URI ontologyURI) throws ComponentInitException, URISyntaxException, MalformedURLException{
		this.ontologyURI = ontologyURI;
		ComponentManager cm = ComponentManager.getInstance();
		// initialize KnowledgeSource
		ks = cm.knowledgeSource(OWLFile.class);
		ks.setURL(ontologyURI.toURL());
		ks.init();
			
		System.out.println("Loaded ontology " + ontologyURI + ".");
	}

	private void saveResults() {
		OutputStream fos = null;
		File old = new File(ontologyURI.getPath());
		int index = old.getName().lastIndexOf('.');
		String fileName = "test.res";
	    if (index > 0) {
	    	  fileName = old.getName().substring(0, index)+ "_";
	    	  if(reuseExistingDescription){
	    		  fileName = fileName + "t";
	    	  } else {
	    		  fileName = fileName + "f";
	    	  }
	    	  if(filterDescriptionsFollowingFromKB){
	    		  fileName = fileName + "t";
	    	  } else {
	    		  fileName = fileName + "f";
	    	  }
	    	  fileName = fileName + ".res";
	    }  
		File file = new File(fileName);
		try {
			fos = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			
			o.writeObject(owlEquivalenceStandardMap);
			o.writeObject(owlEquivalenceFMeasureMap);
			o.writeObject(owlEquivalencePredaccMap);
			o.writeObject(owlEquivalenceJaccardMap);
			o.writeObject(owlEquivalenceGenFMeasureMap);
			
			o.writeObject(fastEquivalenceStandardMap);
			o.writeObject(fastEquivalenceFMeasureMap);
			o.writeObject(fastEquivalencePredaccMap);
			o.writeObject(fastEquivalenceJaccardMap);
			o.writeObject(fastEquivalenceGenFMeasureMap);
			
			o.writeObject(defaultEquivalenceMap);
			
			
			o.flush();
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void computeSuggestions() throws ComponentInitException, MalformedURLException,
			LearningProblemUnsupportedException {
		ComponentManager cm = ComponentManager.getInstance();
		ClassLearningProblem lp = null;
		CELOE celoe = null;
		CELOEConfigurator cf = null;
		TreeSet<EvaluatedDescriptionClass> suggestions;
		for (int i = 0; i <= 1; i++) {
			if (i == 0) {
				reasoner = null;
				reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
				System.out.println("using OWLAPI-Reasoner");
			} else {
				reasoner = null;
				reasoner = cm.reasoner(FastInstanceChecker.class, ks);
				System.out.println("using FastInstanceChecker");
			}

			reasoner.init();
			baseURI = reasoner.getBaseURI();
			prefixes = reasoner.getPrefixes();

			// loop through all classes
			for (NamedClass nc : defaultEquivalenceMap.keySet()) {
					System.out.println("\nlearning axioms for class " + nc.toManchesterSyntaxString(baseURI, prefixes)
							);
//					ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
//					lp.getConfigurator().setClassToDescribe(nc.getURI().toURL());
					for (int k = 0; k <= 3; k++) {
						lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
						lp.getConfigurator().setClassToDescribe(nc.getURI().toURL());
						lp.getConfigurator().setCheckConsistency(false);
						lp.getConfigurator().setType("equivalence");
						System.out.println("Learning equivalentClass expressions");
						if (k == 0) {
							lp.getConfigurator().setAccuracyMethod("standard");
							System.out.println("Using accuracy method: standard");
						} else if (k == 1) {
							lp.getConfigurator().setAccuracyMethod("fmeasure");
							System.out.println("Using accuracy method: F-Measure");
						} else if (k == 2) {
							lp.getConfigurator().setAccuracyMethod("pred_acc");
							System.out.println("Using accuracy method: Predictive accuracy");
						} else if (k == 3) {
							lp.getConfigurator().setAccuracyMethod("jaccard");
							System.out.println("Using accuracy method: Jaccard");
						} else {
							lp.getConfigurator().setAccuracyMethod("generalised_fmeasure");
							System.out.println("Using accuracy method: Generalised F-Measure");
						}
						lp.getConfigurator().setUseApproximations(useApproximations);
						lp.init();
						celoe = cm.learningAlgorithm(CELOE.class, lp, reasoner);
						cf = celoe.getConfigurator();
						cf.setUseNegation(false);
						cf.setValueFrequencyThreshold(3);
						cf.setMaxExecutionTimeInSeconds(algorithmRuntimeInSeconds);
						cf.setNoisePercentage(noisePercent);
						cf.setMaxNrOfResults(10);
//						if(testReuseExistingDescription == ThreeValuedLogic.True){
//							cf.setReuseExistingDescription(true);
//						} else {
//							cf.setReuseExistingDescription(false);
//						}
//						if(testFilterDescriptionsFollowingFromKB == ThreeValuedLogic.True){
//							cf.setFilterDescriptionsFollowingFromKB(true);
//						} else {
//							cf.setFilterDescriptionsFollowingFromKB(false);
//						}
						cf.setReuseExistingDescription(reuseExistingDescription);
						cf.setFilterDescriptionsFollowingFromKB(filterDescriptionsFollowingFromKB);
						
						celoe.init();

						celoe.start();
						suggestions = (TreeSet<EvaluatedDescriptionClass>) celoe
						.getCurrentlyBestEvaluatedDescriptions();
						List<EvaluatedDescriptionClass> suggestionsList = new LinkedList<EvaluatedDescriptionClass>(
								suggestions.descendingSet());

						if (i == 0) {
							if (k == 0) {
								owlEquivalenceStandardMap.put(nc, suggestionsList);
							} else if (k == 1) {
								owlEquivalenceFMeasureMap.put(nc, suggestionsList);
							} else if (k == 2) {
								owlEquivalencePredaccMap.put(nc, suggestionsList);
							} else if (k == 3) {
								owlEquivalenceJaccardMap.put(nc, suggestionsList);
							} else {
								owlEquivalenceGenFMeasureMap.put(nc, suggestionsList);
							}
						} else {
							if (k == 0) {
								fastEquivalenceStandardMap.put(nc, suggestionsList);
							} else if (k == 1) {
								fastEquivalenceFMeasureMap.put(nc, suggestionsList);
							} else if (k == 2) {
								fastEquivalencePredaccMap.put(nc, suggestionsList);
							} else if (k == 3) {
								fastEquivalenceJaccardMap.put(nc, suggestionsList);
							} else {
								fastEquivalenceGenFMeasureMap.put(nc, suggestionsList);
							}
						}
						// }
						cm.freeComponent(celoe);
						 cm.freeComponent(lp);
					}
					
			}

			cm.freeComponent(reasoner);
		}
		cm.freeComponent(reasoner);
		cm.freeComponent(lp);
		cm.freeComponent(celoe);
	}
	
	
	/**
	 * Computing results for accuracy method 'Generalised F-Measure'. This is done separate because
	 * for FastInstanceChecker option useDefaultNegation is set to 'false'.
	 * @throws ComponentInitException
	 * @throws MalformedURLException
	 * @throws LearningProblemUnsupportedException
	 */
	@SuppressWarnings("unchecked")
	private void computeGenFMeasureWithoutDefaultNegation() throws ComponentInitException, MalformedURLException,
			LearningProblemUnsupportedException {
		ComponentManager cm = ComponentManager.getInstance();
		ClassLearningProblem lp = null;
		CELOE celoe = null;
		CELOEConfigurator cf = null;
		TreeSet<EvaluatedDescriptionClass> suggestions;
		for (int i = 0; i <= 1; i++) {
			if (i == 0) {
				reasoner = null;
				reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
				System.out.println("using OWLAPI-Reasoner");
			} else {
				reasoner = null;
				reasoner = cm.reasoner(FastInstanceChecker.class, ks);
				((FastInstanceChecker) reasoner).getConfigurator().setDefaultNegation(false);
				System.out.println("using FastInstanceChecker");
			}

			reasoner.init();
			baseURI = reasoner.getBaseURI();
			prefixes = reasoner.getPrefixes();

			for (NamedClass nc : defaultEquivalenceMap.keySet()) {
					System.out.println("\nlearning axioms for class " + nc.toManchesterSyntaxString(baseURI, prefixes));
					
					lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
					lp.getConfigurator().setClassToDescribe(nc.getURI().toURL());
					lp.getConfigurator().setCheckConsistency(false);
					lp.getConfigurator().setType("equivalence");
					System.out.println("Learning equivalentClass expressions");
					lp.getConfigurator().setAccuracyMethod("generalised_fmeasure");
					System.out.println("Using accuracy method: Generalised F-Measure");
					lp.getConfigurator().setUseApproximations(useApproximations);
					lp.init();
					celoe = cm.learningAlgorithm(CELOE.class, lp, reasoner);
					cf = celoe.getConfigurator();
					cf.setUseNegation(false);
					cf.setValueFrequencyThreshold(3);
					cf.setMaxExecutionTimeInSeconds(algorithmRuntimeInSeconds);
					cf.setNoisePercentage(noisePercent);
					cf.setMaxNrOfResults(10);
//					if(testReuseExistingDescription == ThreeValuedLogic.True){
//						cf.setReuseExistingDescription(true);
//					} else {
//						cf.setReuseExistingDescription(false);
//					}
//					if(testFilterDescriptionsFollowingFromKB == ThreeValuedLogic.True){
//						cf.setFilterDescriptionsFollowingFromKB(true);
//					} else {
//						cf.setFilterDescriptionsFollowingFromKB(false);
//					}
					cf.setReuseExistingDescription(reuseExistingDescription);
					cf.setFilterDescriptionsFollowingFromKB(filterDescriptionsFollowingFromKB);
					celoe.init();

					celoe.start();

					suggestions = (TreeSet<EvaluatedDescriptionClass>) celoe
					.getCurrentlyBestEvaluatedDescriptions();
					List<EvaluatedDescriptionClass> suggestionsList = new LinkedList<EvaluatedDescriptionClass>(
							suggestions.descendingSet());

					if (i == 0) {
						owlEquivalenceGenFMeasureMap.put(nc, suggestionsList);
					} else {
						fastEquivalenceGenFMeasureMap.put(nc, suggestionsList);
					}
					cm.freeComponent(celoe);
					cm.freeComponent(lp);
					
			}
			cm.freeComponent(reasoner);
		}
//		cm.freeComponent(reasoner);
		cm.freeComponent(lp);
		cm.freeComponent(celoe);
	}
	
	@SuppressWarnings("unchecked")
	private void computeWithApproximation() throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException {
		ComponentManager cm = ComponentManager.getInstance();
		TreeSet<EvaluatedDescriptionClass> suggestions;
		reasoner = null;
		reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();

		// loop through all classes
		Set<NamedClass> classes = new TreeSet<NamedClass>(reasoner.getNamedClasses());
		classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
		// reduce number of classes for testing purposes
		// shrinkSet(classes, 20);
		ClassLearningProblem lp = null;
		CELOE celoe = null;
		CELOEConfigurator cf = null;
		for (NamedClass nc : classes) {
			// check whether the class has sufficient instances
			int instanceCount = reasoner.getIndividuals(nc).size();
			if (instanceCount < minInstanceCount) {
				System.out.println("class " + nc.toManchesterSyntaxString(baseURI, prefixes) + " has only "
						+ instanceCount + " instances (minimum: " + minInstanceCount + ") - skipping");
			} else {
				System.out.println("\nlearning axioms for class " + nc.toManchesterSyntaxString(baseURI, prefixes)
						+ " with " + instanceCount + " instances");
				
				lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
				lp.getConfigurator().setClassToDescribe(nc.getURI().toURL());
				lp.getConfigurator().setCheckConsistency(false);
				lp.getConfigurator().setType("equivalence");
				System.out.println("Learning equivalentClass expressions");
				lp.getConfigurator().setUseApproximations(true);
				lp.init();
				celoe = cm.learningAlgorithm(CELOE.class, lp, reasoner);
				cf = celoe.getConfigurator();
				cf.setUseNegation(false);
				cf.setValueFrequencyThreshold(3);
				cf.setMaxExecutionTimeInSeconds(algorithmRuntimeInSeconds);
				cf.setNoisePercentage(noisePercent);
				cf.setMaxNrOfResults(10);
//				if(testReuseExistingDescription == ThreeValuedLogic.True){
//					cf.setReuseExistingDescription(true);
//				} else {
//					cf.setReuseExistingDescription(false);
//				}
//				if(testFilterDescriptionsFollowingFromKB == ThreeValuedLogic.True){
//					cf.setFilterDescriptionsFollowingFromKB(true);
//				} else {
//					cf.setFilterDescriptionsFollowingFromKB(false);
//				}
				cf.setReuseExistingDescription(reuseExistingDescription);
				cf.setFilterDescriptionsFollowingFromKB(filterDescriptionsFollowingFromKB);
				celoe.init();

				celoe.start();
				
				//check if perfect definition already exists in knowledgebase
				boolean perfectDefinitionExists = false;
				if(checkExistingDefinitions){
					for(Description def : reasoner.getAssertedDefinitions(nc)){
						if(lp.computeScore(def).getAccuracy() == 1.0){
							perfectDefinitionExists = true;
							break;
						}
					}
				}

				// test whether a solution above the threshold was found
				EvaluatedDescription best = celoe.getCurrentlyBestEvaluatedDescription();
				double bestAcc = best.getAccuracy();
				
				if (bestAcc < minAccuracy || (best.getDescription() instanceof Thing)) {
					System.out
							.println("The algorithm did not find a suggestion with an accuracy above the threshold of "
									+ (100 * minAccuracy)
									+ "% or the best description is not appropriate. (The best one was \""
									+ best.getDescription().toManchesterSyntaxString(baseURI, prefixes)
									+ "\" with an accuracy of " + df.format(bestAcc) + ".) - skipping");
				} else if(perfectDefinitionExists){
					System.out.println("It does already exists a perfect definition.\n "
									+"(The best computed was \""
									+ best.getDescription().toManchesterSyntaxString(baseURI, prefixes)
									+ "\" with an accuracy of " + df.format(bestAcc) + ") - skipping ");
														
				} else {

					suggestions = (TreeSet<EvaluatedDescriptionClass>) celoe
							.getCurrentlyBestEvaluatedDescriptions();
					List<EvaluatedDescriptionClass> suggestionsList = new LinkedList<EvaluatedDescriptionClass>(
							suggestions.descendingSet());
					defaultEquivalenceMap.put(nc, suggestionsList);
				}
				cm.freeComponent(celoe);
				cm.freeComponent(lp);

			}
		}
	}

	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws LearningProblemUnsupportedException 
	 * @throws ComponentInitException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws MalformedURLException, ComponentInitException, LearningProblemUnsupportedException, URISyntaxException {
//		Logger.getRootLogger().setLevel(Level.WARN);
		if (args.length == 0) {
			System.out.println("You need to give an OWL file as argument.");
			System.exit(0);
		}
		URI ontologyUri;
		if(args[0].startsWith("http://")){
			ontologyUri = URI.create(args[0]);
		} else {
			ontologyUri = new File(args[0]).toURI();
		}
		System.out.println(URI.create(args[0]));
		long startTime = System.currentTimeMillis();
		
		if(args.length == 1){
			new EvaluationComputingScript(ontologyUri);
		} else if(args.length == 2){
			boolean reuseExistingDescription = false;
			boolean filterDescriptionsFollowingFromKB = false;
			if(args[1].equals("-r")){
				reuseExistingDescription = true;
			} else if(args[1].equals("-f")){
				filterDescriptionsFollowingFromKB = true;
			} else {
				System.out.println("Wrong arguments. Type for reusing existing descriptions '-r' and for filtering existing description following from KB '-f'.");
				System.exit(0);
			}
			new EvaluationComputingScript(ontologyUri, reuseExistingDescription, filterDescriptionsFollowingFromKB);
		} else if(args.length == 3){
			boolean reuseExistingDescription = false;
			boolean filterDescriptionsFollowingFromKB = false;
			if(args[1].equals("-r")){
				reuseExistingDescription = true;
			} else if(args[1].equals("-f")){
				filterDescriptionsFollowingFromKB = true;
			} else {
				System.out.println("Wrong arguments. Type for reusing existing descriptions '-r' and for filtering existing description following from KB '-f'.");
				System.exit(0);
			}
			if(args[2].equals("-r")){
				reuseExistingDescription = true;
			} else if(args[2].equals("-f")){
				filterDescriptionsFollowingFromKB = true;
			} else {
				System.out.println("Wrong arguments. Type for reusing existing descriptions '-r' and for filtering existing description following from KB '-f'.");
				System.exit(0);
			}
			new EvaluationComputingScript(ontologyUri, reuseExistingDescription, filterDescriptionsFollowingFromKB);
			
		} else {
			System.out.println("Wrong number of arguments. Type the URL (mandatory) and optional for reusing existing descriptions '-r' and for filtering existing description following from KB '-f'.");
			System.exit(0);
		}
		System.out.println("Overall computing time: " + (System.currentTimeMillis() - startTime)/1000 +" s");
		
		
	}

}
