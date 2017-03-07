package org.dllearner.utilities;

import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.*;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A collection of services that are known as non-standard reasoning.
 *
 * @author Lorenz Buehmann
 */
public class NonStandardReasoningServices {

	/**
	 * Computes the least common subsumer (LCS) of the given entities <code>e1</code> and <code>e2</code> if exists,
	 * otherwise <code>null</code>
	 *
	 * @param reasoner the reasoner used to compute the direct parents
	 * @param n1 first entity
	 * @param n2 second entity
	 * @param entityType the entity type
	 * @param <E> the entity type
	 * @return the LCS if exists
	 */
	public static <E extends OWLEntity> Node getLeastCommonSubsumer(AbstractReasonerComponent reasoner, Node n1, Node n2,
																 	EntityType<E> entityType) {
		// trivial case
		if(n1.equals(n2)) {
			return n1;
		}

		E lcs = getLeastCommonSubsumer(reasoner,
									   OwlApiJenaUtils.asOWLEntity(n1, entityType),
									   OwlApiJenaUtils.asOWLEntity(n2, entityType));

		if(lcs != null) {
			return OwlApiJenaUtils.asNode(lcs);
		}

		return null;
	}

	/**
	 * Computes the least common subsumer (LCS) of the given entities <code>e1</code> and <code>e2</code> if exists,
	 * otherwise <code>null</code>
	 *
	 * @param reasoner the reasoner used to compute the direct parents
	 * @param e1 first entity
	 * @param e2 second entity
	 * @param <E> the entity type
	 * @return the LCS if exists
	 */
	@SuppressWarnings("unchecked")
	public static <E extends OWLEntity> E getLeastCommonSubsumer(AbstractReasonerComponent reasoner, E e1, E e2) {
		// check if both entities are of the same type
		if(e1.getEntityType() != e2.getEntityType()) {
			throw new IllegalArgumentException("LCS operation only defined for entities of the same type!");
		}

		// trivial case
		if(e1.equals(e2)) {
			return e1;
		}

		// depending on the entity type define the method to get the direct parents
		EntityType<?> entityType = e1.getEntityType();
		Function<E, SortedSet<E>> f;
		if(entityType == EntityType.CLASS) {
			f = e -> (SortedSet<E>) reasoner.getSuperClasses((OWLClass) e).stream()
						.filter(ce -> !ce.isAnonymous())
						.map(OWLClassExpression::asOWLClass)
						.collect(Collectors.toCollection(TreeSet::new));
		} else if(entityType == EntityType.OBJECT_PROPERTY) {
			f = e -> (SortedSet<E>) reasoner.getSuperProperties((OWLObjectProperty) e);
		} else if(entityType == EntityType.DATA_PROPERTY) {
			f = e -> (SortedSet<E>) reasoner.getSuperProperties((OWLDataProperty) e);
		} else {
			throw new RuntimeException("LCS for " + entityType.getPluralPrintName() + " not implemented!");
		}

		TwoParamFunction<E, E, Boolean> isSubEntity;
		if(entityType == EntityType.CLASS) {
			isSubEntity = (entity1, entity2) -> reasoner.isSuperClassOf((OWLClass) entity2, (OWLClass) entity1);
		} else if(entityType == EntityType.OBJECT_PROPERTY) {
			isSubEntity = (entity1, entity2) -> reasoner.isSubPropertyOf((OWLObjectProperty) entity1, (OWLObjectProperty) entity2);
		} else if(entityType == EntityType.DATA_PROPERTY) {
			isSubEntity = (entity1, entity2) -> reasoner.isSubPropertyOf((OWLDataProperty) entity1, (OWLDataProperty) entity2);
		} else {
			throw new RuntimeException("LCS for " + entityType.getPluralPrintName() + " not implemented!");
		}

		// compute the LCS
		return getLeastCommonSubsumer(e1, e2, f, isSubEntity);
	}

