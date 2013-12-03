package org.dllearner.algorithms.isle.index.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.core.owl.Entity;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public class SemanticIndex extends HashMap<Entity, Set<AnnotatedDocument>>{

    private int nrOfDocuments;

    /**
     * Returns the set of annotated documents which reference the given entity using one of its surface forms.
     *
     * @param entity entity to retrieve documents
     * @return documents referencing given entity
     */
    public Set<AnnotatedDocument> getDocuments(Entity entity) {
        Set<AnnotatedDocument> annotatedDocuments = get(entity);
        if (annotatedDocuments == null) {
            annotatedDocuments = new HashSet<AnnotatedDocument>();
        }
        return annotatedDocuments;
    }

    /**
     * Returns the number of documents for the given entity.
     *
     * @param entity entity to return number of referencing documents for
     * @return number of documents for the given entity in this index
     */
    public int getNrOfDocumentsFor(Entity entity) {
        return get(entity).size();
    }
    
    /**
	 * @param nrOfDocuments the nrOfDocuments to set
	 */
	public void setTotalNrOfDocuments(int nrOfDocuments) {
		this.nrOfDocuments = nrOfDocuments;
	}
	
	/**
	 * @return the nrOfDocuments
	 */
	public int getTotalNrOfDocuments() {
		return nrOfDocuments;
	}

}
