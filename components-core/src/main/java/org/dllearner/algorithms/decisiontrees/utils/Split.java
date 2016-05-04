/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.decisiontrees.utils;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

/**
 * A class for splitting sets of individuals
 * @author Giuseppe Rizzo
 *
 */
public class Split {

	public static void split(OWLClassExpression concept, OWLDataFactory  df, AbstractReasonerComponent  reasoner, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs,
			SortedSet<OWLIndividual> posExsT, SortedSet<OWLIndividual> negExsT, SortedSet<OWLIndividual> undExsT, SortedSet<OWLIndividual> posExsF, SortedSet<OWLIndividual> negExsF,
			SortedSet<OWLIndividual> undExsF) {

		SortedSet<OWLIndividual> posExsU = new TreeSet<>();
		SortedSet<OWLIndividual> negExsU = new TreeSet<>();
		SortedSet<OWLIndividual> undExsU = new TreeSet<>();

		splitGroup(concept,df, reasoner, posExs,posExsT,posExsF,posExsU);
		splitGroup(concept,df, reasoner, negExs,negExsT,negExsF,negExsU);
		splitGroup(concept,df, reasoner, undExs,undExsT,undExsF,undExsU);	

	}

	public static void splitGroup(OWLClassExpression concept, OWLDataFactory  dataFactory, AbstractReasonerComponent  reasoner, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> posExsT,
			SortedSet<OWLIndividual> posExsF, SortedSet<OWLIndividual> posExsU) {
		OWLClassExpression negConcept = dataFactory.getOWLObjectComplementOf(concept);

		for ( OWLIndividual individual :posExs ){//int e=0; e<nodeExamples.size(); e++) {
			
//			int exIndex = nodeExamples.get(e);
			if (reasoner.hasType(concept, individual))
				posExsT.add(individual);
			else if (reasoner.hasType(negConcept, individual))
				posExsF.add(individual);
			else
				posExsU.add(individual);		
		}	

	}
	
	public static void splitting(OWLDataFactory df, AbstractReasonerComponent reasoner, OWLIndividual[] trainingExs, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, OWLClassExpression classToDescribe2, boolean binaryClassification) {

		for (OWLIndividual trainingEx : trainingExs) {

			if (reasoner.hasType(classToDescribe2, trainingEx))
				posExs.add(trainingEx);
			else if (!binaryClassification) {
				OWLObjectComplementOf owlObjectComplementOf = df.getOWLObjectComplementOf(classToDescribe2);

				if (reasoner.hasType(owlObjectComplementOf, trainingEx))
					negExs.add(trainingEx);
				else
					undExs.add(trainingEx);

			} else
				negExs.add(trainingEx);

		}
		
	}

	
	
	
	
	
	
	
	
	
	
}
