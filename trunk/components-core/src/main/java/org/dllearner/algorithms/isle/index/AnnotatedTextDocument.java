/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public class AnnotatedTextDocument implements AnnotatedDocument{
	
	private TextDocument document;
	private Set<Annotation> annotations;
	private Set<Entity> entities;

	
	public AnnotatedTextDocument(TextDocument document, Set<Annotation> annotations) {
		this.document = document;
		this.annotations = annotations;
		
		entities = new HashSet<Entity>();
		for (Annotation annotation : annotations) {
			entities.add(annotation.getEntity());
		}
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
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getContainedEntities()
	 */
	@Override
	public Set<Entity> getContainedEntities() {
		return entities;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getAnnotations()
	 */
	@Override
	public Set<Annotation> getAnnotations() {
		return annotations;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getAnnotation(int, int)
	 */
	@Override
	public Annotation getAnnotation(int offset, int length) {
		for (Annotation annotation : annotations) {
			if(annotation.getOffset() == offset && annotation.getLength() == length){
				return annotation;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.AnnotatedDocument#getEntityFrequency(org.dllearner.core.owl.Entity)
	 */
	@Override
	public int getEntityFrequency(Entity entity) {
		int cnt = 0;
		for (Annotation annotation : annotations) {
			if(annotation.getEntity().equals(entity)){
				cnt++;
			}
		}
		return cnt;
	}

}
