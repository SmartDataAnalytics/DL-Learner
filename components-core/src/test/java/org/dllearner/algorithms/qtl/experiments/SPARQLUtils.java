package org.dllearner.algorithms.qtl.experiments;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.dllearner.utilities.QueryUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Lorenz Buehmann
 */
public class SPARQLUtils {

    private static QueryUtils utils = new QueryUtils();

    enum QueryType {
        IN, OUT, MISC
    }

    public static List<String> getResult(QueryExecutionFactory qef, Query query) throws Exception{
        return getResult(qef, query, query.getProjectVars().get(0));
    }

    public static List<String> getResult(QueryExecutionFactory qef, Query query, Var targetVar) throws Exception{
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            return StreamSupport.stream(((Iterable<QuerySolution>)() -> rs).spliterator(), false)
                    .map(qs -> qs.getResource(targetVar.getName()).getURI())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Failed to get result", e);
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
                    .map(tp -> tp.getObject())
                    .anyMatch(o -> utils.extractIncomingTriplePatterns(query, o).size() > 1);
            tmp = tmp.stream()
                    .filter(tp -> tp.getObject().isVariable())
                    .map(tp -> tp.getObject())
                    .map(o -> utils.extractOutgoingTriplePatterns(query, o))
                    .flatMap(tps -> tps.stream())
                    .collect(Collectors.toSet());
        }
        tmp = utils.extractIncomingTriplePatterns(query, query.getProjectVars().get(0));
        while(!tmp.isEmpty()) {
            incoming = true;
            outgoing |= tmp.stream()
                    .filter(tp -> tp.getSubject().isVariable())
                    .map(tp -> tp.getSubject())
                    .anyMatch(s -> utils.extractOutgoingTriplePatterns(query, s).size() > 1);
            tmp = tmp.stream()
                    .filter(tp -> tp.getSubject().isVariable())
                    .map(tp -> tp.getSubject())
                    .map(s -> utils.extractIncomingTriplePatterns(query, s))
                    .flatMap(tps -> tps.stream())
                    .collect(Collectors.toSet());
        }

        boolean misc = outgoing && incoming;
        return misc ? QueryType.MISC : outgoing ? QueryType.OUT : QueryType.IN;
    }


}
