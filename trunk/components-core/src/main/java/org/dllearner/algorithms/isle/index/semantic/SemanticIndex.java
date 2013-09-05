package org.dllearner.algorithms.isle.index.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.dllearner.algorithms.isle.WordSenseDisambiguation;
import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.algorithms.isle.index.LinguisticAnnotator;
import org.dllearner.algorithms.isle.index.SemanticAnnotator;
import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public abstract class SemanticIndex {


    private static final Logger logger = Logger.getLogger(SemanticIndex.class.getName());

    private SemanticAnnotator semanticAnnotator;
    private SyntacticIndex syntacticIndex;
    private Map<Entity, Set<AnnotatedDocument>> index;
    private OWLOntology ontology;
    
    private int size = 0;

    public SemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex, WordSenseDisambiguation wordSenseDisambiguation,
                         EntityCandidateGenerator entityCandidateGenerator, LinguisticAnnotator linguisticAnnotator) {
        this.ontology = ontology;
        this.syntacticIndex = syntacticIndex;
        semanticAnnotator = new SemanticAnnotator(wordSenseDisambiguation, entityCandidateGenerator, linguisticAnnotator);
    }

    public SemanticIndex(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * @param semanticAnnotator the semanticAnnotator to set
     */
    public void setSemanticAnnotator(SemanticAnnotator semanticAnnotator) {
        this.semanticAnnotator = semanticAnnotator;
    }

    /**
     * Precompute the whole index, i.e. iterate over all entities and compute all annotated documents.
     */
    public void buildIndex(Set<TextDocument> documents) {
        if (semanticAnnotator == null) {
            throw new RuntimeException("No semantic annotator defined, must be set using the setSemanticAnnotator method");
        }
        logger.info("Creating semantic index...");
        index = new HashMap<Entity, Set<AnnotatedDocument>>();
        for (TextDocument document : documents) {
            logger.info("Processing document:\n" + document);
            AnnotatedDocument annotatedDocument = semanticAnnotator.processDocument(document);
            for (Entity entity : annotatedDocument.getContainedEntities()) {
                Set<AnnotatedDocument> existingAnnotatedDocuments = index.get(entity);
                if (existingAnnotatedDocuments == null) {
                    existingAnnotatedDocuments = new HashSet<AnnotatedDocument>();
                    index.put(entity, existingAnnotatedDocuments);
                }
                existingAnnotatedDocuments.add(annotatedDocument);
            }
            logger.info("Annotated document:" + annotatedDocument);
        }
        size = documents.size();
        logger.info("...done.");
    }

    public void buildIndex(OWLAnnotationProperty annotationProperty, String language) {
        Set<OWLEntity> schemaEntities = new HashSet<OWLEntity>();
        schemaEntities.addAll(ontology.getClassesInSignature());
        schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
        schemaEntities.addAll(ontology.getDataPropertiesInSignature());
        Set<TextDocument> documents = new HashSet<TextDocument>();
        for (OWLEntity entity : schemaEntities) {
            String label = null;
            Set<OWLAnnotation> annotations = entity.getAnnotations(ontology, annotationProperty);
            for (OWLAnnotation annotation : annotations) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    if (language != null) {
                        if (val.hasLang(language)) {
                            label = val.getLiteral();
                        }

                    }
                    else {
                        label = val.getLiteral();
                    }
                }
            }
            if (label != null) {
                documents.add(new TextDocument(label));
            }
        }
        buildIndex(documents);
    }

    /**
     * Returns the set of annotated documents which reference the given entity using one of its surface forms.
     *
     * @param entity entity to retrieve documents
     * @return documents referencing given entity
     */
    public Set<AnnotatedDocument> getDocuments(Entity entity) {
        if (index == null) {
            System.err.println("You have to prebuild the index before you can use this method.");
            System.exit(1);
        }

        Set<AnnotatedDocument> annotatedDocuments = index.get(entity);
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
    public int count(Entity entity) {
        return index.get(entity).size();
    }

    /**
     * Returns the total number of documents contained in the index.
     *
     * @return the total number of documents contained in the index
     */
    public int getSize() {
        return size;
    }
}
