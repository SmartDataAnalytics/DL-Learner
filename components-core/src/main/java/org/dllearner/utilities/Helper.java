/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.owl.FlatABox;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * TODO: JavaDoc
 * 
 * @author Jens Lehmann
 * 
 */
public class Helper {

	private static Logger logger = Logger.getLogger(Helper.class);	
	private static final OWLDataFactory df = new OWLDataFactoryImpl();
	
	// findet alle atomaren Konzepte in einem Konzept
	public static List<OWLClass> getAtomicConcepts(OWLClassExpression concept) {
		return new ArrayList<OWLClass>(concept.getClassesInSignature());
	}

	// findet alle atomaren Rollen in einem Konzept
	public static List<OWLObjectProperty> getAtomicRoles(OWLClassExpression concept) {
		return new ArrayList<OWLObjectProperty>(concept.getObjectPropertiesInSignature());
	}

	// sucht, ob der übergebene String mit einem Prefix beginnt der
	// versteckt werden soll und gibt diesen zurück, ansonsten wird
	// null zurück gegeben
	// public static String findPrefixToHide(String name) {
	// for(String prefix : Config.hidePrefixes) {
	// if(name.startsWith(prefix))
	// return prefix;
	// }
	// return null;
	// }

	/**
	 * 
	 * Transforms an URI to an abbreviated version, e.g. if the base URI is
	 * "http://example.com/" and the uri is "http://example.com/test", then
	 * "test" is returned. If the the uri is "http://anotherexample.com/test2"
	 * and a prefix "ns1" is given for "http://anotherexample.com", then
	 * "ns1:test2" is returned. If there is no match, uri is returned.
	 * 
	 * @param uri
	 *            The full uri, which should be transformed to an abbreviated
	 *            version.
	 * @param baseURI
	 *            The base uri (ignored if null).
	 * @param prefixes
	 *            A prefix map (ignored if null), where each entry contains a
	 *            short string e.g. ns1 as key and the corresponding uri as
	 *            value.
	 * @return Abbreviated version of the parameter uri.
	 */
	public static String getAbbreviatedString(String uri, String baseURI,
			Map<String, String> prefixes) {
		if (baseURI != null && uri.startsWith(baseURI)) {
			return uri.substring(baseURI.length());
		} else {
			if (prefixes != null) {
				for (Entry<String, String> prefix : prefixes.entrySet()) {
					if (uri.startsWith(prefix.getValue()))
						return prefix.getKey() + ":" + uri.substring(prefix.getValue().length());
				}
			}
			return uri;
		}
	}

	/**
	 * Transforms a list of URIs into their abbreviated version.
	 * @see #getAbbreviatedString(String, String, Map)
	 * @param list List of URIs.
	 * @param baseURI The base uri (ignored if null).
	 * @param prefixes A prefix map (ignored if null), where each entry contains a
	 *            short string e.g. ns1 as key and the corresponding uri as
	 *            value.
	 * @return A list with shortened URIs.
	 */
	public static String getAbbreviatedCollection(Collection<String> list, String baseURI,
			Map<String, String> prefixes) {
		StringBuffer str = new StringBuffer("[");
		Iterator<String> it = list.iterator(); // easier to implement using an iterator than foreach
		while(it.hasNext()) {
			str.append(getAbbreviatedString(it.next(),baseURI,prefixes));
			if(it.hasNext()) {
				str.append(", ");
			}
		}
		str.append("]");
		return str.toString();
	}
	
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

	public static <T1, T2> void addMapEntry(Map<T1, SortedSet<T2>> map, T1 keyEntry, T2 setEntry) {
		if (map.containsKey(keyEntry)) {
			map.get(keyEntry).add(setEntry);
		} else {
			SortedSet<T2> newSet = new TreeSet<T2>();
			newSet.add(setEntry);
			map.put(keyEntry, newSet);
		}
	}

