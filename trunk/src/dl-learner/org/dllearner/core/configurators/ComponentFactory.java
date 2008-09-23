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

package org.dllearner.core.configurators;

import java.net.URL;
import java.util.Set;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.DBpediaNavigationSuggestor;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.SimpleSuggestionLearningAlgorithm;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegDefinitionLPStrict;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.learningproblems.PosOnlyInclusionLP;
import org.dllearner.learningproblems.RoleLearning;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public final class ComponentFactory  {

private ComponentFactory(){}

/**
* @return a component ready for initialization KBFile
**/
public static KBFile getKBFile()  {
return KBFileConfigurator.getKBFile();
}

/**
* @return a component ready for initialization OWLAPIOntology
**/
public static OWLAPIOntology getOWLAPIOntology()  {
return OWLAPIOntologyConfigurator.getOWLAPIOntology();
}

/**
* @param url URL pointing to the OWL file
* @return a component ready for initialization OWLFile
**/
public static OWLFile getOWLFile(URL url)  {
return OWLFileConfigurator.getOWLFile(url);
}

/**
* @param url URL of SPARQL Endpoint
* @param instances relevant instances e.g. positive and negative examples in a learning problem
* @return a component ready for initialization SparqlKnowledgeSource
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource(URL url, Set<String> instances)  {
return SparqlKnowledgeSourceConfigurator.getSparqlKnowledgeSource(url, instances);
}

/**
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization DIGReasoner
**/
public static DIGReasoner getDIGReasoner(Set<KnowledgeSource> knowledgeSource)  {
return DIGReasonerConfigurator.getDIGReasoner(knowledgeSource);
}

/**
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization FastInstanceChecker
**/
public static FastInstanceChecker getFastInstanceChecker(Set<KnowledgeSource> knowledgeSource)  {
return FastInstanceCheckerConfigurator.getFastInstanceChecker(knowledgeSource);
}

/**
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization FastRetrievalReasoner
**/
public static FastRetrievalReasoner getFastRetrievalReasoner(Set<KnowledgeSource> knowledgeSource)  {
return FastRetrievalReasonerConfigurator.getFastRetrievalReasoner(knowledgeSource);
}

/**
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization OWLAPIReasoner
**/
public static OWLAPIReasoner getOWLAPIReasoner(Set<KnowledgeSource> knowledgeSource)  {
return OWLAPIReasonerConfigurator.getOWLAPIReasoner(knowledgeSource);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosNegDefinitionLP
**/
public static PosNegDefinitionLP getPosNegDefinitionLP(ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples)  {
return PosNegDefinitionLPConfigurator.getPosNegDefinitionLP(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosNegDefinitionLPStrict
**/
public static PosNegDefinitionLPStrict getPosNegDefinitionLPStrict(ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples)  {
return PosNegDefinitionLPStrictConfigurator.getPosNegDefinitionLPStrict(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosNegInclusionLP
**/
public static PosNegInclusionLP getPosNegInclusionLP(ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples)  {
return PosNegInclusionLPConfigurator.getPosNegInclusionLP(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosOnlyDefinitionLP
**/
public static PosOnlyDefinitionLP getPosOnlyDefinitionLP(ReasoningService reasoningService, Set<String> positiveExamples)  {
return PosOnlyDefinitionLPConfigurator.getPosOnlyDefinitionLP(reasoningService, positiveExamples);
}

/**
* @param positiveExamples positive examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosOnlyInclusionLP
**/
public static PosOnlyInclusionLP getPosOnlyInclusionLP(ReasoningService reasoningService, Set<String> positiveExamples)  {
return PosOnlyInclusionLPConfigurator.getPosOnlyInclusionLP(reasoningService, positiveExamples);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization RoleLearning
**/
public static RoleLearning getRoleLearning(ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples)  {
return RoleLearningConfigurator.getRoleLearning(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization BruteForceLearner
**/
public static BruteForceLearner getBruteForceLearner(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return BruteForceLearnerConfigurator.getBruteForceLearner(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization DBpediaNavigationSuggestor
**/
public static DBpediaNavigationSuggestor getDBpediaNavigationSuggestor(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return DBpediaNavigationSuggestorConfigurator.getDBpediaNavigationSuggestor(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization RandomGuesser
**/
public static RandomGuesser getRandomGuesser(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return RandomGuesserConfigurator.getRandomGuesser(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization SimpleSuggestionLearningAlgorithm
**/
public static SimpleSuggestionLearningAlgorithm getSimpleSuggestionLearningAlgorithm(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return SimpleSuggestionLearningAlgorithmConfigurator.getSimpleSuggestionLearningAlgorithm(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization GP
**/
public static GP getGP(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return GPConfigurator.getGP(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization ExampleBasedROLComponent
**/
public static ExampleBasedROLComponent getExampleBasedROLComponent(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return ExampleBasedROLComponentConfigurator.getExampleBasedROLComponent(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization ROLearner
**/
public static ROLearner getROLearner(LearningProblem learningProblem, ReasoningService reasoningService) throws LearningProblemUnsupportedException {
return ROLearnerConfigurator.getROLearner(learningProblem, reasoningService);
}


}
