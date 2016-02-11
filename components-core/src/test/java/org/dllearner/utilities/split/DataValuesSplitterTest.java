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
package org.dllearner.utilities.split;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class DataValuesSplitterTest {
	
	private static AbstractReasonerComponent reasoner;
	private static PosNegLP lp;
	private static OWLDataFactory df = new OWLDataFactoryImpl();

	@BeforeClass
	public static void init() throws Exception {
		String kb = "@prefix : <http://example.org/> .\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
				+ ":r a owl:DatatypeProperty ; rdfs:range xsd:nonNegativeInteger . ";
		
		for(int i = 1; i <= 20; i++) {
			kb += String.format(":p%d :r \"%d\"^^xsd:nonNegativeInteger .%n", i, i);
		}
		
		for(int i = 21; i <= 40; i++) {
			kb += String.format(":n%d :r \"%d\"^^xsd:nonNegativeInteger .%n", i, i);
		}
		
		for(int i = 41; i <= 60; i++) {
			kb += String.format(":p%d :r \"%d\"^^xsd:nonNegativeInteger .%n", i, i);
		}
		
		setup(kb);
	}
	
	private void createOptimizedTestKB() throws Exception {
		String kb = "@prefix : <http://example.org/> .\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
				+ ":r a owl:DatatypeProperty ; rdfs:range xsd:nonNegativeInteger . ";
		
			kb += ":n1 :r \"1\"^^xsd:nonNegativeInteger .";
			kb += ":n2 :r \"2\"^^xsd:nonNegativeInteger .";
			kb += ":n3 :r \"3\"^^xsd:nonNegativeInteger .";
			kb += ":n4 :r \"4\"^^xsd:nonNegativeInteger .";
			kb += ":p1 :r \"5\"^^xsd:nonNegativeInteger .";
			kb += ":p2 :r \"6\"^^xsd:nonNegativeInteger .";
			kb += ":p3 :r \"10\"^^xsd:nonNegativeInteger .";
			kb += ":p4 :r \"12\"^^xsd:nonNegativeInteger .";
			kb += ":p5 :r \"16\"^^xsd:nonNegativeInteger .";
			kb += ":p6 :r \"20\"^^xsd:nonNegativeInteger .";
			kb += ":n5 :r \"28\"^^xsd:nonNegativeInteger .";
			kb += ":n6 :r \"30\"^^xsd:nonNegativeInteger .";

			setup(kb);
	}
	
	private static void setup(String turtleString) throws Exception{
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				new ByteArrayInputStream(turtleString.getBytes(StandardCharsets.UTF_8)));
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();

		reasoner = new OWLAPIReasoner(ks);
		reasoner.init();

		// get examples
		Set<OWLIndividual> posExamples = new HashSet<>();
		Set<OWLIndividual> negExamples = new HashSet<>();

		for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
			if (ind.toStringID().startsWith("http://example.org/p")) {
				posExamples.add(ind);
			} else {
				negExamples.add(ind);
			}
		}

		// create learning problem
		lp = new PosNegLPStandard(reasoner);
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);
	}

	/**
	 * Test method for {@link org.dllearner.utilities.split.DefaultNumericValuesSplitter#computeSplits()}.
	 * @throws ComponentInitException 
	 */
	@Test
	public void testComputeSplitsDefault() throws Exception {
		ValuesSplitter splitter = new DefaultNumericValuesSplitter(reasoner, df);
		splitter.init();
		System.out.println(splitter.computeSplits());
	}
	
	/**
	 * Test method for {@link org.dllearner.utilities.split.DefaultNumericValuesSplitter#computeSplits()}.
	 * @throws ComponentInitException 
	 */
	@Test
	public void testComputeSplitsDefault2() throws Exception {
		String kb = "@prefix : <http://example.org/> .\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
				+ ":r a owl:DatatypeProperty ; rdfs:range xsd:nonNegativeInteger . \n";

		int i = 0;
		for (Integer value : Sets.newHashSet(1, 2, 3, 4, 5, 6, 10, 12, 16, 20, 28, 30)) {
			kb += String.format(":p%d :r \"%d\"^^xsd:nonNegativeInteger .\n", i++, value);
		}
		System.out.println(kb);

		setup(kb);
		DefaultNumericValuesSplitter splitter = new DefaultNumericValuesSplitter(reasoner, df);
		splitter.setMaxNrOfSplits(4);
		splitter.init();
		System.out.println(splitter.computeSplits());
	}
	
	/**
	 * Test method for {@link org.dllearner.utilities.split.OptimizedNumericValuesSplitter#computeSplits()}.
	 * @throws ComponentInitException 
	 */
	@Test
	public void testComputeSplitsOptimized() throws Exception {
		createOptimizedTestKB();
		ValuesSplitter splitter = new OptimizedNumericValuesSplitter(reasoner, df, lp);
		splitter.init();
		System.out.println(splitter.computeSplits());
	}

}
