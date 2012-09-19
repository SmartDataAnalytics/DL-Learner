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

package org.dllearner.algorithms.hybridgp;

import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.dllearner.algorithms.gp.Program;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.refinementoperators.PsiDown;
import org.dllearner.refinementoperators.PsiUp;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;

public class Psi implements GeneticRefinementOperator {

	PsiUp pu;
	PsiDown pd;
	PosNegLP learningProblem;
	int nrOfPositiveExamples;
	int nrOfNegativeExamples;
	Random random;
	
	// Cache, damit keine Konzepte doppelt ausgewertet werden
	ConceptComparator conceptComparator = new ConceptComparator();
	public SortedMap<Description,ScorePosNeg> evalCache = new TreeMap<Description,ScorePosNeg>(conceptComparator);
	
	// Cache, damit PsiDown bzw. PsiUp nicht mehrfach für gleiches Konzept
	// aufgerufen werden
	public SortedMap<Description,Set<Description>> pdCache = new TreeMap<Description,Set<Description>>(conceptComparator);
	public SortedMap<Description,Set<Description>> puCache = new TreeMap<Description,Set<Description>>(conceptComparator);
	
	// Statistiken
	int conceptCacheHits = 0;
	int nrOfRequests = 0;
	int pdCacheHits = 0;
	private long pdRequests = 0;
	int puCacheHits = 0;
	private long puRequests = 0;
	private long psiApplicationStartTime = 0;
	private long psiApplicationTimeNs = 0;
	private long psiReasoningStartTime = 0;
	private long psiReasoningTimeNs = 0;
	
	@SuppressWarnings("unused")
	private long someTimeStart = 0;
	public long someTime = 0;
	
	public Psi(PosNegLP learningProblem, AbstractReasonerComponent reasoningService) { //, PsiUp pu, PsiDown pd) {
		// this.pu = pu;
		// this.pd = pd;
		this.learningProblem = learningProblem;
		pu = new PsiUp(learningProblem, reasoningService);
		pd = new PsiDown(learningProblem, reasoningService);
		nrOfPositiveExamples = learningProblem.getPositiveExamples().size();
		nrOfNegativeExamples = learningProblem.getNegativeExamples().size();
		random = new Random();
	}
	
	public Description applyPsi(Description concept, int coveredPositives, int coveredNegatives) {
		// Wahrscheinlichkeit für upward refinement berechnen
		double tmp = coveredNegatives/(double)nrOfNegativeExamples;
		double tmp2 = coveredPositives/(double)nrOfPositiveExamples;
		
		double downwardProbability = tmp/(1+tmp-tmp2); 
		
		boolean debug = false;
		if(debug) {
			System.out.println("negative percentage covered: " + tmp);
			System.out.println("positive percentage covered: " + tmp2);
			// System.out.println("upward probability: upward")
		}
		
		if(downwardProbability<0 || downwardProbability>1) {
			
			// bei Division gibt es Ungenauigkeit, so dass man einen kleinen
			// Toleranzbereich lassen muss
			if(downwardProbability < -0.0001)
				throw new RuntimeException();
			else if(downwardProbability > 1.0001)
				throw new RuntimeException();
			// Rundung auf korrekten Wert
			else if(downwardProbability<0)
				downwardProbability = 0d;
			else
				downwardProbability = 1d;
		}
			
		// System.out.println(upwardProbability);
		
		boolean downward = (Math.random()<=downwardProbability);
		
		// someTimeStart = System.nanoTime();
		// downward oder upward refinement operator anwenden
		Set<Description> refinements;
		if(downward) {
			pdRequests++;
			// Cache aufrufen
			refinements = pdCache.get(concept);
			if(refinements == null) {
				refinements = pd.refine(concept);
				pdCache.put(concept, refinements);
			} else
				pdCacheHits++;
		} else {
			puRequests++;
			refinements = puCache.get(concept);
			if(refinements == null) {
				refinements = pu.refine(concept);
				puCache.put(concept, refinements);
			} else
				puCacheHits++;
		}
		
		/*
		if(puRequests % 500 == 0 && !downward) {
			System.out.println(concept);
			for(Concept refinement : refinements)
				System.out.println("  " + refinement);
		}*/
		
		
		// someTime += System.nanoTime() - someTimeStart;
		// System.out.println(refinements.size());
		
		String dir = "";
		int prob = -1;
		if(debug) {
			if(downward) {
				dir = "downward";
				prob = (int) (100 * downwardProbability);
			} else {
				dir = "upward";
				prob = (int) (100 * (1-downwardProbability));			
			}
		}
			
		
		// ein refinement zufällig auswählen
		Description[] array = refinements.toArray(new Description[0]);
		// kein refinement gefunden
		if(array.length==0) {
			if(debug) {
				System.out.println("message: no " + dir + " refinement found for " + concept);
				System.out.println();				
			}
			// Konzept selbst wird zurückgegeben (also reproduction)
			return concept;
		}
			
		int position = random.nextInt(array.length);
		Description returnConcept = array[position];
		ConceptTransformation.cleanConcept(returnConcept);
		
		// das Rückgabekonzept wird geklont, damit alle parent-Links repariert
		// werden (damit andere GP-Operatoren korrekt funktionieren)
		Description returnConceptClone = (Description)returnConcept.clone();
		returnConceptClone.setParent(null);
		
		if(debug) {
			System.out.println(concept + " " + dir + "("+prob+"%) to " + returnConcept);
			System.out.println();
		}
		
		return returnConceptClone;
	}
	
