package org.dllearner.kb.sparql;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class Configuration {
	private SparqlEndpoint SparqlEndpoint;
	private SparqlQueryType SparqlQueryType;
	private Manipulator Manipulator;

	private Configuration() {
	}

	public Configuration(SparqlEndpoint SparqlEndpoint, SparqlQueryType SparqlQueryType) {
		this.SparqlEndpoint = SparqlEndpoint;
		this.SparqlQueryType = SparqlQueryType;
	}

	public static Configuration getConfiguration(URI uri) {
		// public static String getTellsString(URL file, URI kbURI){//throws
		// OWLOntologyCreationException{
		Configuration ret = new Configuration();
		try {
			String file = "config/config.owl";

			File f = new File(file);
			String fileURL = "file:///" + f.getAbsolutePath();
			URL u = new URL(fileURL);
			/* Load an ontology from a physical URI */
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(u.toURI());
			// System.out.println( ontology.containsIndividualReference(uri));
			// OWLIndividualImpl ind=new OWLIndividualImpl();
			// System.out.println(ontology.getReferencedIndividuals());
			Set<OWLIndividual> s = ontology.getReferencedIndividuals();
			// System.out.println(ontology.getReferencedClasses());
			// Set<OWLIndividualAxiom> s= ontology.getIndividualAxioms();
			Iterator<OWLIndividual> it = s.iterator();
			while (it.hasNext()) {
				OWLIndividual tmp = (OWLIndividual) it.next();
				// tmp.getURI()
				if (tmp.getURI().equals(uri)) {
					OWLIndividual[] arr = getIndividualsForProperty("hasSparqlEndpoint", tmp
							.getObjectPropertyValues(ontology));
					OWLIndividual sEndpoint = arr[0];
					ret.SparqlEndpoint = makeEndpoint(sEndpoint, ontology);
					arr = getIndividualsForProperty("hasTypedQuery", tmp
							.getObjectPropertyValues(ontology));
					OWLIndividual typedQuery = arr[0];
					ret.SparqlQueryType = makeSparqlQueryType(typedQuery, ontology);

				}
				// {hasSparqlEndpoint=[dbpediaEndpoint]}
			}

			ret.Manipulator = makeManipulator();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static OWLIndividual[] getIndividualsForProperty(String propertyname,
			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> m) {
		Set<OWLObjectPropertyExpression> s = m.keySet();

		Iterator<OWLObjectPropertyExpression> it = s.iterator();
		while (it.hasNext()) {
			OWLObjectPropertyExpression tmp = (OWLObjectPropertyExpression) it.next();
			// System.out.println(tmp);
			// System.out.println(propertyname);
			if (tmp.toString().equals(propertyname)) {
				Object[] arr = ((Set<OWLIndividual>) m.get(tmp)).toArray();
				OWLIndividual[] o = new OWLIndividual[arr.length];
				for (int i = 0; i < o.length; i++) {
					o[i] = (OWLIndividual) arr[i];
				}

				return o;
			}
		}
		return null;

	}

	public static String getFirstValueForDataProperty(String propertyname,
			Map<OWLDataPropertyExpression, Set<OWLConstant>> m) {
		return getValuesForDataProperty(propertyname, m)[0];
	}

	public static String[] getValuesForDataProperty(String propertyname,
			Map<OWLDataPropertyExpression, Set<OWLConstant>> m) {
		Set<OWLDataPropertyExpression> s = m.keySet();

		Iterator<OWLDataPropertyExpression> it = s.iterator();
		while (it.hasNext()) {
			OWLDataPropertyExpression tmp = (OWLDataPropertyExpression) it.next();
			if (tmp.toString().equals(propertyname)) {
				Object[] arr = ((Set<OWLConstant>) m.get(tmp)).toArray();
				String[] str = new String[arr.length];
				for (int i = 0; i < str.length; i++) {
					str[i] = ((OWLConstant) arr[i]).getLiteral();
				}
				return str;
			}
		}
		return null;

	}

	public static SparqlEndpoint makeEndpoint(OWLIndividual sEndpoint, OWLOntology o) {
		String host = getFirstValueForDataProperty("hasHost", sEndpoint.getDataPropertyValues(o));
		String port = getFirstValueForDataProperty("hasPort", sEndpoint.getDataPropertyValues(o));
		String hasAfterGET = getFirstValueForDataProperty("hasAfterGET", sEndpoint
				.getDataPropertyValues(o));
		String hasQueryParameter = getFirstValueForDataProperty("hasQueryParameter", sEndpoint
				.getDataPropertyValues(o));
		OWLIndividual[] para = getIndividualsForProperty("hasGETParameter", sEndpoint
				.getObjectPropertyValues(o));
		// System.out.println("test");
		HashMap<String, String> parameters = new HashMap<String, String>();
		if (para == null)
			return new SparqlEndpoint(host, port, hasAfterGET, hasQueryParameter, parameters);
		for (OWLIndividual p : para) {
			// System.out.println("test2");
			String a1 = getFirstValueForDataProperty("hasParameterName", p.getDataPropertyValues(o));
			String a2 = getFirstValueForDataProperty("hasParameterContent", p
					.getDataPropertyValues(o));
			parameters.put(a1, a2);
		}
		// System.out.println("test2");
		// System.out.println(host+port+ hasAfterGET+ hasQueryParameter+
		// parameters);
		return new SparqlEndpoint(host, port, hasAfterGET, hasQueryParameter, parameters);

	}

	public static SparqlQueryType makeSparqlQueryType(OWLIndividual typedQuery, OWLOntology o) {
		String useLiterals = getFirstValueForDataProperty("usesLiterals", typedQuery
				.getDataPropertyValues(o));
		String hasMode = getFirstValueForDataProperty("hasMode", typedQuery
				.getDataPropertyValues(o));
		// String
		// hasAfterGET=getValuesForDataProperty("hasAfterGET",sEndpoint.getDataPropertyValues(o));
		// String
		// hasQueryParameter=getValuesForDataProperty("hasQueryParameter",sEndpoint.getDataPropertyValues(o));
		OWLIndividual[] objFilter = getIndividualsForProperty("hasObjectFilterSet", typedQuery
				.getObjectPropertyValues(o));
		OWLIndividual[] predFilter = getIndividualsForProperty("hasPredicateFilterSet", typedQuery
				.getObjectPropertyValues(o));

		Set<String> objectFilter = new HashSet<String>();
		Set<String> predicateFilter = new HashSet<String>();

		for (OWLIndividual of : objFilter) {
			String[] tmp = getValuesForDataProperty("filtersURI", of.getDataPropertyValues(o));
			for (String s : tmp) {
				objectFilter.add(s);

			}
		}

		for (OWLIndividual pf : predFilter) {
			String[] tmp = getValuesForDataProperty("filtersURI", pf.getDataPropertyValues(o));
			for (String s : tmp) {
				predicateFilter.add(s);

			}
		}
		// System.out.println(predicateFilter);
		// System.out.println(hasMode+objectFilter+predicateFilter+useLiterals);
		return new SparqlQueryType(hasMode, objectFilter, predicateFilter, useLiterals);

	}

	public static Manipulator makeManipulator() {
		return new Manipulator();
	}

	public Manipulator getManipulator() {
		return this.Manipulator;
	}

	public SparqlEndpoint getSparqlEndpoint() {
		return SparqlEndpoint;
	}

	public SparqlQueryType getSparqlQueryType() {
		return SparqlQueryType;
	}

}
