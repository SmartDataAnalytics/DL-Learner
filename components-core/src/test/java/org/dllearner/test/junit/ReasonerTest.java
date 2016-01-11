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
package org.dllearner.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class ReasonerTest {

	@Test
	public void nlp2rdfTest() throws ComponentInitException {
		// read file into reasoner
		OWLFile file = new OWLFile("src/test/resources/nlp2rdf/positives.owl");
		file.init();
		ClosedWorldReasoner fic = new ClosedWorldReasoner();
		fic.setSources(file);
		fic.init();
		
		OWLDataFactory df = new OWLDataFactoryImpl();
		OWLClass doc = df.getOWLClass(IRI.create("http://nlp2rdf.lod2.eu/schema/string/Document"));
		OWLObjectProperty op = df.getOWLObjectProperty(IRI.create("http://nlp2rdf.lod2.eu/schema/string/subStringTrans"));
		OWLObjectSomeValuesFrom osr = df.getOWLObjectSomeValuesFrom(op, df.getOWLThing());
		OWLObjectIntersectionOf is = df.getOWLObjectIntersectionOf(doc,osr);
		
		OWLIndividual ind = df.getOWLNamedIndividual(IRI.create("http://nlp2rdf.org/POS/2/offset_0_763_COPPER+STUDY+GROUP+C"));
		
//		System.out.println();
		
		// there should be several subStringTrans relations 
		assertFalse(fic.getRelatedIndividuals(ind, op).isEmpty());
		// individual should be member of this expression (required to learn the correct concept)
		assertTrue(fic.hasType(is, ind));
			
	}
	
}
