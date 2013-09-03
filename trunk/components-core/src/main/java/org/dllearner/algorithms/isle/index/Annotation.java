/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public class Annotation {
	
	private Document getReferencedDocument;
	private Entity entity;
	private int offset;
	private int length;
	
	public Annotation(Document getReferencedDocument, Entity entity, int offset, int length) {
		this.getReferencedDocument = getReferencedDocument;
		this.entity = entity;
		this.offset = offset;
		this.length = length;
	}

	public Document getGetReferencedDocument() {
		return getReferencedDocument;
	}

	public Entity getEntity() {
		return entity;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((getReferencedDocument == null) ? 0 : getReferencedDocument.hashCode());
		result = prime * result + length;
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (getReferencedDocument == null) {
			if (other.getReferencedDocument != null)
				return false;
		} else if (!getReferencedDocument.equals(other.getReferencedDocument))
			return false;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	
	

}
