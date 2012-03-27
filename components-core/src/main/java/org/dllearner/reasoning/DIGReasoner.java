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

package org.dllearner.reasoning;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.RoleComparator;
import org.kr.dl.dig.v1_1.Concepts;
import org.kr.dl.dig.v1_1.Csynonyms;
import org.kr.dl.dig.v1_1.IdType;
import org.kr.dl.dig.v1_1.IndividualSetDocument.IndividualSet;
import org.kr.dl.dig.v1_1.Named;
import org.kr.dl.dig.v1_1.ResponseDocument;
import org.kr.dl.dig.v1_1.ResponsesDocument;
import org.kr.dl.dig.v1_1.Roles;
import org.kr.dl.dig.v1_1.Rsynonyms;

/**
 * DIG 1.1 implementation of the reasoner interface.
 * 
 * @author Jens Lehmann
 * 
 */
public class DIGReasoner extends AbstractReasonerComponent {

	URL reasonerURL;

	// Variablen für Reasoner
	DIGHTTPConnector connector;
	String identifier;
	URI kbURI;
	private String asksPrefix;
	// Cache für Konzepte, Rollen und Individuen
	Set<NamedClass> atomicConcepts;
	Set<ObjectProperty> atomicRoles;
	SortedSet<Individual> individuals;

	// Cache für Subsumptionhierarchie
	// Comparator ist notwendig, da sonst z.B. verschiedene Instanzen des
	// atomaren Konzepts male
	// unterschiedlich sind;
	// alternativ wäre auch eine Indizierung über Strings möglich
	ConceptComparator conceptComparator = new ConceptComparator();
	RoleComparator roleComparator = new RoleComparator();
	ClassHierarchy subsumptionHierarchy;
	ObjectPropertyHierarchy roleHierarchy;
	// enthält atomare Konzepte, sowie Top und Bottom
	Set<Description> allowedConceptsInSubsumptionHierarchy;

	private boolean writeDIGProtocol;
	private File digProtocolFile;
	private static String defaultDIGProtocolFile = "log/digProtocol.txt";
	
	
	
	public DIGReasoner(Set<KnowledgeSource> sources) {
		super(sources);
		try {
			reasonerURL = new URL("http://localhost:8081");
		} catch (MalformedURLException e) {
		}
	}

	@Override
	public void init() throws ComponentInitException {
		// if a DIG protocol is written clear the file first
		if(writeDIGProtocol) {
			if(digProtocolFile == null)
				digProtocolFile = new File(defaultDIGProtocolFile);
			Files.clearFile(digProtocolFile);
			connector = new DIGHTTPConnector(reasonerURL, digProtocolFile);
		} else {
			connector = new DIGHTTPConnector(reasonerURL);
		}
		
		try {
			identifier = connector.getIdentifier();
		} catch (IOException e1) {
			throw new ComponentInitException("Communication problem with DIG Reasoner. Please make sure there is a DIG reasoner running at " + reasonerURL + " and try again.", e1);
		}
		kbURI = connector.newKB();

		// asks-Prefix entsprechend der KB-URI initialisieren
		asksPrefix = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
		asksPrefix += "<asks xmlns=\"http://dl.kr.org/dig/2003/02/lang\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://dl.kr.org/dig/2003/02/lang\n"
				+ "http://dl-web.man.ac.uk/dig/2003/02/dig.xsd\" uri=\"" + kbURI + "\">";

		// momementan wird davon ausgegangen, dass toDIG(kbURI) den gesamten
		// tells-Request liefert
		StringBuilder sb = new StringBuilder();
		// sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		// sb.append("<tells xmlns=\"http://dl.kr.org/dig/2003/02/lang\" " +
		// "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
		// "xsi:schemaLocation=\"http://dl.kr.org/dig/2003/02/lang\n" +
		// "http://dl-web.man.ac.uk/dig/2003/02/dig.xsd\" uri=\""+kbURI+"\">");
		for (KnowledgeSource source : sources) {
			sb.append(((AbstractKnowledgeSource)source).toDIG(kbURI));

			ResponseDocument rd = null;
			try {
				rd = connector.tells(sb.toString());
			} catch (IOException e) {
				throw new ComponentInitException("Could not read knowledge source " + source + ".", e);
			}
			if (!rd.getResponse().isSetOk()) {
				System.err.println("DIG-Reasoner cannot read knowledgebase.");
				System.exit(0);
			}
		}
		// sb.append("</tells>");

		// DIG-Abfragen nach Konzepten, Rollen, Individuals
		atomicConcepts = getAtomicConceptsDIG();
		atomicRoles = getAtomicRolesDIG();
		individuals = getIndividualsDIG();
		
	}

