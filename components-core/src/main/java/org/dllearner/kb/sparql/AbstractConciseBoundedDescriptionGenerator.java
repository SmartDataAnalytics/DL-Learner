package org.dllearner.kb.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 */
public abstract class AbstractConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator {

    protected static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

//    protected final QueryExecutionFactory qef;
    protected QueryExecutionFactory qef;

    protected Set<String> allowedPropertyNamespaces = new TreeSet<>();
    protected Set<String> allowedObjectNamespaces = new TreeSet<>();
    protected Set<String> allowedClassNamespaces = new TreeSet<>();
    protected Set<String> ignoredProperties = new TreeSet<>();

    protected AbstractConciseBoundedDescriptionGenerator(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    public void setAllowedPropertyNamespaces(Set<String> allowedPropertyNamespaces) {
        this.allowedPropertyNamespaces = allowedPropertyNamespaces;
    }

    public void setAllowedObjectNamespaces(Set<String> allowedObjectNamespaces) {
        this.allowedObjectNamespaces = allowedObjectNamespaces;
    }

    public void setAllowedClassNamespaces(Set<String> allowedClassNamespaces) {
        this.allowedClassNamespaces = allowedClassNamespaces;
    }

    public void setIgnoredProperties(Set<String> ignoredProperties) {
        this.ignoredProperties = ignoredProperties;
    }

    protected abstract String generateQuery(String resource, int depth, boolean withTypesForLeafs);

    @Override
    public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
        log.trace("Computing CBD for {} ...", resource);
        long start = System.currentTimeMillis();
        String query = generateQuery(resource, depth, withTypesForLeafs);
        log.debug(query);
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            Model model = qe.execConstruct();
            log.trace("Got {} triples in {} ms.", model.size(), (System.currentTimeMillis() - start));
            return model;
        } catch (Exception e) {
            log.error("Failed to computed CBD for resource {}", resource );
            throw new RuntimeException("Failed to computed CBD for resource " + resource, e);
        }
    }


    private boolean USE_FILTER_IN = true;
    private static final String FILTER_NOT_IN_CLAUSE = "%s NOT IN (%s)";
    private static final String FILTER_IN_CLAUSE = "%s IN (%s)";

    protected String createPredicateFilter(final Var predicateVar) {
        String filter = "";

        if(!ignoredProperties.isEmpty()) {
            filter += "FILTER(";
            if(USE_FILTER_IN) {
                filter += String.format(
                        FILTER_NOT_IN_CLAUSE,
                        predicateVar.toString(),
                        ignoredProperties.stream()
                                .map(p -> "<" + p + ">")
                                .collect(Collectors.joining(",")));
            } else {
                filter += ignoredProperties.stream()
                        .map(input -> predicateVar.toString() + " != <" + input + ">")
                        .collect(Collectors.joining(" && "));
            }
            filter += ")\n";
        }

        if(!allowedPropertyNamespaces.isEmpty()){
            filter += "FILTER(" + predicateVar + " = <" + RDF.type.getURI() + "> || ";
            filter += allowedPropertyNamespaces.stream()
                    .map(ns -> "(STRSTARTS(STR(" + predicateVar + "),'" + ns + "'))")
                    .collect(Collectors.joining(" || "));
            filter += ")\n";
        }

        return filter;
    }

    protected String createObjectFilter(Var predicateVar, Var objectVar){
        String filter = "";
        if(!allowedObjectNamespaces.isEmpty() || !allowedClassNamespaces.isEmpty()) {
            filter += "FILTER(ISLITERAL(" + objectVar + ")";
        }

        if(!allowedObjectNamespaces.isEmpty()){
            filter += " || (" + predicateVar + " != " + FmtUtils.stringForResource(RDF.type) + " && ";
            filter += allowedObjectNamespaces.stream()
                    .map(ns -> "(STRSTARTS(STR(" + objectVar + "),'" + ns + "'))")
                    .collect(Collectors.joining(" || "));
            filter += ")\n";
        } else if(!allowedClassNamespaces.isEmpty()){
            filter += " || " + predicateVar + " != " + FmtUtils.stringForResource(RDF.type) + " || ";
        }

        if(!allowedClassNamespaces.isEmpty()){
//			if(allowedObjectNamespaces.isEmpty()) {
//				filter += predicateVar + " != " + FmtUtils.stringForResource(RDF.type) + " || ";
//			}
            filter += "(" + predicateVar + " = " + FmtUtils.stringForResource(RDF.type) + " && ";
            filter += allowedClassNamespaces.stream()
                    .map(ns -> "(STRSTARTS(STR(" + objectVar + "),'" + ns + "'))")
                    .collect(Collectors.joining(" || "));
            filter += ")\n";
        } else if(!allowedObjectNamespaces.isEmpty()){
            filter += " || " + predicateVar + " = " + FmtUtils.stringForResource(RDF.type);
        }

        if(!allowedObjectNamespaces.isEmpty() || !allowedClassNamespaces.isEmpty()) {
            filter += ")";
        }
        return filter;
    }
}
