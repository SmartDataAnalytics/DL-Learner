/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleSemanticIndex implements SemanticIndex{
	
	private SyntacticIndex syntacticIndex;
	private RDFSLabelEntityTextRetriever labelRetriever;

	/**
	 * 
	 */
	public SimpleSemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex) {
		this.syntacticIndex = syntacticIndex;
		labelRetriever = new RDFSLabelEntityTextRetriever(ontology);
	}
	

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SemanticIndex#getDocuments(org.dllearner.core.owl.Entity)
	 */
	@Override
	public Set<String> getDocuments(Entity entity) {
		Set<String> documents = new HashSet<String>();
		Map<String, Double> relevantText = labelRetriever.getRelevantText(entity);
		
		for (Entry<String, Double> entry : relevantText.entrySet()) {
			String label = entry.getKey();
			documents.addAll(syntacticIndex.getDocuments(label));
		}
		
		return documents;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SemanticIndex#count(java.lang.String)
	 */
	@Override
	public int count(Entity entity) {
		return getDocuments(entity).size();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SemanticIndex#getSize()
	 */
	@Override
	public int getSize() {
		return syntacticIndex.getSize();
	}
	
	

}
