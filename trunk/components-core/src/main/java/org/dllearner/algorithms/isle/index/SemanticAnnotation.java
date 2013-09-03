/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public class SemanticAnnotation extends Annotation{
	
	private Entity entity;
	
	public SemanticAnnotation(Document getReferencedDocument, Entity entity, int offset, int length) {
		super(getReferencedDocument, offset, length);
		this.entity = entity;
	}

	public Entity getEntity() {
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


	

}
