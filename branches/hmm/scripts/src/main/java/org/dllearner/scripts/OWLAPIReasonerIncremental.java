/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.scripts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;

/**
 * 
 * Test script for incremental reasoning using the OWLAPIReasoner. 
 * Use with caution as reasoner components were designed for reasoning
 * against a static knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIReasonerIncremental {
	
	private static void usePelletIncremantalClassifier() throws OWLOntologyCreationException{
		System.out.println("Using Pellet IncrementalClassifier");
		String url = "http://morpheus.cs.umbc.edu/aks1/ontosem.owl";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology ontology = manager.loadOntology(IRI.create(url));
		IncrementalClassifier incReasoner = PelletIncremantalReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
		incReasoner.prepareReasoner();
		System.out.println("Loaded ontology " + url + ".");
		
		OWLClass cl1 = factory.getOWLClass(IRI.create("http://morpheus.cs.umbc.edu/aks1/ontosem.owl#christianity"));
		OWLClass cl2 = factory.getOWLClass(IRI.create("http://morpheus.cs.umbc.edu/aks1/ontosem.owl#priest"));
		OWLClass newCl = factory.getOWLClass(IRI.create("http://nke.aksw.org/new"));
		OWLObjectProperty op1 = factory.getOWLObjectProperty(IRI.create("http://morpheus.cs.umbc.edu/aks1/ontosem.owl#operated-by"));
		OWLClassExpression exp1 = factory.getOWLObjectSomeValuesFrom(op1, cl2);
		OWLClassExpression exp2 = factory.getOWLObjectIntersectionOf(cl1, exp1);
		OWLEquivalentClassesAxiom eq = factory.getOWLEquivalentClassesAxiom(newCl, exp2);
		
		System.out.println("Adding " + eq + ".");
		long startTime = System.nanoTime();
		manager.applyChange(new AddAxiom(ontology, eq));
		// perform reasoning using OWL API
		boolean consistent = incReasoner.isConsistent();
		Set<OWLClass> superClasses = incReasoner.getSuperClasses(newCl, true).getFlattened();
		Set<OWLClass> subClasses = incReasoner.getSubClasses(newCl, true).getFlattened();
		Set<OWLClass> parallelClasses = new TreeSet<OWLClass>(); 
		for(OWLClass clazz : superClasses) {
			parallelClasses.addAll(incReasoner.getSubClasses(clazz, true).getFlattened());
		}
		parallelClasses.remove(newCl);
		// remove axiom again (otherwise internal DL-Learner state would be corrupt!) 
		manager.applyChange(new RemoveAxiom(ontology, eq));
		String timeStr = Helper.prettyPrintNanoSeconds(System.nanoTime()-startTime);
		
		if(!consistent) {
			System.out.println("Adding the axiom renders the ontology inconsistent.");
		} else {
			System.out.println("\nSuper classes of " + newCl + ":");
			System.out.println(superClasses);
			System.out.println("\nSub classes of " + newCl + ":");
			System.out.println(subClasses);
			System.out.println("\nParallel classes of " + newCl + ":");
			System.out.println(parallelClasses);
			System.out.println("\nReasoning time: " + timeStr);
		}
	}
	
	private static void useOWLAPIReasoner() throws MalformedURLException, ComponentInitException{
		System.out.println("Using DL-Learner OWLAPIReasoner");
		// some random OWL file
		String url = "http://morpheus.cs.umbc.edu/aks1/ontosem.owl";
		// create reasoner
		ComponentManager cm = ComponentManager.getInstance();
		AbstractKnowledgeSource ks = new OWLFile(new URL(url));
		OWLAPIReasoner reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		ks.init();
		reasoner.init();
		System.out.println("Loaded ontology " + url + ".");
		
		// create a definition
		NamedClass nc1 = new NamedClass("http://morpheus.cs.umbc.edu/aks1/ontosem.owl#christianity");
		NamedClass nc2 = new NamedClass("http://morpheus.cs.umbc.edu/aks1/ontosem.owl#priest");
		NamedClass newNC = new NamedClass("http://nke.aksw.org/new");
		ObjectProperty op1 = new ObjectProperty("http://morpheus.cs.umbc.edu/aks1/ontosem.owl#operated-by");
		Description d1 = new ObjectSomeRestriction(op1, nc2);
		Description d2 = new Intersection(nc1, d1);
		EquivalentClassesAxiom eq = new EquivalentClassesAxiom(newNC,d2);
		
		System.out.println("Adding " + eq + ".");
		long startTime = System.nanoTime();
		OWLOntologyManager manager = reasoner.getManager();
		OWLOntology ontology = reasoner.getOntology();
		OWLReasoner internalReasoner = reasoner.getReasoner();
		OWLAxiom axiomOWLAPI = OWLAPIAxiomConvertVisitor.convertAxiom(eq);
		manager.applyChange(new AddAxiom(ontology, axiomOWLAPI));
		// perform reasoning using OWL API
		boolean consistent = internalReasoner.isConsistent();
		OWLClassExpression newNCO = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(newNC);
		Set<OWLClass> superClasses = internalReasoner.getSuperClasses(newNCO, true).getFlattened();
		Set<OWLClass> subClasses = internalReasoner.getSubClasses(newNCO, true).getFlattened();
		Set<OWLClass> parallelClasses = new TreeSet<OWLClass>(); 
		for(OWLClass clazz : superClasses) {
			parallelClasses.addAll(internalReasoner.getSubClasses(clazz, true).getFlattened());
		}
		parallelClasses.remove(newNCO);
		// remove axiom again (otherwise internal DL-Learner state would be corrupt!) 
		manager.applyChange(new RemoveAxiom(ontology, axiomOWLAPI));
		String timeStr = Helper.prettyPrintNanoSeconds(System.nanoTime()-startTime);
		
		if(!consistent) {
			System.out.println("Adding the axiom renders the ontology inconsistent.");
		} else {
			System.out.println("\nSuper classes of " + newNC + ":");
			System.out.println(superClasses);
			System.out.println("\nSub classes of " + newNC + ":");
			System.out.println(subClasses);
			System.out.println("\nParallel classes of " + newNC + ":");
			System.out.println(parallelClasses);
			System.out.println("\nReasoning time: " + timeStr);
		}
	}

	public static void main(String[] args) throws MalformedURLException, ComponentInitException, OWLOntologyCreationException {
		useOWLAPIReasoner();
		usePelletIncremantalClassifier();
	}

}