	public static String getName() {
		return "DIG reasoner";
	}
	

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("reasonerUrl", "URL of the DIG reasoner"));
		options.add(new BooleanConfigOption("writeDIGProtocol",
				"specifies whether or not to write a protocoll of send and received DIG requests",
				false));
		options.add(new StringConfigOption("digProtocolFile", "the file to store the DIG protocol",
				defaultDIGProtocolFile));
		return options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if (name.equals("reasonerUrl")) {
			String s = (String) entry.getValue();
			try {
				reasonerURL = new URL(s);
			} catch (MalformedURLException e) {
				// e.printStackTrace();
				throw new InvalidConfigOptionValueException(entry.getOption(), entry.getValue());
			}
		} else if(name.equals("writeDIGProtocol")) {
			writeDIGProtocol = (Boolean) entry.getValue();
		} else if(name.equals("digProtocolFile")) {
			digProtocolFile = new File((String) entry.getValue());
		}
	}

	/**
	 * Construct a subsumption hierarchy using DIG queries. After calling this
	 * method one can ask for children or parents in the subsumption hierarchy.
	 */
//	public void prepareSubsumptionHierarchy(Set<NamedClass> allowedConcepts) {
//		allowedConceptsInSubsumptionHierarchy = new TreeSet<Description>(conceptComparator);
//		allowedConceptsInSubsumptionHierarchy.addAll(allowedConcepts);
//		allowedConceptsInSubsumptionHierarchy.add(new Thing());
//		allowedConceptsInSubsumptionHierarchy.add(new Nothing());
//
//		TreeMap<Description, TreeSet<Description>> subsumptionHierarchyUp = new TreeMap<Description, TreeSet<Description>>(
//				conceptComparator);
//		TreeMap<Description, TreeSet<Description>> subsumptionHierarchyDown = new TreeMap<Description, TreeSet<Description>>(
//				conceptComparator);
//
//		// Subsumptionhierarchy berechnen
//		// TODO: kann man effizienter auch in einer Abfrage machen
//
//		// Refinements von Top
//		TreeSet<Description> tmp = getMoreSpecialConceptsDIG(new Thing());
//		tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
//		subsumptionHierarchyDown.put(new Thing(), tmp);
//
//		// Refinements von Bottom
//		tmp = getMoreGeneralConceptsDIG(new Nothing());
//		tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
//		subsumptionHierarchyUp.put(new Nothing(), tmp);
//
//		// Refinement atomarer Konzepte
//		for (NamedClass atom : atomicConcepts) {
//			tmp = getMoreSpecialConceptsDIG(atom);
//			tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
//			subsumptionHierarchyDown.put(atom, tmp);
//
//			tmp = getMoreGeneralConceptsDIG(atom);
//			tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
//			subsumptionHierarchyUp.put(atom, tmp);
//		}
//
//		subsumptionHierarchy = new ClassHierarchy(
//				subsumptionHierarchyUp, subsumptionHierarchyDown);
//	}

	/**
	 * Constructs a role hierarchy using DIG queries. After calling this method,
	 * one can query parents or children of roles.
	 * 
	 * TODO Does not yet take ignored roles into account.
	 */
