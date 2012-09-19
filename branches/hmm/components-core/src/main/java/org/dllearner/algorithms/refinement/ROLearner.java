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

package org.dllearner.algorithms.refinement;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.CommonConfigMappings;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.refinementoperators.RhoDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionPosNegComparator;

public class ROLearner extends AbstractCELA {
	
	private static Logger logger = Logger
	.getLogger(AbstractCELA.class);	
	
	private String logLevel = CommonConfigOptions.logLevelDefault;
	
	public enum Heuristic {	LEXICOGRAPHIC, FLEXIBLE	}
	
	// configuration options
	private boolean writeSearchTree;
	private File searchTreeFile;
	private boolean replaceSearchTree = false;
	private static String defaultSearchTreeFile = "log/searchTree.txt";
	private Heuristic heuristic = Heuristic.LEXICOGRAPHIC;
	Set<NamedClass> allowedConcepts;
	Set<ObjectProperty> allowedRoles;
	Set<NamedClass> ignoredConcepts;
	Set<ObjectProperty> ignoredRoles;
	// these are computed as the result of the previous four settings
	Set<NamedClass> usedConcepts;
	Set<ObjectProperty> usedRoles;	
	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;	
	private boolean useTooWeakList = true;
	private boolean useOverlyGeneralList = true;
	private boolean useShortConceptConstruction = true;
	private double horizontalExpansionFactor = 0.6;
	private boolean improveSubsumptionHierarchy = true;
	private boolean useAllConstructor = CommonConfigOptions.useAllConstructorDefault;
	private boolean useExistsConstructor = CommonConfigOptions.useExistsConstructorDefault;
	//this was added so you can switch algorithm without removing everything not applicable
	@SuppressWarnings("unused")
	private boolean useCardinalityRestrictions = CommonConfigOptions.useCardinalityRestrictionsDefault;
	private boolean useNegation = CommonConfigOptions.useNegationDefault;
	//TODO different standard options to CommonConfigOptions 
	private boolean useBooleanDatatypes = false;
	
	
	
	//extended Options
	private int maxExecutionTimeInSeconds = CommonConfigOptions.maxExecutionTimeInSecondsDefault;
	private boolean maxExecutionTimeShown = false;
	private int minExecutionTimeInSeconds = CommonConfigOptions.minExecutionTimeInSecondsDefault;
	private boolean minExecutionTimeShown = false;
	private int guaranteeXgoodDescriptions = CommonConfigOptions.guaranteeXgoodDescriptionsDefault;
	private boolean guaranteeXgoodShown = false;
	
	
	private boolean quiet = false;
	
	private boolean stop = false;
	private boolean isRunning = false;
	
	private Comparator<Node> nodeComparator;
	private NodeComparatorStable nodeComparatorStable = new NodeComparatorStable();
	private ConceptComparator conceptComparator = new ConceptComparator();
	// comparator for evaluated descriptions
	private EvaluatedDescriptionPosNegComparator edComparator = new EvaluatedDescriptionPosNegComparator();
	DecimalFormat df = new DecimalFormat();	
	
	private PosNegLP learningProblem;
	
	// Menge von Kandidaten für Refinement
	// (wird für Direktzugriff auf Baumknoten verwendet)
	private TreeSet<Node> candidates;
	// während eines Durchlaufs neu gefundene Knoten
	private List<Node> newCandidates = new LinkedList<Node>();
	// stabiles candidate set, da sich die Knoten nach dem einfügen nicht
	// verschieben können => das Set enthält nicht die aktuellen horizontal
	// expansions, es dient nur dazu die besten Konzepte zu speichern; hat also
	// keine Funktion im Algorithmus
	private TreeSet<Node> candidatesStable = new TreeSet<Node>(nodeComparatorStable);
	// vorhandene Konzepte, die irgendwann als proper eingestuft worden
	private SortedSet<Description> properRefinements = new TreeSet<Description>(conceptComparator);
	// speichert Konzept und deren Evaluierung, um sie leicht wiederzufinden für
	// Strategien wie Konzeptverkürzung etc.
	// Zahl = covered negatives, -1 = too weak
	// private Map<Concept, Integer> evaluationCache = new TreeMap<Concept, Integer>(conceptComparator);
	// Blacklists
	private SortedSet<Description> tooWeakList = new TreeSet<Description>(conceptComparator);
	private SortedSet<Description> overlyGeneralList = new TreeSet<Description>(conceptComparator);
	
	// Lösungen protokollieren
	boolean solutionFound = false;
	List<Description> solutions = new LinkedList<Description>();	
	
	// verwendeter Refinement-Operator (momentan werden für Statistik RhoDown-spezifische
	// Sachen abgefragt)
	// RefinementOperator operator;
	RhoDown operator;
	
	// Variablen zur Einstellung der Protokollierung
	// boolean quiet = false;
	boolean showBenchmarkInformation = false;
	// the previous best node (used only for logging, such that we can
	// detect whether a new best node has been found since the last time
	// statistics were printed)
	private Node previousBestNode;
	
	// record start node such that other applications can
	// get information about the search tree
	private Node startNode;
	
	// boolean createTreeString = false;
	// String searchTree = new String();
	TreeSet<Node> expandedNodes = new TreeSet<Node>(nodeComparatorStable);
	
	// Konfiguration des Algorithmus
	// Faktor für horizontale Erweiterung (notwendig für completeness)
	// double horizontalExpansionFactor = 0.6;	

	// statistische Variablen
	private int maxRecDepth = 0;
	private int maxNrOfRefinements = 0;
	private int maxNrOfChildren = 0;
	private int redundantConcepts = 0;
	int maximumHorizontalExpansion;
	int minimumHorizontalExpansion;
	// private int propernessTests = 0;
	private int propernessTestsReasoner = 0;
	private int propernessTestsAvoidedByShortConceptConstruction = 0;
	private int propernessTestsAvoidedByTooWeakList = 0;
	private int conceptTestsTooWeakList = 0;
	private int conceptTestsOverlyGeneralList = 0;
	private int conceptTestsReasoner = 0;
	
