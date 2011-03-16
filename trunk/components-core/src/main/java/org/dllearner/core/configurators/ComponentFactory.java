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
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive;
import org.dllearner.algorithms.fuzzydll.FuzzyCELOE;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.isle.ISLE;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosNegLPStrict;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.reasoning.ProtegeReasoner;
import org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner;

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
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization PelletReasoner
**/
public static PelletReasoner getPelletReasoner(Set<KnowledgeSource> knowledgeSource)  {
return PelletReasonerConfigurator.getPelletReasoner(knowledgeSource);
}

/**
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization ProtegeReasoner
**/
public static ProtegeReasoner getProtegeReasoner(Set<KnowledgeSource> knowledgeSource)  {
return ProtegeReasonerConfigurator.getProtegeReasoner(knowledgeSource);
}

/**
* @param knowledgeSource see KnowledgeSource
* @return a component ready for initialization FuzzyOWLAPIReasoner
**/
public static FuzzyOWLAPIReasoner getFuzzyOWLAPIReasoner(Set<KnowledgeSource> knowledgeSource)  {
return FuzzyOWLAPIReasonerConfigurator.getFuzzyOWLAPIReasoner(knowledgeSource);
}

/**
* @param classToDescribe class of which a description should be learned
* @param reasoningService see ReasoningService
* @return a component ready for initialization ClassLearningProblem
**/
public static ClassLearningProblem getClassLearningProblem(ReasonerComponent reasoningService, URL classToDescribe)  {
return ClassLearningProblemConfigurator.getClassLearningProblem(reasoningService, classToDescribe);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosNegLPStandard
**/
public static PosNegLPStandard getPosNegLPStandard(ReasonerComponent reasoningService, Set<String> positiveExamples, Set<String> negativeExamples)  {
return PosNegLPStandardConfigurator.getPosNegLPStandard(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosNegLPStrict
**/
public static PosNegLPStrict getPosNegLPStrict(ReasonerComponent reasoningService, Set<String> positiveExamples, Set<String> negativeExamples)  {
return PosNegLPStrictConfigurator.getPosNegLPStrict(reasoningService, positiveExamples, negativeExamples);
}

/**
* @param positiveExamples positive examples
* @param reasoningService see ReasoningService
* @return a component ready for initialization PosOnlyLP
**/
public static PosOnlyLP getPosOnlyLP(ReasonerComponent reasoningService, Set<String> positiveExamples)  {
return PosOnlyLPConfigurator.getPosOnlyLP(reasoningService, positiveExamples);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization BruteForceLearner
**/
public static BruteForceLearner getBruteForceLearner(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return BruteForceLearnerConfigurator.getBruteForceLearner(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization RandomGuesser
**/
public static RandomGuesser getRandomGuesser(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return RandomGuesserConfigurator.getRandomGuesser(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization CELOE
**/
public static CELOE getCELOE(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return CELOEConfigurator.getCELOE(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization ELLearningAlgorithm
**/
public static ELLearningAlgorithm getELLearningAlgorithm(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return ELLearningAlgorithmConfigurator.getELLearningAlgorithm(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization ELLearningAlgorithmDisjunctive
**/
public static ELLearningAlgorithmDisjunctive getELLearningAlgorithmDisjunctive(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return ELLearningAlgorithmDisjunctiveConfigurator.getELLearningAlgorithmDisjunctive(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization FuzzyCELOE
**/
public static FuzzyCELOE getFuzzyCELOE(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return FuzzyCELOEConfigurator.getFuzzyCELOE(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization GP
**/
public static GP getGP(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return GPConfigurator.getGP(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization ISLE
**/
public static ISLE getISLE(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return ISLEConfigurator.getISLE(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization OCEL
**/
public static OCEL getOCEL(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return OCELConfigurator.getOCEL(learningProblem, reasoningService);
}

/**
* @param learningProblem see LearningProblem
* @param reasoningService see ReasoningService
* @throws LearningProblemUnsupportedException see
* @return a component ready for initialization ROLearner
**/
public static ROLearner getROLearner(LearningProblem learningProblem, ReasonerComponent reasoningService) throws LearningProblemUnsupportedException {
return ROLearnerConfigurator.getROLearner(learningProblem, reasoningService);
}


}
