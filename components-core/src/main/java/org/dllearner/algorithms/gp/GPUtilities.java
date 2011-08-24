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

package org.dllearner.algorithms.gp;

import java.util.*;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.FlatABox;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.PosNegLPStrict;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.learningproblems.ScoreThreeValued;
import org.dllearner.reasoning.FastRetrieval;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;


/**
 * A utility class, which implements crossover, mutation and tree creation methods.
 * 
 * Schwaechen: Der Code ist ziemlich komplex und die Performance nicht besonders gut,
 * da oefter Baeume komplett neu erstellt statt geklont werden etc. Wenn der Code
 * erstmal lauffaehig ist, dann kann man hier noch mit Optimierungen ansetzen.
 * 
 * Notiz: Wenn man den einzelnen Knoten noch eine Stelligkeit zuweist, dann koennte
 * man diese Klasse komplett generisch halten, d.h. unabhaengig davon, dass
 * DLs gelernt werden sollen.
 * 
 * @author Jens Lehmann
 * 
 */
public class GPUtilities {

	public static int fitnessEvaluations = 0;
	public static int crossover = 0;
	public static int mutation = 0;
	public static int hillClimbing = 0;
	
    private static Random rand = new Random();
    
    private static ScorePosNeg calculateFitness(AbstractLearningProblem learningProblem, Description hypothesis) {
    	return calculateFitness(learningProblem, hypothesis, null);
    }
    
    // TODO: eventuell darueber nachdenken diese return-type-Sache zu entfernen
    // Alternative: man bezieht sie in die zentralen Score-Berechnungen mit ein
    // (macht aber nicht so viel Sinn, da man das bei richtigen Reasoning-Algorithmen
    // ohnehin mit einer Erweiterung der Wissensbasis um die Inklusion Target SUBSETOF ReturnType
    // erschlagen kann)
	private static ScorePosNeg calculateFitness(AbstractLearningProblem learningProblem, Description hypothesis, Description adc) {
		Description extendedHypothesis;
		
		// return type temporarily disabled 
		// => it is probably more appropriate to have the 
		// number of superclasses of a target concept
		// as parameter of the learning problem
//		
//		if (!Config.returnType.equals("")) {
//			System.out.println("return type");
//			
//			Concept newRoot;
//			if(Config.GP.useMultiStructures)
//				newRoot = new MultiConjunction(new AtomicConcept(Config.returnType),hypothesis);
//			else
//				newRoot = new Conjunction(new AtomicConcept(Config.returnType),hypothesis);			
//			// parent wieder auf null setzen, damit es nicht inkonsistent wird
//			// TODO: ist nicht wirklich elegant und auch inkompatibel mit
//			// dem Hill-Climbing-Operator
//			hypothesis.setParent(null);
//			extendedHypothesis = newRoot;
//		} else
			extendedHypothesis = hypothesis;		
		
		ScorePosNeg score;
		if(adc != null)
			// TODO: ADC-Support
			// score = learningProblem.computeScore(extendedHypothesis, adc);
			throw new RuntimeException("ADC not supported");
		else
			score = (ScorePosNeg) learningProblem.computeScore(extendedHypothesis);
		
		// System.out.println(hypothesis);
		// System.out.println(score.getScore());
		
		/*
		if (Config.GP.adc)
			scoreAdc = adc.computeScore();

		if (Config.GP.adc)
			score = extendedHypothesis.computeScore(scoreAdc.getDefPosSet(), scoreAdc
					.getDefNegSet());
		else
			score = extendedHypothesis.computeScore();
			*/
		/*
		fitness = score.getScore() - 0.1 * hypothesis.getConceptLength();
		
		if (Config.GP.adc)
			fitness -= 0.1 * adc.getConceptLength();

		// zus�tzliche Bestrafung f�r sehr lange Definitionen
		if(hypothesis.getNumberOfNodes()>50)
			fitness -= 10;
		*/
		fitnessEvaluations++;
		
		return score;
	}    
    
	public static Program createProgram(AbstractLearningProblem learningProblem, Description mainTree) {
		return new Program(calculateFitness(learningProblem, mainTree), mainTree);
	}
	
	private static Program createProgram(AbstractLearningProblem learningProblem, Description mainTree, Description adc) {
		return new Program(calculateFitness(learningProblem, mainTree,adc), mainTree, adc);
	}
	
