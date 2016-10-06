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

package org.dllearner.algorithms.isle.textretrieval;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.isle.index.Token;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Interface for methods, which retrieve relevant texts given an entity
 * in an ontology. An entity text retriever can do simple operations such
 * as converting the URI into text or retrieving an rdfs:label, but could
 * also search web pages for textual explanations of an entity.
 * 
 * @author Jens Lehmann
 *
 */
public interface EntityTextRetriever {
	
	/**
	 * The method retrieves a string or a set of strings, which is weighted by
	 * importance with respect to the entity. For instance, an rdfs:label of
	 * an entity can be given more weight than an rdfs:comment, which in turn 
	 * can be more important than a OWLClassExpression retrieved from a web page.
	 *  
	 * @param entity The entity to handle.
	 * @return A weighted set of strings. For a value x, we need to have 0 <= x <= 1.
	 */
	Map<List<Token>, Double> getRelevantText(OWLEntity entity);
	
	Map<OWLEntity, Set<List<Token>>> getRelevantText(OWLOntology ontology);

	/**
	 * @param entity
	 * @return
	 */
	Map<String, Double> getRelevantTextSimple(OWLEntity entity);
	
}
