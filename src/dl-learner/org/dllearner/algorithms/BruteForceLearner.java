package org.dllearner.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dllearner.Config;
import org.dllearner.core.ConfigEntry;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.IntegerConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.Disjunction;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.Top;

/**
 * TODO: Man könnte den Speicherbedarf gegen Null gehen lassen, wenn man gar keine Programme
 * generiert, also in einer Menge speichert, sondern sofort testet. Allerdings ist das
 * schwierig, da Programme kleinerer Länge immer weiterverwendet werden.
 * 
 * @author Jens Lehmann
 *
 */
public class BruteForceLearner extends LearningAlgorithm {
    
	LearningProblem learningProblem;
	
    // Set<String> posExamples = null;   
    // Set<String> negExamples = null;    
    
    private Concept bestDefinition;
    private Score bestScore;
    // private Set<String> bestDefPosSet = new TreeSet<String>();
    // private Set<String> bestDefNegSet = new TreeSet<String>();     
    // int bestScore;
    // int maxScore;
    
    // Liste aller generierten Programme
    // private List<Node> generatedPrograms = new LinkedList<Node>();
    // Programme nach Anzahl Knoten sortiert
    private Map<Integer,List<Concept>> generatedDefinitions = new HashMap<Integer,List<Concept>>();
    
