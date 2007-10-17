package org.dllearner;

import java.lang.reflect.Field;

import org.dllearner.algorithms.gp.GP.AlgorithmType;
import org.dllearner.algorithms.gp.GP.SelectionType;

public class Config {
	// standardmäßig wird bis Tiefe 7 gesucht
	// public static int maxLength = 7;

	// public static int maxDepth;

	// Punktabzug für "ungenaue" Klassifizierungen, also positiv als neutral,
	// neutral als negativ und negativ als neutral
	// public static double accuracyPenalty = 1;

	// Punktabzug für fehlerhafte Klassifizierungen, also positiv als negativ
	// und negativ als positiv
	// public static double errorPenalty = 3;

	// public static ScoreMethod scoreMethod = ScoreMethod.POSITIVE;

	// public static LearningProblemType learningProblemType = LearningProblemType.TWO_VALUED;

	// public static boolean penalizeNeutralExamples = false;

	// public static boolean showCorrectClassifications = false;

	// wieviel Prozent darf ein um eine Einheit längeres Konzept schlechter
	// sein (aktuell: 5% sind eine Verlängerung um 1 wert)
	// Problem: dieser Parameter hat für GP und Refinement zwar die gleiche
	// Bedeutung,
	// aber es sind unterschiedliche Werte angebracht;
	// bei GP sollte der Wert so sein, dass am Ende das gewünschte Konzept
	// tatsächlich die beste Fitness hat, also eher niedrig (<0.005);
	// bei Refinement in flexible heuristic soll es so sein, dass schlechtere
	// Konzepte
	// probiert werden sollen, sobald horizontal expansion eines gut
	// klassifizierenden
	// Knotens steigt => da ist also gewünscht das kürzere aber schlechtere
	// Knoten
	// ev. einen Vorsprung haben => demzufolge ist dort ein hoher Wert (ca.
	// 0.05)
	// angebracht
	// public static double percentPerLengthUnit = 0.0025;
	// public static double percentPerLengthUnit = 0.05;

//	public enum Algorithm {
//		GP, BRUTE_FORCE, RANDOM_GUESSER, REFINEMENT, HYBRID_GP
//	};
//
//	public static Algorithm algorithm = Algorithm.REFINEMENT;

	// Rückgabetyp des gelernten Konzepts
	// public static String returnType = "";

	// public static boolean statisticMode = false;

	// if set to true a retrieval algorithm is used for classification
	// instead of single instance checks (default is now false, because
	// we can send all instance checks in a single request), for KAON2
	// as reasoner it should in many cases be set to true
	// public static boolean useRetrievalForClassification = false;

	// welche Art von Reasoning wird benutzt (eigener Algorithmus,
	// KAON2-API, DIG-Interface)
	// public static ReasonerType reasonerType = ReasonerType.DIG;

	// bei fast retrieval muss trotzdem irgendein Reasoner gesetzt
	// werden um die flat ABox zu erzeugen
	// public static ReasonerType startUpReasoningType = ReasonerType.KAON2;

	// public static URL digReasonerURL = null;

	// unique names assumption
	// public static boolean una = false;

	// open world assumption; momentan wird closed world assumption nur
	// fuer Rollen unterstuetzt
	// public static boolean owa = true;

	// an-/abschalten von System.nanoTime()-Aufrufen außerhalb des
	// Reasoningservice
	// public static boolean useNonReasonerBenchmarks = false;

	// erlaubt das abschalten von Benchmarks bei der Subsumptionhierarchie,
	// da diese ohnehin gecacht wird, also die System.nanoTime()-Aufrufe nur
	// Zeit kosten
	// public static boolean useHierarchyReasonerBenchmarks = false;

	// public static List<String> hidePrefixes = new LinkedList<String>();

	// Informationen (Rollen, Konzepte, Individuen, Anzahl Axiome) über
	// Wissensbasis anzeigen => alles konfigurierbar um Output in Grenzen
	// zu halten
//	public static boolean showRoles = false;
//	public static boolean showConcepts = false;
//	public static boolean showIndividuals = false;
//	public static boolean showSubsumptionHierarchy = false;
	// zeigt die interne Wissensbasis an (d.h. keine externen OWL-Dateien)
//	public static boolean showInternalKB = false;
//	public static int maxLineLength = 100;

	// public static boolean writeDIGProtocol = false;
	// public static File digProtocolFile = new File("log/digProtocol.txt");

	// public static String preprocessingModule = "";

	// TODO: noch nicht implementiert, dass man diese per Config-Datei setzen
	// kann
	public static class Refinement {

		// Nutzung der Äquivalenz ALL R.C AND ALL R.D = ALL R.(C AND D)
		public static boolean applyAllFilter = true;

