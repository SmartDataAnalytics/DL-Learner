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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/13/12
 * Time: 6:24 PM
 * 
 * A Byte Array based implementation of the OntologyToByteConverter interface.
 */
public class SimpleOntologyToByteConverter implements OntologyToByteConverter {

    @Override
    public byte[] convert(OWLOntology ontology) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        try {
            manager.saveOntology(ontology, baos);
            baos.close();
        } catch (OWLOntologyStorageException | IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    @Override
    public  OWLOntology convert(byte[] bytes, OWLOntologyManager manager) {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(bais);
            bais.close();
            return ontology;
        } catch (OWLOntologyCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