    public BruteForceLearner(LearningProblem learningProblem, ReasoningService rs) {
    	this.learningProblem = learningProblem;
    }
    
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(LearningProblem.class);
		return problems;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new IntegerConfigOption("numberOfTrees", "number of randomly generated concepts/trees"));
		options.add(new IntegerConfigOption("maxDepth", "maximum depth of generated concepts/trees"));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {

	}	
    
    public void start() {
       	// FlatABox abox = FlatABox.getInstance();
    	int maxLength = Config.maxLength;
    	
        System.out.print("Generating definitions up to length " + maxLength + " ... ");
        long generationStartTime = System.currentTimeMillis();
        
        for(int i=1; i<=maxLength; i++)
            generatePrograms(i);
        
        long generationTime = System.currentTimeMillis() - generationStartTime;
        System.out.println("OK (" + generationTime + " ms)");
        
        // es wird angenommen, dass nur ein Konzept gelernt wird
        /*
        for(String s : abox.exampleConceptsPos.keySet()) {
            posExamples = abox.exampleConceptsPos.get(s);
        }
        for(String s : abox.exampleConceptsNeg.keySet()) {
            negExamples = abox.exampleConceptsNeg.get(s); 
        }
        */
        
        testGeneratedDefinitions(maxLength);
    
        // System.out.println("test duration: " + testDuration + " ms");
        System.out.println();
        System.out.println("Best definition found: \n" + bestDefinition);
        // System.out.println("classified positive: " + bestScore.getPosClassified());
        // System.out.println("classified negative: " + bestScore.getNegClassified());
        
        /*
        // HIER WIRD ANGENOMMEN, DASS NUR EIN KONZEPT GERLERNT WIRD
        Set<String> posExamples = null;
        Set<String> negExamples = null;
        Set<String> neutralExamples;

        // es wird angenommen, dass nur ein Konzept gelernt wird
        for(String s : abox.exampleConceptsPos.keySet()) {
            posExamples = abox.exampleConceptsPos.get(s);
        }
        for(String s : abox.exampleConceptsNeg.keySet()) {
            negExamples = abox.exampleConceptsNeg.get(s); 
        }
        neutralExamples = Helper.intersection(abox.top,posExamples);
        neutralExamples = Helper.intersection(neutralExamples,negExamples);
        
        Set<String> bestDefNeutralSet = Helper.intersection(abox.top,bestDefPosSet);
        bestDefNeutralSet = Helper.intersection(bestDefNeutralSet,bestDefNegSet);
        
        // Fehler berechnen
        // int numberOfErrors = Helper.intersection(bestDefPosSet)
        
        
        DecimalFormat df = new DecimalFormat("0.00");
        */
        System.out.println();
        System.out.println("Classification results:");
        System.out.println(bestScore);
        //System.out.println("false positives: " + Helper.intersection(bestDefPosSet,negExamples));
        //System.out.println("false negatives: " + Helper.intersection(bestDefNegSet,posExamples));
        //System.out.print("Score: " + bestScore + " Max: " + maxScore + " Difference: " + (maxScore-bestScore));
        //System.out.println(" Accuracy: " + df.format((double)bestScore/maxScore*100) + "%");
      	
    }
    
    private void testGeneratedDefinitions(int maxLength) {
        long testStartTime = System.currentTimeMillis();        
        // maxScore = posExamples.size() + negExamples.size();
        bestDefinition = generatedDefinitions.get(1).get(0);
        double bestScorePoints = Double.NEGATIVE_INFINITY;
        int overallCount = 0;
        int count = 0;
        Score tmp;
        double score;
        
        for(int i=1; i<=maxLength; i++) {
            long startTime = System.currentTimeMillis();
            System.out.print("Testing definitions of length " + i + " ... ");
            count = 0;
            for(Concept program : generatedDefinitions.get(i)) {
            	// falls return type angegeben ist, dann wird hier ein
            	// entsprechender Baum konstruiert
            	Concept newRoot;
            	if(!Config.returnType.equals("")) {
            		newRoot = new Conjunction(new AtomicConcept(Config.returnType),program);
            	} else
            		newRoot = program;
            	
                // int score = computeScore(abox,program);
                // tmp = new Score(newRoot); // newRoot.computeScore();
            	tmp = learningProblem.computeScore(newRoot);
                score = tmp.getScore();
                // später Abbruch bei maximaler Punktzahl einführen
                //if(score == maxScore)
                //    return;
                if(score > bestScorePoints) {
                    bestDefinition = newRoot;
                    bestScorePoints = score;
                    bestScore = tmp;
                }
                count++;
            }
            long duration = System.currentTimeMillis() - startTime; 
            System.out.println(count + " definitions tested (" + duration + " ms)");            
            overallCount += count;
        }        
        
        long testDuration = System.currentTimeMillis() - testStartTime;
        System.out.println("Overall: " + overallCount + " definitions tested (" + testDuration + " ms)");
    }
    

    
    private void generatePrograms(int length) {
        generatedDefinitions.put(length,new LinkedList<Concept>());
        if(length==1) {
            generatedDefinitions.get(1).add(new Top());
            generatedDefinitions.get(1).add(new Bottom());
            for(AtomicConcept atomicConcept : learningProblem.getReasoningService().getAtomicConcepts()) {
                generatedDefinitions.get(1).add(atomicConcept);
            }
        }
        
        if(length>1) {
            // Negation
            for(Concept childNode : generatedDefinitions.get(length-1)) {
                Concept root = new Negation(childNode);
                generatedDefinitions.get(length).add(root);
            }
        }
        
        // Mindestlänge 3, da man sonst mit Disjunktion und Konjunktion nichts
        // konstruieren kann
        if(length>2) {
            // Konjunktion oder Disjunktion auswählen
            for(int i=0; i<=1; i++) {
                // Variable von 1 bis angerundet (length-1)/2 gehen lassen
                // = Länge des linken Teilbaumes
                for(int z=1; z<=Math.floor(0.5*(length-1)); z++) {
                    // alle Programme der Länge z durchgehen (linker Teilbaum)
                    for(Concept leftChild : generatedDefinitions.get(z)) { 
                        // alle Programme der Länge length-z-1 durchgehen (rechter Teilbaum)
                        for(Concept rightChild : generatedDefinitions.get(length-z-1)) {
                            // Baum erzeugen
                            Concept root;
                            if(i==0) {
                            	root = new Disjunction(leftChild,rightChild);
                            } else {
                                root = new Conjunction(leftChild,rightChild);  
                            }
                            
                            // man beachte, dass hier nur Links gesetzt werden, d.h.
                            // dass:
                            // 1. jede Modifikation an einem generierten Programm auch
                            //    andere Programme beeinflussen kann
                            // 2. wohin der parent-Link eines Knotens zeigt ist nicht
                            //    spezifiziert, da ein Knoten Kind mehererer anderer
                            //    Knoten sein kann
                            // Das wird gemacht, da der Retrieval-Algorithmus den parent-
                            // Link nicht benötigt und die Bäume nicht modifiziert, abgesehen
                            // vom posSet und negSet, welches aber für jeden Baum neu
                            // berechnet wird. Das speichern der Programme ist auf diese
                            // Weise platzsparend, also 1 Programm = 1 Knoten.
                            // root.addChild(leftChild);
                            // root.addChild(rightChild);
                            // System.out.println(root);
                            
                            generatedDefinitions.get(length).add(root);
                        }
                    }
                }
            }
            
            // Exists und All (zählen als Länge 2 wegen Quantor und Rollennamen)
            for(Concept childNode : generatedDefinitions.get(length-2)) {
                // for(String roleName : abox.roles) {
            	for(AtomicRole atomicRole : learningProblem.getReasoningService().getAtomicRoles()) {
                    Concept root1 = new Exists(atomicRole,childNode);
                    generatedDefinitions.get(length).add(root1);
                    
                    Concept root2 = new All(atomicRole,childNode);
                    generatedDefinitions.get(length).add(root2);
                }
            }            
        }
    }

	public Score getSolutionScore() {
		return bestScore;
	}

	public Concept getBestSolution() {
		return bestDefinition;
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
