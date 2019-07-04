package org.dllearner.algorithms.qtl.qald;

import org.apache.jena.query.QueryFactory;
import org.dllearner.algorithms.qtl.qald.schema.Question;
import org.dllearner.utilities.QueryUtils;

import java.util.function.Predicate;

/**
 * A set of predicates used to filter the QALD benchmark dataset of SPARQL queries.
 */
public class QALDPredicates {

	private static QueryUtils utils = new QueryUtils();

	/**
	 * @return whether a question contains a UNION.
	 */
	public static Predicate<Question> isUnion() {
		return q -> q.getQuery().getSparql().toLowerCase().contains(" union");
	}

	/**
	 * @return whether a question contains a FILTER.
	 */
	public static Predicate<Question> hasFilter() {
		return q -> q.getQuery().getSparql().toLowerCase().contains(" filter");
	}

	/**
	 * @return whether the target projection variable is used only in subject position of a triple pattern
	 */
	public static Predicate<Question> isSubjectTarget() {
		return q -> utils.extractIncomingTriplePatterns(
				QueryFactory.create(q.getQuery().getSparql()),
				QueryFactory.create(q.getQuery().getSparql()).getProjectVars().get(0).asNode()
		).isEmpty();

	}

	/**
	 * @return whether the target projection variable is used only in object position of a triple pattern
	 */
	public static Predicate<Question> isObjectTarget() {
		return q -> utils.extractOutgoingTriplePatterns(
				QueryFactory.create(q.getQuery().getSparql()),
				QueryFactory.create(q.getQuery().getSparql()).getProjectVars().get(0).asNode()
		).isEmpty();

	}

	/**
	 * @return whether the question has no answer
	 */
	public static Predicate<Question> hasNoAnswer() {
		return q -> q.getQuery().getSparql() == null;

	}

	/**
	 * @return whether only schema entities from the DBpedia ontology namespace (http://dbpedia.org/ontology/)
	 * are used
	 */
	public static Predicate<Question> isOnlyDBO() {
		return Question::isOnlyDBO;
	}

}