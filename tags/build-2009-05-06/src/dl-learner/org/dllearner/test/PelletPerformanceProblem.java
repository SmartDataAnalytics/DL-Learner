/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.test;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * @author Jens Lehmann
 *
 */
public class PelletPerformanceProblem {

	public static void main(String[] args) throws OWLOntologyCreationException, OWLReasonerException {
		Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
		pelletLogger.setLevel(Level.WARN);		
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        File f = new File("examples/epc/conf/sap_modell_komplett_2.owl");
        URI physicalURI = f.toURI();
        OWLOntology ontology = manager.loadOntologyFromPhysicalURI(physicalURI);
        
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        ontologies.add(ontology);
        OWLReasoner reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);
        reasoner.loadOntologies(ontologies);
        System.out.println("ontology loaded");
        
        reasoner.classify();
        System.out.println("ontology classified");
        reasoner.realise();
        System.out.println("ontology realised");
	}
	
}
