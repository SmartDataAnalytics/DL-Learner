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

import java.io.IOException;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.KBFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A suite of JUnit tests related to the DL-Learner reasoning.
 * 
 * @author Jens Lehmann
 * 
 */
public class ReasonerTests {

	private KB getSimpleKnowledgeBase() {
		String kb = "person SUB TOP.";
		kb += "man SUB person.";
		kb += "woman SUB person.";
		KB kbObject = null;
		try {
			kbObject = KBParser.parseKBFile(kb);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return kbObject;
	}

	@Test
	public void instanceCheckTest() {
		try {
			ComponentManager cm = ComponentManager.getInstance();
			KB kb = getSimpleKnowledgeBase();
			KnowledgeSource ks = new KBFile(kb);
			ks.init();
			ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
			reasoner.init();
			Description d;
			d = KBParser.parseConcept("man");
			Individual i = new Individual("alex");
			boolean result = reasoner.instanceCheck(d, i);
			assertFalse(result);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ReasoningMethodUnsupportedException e) {
			e.printStackTrace();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}

}