	/**
	 * Das ist eine "generic method", d.h. die Methode hat einen bestimmten Typ.
	 * Ich habe das benutzt um allen beteiligten Mengen den gleichen Typ zu
	 * geben, denn ansonsten ist es nicht möglich der neu zu erzeugenden Menge
	 * (union) den gleichen Typ wie den Argumenten zu geben.
	 * 
	 * Die Methode hat gegenüber addAll den Vorteil, dass sie ein neues Objekt
	 * erzeugt.
	 * 
	 * @param <T>
	 * @param set1
	 * @param set2
	 */
	public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
		// TODO: effizientere Implementierung (längere Liste klonen und Elemente
		// anhängen)
		Set<T> union = new TreeSet<T>();
		union.addAll(set1);
		union.addAll(set2);
		return union;
		/*
		 * Set union; if(set1.size()>set2.size()) { union = set1.clone(); } else {
		 *  } return union;
		 */
	}

	public static <T> SortedSet<T> union(SortedSet<T> set1, SortedSet<T> set2) {
		// Set<T> union = set1.clone();
		// ((Cloneable) set1).clone();

		// TODO: effizientere Implementierung (längere Liste klonen und Elemente
		// anhängen)

		// f�r TreeSet gibt es einen Konstruktor, der eine Collection
		// entgegennimmt
		// und einen weiteren, der ein SortedSet entgegennimmt; vermutlich ist
		// letzterer schneller

		SortedSet<T> union;
		if (set1.size() > set2.size()) {
			union = new TreeSet<T>(set1);
			union.addAll(set2);
		} else {
			union = new TreeSet<T>(set2);
			union.addAll(set1);
		}
		// SortedSet<T> union = new TreeSet<T>(set1);
		// union.addAll(set1);
		// union.addAll(set2);
		return union;

	}

	public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
		// TreeSet<T> intersection = (TreeSet<T>) set1.clone();
		// TODO: effizienter implementieren d.h. lange Liste klonen und dann
		// retainAll
		SortedSet<T> intersection = new TreeSet<T>(set1);
		// intersection.addAll(set1);
		intersection.retainAll(set2);
		return intersection;
	}

	public static <T> Set<T> intersectionTuple(Set<T> set, SortedSetTuple<T> tuple) {
		Set<T> ret = intersection(set, tuple.getPosSet());
		ret.retainAll(tuple.getNegSet());
		return ret;
	}

	public static <T> SortedSet<T> difference(SortedSet<T> set1, SortedSet<T> set2) {
		// TODO: effizienter implementieren
		SortedSet<T> difference = new TreeSet<T>(set1);
		// difference.addAll(set1);
		difference.removeAll(set2);
		return difference;
	}

	public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
		// TODO: effizienter implementieren
		SortedSet<T> difference = new TreeSet<T>(set1);
		// difference.addAll(set1);
		difference.removeAll(set2);
		return difference;
	}	
	
	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<OWLIndividual> getIndividualSet(Set<String> individuals) {
		SortedSet<OWLIndividual> ret = new TreeSet<OWLIndividual>();
		for (String s : individuals) {
			ret.add(df.getOWLNamedIndividual(IRI.create(s)));
		}
		return ret;
	}

	public static SortedSetTuple<OWLIndividual> getIndividualTuple(SortedSetTuple<String> tuple) {
		return new SortedSetTuple<OWLIndividual>(getIndividualSet(tuple.getPosSet()),
				getIndividualSet(tuple.getNegSet()));
	}

	public static SortedSetTuple<String> getStringTuple(SortedSetTuple<OWLIndividual> tuple) {
		return new SortedSetTuple<String>(getStringSet(tuple.getPosSet()), getStringSet(tuple
				.getNegSet()));
	}

	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<String> getStringSet(Set<OWLIndividual> individuals) {
		SortedSet<String> ret = new TreeSet<String>();
		for (OWLIndividual i : individuals) {
			ret.add(i.toStringID());
		}
		return ret;
	}

	public static Map<String, SortedSet<String>> getStringMap(
			Map<OWLIndividual, SortedSet<OWLIndividual>> roleMembers) {
		Map<String, SortedSet<String>> ret = new TreeMap<String, SortedSet<String>>();
		for (OWLIndividual i : roleMembers.keySet()) {
			ret.put(i.toStringID(), getStringSet(roleMembers.get(i)));
		}
		return ret;
	}

	/**
	 * TODO: split in two methods (one for concepts, one for roles), document
	 * what exactly the method is doing, remove dependencies from old Config
	 * class, incorporate the new methods in the learning algorithms when
	 * appropriate (common conf options for allowed concepts/roles and forbidden
	 * concepts/roles need to be created)
	 * 
	 * Computes the set of allowed concepts based on configuration settings
	 * (also ignores anonymous and standard RDF, RDFS, OWL concept produces by
	 * Jena).
	 * 
	 * DEPRECATED METHOD (RELIED ON OLD CONFIG).
	 * 
	 */
