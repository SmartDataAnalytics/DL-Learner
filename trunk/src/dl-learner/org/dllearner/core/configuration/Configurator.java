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

package org.dllearner.core.configuration;

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
import org.dllearner.core.configuration.BruteForceLearnerConfigurator;
import org.dllearner.core.configuration.DBpediaNavigationSuggestorConfigurator;
import org.dllearner.core.configuration.DIGReasonerConfigurator;
import org.dllearner.core.configuration.ExampleBasedROLComponentConfigurator;
import org.dllearner.core.configuration.FastInstanceCheckerConfigurator;
import org.dllearner.core.configuration.FastRetrievalReasonerConfigurator;
import org.dllearner.core.configuration.GPConfigurator;
import org.dllearner.core.configuration.KBFileConfigurator;
import org.dllearner.core.configuration.OWLAPIReasonerConfigurator;
import org.dllearner.core.configuration.OWLFileConfigurator;
import org.dllearner.core.configuration.PosNegDefinitionLPConfigurator;
import org.dllearner.core.configuration.PosNegInclusionLPConfigurator;
import org.dllearner.core.configuration.PosOnlyDefinitionLPConfigurator;
import org.dllearner.core.configuration.ROLearnerConfigurator;
import org.dllearner.core.configuration.RandomGuesserConfigurator;
import org.dllearner.core.configuration.SparqlKnowledgeSourceConfigurator;
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
@SuppressWarnings("unused")
public class Configurator  {

/**
* @param filename pointer to the KB file on local file system
**/
public static KBFile getKBFile (ComponentManager cm, String filename )  {
return KBFileConfigurator.getKBFile(cm, filename);
}

/**
* @param url URL pointing to the OWL file
**/
public static OWLFile getOWLFile (ComponentManager cm, String url )  {
return OWLFileConfigurator.getOWLFile(cm, url);
}

/**
* @param instances relevant instances e.g. positive and negative examples in a learning problem
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource (ComponentManager cm, Set<String> instances )  {
return SparqlKnowledgeSourceConfigurator.getSparqlKnowledgeSource(cm, instances);
}

/**
**/
public static DIGReasoner getDIGReasoner (ComponentManager cm, KnowledgeSource knowledgeSource )  {
return DIGReasonerConfigurator.getDIGReasoner(cm, knowledgeSource);
}

/**
**/
public static FastInstanceChecker getFastInstanceChecker (ComponentManager cm, KnowledgeSource knowledgeSource )  {
return FastInstanceCheckerConfigurator.getFastInstanceChecker(cm, knowledgeSource);
}

/**
**/
public static FastRetrievalReasoner getFastRetrievalReasoner (ComponentManager cm, KnowledgeSource knowledgeSource )  {
return FastRetrievalReasonerConfigurator.getFastRetrievalReasoner(cm, knowledgeSource);
}

/**
**/
public static OWLAPIReasoner getOWLAPIReasoner (ComponentManager cm, KnowledgeSource knowledgeSource )  {
return OWLAPIReasonerConfigurator.getOWLAPIReasoner(cm, knowledgeSource);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
**/
public static PosNegDefinitionLP getPosNegDefinitionLP (ComponentManager cm, ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples )  {
return PosNegDefinitionLPConfigurator.getPosNegDefinitionLP(cm, reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
**/
public static PosNegInclusionLP getPosNegInclusionLP (ComponentManager cm, ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples )  {
return PosNegInclusionLPConfigurator.getPosNegInclusionLP(cm, reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
**/
public static PosOnlyDefinitionLP getPosOnlyDefinitionLP (ComponentManager cm, ReasoningService reasoningService, Set<String> positiveExamples )  {
return PosOnlyDefinitionLPConfigurator.getPosOnlyDefinitionLP(cm, reasoningService, positiveExamples);
}

/**
**/
public static BruteForceLearner getBruteForceLearner (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return BruteForceLearnerConfigurator.getBruteForceLearner(cm, learningProblem, reasoningService);
}

/**
**/
public static DBpediaNavigationSuggestor getDBpediaNavigationSuggestor (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return DBpediaNavigationSuggestorConfigurator.getDBpediaNavigationSuggestor(cm, learningProblem, reasoningService);
}

/**
**/
public static RandomGuesser getRandomGuesser (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return RandomGuesserConfigurator.getRandomGuesser(cm, learningProblem, reasoningService);
}

/**
**/
public static GP getGP (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return GPConfigurator.getGP(cm, learningProblem, reasoningService);
}

/**
**/
public static ExampleBasedROLComponent getExampleBasedROLComponent (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return ExampleBasedROLComponentConfigurator.getExampleBasedROLComponent(cm, learningProblem, reasoningService);
}

/**
**/
public static ROLearner getROLearner (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException {
return ROLearnerConfigurator.getROLearner(cm, learningProblem, reasoningService);
}


}
