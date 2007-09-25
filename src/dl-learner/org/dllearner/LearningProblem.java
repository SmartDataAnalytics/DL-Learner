package org.dllearner;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.Config.Refinement;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.Negation;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.SortedSetTuple;

/**
 * Der Score-Calculator nimmt Konzepte (= m�gliche L�sungen des Lernproblems)
 * entgegen und gibt Score-Objekte zur�ck.
 * 
 * @author jl
 * 
 */
public class LearningProblem {

	public enum LearningProblemType {POSITIVE_ONLY, TWO_VALUED, THREE_VALUED};
	
	private ReasoningService reasoningService;

	private SortedSet<Individual> positiveExamples;
	private SortedSet<Individual> negativeExamples;
	private SortedSet<Individual> neutralExamples;
	// positive and negative examples
	private SortedSet<Individual> posNegExamples;

	public LearningProblem(ReasoningService reasoningService,
			SortedSet<Individual> positiveExamples,
			SortedSet<Individual> negativeExamples) {
		this.reasoningService = reasoningService;
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
		
		posNegExamples = Helper.union(positiveExamples, negativeExamples);
		
		// neutrale Beispiele berechnen (nur bei three valued interessant)
		// auch hier aufpassen, dass man die Mengen immer kopiert
		neutralExamples = Helper.intersection(reasoningService.getIndividuals(),positiveExamples);
		neutralExamples.retainAll(negativeExamples);
		
		// System.out.println(positiveExamples);
		// System.out.println(negativeExamples);
	}

	// gibt -1 zurück, falls Konzept too weak;
	// ansonsten die negativen Beispiele, die aus Konzept folgen
	// wird verwendet für top-down-approach
	// TODO: hier sind vielleicht noch Effizienzverbesserungen gegenüber dem reinen berechnen
	// mit computeScore() drin [ist bis jetzt noch mehr oder weniger rüberkopiert]
	public int coveredNegativeExamplesOrTooWeak(Concept concept) {
		if(!Config.learningProblemType.equals(LearningProblemType.TWO_VALUED))
			throw new RuntimeException("Can use this method only for two valued learning problem.");
		
		if(Config.useRetrievalForClassification) {
			//if(Config.reasonerType == ReasonerType.KAON2 || Config.reasonerType == ReasonerType.FAST_RETRIEVAL) {
				SortedSet<Individual> posClassified = reasoningService.retrieval(concept);
				SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
				SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
				// die Menge wird schrittweise konstruiert um Operationen mit der potentiell
				// großen Menge aller Individuals zu vermeiden
				for(Individual posExample : positiveExamples) {
					if(!posClassified.contains(posExample))
						posAsNeg.add(posExample);
				}
				
				// es werden nicht alle positiven Beispiele auch positiv klassifiziert
				if(posAsNeg.size()>0)
					return -1;
				else
					return negAsPos.size();
			//} else
			//	throw new Error("LP not completely implemented");
		} else {
			if(Refinement.useDIGMultiInstanceChecks != Config.Refinement.UseDIGMultiInstanceChecks.NEVER) {
				// Option wird nur bei DIG-Reasoner genutzt, ansonsten einfach ignoriert
				if(Config.reasonerType == ReasonerType.DIG) {
					// two checks
					if(Config.Refinement.useDIGMultiInstanceChecks == Config.Refinement.UseDIGMultiInstanceChecks.TWOCHECKS) {
						Set<Individual> s = reasoningService.instanceCheck(concept, positiveExamples);
						// if the concept is too weak, then do not query negative examples
						if(s.size()!=positiveExamples.size())
							return -1;
						else {
							s = reasoningService.instanceCheck(concept, negativeExamples);
							return s.size();
						}
					// one check
					} else {
						Set<Individual> s = reasoningService.instanceCheck(concept, posNegExamples);
						// test whether all positive examples are covered
						if(s.containsAll(positiveExamples))
							return s.size() - positiveExamples.size();
						else
							return -1;
					}
				}
			}
				
			if(Config.reasonerType != ReasonerType.FAST_RETRIEVAL) {
				SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
				SortedSet<Individual> negAsPos = new TreeSet<Individual>();
				for(Individual example : positiveExamples) {
					if(!reasoningService.instanceCheck(concept, example))
						posAsNeg.add(example);
				}
				for(Individual example : negativeExamples) {
					if(reasoningService.instanceCheck(concept, example))
						negAsPos.add(example);
				}
				
				if(posAsNeg.size()>0)
					return -1;
				else
					return negAsPos.size();
			}
		}
		throw new Error("LP not completely implemented");		
	}
	
	public Score computeScore(Concept concept) {
		if(Config.learningProblemType.equals(LearningProblemType.TWO_VALUED))
			return computeScoreTwoValued(concept);
		else
			return computeScoreThreeValued(concept);
	}
	
	public Score computeScore(Concept concept, Concept adc) {
		if(Config.learningProblemType.equals(LearningProblemType.TWO_VALUED))
			throw new Error("LP not completely implemented");
		else
			return computeScoreThreeValued(concept, adc);
	}
	
