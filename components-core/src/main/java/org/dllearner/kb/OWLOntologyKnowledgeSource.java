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
package org.dllearner.kb;

import org.dllearner.core.KnowledgeSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/11/12
 * Time: 6:36 PM
 *
 * This interface represents objects which can return an OWLOntology representation of itself.
 */
public interface OWLOntologyKnowledgeSource extends KnowledgeSource{

    /**
     * Create an OWL Ontology associated with the specified manager.
     *
     * @param manager The manager to associate the new ontology with.
     * @return The result ontology
     */
    OWLOntology createOWLOntology(OWLOntologyManager manager);
}
