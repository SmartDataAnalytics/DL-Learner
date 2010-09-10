/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.scripts.improveWikipedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.datastructures.SetManipulation;

/**
 * This is a simple class, it might be worked into other classes later. filters
 * concepts and records some results
 * 
 * @author Sebastian Hellmann
 * 
 */
public class ConceptSelector {

	private static final long WASH = 1216800000000L;

	// List<EvaluatedDescription> concepts;

	public ConceptSelector() {
		super();
		// this.concepts = concepts;
		// this.recordConceptClasses();

	}

	public List<EvaluatedDescriptionPosNeg> getAllConceptsWithoutOR(
			List<EvaluatedDescriptionPosNeg> concepts) {
		return getConceptsNotContainingString(concepts, "OR");
	}

	public List<EvaluatedDescriptionPosNeg> getConceptsNotContainingString(
			List<EvaluatedDescriptionPosNeg> concepts, String filterString,
			int limitSize) {
		// List<EvaluatedDescription> tmp =
		// getConceptsNotContainingString(filterString);
		// List<EvaluatedDescription> result = new
		// ArrayList<EvaluatedDescription>();
		return SetManipulation.getFirst(getConceptsNotContainingString(
				concepts, filterString), limitSize);
		/*
		 * while ((!tmp.isEmpty()) && (result.size() <= limitSize)) {
		 * result.add(tmp.remove(0)); } return result;
		 */
	}

	public List<EvaluatedDescriptionPosNeg> getConceptsNotContainingString(
			List<EvaluatedDescriptionPosNeg> concepts, String filterString) {

		List<EvaluatedDescriptionPosNeg> result = new ArrayList<EvaluatedDescriptionPosNeg>();
		for (EvaluatedDescriptionPosNeg description : concepts) {
			if (!description.toString().contains(filterString)) {
				result.add(description);
			}

		}
		return result;
	}

	public void recordConceptClasses(List<EvaluatedDescriptionPosNeg> concepts) {
		StringBuffer result = new StringBuffer();
		StringBuffer result1 = new StringBuffer("\n\n ***********Entity*****\n");
		StringBuffer result2 = new StringBuffer("\n\n ***********OR*****\n");
		int result1count = 1;
		int result2count = 1;

		int x = 0;
		for (EvaluatedDescriptionPosNeg description : concepts) {
			if (x < 50) {
				x++;
				result.append(description + "\n");
			}

			if (!description.toString().contains("Entity")) {
				result1.append(description + "\n");
				result1count++;
			}
			if (!description.toString().contains("OR")) {
				result2.append(description + "\n");
				result2count++;
			}
		}
		result.append("full size: " + concepts.size());
		result.append(result1.toString() + " size: " + result1count + "\n");
		result.append(result2.toString() + " size: " + result2count + "\n");

		Files.createFile(new File("results/descriptions/concepts" + time()
				+ ".txt"), result.toString());
	}

	public static String time() {
		return ("" + (System.currentTimeMillis() - WASH)).substring(0, 7);

	}

}
