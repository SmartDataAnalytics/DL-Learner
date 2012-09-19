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
 */

package org.dllearner.test.junit;

import static org.junit.Assert.*;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.FastInstanceChecker;
import org.junit.Test;

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
		FastInstanceChecker fic = new FastInstanceChecker();
		fic.setSources(file);
		fic.init();
		
		NamedClass doc = new NamedClass("http://nlp2rdf.lod2.eu/schema/string/Document");
		ObjectProperty op = new ObjectProperty("http://nlp2rdf.lod2.eu/schema/string/subStringTrans");
		ObjectSomeRestriction osr = new ObjectSomeRestriction(op,Thing.instance);
		Intersection is = new Intersection(doc,osr);
		
		Individual ind = new Individual("http://nlp2rdf.org/POS/2/offset_0_763_COPPER+STUDY+GROUP+C");
		
//		System.out.println();
		
		// there should be several subStringTrans relations 
		assertFalse(fic.getRelatedIndividuals(ind, op).isEmpty());
		// individual should be member of this expression (required to learn the correct concept)
		assertTrue(fic.hasType(is, ind));
			
	}
	
}