//	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
//		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyUp = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
//				roleComparator);
//		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyDown = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
//				roleComparator);
// 
//		// Refinement atomarer Konzepte
//		for (ObjectProperty role : atomicRoles) {
//			roleHierarchyDown.put(role, getMoreSpecialRolesDIG(role));
//			roleHierarchyUp.put(role, getMoreGeneralRolesDIG(role));
//		}
//
//		roleHierarchy = new ObjectPropertyHierarchy(allowedRoles, roleHierarchyUp,
//				roleHierarchyDown);
//	}

	// eigentlich müsste man klonen um sicherzustellen, dass der parent-Link
	// bei null bleibt; bei der aktuellen Implementierung ist der parent-Link
	// nicht immer null, was bei GP negative Auswirkungen haben könnte
	// Update: wird durch klonen innerhalb der GP-Operationen erledigt
	public Set<NamedClass> getNamedClasses() {
		/*
		 * if(Config.algorithm == Config.Algorithm.GP || Config.algorithm ==
		 * Config.Algorithm.HYBRID_GP) { Set<AtomicConcept> returnSet = new
		 * HashSet<AtomicConcept>(); for(AtomicConcept ac : atomicConcepts)
		 * returnSet.add((AtomicConcept)ac.clone()); return returnSet; }
		 */
		return atomicConcepts;
	}

	private Set<NamedClass> getAtomicConceptsDIG() {
		String atomicConceptsDIG = asksPrefix;
		atomicConceptsDIG += "<allConceptNames id=\"ask_names\"/></asks>";

		ResponsesDocument rd = connector.asks(atomicConceptsDIG);
		// Struktur: einzelnes conceptSet außen, dann mehrere synonyms, die dann
		// die Konzept inkl. Top und Bottom enthalten
		Csynonyms[] synonymsArray = rd.getResponses().getConceptSetArray();
		Concepts[] conceptsArray = synonymsArray[0].getSynonymsArray();

		Set<NamedClass> atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
		for (Concepts concepts : conceptsArray) {
			boolean topOrBottomFound = false;
			if (concepts.getBottomArray().length != 0 || concepts.getTopArray().length != 0)
				topOrBottomFound = true;

			// nur weitersuchen falls das Konzept nicht äquivalent zu Top
			// oder Bottom ist
			if (!topOrBottomFound) {
				boolean nonAnonymousConceptFound = false;
				NamedClass foundConcept = null;
				Named[] catoms = concepts.getCatomArray();
				for (Named catom : catoms) {
					String name = catom.getName();
					if (!name.startsWith("anon")) {
						if (!nonAnonymousConceptFound) {
							nonAnonymousConceptFound = true;
							foundConcept = new NamedClass(catom.getName());
							atomicConcepts.add(foundConcept);
						} else {
							System.out
									.println("Warning: Background knowledge contains synonym concepts. "
											+ "We decide to pick "
											+ foundConcept
											+ ". \nDIG-XML:\n" + concepts);
						}
					}
				}
			}
		}

		return atomicConcepts;
	}

	public Set<ObjectProperty> getObjectProperties() {
		return atomicRoles;
	}

	private Set<ObjectProperty> getAtomicRolesDIG() {
		String atomicRolesDIG = asksPrefix;
		atomicRolesDIG += "<allRoleNames id=\"ask_roles\"/></asks>";

		ResponsesDocument rd = connector.asks(atomicRolesDIG);
		// Struktur: einzelnes roleSet außen, dann synonyms mit ratoms
		// innen
		Rsynonyms[] synonymsArray = rd.getResponses().getRoleSetArray();
		Roles[] rolesArray = synonymsArray[0].getSynonymsArray();

		Set<ObjectProperty> digAtomicRoles = new HashSet<ObjectProperty>();
		for (Roles roles : rolesArray) {
			// hier koennen wiederum mehrere ratoms enthalten sein,
			// aber wir wollen nur eins auslesen
			Named[] ratoms = roles.getRatomArray();
			Named role = ratoms[0];
			digAtomicRoles.add(new ObjectProperty(role.getName()));

			if (ratoms.length > 1)
				System.out.println("Warning: Background knowledge contains synonym roles. "
						+ "Will ignore all but the first. \nDIG-XML:\n" + roles);
		}

		return digAtomicRoles;
	}

	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	private SortedSet<Individual> getIndividualsDIG() {
		String individualsDIG = asksPrefix;
		individualsDIG += "<allIndividuals id=\"ask_individuals\"/></asks>";

		ResponsesDocument rd = connector.asks(individualsDIG);
		// Struktur: einzelnes individualSet außen, dann Liste von
		// individual-Elementen
		IndividualSet[] individualsArray = rd.getResponses().getIndividualSetArray();
		Named[] namedIndividuals = individualsArray[0].getIndividualArray();

		SortedSet<Individual> digIndividuals = new TreeSet<Individual>();
		for (Named named : namedIndividuals)
			digIndividuals.add(new Individual(named.getName()));

		return digIndividuals;
	}

	@Override
	public ReasonerType getReasonerType() {
		return ReasonerType.DIG;
	}

	@Override
	public boolean isSuperClassOfImpl(Description superConcept, Description subConcept) {
		// System.out.println("subsumes(" + superConcept + "," + subConcept +
		// ")");
		String subsumesDIG = asksPrefix;
		subsumesDIG += "<subsumes id=\"query_subsume\">";
		subsumesDIG += DIGConverter.getDIGString(superConcept);
		subsumesDIG += DIGConverter.getDIGString(subConcept);
		subsumesDIG += "</subsumes></asks>";

		return parseBooleanAnswer(subsumesDIG);
	}

