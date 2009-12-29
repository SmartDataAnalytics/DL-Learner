package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;


public class StatsGenerator {
	

	private Map<NamedClass, List<EvaluatedDescriptionClass>> equivalentSuggestions;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> superSuggestions;
	
	private Map<NamedClass, String> equivalentInput;
	private Map<NamedClass, String> superInput;
	
	private StringBuilder latex;
	
	//general stats
	private Stat acceptedGlobalStat = new Stat();
	private Stat rejectedGlobalStat = new Stat();
	private Stat failedGlobalStat = new Stat();
	
	//equivalent class stats
	private Stat acceptedStat = new Stat();
	private Stat rejectedStat = new Stat();
	private Stat failedStat = new Stat();
	private Set<Stat> moreInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> accStats = new HashSet<Stat>();
	private Set<Stat> accSelectedStats = new HashSet<Stat>();
	private Set<Stat> accAboveThresholdStats = new HashSet<Stat>();
	private Set<Stat> positionStats = new HashSet<Stat>();
	
	//super class stats
	private Stat acceptedStatSC = new Stat();
	private Stat rejectedStatSC = new Stat();
	private Stat failedStatSC = new Stat();
	private Set<Stat> moreInstancesCountStatsSC = new HashSet<Stat>();
	private Set<Stat> accStatsSC = new HashSet<Stat>();
	private Set<Stat> accSelectedStatsSC = new HashSet<Stat>();
	private Set<Stat> accAboveThresholdStatsSC = new HashSet<Stat>();
	private Set<Stat> positionStatsSC = new HashSet<Stat>();
	
	private int suggestionListsCount;
	private int logicalAxiomCount;
	
	
	public StatsGenerator(File directory){
		//begin latex table with headers
		beginTable();
		//for each ontology
		for(File suggestionFile : directory.listFiles(new ResultFileFilter())){
			clearStats();
			loadSuggestions(suggestionFile);
			loadOntology(suggestionFile);
			//for each user evaluation input file
			for(File inputFile : directory.listFiles(new NameFilter(suggestionFile))){
				loadUserInput(inputFile);
				makeSingleStat();
				printStats();
				
			}
			//add row to the latex table for current ontology
			addTableRow();
		}
		//end latex table
		endTable();
		printLatexCode();
		
		
	}
	
