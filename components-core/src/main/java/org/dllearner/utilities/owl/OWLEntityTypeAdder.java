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
package org.dllearner.utilities.owl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class OWLEntityTypeAdder {

	/**
	 * Infers the type of predicates p_i by analyzing the object of the triples using p_i and adds the
	 * entity type assertion to the model, i.e. for a data property dp <dp a owl:DatatypeProperty>
	 * will be added.
	 * @param model the model
	 */
	public static void addEntityTypes(Model model){
		StmtIterator iterator = model.listStatements();
		Set<Property> objectPropertyPredicates = new HashSet<>();
		Set<Property> dataPropertyPredicates = new HashSet<>();
		while(iterator.hasNext()){
			Statement st = iterator.next();
			Property predicate = st.getPredicate();
			if(!predicate.getURI().startsWith(RDF.getURI()) && !predicate.getURI().startsWith(RDFS.getURI()) 
					&& !predicate.getURI().startsWith(OWL.getURI())){
				RDFNode object = st.getObject();
				if(object.isLiteral()){
					dataPropertyPredicates.add(predicate);
				} else if(object.isResource()){
					objectPropertyPredicates.add(predicate);
				}
			}
		}
		iterator.close();
		for (Property property : dataPropertyPredicates) {
			if(!objectPropertyPredicates.contains(property)){
				model.add(property, RDF.type, OWL.DatatypeProperty);
			}
		}
		for (Property property : objectPropertyPredicates) {
			if(!dataPropertyPredicates.contains(property)){
				model.add(property, RDF.type, OWL.ObjectProperty);
			}
		}
	}

}
