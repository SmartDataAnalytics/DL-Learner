package org.dllearner.kb.repository;

import java.util.Collection;
import java.util.List;

public interface OntologyRepository {
	
	/**
     * Gets the name of the repository
     * @return A short name for the repository
     */
    String getName();

    /**
     * Gets a description of the location of the repository
     * @return A human readable description of the repository location
     */
    String getLocation();


    /**
     * Ensures the repository is up to date
     */
    void refresh();
    
    void initialize();

    Collection<OntologyRepositoryEntry> getEntries();
    
    List<Object> getMetaDataKeys();

}
