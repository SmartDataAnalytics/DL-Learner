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

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.KBFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.FastInstanceChecker;

/**
 * Some ontologies to simplify unit tests.
 * 
 * @author Jens Lehmann
 *
 */
public final class TestOntologies {

	public enum TestOntology { EMPTY, SIMPLE, SIMPLE2, R1SUBR2, DATA1 };
	
	public static ReasonerComponent getTestOntology(TestOntology ont) {
		String kbString = "";
		
		if(ont.equals(TestOntology.EMPTY)) {
			// no background knowledge
		} else if(ont.equals(TestOntology.SIMPLE)) {
			// background knowledge used in EL paper
			kbString += "OPDOMAIN(hasChild) = human.\n";
			kbString += "OPRANGE(hasChild) = human.\n";
			kbString += "OPDOMAIN(hasPet) = human.\n";
			kbString += "OPRANGE(hasPet) = animal.\n";
			kbString += "Subrole(hasChild, has).\n";
			kbString += "Subrole(hasPet, has).\n";
			kbString += "bird SUB animal.\n";
			kbString += "cat SUB animal.\n";
			kbString += "(human AND animal) = BOTTOM.\n";
		} else if(ont.equals(TestOntology.SIMPLE2)) {
			kbString += "Subrole(r2,r3).";
			kbString += "a2 SUB a4.";
		} else if(ont.equals(TestOntology.R1SUBR2)) {
			kbString += "Subrole(r1,r2).\n";
		} else if(ont.equals(TestOntology.DATA1)) {
			kbString += "man SUB person.\n";
			kbString += "woman SUB person.\n";
			kbString += "man(eric).\n";
			kbString += "woman(diana).\n";
			kbString += "married(eric,diana).\n";
			kbString += "hasChild(eric,frank).\n";
			kbString += "hasChild(eric,tim).\n";
		}
		
		try {	
			KB kb = KBParser.parseKBFile(kbString);
			
			// create reasoner
			ComponentManager cm = ComponentManager.getInstance();
			KBFile source = new KBFile(kb);
			ReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, source);
//			ReasonerComponent rs = cm.reasoningService(rc);
			source.init();
			rc.init();
			// TODO there shouldn't be a need to call this explicitly!
			// (otherwise we get a NullPointerException, because the hierarchy is not created)
//			rs.prepareSubsumptionHierarchy();
//			rs.prepareRoleHierarchy();
			return rc;	
		} catch(ParseException e) {
			e.printStackTrace();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		throw new Error("Test ontology could not be created.");	
	}
	
}
