package org.dllearner.utilities;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;

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
	public static SortedSet<OWLIndividual> getIndividualSet(Set<String> individuals) {
		SortedSet<OWLIndividual> ret = new TreeSet<>();
		for (String s : individuals) {
			ret.add(df.getOWLNamedIndividual(IRI.create(s)));
		}
		return ret;
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
	public static SortedSet<String> getStringSet(Set<OWLIndividual> individuals) {
		SortedSet<String> ret = new TreeSet<>();
		for (OWLIndividual i : individuals) {
			ret.add(i.toStringID());
		}
		return ret;
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

}