//	public static void autoDetectConceptsAndRoles(ReasonerComponent rs) {
//		// einige Sachen, die momentan nur vom Refinement-Algorithmus
//		// unterstützt werden (später ev. auch von anderen Algorithmen)
//		// if (Config.algorithm == Algorithm.REFINEMENT) {
//
//		// berechnen der verwendbaren Konzepte
//		if (Config.Refinement.allowedConceptsAutoDetect) {
//			// TODO: Code aus DIG-Reasoner-Klasse einfügen
//
//			Set<AtomicConcept> allowedConceptsTmp = new TreeSet<AtomicConcept>(
//					new ConceptComparator());
//			allowedConceptsTmp.addAll(rs.getAtomicConcepts());
//			Iterator<AtomicConcept> it = allowedConceptsTmp.iterator();
//			while (it.hasNext()) {
//				String conceptName = it.next().toStringID();
//				// System.out.println(conceptName);
//				// seltsame anon-Konzepte, die von Jena erzeugt werden
//				// löschen
//				if (conceptName.startsWith("anon")) {
//					System.out
//							.println("  Ignoring concept "
//									+ conceptName
//									+ " (probably an anonymous concept produced by Jena when reading in OWL file).");
//					it.remove();
//				} else if (conceptName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
//					System.out.println("  Ignoring concept " + conceptName
//							+ " (RDF construct produced by Jena when reading in OWL file).");
//					it.remove();
//				} else if (conceptName.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
//					System.out.println("  Ignoring concept " + conceptName
//							+ " (RDF Schema construct produced by Jena when reading in OWL file).");
//					it.remove();
//				} else if (conceptName.startsWith("http://www.w3.org/2002/07/owl#")) {
//					System.out.println("  Ignoring concept " + conceptName
//							+ " (OWL construct produced by Jena when reading in OWL file).");
//					it.remove();
//				}
//			}
//
//			// hier werden jetzt noch die zu ignorierenden Konzepte entfernt
//			if (Config.Refinement.ignoredConcepts != null) {
//
//				for (AtomicConcept ac : Config.Refinement.ignoredConcepts) {
//					boolean success = allowedConceptsTmp.remove(ac);
//					if (!success) {
//						System.out.println("Ignored concept " + ac
//								+ " does not exist in knowledge base.");
//						System.exit(0);
//					}
//
//				}
//			}
//
//			Config.Refinement.allowedConcepts = allowedConceptsTmp;
//		} else {
//			// prüfen, ob nur verfügbare Konzepte vom Nutzer gewählt worden
//			Set<AtomicConcept> allowed = new HashSet<AtomicConcept>();
//			allowed.addAll(Config.Refinement.allowedConcepts);
//			allowed.removeAll(rs.getAtomicConcepts());
//			if (allowed.size() > 0) {
//				System.out
//						.println("Some of the concepts you told the learner to use in the definition, "
//								+ "do not exist in the background knowledge: " + allowed);
//				System.out.println("Please correct this problem and restart.");
//				System.exit(0);
//			}
//		}
//
//		if (Config.Refinement.allowedRolesAutoDetect) {
//			Set<AtomicRole> allowedRolesTmp = rs.getAtomicRoles();
//
//			// hier werden jetzt noch die zu ignorierenden Rollen entfernt
//			if (Config.Refinement.ignoredRoles != null) {
//
//				for (AtomicRole ar : Config.Refinement.ignoredRoles) {
//					boolean success = allowedRolesTmp.remove(ar);
//					if (!success) {
//						System.out.println("Ignored role " + ar
//								+ " does not exist in knowledge base.");
//						System.exit(0);
//					}
//
//				}
//			}
//
//			Config.Refinement.allowedRoles = allowedRolesTmp;
//
//		} else {
//			Set<AtomicRole> allowedR = new HashSet<AtomicRole>();
//			allowedR.addAll(Config.Refinement.allowedRoles);
//
//			Set<AtomicRole> existingR = new TreeSet<AtomicRole>(new RoleComparator());
//			existingR.addAll(rs.getAtomicRoles());
//
//			// allowedR.removeAll(rs.getAtomicRoles());
//			allowedR.removeAll(existingR);
//
//			if (allowedR.size() > 0) {
//				System.out
//						.println("Some of the roles you told the learner to use in the definition, "
//								+ "do not exist in the background knowledge: " + allowedR);
//				System.out.println("Please correct this problem and restart.");
//				System.out.println(rs.getAtomicRoles());
//				System.out.println(Config.Refinement.allowedRoles);
//				System.exit(0);
//			}
//
//		}
//	}

	/**
	 * Removes concepts, which should be ignored by the learning algorithm.
	 * (The main reason to use this method is because Jena introduces such
	 * concepts when ontologies are converted to DIG.) Currently ignored
	 * concepts are those having prefix "anon" and concepts belonging to
	 * the RDF, RDFS, OWL standards.
	 * 
	 * @param concepts The set from which concepts will be removed.
	 * @deprecated Deprecated method, because it is not needed anymore. 
	 */
	@Deprecated
	public static void removeUninterestingConcepts(Set<OWLClass> concepts) {
		Iterator<OWLClass> it = concepts.iterator();
		while (it.hasNext()) {
			String conceptName = it.next().toStringID();
			
			// ignore some concepts (possibly produced by Jena)
			if (conceptName.startsWith("anon")) {
				logger.debug("  Ignoring concept "
								+ conceptName
								+ " (probably an anonymous concept produced by Jena when reading in OWL file).");
				it.remove();
			} else if (conceptName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
				logger.debug("  Ignoring concept " + conceptName
						+ " (RDF construct produced by Jena when reading in OWL file).");
				it.remove();
			} else if (conceptName.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
				logger.debug("  Ignoring concept " + conceptName
						+ " (RDF Schema construct produced by Jena when reading in OWL file).");
				it.remove();
			} else if (conceptName.startsWith("http://www.w3.org/2002/07/owl#")) {
				logger.debug("  Ignoring concept " + conceptName
						+ " (OWL construct produced by Jena when reading in OWL file).");
				it.remove();
			}
			
		}
	}
	
	// concepts case 1: no ignore or allowed list
	public static Set<OWLClass> computeConcepts(AbstractReasonerComponent rs) {
		// if there is no ignore or allowed list, we just ignore the concepts
		// of uninteresting namespaces
		Set<OWLClass> concepts = rs.getClasses();
//		Helper.removeUninterestingConcepts(concepts);
		return concepts;
	}
	
	// concepts case 2: ignore list
	public static Set<OWLClass> computeConceptsUsingIgnoreList(AbstractReasonerComponent rs, Set<OWLClass> ignoredConcepts) {
		Set<OWLClass> concepts = new TreeSet<OWLClass>(rs.getClasses());
//		Helper.removeUninterestingConcepts(concepts);
		for (OWLClass ac : ignoredConcepts) {
			boolean success = concepts.remove(ac);
			if (!success)
				logger.warn("Warning: Ignored concept " + ac + " does not exist in knowledge base.");
		}
		return concepts;
	}
	
	// concepts case 3: allowed list
	// superseeded by checkConcepts()
