/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 *
 */
public class AnnotatedTextDocument implements AnnotatedDocument{
	
	private TextDocument document;
	private Set<SemanticAnnotation> annotations;
	private Set<OWLEntity> entities;

	
	public AnnotatedTextDocument(TextDocument document, Set<SemanticAnnotation> annotations) {
		this.document = document;
		this.annotations = annotations;
		
		entities = annotations.stream()
				.map(SemanticAnnotation::getEntity)
				.collect(Collectors.toCollection(HashSet<OWLEntity>::new));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Document#getContent()
	 */
	@Override
	public String getContent() {
		return document.getContent();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Document#getRawContent()
	 */
	@Override
	public String getRawContent() {
		return document.getRawContent();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Document#getPOSTaggedContent()
	 */
	@Override
	public String getPOSTaggedContent() {
		return document.getPOSTaggedContent();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getContainedEntities()
	 */
	@Override
	public Set<OWLEntity> getContainedEntities() {
		return entities;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getAnnotations()
	 */
	@Override
	public Set<SemanticAnnotation> getAnnotations() {
		return annotations;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getEntityFrequency(org.dllearner.core.owl.Entity)
	 */
	@Override
	public int getEntityFrequency(OWLEntity entity) {
		int cnt = 0;
		for (SemanticAnnotation annotation : annotations) {
			if(annotation.getEntity().equals(entity)){
				cnt++;
			}
		}
		return cnt;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Document:" + document.getContent() + "\nAnnotations:" + annotations;
	}

}
