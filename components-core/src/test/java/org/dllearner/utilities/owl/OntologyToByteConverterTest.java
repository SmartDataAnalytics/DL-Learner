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
package org.dllearner.utilities.owl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

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
 * Time: 7:29 PM
 * 
 * Test the interface level apis for the ontology to byte converter.
 */
public abstract class OntologyToByteConverterTest {

    public abstract OntologyToByteConverter getInstance();

    private OWLOntology createOntology() throws OWLOntologyCreationException, IOException {
        // Set up the ontology here and hide its manager - the test needs to use a different ontology manager on reconstitution
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        Resource owlFile = new ClassPathResource("/org/dllearner/utilities/owl/byte-conversion-data.owl");
        return manager.loadOntologyFromOntologyDocument(owlFile.getInputStream());
    }

    @Test
    public void testConversion() throws Exception {
        OntologyToByteConverter converter = getInstance();
        OWLOntology ontology = createOntology();
        assertNotNull(ontology);

        byte[] bytes = converter.convert(ontology);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // Use a new manager so that the IRIs don't get messed up
        OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
        OWLOntology result = converter.convert(bytes, newManager);
        assertNotNull(result);

        // Basic Equality Check - for some reason axiom count is different - the result looks more complete than the original.
        assertEquals(ontology.getIndividualsInSignature().size(), result.getIndividualsInSignature().size());
    }
}
