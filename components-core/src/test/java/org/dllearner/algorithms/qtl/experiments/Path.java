/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.qtl.experiments;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

import org.apache.jena.sparql.core.Var;

public class Path {
	
	OWLClass cls;
	List<Set<String>> properties;
	String object;

	public Path(OWLClass cls, List<Set<String>> properties, String object) {
		this.cls = cls;
		this.properties = properties;
		this.object = object;
	}

	public OWLClass getOWLClass() {
		return cls;
	}

	public List<Set<String>> getProperties() {
		return properties;
	}

	public String getObject() {
		return object;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return cls + ": " + properties + "-->" + object;
	}

	public String asSPARQLQuery(Var targetVar) {
		String query = "SELECT DISTINCT " + targetVar + " WHERE {";
		query += targetVar + " a <" + cls.toStringID() + "> .";
		Set<String> lastPropertyCluster = properties.get(properties.size() - 1);
		Var joinVar = targetVar;
		for (int i = 0; i < properties.size() - 1; i++) {
			Set<String> cluster = properties.get(i);
			for (String property : cluster) {
				Var joinVarTmp = Var.alloc("o" + i);
				query += joinVar + " <" + property + "> " + joinVarTmp + " .";
				joinVar = joinVarTmp;
			}
		}
		for (String lastProperty : lastPropertyCluster) {
			query += joinVar + " <" + lastProperty + "> <" + object + "> .";
		}

		query += "}";
		return query;
	}

	public String asSPARQLPathQuery(Var targetVar) {
		String query = "SELECT DISTINCT " + targetVar + " WHERE {";
		query += targetVar + " a <" + cls.toStringID() + "> ;";
		
		Set<String> firstPropertyCluster = properties.get(0);
		for (String firstProperty : firstPropertyCluster) {
			query += " <" + firstProperty + "> ";
		}
		
		for (int i = 1; i < properties.size(); i++) {
			for (String p : properties.get(i)) {
				query += "/" + "<" + p + ">";
			}
		}
		query += " <" + object + "> .";
		query += "}";
		return query;
	}
}