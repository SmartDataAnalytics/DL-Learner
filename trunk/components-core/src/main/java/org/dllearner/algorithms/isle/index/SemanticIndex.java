package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

import java.util.Set;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public interface SemanticIndex {
    /**
     * Returns the set of documents which reference the given entity using one of its surface forms.
     *
     * @param entity entity to retrieve documents
     * @return documents referencing given entity
     */
    public Set<Document> getDocuments(Entity entity);

    /**
     * Returns the number of documents for the given entity.
     *
     * @param entity entity to return number of referencing documents for
     * @return number of documents for the given entity in this index
     */
    public int count(Entity entity);

    /**
     * Returns the total number of documents contained in the index.
     *
     * @return the total number of documents contained in the index
     */
    public int getSize();
}
