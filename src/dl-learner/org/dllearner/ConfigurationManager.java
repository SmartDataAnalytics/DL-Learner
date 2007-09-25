package org.dllearner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.Config.Algorithm;
import org.dllearner.ScoreThreeValued.ScoreMethod;
import org.dllearner.algorithms.gp.GP.AlgorithmType;
import org.dllearner.algorithms.gp.GP.SelectionType;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.parser.DLLearner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.RoleComparator;

/**
 * Nach dem einlesen der Datei werden hier alle Konfigurationsoptionen
 * abgespeichert. Diese werden hier erst verwaltet und dann 
 * angewandt, indem die Werte der Variablen in Config umgeschrieben
 * werden (bzw. es werden Fehlermeldungen bei nicht existierenden,
 * inkorrekten oder inkompatiblen Einstellungen ausgegeben).
 * 
 * TODO: Eventuell kann man damit Config.java ersetzen? Das w�re etwas
 * sauberer, wenn man den DL-Learner als API sieht, der z.B. von
 * OntoWiki aus genutzt wird. Dann k�nnte man sicherstellen, dass
 * Konfigurationsoptionen nicht direkt ge�ndert werden, sondern
 * �ber Anmeldung von �nderungen beim Konfigurationsmanager.
 * 
 * TODO: noch checken, ob man die �berpr�fung der G�ltigkeit von
 * Konfigurationsoptionen nicht schon bei deren Erstellung machen sollte
 * 
 * @author jl
 *
 */
public class ConfigurationManager {

	private Collection<ConfigurationOption> options;
	
	// verfuegbare Optionen
	// Problemfall: double-Optionen, die auch int-Optionen sein können;
	// diese sind in beiden Mengen vertreten => man kann vereinbaren, dass
	// jede double-Option auch eine int-Option sein kann
	private Map<String, Integer[]> intOptions;
	private Map<String, Double[]> doubleOptions;
	private Map<String, String[]> strOptions;
	private List<String> setOptions;
	
	public ConfigurationManager() {
		this(new LinkedList<ConfigurationOption>());
	}
	
	public ConfigurationManager(Collection<ConfigurationOption> confOptions) {
		// options = new HashSet<ConfigurationOption>();
		options = confOptions;
		strOptions = new HashMap<String, String[]>();
		intOptions = new HashMap<String, Integer[]>();
		doubleOptions = new HashMap<String, Double[]>();
		setOptions = new LinkedList<String>();
		createIntOptions();
		createDoubleOptions();
		createStringOptions();
		createSetOptions();
	}
	
	public void applyOptions() {
		for(ConfigurationOption option : options) {
			// System.out.println(option);
			applyConfigurationOption(option);
		}
		
		// DIG-Reasoner-URL setzen, falls nicht schon geschehen (kann wegen Exception
		// nicht in Config gesetzt werden)
		if(Config.digReasonerURL == null) {
			try {
				Config.digReasonerURL = new URL("http://localhost:8081");
			} catch (MalformedURLException e) {
				// Exception tritt nie auf, da URL korrekt
				e.printStackTrace();
			}
		}
		
		// der parserinterne Namespace wird immer ausgeblendet
		Config.hidePrefixes.add(DLLearner.internalNamespace);
		
	}
	
	// ueberprueft, ob es Konflikte zwischen derzeit gesetzten Konfigurationsoptionen gibt
	public boolean checkConsistency() {
		return true;
	}
	
