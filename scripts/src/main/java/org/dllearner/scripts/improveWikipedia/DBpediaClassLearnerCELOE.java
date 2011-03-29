/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.gui.Config;
import org.dllearner.gui.ConfigSave;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * A script, which learns definitions / super classes of classes in the DBpedia ontology.
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaClassLearnerCELOE {

	public DBpediaClassLearnerCELOE() {
		// OPTIONAL: if you want to do some case distinctions in the learnClass method, you could add
		// parameters to the constructure e.g. YAGO_ 
	}
	
	public KB learnAllClasses(Set<String> classesToLearn) throws LearningProblemUnsupportedException, IOException {
		KB kb = new KB();
		for(String classToLearn : classesToLearn) {
			kb.addAxiom(new EquivalentClassesAxiom(new NamedClass(classToLearn), learnClass(classToLearn)));
		}
		return kb;
	}
	
	public Description learnClass(String classToLearn) throws LearningProblemUnsupportedException, IOException {
		
		// TODO: code for getting postive and negative examples for class to learn
		SortedSet<Individual> posExamples = null;
		SortedSet<Individual> negExamples = null;
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples, negExamples);
		
		ComponentManager cm = ComponentManager.getInstance();
		
		SparqlKnowledgeSource ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
		ks.getConfigurator().setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
		ks.getConfigurator().setPredefinedEndpoint("DBPEDIA"); // TODO: probably the official endpoint is too slow?
		
		ReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, ks);
		
		PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
		lp.getConfigurator().setAccuracyMethod("fMeasure");
		lp.getConfigurator().setUseApproximations(false);
		
		CELOE la = cm.learningAlgorithm(CELOE.class, lp, rc);
		CELOEConfigurator cc = la.getConfigurator();
		cc.setMaxExecutionTimeInSeconds(100);
		cc.setNoisePercentage(20);
		// TODO: set more options as needed
		
		// to write the above configuration in a conf file (optional)
		Config cf = new Config(cm, ks, rc, lp, la);
		new ConfigSave(cf).saveFile(new File("/dev/null"));
		
		la.start();
		
		return la.getCurrentlyBestDescription();
	}
	
	public static void main(String args[]) throws LearningProblemUnsupportedException, IOException {
		DBpediaClassLearnerCELOE dcl = new DBpediaClassLearnerCELOE();
		Set<String> classesToLearn = null;
		KB kb = dcl.learnAllClasses(classesToLearn);
		kb.export(new File("/dev/null"), OntologyFormat.RDF_XML); // TODO: pick appropriate place to save ontology
	}
	
}
