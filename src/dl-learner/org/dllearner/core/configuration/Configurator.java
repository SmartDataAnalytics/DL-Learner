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
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.configuration.FastInstanceCheckerConfigurator;
import org.dllearner.core.configuration.OWLFileConfigurator;
import org.dllearner.core.configuration.SparqlKnowledgeSourceConfigurator;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.reasoning.FastInstanceChecker;

/**
* automatically generated, do not edit manually
**/
public class Configurator  {

/**
* url: URL pointing to the OWL file
**/
public static OWLFile getOWLFile (ComponentManager cm, String url ) {
return OWLFileConfigurator.getOWLFile(cm, url);
}

/**
* instances: relevant instances e.g. positive and negative examples in a learning problem
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource (ComponentManager cm, Set<String> instances ) {
return SparqlKnowledgeSourceConfigurator.getSparqlKnowledgeSource(cm, instances);
}

/**
**/
public static FastInstanceChecker getFastInstanceChecker (ComponentManager cm, KnowledgeSource knowledgeSource ) {
return FastInstanceCheckerConfigurator.getFastInstanceChecker(cm, knowledgeSource);
}


}
