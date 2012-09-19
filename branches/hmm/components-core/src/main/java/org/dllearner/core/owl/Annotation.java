/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.core.owl;

import java.net.URI;
import java.util.Map;

/**
 * An annotation, e.g. rdfs:label "foo".
 * 
 * @author Jens Lehmann
 *
 */
public class Annotation implements KBElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 46761104877109257L;
	protected URI annotationURI;
	protected KBElement annotationValue;
	
	public Annotation(URI annotationURI, KBElement annotationValue) {
		this.annotationURI = annotationURI;
		this.annotationValue = annotationValue;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#accept(org.dllearner.core.owl.KBElementVisitor)
	 */
	@Override
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	@Override
	public int getLength() {
		return 1 + annotationValue.getLength();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toKBSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return annotationURI + annotationValue.toKBSyntaxString(baseURI, prefixes);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toString(String baseURI, Map<String, String> prefixes) {
		return annotationURI + annotationValue.toString(baseURI, prefixes);	
	}

	/**
	 * @return the annotationURI
	 */
	public URI getAnnotationURI() {
		return annotationURI;
	}

	/**
	 * @return the annotationValue
	 */
	public KBElement getAnnotationValue() {
		return annotationValue;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}

}
