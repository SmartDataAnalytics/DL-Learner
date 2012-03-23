/**
 * 
 */
package org.dllearner.kb.sparql.simple;

import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * @author didierc
 * 
 */
public class TBoxQueryGenerator {
    public String createQuery(OntModel model, List<String> filters, List<String> individuals) {
        StringBuilder builder = new StringBuilder("CONSTRUCT { ?example a ?class . } ");
        builder.append("{ ?example a ?class . ");
        builder.append("Filter ( ?example IN(");
        for (String individual : individuals) {
            builder.append("<");
            builder.append(individual);
            builder.append(">");
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append(")).");
        if (filters != null) {
            for (String filter : filters) {
                builder.append(filter);
            }
        }
        builder.append("}");
        return builder.toString();
    }
}
