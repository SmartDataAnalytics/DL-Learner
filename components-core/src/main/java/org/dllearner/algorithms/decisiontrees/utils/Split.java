package org.dllearner.algorithms.decisiontrees.utils;

import java.util.ArrayList;
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

		for (int e=0; e<trainingExs.length; e++){
			
			if (reasoner.hasType(classToDescribe2, trainingExs[e]))
				posExs.add(trainingExs[e]);
			else if (!binaryClassification){
				OWLObjectComplementOf owlObjectComplementOf = df.getOWLObjectComplementOf(classToDescribe2);
				
				if (reasoner.hasType(owlObjectComplementOf, trainingExs[e]))
					negExs.add(trainingExs[e]);
				else
					undExs.add(trainingExs[e]);
				
			}
			else
				negExs.add(trainingExs[e]);
				
			
		}
		
	}


	
	
	
	
	
	
	
	
	
	
}
