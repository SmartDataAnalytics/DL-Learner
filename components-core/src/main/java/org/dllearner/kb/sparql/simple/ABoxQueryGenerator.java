/**
 *
 */
package org.dllearner.kb.sparql.simple;

import java.util.*;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author didierc
 */
public class ABoxQueryGenerator {
    public String createQuery(List<String> individuals, OntModel model, List<String> filters) {
        Monitor monABoxQueryGeneration = MonitorFactory.getTimeMonitor("ABox query generator").start();
        StringBuilder builder = new StringBuilder();
        builder.append("CONSTRUCT {?s ?p ?o} ");
        builder.append("{ ?s ?p ?o .");
        Set<String> curIndividuals;
        if (model.isEmpty()) {
            curIndividuals = new HashSet<String>(individuals);
        } else {
            curIndividuals = this.difference2(individuals, model);
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
        monABoxQueryGeneration.stop();
        return builder.toString();
    }

    public String createLastQuery(List<String> individuals, OntModel model, List<String> filters) {
        Monitor monABoxQueryGeneration = MonitorFactory.getTimeMonitor("ABox query generator")
                .start();
        StringBuilder builder = new StringBuilder();
        if (false) {
            builder.append("CONSTRUCT {?s ?p ?o . ?o a ?class} ");
            builder.append("{ ?s ?p ?o .");
            builder.append("?o a ?class");
        } else {
            builder.append("CONSTRUCT {?s ?p ?o } ");
            builder.append("{ ?s ?p ?o . ");
        }

        Set<String> curIndividuals;
        if (model.isEmpty()) {
            curIndividuals = new HashSet<String>(individuals);
        } else {
            curIndividuals = this.difference2(individuals, model);
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
        monABoxQueryGeneration.stop();
        return builder.toString();
    }

    public Set<String> difference2
            (List<String> a, OntModel model) {
        Set<String> inds = new HashSet<String>(a);
        Set<String> result = new HashSet<String>();
        for (ExtendedIterator<Individual> it = model.listIndividuals(); it.hasNext(); ) {
            String individual = it.next().getURI();
            if (!inds.contains(individual)) {
                result.add(individual);
            }
        }
        return result;
    }

    /*private List<String> getIndividualsFromModel
            (OntModel
                     model) {
        ExtendedIterator<Individual> iterator = model.listIndividuals();
        LinkedList<String> result = new LinkedList<String>();
        while (iterator.hasNext()) {
            result.add(iterator.next().getURI());
        }
        return result;
    }

    public List<String> difference
            (List<String> a, List<String> b) {
        ArrayList<String> result = new ArrayList<String>(b);
        result.removeAll(a);
        return result;
    } */
}
