/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Lorenz Buehmann
 *
 */
public class SemanticAnnotation extends Annotation{
	
	private OWLEntity entity;
	
	public SemanticAnnotation(Annotation annotation, OWLEntity entity) {
		super(annotation.getReferencedDocument(), annotation.getTokens());
		this.entity = entity;
	}

	public OWLEntity getEntity() {
		return entity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SemanticAnnotation other = (SemanticAnnotation) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Annotation#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "->" + entity;
	}
	

}