//	@Override
//	public Set<Description> subsumesImpl(Description superConcept, Set<Description> subConcepts) {
//		String subsumesDIG = asksPrefix;
//		int id = 0;
//		// ID-Konzept-Zuordnung speichern, da bei der Antwort nur die IDs
//		// ausgegeben werden
//		Map<String, Description> queryMap = new HashMap<String, Description>();
//		for (Description subConcept : subConcepts) {
//			queryMap.put("query" + id, subConcept);
//			subsumesDIG += "<subsumes id=\"query" + id + "\">";
//			subsumesDIG += DIGConverter.getDIGString(superConcept);
//			subsumesDIG += DIGConverter.getDIGString(subConcept);
//			subsumesDIG += "</subsumes>";
//			id++;
//		}
//		subsumesDIG += "</asks>";
//
//		ResponsesDocument rd = connector.asks(subsumesDIG);
//		IdType[] subsumedConceptsIds = rd.getResponses().getTrueArray();
//
//		Set<Description> returnSet = new HashSet<Description>();
//		for (IdType idType : subsumedConceptsIds) {
//			returnSet.add(queryMap.get(idType.getId()));
//		}
//		return returnSet;
//	}

	@Override
	public Set<Description> isSuperClassOfImpl(Set<Description> superConcepts, Description subConcept) {
		String subsumesDIG = asksPrefix;
		int id = 0;
		Map<String, Description> queryMap = new HashMap<String, Description>();
		for (Description superConcept : superConcepts) {
			queryMap.put("query" + id, superConcept);
			subsumesDIG += "<subsumes id=\"query" + id + "\">";
			subsumesDIG += DIGConverter.getDIGString(superConcept);
			subsumesDIG += DIGConverter.getDIGString(subConcept);
			subsumesDIG += "</subsumes>";
			id++;
		}
		subsumesDIG += "</asks>";

		ResponsesDocument rd = connector.asks(subsumesDIG);
		IdType[] subsumedConceptsIds = rd.getResponses().getTrueArray();

		Set<Description> returnSet = new HashSet<Description>();
		for (IdType idType : subsumedConceptsIds) {
			returnSet.add(queryMap.get(idType.getId()));
		}
		return returnSet;
	}

	/*
	 * // es wird geklont, damit Subsumptionhierarchie nicht von außen verändert
	 * werden // kann @SuppressWarnings("unchecked") @Override public SortedSet<Concept>
	 * getMoreGeneralConcepts(Concept concept) { return (TreeSet<Concept>)
	 * subsumptionHierarchyUp.get(concept).clone(); // return
	 * subsumptionHierarchyUp.get(concept); // ohne klonen geht es nicht }
	 * 
	 * @SuppressWarnings("unchecked") @Override public SortedSet<Concept>
	 * getMoreSpecialConcepts(Concept concept) { return (TreeSet<Concept>)
	 * subsumptionHierarchyDown.get(concept).clone(); // return
	 * subsumptionHierarchyDown.get(concept); // ohne klonen geht es nicht }
	 */