	// Zeitvariablen
	private long runtime;
	private long algorithmStartTime;
	private long propernessCalcTimeNs = 0;
	private long propernessCalcReasoningTimeNs = 0;	
	private long childConceptsDeletionTimeNs = 0;
	private long refinementCalcTimeNs = 0;
	private long redundancyCheckTimeNs = 0;
	private long evaluateSetCreationTimeNs = 0;
	private long improperConceptsRemovalTimeNs = 0;
	long someTimeNs = 0;
	int someCount = 0;
	
	// prefixes
	private String baseURI;

	public ROLearner(PosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
		this.learningProblem = learningProblem;
		baseURI = reasoningService.getBaseURI();
		
	}
	
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(PosNegLP.class);
		return problems;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new BooleanConfigOption("writeSearchTree", "specifies whether to write a search tree", false));
		options.add(new StringConfigOption("searchTreeFile","file to use for the search tree", defaultSearchTreeFile));
		options.add(new BooleanConfigOption("replaceSearchTree","specifies whether to replace the search tree in the log file after each run or append the new search tree", false));
		StringConfigOption heuristicOption = new StringConfigOption("heuristic", "specifiy the heuristic to use", "lexicographic");
		heuristicOption.setAllowedValues(new String[] {"lexicographic", "flexible"});
		options.add(heuristicOption);
		options.add(new BooleanConfigOption("applyAllFilter", "usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)", true));
		options.add(new BooleanConfigOption("applyExistsFilter", "usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)", true));
		options.add(new BooleanConfigOption("useTooWeakList", "try to filter out too weak concepts without sending them to the reasoner", true));
		options.add(new BooleanConfigOption("useOverlyGeneralList", "try to find overly general concept without sending them to the reasoner", true));
		options.add(new BooleanConfigOption("useShortConceptConstruction", "shorten concept to see whether they already exist", true));
		DoubleConfigOption horizExp = new DoubleConfigOption("horizontalExpansionFactor", "horizontal expansion factor (see publication for description)", 0.6);
		horizExp.setLowerLimit(0.0);
		horizExp.setUpperLimit(1.0);
		options.add(horizExp);
		options.add(new BooleanConfigOption("improveSubsumptionHierarchy", "simplify subsumption hierarchy to reduce search space (see publication for description)", true));
		// TODO: replace by a general verbosity option for all components
		options.add(new BooleanConfigOption("quiet", "may be deprecated soon", false));
		// allowed/ignored concepts/roles could also be a reasoner option (?)
		options.add(CommonConfigOptions.allowedConcepts());
		options.add(CommonConfigOptions.ignoredConcepts());
		options.add(CommonConfigOptions.allowedRoles());
		options.add(CommonConfigOptions.ignoredRoles());
		options.add(CommonConfigOptions.useAllConstructor());
		options.add(CommonConfigOptions.useExistsConstructor());
		options.add(CommonConfigOptions.useNegation());	
		options.add(CommonConfigOptions.useCardinalityRestrictions());	
		options.add(CommonConfigOptions.useBooleanDatatypes());
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds());
		options.add(CommonConfigOptions.minExecutionTimeInSeconds());
		options.add(CommonConfigOptions.guaranteeXgoodDescriptions());
		options.add(CommonConfigOptions.getLogLevel());
		options.add(CommonConfigOptions.getInstanceBasedDisjoints());
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings({"unchecked"})
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("writeSearchTree"))
			writeSearchTree = (Boolean) entry.getValue();
		else if(name.equals("searchTreeFile"))
			searchTreeFile = new File((String)entry.getValue());
		else if(name.equals("replaceSearchTree"))
			replaceSearchTree = (Boolean) entry.getValue();
		else if(name.equals("heuristic")) {
			String value = (String) entry.getValue();
			if(value.equals("lexicographic"))
				heuristic = Heuristic.LEXICOGRAPHIC;
			else
				heuristic = Heuristic.FLEXIBLE;
		} else if(name.equals("allowedConcepts")) {
			allowedConcepts = CommonConfigMappings.getAtomicConceptSet((Set<String>)entry.getValue());
		} else if(name.equals("allowedRoles")) {
			allowedRoles = CommonConfigMappings.getAtomicRoleSet((Set<String>)entry.getValue());
		} else if(name.equals("ignoredConcepts")) {
			ignoredConcepts = CommonConfigMappings.getAtomicConceptSet((Set<String>)entry.getValue());
		} else if(name.equals("ignoredRoles")) {
			ignoredRoles = CommonConfigMappings.getAtomicRoleSet((Set<String>)entry.getValue());
		} else if(name.equals("applyAllFilter")) {
			applyAllFilter = (Boolean) entry.getValue();
		} else if(name.equals("applyExistsFilter")) {
			applyExistsFilter = (Boolean) entry.getValue();
		} else if(name.equals("useTooWeakList")) {
			useTooWeakList = (Boolean) entry.getValue();
		} else if(name.equals("useOverlyGeneralList")) {
			useOverlyGeneralList = (Boolean) entry.getValue();
		} else if(name.equals("useShortConceptConstruction")) {
			useShortConceptConstruction = (Boolean) entry.getValue();
		} else if(name.equals("horzontalExpansionFactor")) {
			horizontalExpansionFactor = (Double) entry.getValue();
		} else if(name.equals("improveSubsumptionHierarchy")) {
			improveSubsumptionHierarchy = (Boolean) entry.getValue();
		} else if(name.equals("useAllConstructor")) {
			useAllConstructor = (Boolean) entry.getValue();
		} else if(name.equals("useExistsConstructor")) {
			useExistsConstructor = (Boolean) entry.getValue();
		}else if(name.equals("useCardinalityRestrictions")) {
				useCardinalityRestrictions = (Boolean) entry.getValue();
		} else if(name.equals("useNegation")) {
			useNegation = (Boolean) entry.getValue();
		} else if(name.equals("useBooleanDatatypes")) {
			useBooleanDatatypes = (Boolean) entry.getValue();
		}else if(name.equals("maxExecutionTimeInSeconds")) {
			maxExecutionTimeInSeconds = (Integer) entry.getValue();
		}else if(name.equals("minExecutionTimeInSeconds")) {
			minExecutionTimeInSeconds = (Integer) entry.getValue();
		}else if(name.equals("guaranteeXgoodDescriptions")) {
			guaranteeXgoodDescriptions =  (Integer) entry.getValue();
		} else if(name.equals("logLevel")) {
			logLevel = ((String)entry.getValue()).toUpperCase();
		}
			
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// set log level if the option has been set
		if(!logLevel.equals(CommonConfigOptions.logLevelDefault))		
			logger.setLevel(Level.toLevel(logLevel,Level.toLevel(CommonConfigOptions.logLevelDefault)));
		
		if(searchTreeFile == null)
			searchTreeFile = new File(defaultSearchTreeFile);

		if(writeSearchTree)
			Files.clearFile(searchTreeFile);
		
		// adjust heuristic
		if(heuristic == Heuristic.LEXICOGRAPHIC) {
			nodeComparator = new NodeComparator();
		} else {
			nodeComparator = new NodeComparator2(learningProblem.getNegativeExamples().size(), learningProblem.getPercentPerLengthUnit());
		}
		
		// this.learningProblem2 = learningProblem2;
		operator = new RhoDown(reasoner, applyAllFilter, applyExistsFilter, useAllConstructor, useExistsConstructor, useNegation, useBooleanDatatypes);
		
		// candidate sets entsprechend der gewählten Heuristik initialisieren
		candidates = new TreeSet<Node>(nodeComparator);
		// newCandidates = new TreeSet<Node>(nodeComparator);
		
		if(allowedConcepts != null) {
			// sanity check to control if no non-existing concepts are in the list
			Helper.checkConcepts(reasoner, allowedConcepts);
			usedConcepts = allowedConcepts;
		} else if(ignoredConcepts != null) {
			usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
		} else {
			usedConcepts = Helper.computeConcepts(reasoner);
		}
		
		if(allowedRoles != null) {
			Helper.checkRoles(reasoner, allowedRoles);
			usedRoles = allowedRoles;
		} else if(ignoredRoles != null) {
			Helper.checkRoles(reasoner, ignoredRoles);
			usedRoles = Helper.difference(reasoner.getObjectProperties(), ignoredRoles);
		} else {
			usedRoles = reasoner.getObjectProperties();
		}
		
		// prepare subsumption and role hierarchies, because they are needed
		// during the run of the algorithm
