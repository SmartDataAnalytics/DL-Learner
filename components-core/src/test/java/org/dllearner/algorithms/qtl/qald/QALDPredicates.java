package org.dllearner.algorithms.qtl.qald;

import org.apache.jena.query.QueryFactory;
import org.dllearner.algorithms.qtl.qald.schema.Question;
import org.dllearner.utilities.QueryUtils;

import java.util.function.Predicate;

public class QALDPredicates {

    private static QueryUtils utils = new QueryUtils();

    public static Predicate<Question> isUnion() {
        return q -> q.getQuery().getSparql().toLowerCase().contains(" union");
    }

    public static Predicate<Question> hasFilter() {
        return q -> q.getQuery().getSparql().toLowerCase().contains(" filter");
    }

    public static Predicate<Question> isSubjectTarget() {
        return q -> utils.extractIncomingTriplePatterns(
                        QueryFactory.create(q.getQuery().getSparql()),
                        QueryFactory.create(q.getQuery().getSparql()).getProjectVars().get(0).asNode()
                    ).isEmpty();

    }

    public static Predicate<Question> isObjectTarget() {
        return q -> utils.extractOutgoingTriplePatterns(
                QueryFactory.create(q.getQuery().getSparql()),
                QueryFactory.create(q.getQuery().getSparql()).getProjectVars().get(0).asNode()
        ).isEmpty();

    }

}