    /**
     * Perform a point mutation on the given program.
     * @param p The program to be mutated.
     */
    public static Program mutation(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, Program p) {
    	mutation++;
    	if(p.getAdc() != null) {
    		// TODO: hier kann man noch mehr Feinabstimmung machen, d.h.
    		// Mutation abh�ngig von Knotenanzahl
    		if(Math.random()<0.5) {
    			Description mainTree = mutation(learningProblem, rs, p.getTree(),true);
    			Description adc = p.getAdc();
    			ScorePosNeg score = calculateFitness(learningProblem,mainTree,adc);
    			return new Program(score, mainTree, adc);
    		}
    		else {
    			Description mainTree = p.getTree();
    			Description adc = mutation(learningProblem, rs, p.getAdc(),false);
    			ScorePosNeg score = calculateFitness(learningProblem,mainTree,adc);
    			return new Program(score, mainTree, adc);    			
    		}
    	} else {
    		Description tree = mutation(learningProblem, rs,p.getTree(),false);
    		ScorePosNeg score = calculateFitness(learningProblem, tree);
            return new Program(score, tree);
    	}
    }

    private static Description mutation(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, Description tree, boolean useADC) {
    	// auch bei Mutation muss darauf geachtet werden, dass 
    	// Baum nicht modifiziert wird (sonst w�rde man automatisch auch
    	// andere "selected individuals" modifizieren)
    	Description t = (Description) tree.clone();
    	// bis auf Klon Mutation genau wie vorher
        int size = t.getNumberOfNodes();
        // TODO: auch Mutationen von Einzelknoten erlauben
        if (size > 1) {
        	/* alte Implementierung
            int randNr = rand.nextInt(size) + 1;
            Node st = t.getSubtree(randNr);
            Node stParent = st.getParent();
            stParent.getChildren().remove(st);
            st.setParent(null);
            // int subTreeSize = st.getNumberOfNodes();
            // ProgramTree treeNew = createGrowRandomTree(subTreeSize);
            // Point mutation
            Node treeNew = createGrowRandomTree(3);
            treeNew.setParent(stParent);
            stParent.getChildren().add(treeNew);
            */
        	// neue Implementierung
            int randNr = rand.nextInt(size) + 1;
            Description st = t.getSubtree(randNr);
            Description stParent = st.getParent();
            stParent.getChildren().remove(st);
            Description treeNew = createGrowRandomTree(learningProblem, rs, 3, useADC);
            stParent.addChild(treeNew);
        } else
        	// return createLeafNode(useADC);
        	return pickTerminalSymbol(learningProblem,rs,useADC);
        return t;
    }

    private static void swapSubtrees(Description t1, Description t2) {
        if (t1.getParent() != null && t2.getParent() != null) {
            // handling children
            Description t1Parent = t1.getParent();
            Description t2Parent = t2.getParent();
            // t1 is first child
            if (t1Parent.getChild(0).equals(t1))
                t1Parent.getChildren().add(0, t2);
            else
                t1Parent.getChildren().add(1, t2);
            // t2 is first child
            if (t2Parent.getChild(0).equals(t2))
                t2Parent.getChildren().add(0, t1);
            else
                t2Parent.getChildren().add(1, t1);
            // remove old children
            t1Parent.getChildren().remove(t1);
            t2Parent.getChildren().remove(t2);

            // handling parents
            // ProgramTree tmp = t1Parent;
            t1.setParent(t2Parent);
            t2.setParent(t1Parent);
        } else
            throw new Error(
                    "Trees are not real subtrees (at least one of them is a root node).");
    }

    /**
     * Perform crossover on two programs.
     * @param p1 First parent.
     * @param p2 Second parent.
     * @return A two-element array containing the offpsring.
     */
    public static Program[] crossover(AbstractLearningProblem learningProblem, Program p1, Program p2) {
    	crossover++;
    	if(p1.getAdc() != null) {
    		Description[] pt;
    		Program result[] = new Program[2];
    		
    		// es wird entweder ADC oder Hauptbaum einem Crossover
    		// unterzogen und dann ein neues Programm erstellt
    		if(Math.random()<0.5) {
    			pt = crossover(p1.getTree(), p2.getTree()); 
    			result[0] = createProgram(learningProblem, pt[0], p1.getAdc());
                result[1] = createProgram(learningProblem, pt[1], p2.getAdc());
    		} else {
    			pt = crossover(p1.getAdc(), p2.getAdc());
    			result[0] = createProgram(learningProblem, p1.getTree(),pt[0]);
                result[1] = createProgram(learningProblem, p2.getTree(),pt[1]);
    		}
            return result;      		
    	} else {
            Description[] pt = crossover(p1.getTree(), p2.getTree());
            Program result[] = new Program[2];
            result[0] = createProgram(learningProblem,pt[0]);
            result[1] = createProgram(learningProblem,pt[1]);
            return result;    		
    	}
    }

