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
package org.dllearner.test.junit;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.KBFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.ELDown;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests related to the EL downward refinement operator.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDownTests {

	/**
	 * Implementation of test case created by Christoph Haase for 
	 * new operator.
	 * 
	 * @throws ParseException Thrown if concept syntax does not correspond
	 * to current KB syntax.
	 * @throws ComponentInitException 
	 */
	@Test
	public void refinementTest() throws ParseException, ComponentInitException {
		ComponentManager cm = ComponentManager.getInstance();
		
		// background knowledge
		String kbString = "";
		kbString += "OPDOMAIN(hasChild) = human.\n";
		kbString += "OPRANGE(hasChild) = human.\n";
		kbString += "OPDOMAIN(hasPet) = human.\n";
		kbString += "OPRANGE(hasPet) = animal.\n";
		kbString += "Subrole(hasChild, has).\n";
		kbString += "Subrole(hasPet, has).\n";
		kbString += "bird SUB animal.\n";
		kbString += "cat SUB animal.\n";
		kbString += "(human AND animal) = BOTTOM.\n";
		KB kb = KBParser.parseKBFile(kbString);
		
		// input description
		Description input = KBParser.parseConcept("(human AND EXISTS has.animal)");
		
		// create reasoner
		KBFile source = new KBFile(kb);
		ReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, source);
		ReasoningService rs = cm.reasoningService(rc);
		source.init();
		rc.init();
		// TODO there shouldn't be a need to call this explicitly!
		// (otherwise we get a NullPointerException, because the hierarchy is not created)
		rs.prepareSubsumptionHierarchy();
		rs.prepareRoleHierarchy();
		
		ELDown operator = new ELDown(rs);
		
		// desired refinements as strings
		Set<String> desiredString = new TreeSet<String>();
		desiredString.add("(human AND EXISTS hasPet.animal)");
		desiredString.add("(human AND EXISTS has.bird)");
		desiredString.add("(human AND EXISTS has.cat)");
		desiredString.add("((human AND EXISTS hasPet.TOP) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS hasChild.TOP) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS hasPet.TOP) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS has.person) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS has.EXISTS has.TOP) AND EXISTS has.animal)");
		desiredString.add("(human AND EXISTS has.(animal AND EXISTS has.TOP))");
		
		ConceptComparator cc = new ConceptComparator();
		SortedSet<Description> desired = new TreeSet<Description>(cc);
		for(String str : desiredString) {
			Description tmp = KBParser.parseConcept(str);
			// eliminate conjunctions nested in other conjunctions
			ConceptTransformation.cleanConcept(tmp);
			desired.add(tmp);
			System.out.println("desired: " + tmp);
		}
		
		// perform refinement and compare solutions
		Set<Description> refinements = operator.refine(input);
		
		// number of refinements has to be correct and each produced
		// refinement must be in the set of desired refinements
//		assertTrue(refinements.size() == desired.size());
		for(Description refinement : refinements) {
			System.out.println(refinement);
//			assertTrue(desired.contains(refinement));
		}
	}	
	
}
