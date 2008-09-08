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
 **/

package org.dllearner.core.configurators;

import java.util.Set;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.DBpediaNavigationSuggestor;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.configurators.BruteForceLearnerConfigurator;
import org.dllearner.core.configurators.DBpediaNavigationSuggestorConfigurator;
import org.dllearner.core.configurators.DIGReasonerConfigurator;
import org.dllearner.core.configurators.ExampleBasedROLComponentConfigurator;
import org.dllearner.core.configurators.FastInstanceCheckerConfigurator;
import org.dllearner.core.configurators.FastRetrievalReasonerConfigurator;
import org.dllearner.core.configurators.GPConfigurator;
import org.dllearner.core.configurators.KBFileConfigurator;
import org.dllearner.core.configurators.OWLAPIReasonerConfigurator;
import org.dllearner.core.configurators.OWLFileConfigurator;
import org.dllearner.core.configurators.PosNegDefinitionLPConfigurator;
import org.dllearner.core.configurators.PosNegInclusionLPConfigurator;
import org.dllearner.core.configurators.PosOnlyDefinitionLPConfigurator;
import org.dllearner.core.configurators.ROLearnerConfigurator;
import org.dllearner.core.configurators.RandomGuesserConfigurator;
import org.dllearner.core.configurators.SparqlKnowledgeSourceConfigurator;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
* automatically generated, do not edit manually
**/
public class ComponentFactory  {

/**
* @param filename pointer to the KB file on local file system
**/
public static KBFile getKBFile (String filename )  {
return KBFileConfigurator.getKBFile(filename);
}

/**
* @param url URL pointing to the OWL file
**/
public static OWLFile getOWLFile (String url )  {
return OWLFileConfigurator.getOWLFile(url);
}

/**
* @param instances relevant instances e.g. positive and negative examples in a learning problem
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource (Set<String> instances )  {
return SparqlKnowledgeSourceConfigurator.getSparqlKnowledgeSource(instances);
}

/**
**/
public static DIGReasoner getDIGReasoner (KnowledgeSource knowledgeSource )  {
return DIGReasonerConfigurator.getDIGReasoner(knowledgeSource);
}

/**
**/
public static FastInstanceChecker getFastInstanceChecker (KnowledgeSource knowledgeSource )  {
return FastInstanceCheckerConfigurator.getFastInstanceChecker(knowledgeSource);
}

/**
**/
public static FastRetrievalReasoner getFastRetrievalReasoner (KnowledgeSource knowledgeSource )  {
return FastRetrievalReasonerConfigurator.getFastRetrievalReasoner(knowledgeSource);
}

/**
**/
public static OWLAPIReasoner getOWLAPIReasoner (KnowledgeSource knowledgeSource )  {
return OWLAPIReasonerConfigurator.getOWLAPIReasoner(knowledgeSource);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
**/
public static PosNegDefinitionLP getPosNegDefinitionLP (ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples )  {
return PosNegDefinitionLPConfigurator.getPosNegDefinitionLP(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
**/
public static PosNegInclusionLP getPosNegInclusionLP (ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples )  {
return PosNegInclusionLPConfigurator.getPosNegInclusionLP(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
**/
public static PosOnlyDefinitionLP getPosOnlyDefinitionLP (ReasoningService reasoningService, Set<String> positiveExamples )  {
return PosOnlyDefinitionLPConfigurator.getPosOnlyDefinitionLP(reasoningService, positiveExamples);
}

/**
**/
public static BruteForceLearner getBruteForceLearner (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return BruteForceLearnerConfigurator.getBruteForceLearner(learningProblem, reasoningService);
}

/**
**/
public static DBpediaNavigationSuggestor getDBpediaNavigationSuggestor (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return DBpediaNavigationSuggestorConfigurator.getDBpediaNavigationSuggestor(learningProblem, reasoningService);
}

/**
**/
public static RandomGuesser getRandomGuesser (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return RandomGuesserConfigurator.getRandomGuesser(learningProblem, reasoningService);
}

/**
**/
public static GP getGP (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return GPConfigurator.getGP(learningProblem, reasoningService);
}

/**
**/
public static ExampleBasedROLComponent getExampleBasedROLComponent (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return ExampleBasedROLComponentConfigurator.getExampleBasedROLComponent(learningProblem, reasoningService);
}

/**
**/
public static ROLearner getROLearner (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return ROLearnerConfigurator.getROLearner(learningProblem, reasoningService);
}


}
