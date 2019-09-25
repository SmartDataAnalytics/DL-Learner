/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 * <p>
 * This file is part of DL-Learner.
 * <p>
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.sparql;

import java.io.StringReader;
import java.util.Set;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CBD generator that resolves blank nodes.
 *
 * @author Lorenz Buehmann
 */
public class BlanknodeResolvingCBDGenerator implements ConciseBoundedDescriptionGenerator {

    private static final Logger log = LoggerFactory.getLogger(BlanknodeResolvingCBDGenerator.class);

    private QueryExecutionFactoryModel qef;

    public BlanknodeResolvingCBDGenerator(Model model) {
        String query = "prefix : <http://dl-learner.org/ontology/> "
                + "construct { ?s ?p ?o ; ?type ?s .} "
                + "where {  ?s ?p ?o .  bind( if(isIRI(?s),:sameIri,:sameBlank) as ?type )}";
        qef = new QueryExecutionFactoryModel(model);
        QueryExecution qe = qef.createQueryExecution(query);
        Model extendedModel = qe.execConstruct();
        qe.close();

        qef = new QueryExecutionFactoryModel(extendedModel);
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String)
     */
    @Override
    public Model getConciseBoundedDescription(String resourceURI) {
        return getConciseBoundedDescription(resourceURI, 0);
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int)
     */
    @Override
    public Model getConciseBoundedDescription(String resource, int depth) {
        return getConciseBoundedDescription(resource, depth, false);
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
     */
    @Override
    public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
        StringBuilder constructTemplate = new StringBuilder("?s0 ?p0 ?o0 .\n");
        for (int i = 1; i < depth; i++) {
            constructTemplate.append("?s").append(i).append(" ?p").append(i).append(" ?o").append(i).append(" .\n");
        }

        StringBuilder triplesTemplate = new StringBuilder("?x ((!<x>|!<y>)/:sameBlank)* ?s0 . ?s0 ?p0 ?o0 . filter(!(?p0 in (:sameIri, :sameBlank)))\n");
        for (int i = 1; i < depth; i++) {
            triplesTemplate.append("OPTIONAL {").append("?o").append(i - 1).append(" ((!<x>|!<y>)/:sameBlank)* ?s").append(i).append(" .")
                    .append("?s").append(i).append(" ?p").append(i).append(" ?o").append(i).append(" filter(!(?p").append(i).append(" in (:sameIri, :sameBlank)))");
        }


        for (int i = 1; i < depth; i++) {
            triplesTemplate.append("}");
        }

        ParameterizedSparqlString query = new ParameterizedSparqlString("prefix : <http://dl-learner.org/ontology/> " +
                "\nCONSTRUCT {\n" + constructTemplate + "} WHERE {\n" + triplesTemplate + "\n}");
        query.setIri("x", resource);

        log.debug("CBD query:\n" + query.toString());

        try (QueryExecution qe = qef.createQueryExecution(query.toString())) {
            return qe.execConstruct();
        }
    }

    @Override
    public void setIgnoredProperties(Set<String> properties) {
        throw new UnsupportedOperationException("not supported yet!");
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#setRestrictToNamespaces(java.util.List)
     */
    @Override
    public void setAllowedPropertyNamespaces(Set<String> namespaces) {
        throw new UnsupportedOperationException("not supported yet!");
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#setAllowedObjectNamespaces(java.util.Set)
     */
    @Override
    public void setAllowedObjectNamespaces(Set<String> namespaces) {
        throw new UnsupportedOperationException("not supported yet!");
    }

    public static void main(String[] args) {
        String data = "@prefix : <http://dl-learner.org/test/> .\n" +
                "\n" +
                ":A :p :B ;\n" +
                "   :q [ :r :C , :D ] .\n" +
                ":B :q :D, :E .\n" +
                ":C :r :F .\n" +
                ":D :r :F , :G ;\n" +
                "   :s :A , :B .";

        printCBD(data, 1);

        data = "@prefix : <http://dl-learner.org/test/> .\n" +
                "\n" +
                ":A :p :B . :B :q [ :r :C , :D ] . \n" +
                ":B :q :D, :E .\n" +
                ":C :r :F .\n" +
                ":D :r :F , :G ;\n" +
                "   :s :A , :B .";

        printCBD(data, 2);
    }

    private static void printCBD(String data, int depth) {
        Model m = ModelFactory.createDefaultModel();
        m.read(new StringReader(data), null, "Turtle");

        BlanknodeResolvingCBDGenerator cbdGen = new BlanknodeResolvingCBDGenerator(m);

        Model cbd = cbdGen.getConciseBoundedDescription("http://dl-learner.org/test/A", 2);
        cbd.setNsPrefix("", "http://dl-learner.org/test/");
        cbd.write(System.out, "Turtle");
    }
}
