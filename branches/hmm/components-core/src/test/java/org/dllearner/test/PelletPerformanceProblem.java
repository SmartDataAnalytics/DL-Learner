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

package org.dllearner.test;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author Jens Lehmann
 *
 */
public class PelletPerformanceProblem {

	public static void main(String[] args) throws OWLOntologyCreationException {
		Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
		pelletLogger.setLevel(Level.WARN);		
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        File f = new File("examples/epc/conf/sap_modell_komplett_2.owl");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
        OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology, new SimpleConfiguration());
        System.out.println("ontology loaded");
        
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
	}
	
}
