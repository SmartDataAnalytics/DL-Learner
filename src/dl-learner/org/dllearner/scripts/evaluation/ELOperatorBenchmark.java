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
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.ELDown2;
import org.dllearner.utilities.statistics.Stat;

/**
 * An evaluation of the EL refinement operator {@link ELDown2}. It creates
 * a set of artificial ontologies with varying complexity and performs
 * refinement steps on them.
 * 
 * @author Jens Lehmann
 *
 */
public class ELOperatorBenchmark {

	private static Random rand = new Random(1);
	
	public static void main(String[] args) throws MalformedURLException, ComponentInitException {
		String example = "/home/jl/promotion/ontologien/galen2.owl";
		testOntology(example);
		System.exit(0);
		
		/* TEST ON ARTIFICIAL ONTOLOGIES
		  
		 
		// number of concepts and roles
		int[] conceptCounts = new int[] { 5, 10 };
		int[] roleCounts = new int[] { 5, 10};
		
		// number of applications of operator
		int opApplications = 10;
		
		// statistics directory
		String statDir = "/log/stat/el/";
		String statFile = statDir + "stats.txt";
		String gnuPlotApplicationTimeFile = statDir + "application.gp";
		String gnuPlotRefinementTimeFile = statDir + "refinement.gp";
		boolean writeOntologies = true;
		String ontologyDir = "/log/stat/el/ontologies/";
		
		
		
		for(int conceptCount : conceptCounts) {
			for(int roleCount : roleCounts) {
				// code for ontology creation
				KB kb = new KB();
				
				// create class hierarchy (concept 0 is owl:Thing)
				for(int i=1; i<=conceptCount; i++) {
					// create class
					NamedClass nc = new NamedClass("a" + i);
					// pick an existing class as super class
					int j = (i == 0) ? 0 : rand.nextInt(i);
					Description superClass;
					if(j==0) {
						superClass = Thing.instance;
					} else {
						superClass = new NamedClass("a" + j);
					}
					kb.addAxiom(new SubClassAxiom(nc, superClass));
					// disjointness with siblings
				}
				
				
				// save ontology
				File f = new File(ontologyDir + "c" + conceptCount + "r" + roleCount + ".owl");
				kb.export(f, OntologyFormat.RDF_XML);
				

			}
		}
		*/
//		ELDown2 operator = new ELDown2();
	}
	
	private static void testOntology(String ont) throws MalformedURLException, ComponentInitException {
		System.out.print("Reading in " + ont + " ... ");
		ComponentManager cm = ComponentManager.getInstance();
		// reading ontology into a reasoner
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(source, "url", new File(ont).toURI().toURL());
		source.init();
		ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, source);
		reasoner.init();
		System.out.println("done.");
		System.out.println();
		
		int outerLoops = 10;
		for(int loop = 0; loop < outerLoops; loop++) {
		
			// application of operator and statistics recording	
			int nrOfApplications = 10;
			ELDescriptionTree currTree = new ELDescriptionTree(reasoner, Thing.instance);
			ELDown2 operator = new ELDown2(reasoner);
			Stat runtime = new Stat();
			Stat runtimePerRefinement = new Stat();
			
			System.out.println("Testing operator (applying it " + nrOfApplications + " times):");
			for(int i=0; i < nrOfApplications; i++) {
				System.out.print("current concept: " + currTree.transformToDescription().toString(reasoner.getBaseURI(), reasoner.getPrefixes()));
				// apply operator on current description
				long start = System.nanoTime();
				Set<ELDescriptionTree> refinements = operator.refine(currTree);
				long time = System.nanoTime() - start;
				runtime.addNumber(time/1000000d);
				runtimePerRefinement.addNumber(time/1000000d/refinements.size());
				System.out.println("  [has " + refinements.size() + " refinements]");
				// pick a refinement randomly (which is kind of slow for a set, but
				// this does not matter here)
				int index = rand.nextInt(refinements.size());
				currTree = new ArrayList<ELDescriptionTree>(refinements).get(index);
			}
			System.out.println("operator time: " + runtime.prettyPrint("ms"));
			System.out.println("operator time per refinement: " + runtimePerRefinement.prettyPrint("ms"));
			System.out.println();
			
		}
	}
}