//	@Override
//	public ObjectPropertyHierarchy getRoleHierarchy() {
//		return roleHierarchy;
//	}

	@Override
	protected TreeSet<Description> getSuperClassesImpl(Description concept) {
		String moreGeneralDIG = asksPrefix;
		moreGeneralDIG += "<parents id=\"query_parents\">";
		moreGeneralDIG += DIGConverter.getDIGString(concept);
		moreGeneralDIG += "</parents></asks>";

		ResponsesDocument rd = connector.asks(moreGeneralDIG);
		TreeSet<Description> resultsSet = new TreeSet<Description>(conceptComparator);
		// ein Array, der Synomyms-Elemente enthält, die dann Mengen von
		// äquivalenten Konzepten enthalten;
		// (es wird hier nur das erste Element des ConceptSetArrays gelesen,
		// da nur ein solches Element bei dieser Abfrage erwartet wird)
		Concepts[] conceptsArray = rd.getResponses().getConceptSetArray()[0].getSynonymsArray();

		for (int i = 0; i < conceptsArray.length; i++) {
			// es werden nur atomare Konzepte erwartet
			Named[] atoms = conceptsArray[i].getCatomArray();

			for (Named atom : atoms) {
				NamedClass ac = new NamedClass(atom.getName());
				if (allowedConceptsInSubsumptionHierarchy.contains(ac))
					resultsSet.add(ac);
			}

			// hinzufügen von Top, falls notwendig
			if (conceptsArray[i].getTopArray().length > 0)
				resultsSet.add(new Thing());

			// falls bisher kein erlaubtes Konzept gefunden wurden, dann gibt es
			// entweder keine allgemeineren Konzepte oder es handelt sich um
			// nicht erlaubte (von Jena erzeugte) Konzepte, die nicht äquivalent
			// zu einem erlaubten Konzept sind; in dem Fall muss die Methode
			// rekursiv
			// noch einmal aufgerufen werden
			if (resultsSet.size() == 0 && atoms.length > 0) {
				// wir wählen das erste Konzept aus, welches ein ignoriertes
				// Konzept ist
				// (sonst wäre es weiter oben gefunden wurden)
				NamedClass ignoredAtomicConcept = new NamedClass(atoms[0].getName());
				resultsSet.addAll(getSuperClassesImpl(ignoredAtomicConcept));
			}

		}

		return resultsSet;
	}

	@Override
	protected TreeSet<Description> getSubClassesImpl(Description concept) {
		String moreSpecialDIG = asksPrefix;
		moreSpecialDIG += "<children id=\"query_children\">";
		moreSpecialDIG += DIGConverter.getDIGString(concept);
		moreSpecialDIG += "</children></asks>";

		// Kommentare siehe getMoreGeneralConcepts(Concept)
		ResponsesDocument rd = connector.asks(moreSpecialDIG);
		TreeSet<Description> resultsSet = new TreeSet<Description>(conceptComparator);
		Concepts[] conceptsArray = rd.getResponses().getConceptSetArray()[0].getSynonymsArray();

		for (int i = 0; i < conceptsArray.length; i++) {
			Named[] atoms = conceptsArray[i].getCatomArray();
			for (Named atom : atoms) {
				NamedClass ac = new NamedClass(atom.getName());
				if (allowedConceptsInSubsumptionHierarchy.contains(ac))
					resultsSet.add(ac);
			}

			// hinzufügen von Bottom, falls notwendig
			if (conceptsArray[i].getBottomArray().length > 0)
				resultsSet.add(new Nothing());

			if (resultsSet.size() == 0 && atoms.length > 0) {
				NamedClass ignoredAtomicConcept = new NamedClass(atoms[0].getName());
				resultsSet.addAll(getSubClassesImpl(ignoredAtomicConcept));
			}
		}

		return resultsSet;
	}

	@Override
	protected TreeSet<ObjectProperty> getSuperPropertiesImpl(ObjectProperty role) {
		String moreGeneralRolesDIG = asksPrefix;
		moreGeneralRolesDIG += "<rparents id=\"query_parents\">";
		moreGeneralRolesDIG += "<ratom name=\"" + role.getName() + "\" />";
		moreGeneralRolesDIG += "</rparents></asks>";

		ResponsesDocument rd = connector.asks(moreGeneralRolesDIG);
		TreeSet<ObjectProperty> resultsSet = new TreeSet<ObjectProperty>(roleComparator);
		Roles[] rolesArray = rd.getResponses().getRoleSetArray()[0].getSynonymsArray();

		for (int i = 0; i < rolesArray.length; i++) {
			Named[] atoms = rolesArray[i].getRatomArray();

			for (Named atom : atoms) {
				ObjectProperty ar = new ObjectProperty(atom.getName());
				// if(Config.Refinement.allowedRoles.contains(ar))
				resultsSet.add(ar);
			}
		}

		// System.out.println(rd);

		return resultsSet;
	}

	@Override
	protected TreeSet<ObjectProperty> getSubPropertiesImpl(ObjectProperty role) {
		String moreSpecialRolesDIG = asksPrefix;
		moreSpecialRolesDIG += "<rchildren id=\"query_children\">";
		moreSpecialRolesDIG += "<ratom name=\"" + role.getName() + "\" />";
		moreSpecialRolesDIG += "</rchildren></asks>";

		ResponsesDocument rd = connector.asks(moreSpecialRolesDIG);
		TreeSet<ObjectProperty> resultsSet = new TreeSet<ObjectProperty>(roleComparator);
		Roles[] rolesArray = rd.getResponses().getRoleSetArray()[0].getSynonymsArray();

		for (int i = 0; i < rolesArray.length; i++) {
			Named[] atoms = rolesArray[i].getRatomArray();

			for (Named atom : atoms) {
				ObjectProperty ar = new ObjectProperty(atom.getName());
				// if(Config.Refinement.allowedRoles.contains(ar))
				resultsSet.add(ar);
			}
		}

		return resultsSet;
	}

	@Override
	public boolean hasTypeImpl(Description concept, Individual individual) {
		String instanceCheckDIG = asksPrefix;
		instanceCheckDIG += "<instance id= \"query_instance\">";
		instanceCheckDIG += "<individual name=\"" + individual.getName() + "\"/>";
		instanceCheckDIG += DIGConverter.getDIGString(concept);
		instanceCheckDIG += "</instance></asks>";

		return parseBooleanAnswer(instanceCheckDIG);
	}

	@Override
	public SortedSet<Individual> hasTypeImpl(Description concept, Set<Individual> individuals) {
		String instanceCheckDIG = asksPrefix;
		int id = 0;
		// ID-Konzept-Zuordnung speichern, da bei der Antwort nur die IDs
		// ausgegeben werden
		Map<String, String> queryMap = new HashMap<String, String>();
		for (Individual individual : individuals) {
			queryMap.put("query" + id, individual.getName());
			instanceCheckDIG += "<instance id= \"query" + id + "\">";
			instanceCheckDIG += "<individual name=\"" + individual.getName() + "\"/>";
			instanceCheckDIG += DIGConverter.getDIGString(concept);
			instanceCheckDIG += "</instance>";
			id++;
		}
		instanceCheckDIG += "</asks>";

		ResponsesDocument rd = connector.asks(instanceCheckDIG);
		IdType[] ids = rd.getResponses().getTrueArray();

		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		for (IdType idType : ids) {
			returnSet.add(new Individual(queryMap.get(idType.getId())));
		}
		return returnSet;
	}

	@Override
	public SortedSet<Individual> getIndividualsImpl(Description concept) {

		String retrievalDIG = asksPrefix;
		retrievalDIG += "<instances id= \"query_instance\">";
		retrievalDIG += DIGConverter.getDIGString(concept);
		retrievalDIG += "</instances></asks>";

		ResponsesDocument rd = connector.asks(retrievalDIG);
		// System.out.println(rd);
		Named[] individuals = rd.getResponses().getIndividualSetArray()[0].getIndividualArray();

		SortedSet<Individual> results = new TreeSet<Individual>();
		for (Named individual : individuals)
			results.add(new Individual(individual.getName()));
		return results;
	}

	// ToDo: gibt momentan nur einen Wert bei äquivalenten Klassen aus
	@Override
	public Set<NamedClass> getTypesImpl(Individual individual) {
		String typesDIG = asksPrefix;
		typesDIG += "<types id=\"query_types\">";
		typesDIG += "<individual name=\"" + individual.getName() + "\" />";
		typesDIG += "</types></asks>";

		ResponsesDocument rd = connector.asks(typesDIG);
		TreeSet<NamedClass> resultsSet = new TreeSet<NamedClass>(conceptComparator);
		Concepts[] conceptsArray = rd.getResponses().getConceptSetArray()[0].getSynonymsArray();

		for (int i = 0; i < conceptsArray.length; i++) {
			Named[] atoms = conceptsArray[i].getCatomArray();
			for (Named atom : atoms) {
				NamedClass ac = new NamedClass(atom.getName());
				if (allowedConceptsInSubsumptionHierarchy.contains(ac))
					// if(Config.Refinement.allowedConcepts.contains(ac))
					resultsSet.add(ac);
			}
		}

		// System.out.println("document:");
		// System.out.println(rd);
		// System.out.println("parsed:");
		// System.out.println(resultsSet);

		return resultsSet;
	}

	// es sieht so aus, als ob die XSD-Datei kaputt ist - es gibt zumindest
	// keine getter um ein IndividualPairSet zu finden; in der XSD-Datei ist
	// das in Responsegroup auch nicht definiert
	// => deswegen wird hier die XML-Cursor-API verwendet
	@Override
	public Map<Individual, SortedSet<Individual>> getPropertyMembersImpl(ObjectProperty atomicRole) {
		String relatedIndividualsDIG = asksPrefix;
		relatedIndividualsDIG += "<relatedIndividuals id=\"related_individuals\">";
		relatedIndividualsDIG += "<ratom name=\"" + atomicRole.getName() + "\" />";
		relatedIndividualsDIG += "</relatedIndividuals></asks>";

		ResponsesDocument rd = connector.asks(relatedIndividualsDIG);
		Map<Individual, SortedSet<Individual>> resultMap = new TreeMap<Individual, SortedSet<Individual>>();

		QName name = new QName("name");
		XmlCursor cursor = rd.newCursor();
		cursor.toFirstChild(); // Responses
		cursor.toFirstChild(); // IndividualPairSet

		int childNumber = 0;
		Individual ind1;
		Individual ind2;

		// so lange noch Kinder existieren
		while (cursor.toChild(childNumber)) {
			// Cursor steht jetzt bei einem IndividualPair
			cursor.toFirstChild();
			// jetzt steht er bei einem Individual, dessen Namen wir auslesen
			// können
			ind1 = new Individual(cursor.getAttributeText(name).toString());
			cursor.toNextSibling();
			ind2 = new Individual(cursor.getAttributeText(name).toString());

			Helper.addMapEntry(resultMap, ind1, ind2);

			// Cursor wieder hoch auf IndividualPairSet bewegen
			cursor.toParent();
			cursor.toParent();
			childNumber++;
		}

		/*
		 * System.out.println("document:"); System.out.println(rd);
		 * System.out.println("parsed:"); System.out.println(resultMap);
		 */

		return resultMap;
	}

	@Override
	public boolean isSatisfiableImpl() {
		String satisfiableDIG = asksPrefix;
		// wenn Top erfüllbar ist, dann gibt es auch ein Modell für die KB
		// (satisfiability für KB ist nicht Teil von DIG 1.1)
		satisfiableDIG += "<satisfiable id=\"query_satisfiable\"><top/></satisfiable>";
		satisfiableDIG += "</asks>";

		return parseBooleanAnswer(satisfiableDIG);
	}

	private boolean parseBooleanAnswer(String asks) {
		ResponsesDocument rd = connector.asks(asks);
		if (rd.getResponses().getTrueArray().length == 1)
			return true;
		else
			return false;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void releaseKB() {
		connector.releaseKB(kbURI);
	}

	// TODO: not working yet - it is probably better to include a method
	// in knowledge source to save the corresponding source to a file
	public void saveOntology(File file, OntologyFormat format) {
		// KAON2-Reasoner erzeugen und den die Ontologie speichern lassen
		// (später könnte man das über Jena erledigen, allerdings funktioniert
		// das mit KAON2 auch gut)
		// KAON2Reasoner kaon2Reasoner = new KAON2Reasoner(kb,imports);
		// kaon2Reasoner.saveOntology(file, format);
		throw new UnsupportedOperationException("Saving ontologies not yet implemented.");
	}

	public URL getReasonerURL() {
		return reasonerURL;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getBaseURI()
	 */
	public String getBaseURI() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getPrefixes()
	 */
	public Map<String, String> getPrefixes() {
		return null;
	}

//	@Override
//	public boolean hasDatatypeSupport() {
//		return false;
//	}

}