	// TODO: bei Fehlerbehandlungen müsste man jetzt noch berücksichtigen, dass
	// Mengen als 4. Optionstyp (neben int, double, string) dazugekommen sind
	public void applyConfigurationOption(ConfigurationOption option) {
		String optionString;
		if(option.containsSubOption())
			optionString = option.getOption() + "." + option.getSubOption();
		else
			optionString = option.getOption();
		
		if(option.isNumeric()) {
				
				boolean isInIntOptions = intOptions.containsKey(optionString);
				boolean isInDoubleOptions = doubleOptions.containsKey(optionString);
				
				// numerische Option existiert
				if(isInIntOptions || isInDoubleOptions) {
					
					// es ist eine Option, die nur int-Werte haben darf
					if(isInIntOptions) {
						if(!option.isIntegerOption())
							throw new Error("The argument of configuration option \""
									+ optionString + "\" has to be an integer.");
						
						// ueberpruefen, ob Wert innerhalb der vorgegebenen Schranken liegt
						int minValue = intOptions.get(optionString)[0];
						int maxValue = intOptions.get(optionString)[1];
						if (option.getIntValue() < minValue || option.getIntValue() > maxValue) {
							System.out.println("Error: The argument of configuration option \""
									+ optionString + "\" is out of range. It has to be between "
									+ minValue + " and " + maxValue + ".");
							System.exit(0);
						}
						
						// alles OK, Option kann angewandt werden
						applyIntOptions(optionString,option.getIntValue());
					// es ist eine Option, die int- oder double-Werte haben darf
					} else {
						// ueberpruefen, ob Wert innerhalb der vorgegebenen Schranken liegt
						double minValue = doubleOptions.get(optionString)[0];
						double maxValue = doubleOptions.get(optionString)[1];
						
						// Wert der Option bestimmen
						double val;
						if(option.isIntegerOption())
							val = option.getIntValue();
						else
							val = option.getDoubleValue();
						
						if (val < minValue || val > maxValue) {
							System.out.println("Error: The argument of configuration option \""
									+ optionString + "\" is out of range. It has to be between "
									+ minValue + " and " + maxValue + ".");
							System.exit(0);
						}
						
						// alles OK, Option kann angewandt werden
						applyDoubleOptions(optionString,val);						
					}
					
				// numerische Option existiert nicht => Fehlerbehandlung
				} else {
					if(strOptions.containsKey(optionString))
						throw new RuntimeException("The argument of configuration option \""
								+ optionString + "\" has to be a string.");
					else
						throw new RuntimeException("Configuration option \""
								+ optionString + "\" does not exist.");					
				}
		} else if (option.isSetOption()) {
			if(setOptions.contains(optionString)) {
				applySetOptions(optionString,option.getSetValues());
			} else {
				throw new Error("Configuration option \""
							+ optionString + "\" does not exist or its argument is not a list.");
			}
		
		} else {
			// Option existiert
			if(strOptions.containsKey(optionString)) {
				// ueberpruefen, ob Optionswert numerisch ist
				if(option.isIntegerOption())
					throw new Error("The argument of configuration option \""
							+ optionString + "\" has to be a string.");
				
				// moegliche Werte pruefen (wenn keine moeglichen Werte angegeben sind, dann sind
				// alle Strings erlaubt)
				String[] possibleValuesArray = strOptions.get(optionString);
				List<String> possibleValues = Arrays.asList(possibleValuesArray);
				if (!possibleValues.contains(option.getStrValue()) && possibleValues.size() != 0) {
					System.out.println("Error: The configuration option \"" + optionString
							+ "\" must not have value \"" + option.getStrValue()
							+ "\". The value must be one of " + possibleValues + ".");
					System.exit(0);
				}
				
				// alles OK, Option kann angewandt werden
				applyStringOptions(optionString,option.getStrValue());
				
			// Option existiert nicht => Fehlerbehandlung
			} else {
				if(intOptions.containsKey(optionString))
					throw new Error("The argument of configuration option \""
							+ optionString + "\" has to be a number.");
				else
					throw new Error("Configuration option \""
							+ optionString + "\" does not exist.");					
			}			
		}
		// options.add(option);
	}
	
