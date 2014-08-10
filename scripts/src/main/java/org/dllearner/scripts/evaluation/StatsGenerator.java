package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.statistics.RawAgreement;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

public class StatsGenerator {
	
	private Hashtable<OWLClass, Map<EvaluatedDescriptionClass, Integer>> userInputMap;
	
	private Map<OWLClass, List<EvaluatedDescriptionClass>> fastStandardMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> fastFMeasureMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> fastPredaccMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> fastGenFMeasureMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> fastJaccardMap;

	private Map<OWLClass, List<EvaluatedDescriptionClass>> owlStandardMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> owlFMeasureMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> owlPredaccMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> owlGenFMeasureMap;
	private Map<OWLClass, List<EvaluatedDescriptionClass>> owlJaccardMap;

	private Map<OWLClass, List<EvaluatedDescriptionClass>> defaultMap;

	
	private StringBuilder latexStats;
	private StringBuilder latexMetrics;
	
	@SuppressWarnings("unused")
	private Map<OWLClass, Map<EvaluatedDescriptionClass, Integer>> incMap;

	List<Stat> defaultFractions = new ArrayList<Stat>();
	List<Stat> owlStandardFractions = new ArrayList<Stat>();
	List<Stat> owlFMeasureFractions = new ArrayList<Stat>();
	List<Stat> owlGenFMeasureFractions = new ArrayList<Stat>();
	List<Stat> owlPredaccFractions = new ArrayList<Stat>();
	List<Stat> owlJaccardFractions = new ArrayList<Stat>();
	List<Stat> fastStandardFractions = new ArrayList<Stat>();
	List<Stat> fastFMeasureFractions = new ArrayList<Stat>();
	List<Stat> fastGenFMeasureFractions = new ArrayList<Stat>();
	List<Stat> fastPredaccFractions = new ArrayList<Stat>();
	List<Stat> fastJaccardFractions = new ArrayList<Stat>();
	
	List<Stat> defaultFractionsBest = new ArrayList<Stat>();
	List<Stat> owlStandardFractionsBest = new ArrayList<Stat>();
	List<Stat> owlFMeasureFractionsBest = new ArrayList<Stat>();
	List<Stat> owlGenFMeasureFractionsBest = new ArrayList<Stat>();
	List<Stat> owlPredaccFractionsBest = new ArrayList<Stat>();
	List<Stat> owlJaccardFractionsBest = new ArrayList<Stat>();
	List<Stat> fastStandardFractionsBest = new ArrayList<Stat>();
	List<Stat> fastFMeasureFractionsBest = new ArrayList<Stat>();
	List<Stat> fastGenFMeasureFractionsBest = new ArrayList<Stat>();
	List<Stat> fastPredaccFractionsBest = new ArrayList<Stat>();
	List<Stat> fastJaccardFractionsBest = new ArrayList<Stat>();
	
	int[] count = new int[6];
	int[] bestCount = new int[6];
	int best = 6;
	
	int classesCount = 0;
	
	private static final int RATER = 4;
	private static final double MIN_ACCURACY = 0.9;
	
	int defaultMissedImprovementsCount = 0;
	int owlStandardMissedImprovementsCount = 0;
	int owlFMeasureMissedImprovementsCount = 0;
	int owlGenFMeasureMissedImprovementsCount = 0;
	int owlJaccardMissedImprovementsCount = 0;
	int owlPredaccMissedImprovementsCount = 0;
	int fastStandardMissedImprovementsCount = 0;
	int fastFMeasureMissedImprovementsCount = 0;
	int fastGenFMeasureMissedImprovementsCount = 0;
	int fastJaccardMissedImprovementsCount = 0;
	int fastPredaccMissedImprovementsCount = 0;
	
	double defaultSuggestions = 0;
	double owlStandardSuggestions = 0;
	double owlFMeasureSuggestions = 0;
	double owlGenFMeasureSuggestions = 0;
	double owlJaccardSuggestions = 0;
	double owlPredaccSuggestions = 0;
	double fastStandardSuggestions = 0;
	double fastFMeasureSuggestions = 0;
	double fastGenFMeasureSuggestions = 0;
	double fastJaccardSuggestions = 0;
	double fastPredaccSuggestions = 0;
	
