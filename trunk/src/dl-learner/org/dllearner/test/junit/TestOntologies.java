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

import java.io.File;
import java.net.MalformedURLException;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
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

	public enum TestOntology { EMPTY, SIMPLE, SIMPLE_NO_DR, SIMPLE_NO_DISJOINT, SIMPLE_NO_DR_DISJOINT, SIMPLE2, SIMPLE3, R1SUBR2, DATA1, FIVE_ROLES, FATHER_OE, CARCINOGENESIS, EPC_OE };
	
	public static ReasonerComponent getTestOntology(TestOntology ont) {
		String kbString = "";
		String owlFile = "";
		
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
		} else if(ont.equals(TestOntology.SIMPLE_NO_DR)) {
			kbString += "Subrole(hasChild, has).\n";
			kbString += "Subrole(hasPet, has).\n";
			kbString += "bird SUB animal.\n";
			kbString += "cat SUB animal.\n";
			kbString += "(human AND animal) = BOTTOM.\n";
		} else if(ont.equals(TestOntology.SIMPLE_NO_DISJOINT)) {
			kbString += "OPDOMAIN(hasChild) = human.\n";
			kbString += "OPRANGE(hasChild) = human.\n";
			kbString += "OPDOMAIN(hasPet) = human.\n";
			kbString += "OPRANGE(hasPet) = animal.\n";
			kbString += "Subrole(hasChild, has).\n";
			kbString += "Subrole(hasPet, has).\n";
			kbString += "bird SUB animal.\n";
			kbString += "cat SUB animal.\n";
			kbString += "human SUB TOP.\n";
		} else if(ont.equals(TestOntology.SIMPLE_NO_DR_DISJOINT)) {
			kbString += "Subrole(hasChild, has).\n";
			kbString += "Subrole(hasPet, has).\n";
			kbString += "bird SUB animal.\n";
			kbString += "cat SUB animal.\n";
			kbString += "human SUB TOP.\n";			
		} else if(ont.equals(TestOntology.SIMPLE2)) {
			kbString += "Subrole(r2,r3).\n";
			kbString += "a1 SUB TOP.\n";
			kbString += "a2 SUB a3.\n";
			kbString += "r1(a,b).\n"; // we have to declare r1
		} else if(ont.equals(TestOntology.SIMPLE3)) {
			kbString += "a1 SUB a2.\n";
			kbString += "Subrole(r1,r2).\n";
		} else if(ont.equals(TestOntology.R1SUBR2)) {
			kbString += "Subrole(r1,r2).\n";
			kbString += "a1 SUB TOP.\n";
			kbString += "a2 SUB TOP.\n";
		} else if(ont.equals(TestOntology.DATA1)) {
			kbString += "man SUB person.\n";
			kbString += "woman SUB person.\n";
			kbString += "man(eric).\n";
			kbString += "woman(diana).\n";
			kbString += "married(eric,diana).\n";
			kbString += "hasChild(eric,frank).\n";
			kbString += "hasChild(eric,tim).\n";
		} else if(ont.equals(TestOntology.FIVE_ROLES)) {
			// we only define five roles, no TBox
			kbString += "r1(a,b).\n";
			kbString += "r2(a,b).\n";
			kbString += "r3(a,b).\n";
			kbString += "r4(a,b).\n";
			kbString += "r5(a,b).\n";
		} else if(ont.equals(TestOntology.FATHER_OE)) {
			owlFile = "examples/family/father_oe.owl";
		} else if(ont.equals(TestOntology.CARCINOGENESIS)) {
			owlFile = "examples/carcinogenesis/carcinogenesis.owl";
		} else if(ont.equals(TestOntology.EPC_OE)) {
			owlFile = "examples/epc/sap_epc_oe.owl";
		}
		
		try {	
			ComponentManager cm = ComponentManager.getInstance();
			KnowledgeSource source;
			
			// parse KB string if one has been specified
			if(!kbString.isEmpty()) {
				KB kb = KBParser.parseKBFile(kbString);
				source = new KBFile(kb);
			// parse OWL file otherwise
			} else {
				source = cm.knowledgeSource(OWLFile.class);
				try {
					cm.applyConfigEntry(source, "url", new File(owlFile).toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}			
			}
			
			ReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, source);
			source.init();
			rc.init();
			return rc;	
		} catch(ParseException e) {
			e.printStackTrace();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		throw new Error("Test ontology could not be created.");	
	}
	
}