	public Program applyOperator(Program program) {
		psiApplicationStartTime = System.nanoTime();
		nrOfRequests++;
		
		Description concept = program.getTree();
		// es muss sichergestellt sein, dass Konjunktionen nur als MultConjunctions
		// vorhanden sind (analog bei Disjunktion) => effizienter wäre eine Auslagerung
		// dieses Codes in die Konzepterzeugung, da die Transformation häufig nichts
		// bringt; allerdings sollte GP noch kompatibel mit anderen Operatoren bleiben
		// Concept conceptMod = ConceptTransformation.transformToMulti(concept);		
		// sicherstellen, dass Konstrukte wie NOT TOP, die momentan nicht vom
		// Operator behandelt werden können, herausfallen (TODO: anschauen, ob es
		// sich lohnt Operatoren zu definieren, die keine Negationsnormalform
		// erfordern)
		
		Description conceptMod = ConceptTransformation.transformToNegationNormalForm(concept);
		// um mehr Cache Hits zu bekommen, wird noch vereinfach und geordnet
		
		
		Description conceptModForCache = ConceptTransformation.applyEquivalenceRules(conceptMod);
		ConceptTransformation.transformToOrderedForm(conceptModForCache, conceptComparator);
		
		ScorePosNeg score = program.getScore();
		// Eval-Cache füllen
		evalCache.put(conceptModForCache, score);
		
		// System.out.println("covered positives: " + score.getCoveredPositives());
		// System.out.println("covered negatives: " + score.getCoveredNegatives());		
		int coveredPositives = score.getCoveredPositives().size();
		int coveredNegatives = score.getCoveredNegatives().size();
		// someTimeStart = System.nanoTime();
		Description newConcept = applyPsi(conceptMod, coveredPositives, coveredNegatives);
		ConceptTransformation.cleanConcept(newConcept);
		// someTime += System.nanoTime() - someTimeStart;
		// newConcept.setParent(null);
		
		/////////// TESTCODE: umwandeln des erhaltenen Konzepts
		// someTimeStart = System.nanoTime();
		Description newConceptMod = ConceptTransformation.applyEquivalenceRules(newConcept);
		ConceptTransformation.transformToOrderedForm(newConceptMod, conceptComparator);
		// someTime += System.nanoTime() - someTimeStart;
		///////////
		
		// versuchen Reasoner-Cache zu treffen
		// Problem: Score hängt von Konzeptlänge ab!! => muss hier explizit
		// reingerechnet werden
		ScorePosNeg newScore = evalCache.get(newConceptMod);
		
		if(newScore==null) {
			psiReasoningStartTime = System.nanoTime();
			newScore = (ScorePosNeg) learningProblem.computeScore(newConcept);
			psiReasoningTimeNs += System.nanoTime() - psiReasoningStartTime;
			
			evalCache.put(newConceptMod, newScore);
		} else {
			conceptCacheHits++;
			
			// ToDo: es muss jetzt explizit ein neues Score-Objekt
			// erzeugt werden, welches die geänderte Konzeptlänge
			// berücksichtigt
			newScore = newScore.getModifiedLengthScore(newConcept.getLength());
		}
		
		/*
		if(nrOfRequests % 1000 == 0) {
			System.out.println(concept);
			System.out.println(newConcept);
		}
		*/
		
		Program newProgram = new Program(newScore, newConcept);
		psiApplicationTimeNs += System.nanoTime() - psiApplicationStartTime;
		return newProgram;
	}
	
	// gibt die Größe des Caches zurück (gutes Maß um zu sehen, ob überhaupt
	// neue Konzepte erforscht werden)
	public int getCacheSize() {
		return evalCache.size();
	}

	public int getConceptCacheHits() {
		return conceptCacheHits;
	}

	public int getNrOfRequests() {
		return nrOfRequests;
	}

	public long getPsiApplicationTimeNs() {
		return psiApplicationTimeNs;
	}

	public long getPsiReasoningTimeNs() {
		return psiReasoningTimeNs;
	}

	public int getPdCacheHits() {
		return pdCacheHits;
	}

	public int getPuCacheHits() {
		return puCacheHits;
	}

	public long getPdRequests() {
		return pdRequests;
	}

	public long getPuRequests() {
		return puRequests;
	}

	public SortedMap<Description, Set<Description>> getPdCache() {
		return pdCache;
	}

	public SortedMap<Description, Set<Description>> getPuCache() {
		return puCache;
	}
	
}