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

import java.util.Map;
import java.util.Set;

public class ObjectOneOf extends Description{
	
	private static final long serialVersionUID = 5494347630962268139L;
	
	private Set<Individual> individuals;
	
	
	public ObjectOneOf(Set<Individual> individuals){
		this.individuals = individuals;
	}
	
	public Set<Individual> getIndividuals(){
		return individuals;
	}

	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		sb.append("{");
		for(Individual ind : individuals){
			sb.append(ind.toString(baseURI, prefixes));
			if(count < individuals.size()){
				sb.append(",");
				count++;
			}
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int getLength() {
		return 1;
	}

	@Override
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		sb.append("{");
		for(Individual ind : individuals){
			sb.append(ind.toKBSyntaxString(baseURI, prefixes));
			if(count < individuals.size()){
				sb.append(",");
				count++;
			}
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public String toString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		sb.append("{");
		for(Individual ind : individuals){
			sb.append(ind.toString(baseURI, prefixes));
			if(count < individuals.size()){
				sb.append(", ");
				count++;
			}
		}
		sb.append("}");
		
		return sb.toString();
	}

}
