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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.junit.Test;

/**
 * A suite of JUnit tests related to refinement operators.
 * 
 * @author Jens Lehmann
 *
 */
public class RefinementOperatorTests {

	private String baseURI;
	
	/**
	 * Applies the RhoDRDown operator to a concept and checks that the number of
	 * refinements is correct.
	 *
	 */
	@Test
	public void rhoDRDownTest() {
		try {
			String file = "examples/carcinogenesis/carcinogenesis.owl";
			ComponentManager cm = ComponentManager.getInstance();
			KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
			try {
				cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
			} catch (MalformedURLException e) {
				// should never happen
				e.printStackTrace();
			}
			ks.init();
			ReasonerComponent rc = cm.reasoner(OWLAPIReasoner.class, ks);
			rc.init();
			baseURI = rc.getBaseURI();
//			ReasonerComponent rs = cm.reasoningService(rc);
			
			// TODO the following two lines should not be necessary
//			rs.prepareSubsumptionHierarchy();
//			rs.prepareRoleHierarchy();
			
			RhoDRDown op = new RhoDRDown(rc);
			Description concept = KBParser.parseConcept(uri("Compound"));
			Set<Description> results = op.refine(concept, 4, null);

			for(Description result : results) {
				System.out.println(result);
			}
			
			int desiredResultSize = 141;
			if(results.size() != desiredResultSize) {
				System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
			}
			assertTrue(results.size()==desiredResultSize);
		} catch(ComponentInitException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private String uri(String name) {
		return "\""+baseURI+name+"\"";
	}
}