	// Code aus Main zur Behandlung von Optionen
	/*
	  if (node instanceof ASTConfOption) {
				// allgemeine Variablenzuweisungen, damit die Implementierung
				// f�r jede einzelne Option dann einfach wird
				String option = ((ASTId) node.jjtGetChild(0)).getId();
				String optionIndex = option;
				// etwas vorsichtig sein, da Werte initialisiert werden, auch
				// wenn es z.B. keinen Integer in der Option gibt
				String subOption = "";
				boolean valueIsInt = false;
				int intValue = 0;
				String strValue = "";
				// zwei Optionen mit Punkt getrennt
				if (node.jjtGetNumChildren() == 3) {
					subOption = ((ASTId) node.jjtGetChild(1)).getId();
					optionIndex += "." + subOption;
					if (node.jjtGetChild(2) instanceof ASTNumber) {
						intValue = ((ASTNumber) node.jjtGetChild(2)).getId();
						valueIsInt = true;
					} else
						strValue = ((ASTId) node.jjtGetChild(2)).getId();
					// eine Option
				} else {
					if (node.jjtGetChild(1) instanceof ASTNumber) {
						intValue = ((ASTNumber) node.jjtGetChild(1)).getId();
						valueIsInt = true;
					} else
						strValue = ((ASTId) node.jjtGetChild(1)).getId();
				}

				if (intOptions.containsKey(optionIndex)) {
					if (!valueIsInt) {
						System.out.println("Error: The argument of configuration option \""
								+ optionIndex + "\" has to be a number.");
						System.exit(0);
					}
					int minValue = intOptions.get(optionIndex)[0];
					int maxValue = intOptions.get(optionIndex)[1];
					if (intValue < minValue || intValue > maxValue) {
						System.out.println("Error: The argument of configuration option \""
								+ optionIndex + "\" is out of range. It has to be between "
								+ minValue + " and " + maxValue + ".");
						System.exit(0);
					}

					applyIntOptions(optionIndex, intValue);

				} else if (strOptions.containsKey(optionIndex)) {
					if (valueIsInt) {
						System.out.println("The argument of configuration option \"" + optionIndex
								+ "\" must not be a number.");
						System.exit(0);
					}

					String[] possibleValuesArray = strOptions.get(optionIndex);
					List<String> possibleValues = Arrays.asList(possibleValuesArray);
					if (!possibleValues.contains(strValue) && possibleValues.size() != 0) {
						System.out.println("Error: The configuration option \"" + optionIndex
								+ "\" must not have value \"" + strValue
								+ "\". The value must be one of " + possibleValues + ".");
						System.exit(0);
					}

					applyStringOptions(optionIndex, strValue);

				} else {
					System.out.println("Error: " + optionIndex
							+ " is not a valid configuration option");
					System.exit(0);
				}

			}

	 */
	


	//private Map<String, Integer[]> createIntOptions() {
	//	Map<String, Integer[]> intOptions = new HashMap<String, Integer[]>();
	private void createIntOptions() {

		intOptions.put("maxLength", new Integer[] { 1, 20 });
		// intOptions.put("accuracyPenalty", new Integer[] { 1, 1000 });
		// intOptions.put("errorPenalty", new Integer[] { 1, 1000 });
		intOptions.put("gp.numberOfIndividuals", new Integer[] { 10, 1000000 });
		intOptions.put("gp.numberOfSelectedIndividuals", new Integer[] { 10, 1000000 });
		// intOptions.put("gp.crossoverPercent", new Integer[] { 0, 100 });
		// intOptions.put("gp.mutationPercent", new Integer[] { 0, 100 });
		// intOptions.put("gp.hillClimbingPercent", new Integer[] { 0, 100 });		
		intOptions.put("gp.postConvergenceGenerations", new Integer[] { 10, 1000 });
		intOptions.put("gp.generations", new Integer[] { 10, 1000 });
		intOptions.put("gp.tournamentSize", new Integer[] { 1, 10 });
		intOptions.put("gp.initMinDepth", new Integer[] { 1, 10 });
		intOptions.put("gp.initMaxDepth", new Integer[] { 1, 20 });

		// return intOptions;
	}

	private void createDoubleOptions() {

		// double-Optionen, die auch int sein können
		doubleOptions.put("accuracyPenalty", new Double[] { 1d, 1000d });
		doubleOptions.put("errorPenalty", new Double[] { 1d, 1000d });
		doubleOptions.put("gp.crossoverPercent", new Double[] { 0d, 100d });
		doubleOptions.put("gp.mutationPercent", new Double[] { 0d, 100d });
		doubleOptions.put("gp.hillClimbingPercent", new Double[] { 0d, 100d });
		doubleOptions.put("gp.refinementPercent", new Double[] { 0d, 100d });		
		doubleOptions.put("refinement.horizontalExpansionFactor", new Double[] { 0d, 1d });
		doubleOptions.put("percentPerLengthUnit", new Double[] { 0d, 1d });
		
	}	
	