//		reasoner.prepareSubsumptionHierarchy(usedConcepts);
		if(improveSubsumptionHierarchy)
			reasoner.getClassHierarchy().thinOutSubsumptionHierarchy();
//		reasoner.prepareRoleHierarchy(usedRoles);
	}
	
	public static String getName() {
		return "refinement operator based learning algorithm";
	}
	
	private int coveredNegativesOrTooWeak(Description concept) {
		return learningProblem.coveredNegativeExamplesOrTooWeak(concept);
	}
	
	private int getNumberOfNegatives() {
		return learningProblem.getNegativeExamples().size();
	}
	
	// Kernalgorithmus
	@Override
	public void start() {
		isRunning = true;
		runtime=System.currentTimeMillis();
		// Suche wird mit Top-Konzept gestartet
		Thing top = new Thing();
		Node topNode = new Node(top);
		// int coveredNegativeExamples = learningProblem.coveredNegativeExamplesOrTooWeak(top);
		// aus Top folgen immer alle negativen Beispiele, d.h. es ist nur eine Lösung, wenn
		// es keine negativen Beispiele gibt
		int coveredNegativeExamples = getNumberOfNegatives();
		topNode.setCoveredNegativeExamples(coveredNegativeExamples);
		// topNode.setHorizontalExpansion(1); // die 0 ist eigentlich richtig, da keine Refinements
		// der Länge 1 untersucht wurden
		candidates.add(topNode);
		candidatesStable.add(topNode);
		startNode = topNode;
		// Abbruchvariable => beachten, dass bereits TOP eine Lösung sein kann
		solutionFound = (coveredNegativeExamples == 0);
		solutions = new LinkedList<Description>();
		if(solutionFound)
			solutions.add(top);
		
		int loop = 0;

		// Voreinstellung für horizontal expansion
		maximumHorizontalExpansion = 0;
		minimumHorizontalExpansion = 0;		
		
		algorithmStartTime = System.nanoTime();
		
		//second set of lines below, sometimes doesn't go into while, see above
		handleStoppingConditions();
		
		// TODO: effizienter Traversal der Subsumption-Hierarchie
		// TODO: Äquivalenzen nutzen
		// TODO: Gibt es auch eine andere Abbruchbedingung? Es könnte sein, dass irgendwann keine
		// proper refinements mehr gefunden werden, aber wie stelle man das fest?
		while(!solutionFound && !stop) {
			
			if(!quiet) 
				printStatistics(false);			
			
			// besten Knoten nach Heuristik auswählen
			Node bestNode = candidates.last();
			// besten Knoten erweitern			
			// newCandidates = new TreeSet<Node>(nodeComparator);	
			newCandidates.clear();
			candidates.remove(bestNode);
			extendNodeProper(bestNode, bestNode.getHorizontalExpansion()+1);
			candidates.add(bestNode);
			candidates.addAll(newCandidates);
			candidatesStable.addAll(newCandidates);			
			
			// minimum horizontal expansion berechnen
			if(bestNode.getHorizontalExpansion()>maximumHorizontalExpansion)
				maximumHorizontalExpansion = bestNode.getHorizontalExpansion();
			minimumHorizontalExpansion = (int) Math.floor(horizontalExpansionFactor*maximumHorizontalExpansion);
			
			// neu: es werden solange Knoten erweitert bis wirklich jeder Knoten die
			// notwendige minimum horizontal expansion hat
			boolean nodesExpanded;
			do {
				nodesExpanded = false;
				

				// es darf nicht candidatesStable geklont werden, da diese Menge nicht
				// aktualisiert wird, also die falschen horizontal expansions vorliegen
				// TODO: bei Tests war die Performance der clone-Operation ganz gut, aber
				// es skaliert natürlich nicht so gut mit größer werdenden candidate set
				// => Lösung ist vielleicht einfach einen Iterator zu verwenden und das
				// aktuelle Konzept gleich hier zu löschen (wird dann bei expansion wieder
				// hinzugefügt)
				// TreeSet<Node> candidatesClone = (TreeSet<Node>) candidates.clone();
				newCandidates.clear();


				// for(Node candidate : candidatesClone) {
				Iterator<Node> it = candidates.iterator();
				List<Node> changedNodes = new LinkedList<Node>();
				 while(it.hasNext()){
					Node candidate = it.next();
					// alle Kandidaten, die nicht too weak sind und unter minimumHorizontalExpansion
					// liegen, werden erweitert
					if(!candidate.isTooWeak() && candidate.getHorizontalExpansion()<minimumHorizontalExpansion) {
						// Vorsicht, falls candidates irgendwann in extendProper benutzt
						// werden sollten! Es könnten auf diese Weise Knoten fehlen 
						// (momentan wird candidates nur zur Auswahl des besten Knotens
						// benutzt).
						it.remove();
						
						extendNodeProper(candidate, minimumHorizontalExpansion);
						nodesExpanded = true;

						changedNodes.add(candidate);
					}
				}
				 
				long someTimeStart = System.nanoTime();
				someCount++;				 
				// geänderte temporär entfernte Knoten wieder hinzufügen
				candidates.addAll(changedNodes);
				// neu gefundene Knoten hinzufügen
				candidates.addAll(newCandidates);
				candidatesStable.addAll(newCandidates);
				someTimeNs += System.nanoTime() - someTimeStart;

			} while(nodesExpanded && !stop);
			
			//System.out.println("candidate set:");
			//for(Node n : candidates) {
			//	System.out.println(n);
			//}
			
			if(writeSearchTree) {
				// String treeString = "";
				String treeString = "best expanded node: " + bestNode+ "\n";
				if(expandedNodes.size()>1) {
					treeString += "all expanded nodes:\n"; // due to minimum horizontal expansion:\n";
					for(Node n : expandedNodes) {
						treeString += "   " + n + "\n";
					}
				}
				expandedNodes.clear();
				treeString += "horizontal expansion: " + minimumHorizontalExpansion + " to " + maximumHorizontalExpansion + "\n";
				treeString += topNode.getTreeString();
				treeString += "\n";
				// System.out.println(treeString);
				// searchTree += treeString + "\n";
				// TODO: ev. immer nur einen search tree speichern und den an die
				// Datei anhängen => spart Speicher
				if(replaceSearchTree)
					Files.createFile(searchTreeFile, treeString);
				else
					Files.appendToFile(searchTreeFile, treeString);
			}//write search tree
			
			// Anzahl Schleifendurchläufe
			loop++;
			
			if(!quiet)
				logger.debug("--- loop " + loop + " finished ---");	
			
			handleStoppingConditions();
			
		}//end while
		
		
		
		
		// Suchbaum in Datei schreiben
//		if(writeSearchTree)
//			Files.createFile(searchTreeFile, searchTree);
		
		// Ergebnisausgabe
		/*
		System.out.println("candidate set:");
		for(Node n : candidates) {
			System.out.println(n);
		}*/
		
		// Set<Concept> solutionsSorted = new TreeSet(conceptComparator);
		// solutionsSorted.addAll(solutions);
		
		// System.out.println("retrievals:");
		// for(Concept c : ReasonerComponent.retrievals) {
		// 	System.out.println(c);
		// }
		
		if(solutionFound) {
			logger.info("\nsolutions:");
			int show=1;
			for(Description c : solutions) {
				logger.info(show+": " +c.toString(baseURI,null) + " (length " + c.getLength() +", depth " + c.getDepth() + ")");
				//TODO remove this line maybe
				// watch for String.replace Quick hack
				logger.info("   MANCHESTER: " + 
						c.toManchesterSyntaxString(baseURI, new HashMap<String,String>()).
						replace("\"", ""));
				logger.info("   KBSyntax: " + c.toKBSyntaxString());
				if(show>=5){break;}
				show++;
			}
			
		}
		logger.info("  horizontal expansion: " + minimumHorizontalExpansion + " to " + maximumHorizontalExpansion);
		logger.info("  size of candidate set: " + candidates.size());
		
		//logger.trace("test");
		//logger.trace(solutions.size());
		printBestSolutions(0);
		printStatistics(true);
		
		if(stop)
			logger.info("Algorithm stopped.");
		else
			logger.info("Algorithm terminated succesfully.");
		
		isRunning = false;
	}
	
	// einfache Erweiterung des Knotens (ohne properness)
	@SuppressWarnings({"unused"})
	private void extendNodeSimple(Node node, int maxLength) {
		
	}
	
	private void extendNodeProper(Node node, int maxLength) {
		// Rekursionsanfang ist das Konzept am Knoten selbst; danach wird der Operator
		// so lange darauf angewandt bis alle proper refinements bis zu maxLength
		// gefunden wurden
		long propCalcNsStart = System.nanoTime();
		
		if(writeSearchTree)
			expandedNodes.add(node);
		
		if(node.getChildren().size()>maxNrOfChildren)
			maxNrOfChildren = node.getChildren().size();
		
		// Knoten in instabiler Menge muss aktualisiert werden
		// => wird jetzt schon vom Algorithmus entfernt
		/*
		boolean remove = candidates.remove(node);
		
		if(!remove) {
			System.out.println(candidates);
			System.out.println(candidatesStable);
			System.out.println(node);
			
			throw new RuntimeException("remove failed");
		}*/
		
		extendNodeProper(node, node.getConcept(), maxLength, 0);
		node.setHorizontalExpansion(maxLength);
		
		// wird jetzt schon im Kernalgorithmus hinzugefügt
		/*
		boolean add = candidates.add(node);
		if(!add) {
			throw new RuntimeException("add failed");
		}*/
		
		// Knoten wird entfernt und wieder hinzugefügt, da sich seine
		// Position geändert haben könnte => geht noch nicht wg. ConcurrentModification
		// falls Knoten wg. min. horiz. exp. expandiert werden 
		// candidates.remove(node);
		// candidates.add(node);
		propernessCalcTimeNs += (System.nanoTime()-propCalcNsStart);
	}
	

	
	// für alle proper refinements von concept bis maxLength werden Kinderknoten
	// für node erzeugt;
	// recDepth dient nur zur Protokollierung
	private void extendNodeProper(Node node, Description concept, int maxLength, int recDepth) {
		
		// führe Methode nicht aus, wenn Algorithmus gestoppt wurde (alle rekursiven Funktionsaufrufe
		// werden nacheinander abgebrochen, so dass ohne weitere Reasoninganfragen relativ schnell beendet wird)
		if(stop)
			return;
		
		if(recDepth > maxRecDepth)
			maxRecDepth = recDepth;
		
		// Refinements berechnen => hier dürfen dürfen refinements <= horizontal expansion
		// des Konzepts nicht gelöscht werden!
		long refinementCalcTimeNsStart = System.nanoTime();
		Set<Description> refinements = operator.refine(concept, maxLength, null);
		refinementCalcTimeNs += System.nanoTime() - refinementCalcTimeNsStart;
		
		if(refinements.size()>maxNrOfRefinements)
			maxNrOfRefinements = refinements.size();
		
		long childConceptsDeletionTimeNsStart = System.nanoTime();
		// entferne aus den refinements alle Konzepte, die bereits Kinder des Knotens sind
		// for(Node n : node.getChildren()) {
		// 	refinements.remove(n.getConcept());
		// }
		
		// das ist viel schneller, allerdings bekommt man ein anderes candidate set(??)
		refinements.removeAll(node.getChildConcepts());

		childConceptsDeletionTimeNs += System.nanoTime() - childConceptsDeletionTimeNsStart;
		
		long evaluateSetCreationTimeNsStart = System.nanoTime();
		
		// alle Konzepte, die länger als horizontal expansion sind, müssen ausgewertet
		// werden
		Set<Description> toEvaluateConcepts = new TreeSet<Description>(conceptComparator);
		Iterator<Description> it = refinements.iterator();
		// for(Concept refinement : refinements) {
		while(it.hasNext()) {
			Description refinement = it.next();
			if(refinement.getLength()>node.getHorizontalExpansion()) {
				// TODO: an dieser Stelle könnte man Algorithmen ansetzen lassen, die
				// versuchen properness-Anfragen zu vermeiden:
				// 1. Konzept kürzen und schauen, ob es Mutterkonzept entspricht
				// 2. Blacklist, die überprüft, ob Konzept too weak ist
				// (dann ist es auch proper)
				
				// sagt aus, ob festgestellt wurde, ob refinement proper ist
				// (sagt nicht aus, dass das refinement proper ist!)
				boolean propernessDetected = false;
				
				// 1. short concept construction
				if(useShortConceptConstruction) {
					// kurzes Konzept konstruieren
					Description shortConcept = ConceptTransformation.getShortConcept(refinement, conceptComparator);
					int n = conceptComparator.compare(shortConcept, concept);
					
					// Konzepte sind gleich also Refinement improper
					if(n==0) {
						propernessTestsAvoidedByShortConceptConstruction++;
						propernessDetected = true;
					}
				}
				
				// 2. too weak test
				if(!propernessDetected && useTooWeakList) {
					if(refinement instanceof Intersection) {
						boolean tooWeakElement = containsTooWeakElement((Intersection)refinement);
						if(tooWeakElement) {
							propernessTestsAvoidedByTooWeakList++;
							conceptTestsTooWeakList++;
							propernessDetected = true;
							tooWeakList.add(refinement);
							
							// Knoten wird direkt erzeugt (es ist buganfällig zwei Plätze
							// zu haben, an denen Knoten erzeugt werden, aber es erscheint
							// hier am sinnvollsten)
							properRefinements.add(refinement);
							tooWeakList.add(refinement);
							
							Node newNode = new Node(refinement);
							newNode.setHorizontalExpansion(refinement.getLength()-1);
							newNode.setTooWeak(true);
							newNode.setQualityEvaluationMethod(Node.QualityEvaluationMethod.TOO_WEAK_LIST);
							node.addChild(newNode);
							
							// Refinement muss gelöscht werden, da es proper ist
							it.remove();
						}
					}
				}
				
				// properness konnte nicht vorher ermittelt werden
				if(!propernessDetected)
					toEvaluateConcepts.add(refinement);
				
					
			}
		}
		evaluateSetCreationTimeNs += System.nanoTime() - evaluateSetCreationTimeNsStart;
		
		// System.out.println(toEvaluateConcepts.size());
		
		Set<Description> improperConcepts = null;
		if(toEvaluateConcepts.size()>0) {
			// Test aller Konzepte auf properness (mit DIG in nur einer Anfrage)
			long propCalcReasoningStart = System.nanoTime();
			improperConcepts = reasoner.isSuperClassOf(toEvaluateConcepts, concept);
			propernessTestsReasoner+=toEvaluateConcepts.size();
			// boolean isProper = !learningProblem.getReasonerComponent().subsumes(refinement, concept);
			propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart;
		}

		long improperConceptsRemovalTimeNsStart = System.nanoTime();
		// die improper Konzepte werden von den auszuwertenden gelöscht, d.h.
		// alle proper concepts bleiben übrig (einfache Umbenennung)
		if(improperConcepts != null)
			toEvaluateConcepts.removeAll(improperConcepts);
		Set<Description> properConcepts = toEvaluateConcepts;
		// alle proper concepts von refinements löschen
		refinements.removeAll(properConcepts);
		improperConceptsRemovalTimeNs += System.nanoTime() - improperConceptsRemovalTimeNsStart;
		
		for(Description refinement : properConcepts) {
			long redundancyCheckTimeNsStart = System.nanoTime();
			boolean nonRedundant = properRefinements.add(refinement);
			redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;
			
			if(!nonRedundant)
				redundantConcepts++;
			
			// es wird nur ein neuer Knoten erzeugt, falls das Konzept nicht
			// schon existiert
			if(nonRedundant) {
			
				// Knoten erzeugen
				Node newNode = new Node(refinement);
				// die -1 ist wichtig, da sonst keine gleich langen Refinements 
				// für den neuen Knoten erlaubt wären z.B. person => male
				newNode.setHorizontalExpansion(refinement.getLength()-1);

				
				// hier finden Tests statt, die Retrieval-Anfrage vermeiden sollen
				/*
				Integer n = evaluationCache.get(concept);
				// Konzept gefunden
				if(n!=null) {
					// Knoten erzeugen
					Node newNode = new Node(refinement);
					newNode.setHorizontalExpansion(refinement.getLength()-1);
					node.addChild(newNode);
					
					// too weak
					if(n==-1) {
						newNode.setTooWeak(true);
					// nicht too weak
					} else {
						// feststellen, ob proper => geht so nicht
						// gleiche covered negatives bedeutet nicht improper
						boolean proper = (n==node.getCoveredNegativeExamples());
						newNode.setCoveredNegativeExamples(n);
						
					}
				// Konzept nicht gefunden => muss ausgewertet werden
				} else {
					toEvaluateConcepts.add(refinement);
				}
				*/
				
				boolean qualityKnown = false;
				int quality = -2;
				
				// overly general list verwenden
				if(useOverlyGeneralList && refinement instanceof Union) {
					if(containsOverlyGeneralElement((Union)refinement)) {
						conceptTestsOverlyGeneralList++;
						quality = getNumberOfNegatives();
						qualityKnown = true;
						newNode.setQualityEvaluationMethod(Node.QualityEvaluationMethod.OVERLY_GENERAL_LIST);
					}	
				}
				
				// Qualität des Knotens auswerten
				if(!qualityKnown) {
					long propCalcReasoningStart2 = System.nanoTime();
					conceptTestsReasoner++;
					quality = coveredNegativesOrTooWeak(refinement);
					propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart2;
					newNode.setQualityEvaluationMethod(Node.QualityEvaluationMethod.REASONER);
				}

				if(quality == -1) {
					newNode.setTooWeak(true);
					// Blacklist für too weak concepts
					tooWeakList.add(refinement);
				} else {
					// Lösung gefunden
					if(quality == 0) {
						solutionFound = true;
						solutions.add(refinement);
					}			
					
					newNode.setCoveredNegativeExamples(quality);
					newCandidates.add(newNode);
					// candidates.add(newNode);
					// candidatesStable.add(newNode);
				
					
					if(quality == getNumberOfNegatives())
						overlyGeneralList.add(refinement);
					
					// System.out.print(".");
				}
				
				node.addChild(newNode);
			}			
		}
		
		
		/*
		Iterator<Concept> it = refinements.iterator();
		while(it.hasNext()) {
			Concept refinement = it.next();
			if(refinement.getLength()>node.getHorizontalExpansion()) {
				// Test auf properness
				long propCalcReasoningStart = System.nanoTime();
				boolean isProper = !learningProblem.getReasonerComponent().subsumes(refinement, concept);
				propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart;
				
				if(isProper) {
					long redundancyCheckTimeNsStart = System.nanoTime();
					boolean nonRedundant = properRefinements.add(refinement);
					redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;
					
					if(!nonRedundant)
						redundantConcepts++;
					
					// es wird nur ein neuer Knoten erzeugt, falls das Konzept nicht
					// schon existiert
					if(nonRedundant) {
					
						// Knoten erzeugen
						Node newNode = new Node(refinement);
						// die -1 ist wichtig, da sonst keine gleich langen Refinements 
						// für den neuen Knoten erlaubt wären z.B. person => male
						newNode.setHorizontalExpansion(refinement.getLength()-1);
						node.addChild(newNode);
						
						// Qualität des Knotens auswerten
						long propCalcReasoningStart2 = System.nanoTime();
						int quality = learningProblem.coveredNegativeExamplesOrTooWeak(refinement);
						propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart2;
						
						if(quality == -1) {
							newNode.setTooWeak(true);
						} else {
							// Lösung gefunden
							if(quality == 0) {
								solutionFound = true;
								solutions.add(refinement);
							}			
							
							newNode.setCoveredNegativeExamples(quality);
							newCandidates.add(newNode);
							
							// System.out.print(".");
						}
					}
					
					// jedes proper Refinement wird gelöscht
					it.remove();

				}
			}
		}
		*/
		
		
		
		// es sind jetzt noch alle Konzepte übrig, die improper refinements sind
		// auf jedem dieser Konzepte wird die Funktion erneut aufgerufen, da sich
		// proper refinements ergeben könnten
		for(Description refinement : refinements) {
			// for(int i=0; i<=recDepth; i++)
			//	System.out.print("  ");
			// System.out.println("call: " + refinement + " [maxLength " + maxLength + "]");
			extendNodeProper(node, refinement, maxLength, recDepth+1);
			// for(int i=0; i<=recDepth; i++)
			//	System.out.print("  ");
			// System.out.println("finished: " + refinement + " [maxLength " + maxLength + "]");			
		}
	}
	
	private void printStatistics(boolean finalStats) {
		// TODO: viele Tests haben ergeben, dass man nie 100% mit der Zeitmessung abdecken
		// kann (zum einen weil Stringausgabe verzögert erfolgt und zum anderen weil 
		// Funktionsaufrufe, garbage collection, Zeitmessung selbst auch Zeit benötigt); 
		// es empfiehlt sich folgendes Vorgehen:
		// - Messung der Zeit eines Loops im Algorithmus
		// - Messung der Zeit für alle node extensions innerhalb eines Loops
		// => als Normalisierungsbasis empfehlen sich dann die Loopzeit statt
		// Algorithmuslaufzeit
		// ... momentan kann es aber auch erstmal so lassen

		long algorithmRuntime = System.nanoTime() - algorithmStartTime;
		
		if(!finalStats) {
			Node bestNode = candidatesStable.last();
			boolean newBestNodeFound = false;
			if(!bestNode.equals(previousBestNode)) {
				newBestNodeFound = true;
				previousBestNode = bestNode;
			}
			if(newBestNodeFound)
				logger.info("currently best node: " + bestNode);
			
			// Refinementoperator auf Konzept anwenden
			String bestNodeString = "currently best node: " + candidatesStable.last();
			// searchTree += bestNodeString + "\n";
			if(!newBestNodeFound)
				logger.debug(bestNodeString);
			String expandedNodeString = "next expanded node: " + candidates.last();
			// searchTree += expandedNodeString + "\n";
			logger.debug(expandedNodeString);		
			logger.debug("algorithm runtime " + Helper.prettyPrintNanoSeconds(algorithmRuntime));
			String expansionString = "horizontal expansion: " + minimumHorizontalExpansion + " to " + maximumHorizontalExpansion;
			// searchTree += expansionString + "\n";
			logger.debug(expansionString);
			logger.debug("size of candidate set: " + candidates.size());
			// System.out.println("properness max recursion depth: " + maxRecDepth);
			// System.out.println("max. number of one-step refinements: " + maxNrOfRefinements);
			// System.out.println("max. number of children of a node: " + maxNrOfChildren);
			logger.debug("subsumption time: " + Helper.prettyPrintNanoSeconds(reasoner.getSubsumptionReasoningTimeNs()));
			logger.debug("instance check time: " + Helper.prettyPrintNanoSeconds(reasoner.getInstanceCheckReasoningTimeNs()));
		}
		
		if(showBenchmarkInformation) {
			

			long reasoningTime = reasoner.getOverallReasoningTimeNs();
			double reasoningPercentage = 100 * reasoningTime/(double)algorithmRuntime;
			long propWithoutReasoning = propernessCalcTimeNs-propernessCalcReasoningTimeNs;
			double propPercentage = 100 * propWithoutReasoning/(double)algorithmRuntime;
			double deletionPercentage = 100 * childConceptsDeletionTimeNs/(double)algorithmRuntime;
			long subTime = reasoner.getSubsumptionReasoningTimeNs();
			double subPercentage = 100 * subTime/(double)algorithmRuntime;
			double refinementPercentage = 100 * refinementCalcTimeNs/(double)algorithmRuntime;
			double redundancyCheckPercentage = 100 * redundancyCheckTimeNs/(double)algorithmRuntime;
			double evaluateSetCreationTimePercentage = 100 * evaluateSetCreationTimeNs/(double)algorithmRuntime;
			double improperConceptsRemovalTimePercentage = 100 * improperConceptsRemovalTimeNs/(double)algorithmRuntime;
			double mComputationTimePercentage = 100 * operator.mComputationTimeNs/(double)algorithmRuntime;
			double topComputationTimePercentage = 100 * operator.topComputationTimeNs/(double)algorithmRuntime;
			double cleanTimePercentage = 100 * ConceptTransformation.cleaningTimeNs/(double)algorithmRuntime;
			double onnfTimePercentage = 100 * ConceptTransformation.onnfTimeNs/(double)algorithmRuntime;
			double shorteningTimePercentage = 100 * ConceptTransformation.shorteningTimeNs/(double)algorithmRuntime;
			
			// nur temporär 
			double someTimePercentage = 100 * someTimeNs/(double)algorithmRuntime;			
			
			System.out.println("reasoning percentage: " + df.format(reasoningPercentage) + "%");
			System.out.println("   subsumption check time: " + df.format(subPercentage) + "%");		
			System.out.println("proper calculation percentage (wo. reasoning): " + df.format(propPercentage) + "%");
			System.out.println("   deletion time percentage: " + df.format(deletionPercentage) + "%");
			System.out.println("   refinement calculation percentage: " + df.format(refinementPercentage) + "%");
			System.out.println("      some time percentage: " + df.format(someTimePercentage) + "% " + Helper.prettyPrintNanoSeconds(someTimeNs) + " " + someCount + " times");
			System.out.println("      m calculation percentage: " + df.format(mComputationTimePercentage) + "%");
			System.out.println("      top calculation percentage: " + df.format(topComputationTimePercentage) + "%");
			System.out.println("   redundancy check percentage: " + df.format(redundancyCheckPercentage) + "%");
			System.out.println("   evaluate set creation time percentage: " + df.format(evaluateSetCreationTimePercentage) + "%");
			System.out.println("   improper concepts removal time percentage: " + df.format(improperConceptsRemovalTimePercentage) + "%");
			System.out.println("clean time percentage: " + df.format(cleanTimePercentage) + "%");
			System.out.println("onnf time percentage: " + df.format(onnfTimePercentage) + "%");
			System.out.println("shortening time percentage: " + df.format(shorteningTimePercentage) + "%");			
		}
		logger.debug("properness tests (reasoner/short concept/too weak list): " + propernessTestsReasoner + "/" + propernessTestsAvoidedByShortConceptConstruction 
				+ "/" + propernessTestsAvoidedByTooWeakList);
		logger.debug("concept tests (reasoner/too weak list/overly general list/redundant concepts): " + conceptTestsReasoner + "/"
				+ conceptTestsTooWeakList + "/" + conceptTestsOverlyGeneralList + "/" + redundantConcepts);	
	}
	
	private boolean containsTooWeakElement(Intersection mc) {
		for(Description child : mc.getChildren()) {
			if(tooWeakList.contains(child))
				return true;
		}
		return false;
	}
	
	private boolean containsOverlyGeneralElement(Union md) {
		for(Description child : md.getChildren()) {
			if(overlyGeneralList.contains(child))
				return true;
		}
		return false;
	}	
	
	@Override
	public Description getCurrentlyBestDescription() {
		return candidatesStable.last().getConcept();
	}	
	
	@Override
	public EvaluatedDescriptionPosNeg getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescriptionPosNeg(candidatesStable.last().getConcept(), getSolutionScore());
	}
	
	@Override
	public TreeSet<EvaluatedDescriptionPosNeg> getCurrentlyBestEvaluatedDescriptions() {
		int count = 0;
		SortedSet<Node> rev = candidatesStable.descendingSet();
		TreeSet<EvaluatedDescriptionPosNeg> cbd = new TreeSet<EvaluatedDescriptionPosNeg>(edComparator);
		for(Node eb : rev) {
			cbd.add(new EvaluatedDescriptionPosNeg(eb.getConcept(), getSolutionScore(eb.getConcept())));
			// return a maximum of 200 elements (we need a maximum, because the
			// candidate set can be very large)
			if(count > 200)
				return cbd;
			count++;
		}
		return cbd; 
	}	
	
	public void printBestSolutions(int nrOfSolutions){
		if(!logLevel.equalsIgnoreCase("TRACE")){return;}
		if(nrOfSolutions==0)nrOfSolutions=solutions.size();
		int i=0;
		for(;i<nrOfSolutions; i++) {
			Description d = solutions.get(i);
			logger.trace("  " + d.toString(baseURI,null) + " (length " + d.getLength() + " " +
					"" );
		}
			
	}

	@Override
	public synchronized List<Description> getCurrentlyBestDescriptions(int nrOfSolutions) {
		List<Description> best = new LinkedList<Description>();
		int i=0;
		for(Node n : candidatesStable.descendingSet()) {
			best.add(n.getConcept());
			if(i==nrOfSolutions)
				return best;
			i++;
		}
		return best;
	}
	
