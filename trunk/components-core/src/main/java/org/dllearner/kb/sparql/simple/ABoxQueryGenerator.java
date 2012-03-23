/**
 * 
 */
package org.dllearner.kb.sparql.simple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author didierc
 *
 */
public class ABoxQueryGenerator {
    public String createQuery(List<String> individuals, OntModel model, List<String> filters) {
        StringBuilder builder = new StringBuilder();
        builder.append("CONSTRUCT {?s ?p ?o} ");
        builder.append("{ ?s ?p ?o .");
        List<String> curIndividuals;
        if (model.isEmpty()) {
            curIndividuals = individuals;
        } else {
            curIndividuals = this.difference(individuals, this.getIndividualsFromModel(model));
        }
        builder.append(" FILTER (?s IN( ");
        for (String individual : curIndividuals) {
            builder.append("<");
            builder.append(individual);
            builder.append(">");
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append("))");
        if (filters != null) {
            for (String filter : filters) {
                builder.append("filter");
            }
        }
        builder.append("}");
        return builder.toString();
    }
    
    public String createLastQuery(List<String> individuals, OntModel model, List<String> filters) {
        StringBuilder builder = new StringBuilder();
        builder.append("CONSTRUCT {?s ?p ?o . ?o a ?class} ");
        builder.append("{ ?s ?p ?o .");
        builder.append("?o a ?class");
        List<String> curIndividuals;
        if (model.isEmpty()) {
            curIndividuals = individuals;
        } else {
            curIndividuals = this.difference(individuals, this.getIndividualsFromModel(model));
        }
        builder.append(" FILTER ( ?s IN( ");
        for (String individual : curIndividuals) {
            builder.append("<");
            builder.append(individual);
            builder.append(">");
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append(")). ");
        if (filters != null) {
            for (String filter : filters) {
                builder.append(filter);
                builder.append(". ");
            }
        }
        builder.append("}");
        return builder.toString();
    }
    
    private List<String> getIndividualsFromModel(OntModel model) {
        ExtendedIterator<Individual> iterator = model.listIndividuals();
        LinkedList<String> result = new LinkedList<String>();
        while (iterator.hasNext()) {
            result.add(iterator.next().getURI());
        }
        return result;
    }
    
    public List<String> difference(List<String> a, List<String> b) {
        ArrayList<String> result = new ArrayList<String>(b);
        result.removeAll(a);
        return result;
    }
}