	private void loadOntology(File file){
		String ontologyPath = file.toURI().toString().substring(0, file.toURI().toString().lastIndexOf('.') ) + ".owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		try {
			OWLOntology ont = man.loadOntologyFromPhysicalURI(URI.create(ontologyPath));
			logicalAxiomCount = ont.getLogicalAxiomCount();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void clearStats(){
		acceptedGlobalStat = new Stat();
		rejectedGlobalStat = new Stat();
		failedGlobalStat = new Stat();
		
		moreInstancesCountStats.clear();
		accStats.clear();
		accSelectedStats.clear();
		accAboveThresholdStats.clear();
		positionStats.clear();
		
		moreInstancesCountStatsSC.clear();
		accStatsSC.clear();
		accSelectedStatsSC.clear();
		accAboveThresholdStatsSC.clear();
		positionStatsSC.clear();
	}
	
	private void makeSingleStat(){
		// equivalence classes
		int candidatesAboveThresholdCount = 0;
		int missesCount = 0;
		int foundDescriptionCount = 0;
		int noSensibleDescriptionCount = 0;
		int inconsistencyDetected = 0;
		int moreInstancesCount = 0;
		int nonPerfectCount = 0;
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat accSelectedStat = new Stat();
		Stat accAboveThresholdStat = new Stat();
		Stat positionStat = new Stat();

		// super classes
		int candidatesAboveThresholdCountSC = 0;
		int missesCountSC = 0;
		int foundDescriptionCountSC = 0;
		int noSensibleDescriptionCountSC = 0;
		int inconsistencyDetectedSC = 0;
		int moreInstancesCountSC = 0;
		int nonPerfectCountSC = 0;
		Stat moreInstancesCountStatSC = new Stat();
		Stat accStatSC = new Stat();
		Stat accSelectedStatSC = new Stat();
		Stat accAboveThresholdStatSC = new Stat();
		Stat positionStatSC = new Stat();
		
		//analysing input for equivalent class expressions
		for (Entry<NamedClass, String> e : equivalentInput.entrySet()) {
			NamedClass currentClass = e.getKey();
			String input = e.getValue();
			if (input.equals("m")) {
				missesCount++;
			} else if (input.equals("n")) {
				noSensibleDescriptionCount++;
			} else {
				int selectedIndex = Integer.parseInt(input);
				EvaluatedDescriptionClass selectedExpression = equivalentSuggestions.get(currentClass).get(
						selectedIndex);
				double bestAcc = equivalentSuggestions.get(currentClass).get(0).getAccuracy();
				int selectedNr = selectedIndex + 1;
				boolean isConsistent = selectedExpression.isConsistent();
				Set<Individual> addInst = selectedExpression.getAdditionalInstances();
				int additionalInstances = addInst.size();

				accSelectedStat.addNumber(selectedExpression.getAccuracy());
				positionStat.addNumber(selectedNr);
				foundDescriptionCount++;
				if (!isConsistent) {
					inconsistencyDetected++;
				}
				if (additionalInstances > 0) {
					moreInstancesCount++;
					moreInstancesCountStat.addNumber(additionalInstances);
				}
				if (bestAcc < 0.9999) {
					nonPerfectCount++;
				}
			}
		}
		acceptedStat.addNumber(foundDescriptionCount);
		rejectedStat.addNumber(noSensibleDescriptionCount);
		failedStat.addNumber(missesCount);
		moreInstancesCountStats.add(moreInstancesCountStat);
		accStats.add(accStat);
		accSelectedStats.add(accSelectedStat);
		accAboveThresholdStats.add(accSelectedStat);
		positionStats.add(positionStat);
		
		//analysing input for super class expressions
		for (Entry<NamedClass, String> e : superInput.entrySet()) {
			NamedClass currentClass = e.getKey();
			if (e.getValue().equals("m")) {
				missesCountSC++;
			} else if (e.getValue().equals("n")) {
				noSensibleDescriptionCountSC++;
			} else {
				int selectedIndex = Integer.parseInt(e.getValue());
				EvaluatedDescriptionClass selectedExpression = superSuggestions.get(currentClass).get(selectedIndex);
				double bestAcc = superSuggestions.get(currentClass).get(0).getAccuracy();
				int selectedNr = selectedIndex + 1;
				boolean isConsistent = selectedExpression.isConsistent();
				Set<Individual> addInst = selectedExpression.getAdditionalInstances();
				int additionalInstances = addInst.size();

				accSelectedStatSC.addNumber(selectedExpression.getAccuracy());
				positionStatSC.addNumber(selectedNr);
				foundDescriptionCountSC++;
				if (!isConsistent) {
					inconsistencyDetectedSC++;
				}
				if (additionalInstances > 0) {
					moreInstancesCountSC++;
					moreInstancesCountStatSC.addNumber(additionalInstances);
				}
				if (bestAcc < 0.9999) {
					nonPerfectCountSC++;
				}
			}
		}
		acceptedStatSC.addNumber(foundDescriptionCountSC);
		rejectedStatSC.addNumber(noSensibleDescriptionCountSC);
		failedStatSC.addNumber(missesCountSC);
		moreInstancesCountStatsSC.add(moreInstancesCountStatSC);
		accStatsSC.add(accStatSC);
		accSelectedStatsSC.add(accSelectedStatSC);
		accAboveThresholdStatsSC.add(accSelectedStatSC);
		positionStatsSC.add(positionStatSC);
		
		acceptedGlobalStat.addNumber(foundDescriptionCount + foundDescriptionCountSC);
		rejectedGlobalStat.addNumber(noSensibleDescriptionCountSC + noSensibleDescriptionCount);
		failedGlobalStat.addNumber(missesCountSC + missesCount);
		
		System.out.println("statistics for equivalence axioms:");
		System.out.println("classes above 85% threshold: " + candidatesAboveThresholdCount);
		System.out.println("axioms learned succesfully: " + foundDescriptionCount);
		System.out.println("axioms missed: " + missesCount);
		System.out.println("class with no sensible axioms: " + noSensibleDescriptionCount);
		System.out.println("inconsistencies detected: " + inconsistencyDetected);
		System.out.println("additional instances found: " + moreInstancesCountStat.prettyPrint(""));
		System.out.println("average accuracy overall: " + accStat.prettyPrint(""));
		System.out.println("average accuracy of selected expressions: " + accSelectedStat.prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold: " + accAboveThresholdStat.prettyPrint(""));
		System.out.println("non-perfect (not 100% accuracy) axioms selected: " + nonPerfectCount);
		System.out.println("average number typed by user: " + positionStat.prettyPrint(""));
		System.out.println();

		System.out.println("statistics for super class axioms:");
		System.out.println("classes above 85% threshold: " + candidatesAboveThresholdCountSC);
		System.out.println("axioms learned succesfully: " + foundDescriptionCountSC);
		System.out.println("axioms missed: " + missesCountSC);
		System.out.println("class with no sensible axioms: " + noSensibleDescriptionCountSC);
		System.out.println("inconsistencies detected: " + inconsistencyDetectedSC);
		System.out.println("additional instances found: " + moreInstancesCountStatSC.prettyPrint(""));
		System.out.println("average accuracy overall: " + accStatSC.prettyPrint(""));
		System.out.println("average accuracy of selected expressions: " + accSelectedStatSC.prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold: "
				+ accAboveThresholdStatSC.prettyPrint(""));
		System.out.println("non-perfect (not 100% accuracy) axioms selected: " + nonPerfectCountSC);
		System.out.println("average number typed by user: " + positionStatSC.prettyPrint(""));
		System.out.println();

		System.out.println("merged statistics for equivalence/superclass:");
		System.out.println("classes above 85% threshold: "
				+ (candidatesAboveThresholdCount + candidatesAboveThresholdCountSC));
		System.out.println("axioms learned succesfully: " + (foundDescriptionCount + foundDescriptionCountSC));
		System.out.println("axioms missed: " + (missesCount + missesCountSC));
		System.out.println("class with no sensible axioms: "
				+ (noSensibleDescriptionCount + noSensibleDescriptionCountSC));
		System.out.println("inconsistencies detected: " + (inconsistencyDetected + inconsistencyDetectedSC));
		System.out.println("additional instances found: "
				+ new Stat(moreInstancesCountStat, moreInstancesCountStatSC).prettyPrint(""));
		System.out.println("average accuracy overall: " + new Stat(accStat, accStatSC).prettyPrint(""));
		System.out.println("average accuracy of selected expressions: "
				+ new Stat(accSelectedStat, accSelectedStatSC).prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold: "
				+ new Stat(accAboveThresholdStat, accAboveThresholdStatSC).prettyPrint(""));
		System.out.println("non-perfect (not 100% accuracy) axioms selected: " + (nonPerfectCount + nonPerfectCountSC));
		System.out.println("average number typed by user: " + new Stat(positionStat, positionStatSC).prettyPrint(""));
		System.out.println();
	}
	
	
	private void printStats() {

		
	}
	
	private void printLatexCode(){
		System.out.println(latex.toString());
	}
	
	private void addTableRow(){
		double accept = acceptedGlobalStat.getMean() / suggestionListsCount * 100;
		double reject = rejectedGlobalStat.getMean() /suggestionListsCount * 100;
		double fail = failedGlobalStat.getMean() /suggestionListsCount * 100;
		latex.append(logicalAxiomCount + " & " + suggestionListsCount + " & " + 
				accept + " & " + reject + " & " + fail + " & " + new Stat(positionStats).getMean() +"\\\\\n");
	}
	
	private void beginTable(){
		latex = new StringBuilder();
		latex.append("\\begin{tabular}{ c | c | c | c | c | c } \n");
		latex.append("\\rotatebox{90}{\\#logical axioms} & ");
		latex.append("\\rotatebox{90}{\\#suggestions lists} & ");
		latex.append("\\rotatebox{90}{accept} & ");
		latex.append("\\rotatebox{90}{reject} & ");
		latex.append("\\rotatebox{90}{fail} & ");
		latex.append("\\rotatebox{90}{selected positions}");
		latex.append(" \\\\\n");
		latex.append("\\hline\n");
	}
	
	private void endTable(){
		latex.append("\\hline\n");
		latex.append("\\end{tabular}");
	}
	
	private void loadSuggestions(File resultFile) {
		InputStream fis = null;

		try {
			fis = new FileInputStream(resultFile);
			ObjectInputStream o = new ObjectInputStream(fis);
			for (int i = 0; i < 20; i++) {
				o.readObject();
			}
			equivalentSuggestions = (Map<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			superSuggestions = (Map<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
		} catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		suggestionListsCount = equivalentSuggestions.size() + superSuggestions.size();
	}
	
	private void loadUserInput(File input){
		InputStream fis = null;

		try {
			fis = new FileInputStream(input);
			ObjectInputStream o = new ObjectInputStream(fis);

			equivalentInput = (Map<NamedClass, String>) o.readObject();
			superInput = (Map<NamedClass, String>) o.readObject();
		} catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		File directory = new File(new URL(args[0]).toURI());
		new StatsGenerator(directory);
	}

}

class ResultFileFilter implements FileFilter {

	public boolean accept(File pathname) {

		if (pathname.getName().endsWith(".res")) {
			return true;
		}
		return false;
	}
}

class InputFileFilter implements FileFilter {

	public boolean accept(File pathname) {

		if (pathname.getName().endsWith(".inp")) {
			return true;
		}
		return false;
	}
}

class NameFilter implements FilenameFilter {
	private File file;
	
	public NameFilter(File file){
		this.file = file;
	}

	@Override
	public boolean accept(File dir, String name) {
		if(name.endsWith("inp")){
			if(name.substring(0, name.indexOf('.')).startsWith(file.getName().substring(0, file.getName().indexOf('.')))){
				return true;
			}
		}
		return false;
	}
}
