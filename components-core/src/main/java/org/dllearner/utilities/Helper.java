/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities;

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: JavaDoc
 * 
 * @author Jens Lehmann
 * 
 */
public class Helper {

	private static Logger logger = LoggerFactory.getLogger(Helper.class);
	private static final OWLDataFactory df = new OWLDataFactoryImpl();
	
	public static String prettyPrintNanoSeconds(long nanoSeconds) {
		return prettyPrintNanoSeconds(nanoSeconds, false, false);
	}

	// formatiert Nano-Sekunden in einen leserlichen String
	public static String prettyPrintNanoSeconds(long nanoSeconds, boolean printMicros,
			boolean printNanos) {
		// String str = "";
		// long seconds = 0;
		// long milliSeconds = 0;
		// long microseconds = 0;

		long seconds = nanoSeconds / 1000000000;
		nanoSeconds = nanoSeconds % 1000000000;

		long milliSeconds = nanoSeconds / 1000000;
		nanoSeconds = nanoSeconds % 1000000;

		// Mikrosekunden werden immer angezeigt, Sekunden nur falls größer 0
		String str = "";
		if (seconds > 0)
			str = seconds + "s ";
		str += milliSeconds + "ms";

		if (printMicros) {
			long microSeconds = nanoSeconds / 1000;
			nanoSeconds = nanoSeconds % 1000;
			str += " " + microSeconds + "usec";
		}
		if (printNanos) {
			str += " " + nanoSeconds + "ns";
		}

		return str;
	}
	
	public static String prettyPrintMilliSeconds(long milliSeconds) {
	

		long seconds = milliSeconds / 1000;
		milliSeconds = milliSeconds % 1000;

		// Mikrosekunden werden immer angezeigt, Sekunden nur falls größer 0
		String str = "";
		if (seconds > 0)
			str = seconds + "s ";
		str += milliSeconds + "ms";
		
		return str;
	}

