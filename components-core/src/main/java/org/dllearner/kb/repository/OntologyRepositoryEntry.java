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
package org.dllearner.kb.repository;

import java.net.URI;

public interface OntologyRepositoryEntry {
	/**
     * Gets a short human readable name for this entry
     * @return A short human readable name
     */
    String getOntologyShortName();

    /**
     * Gets the URI of the ontology that is described by this entry.
     * @return The ontology URI.
     */
    URI getOntologyURI();

    /**
     * Gets the physical URI of the ontology that is described by this entry.
     * @return The physical URI.
     */
    URI getPhysicalURI();

    /**
     * Gets associated metadata.
     * @param key The key that describes the metadata
     * @return The metadata or <code>null</code> if there is no metadata associated with this key.
     */
    String getMetaData(Object key);
}
