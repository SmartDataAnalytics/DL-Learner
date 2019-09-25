package org.dllearner.algorithms.qtl.experiments;

import com.google.common.base.Joiner;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.Var;
import org.dllearner.utilities.QueryUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Lorenz Buehmann
 */
public class SPARQLUtils {

    public enum QueryType {
        IN, OUT, MISC
    }

    private static QueryUtils utils = new QueryUtils();

    public static ParameterizedSparqlString CBD_TEMPLATE_DEPTH3;

    static {
        try {
            String query = Joiner.on("\n").join(Files.readAllLines(Paths.get(
					SPARQLUtils.class.getClassLoader().getResource("org/dllearner/algorithms/qtl/cbd-depth3.query").toURI())));
            CBD_TEMPLATE_DEPTH3 = new ParameterizedSparqlString(query);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public static List<String> getResult(QueryExecutionFactory qef, Query query) throws Exception{
        return getResult(qef, query, query.getProjectVars().get(0));
    }

    public static List<String> getResult(QueryExecutionFactory qef, Query query, Var targetVar) throws Exception{
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            return StreamSupport.stream(((Iterable<QuerySolution>)() -> rs).spliterator(), false)
                    .map(qs -> qs.get(targetVar.getName()).toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Failed to get result for query\n" + query, e);
        }
    }

    public static QueryType getQueryType(Query query) {
        boolean outgoing = false;
        boolean incoming = false;
        Set<Triple> tmp = utils.extractOutgoingTriplePatterns(query, query.getProjectVars().get(0));
        while(!tmp.isEmpty()) {
            outgoing = true;
            incoming |= tmp.stream()
                    .filter(tp -> tp.getObject().isVariable())
                    .map(Triple::getObject)
                    .anyMatch(o -> utils.extractIncomingTriplePatterns(query, o).size() > 1);
            tmp = tmp.stream()
                    .filter(tp -> tp.getObject().isVariable())
                    .map(Triple::getObject)
                    .map(o -> utils.extractOutgoingTriplePatterns(query, o))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }
        tmp = utils.extractIncomingTriplePatterns(query, query.getProjectVars().get(0));
        while(!tmp.isEmpty()) {
            incoming = true;
            outgoing |= tmp.stream()
                    .filter(tp -> tp.getSubject().isVariable())
                    .map(Triple::getSubject)
                    .anyMatch(s -> utils.extractOutgoingTriplePatterns(query, s).size() > 1);
            tmp = tmp.stream()
                    .filter(tp -> tp.getSubject().isVariable())
                    .map(Triple::getSubject)
                    .map(s -> utils.extractIncomingTriplePatterns(query, s))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }

        boolean misc = outgoing && incoming;
        return misc ? QueryType.MISC : outgoing ? QueryType.OUT : QueryType.IN;
    }


}
