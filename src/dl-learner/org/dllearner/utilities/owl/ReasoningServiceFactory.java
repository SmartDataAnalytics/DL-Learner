/**
 * Copyright (C) 2008, Jens Lehmann
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

package org.dllearner.utilities.owl;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
 * This class should maybe be included in ReasonerComponent
 * 
 * @author Sebastian Hellmann
 *
 */
public class ReasonerComponentFactory {

	public enum AvailableReasoners {FASTINSTANCECHECKER, OWLAPIREASONERPELLET, OWLAPIREASONERFACT};
	
	public static void main(String[] args) {
		String ontologyURL = "examples/arch/arch.owl";
		System.out.println(ontologyURL);
		
		ReasonerComponent rs;
		for (AvailableReasoners r : AvailableReasoners.values()) {
			System.out.println(r);
			rs = getReasonerComponent(ontologyURL, r);
			List<NamedClass> l = rs.getAtomicConceptsList();
			System.out.println(l);
			l = rs.getAtomicConceptsList(true);
			
			System.out.println(l);
		}
		
		
	}
	
	public static ReasonerComponent getReasonerComponent (String ontologyFile, AvailableReasoners r ){
		ReasonerComponent rs = null;
		try{
		// the component manager is the central object to create
		// and configure components
		ComponentManager cm = ComponentManager.getInstance();

		// knowledge source
		KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
		URL fileURL = new File(ontologyFile).toURI().toURL();
		cm.applyConfigEntry(ks, "url", fileURL);
		ks.init();
		
		// reasoner
		ReasonerComponent rc;
		switch (r) {
	      case FASTINSTANCECHECKER:
	    	  rc = cm.reasoner(FastInstanceChecker.class, ks);
	    	  break;
	      case OWLAPIREASONERFACT:
	    	  rc = cm.reasoner(OWLAPIReasoner.class, ks);
	    	  ((OWLAPIReasoner)rc).getConfigurator().setReasonerType("fact");
	    	  break;
	      case OWLAPIREASONERPELLET:
	    	  rc = cm.reasoner(OWLAPIReasoner.class, ks);
	    	  ((OWLAPIReasoner)rc).getConfigurator().setReasonerType("pellet");
	    	  break;
	      default:
	    	  rc = cm.reasoner(FastInstanceChecker.class, ks);
		      break;
	    }

		
		rs = cm.reasoningService(rc);
		rc.init();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	
	
	
}