	//private Map<String, String[]> createStringOptions() {
	//	Map<String, String[]> strOptions = new HashMap<String, String[]>();
	private void createStringOptions() {
		final String[] booleanArray = new String[] { "true", "false" };
		// leerer Array = keine Einschraenkung

		strOptions.put("penalizeNeutralExamples", booleanArray);
		strOptions.put("scoreMethod", new String[] { "full", "positive" });
		strOptions.put("showCorrectClassifications", booleanArray);
		// etwas eleganter waere hier, wenn man erst die Wissensbasis und dann
		// die Optionen parst, dann koennte man implementieren, dass der Rueckgabetyp
		// einem Konzeptname entsprechen muss
		strOptions.put("returnType", new String[] {});
		strOptions.put("statMode", booleanArray);
		strOptions.put("una", booleanArray);
		strOptions.put("owa", booleanArray);
		strOptions.put("algorithm", new String[] { "gp", "bruteForce", "random", "refinement", "hybridGP" });
		strOptions.put("reasoner", new String[] { "dig", "kaon2", "fastRetrieval" });
		strOptions.put("digReasonerURL", new String[] {});
		strOptions.put("useRetrievalForClassification", booleanArray);
		strOptions.put("hidePrefix", new String[] {});
		strOptions.put("showIndividuals", booleanArray);
		strOptions.put("showConcepts", booleanArray);
		strOptions.put("showRoles", booleanArray);
		strOptions.put("showInternalKB", booleanArray);
		strOptions.put("showSubsumptionHierarchy", booleanArray);
		strOptions.put("writeDIGProtocol", booleanArray);
		strOptions.put("digProtocolFile", new String[] {});
		// strOptions.put("preprocessingModule", new String[] {});
		strOptions.put("gp.selectionType", new String[] { "rankSelection", "fps", "tournament" });
		strOptions.put("gp.elitism", booleanArray);
		strOptions.put("gp.algorithmType", new String[] { "steadyState", "generational" });
		strOptions.put("gp.adc", booleanArray);
		strOptions.put("gp.useFixedNumberOfGenerations", booleanArray);
		strOptions.put("refinement.heuristic", new String[] { "lexicographic", "flexible" });
		strOptions.put("refinement.quiet", booleanArray);
		strOptions.put("refinement.writeSearchTree", new String[] {});
		strOptions.put("refinement.searchTreeFile", new String[] {});
		strOptions.put("refinement.applyAllFilter", booleanArray);
		strOptions.put("refinement.applyExistsFilter", booleanArray);
		strOptions.put("refinement.useTooWeakList", booleanArray);
		strOptions.put("refinement.useOverlyGeneralList", booleanArray);
		strOptions.put("refinement.useShortConceptConstruction", booleanArray);
		strOptions.put("refinement.useDIGMultiInstanceChecks", new String[] { "never", "twoChecks", "oneCheck"});
		strOptions.put("refinement.useAllConstructor", booleanArray);
		strOptions.put("refinement.useExistsConstructor", booleanArray);
		strOptions.put("refinement.useNegation", booleanArray);
	}
	
	private void createSetOptions() {
		setOptions.add("refinement.allowedConcepts");
		setOptions.add("refinement.allowedRoles");
		setOptions.add("refinement.ignoredConcepts");
		setOptions.add("refinement.ignoredRoles");		
	}

	private void applyIntOptions(String option, int value) {
		if (option.equals("maxLength"))
			Config.maxLength = value;
		else if (option.equals("gp.numberOfIndividuals"))
			Config.GP.numberOfIndividuals = value;
		else if (option.equals("gp.numberOfSelectedIndividuals"))
			Config.GP.numberOfSelectedIndividuals = value;		
		else if (option.equals("gp.postConvergenceGenerations"))
			Config.GP.postConvergenceGenerations = value;
		else if (option.equals("gp.generations"))
			Config.GP.generations = value;
		else if (option.equals("gp.tournamentSize"))
			Config.GP.tournamentSize = value;
		else if (option.equals("gp.initMinDepth"))
			Config.GP.initMinDepth = value;	
		else if (option.equals("gp.initMaxDepth"))
			Config.GP.initMaxDepth = value;		
	}
	
	private void applyDoubleOptions(String option, double value) {
		// System.out.println(option + "  " + value);
		if (option.equals("accuracyPenalty"))
			Config.accuracyPenalty = value;
		else if (option.equals("errorPenalty"))
			Config.errorPenalty = value;	
		else if (option.equals("gp.crossoverPercent"))
			Config.GP.crossoverProbability = value / (double) 100;
		else if (option.equals("gp.mutationPercent"))
			Config.GP.mutationProbability = value / (double) 100;
		else if (option.equals("gp.hillClimbingPercent"))
			Config.GP.hillClimbingProbability = value / (double) 100;
		else if (option.equals("gp.refinementPercent"))
			Config.GP.refinementProbability = value / (double) 100;		
		else if (option.equals("refinement.horizontalExpansionFactor"))
			Config.Refinement.horizontalExpansionFactor = value;
		else if (option.equals("percentPerLengthUnit"))
			Config.percentPerLengthUnit = value;		
	}
	