	private Set<Stat> defaultPositionStats = new HashSet<Stat>();
	private Set<Stat> defaultAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> defaultAccStats = new HashSet<Stat>();
	private Set<Stat> defaultHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> owlStandardPositionStats = new HashSet<Stat>();
	private Set<Stat> owlStandardAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> owlStandardAccStats = new HashSet<Stat>();
	private Set<Stat> owlStandardHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> owlFMeasurePositionStats = new HashSet<Stat>();
	private Set<Stat> owlFMeasureAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> owlFMeasureAccStats = new HashSet<Stat>();
	private Set<Stat> owlFMeasureHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> owlGenFMeasurePositionStats = new HashSet<Stat>();
	private Set<Stat> owlGenFMeasureAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> owlGenFMeasureAccStats = new HashSet<Stat>();
	private Set<Stat> owlGenFMeasureHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> owlJaccardPositionStats = new HashSet<Stat>();
	private Set<Stat> owlJaccardAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> owlJaccardAccStats = new HashSet<Stat>();
	private Set<Stat> owlJaccardHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> owlPredaccPositionStats = new HashSet<Stat>();
	private Set<Stat> owlPredaccAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> owlPredaccAccStats = new HashSet<Stat>();
	private Set<Stat> owlPredaccHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> fastStandardPositionStats = new HashSet<Stat>();
	private Set<Stat> fastStandardAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> fastStandardAccStats = new HashSet<Stat>();
	private Set<Stat> fastStandardHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> fastFMeasurePositionStats = new HashSet<Stat>();
	private Set<Stat> fastFMeasureAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> fastFMeasureAccStats = new HashSet<Stat>();
	private Set<Stat> fastFMeasureHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> fastGenFMeasurePositionStats = new HashSet<Stat>();
	private Set<Stat> fastGenFMeasureAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> fastGenFMeasureAccStats = new HashSet<Stat>();
	private Set<Stat> fastGenFMeasureHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> fastJaccardPositionStats = new HashSet<Stat>();
	private Set<Stat> fastJaccardAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> fastJaccardAccStats = new HashSet<Stat>();
	private Set<Stat> fastJaccardHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	private Set<Stat> fastPredaccPositionStats = new HashSet<Stat>();
	private Set<Stat> fastPredaccAddInstancesCountStats = new HashSet<Stat>();
	private Set<Stat> fastPredaccAccStats = new HashSet<Stat>();
	private Set<Stat> fastPredaccHiddenInconsistenciesCountStats = new HashSet<Stat>();
	
	@SuppressWarnings("unused")
	private Stat defaultSuggestionsCountStat = new Stat();
	private Stat owlStandardSuggestionsCountStat = new Stat();
	private Stat owlFMeasureSuggestionsCountStat = new Stat();
	private Stat owlGenFMeasureSuggestionsCountStat = new Stat();
	private Stat owlPredaccSuggestionsCountStat = new Stat();
	private Stat owlJaccardSuggestionsCountStat = new Stat();
	private Stat fastStandardSuggestionsCountStat = new Stat();
	private Stat fastFMeasureSuggestionsCountStat = new Stat();
	private Stat fastGenFMeasureSuggestionsCountStat = new Stat();
	private Stat fastPredaccSuggestionsCountStat = new Stat();
	private Stat fastJaccardSuggestionsCountStat = new Stat();

	private OWLOntology ont;
	
	//matrix for Fleiss Kappa input
	private short[][] mat;
	
	private Set<OWLClass> classesToEvaluate = new HashSet<OWLClass>();
	
	private ConceptComparator c = new ConceptComparator();
	
	private File directory;
	
	OWLDataFactory factory ;
	ClassLearningProblem lp;
	FastInstanceChecker rc;
	
	public StatsGenerator(File directory) {
		this.directory = directory;
	
		for(int i = 0; i < 6; i++){
			defaultFractions.add(new Stat());
			owlStandardFractions.add(new Stat());
			owlFMeasureFractions.add(new Stat());
			owlGenFMeasureFractions.add(new Stat());
			owlPredaccFractions.add(new Stat());
			owlJaccardFractions.add(new Stat());
			fastStandardFractions.add(new Stat());
			fastFMeasureFractions.add(new Stat());
			fastGenFMeasureFractions.add(new Stat());
			fastPredaccFractions.add(new Stat());
			fastJaccardFractions.add(new Stat());
			
			defaultFractionsBest.add(new Stat());
			owlStandardFractionsBest.add(new Stat());
			owlFMeasureFractionsBest.add(new Stat());
			owlGenFMeasureFractionsBest.add(new Stat());
			owlPredaccFractionsBest.add(new Stat());
			owlJaccardFractionsBest.add(new Stat());
			fastStandardFractionsBest.add(new Stat());
			fastFMeasureFractionsBest.add(new Stat());
			fastGenFMeasureFractionsBest.add(new Stat());
			fastPredaccFractionsBest.add(new Stat());
			fastJaccardFractionsBest.add(new Stat());
		}
		count();
		// begin latex table with headers
		beginOntologyMetricsTable();
		beginStatsTable();
		// for each ontology
		for (File suggestionFile : directory.listFiles(new ResultFileFilter())) {
			loadLearnResults(suggestionFile);
			loadOntology(suggestionFile);
			computeClassesToEvaluate();
			// for each user evaluation input file
			for (File inputFile : directory.listFiles(new NameFilter(suggestionFile))) {
				loadUserInput(inputFile);
				evaluateUserInput();
			}
			// add row to the metrics latex table for current ontology
			addOntologyMetricsTableRow();
//			break;
		}
//		showMeasureValues();
		computeAverageSuggestionsPerClass();
		showMeasureFractions();
		generateBestMeasureFractionsTable();
		generateMetricsTable();
		computeFleissKappa(directory);
		// end latex tables
		endTables();
		printLatexCode();

	}
	
