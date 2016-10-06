package org.dllearner.utilities;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.*;

import java.util.SortedSet;
import java.util.TreeSet;
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

		// compute the LCS
		return getLeastCommonSubsumer(e1, e2, f);
	}

	private static <E extends OWLEntity> E getLeastCommonSubsumer(E e1, E e2, Function<E, SortedSet<E>> f) {
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

		// recursive call using the parents
		for (E sup1 : superEntities1) {
			for (E sup2 : superEntities2) {
				E lcs = getLeastCommonSubsumer(sup1, sup2, f);

				if(lcs != null) {
					return lcs;
				}
			}
		}

		// no LCS found
		return null;
	}
}
