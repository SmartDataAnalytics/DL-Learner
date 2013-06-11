package org.dllearner.utilities.owl;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OWLEntityTypeAdder {

	/**
	 * Infers the type of predicates p_i by analyzing the object of the triples using p_i and adds the
	 * entity type assertion to the model, i.e. for a data property dp <dp a owl:DatatypeProperty>
	 * will be added.
	 * @param model
	 */
	public static void addEntityTypes(Model model){
		StmtIterator iterator = model.listStatements();
		Set<Property> objectPropertyPredicates = new HashSet<Property>();
		Set<Property> dataPropertyPredicates = new HashSet<Property>();
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
