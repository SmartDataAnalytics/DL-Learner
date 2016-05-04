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
/**
 *
 */
package org.dllearner.kb.sparql.simple;

import java.util.Set;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author didierc
 */
@SuppressWarnings("ALL")
public class ABoxQueryGenerator {

    public String createQuery(Set<String> individuals, String aboxfilter) {
        Monitor monABoxQueryGeneration = MonitorFactory.getTimeMonitor("ABox query generator").start();
        StringBuilder builder = new StringBuilder();
        builder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        builder.append("CONSTRUCT {?s ?p ?o  } ");
        builder.append("{ ?s ?p ?o . " );
        builder.append(makeInFilter("?s", individuals));
        if (aboxfilter != null) {
            builder.append(aboxfilter);
        }
        builder.append("FILTER ( (?p!=rdf:type))");
        builder.append("}");
        monABoxQueryGeneration.stop();
        return builder.toString();
    }

    public static StringBuilder makeInFilter(String var, Set<String> uris) {
        StringBuilder builder = new StringBuilder();
        builder.append(" FILTER (").append(var).append(" IN( ");
        for (String uri : uris) {
            if (!uri.startsWith("<")) builder.append("<");
            builder.append(uri);
            if (!uri.endsWith(">")) builder.append(">");
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append(")) . ");
        return builder;
    }

    /**
     *
     *
     * @param oldIndividuals
     * @param model
     * @return
     */

    /*public String createLastQuery(List<String> individuals, OntModel model, List<String> filters) {
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
}    */

    /*private List<String> getIndividualsFromModel
           (OntModel
                    model) {
       ExtendedIterator<OWLIndividual> iterator = model.listIndividuals();
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
