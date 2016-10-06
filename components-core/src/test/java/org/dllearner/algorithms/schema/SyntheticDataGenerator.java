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
package org.dllearner.algorithms.schema;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorenz Buehmann 
 * @since Oct 25, 2014
 */
public class SyntheticDataGenerator {
	
	private static final String NS = "http://dl-learner.org/data/";
	
	PrefixManager pm = new DefaultPrefixManager();

	IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
	OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	OWLDataFactory df = man.getOWLDataFactory();
	
	char classCharacter = 'A';
	
	RandomDataGenerator rnd = new RandomDataGenerator();
	
	OWLOntology ontology;

	public SyntheticDataGenerator() {
		pm.setDefaultPrefix(NS);
	}
	
	
	/**
	 * The simplest case: 
	 * n classes 
	 * connected in a chain 
	 * by one single property
	 * all with the same strength of connectivity
	 * @param nrOfClasses
	 */
	public OWLOntology createData(int nrOfClasses, int nrOfConnectionsBetweenClasses){
		try {
			ontology = man.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		// create the classes first
		List<OWLClass> classes = new ArrayList<>(nrOfClasses);
		for (int i = 0; i < nrOfClasses; i++) {
			OWLClass cls = createClass(String.valueOf(classCharacter++));
			classes.add(cls);
		}
		
		// create the single property
		OWLObjectProperty property = createObjectProperty("p");
		
		// create chain of classes
		for (int i = 0; i < classes.size()-1; i++) {
			OWLClass source = classes.get(i);
			OWLClass target = classes.get(i+1);
			connect(source, target, property, nrOfConnectionsBetweenClasses);
		}
		return ontology;
	}
	
	/**
	 * Connect sourceClass CS with targetClass CT by property P, i.e. add
	 * statements P(cs_i, ct_j) with i,j <= n
	 */
	private void connect(OWLClass sourceClass, OWLClass targetClass, OWLObjectProperty property, int nrOfConnections){
		// create individuals for sourceClass
		List<OWLIndividual> sourceIndividuals = addIndividuals(sourceClass, nrOfConnections);
		
		// create individuals for targetClass
		List<OWLIndividual> targetIndividuals = addIndividuals(targetClass, nrOfConnections);
		
		// add connections
		for (int i = 0; i < nrOfConnections; i++) {
			man.addAxiom(ontology, 
					df.getOWLObjectPropertyAssertionAxiom(
							property, 
							sourceIndividuals.get(i), 
							targetIndividuals.get(i)));
		}
	}
	
	/**
	 * Add n fresh individuals to given class.
	 */
	private List<OWLIndividual> addIndividuals(OWLClass cls, int n){
		List<OWLIndividual> individuals = new ArrayList<>();
		
		for (int i = 0; i < n; i++) {
			OWLIndividual ind = df.getOWLNamedIndividual(sfp.getShortForm(cls.getIRI()).toLowerCase() + i, pm);
			individuals.add(ind);
			man.addAxiom(ontology, df.getOWLClassAssertionAxiom(cls, ind));
		}
		
		return individuals;
	}
	
	private OWLClass createClass(String name){
		return df.getOWLClass(name, pm);
	}
	
	private OWLObjectProperty createObjectProperty(String name){
		return df.getOWLObjectProperty(name, pm);
	}
	
	private OWLDataProperty createDataProperty(String name){
		return df.getOWLDataProperty(name, pm);
	}
	
	private OWLIndividual createIndividual(String name){
		return df.getOWLNamedIndividual(name, pm);
	}
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		SyntheticDataGenerator dataGenerator = new SyntheticDataGenerator();
		
		OWLOntology ontology = dataGenerator.createData(3, 10);
		System.out.println(ontology.getLogicalAxioms());
	}

}
