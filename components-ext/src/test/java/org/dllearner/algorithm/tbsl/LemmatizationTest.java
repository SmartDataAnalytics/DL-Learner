package org.dllearner.algorithm.tbsl;

import org.dllearner.algorithm.tbsl.nlp.LingPipeLemmatizer;

public class LemmatizationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		LingPipeLemmatizer lem = new LingPipeLemmatizer();

		System.out.println(lem.stem("soccer clubs"));
		System.out.println(lem.stem("Permier League","NNP"));
		System.out.println(lem.stem("cities","NNS"));
		System.out.println(lem.stem("killed"));
		System.out.println(lem.stem("bigger"));
	}

}
