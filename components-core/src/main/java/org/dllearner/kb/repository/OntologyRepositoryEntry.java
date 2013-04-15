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
