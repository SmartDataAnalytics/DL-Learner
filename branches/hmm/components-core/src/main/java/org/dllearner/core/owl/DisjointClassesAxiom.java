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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Jens Lehmann
 *
 */
public class DisjointClassesAxiom extends TerminologicalAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7788863077013583508L;
	private Collection<Description> descriptions;
	
	public DisjointClassesAxiom(Collection<Description> descriptions) {
		this.descriptions = descriptions;
	}
	
	public DisjointClassesAxiom(Description ... descriptions) {
		this.descriptions = Arrays.asList(descriptions);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		int length = 1;
		for(Description d : descriptions)
			length += d.getLength();
		return length;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		sb.append("DisjointClasses(");
		Iterator<Description> it = descriptions.iterator();
		while(it.hasNext()){
			sb.append(it.next().toString());
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		sb.append("DisjointClasses(");
		Iterator<Description> it = descriptions.iterator();
		while(it.hasNext()){
			sb.append(it.next().toKBSyntaxString());
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	

	@Override
	public void accept(AxiomVisitor visitor) {
		visitor.visit(this);
	}

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the descriptions
	 */
	public Collection<Description> getDescriptions() {
		return descriptions;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		sb.append("DisjointClasses(");
		Iterator<Description> it = descriptions.iterator();
		while(it.hasNext()){
			sb.append(it.next().toManchesterSyntaxString(baseURI, prefixes));
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}	
	
}
