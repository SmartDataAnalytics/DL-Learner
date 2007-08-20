package org.dllearner.algorithms.gp;



import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;

import org.dllearner.Config;
import org.dllearner.LearningProblem;
import org.dllearner.Main;
import org.dllearner.Score;
import org.dllearner.ScoreThreeValued;
import org.dllearner.dl.All;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.AtomicRole;
import org.dllearner.dl.Bottom;
import org.dllearner.dl.Concept;
import org.dllearner.dl.Conjunction;
import org.dllearner.dl.Disjunction;
import org.dllearner.dl.Exists;
import org.dllearner.dl.FlatABox;
import org.dllearner.dl.Individual;
import org.dllearner.dl.MultiConjunction;
import org.dllearner.dl.MultiDisjunction;
import org.dllearner.dl.Negation;
import org.dllearner.dl.Top;
import org.dllearner.reasoning.FastRetrieval;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.SortedSetTuple;


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
    
    private static Score calculateFitness(LearningProblem learningProblem, Concept hypothesis) {
    	return calculateFitness(learningProblem, hypothesis, null);
    }
    
    // TODO: eventuell darueber nachdenken diese return-type-Sache zu entfernen
    // Alternative: man bezieht sie in die zentralen Score-Berechnungen mit ein
    // (macht aber nicht so viel Sinn, da man das bei richtigen Reasoning-Algorithmen
    // ohnehin mit einer Erweiterung der Wissensbasis um die Inklusion Target SUBSETOF ReturnType
    // erschlagen kann)
	private static Score calculateFitness(LearningProblem learningProblem, Concept hypothesis, Concept adc) {
		Concept extendedHypothesis;
		
		if (!Config.returnType.equals("")) {
			System.out.println("return type");
			
			// newRoot.addChild(new AtomicConcept(Config.returnType));
			// newRoot.addChild(hypothesis);
			Concept newRoot;
			if(Config.GP.useMultiStructures)
				newRoot = new MultiConjunction(new AtomicConcept(Config.returnType),hypothesis);
			else
				newRoot = new Conjunction(new AtomicConcept(Config.returnType),hypothesis);			
			// parent wieder auf null setzen, damit es nicht inkonsistent wird
			// TODO: ist nicht wirklich elegant und auch inkompatibel mit
			// dem Hill-Climbing-Operator
			hypothesis.setParent(null);
			extendedHypothesis = newRoot;
		} else
			extendedHypothesis = hypothesis;		
		
		Score score;
		if(Config.GP.adc)
			score = learningProblem.computeScore(extendedHypothesis, adc);
		else
			score = learningProblem.computeScore(extendedHypothesis);
		
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
    
	public static Program createProgram(LearningProblem learningProblem, Concept mainTree) {
		return new Program(calculateFitness(learningProblem, mainTree), mainTree);
	}	
	
	private static Program createProgram(LearningProblem learningProblem, Concept mainTree, Concept adc) {
		return new Program(calculateFitness(learningProblem, mainTree,adc), mainTree, adc);
	}
	
    /**
     * Perform a point mutation on the given program.
     * @param p The program to be mutated.
     */
    public static Program mutation(LearningProblem learningProblem, Program p) {
    	mutation++;
    	if(Config.GP.adc) {
    		// TODO: hier kann man noch mehr Feinabstimmung machen, d.h.
    		// Mutation abh�ngig von Knotenanzahl
    		if(Math.random()<0.5) {
    			Concept mainTree = mutation(learningProblem, p.getTree(),true);
    			Concept adc = p.getAdc();
    			Score score = calculateFitness(learningProblem,mainTree,adc);
    			return new Program(score, mainTree, adc);
    		}
    		else {
    			Concept mainTree = p.getTree();
    			Concept adc = mutation(learningProblem, p.getAdc(),false);
    			Score score = calculateFitness(learningProblem,mainTree,adc);
    			return new Program(score, mainTree, adc);    			
    		}
    	} else {
    		Concept tree = mutation(learningProblem, p.getTree(),false);
    		Score score = calculateFitness(learningProblem, tree);
            return new Program(score, tree);
    	}
    }

    private static Concept mutation(LearningProblem learningProblem, Concept tree, boolean useADC) {
    	// auch bei Mutation muss darauf geachtet werden, dass 
    	// Baum nicht modifiziert wird (sonst w�rde man automatisch auch
    	// andere "selected individuals" modifizieren)
    	Concept t = (Concept) tree.clone();
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
            Concept st = t.getSubtree(randNr);
            Concept stParent = st.getParent();
            stParent.getChildren().remove(st);
            Concept treeNew = createGrowRandomTree(learningProblem,3, useADC);
            stParent.addChild(treeNew);
        } else
        	// return createLeafNode(useADC);
        	return pickTerminalSymbol(learningProblem,useADC);
        return t;
    }

    private static void swapSubtrees(Concept t1, Concept t2) {
        if (t1.getParent() != null && t2.getParent() != null) {
            // handling children
            Concept t1Parent = t1.getParent();
            Concept t2Parent = t2.getParent();
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
    public static Program[] crossover(LearningProblem learningProblem, Program p1, Program p2) {
    	crossover++;
    	if(Config.GP.adc) {
    		Concept[] pt;
    		Program result[] = new Program[2];
    		
    		// es wird entweder ADC oder Hauptbaum einem Crossover
    		// unterzogen und dann ein neues Programm erstellt
    		if(Math.random()<0.5) {
    			pt = crossover(p1.getTree(), p2.getTree()); 
    			result[0] = createProgram(learningProblem, pt[0],p1.getAdc());
                result[1] = createProgram(learningProblem, pt[1],p2.getAdc());
    		} else {
    			pt = crossover(p1.getAdc(), p2.getAdc());
    			result[0] = createProgram(learningProblem, p1.getTree(),pt[0]);
                result[1] = createProgram(learningProblem, p2.getTree(),pt[1]);
    		}
            return result;      		
    	} else {
            Concept[] pt = crossover(p1.getTree(), p2.getTree());
            Program result[] = new Program[2];
            result[0] = createProgram(learningProblem,pt[0]);
            result[1] = createProgram(learningProblem,pt[1]);
            return result;    		
    	}
    }

    private static Concept[] crossover(Concept tree1, Concept tree2) {
        Concept tree1cloned = (Concept) tree1.clone();
        Concept tree2cloned = (Concept) tree2.clone();

        Concept[] results = new Concept[2];
        int rand1 = rand.nextInt(tree1.getNumberOfNodes());
        int rand2 = rand.nextInt(tree2.getNumberOfNodes());
        Concept t1 = tree1cloned.getSubtree(rand1);
        Concept t2 = tree2cloned.getSubtree(rand2);

        if (t1.isRoot() && !t2.isRoot()) {
            Concept t2Parent = t2.getParent();
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
            Concept t1Parent = t1.getParent();
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
    public static Program hillClimbing(LearningProblem learningProblem, Program p) {
    	hillClimbing++;
    	// checken, ob Bedingungen f�r hill-climbing erf�llt sind
    	if(!learningProblem.getReasoningService().getReasonerType().equals(ReasonerType.FAST_RETRIEVAL)
    			|| !(p.getScore() instanceof ScoreThreeValued)) {
    		throw new Error("Hill climbing can only be used with the fast-retrieval-algorithm on a three valued learning problem.");
    	}
    	
    	// SortedSetTuple s = new SortedSetTuple(p.getScore().getDefPosSet(),p.getScore().getDefNegSet());
    	// Knoten kopieren, damit er sich nicht ver�ndert (es wird sonst der
    	// parent-Link ver�ndert)
    	Concept treecloned = (Concept) p.getTree().clone();
    	if(Config.GP.adc)
    		return createProgram(learningProblem,hillClimbing(learningProblem,treecloned,(ScoreThreeValued)p.getScore()),p.getAdc());
    	else
    		return createProgram(learningProblem,hillClimbing(learningProblem,treecloned,(ScoreThreeValued)p.getScore()));
    }
    
    // one-step hill-climbing
    // es ist zwar von der Implementierung her sehr aufw�ndig die besten
    // Alternativen zu speichern und dann ein Element zuf�llig auszuw�hlen,
    // aber w�rde man das nicht machen, dann w�re das ein starker Bias
    // zu z.B. Disjunktion (weil die als erstes getestet wird)
    private static Concept hillClimbing(LearningProblem learningProblem, Concept node, ScoreThreeValued score) {
    	SortedSetTuple<Individual> tuple = new SortedSetTuple<Individual>(score.getPosClassified(),score.getNegClassified());
    	SortedSetTuple<String> stringTuple = Helper.getStringTuple(tuple);
    	// FlatABox abox = FlatABox.getInstance();
    	Concept returnNode = node;
    	// damit stellen wir sicher, dass nur Konzepte in die Auswahl
    	// genommen werden, die besser klassifizieren als das �bergebene
    	// Konzept (falls das nicht existiert, dann hill climbing = reproduction)
    	double bestScore = score.getScore()+Config.accuracyPenalty/2;
    	Map<Integer,List<String>> bestNeighbours = new TreeMap<Integer,List<String>>();
    	Score tmpScore;
    	SortedSetTuple<String> tmp, tmp2;
    	// FlatABox abox = ((FastRetrievalReasoner)learningProblem.getReasoner().getFastRetrieval().getAbox();
    	FlatABox abox = Main.getFlatAbox();
    	
    	// TODO: testen, ob aktuelles Konzept zu speziell bzw. allgemein ist;
    	// dann kann man das effizienter implementieren
    	
    	// Tests f�r Disjunktion bzw. Konjunktion
    	for(String concept : abox.concepts) {
    	// for(AtomicConcept ac : learningProblem.getReasoner().getAtomicConcepts())
    		tmp = new SortedSetTuple<String>(abox.atomicConceptsPos.get(concept),abox.atomicConceptsNeg.get(concept));
    		// TODO: double retrieval nutzen
    		
    		tmp2 = FastRetrieval.calculateDisjunctionSets(stringTuple, tmp);
    		tmpScore = getScore(node.getLength()+2, learningProblem,Helper.getIndividualSet(tmp2.getPosSet()),Helper.getIndividualSet(tmp2.getNegSet()));
    		if(tmpScore.getScore()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,1,concept,false);
    		else if(tmpScore.getScore()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,1,concept,true);
    		
    		tmp2 = FastRetrieval.calculateConjunctionSets(stringTuple, tmp);
    		tmpScore = getScore(node.getLength()+2,learningProblem,Helper.getIndividualSet(tmp2.getPosSet()),Helper.getIndividualSet(tmp2.getNegSet()));
    		if(tmpScore.getScore()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,2,concept,false);
    		else if(tmpScore.getScore()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,2,concept,true);    		
    	}
    	
    	// Tests f�r All und Exists
    	for(String role : abox.roles) {
    		tmp = FastRetrieval.calculateAllSet(abox,role,stringTuple);
    		tmpScore = getScore(node.getLength()+2,learningProblem,Helper.getIndividualSet(tmp.getPosSet()),Helper.getIndividualSet(tmp.getNegSet()));
    		if(tmpScore.getScore()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,3,role,false);
    		else if(tmpScore.getScore()>bestScore)
    			bestNeighbours = updateMap(bestNeighbours,3,role,true);    		

    		tmp = FastRetrieval.calculateExistsSet(abox,role,stringTuple);
    		tmpScore = getScore(node.getLength()+2,learningProblem,Helper.getIndividualSet(tmp.getPosSet()),Helper.getIndividualSet(tmp.getNegSet()));
    		if(tmpScore.getScore()==bestScore)
    			bestNeighbours = updateMap(bestNeighbours,4,role,false);
    		else if(tmpScore.getScore()>bestScore)
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
    		if(Config.GP.useMultiStructures)
    			returnNode = new MultiDisjunction(new AtomicConcept(name),node);
    		else
    			returnNode = new Disjunction(new AtomicConcept(name),node);
    	// wegen else if schlie�en sich die F�lle gegenseitig aus
    	} else if(nr<sizeSum2) {
    		name = bestNeighbours.get(2).get(nr-sizeSum1);
    		// returnNode = new Conjunction();
    		// returnNode.addChild(new AtomicConcept(name));    		
    		// returnNode.addChild(node);
    		if(Config.GP.useMultiStructures)
    			returnNode = new MultiConjunction(new AtomicConcept(name),node);
    		else
    			returnNode = new Conjunction(new AtomicConcept(name),node);
    	} else if(nr<sizeSum3) {
    		name = bestNeighbours.get(3).get(nr-sizeSum2);
    		// returnNode = new Exists(new AtomicRole(name));
    		// returnNode.addChild(node);
    		returnNode = new Exists(new AtomicRole(name),node);
    	} else {
    		name = bestNeighbours.get(4).get(nr-sizeSum3);
    		// returnNode = new All(new AtomicRole(name));
    		// returnNode.addChild(node);   
    		returnNode = new All(new AtomicRole(name),node);
    	}
    		
    	return returnNode;
    	}
    }
    
    private static ScoreThreeValued getScore(int conceptLength, LearningProblem learningProblem, SortedSet<Individual> posClassified, SortedSet<Individual> negClassified) {
    	// es muss hier die Helper-Methode verwendet werden, sonst werden
    	// Individuals gel�scht !!
    	SortedSet<Individual> neutClassified = Helper.intersection(learningProblem.getReasoningService().getIndividuals(),posClassified); 
    	// learningProblem.getReasoner().getIndividuals();
    	// neutClassified.retainAll(posClassified);
    	neutClassified.retainAll(negClassified);
    	
    	return new ScoreThreeValued(conceptLength, posClassified, neutClassified, negClassified, learningProblem.getPositiveExamples(),learningProblem.getNeutralExamples(),learningProblem.getNegativeExamples());
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
    
    private static Concept pickTerminalSymbol(LearningProblem learningProblem, boolean useADC) {
        // FlatABox abox = FlatABox.getInstance();
        int nr;
        int nrOfConcepts = learningProblem.getReasoningService().getAtomicConcepts().size();
        
        // ein Blattknoten kann folgendes sein:
        // Top, Bottom, Konzept => alles am Besten gleichwahrscheinlich         
        if(useADC)
        	nr = rand.nextInt(3+nrOfConcepts);
        else
        	nr = rand.nextInt(2+nrOfConcepts);
        	
        if(nr==0)
        	return new Top();
        else if(nr==1)
            return new Bottom();
        // die Zahl kann nur vorkommen, wenn ADC aktiviert ist
        else if(nr==2+nrOfConcepts) {
        	// System.out.println("== ADC 2 ==");
        	return new ADC();
         }
        else
            return (AtomicConcept) learningProblem.getReasoningService().getAtomicConceptsList().get(nr-2).clone();    	  	
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
    public static Program createFullRandomProgram(LearningProblem learningProblem, int depth) {
    	if(Config.GP.adc) {
    		// erster Baum Hauptbaum, zweiter Baum ADC
    		return createProgram(learningProblem, createFullRandomTree(learningProblem, depth, true),
    				createFullRandomTree(learningProblem, depth, false));
    	}
    	else
    		return createProgram(learningProblem, createFullRandomTree(learningProblem, depth, false));
    }

    private static Concept createFullRandomTree(LearningProblem learningProblem, int depth, boolean useADC) {
        // FlatABox abox = FlatABox.getInstance();
        int numberOfRoles = learningProblem.getReasoningService().getAtomicRoles().size(); //  abox.roles.size();
        
        if (depth > 1) {
            int nr = rand.nextInt(3+2*numberOfRoles);
            // System.out.println(nr);
            // Node node = createNonLeafNodeEqualProp();
        	// Concept node = pickFunctionSymbol();
            Concept child1 = createFullRandomTree(learningProblem, depth-1, useADC);
            if(nr == 0 || nr == 1) {
            	Concept child2 = createFullRandomTree(learningProblem, depth-1, useADC);
            	if(nr == 0) {
            		if(Config.GP.useMultiStructures)
            			return new MultiDisjunction(child1,child2);
            		else
            			return new Disjunction(child1,child2);
            	} else {
            		if(Config.GP.useMultiStructures)
            			return new MultiConjunction(child1, child2);
            		else
            			return new Conjunction(child1, child2);
            	}
            } else if(nr==2) {
            	return new Negation(child1);
            } else if(nr - 3 < numberOfRoles)
            	return new Exists(learningProblem.getReasoningService().getAtomicRolesList().get(nr-3),child1);
            else
            	return new All(learningProblem.getReasoningService().getAtomicRolesList().get(nr-3-numberOfRoles),child1);
            
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
        	return pickTerminalSymbol(learningProblem, useADC);
        }
    }

    /**
     * Create a program using the grow method.
     * @param depth The maximum depth of the program tree.
     * @return The created program.
     */
    public static Program createGrowRandomProgram(LearningProblem learningProblem, int depth) {
    	if(Config.GP.adc) {
    		// erster Baum Hauptbaum, zweiter Baum ADC
    		return createProgram(learningProblem, createGrowRandomTree(learningProblem,depth,true),
    				createGrowRandomTree(learningProblem,depth,false));
    	}
    	else
    		return createProgram(learningProblem, createGrowRandomTree(learningProblem, depth,false));    	
    }

    private static Concept createGrowRandomTree(LearningProblem learningProblem, int depth, boolean useADC) {
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
        int numberOfConcepts = learningProblem.getReasoningService().getAtomicConcepts().size();
        int numberOfRoles = learningProblem.getReasoningService().getAtomicRoles().size();
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
            	return new Top();
            else if(nr==1)
            	return new Bottom();
            // ADC bekommt die h�chste Nummer, die nur in diesem Fall m�glich ist
            else if(nr==numberOfConcepts + 2*numberOfRoles + 5) 
            	return new ADC(); 
            else if(nr>=2 && nr < numberOfConcepts+2)
            	return (AtomicConcept)learningProblem.getReasoningService().getAtomicConceptsList().get(nr-2).clone();
            else if(nr==numberOfConcepts+2) {
            	if(Config.GP.useMultiStructures)
            		return new MultiConjunction(createGrowRandomTree(learningProblem, depth-1, useADC),createGrowRandomTree(learningProblem, depth-1, useADC));
            	else
            		return new Conjunction(createGrowRandomTree(learningProblem, depth-1, useADC),createGrowRandomTree(learningProblem, depth-1, useADC));
            } else if(nr==numberOfConcepts+3) {
            	if(Config.GP.useMultiStructures)
            		return new MultiDisjunction(createGrowRandomTree(learningProblem, depth-1, useADC),createGrowRandomTree(learningProblem, depth-1, useADC));
            	else
            		return new Disjunction(createGrowRandomTree(learningProblem, depth-1, useADC),createGrowRandomTree(learningProblem, depth-1, useADC));
            } else if(nr==numberOfConcepts+4)
            	return new Negation(createGrowRandomTree(learningProblem, depth-1, useADC));
            else if(nr>=numberOfConcepts+5 && nr<numberOfConcepts+5+numberOfRoles)
            	return new Exists(learningProblem.getReasoningService().getAtomicRolesList().get(nr-numberOfConcepts-5),createGrowRandomTree(learningProblem,depth-1, useADC));
            else
            	return new All(learningProblem.getReasoningService().getAtomicRolesList().get(nr-numberOfConcepts-5-numberOfRoles),createGrowRandomTree(learningProblem,depth-1, useADC));        	
        	
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
        	return pickTerminalSymbol(learningProblem, useADC);
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
        
    public static boolean checkTree(Concept node, boolean isRootNode) {
        if(isRootNode && node.getParent()!=null) {
        	System.out.println("inconsistent root");
            return false;
        }
        
        // Kinder �berpr�fen
        for(Concept child : node.getChildren()) {
            if(!child.getParent().equals(node)) {
                System.out.println("inconsistent tree " + node + " (child " + child + ")");
                return false;
            }       
        }
        
        return true;
    }
}