	private void applyStringOptions(String option, String value) {
		if (option.equals("penalizeNeutralExamples"))
			Config.penalizeNeutralExamples = strToBool(value);
		else if (option.equals("showCorrectClassifications"))
			Config.showCorrectClassifications = strToBool(value);
		else if (option.equals("statMode"))
			Config.statisticMode = strToBool(value);
		else if (option.equals("una"))
			Config.una = strToBool(value);		
		else if (option.equals("owa"))
			Config.owa = strToBool(value);		
		else if (option.equals("gp.useFixedNumberOfGenerations"))
			Config.GP.useFixedNumberOfGenerations = strToBool(value);
		else if (option.equals("scoreMethod")) {
			if (value.equals("full"))
				Config.scoreMethod = ScoreMethod.FULL;
			else
				Config.scoreMethod = ScoreMethod.POSITIVE;
		} else if (option.equals("returnType"))
			Config.returnType = value;
		else if (option.equals("algorithm")) {
			if (value.equals("gp"))
				Config.algorithm = Algorithm.GP;
			else if (value.equals("random"))
				Config.algorithm = Algorithm.RANDOM_GUESSER;
			else if(value.equals("bruteForce"))
				Config.algorithm = Algorithm.BRUTE_FORCE;
			else if(value.equals("refinement"))
				Config.algorithm = Algorithm.REFINEMENT;
			// hybrid GP = Menge von Konfigurationsoptionen
			else {
				Config.algorithm = Algorithm.HYBRID_GP;
				Config.GP.refinementProbability = 1.0d;
				Config.GP.crossoverProbability = 0.0d;
				Config.GP.hillClimbingProbability = 0.0d;
				Config.GP.mutationProbability = 0.0d;
			}
		} else if(option.equals("hidePrefix")) {
			Config.hidePrefixes.add(value);
		} else if (option.equals("showIndividuals")) {
			Config.showIndividuals = strToBool(value);
		} else if (option.equals("showConcepts")) {
			Config.showConcepts = strToBool(value);
		} else if (option.equals("showRoles")) {
			Config.showRoles = strToBool(value);
		} else if (option.equals("showInternalKB")) {
			Config.showInternalKB = strToBool(value);
		} else if (option.equals("showSubsumptionHierarchy")) {
			Config.showSubsumptionHierarchy = strToBool(value);
		} else if (option.equals("writeDIGProtocol")) {
			Config.writeDIGProtocol = strToBool(value);
		} else if (option.equals("digProtocolFile")) {
			Config.digProtocolFile = new File(value);
		// } else if (option.equals("preprocessingModule")) {
		//	Config.preprocessingModule = value;
		} else if (option.equals("gp.selectionType")) {
			if (value.equals("fps"))
				Config.GP.selectionType = SelectionType.FPS;
			else if (value.equals("rankSelection"))
				Config.GP.selectionType = SelectionType.RANK_SELECTION;
			else
				Config.GP.selectionType = SelectionType.TOURNAMENT_SELECTION;
		} else if (option.equals("gp.algorithmType")) {
			if (value.equals("steadyState"))
				Config.GP.algorithmType = AlgorithmType.STEADY_STATE;
			else
				Config.GP.algorithmType = AlgorithmType.GENERATIONAL;
		} else if (option.equals("gp.adc")) {
			Config.GP.adc = strToBool(value);
		} else if (option.equals("refinement.heuristic")) {
			if(value.equals("lexicographic"))
				Config.Refinement.heuristic = Config.Refinement.Heuristic.LEXICOGRAPHIC;
			else
				Config.Refinement.heuristic = Config.Refinement.Heuristic.FLEXIBLE;
		} else if (option.equals("refinement.quiet"))
			Config.Refinement.quiet = strToBool(value);
		else if (option.equals("refinement.writeSearchTree"))
			Config.Refinement.writeSearchTree = strToBool(value);
		else if (option.equals("refinement.searchTreeFile")) {
			Config.Refinement.searchTreeFile = new File(value);
		} else if (option.equals("refinement.applyAllFilter"))
			Config.Refinement.applyAllFilter = strToBool(value);
		else if (option.equals("refinement.applyExistsFilter"))
			Config.Refinement.applyExistsFilter = strToBool(value);
		else if (option.equals("refinement.useTooWeakList"))
			Config.Refinement.useTooWeakList = strToBool(value);
		else if (option.equals("refinement.useOverlyGeneralList"))
			Config.Refinement.useOverlyGeneralList = strToBool(value);
		else if (option.equals("refinement.useShortConceptConstruction"))
			Config.Refinement.useShortConceptConstruction = strToBool(value);
		else if (option.equals("refinement.useAllConstructor"))
			Config.Refinement.useAllConstructor = strToBool(value);
		else if (option.equals("refinement.useExistsConstructor"))
			Config.Refinement.useExistsConstructor = strToBool(value);
		else if (option.equals("refinement.useNegation"))
			Config.Refinement.useNegation = strToBool(value);		
		else if (option.equals("reasoner")) {
			if(value.equals("dig"))
				Config.reasonerType = ReasonerType.DIG;
			else if(value.equals("kaon2"))
				Config.reasonerType = ReasonerType.KAON2;
			else if(value.equals("fastRetrieval"))
				Config.reasonerType = ReasonerType.FAST_RETRIEVAL;
		} else if (option.equals("digReasonerURL")) {
			try {
				Config.digReasonerURL = new URL(value);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.err.println("Malformed URL for DIG reasoner was given.");
				System.exit(0);
			}
		} else if (option.equals("useRetrievalForClassification"))
			Config.useRetrievalForClassification = strToBool(value);
		else if (option.equals("refinement.useDIGMultiInstanceChecks")) {
			if(value.equals("never"))
				Config.Refinement.useDIGMultiInstanceChecks = Config.Refinement.UseDIGMultiInstanceChecks.NEVER;
			else if(value.equals("twoChecks"))
				Config.Refinement.useDIGMultiInstanceChecks = Config.Refinement.UseDIGMultiInstanceChecks.TWOCHECKS;
			else if(value.equals("oneCheck"))
				Config.Refinement.useDIGMultiInstanceChecks = Config.Refinement.UseDIGMultiInstanceChecks.ONECHECK;
		} 
	}