	private void evaluateUserInput(){
		evaluateDefault();
		evaluateFastFMeasure();
		evaluateFastGenFMeasure();
		evaluateFastJaccard();
		evaluateFastPredacc();
		evaluateFastStandard();
		evaluateOWLFMeasure();
		evaluateOWLGenFMeasure();
		evaluateOWLJaccard();
		evaluateOWLPredacc();
		evaluateOWLStandard();
	}
	
	private void computeAverageSuggestionsPerClass(){
		for (File suggestionFile : directory.listFiles(new ResultFileFilter())) {
			loadLearnResults(suggestionFile);
			computeClassesToEvaluate();
			classesCount += classesToEvaluate.size();
			defaultSuggestions += getSuggestionsCount(defaultMap);
			owlStandardSuggestions += getSuggestionsCount(owlStandardMap);
			owlFMeasureSuggestions += getSuggestionsCount(owlFMeasureMap);
			owlGenFMeasureSuggestions += getSuggestionsCount(owlGenFMeasureMap);
			owlPredaccSuggestions += getSuggestionsCount(owlPredaccMap);
			owlJaccardSuggestions += getSuggestionsCount(owlJaccardMap);
			fastStandardSuggestions += getSuggestionsCount(fastStandardMap);
			fastFMeasureSuggestions += getSuggestionsCount(fastFMeasureMap);
			fastGenFMeasureSuggestions += getSuggestionsCount(fastGenFMeasureMap);
			fastPredaccSuggestions += getSuggestionsCount(fastPredaccMap);
			fastJaccardSuggestions += getSuggestionsCount(fastJaccardMap);
		}
		System.out.println(classesCount);
	}
	
	private void count(){
	
		for (File suggestionFile : directory.listFiles(new ResultFileFilter())) {
			loadLearnResults(suggestionFile);
			computeClassesToEvaluate();
			for(NamedClass nc : classesToEvaluate){
				System.out.println(nc + " : " + fastStandardMap.get(nc).size());
				owlStandardSuggestionsCountStat.addNumber(owlStandardMap.get(nc).size());
				owlFMeasureSuggestionsCountStat.addNumber(owlFMeasureMap.get(nc).size());
				owlGenFMeasureSuggestionsCountStat.addNumber(owlGenFMeasureMap.get(nc).size());
				owlPredaccSuggestionsCountStat.addNumber(owlPredaccMap.get(nc).size());
				owlJaccardSuggestionsCountStat.addNumber(owlJaccardMap.get(nc).size());
				fastStandardSuggestionsCountStat.addNumber(fastStandardMap.get(nc).size());
				fastFMeasureSuggestionsCountStat.addNumber(fastFMeasureMap.get(nc).size());
				fastGenFMeasureSuggestionsCountStat.addNumber(fastGenFMeasureMap.get(nc).size());
				fastPredaccSuggestionsCountStat.addNumber(fastPredaccMap.get(nc).size());
				fastJaccardSuggestionsCountStat.addNumber(fastJaccardMap.get(nc).size());
			}
		}
	}
	
	private int getSuggestionsCount(Map<OWLClass, List<EvaluatedDescriptionClass>> map){
		int suggestionsCount = 0;
		for(Entry<OWLClass, List<EvaluatedDescriptionClass>> entry : map.entrySet()){
			if(!classesToEvaluate.contains(entry.getKey())){
				continue;
			}
			suggestionsCount += entry.getValue().size();
		}
		
		return suggestionsCount;
	}
	
