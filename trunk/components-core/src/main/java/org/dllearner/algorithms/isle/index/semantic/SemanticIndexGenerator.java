package org.dllearner.algorithms.isle.index.semantic;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.dllearner.algorithms.isle.TextDocumentGenerator;
import org.dllearner.algorithms.isle.index.*;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.dllearner.algorithms.isle.wsd.StructureBasedWordSenseDisambiguation;
import org.dllearner.algorithms.isle.wsd.WindowBasedContextExtractor;
import org.dllearner.algorithms.isle.wsd.WordSenseDisambiguation;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public abstract class SemanticIndexGenerator {

	static HashFunction hf = Hashing.md5();
    private static final Logger logger = Logger.getLogger(SemanticIndexGenerator.class.getName());
    private static boolean useCache = false;
    
    public static SemanticIndex generateIndex(Set<String> documents, OWLOntology ontology, WordSenseDisambiguation wordSenseDisambiguation,
                         EntityCandidateGenerator entityCandidateGenerator, LinguisticAnnotator linguisticAnnotator){
    	SemanticAnnotator semanticAnnotator = new SemanticAnnotator(wordSenseDisambiguation, entityCandidateGenerator, linguisticAnnotator);
    	return generateIndex(documents, ontology, semanticAnnotator);
    }
    
    public static SemanticIndex generateIndex(Set<String> documents, OWLOntology ontology, SemanticAnnotator semanticAnnotator){
    	SemanticIndex semanticIndex;
    	//try to load serialized version
    	HashCode hc = hf.newHasher().putInt(documents.hashCode()).putInt(ontology.hashCode()).hash();
    	File file = new File(hc.toString() + ".ser");
    	if(useCache && file.exists()){
    		try {
    			logger.info("Loading semantic index from disk...");
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				semanticIndex = (SemanticIndex) ois.readObject();
				ois.close();
				logger.info("...done.");
			} catch (Exception e) {
				e.printStackTrace();
				semanticIndex = buildIndex(semanticAnnotator, documents);
			} 
    	} else {
    		logger.info("Building semantic index...");
    		semanticIndex = buildIndex(semanticAnnotator, documents);
    		try {
    			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
    			oos.writeObject(semanticIndex);
    			oos.close();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
    		logger.info("...done.");
    	}
    	return semanticIndex;
    }
    
    public static SemanticIndex generateIndex(Set<String> documents, OWLOntology ontology, boolean useWordNormalization){
    	SimpleEntityCandidatesTrie trie;
        trie = new SimpleEntityCandidatesTrie(new RDFSLabelEntityTextRetriever(ontology),
                    ontology);
        trie.printTrie();
        
        TrieLinguisticAnnotator linguisticAnnotator = new TrieLinguisticAnnotator(trie);
        linguisticAnnotator.setNormalizeWords(useWordNormalization);
        
        SemanticAnnotator semanticAnnotator = new SemanticAnnotator(
                new StructureBasedWordSenseDisambiguation(new WindowBasedContextExtractor(), ontology),
                new TrieEntityCandidateGenerator(ontology, trie),
                linguisticAnnotator);
        return generateIndex(documents, ontology, semanticAnnotator);
    }
    
    public static SemanticIndex generateIndex(OWLOntology ontology, OWLAnnotationProperty annotationProperty, String language, boolean useWordNormalization){
    	Set<OWLEntity> schemaEntities = new HashSet<OWLEntity>();
        schemaEntities.addAll(ontology.getClassesInSignature());
        schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
        schemaEntities.addAll(ontology.getDataPropertiesInSignature());
        Set<String> documents = new HashSet<String>();
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
                documents.add(label);
            }
        }
        return generateIndex(documents, ontology, useWordNormalization);
    }

    /**
     * Precompute the whole index, i.e. iterate over all entities and compute all annotated documents.
     */
    private static SemanticIndex buildIndex(SemanticAnnotator semanticAnnotator, Set<String> documents) {
        logger.info("Creating semantic index...");
    	SemanticIndex index = new SemanticIndex();
        for (String document : documents) {
            if (document.isEmpty()) {
                continue;
            }
            TextDocument textDocument = TextDocumentGenerator.getInstance().generateDocument(document);
            logger.debug("Processing document:" + textDocument);
            AnnotatedDocument annotatedDocument = semanticAnnotator.processDocument(textDocument);
            for (Entity entity : annotatedDocument.getContainedEntities()) {
                Set<AnnotatedDocument> existingAnnotatedDocuments = index.get(entity);
                if (existingAnnotatedDocuments == null) {
                    existingAnnotatedDocuments = new HashSet<AnnotatedDocument>();
                    index.put(entity, existingAnnotatedDocuments);
                }
                existingAnnotatedDocuments.add(annotatedDocument);
            }
            logger.debug("Annotated document:" + annotatedDocument);
        }
        int size = documents.size();
        index.setTotalNrOfDocuments(size);
        logger.info("...done.");
        return index;
    }
}
