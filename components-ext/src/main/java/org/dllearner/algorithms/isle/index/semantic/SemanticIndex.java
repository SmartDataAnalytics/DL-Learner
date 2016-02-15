package org.dllearner.algorithms.isle.index.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public class SemanticIndex extends HashMap<OWLEntity, Set<AnnotatedDocument>> implements Index{

    private int nrOfDocuments;

    /**
     * Returns the set of annotated documents which reference the given entity using one of its surface forms.
     *
     * @param entity entity to retrieve documents
     * @return documents referencing given entity
     */
    @Override
    public Set<AnnotatedDocument> getDocuments(OWLEntity entity) {
        Set<AnnotatedDocument> annotatedDocuments = get(entity);
        if (annotatedDocuments == null) {
            annotatedDocuments = new HashSet<>();
        }
        return annotatedDocuments;
    }

    /**
     * Returns the number of documents for the given entity.
     *
     * @param entity entity to return number of referencing documents for
     * @return number of documents for the given entity in this index
     */
    public int getNrOfDocumentsFor(OWLEntity entity) {
        return get(entity).size();
    }
    
    /**
	 * @param nrOfDocuments the nrOfDocuments to set
	 */
	public void setTotalNrOfDocuments(int nrOfDocuments) {
		this.nrOfDocuments = nrOfDocuments;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getTotalNumberOfDocuments()
	 */
	@Override
	public long getTotalNumberOfDocuments() {
		return nrOfDocuments;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getNumberOfDocumentsFor(org.dllearner.core.owl.Entity)
	 */
	@Override
	public long getNumberOfDocumentsFor(OWLEntity entity) {
		return getDocuments(entity).size();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getNumberOfDocumentsFor(org.dllearner.core.owl.Entity[])
	 */
	@Override
	public long getNumberOfDocumentsFor(OWLEntity... entities) {
		
		Set<AnnotatedDocument> documents = getDocuments(entities[0]);
		for (int i = 1; i < entities.length; i++) {
			documents.retainAll(getDocuments(entities[i]));
		}
		return 0;
	}

}
