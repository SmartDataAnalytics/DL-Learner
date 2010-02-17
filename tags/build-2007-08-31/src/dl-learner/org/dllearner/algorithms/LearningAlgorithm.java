package org.dllearner.algorithms;

import org.dllearner.Score;
import org.dllearner.dl.Concept;

/**
 * TODO: Es gibt 3 Sachen, die eine Loesung ausmachen: die Loesung selbst,
 * also z.B. male AND EXISTS hasChild.TOP; die Score und die Fitness;
 * man kann das also vielleicht alles noch sinnvoller integrieren, also
 * z.B. die Score-Klasse so gestalten, dass Sie ein Node entgegennimmt und
 * dort alles berechnet; beim Vergleich mehererer Algorithmen darf es dann
 * z.B. nicht passieren, dass unterschiedliche Fitnessmessungen die Statistik
 * verfaelschen; mit ADC wird das Ganze dann noch komplexer
 * 
 * @author jl
 *
 */
public interface LearningAlgorithm {
	
	/**
	 * Starts the algorithm.
	 *
	 */
	public void start();
	
	/**
	 * Every algorithm must be able to return the score of the
	 * best solution found.
	 * @return Best score.
	 */
	public Score getSolutionScore();
	
	/**
	 * Gibt beste L�sung zur�ck.
	 * @return
	 */
	public Concept getBestSolution();
	
	/**
	 * Stops the algorithm gracefully.
	 *
	 */
	public void stop();
}
