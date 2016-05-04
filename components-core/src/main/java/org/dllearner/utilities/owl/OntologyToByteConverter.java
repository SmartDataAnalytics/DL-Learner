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

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/14/12
 * Time: 7:30 PM
 * <p/>
 * Interface to allow the conversion of an OWL Ontology into a byte array and back.
 * <p/>
 * The purpose of the interface is to allow the association of an OWLOntology object with a specified OWLOntologyManager.
 * <p/>
 * If someone hands us an OWLOntology, we may not want to use the associated OWLOntologyManager.  Rather, we can serialize it out
 * to a byte array and then read it back in with a different OWLOntologyManager.
 */
public interface OntologyToByteConverter {

    /**
     * Convert the ontology into a byte array.
     *
     * @param ontology The ontology to convert to a byte array.
     * @return The byte array representing the ontology
     */
    byte[] convert(OWLOntology ontology);

    /**
     * Convert bytes into an Ontology registered with manager.
     *
     * @param bytes   The bytes to convert to an OWLOntology
     * @param manager The Ontology Manager to load the ontology with.
     * @return The ontology derived from bytes.
     */
    OWLOntology convert(byte[] bytes, OWLOntologyManager manager);
}
