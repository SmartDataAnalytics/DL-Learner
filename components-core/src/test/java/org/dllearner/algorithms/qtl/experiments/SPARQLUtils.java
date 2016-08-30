package org.dllearner.algorithms.qtl.experiments;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Lorenz Buehmann
 */
public class SPARQLUtils {

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
}