    private static Description[] crossover(Description tree1, Description tree2) {
        Description tree1cloned = (Description) tree1.clone();
        Description tree2cloned = (Description) tree2.clone();

        Description[] results = new Description[2];
        int rand1 = rand.nextInt(tree1.getNumberOfNodes());
        int rand2 = rand.nextInt(tree2.getNumberOfNodes());
        Description t1 = tree1cloned.getSubtree(rand1);
        Description t2 = tree2cloned.getSubtree(rand2);

        if (t1.isRoot() && !t2.isRoot()) {
            Description t2Parent = t2.getParent();
            // t2 is first child
            if (t2Parent.getChild(0).equals(t2))
                t2Parent.getChildren().add(0, t1);
            else
                t2Parent.getChildren().add(1, t1);
            t2Parent.getChildren().remove(t2);
            t1.setParent(t2Parent);

            t2.setParent(null);
            results[0] = t2;
            results[1] = tree2cloned;
        } else if (!t1.isRoot() && t2.isRoot()) {
            Description t1Parent = t1.getParent();
            // t2 is first child
            if (t1Parent.getChild(0).equals(t1))
                t1Parent.getChildren().add(0, t2);
            else
                t1Parent.getChildren().add(1, t2);
            t1Parent.getChildren().remove(t1);
            t2.setParent(t1Parent);

            t1.setParent(null);
            results[0] = tree1cloned;
            results[1] = t1;
        } else if (!t1.isRoot() && !t2.isRoot()) {
            swapSubtrees(t1, t2);
            results[0] = tree1cloned;
            results[1] = tree2cloned;
        } else {
            results[0] = tree2cloned;
            results[1] = tree1cloned;
        }
        return results;
    }

    // m�sste auch mit ADC funktionieren, da nur am Hauptbaum etwas 
    // ver�ndert wird
    public static Program hillClimbing(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, Program p) {
    	hillClimbing++;
    	// checken, ob Bedingungen f�r hill-climbing erf�llt sind
    	if(!rs.getReasonerType().equals(ReasonerType.FAST_RETRIEVAL)
    			|| !(p.getScore() instanceof ScoreThreeValued)) {
    		throw new Error("Hill climbing can only be used with the fast-retrieval-algorithm on a three valued learning problem.");
    	}
    	
    	// SortedSetTuple s = new SortedSetTuple(p.getScore().getDefPosSet(),p.getScore().getDefNegSet());
    	// Knoten kopieren, damit er sich nicht ver�ndert (es wird sonst der
    	// parent-Link ver�ndert)
    	Description treecloned = (Description) p.getTree().clone();
    	if(p.getAdc() != null)
    		return createProgram(learningProblem,hillClimbing(learningProblem,rs,treecloned,(ScoreThreeValued)p.getScore()),p.getAdc());
    	else
    		return createProgram(learningProblem,hillClimbing(learningProblem,rs,treecloned,(ScoreThreeValued)p.getScore()));
    }
    