//	public static void checkAllowedList(ReasonerComponent rs, Set<AtomicConcept> allowedConcepts) {
//		// check whether allowed concepts exist in knowledgebase(s)
//		Set<AtomicConcept> allowed = new HashSet<AtomicConcept>();
//		allowed.addAll(allowedConcepts);
//		allowed.removeAll(rs.getAtomicConcepts());
//		if (allowed.size() > 0) {
//			System.out
//					.println("Some of the concepts you told the learner to use in the definition, "
//							+ "do not exist in the background knowledge: " + allowed);
//			System.out.println("Please correct this problem and restart.");
//			System.exit(0);
//		}		
//	}
	
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

	// creates a flat ABox by querying a reasoner
	public static FlatABox createFlatABox(AbstractReasonerComponent rs)
			throws ReasoningMethodUnsupportedException {
		long dematStartTime = System.currentTimeMillis();

		FlatABox aBox = new FlatABox(); // FlatABox.getInstance();
		if(!rs.getClasses().isEmpty()) {
			for (OWLClass atomicConcept : rs.getClasses()) {
				aBox.atomicConceptsPos.put(atomicConcept.toStringID(), getStringSet(rs
						.getIndividuals(atomicConcept)));
				OWLObjectComplementOf negatedAtomicConcept = df.getOWLObjectComplementOf(atomicConcept);
				aBox.atomicConceptsNeg.put(atomicConcept.toStringID(), getStringSet(rs
						.getIndividuals(negatedAtomicConcept)));
				aBox.concepts.add(atomicConcept.toStringID());
			}			
		}

		if(!rs.getObjectProperties().isEmpty()) {
			for (OWLObjectProperty atomicRole : rs.getObjectProperties()) {
				aBox.rolesPos.put(atomicRole.toStringID(), getStringMap(rs.getPropertyMembers(atomicRole)));
				aBox.roles.add(atomicRole.toStringID());
			}			
		}

		aBox.domain = getStringSet(rs.getIndividuals());
		aBox.top = aBox.domain;

		// System.out.println(aBox);

		long dematDuration = System.currentTimeMillis() - dematStartTime;
		System.out.println("OK (" + dematDuration + " ms)");
		return aBox;
	}
	
	public static String arrayContent(int[] ar) {
		String str = ""; 
		for(int i=0; i<ar.length; i++) {
			str += ar[i] + ",";
		}
		return str;
	}	
}