	private Score computeScoreTwoValued(Concept concept) {
		if(Config.useRetrievalForClassification) {
			if(Config.reasonerType == ReasonerType.DIG || Config.reasonerType == ReasonerType.KAON2 || Config.reasonerType == ReasonerType.FAST_RETRIEVAL) {
				SortedSet<Individual> posClassified = reasoningService.retrieval(concept);
				SortedSet<Individual> posAsPos = Helper.intersection(positiveExamples, posClassified);
				SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
				SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
				// die Menge wird schrittweise konstruiert um Operationen mit der potentiell
				// großen Menge aller Individuals zu vermeiden
				for(Individual posExample : positiveExamples) {
					if(!posClassified.contains(posExample))
						posAsNeg.add(posExample);
				}
				SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
				for(Individual negExample : negativeExamples) {
					if(!posClassified.contains(negExample))
						negAsNeg.add(negExample);
				}				
				return new ScoreTwoValued(concept.getLength(), posAsPos, posAsNeg, negAsPos, negAsNeg);
			} else
				throw new Error("LP not completely implemented");
		// instance checks zur Klassifikation
		} else {
			if(Config.reasonerType != ReasonerType.FAST_RETRIEVAL) {
				SortedSet<Individual> posAsPos = new TreeSet<Individual>();					
				SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
				SortedSet<Individual> negAsPos = new TreeSet<Individual>();
				SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
				for(Individual example : positiveExamples) {
					if(reasoningService.instanceCheck(concept, example))
						posAsPos.add(example);
					else
						posAsNeg.add(example);
				}
				for(Individual example : negativeExamples) {
					if(reasoningService.instanceCheck(concept, example))
						negAsPos.add(example);
					else
						negAsNeg.add(example);
				}
				return new ScoreTwoValued(concept.getLength(),posAsPos, posAsNeg, negAsPos, negAsNeg);
			} else
				throw new Error("LP not completely implemented");
		}
	}
	
	private Score computeScoreThreeValued(Concept concept) {
    	// Anfragen an Reasoner
    	if(Config.useRetrievalForClassification) {
    		if(Config.reasonerType == ReasonerType.FAST_RETRIEVAL) {
        		SortedSetTuple<Individual> tuple = reasoningService.doubleRetrieval(concept);
        		// this.defPosSet = tuple.getPosSet();
        		// this.defNegSet = tuple.getNegSet();  
        		SortedSet<Individual> neutClassified = Helper.intersectionTuple(reasoningService.getIndividuals(),tuple);
        		return new ScoreThreeValued(concept.getLength(),tuple.getPosSet(),neutClassified,tuple.getNegSet(),positiveExamples,neutralExamples,negativeExamples);
    		} else if(Config.reasonerType == ReasonerType.KAON2) {
    			SortedSet<Individual> posClassified = reasoningService.retrieval(concept);
    			SortedSet<Individual> negClassified = reasoningService.retrieval(new Negation(concept));
    			SortedSet<Individual> neutClassified = Helper.intersection(reasoningService.getIndividuals(),posClassified);
    			neutClassified.retainAll(negClassified);
    			return new ScoreThreeValued(concept.getLength(), posClassified,neutClassified,negClassified,positiveExamples,neutralExamples,negativeExamples);     			
    		} else
    			throw new Error("score cannot be computed in this configuration");
    	} else {
    		if(Config.reasonerType == ReasonerType.KAON2) {
    			if(Config.penalizeNeutralExamples)
    				throw new Error("It does not make sense to use single instance checks when" +
    						"neutral examples are penalized. Use Retrievals instead.");
    				
    			// TODO: umschreiben in instance checks
    			SortedSet<Individual> posClassified = new TreeSet<Individual>();
    			SortedSet<Individual> negClassified = new TreeSet<Individual>();
    			// Beispiele durchgehen
    			// TODO: Implementierung ist ineffizient, da man hier schon in Klassen wie
    			// posAsNeut, posAsNeg etc. einteilen k�nnte; so wird das extra in der Score-Klasse
    			// gemacht; bei wichtigen Benchmarks des 3-wertigen Lernproblems m�sste man das
    			// umstellen
    			// pos => pos
    			for(Individual example : positiveExamples) {
    				if(reasoningService.instanceCheck(concept, example))
    					posClassified.add(example);
    			}
    			// neg => pos
    			for(Individual example: negativeExamples) {
    				if(reasoningService.instanceCheck(concept, example))
    					posClassified.add(example);
    			}
    			// pos => neg
    			for(Individual example : positiveExamples) {
    				if(reasoningService.instanceCheck(new Negation(concept), example))
    					negClassified.add(example);
    			}
    			// neg => neg
    			for(Individual example : negativeExamples) {
    				if(reasoningService.instanceCheck(new Negation(concept), example))
    					negClassified.add(example);
    			}    			
    			
    			SortedSet<Individual> neutClassified = Helper.intersection(reasoningService.getIndividuals(),posClassified);
    			neutClassified.retainAll(negClassified);
    			return new ScoreThreeValued(concept.getLength(), posClassified,neutClassified,negClassified,positiveExamples,neutralExamples,negativeExamples); 		
    		} else
    			throw new Error("score cannot be computed in this configuration");
    	}
	}
	
	private Score computeScoreThreeValued(Concept concept, Concept adc) {
    	if(!Config.useRetrievalForClassification &&
    			Config.reasonerType != ReasonerType.FAST_RETRIEVAL) {
    		throw new Error("Computing score for concept with ADC"
    				+ " is currently only supported by retrivals "
    				+ " using the fast retrieval algorithm");
    	} else {
    		// SortedSetTuple tuple = reasoner.doubleRetrieval(concept,adc);
    		// this.defPosSet = tuple.getPosSet();
    		// this.defNegSet = tuple.getNegSet();
    		// deriveStatistics();
    	}		
    	throw new Error("LP not completely implemented");
	}
	
	public SortedSet<Individual> getNegativeExamples() {
		return negativeExamples;
	}

	public SortedSet<Individual> getNeutralExamples() {
		return neutralExamples;
	}	
	
	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}

	public ReasoningService getReasoningService() {
		return reasoningService;
	}
}