    // one-step hill-climbing
    // es ist zwar von der Implementierung her sehr aufw�ndig die besten
    // Alternativen zu speichern und dann ein Element zuf�llig auszuw�hlen,
    // aber w�rde man das nicht machen, dann w�re das ein starker Bias
    // zu z.B. Disjunktion (weil die als erstes getestet wird)
    private static Description hillClimbing(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, Description node, ScoreThreeValued score) {
    	SortedSetTuple<Individual> tuple = new SortedSetTuple<Individual>(score.getPosClassified(),score.getNegClassified());
    	SortedSetTuple<String> stringTuple = Helper.getStringTuple(tuple);
    	// FlatABox abox = FlatABox.getInstance();
    	Description returnNode = node;
    	// damit stellen wir sicher, dass nur Konzepte in die Auswahl
    	// genommen werden, die besser klassifizieren als das �bergebene
    	// Konzept (falls das nicht existiert, dann hill climbing = reproduction)
    	System.err.println("Next line needs fixing to work.");
    	System.exit(0);
    	// double bestScore = score.getScore()+Config.accuracyPenalty/2;
    	double bestScore = 0;
    	Map<Integer,List<String>> bestNeighbours = new TreeMap<Integer,List<String>>();
    	ScorePosNeg tmpScore;
    	SortedSetTuple<String> tmp, tmp2;
    	// FlatABox abox = ((FastRetrievalReasoner)learningProblem.getReasoner().getFastRetrieval().getAbox();
    	// FlatABox abox = Main.getFlatAbox();
    	FlatABox abox = null;
		try {
			abox = Helper.createFlatABox(rs);
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// TODO: testen, ob aktuelles Konzept zu speziell bzw. allgemein ist;
    	// dann kann man das effizienter implementieren
    	
    	// Tests f�r Disjunktion bzw. Konjunktion
    	for(String concept : abox.concepts) {
    	// for(AtomicConcept ac : learningProblem.getReasoner().getAtomicConcepts())
    		tmp = new SortedSetTuple<String>(abox.atomicConceptsPos.get(concept),abox.atomicConceptsNeg.get(concept));
    		// TODO: double retrieval nutzen
    		
    		tmp2 = FastRetrieval.calculateDisjunctionSets(stringTuple, tmp);
    		tmpScore = getScore(node.getLength()+2, learningProblem, rs, Helper.getIndividualSet(tmp2.getPosSet()),Helper.getIndividualSet(tmp2.getNegSet()));
    		if(tmpScore.getScoreValue()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,1,concept,false);
    		else if(tmpScore.getScoreValue()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,1,concept,true);
    		
    		tmp2 = FastRetrieval.calculateConjunctionSets(stringTuple, tmp);
    		tmpScore = getScore(node.getLength()+2,learningProblem, rs, Helper.getIndividualSet(tmp2.getPosSet()),Helper.getIndividualSet(tmp2.getNegSet()));
    		if(tmpScore.getScoreValue()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,2,concept,false);
    		else if(tmpScore.getScoreValue()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,2,concept,true);    		
    	}
    	
    	// Tests f�r All und Exists
    	for(String role : abox.roles) {
    		tmp = FastRetrieval.calculateAllSet(abox,role,stringTuple);
    		tmpScore = getScore(node.getLength()+2,learningProblem, rs, Helper.getIndividualSet(tmp.getPosSet()),Helper.getIndividualSet(tmp.getNegSet()));
    		if(tmpScore.getScoreValue()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,3,role,false);
    		else if(tmpScore.getScoreValue()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,3,role,true);    		

    		tmp = FastRetrieval.calculateExistsSet(abox,role,stringTuple);
    		tmpScore = getScore(node.getLength()+2,learningProblem, rs, Helper.getIndividualSet(tmp.getPosSet()),Helper.getIndividualSet(tmp.getNegSet()));
    		if(tmpScore.getScoreValue()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,4,role,false);
    		else if(tmpScore.getScoreValue()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,4,role,true);    		
    	}
    	
    	// Gr��e bestimmen
    	int sizeSum1=0;
    	if(bestNeighbours.containsKey(1))
    		sizeSum1 += bestNeighbours.get(1).size();
    	
    	int sizeSum2 = sizeSum1;
    	if(bestNeighbours.containsKey(2))
    		sizeSum2 += bestNeighbours.get(2).size();
    	
    	int sizeSum3 = sizeSum2;
    	if(bestNeighbours.containsKey(3))
    		sizeSum3 += bestNeighbours.get(3).size();
    	
    	int sizeSum4 = sizeSum3;
    	if(bestNeighbours.containsKey(4))
    		sizeSum4 += bestNeighbours.get(4).size();
    	
    	// reproduction, falls nichts besseres gefunden wurde
    	if(sizeSum4==0)
    		return node;
    	else {
    	// zuf�llig eines der besten Elemente ausw�hlen
    	int nr = rand.nextInt(sizeSum4);
    	String name;
    	if(nr<sizeSum1) {
    		name = bestNeighbours.get(1).get(nr);  
    		// returnNode = new Disjunction();
    		// returnNode.addChild(new AtomicConcept(name));    		
    		// returnNode.addChild(node);
//    		if(useMultiStructures)
    			returnNode = new Union(new NamedClass(name),node);
//    		else
//    			returnNode = new Disjunction(new AtomicConcept(name),node);
    	// wegen else if schlie�en sich die F�lle gegenseitig aus
    	} else if(nr<sizeSum2) {
    		name = bestNeighbours.get(2).get(nr-sizeSum1);
    		// returnNode = new Conjunction();
    		// returnNode.addChild(new AtomicConcept(name));    		
    		// returnNode.addChild(node);
//    		if(useMultiStructures)
    			returnNode = new Intersection(new NamedClass(name),node);
//    		else
//    			returnNode = new Conjunction(new AtomicConcept(name),node);
    	} else if(nr<sizeSum3) {
    		name = bestNeighbours.get(3).get(nr-sizeSum2);
    		// returnNode = new Exists(new AtomicRole(name));
    		// returnNode.addChild(node);
    		returnNode = new ObjectSomeRestriction(new ObjectProperty(name),node);
    	} else {
    		name = bestNeighbours.get(4).get(nr-sizeSum3);
    		// returnNode = new All(new AtomicRole(name));
    		// returnNode.addChild(node);   
    		returnNode = new ObjectAllRestriction(new ObjectProperty(name),node);
    	}
    		
    	return returnNode;
    	}
    }
    
    private static ScoreThreeValued getScore(int conceptLength, AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, SortedSet<Individual> posClassified, SortedSet<Individual> negClassified) {
    	// es muss hier die Helper-Methode verwendet werden, sonst werden
    	// Individuals gel�scht !!
        Set<Individual> neutClassified = Helper.intersection(rs.getIndividuals(),posClassified);
    	// learningProblem.getReasoner().getIndividuals();
    	// neutClassified.retainAll(posClassified);
    	neutClassified.retainAll(negClassified);
    	PosNegLPStrict lp = (PosNegLPStrict)learningProblem;
    	return new ScoreThreeValued(conceptLength, lp.getAccuracyPenalty(), lp.getErrorPenalty(), lp.isPenaliseNeutralExamples(), lp.getPercentPerLengthUnit(), posClassified, neutClassified, negClassified, lp.getPositiveExamples(),lp.getNeutralExamples(),lp.getNegativeExamples());
    }
    
    // aktualisiert die besten Knoten
    // Integer: 1 Disjunktion, 2 Konjunktion, 3 Exists, 4 All
    // Name: Name des Konzepts bzw. der Rolle
    // clear-Flag ist true, falls neuer Nachbar besser als die bisherigen ist und
    // false, falls er gleich gut ist
    private static Map<Integer,List<String>> updateMap(Map<Integer,List<String>> bestNeighbours, Integer cat, String name, boolean clear) {
    	Map<Integer,List<String>> returnMap;
    	List<String> set;
    	if(clear) {
    		returnMap = new TreeMap<Integer,List<String>>();
    		set = new LinkedList<String>();
    		set.add(name);
    		returnMap.put(cat,set);
    	} else {
    		returnMap = bestNeighbours;
    		if(bestNeighbours.containsKey(cat)) {
    			bestNeighbours.get(cat).add(name);
    		} else {
    			set = new LinkedList<String>();
    			set.add(name);
    			returnMap.put(cat,set);
    		}
    	}
    	return returnMap;
    }
    
    private static Description pickTerminalSymbol(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, boolean useADC) {
        // FlatABox abox = FlatABox.getInstance();
        int nr;
        int nrOfConcepts = rs.getNamedClasses().size();
        
        // ein Blattknoten kann folgendes sein:
        // Top, Bottom, Konzept => alles am Besten gleichwahrscheinlich         
        if(useADC)
        	nr = rand.nextInt(3+nrOfConcepts);
        else
        	nr = rand.nextInt(2+nrOfConcepts);
        	
        if(nr==0)
        	return new Thing();
        else if(nr==1)
            return new Nothing();
        // die Zahl kann nur vorkommen, wenn ADC aktiviert ist
        else if(nr==2+nrOfConcepts) {
        	// System.out.println("== ADC 2 ==");
        	return new ADC();
         }
        else
            return (NamedClass) rs.getAtomicConceptsList().get(nr-2).clone();    	  	
    }
    
    // Funktion nach Umstelllung der Konstruktoren nicht mehr ben�tigt
    /*
    private static Concept pickFunctionSymbol() {
        FlatABox abox = FlatABox.getInstance();
        int numberOfRoles = abox.roles.size();
        int nr = rand.nextInt(3+2*numberOfRoles);

        if(nr==0)
            return new Disjunction();
        else if(nr==1)
            return new Conjunction();
        else if(nr==2)
        	return new Negation();
        else if(nr - 3 < numberOfRoles) 
        	return new Exists(new AtomicRole(abox.getRole(nr-3)));
        else
        	return new All(new AtomicRole(abox.getRole(nr-3-numberOfRoles)));   	
    }
    */
    
    /*
    private static Concept pickAlphabetSymbol(boolean useADC) {
        FlatABox abox = FlatABox.getInstance();
        int numberOfConcepts = abox.concepts.size();
        int numberOfRoles = abox.roles.size();
        int numberOfAlphabetSymbols = numberOfConcepts + 2*numberOfRoles + 5;
        if(useADC)
        	numberOfAlphabetSymbols += 1;
        
        int nr = rand.nextInt(numberOfAlphabetSymbols);
        
        if(nr==0)
        	return new Top();
        else if(nr==1)
        	return new Bottom();
        // ADC bekommt die h�chste Nummer, die nur in diesem Fall m�glich ist
        else if(nr==numberOfConcepts + 2*numberOfRoles + 5) 
        	return new ADC(); 
        else if(nr>=2 && nr < numberOfConcepts+2)
        	return new AtomicConcept(abox.getConcept(nr-2));
        else if(nr==numberOfConcepts+2)
        	return new Conjunction();
        else if(nr==numberOfConcepts+3)
        	return new Disjunction();
        else if(nr==numberOfConcepts+4)
        	return new Negation();
        else if(nr>=numberOfConcepts+5 && nr<numberOfConcepts+5+numberOfRoles)
        	return new Exists(new AtomicRole(abox.getRole(nr-numberOfConcepts-5)));
        else
        	return new All(new AtomicRole(abox.getRole(nr-numberOfConcepts-5-numberOfRoles)));
    }
    */
    
    /*
    private static Concept createNonLeafNodeEqualProp() {
        FlatABox abox = FlatABox.getInstance();
        int numberOfRoles = abox.roles.size();
        int nr = rand.nextInt(3+2*numberOfRoles);

        if(nr==0)
            return new Disjunction();
        else if(nr==1)
            return new Conjunction();
        else if(nr==2)
        	return new Negation();
        else if(nr - 3 < numberOfRoles) 
        	return new Exists(new AtomicRole(abox.getRole(nr-3)));
        else
        	return new All(new AtomicRole(abox.getRole(nr-3-numberOfRoles)));
    }    
    */
    
    /* BUG: erzeugt kein NOT
    private static Node createNonLeafNode() {
        // ein Nichtblattknoten kann folgendes sein:
        // Existenz- oder Allquantor mit Rollenname,
        // Disjunktion, Konjunktion
        // => hier muss Disjunktion und Konjunktion wahrscheinlicher sein als
        // ein Quantor+Rollenname => am Besten in einer ersten Stufe entweder
        // Disjunktion, Konjunktion, Existenzquantor oder Allquantor auswählen
        FlatABox abox = FlatABox.getInstance();
        int nr = rand.nextInt(4);
        if(nr==0)
            return new Disjunction();
        else if(nr==1)
            return new Conjunction();
        else {
            // Rollenname generieren
            int nr2 = rand.nextInt(abox.roles.size());
            // Existenz- oder Allquantor
            // TODO: auch hier Performance verbessern, indem toArray() nur
            // einmal ausgeführt wird
            if(nr==3)
                return new Exists((String)abox.roles.toArray()[nr2]);
            else
                return new All((String)abox.roles.toArray()[nr2]);
        }
    }
    */
    
    /**
     * Create a program using the full method.
     * @param depth Depth of the tree.
     * @return The created program.
     */
    public static Program createFullRandomProgram(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, int depth, boolean adc) {
    	if(adc) {
    		// erster Baum Hauptbaum, zweiter Baum ADC
    		return createProgram(learningProblem, createFullRandomTree(learningProblem, rs, depth, true),
    				createFullRandomTree(learningProblem, rs, depth, false));
    	}
    	else
    		return createProgram(learningProblem, createFullRandomTree(learningProblem, rs, depth, false));
    }

    private static Description createFullRandomTree(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, int depth, boolean useADC) {
        // FlatABox abox = FlatABox.getInstance();
        int numberOfRoles = rs.getObjectProperties().size(); //  abox.roles.size();
        
        if (depth > 1) {
            int nr = rand.nextInt(3+2*numberOfRoles);
            // System.out.println(nr);
            // Node node = createNonLeafNodeEqualProp();
        	// Concept node = pickFunctionSymbol();
            Description child1 = createFullRandomTree(learningProblem, rs, depth-1, useADC);
            if(nr == 0 || nr == 1) {
            	Description child2 = createFullRandomTree(learningProblem, rs, depth-1, useADC);
            	if(nr == 0) {
//            		if(useMultiStructures)
            			return new Union(child1,child2);
//            		else
//            			return new Disjunction(child1,child2);
            	} else {
//            		if(useMultiStructures)
            			return new Intersection(child1, child2);
//            		else
//            			return new Conjunction(child1, child2);
            	}
            } else if(nr==2) {
            	return new Negation(child1);
            } else if(nr - 3 < numberOfRoles)
            	return new ObjectSomeRestriction(rs.getAtomicRolesList().get(nr-3),child1);
            else
            	return new ObjectAllRestriction(rs.getAtomicRolesList().get(nr-3-numberOfRoles),child1);
            
            /*
            if(node instanceof Disjunction || node instanceof Conjunction) {
                node.addChild(createFullRandomTree(depth-1, useADC));
                node.addChild(createFullRandomTree(depth-1, useADC));
            }
            if(node instanceof Exists || node instanceof All || node instanceof Negation) {
                node.addChild(createFullRandomTree(depth-1, useADC));
            }
            */
            // return node;
        } else {
            // return createLeafNode(useADC);
        	return pickTerminalSymbol(learningProblem, rs, useADC);
        }
    }

    /**
     * Create a program using the grow method.
     * @param depth The maximum depth of the program tree.
     * @return The created program.
     */
    public static Program createGrowRandomProgram(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, int depth, boolean adc) {
    	if(adc) {
    		// erster Baum Hauptbaum, zweiter Baum ADC
    		return createProgram(learningProblem, createGrowRandomTree(learningProblem,rs,depth,true),
    				createGrowRandomTree(learningProblem,rs,depth,false));
    	}
    	else
    		return createProgram(learningProblem, createGrowRandomTree(learningProblem, rs, depth,false));    	
    }

    public static Description createGrowRandomTree(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs, int depth, boolean useADC) {
    	/*
        private static Concept pickAlphabetSymbol(boolean useADC) {
            FlatABox abox = FlatABox.getInstance();
            int numberOfConcepts = abox.concepts.size();
            int numberOfRoles = abox.roles.size();
            int numberOfAlphabetSymbols = numberOfConcepts + 2*numberOfRoles + 5;
            if(useADC)
            	numberOfAlphabetSymbols += 1;
            
            int nr = rand.nextInt(numberOfAlphabetSymbols);
            
            if(nr==0)
            	return new Top();
            else if(nr==1)
            	return new Bottom();
            // ADC bekommt die h�chste Nummer, die nur in diesem Fall m�glich ist
            else if(nr==numberOfConcepts + 2*numberOfRoles + 5) 
            	return new ADC(); 
            else if(nr>=2 && nr < numberOfConcepts+2)
            	return new AtomicConcept(abox.getConcept(nr-2));
            else if(nr==numberOfConcepts+2)
            	return new Conjunction();
            else if(nr==numberOfConcepts+3)
            	return new Disjunction();
            else if(nr==numberOfConcepts+4)
            	return new Negation();
            else if(nr>=numberOfConcepts+5 && nr<numberOfConcepts+5+numberOfRoles)
            	return new Exists(new AtomicRole(abox.getRole(nr-numberOfConcepts-5)));
            else
            	return new All(new AtomicRole(abox.getRole(nr-numberOfConcepts-5-numberOfRoles)));
        }    	
        */
    	
        // FlatABox abox = FlatABox.getInstance();
        int numberOfConcepts = rs.getNamedClasses().size();
        int numberOfRoles = rs.getObjectProperties().size();
        // TODO: ev. größere Wahrscheinlichkeit für Konjunktion/Disjunktion (?),
        // mit größerer Konzept-, und Rollenanzahl kommen die sonst kaum noch vor
        int numberOfAlphabetSymbols = numberOfConcepts + 2*numberOfRoles + 5; //7;// 5;
        if(useADC)
        	numberOfAlphabetSymbols += 1;        
        
        int nr = rand.nextInt(numberOfAlphabetSymbols);
        
    	// TODO: ev. alternative Erzeugung erlauben, bei der alle Knoten
    	// gleichwahrscheinlich sind (ist hier eindeutig nicht der Fall);
    	// beide Varianten haben Vor- und Nachteile
        if (depth > 1) {
        	// TODO: ev, sollte man diese Wahrsceinlichkeit konfigurierbar machen
        	// 0.5 sieht auf den ersten Blick vern�nftig aus, aber bedeutet auch, dass
        	// 50% aller B�ume Tiefe 1 haben, d.h. B�ume nicht sehr tief werden
        	
        	// ALTER CODE
            // if(rand.nextDouble()>=0.5) // >=0.5)
            //    return createLeafNode(useADC);
            //else {
            //    Node node = createNonLeafNodeEqualProp();
            //    if(node instanceof Disjunction || node instanceof Conjunction) {
            //        node.addChild(createGrowRandomTree(depth-1, useADC));
            //        node.addChild(createGrowRandomTree(depth-1, useADC));
            //    }
            //    if(node instanceof Exists || node instanceof All || node instanceof Negation) {
            //        node.addChild(createGrowRandomTree(depth-1, useADC));
            //    }
            //    return node;                
            // }
        	
        	// NEUER CODE
        	// Concept node = pickAlphabetSymbol(useADC);
        	
            if(nr==0)
            	return new Thing();
            else if(nr==1)
            	return new Nothing();
            // ADC bekommt die h�chste Nummer, die nur in diesem Fall m�glich ist
            else if(nr==numberOfConcepts + 2*numberOfRoles + 5) 
            	return new ADC(); 
            else if(nr>=2 && nr < numberOfConcepts+2)
            	return (NamedClass)rs.getAtomicConceptsList().get(nr-2).clone();
            else if(nr==numberOfConcepts+2) {
//            	if(useMultiStructures)
            		return new Intersection(createGrowRandomTree(learningProblem, rs, depth-1, useADC),createGrowRandomTree(learningProblem, rs, depth-1, useADC));
//            	else
//            		return new Conjunction(createGrowRandomTree(learningProblem, depth-1, useADC),createGrowRandomTree(learningProblem, depth-1, useADC));
            } else if(nr==numberOfConcepts+3) {
//            	if(useMultiStructures)
            		return new Union(createGrowRandomTree(learningProblem, rs, depth-1, useADC),createGrowRandomTree(learningProblem, rs, depth-1, useADC));
//            	else
//            		return new Disjunction(createGrowRandomTree(learningProblem, depth-1, useADC),createGrowRandomTree(learningProblem, depth-1, useADC));
            } else if(nr==numberOfConcepts+4)
            	return new Negation(createGrowRandomTree(learningProblem, rs, depth-1, useADC));
            else if(nr>=numberOfConcepts+5 && nr<numberOfConcepts+5+numberOfRoles)
            	return new ObjectSomeRestriction(rs.getAtomicRolesList().get(nr-numberOfConcepts-5),createGrowRandomTree(learningProblem, rs, depth-1, useADC));
            else
            	return new ObjectAllRestriction(rs.getAtomicRolesList().get(nr-numberOfConcepts-5-numberOfRoles),createGrowRandomTree(learningProblem, rs, depth-1, useADC));        	
        	
            /*
        	if(node instanceof Disjunction || node instanceof Conjunction) {
                node.addChild(createGrowRandomTree(depth-1, useADC));
                node.addChild(createGrowRandomTree(depth-1, useADC));
            }
            if(node instanceof Exists || node instanceof All || node instanceof Negation) {
                node.addChild(createGrowRandomTree(depth-1, useADC));
            }
            */
            // return node;
        } else {
            // return createLeafNode(useADC);
        	return pickTerminalSymbol(learningProblem, rs, useADC);
        }
    }

    public static void checkPrograms(Program[] progs) {
        for(Program prog : progs) {
        	GPUtilities.checkProgram(prog);
        }
    }
    
    public static boolean checkProgram(Program prog) {
        return checkTree(prog.getTree(), true);
    }
        
    public static boolean checkTree(Description node, boolean isRootNode) {
        if(isRootNode && node.getParent()!=null) {
        	System.out.println("inconsistent root");
            return false;
        }
        
        // Kinder �berpr�fen
        for(Description child : node.getChildren()) {
            if(!child.getParent().equals(node)) {
                System.out.println("inconsistent tree " + node + " (child " + child + ")");
                return false;
            }       
        }
        
        return true;
    }
}