	private void showMeasureFractions(){
		DecimalFormat df = new DecimalFormat( "0.00" ); 
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin{tabular}{| l | c | c | c | c | c | c |} \n");
		sb.append(" & ");
		sb.append("\\rotatebox{90}{Improvement} & ");
		sb.append("\\rotatebox{90}{Equal quality (+)} & ");
		sb.append("\\rotatebox{90}{Equal quality (-)} & ");
		sb.append("\\rotatebox{90}{Inferior} & ");
		sb.append("\\rotatebox{90}{Not acceptable} & ");
		sb.append("\\rotatebox{90}{Error}");
		sb.append(" \\\\\n");
		sb.append("\\hline\n");
		
		
		sb.append("Default & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(defaultFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("OWLAPI Reasoner Standard & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlStandardFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("OWLAPI Reasoner FMeasure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlFMeasureFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("OWLAPI Reasoner GenFMeasure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlGenFMeasureFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("OWLAPI Reasoner Predacc & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlPredaccFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("OWLAPI Reasoner Jaccard & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlJaccardFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("FastInstanceChecker Standard & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastStandardFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("FastInstanceChecker FMeasure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastFMeasureFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("FastInstanceChecker GenFMeasure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastGenFMeasureFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("FastInstanceChecker Predacc & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastPredaccFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		sb.append(" \\\\\n");
		sb.append("FastInstanceChecker Jaccard & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastJaccardFractions.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
		
		sb.append(" \\\\\n");
		sb.append("\\hline\n");
		sb.append("\\end{tabular}");
		System.out.println(sb.toString());

	}
	
	private void generateMetricsTable(){
		  
		DecimalFormat df = new DecimalFormat( "0.00" ); 
		DecimalFormat df1 = new DecimalFormat( "0" ); 
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin{tabular}{ l | c | c | c | c | c | c  } \n");
		sb.append("reasoner / heuristic & \n");
		sb.append("\\rotatebox{90}{missed improvements in \\%} & \n");
		sb.append("\\rotatebox{90}{avg. suggestions per class} & \n");
		sb.append("\\begin{sideways}\\makecell[l]{selected position\\\\on suggestion list\\\\(incl.~std.~deviation)}\\end{sideways} & \n");
		sb.append("\\begin{sideways}\\makecell[l]{avg. accuracy of\\\\selected suggestion in \\%}\\end{sideways} & \n");
		sb.append("\\begin{sideways}\\makecell[l]{\\#hidden inconsistencies}\\end{sideways} & \n");
		sb.append("\\rotatebox{90}{\\#add.~instances total}");
		sb.append(" \\\\\n");
		sb.append("\\hline\n");
		
		sb.append("Pellet / R-Measure & ");
		sb.append(df.format((double)owlStandardMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(owlStandardSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlStandardPositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(owlStandardPositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlStandardAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlStandardHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(owlStandardAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / F-Measure & ");
		sb.append(df.format((double)owlFMeasureMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(owlFMeasureSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlFMeasurePositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(owlFMeasurePositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlFMeasureAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlFMeasureHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(owlFMeasureAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / Gen. F-Measure & ");
		sb.append(df.format((double)owlGenFMeasureMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(owlGenFMeasureSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlGenFMeasurePositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(owlGenFMeasurePositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlGenFMeasureAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlGenFMeasureHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(owlGenFMeasureAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / pred. acc. & ");
		sb.append(df.format((double)owlPredaccMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(owlPredaccSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlPredaccPositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(owlPredaccPositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlPredaccAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlPredaccHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(owlPredaccAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / Jaccard & ");
		sb.append(df.format((double)owlJaccardMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(owlJaccardSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlJaccardPositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(owlJaccardPositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlJaccardAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(owlJaccardHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(owlJaccardAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / R-Measure & ");
		sb.append(df.format((double)fastStandardMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(fastStandardSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastStandardPositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(fastStandardPositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastStandardAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastStandardHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(fastStandardAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / F-Measure & ");
		sb.append(df.format((double)fastFMeasureMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(fastFMeasureSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastFMeasurePositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(fastFMeasurePositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastFMeasureAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastFMeasureHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(fastFMeasureAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / Gen. F-Measure & ");
		sb.append(df.format((double)fastGenFMeasureMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(fastGenFMeasureSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastGenFMeasurePositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(fastGenFMeasurePositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastGenFMeasureAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastGenFMeasureHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(fastGenFMeasureAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / pred. acc. & ");
		sb.append(df.format((double)fastPredaccMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(fastPredaccSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastPredaccPositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(fastPredaccPositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastPredaccAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastPredaccHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(fastPredaccAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / Jaccard & ");
		sb.append(df.format((double)fastJaccardMissedImprovementsCount / classesCount * 100));
		sb.append(" & ");
		sb.append(df.format(fastJaccardSuggestionsCountStat.getMean()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastJaccardPositionStats).getMean()));
		sb.append(" $\\pm$ ");
		sb.append(df.format(new Stat(fastJaccardPositionStats).getStandardDeviation()));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastJaccardAccStats).getMean() * 100));
		sb.append(" & ");
		sb.append(df.format(new Stat(fastJaccardHiddenInconsistenciesCountStats).getSum() / 5));
		sb.append(" & ");
		sb.append(df1.format(new Stat(fastJaccardAddInstancesCountStats).getSum() / RATER));
		sb.append(" \\\\\n");
		
		sb.append("\\hline\n");
		sb.append("\\end{tabular}");
		
		System.out.println(sb.toString());
	}
	
	private void generateBestMeasureFractionsTable(){
		DecimalFormat df = new DecimalFormat( "0.00" ); 
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin{tabular}{| l | c | c | c | c | c | c |} \n");
		sb.append(" & ");
		sb.append("\\rotatebox{90}{improvement} & ");
		sb.append("\\rotatebox{90}{equal quality (+)} & ");
		sb.append("\\rotatebox{90}{equal quality (-)} & ");
		sb.append("\\rotatebox{90}{inferior} & ");
		sb.append("\\rotatebox{90}{not acceptable} & ");
		sb.append("\\rotatebox{90}{error} ");
		sb.append(" \\\\\n");
		sb.append("\\hline\n");
		
		classesCount *= RATER;
		
//		sb.append("Default & ");
//		for(int i = 0; i < 6; i++){
//			sb.append(df.format(defaultFractionsBest.get(i).getMean() * 100));
//			sb.append(" & ");
//		}
//		sb.append(df.format((double)defaultMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(defaultSuggestions/(double)classesCount * 4));
//		sb.append(" \\\\\n");
		
		sb.append("Pellet / R-Measure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlStandardFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)owlStandardMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(owlStandardSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / F-Measure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlFMeasureFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)owlFMeasureMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(owlFMeasureSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / Gen. F-Measure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlGenFMeasureFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)owlGenFMeasureMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(owlGenFMeasureSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / pred. acc. & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlPredaccFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)owlPredaccMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(owlPredaccSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet / Jaccard & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(owlJaccardFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)owlJaccardMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(owlJaccardSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / R-Measure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastStandardFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)fastStandardMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(fastStandardSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / F-Measure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastFMeasureFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)fastFMeasureMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(fastFMeasureSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / Gen. F-Measure & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastGenFMeasureFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)fastGenFMeasureMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(fastGenFMeasureSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / pred. acc. & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastPredaccFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)fastPredaccMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(fastPredaccSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("Pellet FIC / Jaccard & ");
		for(int i = 0; i < 6; i++){
			sb.append(df.format(fastJaccardFractionsBest.get(i).getMean() * 100));
			if(i < 5){
				sb.append(" & ");
			}
		}
//		sb.append(df.format((double)fastJaccardMissedImprovementsCount / classesCount * 100));
//		sb.append(" & ");
//		sb.append(df.format(fastJaccardSuggestions/(double)classesCount * 4));
		sb.append(" \\\\\n");
		
		sb.append("\\hline\n");
		sb.append("\\end{tabular}");
		System.out.println(sb.toString());
	}
	

	
	private boolean isImprovementSelected(NamedClass nc){
		Map<EvaluatedDescriptionClass, Integer> input = userInputMap.get(nc);
		boolean improvementSelected = false;
		for(EvaluatedDescriptionClass ec : input.keySet()){
			for(EvaluatedDescriptionClass ec2 : defaultMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : fastStandardMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : fastFMeasureMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : fastGenFMeasureMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : fastJaccardMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : fastPredaccMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : owlStandardMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : owlFMeasureMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : owlGenFMeasureMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : owlJaccardMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			for(EvaluatedDescriptionClass ec2 : owlPredaccMap.get(nc)){
				if(ec2.getAccuracy() < MIN_ACCURACY){
					continue;
				}
				if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
					if(input.get(ec) == 1){
						return true;
					}
				}
			}
			
		}
		
		return improvementSelected;
	}
	
	private void computeClassesToEvaluate(){
		classesToEvaluate.clear();
		for(Entry<OWLClass, List<EvaluatedDescriptionClass>> entry : defaultMap.entrySet()){
			for(EvaluatedDescriptionClass ec : entry.getValue()){
				if(ec.getAccuracy() >= MIN_ACCURACY){
					classesToEvaluate.add(entry.getKey());
					break;
				}
			}
		}
	}
	
	
	private void evaluateDefault(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : defaultMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
							
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				defaultFractions.get(i).addNumber((double)count[i]/defaultMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					defaultMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			defaultFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		defaultPositionStats.add(positionStat);
		defaultAccStats.add(accStat);
		defaultAddInstancesCountStats.add(moreInstancesCountStat);
		defaultHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
		
	}
	
	private void evaluateOWLStandard(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : owlStandardMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				owlStandardFractions.get(i).addNumber((double)count[i]/owlStandardMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					owlStandardMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			owlStandardFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		owlStandardPositionStats.add(positionStat);
		owlStandardAccStats.add(accStat);
		owlStandardAddInstancesCountStats.add(moreInstancesCountStat);
		owlStandardHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateOWLFMeasure(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : owlFMeasureMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
							
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				owlFMeasureFractions.get(i).addNumber((double)count[i]/owlFMeasureMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					owlFMeasureMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			owlFMeasureFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		owlFMeasurePositionStats.add(positionStat);
		owlFMeasureAccStats.add(accStat);
		owlFMeasureAddInstancesCountStats.add(moreInstancesCountStat);
		owlFMeasureHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateOWLGenFMeasure(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : owlGenFMeasureMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				owlGenFMeasureFractions.get(i).addNumber((double)count[i]/owlGenFMeasureMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					owlGenFMeasureMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			owlGenFMeasureFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		owlGenFMeasurePositionStats.add(positionStat);
		owlGenFMeasureAccStats.add(accStat);
		owlGenFMeasureAddInstancesCountStats.add(moreInstancesCountStat);
		owlGenFMeasureHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateOWLJaccard(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : owlJaccardMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				owlJaccardFractions.get(i).addNumber((double)count[i]/owlJaccardMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					owlJaccardMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			owlJaccardFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		owlJaccardPositionStats.add(positionStat);
		owlJaccardAccStats.add(accStat);
		owlJaccardAddInstancesCountStats.add(moreInstancesCountStat);
		owlJaccardHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateOWLPredacc(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : owlPredaccMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				owlPredaccFractions.get(i).addNumber((double)count[i]/owlPredaccMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					owlPredaccMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			owlPredaccFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		owlPredaccPositionStats.add(positionStat);
		owlPredaccAccStats.add(accStat);
		owlPredaccAddInstancesCountStats.add(moreInstancesCountStat);
		owlPredaccHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateFastStandard(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : fastStandardMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				fastStandardFractions.get(i).addNumber((double)count[i]/fastStandardMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					fastStandardMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			fastStandardFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		fastStandardPositionStats.add(positionStat);
		fastStandardAccStats.add(accStat);
		fastStandardAddInstancesCountStats.add(moreInstancesCountStat);
		fastStandardHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateFastFMeasure(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : fastFMeasureMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if((best == 1 || best == 2) && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){System.out.println("CLASS: " + nc);System.out.println(ec);System.out.println(fastFMeasureMap.get(nc));
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				fastFMeasureFractions.get(i).addNumber((double)count[i]/fastFMeasureMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					fastFMeasureMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			fastFMeasureFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		fastFMeasurePositionStats.add(positionStat);
		fastFMeasureAccStats.add(accStat);
		fastFMeasureAddInstancesCountStats.add(moreInstancesCountStat);
		fastFMeasureHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateFastGenFMeasure(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : fastGenFMeasureMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				fastGenFMeasureFractions.get(i).addNumber((double)count[i]/fastGenFMeasureMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					fastGenFMeasureMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			fastGenFMeasureFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		fastGenFMeasurePositionStats.add(positionStat);
		fastGenFMeasureAccStats.add(accStat);
		fastGenFMeasureAddInstancesCountStats.add(moreInstancesCountStat);
		fastGenFMeasureHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateFastJaccard(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : fastJaccardMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				fastJaccardFractions.get(i).addNumber((double)count[i]/fastJaccardMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					fastJaccardMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			fastJaccardFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		fastJaccardPositionStats.add(positionStat);
		fastJaccardAccStats.add(accStat);
		fastJaccardAddInstancesCountStats.add(moreInstancesCountStat);
		fastJaccardHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	
	private void evaluateFastPredacc(){
		Map<EvaluatedDescriptionClass, Integer> input;
		resetBestCount();
		Stat positionStat = new Stat();
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat hiddenIncCountStat = new Stat();
		boolean first = true;
		for(NamedClass nc : classesToEvaluate){
			first = true;
			resetCount();
			input = userInputMap.get(nc);
			for(EvaluatedDescriptionClass ec : fastPredaccMap.get(nc)){
				for(EvaluatedDescriptionClass ec2 : input.keySet()){
					if(c.compare(ec.getDescription(), ec2.getDescription()) == 0){
						count[input.get(ec2) - 1]++;
						if(best > input.get(ec2)){
							best = input.get(ec2);
						}
						if(best == 1 && first){
							first = false;
							positionStat.addNumber(defaultMap.get(nc).indexOf(ec));
							moreInstancesCountStat.addNumber(ec.getAdditionalInstances().size());
							if(!ec.isConsistent()){
								hiddenIncCountStat.addNumber(1);
							}
						}
						if(input.get(ec2) == 1){
							accStat.addNumber(ec.getAccuracy());
						}
					}
				}
			}
			for(int i = 0; i < 6; i++){
				fastPredaccFractions.get(i).addNumber((double)count[i]/fastPredaccMap.get(nc).size());
			}
			bestCount[best - 1]++;
			if(best != 1){
				if(isImprovementSelected(nc)){
					fastPredaccMissedImprovementsCount++;
				}
			}
			
		}
		for(int i = 0; i < 6; i++){
			fastPredaccFractionsBest.get(i).addNumber((double)bestCount[i]/classesToEvaluate.size());
		}
		fastPredaccPositionStats.add(positionStat);
		fastPredaccAccStats.add(accStat);
		fastPredaccAddInstancesCountStats.add(moreInstancesCountStat);
		fastPredaccHiddenInconsistenciesCountStats.add(hiddenIncCountStat);
	}
	

	private void loadOntology(File file) {
		String ontologyPath = file.toURI().toString().substring(0, file.toURI().toString().lastIndexOf('.')) + ".owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		factory = man.getOWLDataFactory();
//		ComponentManager cm = ComponentManager.getInstance();
//		try {
//			OWLFile ks = cm.knowledgeSource(OWLFile.class);
//			ks.getConfigurator().setUrl(new URL(ontologyPath));
//			ks.init();
//			rc = cm.reasoner(FastInstanceChecker.class, ks);
//			rc.init();
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
//		} catch (ComponentInitException e1) {
//			e1.printStackTrace();
//		}
		try {
			ont = man.loadOntologyFromOntologyDocument(IRI.create(ontologyPath));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Loaded ontology " + ont.getOntologyID());
	}
	
	private void resetCount(){
		for(int i = 0; i < 6; i++){
			count[i] = 0;
		}
		best = 6;
	}
	
	private void resetBestCount(){
		for(int i = 0; i < 6; i++){
			bestCount[i] = 0;
		}
		best = 6;
	}



	private void beginStatsTable() {
		latexStats = new StringBuilder();
		latexStats.append("\\begin{tabular}{ c | c | c | c | c | c | c | c | c } \n");
		latexStats.append("\\rotatebox{90}{\\#logical axioms} & ");
		latexStats.append("\\rotatebox{90}{\\#suggestions lists} & ");
		latexStats.append("\\rotatebox{90}{accept in \\%} & ");
		latexStats.append("\\rotatebox{90}{reject in \\%} & ");
		latexStats.append("\\rotatebox{90}{fail in \\%} & ");
		latexStats.append("\\rotatebox{90}{selected positions} \\rotatebox{90}{on suggestion list} \\rotatebox{90}{incl. std. deviation} & ");
		latexStats.append("\\rotatebox{90}{avg. accuracy of} \\rotatebox{90}{selected suggestions in \\%} & ");
		latexStats.append("\\rotatebox{90}{add. instances} \\rotatebox{90}{(equivalence only)} & ");
		latexStats.append("\\rotatebox{90}{add. instances total}");
		latexStats.append(" \\\\\n");
		latexStats.append("\\hline\n");
	}
	
	private void beginOntologyMetricsTable(){
		latexMetrics = new StringBuilder();
		latexMetrics.append("\\begin{tabular}{| l | c | c | c | c | c | c  } \n");
		latexMetrics.append("Ontology ID & ");
		latexMetrics.append("\\rotatebox{90}{\\#logical axioms} & ");
		latexMetrics.append("\\rotatebox{90}{\\#classes} & ");
		latexMetrics.append("\\rotatebox{90}{\\#object properties} & ");
		latexMetrics.append("\\rotatebox{90}{\\#data properties} & ");
		latexMetrics.append("\\rotatebox{90}{\\#individuals} & ");
		latexMetrics.append("DL expressivity");
		latexMetrics.append(" \\\\\n");
		latexMetrics.append("\\hline\n");
	}
	
	
	private void addOntologyMetricsTableRow(){
		int logicalAxiomsCount = ont.getLogicalAxiomCount();
		int classesCount = ont.getClassesInSignature(true).size();
		int objectPropertiesCount = ont.getObjectPropertiesInSignature(true).size();
		int dataPropertiesCount = ont.getDataPropertiesInSignature(true).size();
		int individualsCount = ont.getIndividualsInSignature(true).size();
		String expressivity = new DLExpressivityChecker(Collections.singleton(ont)).getDescriptionLogicName();
		latexMetrics.append(
				ont.getOntologyID() + " & "
				+ logicalAxiomsCount + " & " 
				+ classesCount + " & " 
				+ objectPropertiesCount + " & " 
				+ dataPropertiesCount + " & " 
				+ individualsCount + " & "
				+ "$\\mathcal{" + expressivity + "}$" 
				+ "\\\\\n");
		
	}
	
	private void endTables() {
		latexMetrics.append("\\hline\n");
		latexMetrics.append("\\end{tabular}");
		latexStats.append("\\hline\n");
		latexStats.append("\\end{tabular}");
	}


	/**
	 * Loads the user input evaluated in the EvaluationGUI.
	 * @param input
	 */
	@SuppressWarnings("unchecked")
	private void loadUserInput(File input) {
		InputStream fis = null;

		try {
			fis = new FileInputStream(input);
			ObjectInputStream o = new ObjectInputStream(fis);

			userInputMap = (Hashtable<OWLClass, Map<EvaluatedDescriptionClass, Integer>>) o.readObject();
//			for(Entry<OWLClass, Map<EvaluatedDescriptionClass, Integer>> entry : userInputMap.entrySet()){
//				System.out.println(entry.getKey());
//				for(Entry<EvaluatedDescriptionClass, Integer> e : entry.getValue().entrySet()){
//					System.out.println(e.getKey() + " --> " + e.getValue());
//				}
//			}
			System.out.println("Loaded user input file " + input);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadLearnResults(File file){
		
		InputStream fis = null;

		try {
			fis = new FileInputStream(file);
			ObjectInputStream o = new ObjectInputStream(fis);

			owlStandardMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlFMeasureMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlPredaccMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlJaccardMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlGenFMeasureMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
			fastStandardMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastFMeasureMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastPredaccMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastJaccardMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastGenFMeasureMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();

			defaultMap = (HashMap<OWLClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		
	}
	
	private void computeFleissKappa(File directory){
		System.out.println("Computing Fleiss Kappa...");
		Map<OWLClass, Map<EvaluatedDescriptionClass, Map<Integer, Integer>>> map = new HashMap<OWLClass, Map<EvaluatedDescriptionClass,Map<Integer,Integer>>>();
		Map<EvaluatedDescriptionClass, Map<Integer, Integer>> m;
		for (File suggestionFile : directory.listFiles(new ResultFileFilter())) {System.out.println(suggestionFile);
			loadLearnResults(suggestionFile);
			computeClassesToEvaluate();
			for (File inputFile : directory.listFiles(new NameFilter(suggestionFile))) {
				loadUserInput(inputFile);
				for(Entry<OWLClass, Map<EvaluatedDescriptionClass, Integer>> e : userInputMap.entrySet()){
					if(!classesToEvaluate.contains(e.getKey())){
						continue;
					}
					m = new HashMap<EvaluatedDescriptionClass, Map<Integer,Integer>>();
					for(EvaluatedDescriptionClass ec : e.getValue().keySet()){
						m.put(ec, new HashMap<Integer, Integer>());
					}
					map.put(e.getKey(), m);
				}
				break;
			}
//			break;
			
		}
		for (File suggestionFile : directory.listFiles(new ResultFileFilter())) {
			loadLearnResults(suggestionFile);
			computeClassesToEvaluate();
			for (File inputFile : directory.listFiles(new NameFilter(suggestionFile))) {
				loadUserInput(inputFile);
				for(Entry<OWLClass, Map<EvaluatedDescriptionClass, Integer>> e : userInputMap.entrySet()){
					if(!classesToEvaluate.contains(e.getKey())){
						continue;
					}
					for(Entry<EvaluatedDescriptionClass, Integer> e2 : e.getValue().entrySet()){
						m = map.get(e.getKey());
						Integer i = map.get(e.getKey()).get(e2.getKey()).get(e2.getValue());
						if(i == null){
							i = Integer.valueOf(0);
							
						}
						i++;
						map.get(e.getKey()).get(e2.getKey()).put(e2.getValue(), i);
					}
					
					
				}
//				break;
			}
//			break;
			
		}
		int cnt = 0;
		for(Map<EvaluatedDescriptionClass, Map<Integer, Integer>> m1 : map.values()){
			cnt += m1.keySet().size();
		}
		mat = new short[cnt][6];
		int i = 0;
		for(Map<EvaluatedDescriptionClass, Map<Integer, Integer>> m1 : map.values()){
			for(Map<Integer, Integer> m2 : m1.values()){
				for(Entry<Integer, Integer> entry : m2.entrySet()){
					mat[i][entry.getKey()-1] = entry.getValue().shortValue();
				}
				i++;
//				if(i == 10)break;
			}
//			if(i == 10)break;
		}
		System.out.println("MATRIX" + mat.length);
//		float kappa = FleissKappa.computeKappa(mat);
		for(int j = 0; j < mat.length; j++){
			System.out.println(Arrays.toString(mat[j]));
		}
		float raw = RawAgreement.computeRawAgreement(mat);
		System.out.println("Raw Agreement: "+ raw);
	}
	

	private void printStatsTable() {
		System.out.println(latexStats.toString());
	}
	
	private void printOntologyMetricsTable() {
		System.out.println(latexMetrics.toString());
	}

	private void printLatexCode() {
		printOntologyMetricsTable();
		printStatsTable();
	}

	/**
	 * @param args
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		Locale.setDefault(Locale.ENGLISH);
		Logger.getRootLogger().setLevel(Level.DEBUG);
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
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

	public NameFilter(File file) {
		this.file = file;
	}

	@Override
	public boolean accept(File dir, String name) {
		if (name.endsWith("inp")) {
			if (name.substring(0, name.indexOf('.')).startsWith(
					file.getName().substring(0, file.getName().indexOf('.')))) {
				return true;
			}
		}
		return false;
	}
}
