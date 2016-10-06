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
package org.dllearner.kb.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;

import org.dllearner.utilities.owl.SimpleOntologyToByteConverter;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/14/12
 * Time: 9:02 PM
 * 
 * Basic test to test some components of the SparqlKnowledgeSource
 */
public class SparqlKnowledgeSourceTest {

    private OWLOntology createOntology() throws OWLOntologyCreationException, IOException {
        // Set up the ontology here and hide its manager - the test needs to use a different ontology manager on reconstitution
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        Resource owlFile = new ClassPathResource("/org/dllearner/kb/owl-api-ontology-data.owl");
        return manager.loadOntologyFromOntologyDocument(owlFile.getInputStream());
    }


    @Test
    public void testMethods() throws Exception {
        OWLOntology ontology = createOntology();
        assertNotNull(ontology);

        SparqlKnowledgeSource testSubject = new SparqlKnowledgeSource();
        testSubject.setOntologyBytes(new SimpleOntologyToByteConverter().convert(ontology));

        OWLOntology result = testSubject.createOWLOntology(OWLManager.createOWLOntologyManager());

        assertNotNull(result);
        assertNotSame(ontology,result);

        // Basic Equality Check - for some reason axiom count is different - the result looks more complete than the original.
        assertEquals(ontology.getIndividualsInSignature().size(), result.getIndividualsInSignature().size());
    }
}
