package org.dllearner.algorithms.isle.index.semantic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.dllearner.algorithms.isle.WordSenseDisambiguation;
import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.algorithms.isle.index.LinguisticAnnotator;
import org.dllearner.algorithms.isle.index.SemanticAnnotator;
import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public abstract class SemanticIndex {
	
	private SemanticAnnotator semanticAnnotator;
	private SyntacticIndex syntacticIndex;
	private Map<Entity, Set<AnnotatedDocument>> index;
	private OWLOntology ontology;
	
	public SemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex, WordSenseDisambiguation wordSenseDisambiguation, 
			EntityCandidateGenerator entityCandidateGenerator, LinguisticAnnotator linguisticAnnotator) {
				this.ontology = ontology;
				this.syntacticIndex = syntacticIndex;
				semanticAnnotator = new SemanticAnnotator(wordSenseDisambiguation, entityCandidateGenerator, linguisticAnnotator);
	}
	
	public SemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex, SemanticAnnotator semanticAnnotator) {
				this.semanticAnnotator = semanticAnnotator;
	}
	
	/**
	 * Precompute the whole index, i.e. iterate over all entities and compute all annotated documents.
	 */
	public void buildIndex(Set<TextDocument> documents){
		for (TextDocument document : documents) {
			AnnotatedDocument annotatedDocument = semanticAnnotator.processDocument(document);
			for (Entity entity : annotatedDocument.getContainedEntities()) {
				Set<AnnotatedDocument> existingAnnotatedDocuments = index.get(entity);
				if(existingAnnotatedDocuments == null){
					existingAnnotatedDocuments = new HashSet<AnnotatedDocument>();
					index.put(entity, existingAnnotatedDocuments);
				}
				existingAnnotatedDocuments.add(annotatedDocument);
			}
		}		
	}
	
    /**
     * Returns the set of annotated documents which reference the given entity using one of its surface forms.
     *
     * @param entity entity to retrieve documents
     * @return documents referencing given entity
     */
    public Set<AnnotatedDocument> getDocuments(Entity entity){
    	Set<AnnotatedDocument> annotatedDocuments = index.get(entity);
    	return annotatedDocuments;
    }

    /**
     * Returns the number of documents for the given entity.
     *
     * @param entity entity to return number of referencing documents for
     * @return number of documents for the given entity in this index
     */
    public int count(Entity entity){
    	return index.get(entity).size();
    }

    /**
     * Returns the total number of documents contained in the index.
     *
     * @return the total number of documents contained in the index
     */
    public int getSize(){
    	return index.size();
    }
}