	private void applySetOptions(String optionString, Set<String> setValues) {
		// System.out.println(":" + optionString + " " + setValues);
		
		if(optionString.equals("refinement.allowedConcepts")) {
			Config.Refinement.allowedConceptsAutoDetect = false;
			Config.Refinement.allowedConcepts = new TreeSet<AtomicConcept>(new ConceptComparator());
			for(String s : setValues)
				// es wird die gleiche Funktion wie im Parser genommen um Namen auf URIs zu mappen
				Config.Refinement.allowedConcepts.add(new AtomicConcept(DLLearner.getInternalURI(s)));
		} else if(optionString.equals("refinement.allowedRoles")) {
			Config.Refinement.allowedRolesAutoDetect = false;
			Config.Refinement.allowedRoles = new TreeSet<AtomicRole>(new RoleComparator());
			for(String s : setValues)
				Config.Refinement.allowedRoles.add(new AtomicRole(DLLearner.getInternalURI(s)));
		} else if(optionString.equals("refinement.ignoredConcepts")) {
			Config.Refinement.ignoredConcepts = new TreeSet<AtomicConcept>(new ConceptComparator());
			for(String s : setValues)
				Config.Refinement.ignoredConcepts.add(new AtomicConcept(DLLearner.getInternalURI(s)));			
		} else if(optionString.equals("refinement.ignoredRoles")) {
			Config.Refinement.ignoredRoles = new TreeSet<AtomicRole>(new RoleComparator());
			for(String s : setValues)
				Config.Refinement.ignoredRoles.add(new AtomicRole(DLLearner.getInternalURI(s)));			
		}
	}	
	
	private static boolean strToBool(String str) {
		if (str.equals("true"))
			return true;
		else if (str.equals("false"))
			return false;
		else
			throw new Error("Cannot convert to boolean.");
	}

	public void addDoubleOption(String option, Double[] range) {
		doubleOptions.put(option, range);
	}

	public Integer[] addIntegerOption(String option, Integer[] value) {
		return intOptions.put(option, value);
	}

	public boolean addSetOption(String option) {
		return setOptions.add(option);
	}

	public String[] addStringOption(String option, String[] value) {
		return strOptions.put(option, value);
	}

}