	public static <T> Set<T> intersectionTuple(Set<T> set, SortedSetTuple<T> tuple) {
		Set<T> ret = Sets.intersection(set, tuple.getPosSet());
		ret.retainAll(tuple.getNegSet());
		return ret;
	}
	
	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<OWLIndividual> getIndividualSet(Collection<String> individuals) {
		return individuals.stream()
				.map(s -> df.getOWLNamedIndividual(IRI.create(s)))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public static SortedSetTuple<OWLIndividual> getIndividualTuple(SortedSetTuple<String> tuple) {
		return new SortedSetTuple<>(getIndividualSet(tuple.getPosSet()),
				getIndividualSet(tuple.getNegSet()));
	}

	public static SortedSetTuple<String> getStringTuple(SortedSetTuple<OWLIndividual> tuple) {
		return new SortedSetTuple<>(getStringSet(tuple.getPosSet()), getStringSet(tuple
				.getNegSet()));
	}

	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<String> getStringSet(Collection<OWLIndividual> individuals) {
		return individuals.stream().
				map(OWLIndividual::toStringID).
				collect(Collectors.toCollection(TreeSet::new));
	}

	public static Map<String, SortedSet<String>> getStringMap(
			Map<OWLIndividual, SortedSet<OWLIndividual>> roleMembers) {
		Map<String, SortedSet<String>> ret = new TreeMap<>();
		for (OWLIndividual i : roleMembers.keySet()) {
			ret.put(i.toStringID(), getStringSet(roleMembers.get(i)));
		}
		return ret;
	}

	// concepts case 1: no ignore or allowed list
	@SuppressWarnings("unchecked")
	public static <T extends OWLEntity> Set<T> computeEntities(AbstractReasonerComponent rs, EntityType<T> entityType) {
		// if there is no ignore or allowed list, we just ignore the concepts
		// of uninteresting namespaces
		if (entityType == EntityType.CLASS) {
			return (Set<T>) rs.getClasses();
		} else if (entityType == EntityType.OBJECT_PROPERTY) {
			return (Set<T>) rs.getObjectProperties();
		} else if (entityType == EntityType.DATA_PROPERTY) {
			return (Set<T>) rs.getDatatypeProperties();
		}
		return null;
	}
	
	// concepts case 1: no ignore or allowed list
	public static Set<OWLClass> computeConcepts(AbstractReasonerComponent rs) {
		// if there is no ignore or allowed list, we just ignore the concepts
		// of uninteresting namespaces
		Set<OWLClass> concepts = rs.getClasses();
//		Helper.removeUninterestingConcepts(concepts);
		return concepts;
	}

	@SuppressWarnings("unchecked")
	public static <T extends OWLEntity> Set<T> computeEntitiesUsingIgnoreList(AbstractReasonerComponent rs, EntityType<T> entityType, Set<T> ignoredEntites) {
		Set<T> entities;
		
		if (entityType == EntityType.CLASS) {
			entities = (Set<T>) rs.getClasses();
		} else if (entityType == EntityType.OBJECT_PROPERTY) {
			entities = (Set<T>) rs.getObjectProperties();
		} else if (entityType == EntityType.DATA_PROPERTY) {
			entities = (Set<T>) rs.getDatatypeProperties();
		} else {
			throw new UnsupportedOperationException("Entity type " + entityType + " currently not supported.");
		}
		
		entities = new TreeSet<>(entities);
		
		for (T entity : ignoredEntites) {
			boolean success = entities.remove(entity);
			if (!success) {
				logger.warn("Warning: Ignored entity " + entity + " does not exist in knowledge base.");
			}
		}
		return entities;
	}
	
	// concepts case 2: ignore list
	public static Set<OWLClass> computeConceptsUsingIgnoreList(AbstractReasonerComponent rs, Set<OWLClass> ignoredConcepts) {
		Set<OWLClass> concepts = new TreeSet<>(rs.getClasses());
//		Helper.removeUninterestingConcepts(concepts);
		for (OWLClass ac : ignoredConcepts) {
			boolean success = concepts.remove(ac);
			if (!success)
				logger.warn("Warning: Ignored concept " + ac + " does not exist in knowledge base.");
		}
		return concepts;
	}
	
	/**
	 * Checks whether the roles exist in background knowledge
	 * @param roles The roles to check.
	 * @return The first non-existing role or null if they are all in the
	 * background knowledge.
	 */
	//
	public static OWLObjectProperty checkRoles(AbstractReasonerComponent rs, Set<OWLObjectProperty> roles) {
		Set<OWLObjectProperty> existingRoles = rs.getObjectProperties();
		for (OWLObjectProperty ar : roles) {
			if(!existingRoles.contains(ar))
				return ar;
		}
		return null;
	}
	
	/**
	 * Checks whether the entities exist in background knowledge
	 * @param entities The entities to check.
	 * @return The first non-existing entity or null if they are all in the
	 * background knowledge.
	 */
	public static <T extends OWLEntity> T checkEntities(AbstractReasonerComponent rs, Set<T> entities) {
		Set<T> existingEntities = (Set<T>) computeEntities(rs, entities.iterator().next().getEntityType());
		for (T entity : entities) {
			if(!existingEntities.contains(entity))
				return entity;
		}
		return null;
	}
	
	/**
	 * Checks whether the roles exist in background knowledge
	 * @param concepts The concepts to check.
	 * @return The first non-existing role or null if they are all in the
	 * background knowledge.
	 */
	//
	public static OWLClass checkConcepts(AbstractReasonerComponent rs, Set<OWLClass> concepts) {
		Set<OWLClass> existingConcepts = rs.getClasses();
		for (OWLClass ar : concepts) {
			if(!existingConcepts.contains(ar))
				return ar;
		}
		return null;
	}

	/**
	 * Checks whether all entities in the given class expression do also occur in the knowledge base.
	 *
	 * @param ce The concept to check.
	 * @return {@code true} if all entities of the class expression occur in the knowledge base,
	 * otherwise {@code false}
	 */
	public static boolean checkConceptEntities(AbstractReasonerComponent rc, OWLClassExpression ce) {
		return rc.getClasses().containsAll(ce.getClassesInSignature()) &&
				rc.getObjectProperties().containsAll(ce.getObjectPropertiesInSignature()) &&
				rc.getDatatypeProperties().containsAll(ce.getDataPropertiesInSignature());

	}

	public static void checkIndividuals(AbstractReasonerComponent reasoner, Set<OWLIndividual> individuals) throws ComponentInitException {
		if (!(reasoner instanceof SPARQLReasoner)) {
			SortedSet<OWLIndividual> allIndividuals = reasoner.getIndividuals();

			if (!allIndividuals.containsAll(individuals)) {
				Set<OWLIndividual> missing = Sets.difference(individuals, allIndividuals);
				double percentage = (double) missing.size() / individuals.size();
				percentage = Math.round(percentage * 1000.0) / 1000.0;
				String str = "The examples (" + (percentage * 100) + " % of total) below are not contained in the knowledge base " +
						"(check spelling and prefixes)\n";
				str += missing.toString();
				if (missing.size() == individuals.size()) {
					throw new ComponentInitException(str);
				}
				if (percentage < 0.10) {
					logger.warn(str);
				} else {
					logger.error(str);
				}
			}
		}
	}

	public static void displayProgressPercentage(int done, int total) {
		int size = 5;
		String iconLeftBoundary = "[";
		String iconDone = "=";
		String iconRemain = ".";
		String iconRightBoundary = "]";

		if (done > total) {
			throw new IllegalArgumentException();
		}
		int donePercents = (100 * done) / total;
		int doneLength = size * donePercents / 100;

		StringBuilder bar = new StringBuilder(iconLeftBoundary);
		for (int i = 0; i < size; i++) {
			if (i < doneLength) {
				bar.append(iconDone);
			} else {
				bar.append(iconRemain);
			}
		}
		bar.append(iconRightBoundary);

		System.out.print("\r" + bar + " " + donePercents + "%");

		if (done == total) {
			System.out.print("\n");
		}
	}

}