		// Nutzung der Äquivalenz EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
		public static boolean applyExistsFilter = true;

		public static boolean useTooWeakList = true;

		public static boolean useOverlyGeneralList = true;

		public static boolean useShortConceptConstruction = true;

		public static double horizontalExpansionFactor = 0.6;

		public static boolean improveSubsumptionHierarchy = true;

		public static boolean quiet = false;

		// public static boolean writeSearchTree = false;

		// public static File searchTreeFile = new File("searchTree.txt");

		// public static Heuristic heuristic = Heuristic.LEXICOGRAPHIC;

		// multi instance check => es wird versucht mehrere instance checks pro
		// Anfrage auf einmal an den Reasoner zu schicken; Vorteil bei DIG:
		// weniger Kommunikation; Nachteil: es müssen alle instanceChecks
		// ausgeführt
		// werden, bevor too weak festgestellt werden kann
		// TODO: not implemented
//		public static UseMultiInstanceChecks useDIGMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;

		// geplante Optionen um den Suchraum einzuschränken

		// Konzepte, die in der Definition vorkommen können (per Default
		// ("null") alle)
		// nicht implementiert
//		public static Set<AtomicConcept> allowedConcepts = null;
		// ignorierte Konzepte; Default null = keine
//		public static Set<AtomicConcept> ignoredConcepts = null;
		// beachte: es können nur entweder die erlaubten oder die ignorierten
		// Konzepte
		// gesetzt werden

		// true falls die Konzepte vom Nutzer gesetzt worden (also
		// allowedConcepts
		// gleich null), ansonsten false
//		public static boolean allowedConceptsAutoDetect = true;

		// Rollen, die in der Lösung vorkommen können
		// nicht implementiert
//		public static Set<AtomicRole> allowedRoles = null;
//		public static Set<AtomicRole> ignoredRoles = null;
//		public static boolean allowedRolesAutoDetect = true;

		// max. Verschachtelungstiefe der Lösung
		// nicht implementiert
		public static int maxDepth = 0;

		// Konstruktoren an- und abschalten
		// nicht implementiert
		public static boolean useAllConstructor = true;
		public static boolean useExistsConstructor = true;
		public static boolean useNegation = true;
		// public static boolean useDisjunction = true;
		// public static boolean useConjunction = true;

		// Domain und Range beim Verschachteln von Rollen beachten
		// nicht implementiert
		// TODO: Wie soll man das in DIG machen?
//		public static boolean useDomainRange = false;

		// bereits vorher allgemeinere Konzepte festlegen (Default: keine)
		// nicht implementiert
		// => ist eigentlich unnötig, da dieses Wissen zur Ontologie hinzugefügt
		// und dann gelernt werden kann
		// public static List<AtomicConcept> fixedUpperConcepts;

	}

	public static class GP {

		// FPS funktioniert momentan noch nicht, es muss sichergestellt werden,
		// dass die Fitness positiv ist (momentan ist sie nie gr��er 0)
		public static SelectionType selectionType = SelectionType.RANK_SELECTION;

		public static int tournamentSize = 3;

		public static boolean elitism = true;

		public static AlgorithmType algorithmType = AlgorithmType.STEADY_STATE;

		public static double mutationProbability = 0.03d;

		public static double crossoverProbability = 0.95d;

		public static double hillClimbingProbability = 0.0d;

		public static double refinementProbability = 0.0;

		public static int numberOfIndividuals = 100;

		public static int numberOfSelectedIndividuals = 96;

		public static boolean useFixedNumberOfGenerations = false;

		public static int generations = 20;

		public static int postConvergenceGenerations = 50;

		public static boolean adc = false;

		public static int initMinDepth = 4;

		public static int initMaxDepth = 6;

		public static int maxConceptLength = 75;

		// bei true werden statt Disjunction MultiDisjunction und statt
		// Conjunction
		// MultiConjunction im GP benutzt (es gibt derzeit keinen Grund es auf
		// false
		// zu setzen)
		public static boolean useMultiStructures = true;
		
	}

	public static String print() {
		StringBuffer buff = new StringBuffer();
		buff.append("** overview of all set config values**\n\n");

		Field[] fields = Config.class.getDeclaredFields();
		// Field[] fields = this.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				buff.append(fields[i].getName()).append("\t=>\t").append(fields[i].get(Config.class))
						.append("\n");

				// System.out.println(fields[i].getName() + " : values: " +
				// fields[i].get(this));
			} catch (IllegalAccessException ex) {
				ex.printStackTrace(System.out);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace(System.out);
			}

		}
		return buff.toString();

	}
}