	private static <E extends OWLEntity> E getLeastCommonSubsumer(E e1, E e2, Function<E, SortedSet<E>> f, TwoParamFunction<E, E, Boolean> f2) {
		// e1 = e2 -> e1
		if(e1.equals(e2)) {
			return e1;
		}

		if(f2.apply(e1, e2)) {
			return e2;
		}

		if(f2.apply(e2, e1)) {
			return e1;
		}

		// e2 contained in direct parents(e1) -> e2
		SortedSet<E> superEntities1 = f.apply(e1);
		if(superEntities1.contains(e2)) {
			return e2;
		}

		// e1 contained in direct parents(e2) -> e1
		SortedSet<E> superEntities2 = f.apply(e2);
		if(superEntities2.contains(e1)) {
			return e1;
		}

		// parents(e1) ∩ parents(e2) != empty -> an element of the intersection
		Sets.SetView<E> intersection = Sets.intersection(superEntities1, superEntities2);
		if(!intersection.isEmpty()) {
			return intersection.iterator().next();
		}

		// recursive call using the parents
		for (E sup1 : superEntities1) {
			for (E sup2 : superEntities2) {
				E lcs = getLeastCommonSubsumer(sup1, sup2, f, f2);

				if(lcs != null) {
					return lcs;
				}
			}
		}

		// no LCS found
		return null;
	}

	private static <E extends OWLEntity> E getLeastCommonSubsumer2(E e1, E e2, Function<E, SortedSet<E>> f) {
		// e1 = e2 -> e1
		if(e1.equals(e2)) {
			return e1;
		}

		// e2 contained in direct parents(e1) -> e2
		SortedSet<E> superEntities1 = f.apply(e1);
		if(superEntities1.contains(e2)) {
			return e2;
		}

		// e1 contained in direct parents(e2) -> e1
		SortedSet<E> superEntities2 = f.apply(e2);
		if(superEntities2.contains(e1)) {
			return e1;
		}

		// parents(e1) ∩ parents(e2) != empty -> an element of the intersection
		Sets.SetView<E> intersection = Sets.intersection(superEntities1, superEntities2);
		if(!intersection.isEmpty()) {
			return intersection.iterator().next();
		}

		superEntities1.add(e1);
		superEntities2.add(e2);

		// recursive call using the parents
		for (E sup1 : superEntities1) {System.out.println("P1:" + sup1);
			for (E sup2 : superEntities2) {System.out.println("P2:" + sup2);
				if(!(e1.equals(sup1) && e2.equals(sup2))) {
					E lcs = getLeastCommonSubsumer2(sup1, sup2, f);

					if(lcs != null) {
						return lcs;
					}
				}
			}
		}

		// no LCS found
		return null;
	}

	@FunctionalInterface
	interface TwoParamFunction<One, Two, Value> {
		public Value apply(One one, Two two);
	}

	public static void main(String[] args) throws Exception {
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://sake.aksw.uni-leipzig.de:8890/sparql", "http://dbpedia.org");

		QueryExecutionFactory qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs()).config()
				.withCache(CacheUtilsH2.createCacheFrontend("/tmp/cache", false, TimeUnit.DAYS.toMillis(60)))
				.withPagination(10000).withDelay(50, TimeUnit.MILLISECONDS).end().create();

		SPARQLReasoner reasoner = new SPARQLReasoner(qef);
		reasoner.setPrecomputeClassHierarchy(true);
		reasoner.setPrecomputeObjectPropertyHierarchy(true);
		reasoner.setPrecomputeDataPropertyHierarchy(true);
		reasoner.init();
		reasoner.precomputePropertyDomains();
		reasoner.precomputeObjectPropertyRanges();

		Node lcs = NonStandardReasoningServices.getLeastCommonSubsumer(
				reasoner,
				NodeFactory.createURI("http://dbpedia.org/ontology/Town"),
				NodeFactory.createURI("http://dbpedia.org/ontology/Place"),
				EntityType.CLASS);

		System.out.println(lcs);
	}
}