//	@Override
	public ScorePosNeg getSolutionScore() {
		return (ScorePosNeg) learningProblem.computeScore(getCurrentlyBestDescription());
	}
	
	
	public ScorePosNeg getSolutionScore(Description d) {
		return (ScorePosNeg) learningProblem.computeScore(d);
	}

	@Override
	public void stop() {
		stop = true;
	}

	/**
	 * @return the startNode
	 */
	public Node getStartNode() {
		return startNode;
	}
	
	private void handleStoppingConditions(){
		solutionFound = (guaranteeXgoodDescriptions() );
		solutionFound = (minExecutionTimeReached()&& solutionFound);
		if(maxExecutionTimeReached()) { stop();
			if(solutions.size()>0)solutionFound = true;
		}
	}

	
	private boolean guaranteeXgoodDescriptions(){
		if(guaranteeXgoodShown)return true;
		if(solutions.size()>guaranteeXgoodDescriptions){
			logger.info("Minimum number ("+guaranteeXgoodDescriptions+") of good descriptions reached, stopping now...");
			guaranteeXgoodShown=true;
			return true;}
		else return false;
		
	}
	
	
	private boolean maxExecutionTimeReached(){
		if(maxExecutionTimeInSeconds==0)return false;
		if(maxExecutionTimeShown)return true;
		long needed = System.currentTimeMillis()- this.runtime;
		long maxMilliSeconds = maxExecutionTimeInSeconds *1000 ;
		if(maxMilliSeconds<needed){
			logger.info("Maximum time ("+maxExecutionTimeInSeconds+" seconds) reached, stopping now...");
			maxExecutionTimeShown=true;
			return true;}
		else return false;
		
	}
	
	

	/**
	 * true if minExecutionTime reached
	 * @return true
	 */
	private boolean minExecutionTimeReached(){
		if(minExecutionTimeShown)return true;
		long needed = System.currentTimeMillis()- this.runtime;
		long minMilliSeconds = minExecutionTimeInSeconds *1000 ;
		if(minMilliSeconds<needed){
			logger.info("Minimum time ("+minExecutionTimeInSeconds+" seconds) reached, stopping when next solution is found");
			minExecutionTimeShown=true;
			return true;}
		else return false;
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return isRunning;
	}

}
