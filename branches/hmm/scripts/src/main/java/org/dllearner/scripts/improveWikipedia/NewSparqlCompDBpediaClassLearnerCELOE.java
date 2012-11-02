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
 *
 */
package org.dllearner.scripts.improveWikipedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.commons.sparql.core.SparqlTemplate;
import org.apache.velocity.VelocityContext;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.fuzzydll.FuzzyCELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.gui.Config;
import org.dllearner.gui.ConfigSave;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.simple.SparqlSimpleExtractor;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.datastructures.SortedSetTuple;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * A script, which learns definitions / super classes of classes in the DBpedia
 * ontology.
 * 
 * TODO: This script made heavy use of aksw-commons-sparql-scala and needs to be
 * rewritten to use aksw-commons-sparql (the new SPARQL API).
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 * @author Didier Cherix
 */
public class NewSparqlCompDBpediaClassLearnerCELOE {

	public static String endpointurl = "http://live.dbpedia.org/sparql";
	public static int examplesize = 30;

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(NewSparqlCompDBpediaClassLearnerCELOE.class);

	SparqlEndpoint sparqlEndpoint = null;

	public NewSparqlCompDBpediaClassLearnerCELOE() {
		// OPTIONAL: if you want to do some case distinctions in the learnClass
		// method, you could add
		// parameters to the constructure e.g. YAGO_
		try {
			sparqlEndpoint = new SparqlEndpoint(new URL(endpointurl));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[])
			throws LearningProblemUnsupportedException, IOException, Exception {
		for (int i = 0; i < 4; i++) {
			NewSparqlCompDBpediaClassLearnerCELOE dcl = new NewSparqlCompDBpediaClassLearnerCELOE();
			Set<String> classesToLearn = dcl.getClasses();
			
			Monitor mon = MonitorFactory.start("Learn DBpedia");
			KB kb = dcl.learnAllClasses(classesToLearn);
			mon.stop();
			kb.export(new File("/home/dcherix/dllearner/simple/result" + i
					+ ".owl"), OntologyFormat.RDF_XML);
			// Set<String> pos =
			// dcl.getPosEx("http://dbpedia.org/ontology/Person");
			// dcl.getNegEx("http://dbpedia.org/ontology/Person", pos);
			logger.info("Test" + i + ":\n"
					+ JamonMonitorLogger.getStringForAllSortedByLabel());
			System.out.println(JamonMonitorLogger
					.getStringForAllSortedByLabel());
		}
	}

	public KB learnAllClasses(Set<String> classesToLearn) {
		KB kb = new KB();
		for (String classToLearn : classesToLearn) {
			logger.info("learning "+classToLearn);
			try {
				Description d = learnClass(classToLearn);
				if (d == null
						|| d.toKBSyntaxString().equals(
								new Thing().toKBSyntaxString())) {
					logger.error("Description was " + d + ", continueing");
					continue;
				}
				kb.addAxiom(new EquivalentClassesAxiom(new NamedClass(
						classToLearn), d));
				kb.export(new File(
						"/home/dcherix/dllearner/simple/result_partial.owl"),
						OntologyFormat.RDF_XML);

			} catch (Exception e) {
				logger.warn("", e);
			}
			System.gc();
		}

		return kb;
	}

	public Description learnClass(String classToLearn) throws Exception {
		// TODO: use aksw-commons-sparql instead of sparql-scala
		SortedSet<String> posEx = new TreeSet<String>(getPosEx(classToLearn));
		logger.info("Found " + posEx.size() + " positive examples");
		if (posEx.isEmpty()) {
			return null;
		}
		SortedSet<String> negEx = new TreeSet<String>(getNegEx(classToLearn,
				posEx));

		posEx = SetManipulation.fuzzyShrink(posEx, examplesize);
		negEx = SetManipulation.fuzzyShrink(negEx, examplesize);

		SortedSet<Individual> posExamples = Helper.getIndividualSet(posEx);
		SortedSet<Individual> negExamples = Helper.getIndividualSet(negEx);
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(
				posExamples, negExamples);

		ComponentManager cm = ComponentManager.getInstance();

		SparqlSimpleExtractor ks = cm
				.knowledgeSource(SparqlSimpleExtractor.class);
		ks.setInstances(new ArrayList<String>(Datastructures
				.individualSetToStringSet(examples.getCompleteSet())));
		// ks.getConfigurator().setPredefinedEndpoint("DBPEDIA"); // TODO:
		// probably the official endpoint is too slow?
		ks.setEndpointURL(endpointurl);
		// ks.setUseLits(false);
		// ks.setUseCacheDatabase(true);
		ks.setRecursionDepth(1);
		ArrayList<String> ontologyUrls = new ArrayList<String>();
		ontologyUrls.add("http://downloads.dbpedia.org/3.6/dbpedia_3.6.owl");
		ks.setOntologySchemaUrls(ontologyUrls);
		ks.setAboxfilter("FILTER (!regex(str(?p), '^http://dbpedia.org/property/wikiPageUsesTemplate') && "
				+ "!regex(str(?p), '^http://dbpedia.org/ontology/wikiPageExternalLink') && "
				+ "!regex(str(?p), '^http://dbpedia.org/property/wordnet_type') && "
				+ "!regex(str(?p), '^http://www.w3.org/2002/07/owl#sameAs')) .");
		ks.setTboxfilter("FILTER ( !regex(str(?class), '^http://dbpedia.org/class/yago/') && "
				+ "!regex(str(?class), '^http://dbpedia.org/resource/Category:')) ");
		// ks.setCloseAfterRecursion(true);
		// ks.setSaveExtractedFragment(true);
		// ks.setPredList(new HashSet<String>(Arrays.asList(new String[] {
		// "http://dbpedia.org/property/wikiPageUsesTemplate",(!regex(str(?p),
		// '^http://dbpedia.org/resource/') && ! regex(str(?o),
		// '^http://dbpedia.org/resource/Category') )
		// "http://dbpedia.org/ontology/wikiPageExternalLink",
		// "http://dbpedia.org/property/wordnet_type",
		// "http://www.w3.org/2002/07/owl#sameAs" })));

		// ks.setObjList(new HashSet<String>(Arrays.asList(new String[] {
		// "http://dbpedia.org/class/yago/",
		// "" + "http://dbpedia.org/resource/Category:" })));

		ks.init();

		AbstractReasonerComponent rc = cm.reasoner(FastInstanceChecker.class,
				ks);
		rc.init();

		PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);
		lp.setAccuracyMethod("fmeasure");
		lp.setUseApproximations(false);
		lp.init();
		CELOE la = cm.learningAlgorithm(CELOE.class, lp, rc);
		// CELOEConfigurator cc = la.getConfigurator();
		la.setMaxExecutionTimeInSeconds(100);
		la.init();
		RhoDRDown op = (RhoDRDown) la.getOperator();

		op.setUseNegation(false);
		op.setUseAllConstructor(false);
		op.setUseCardinalityRestrictions(false);
		op.setUseHasValueConstructor(true);

		la.setNoisePercentage(20);
		la.setIgnoredConcepts(new HashSet<NamedClass>(Arrays
				.asList(new NamedClass[] { new NamedClass(classToLearn) })));
		la.init();

		// to write the above configuration in a conf file (optional)
		Config cf = new Config(cm, ks, rc, lp, la);
		// new ConfigSave(cf).saveFile(new File("/dev/null"));

		la.start();

		cm.freeAllComponents();
		return la.getCurrentlyBestDescription();
	}

	public Set<String> getClasses() throws Exception {
		OntModel model = ModelFactory.createOntologyModel();
		model.read(new FileInputStream(
				"/home/dcherix/Downloads/dbpedia_3.6.owl"), null);
		Set<OntClass> classes = model.listClasses().toSet();
		Set<String> results = new HashSet<String>();
		int i = 0;
		for (OntClass ontClass : classes) {
			results.add(ontClass.getURI());
			i++;
		}
		return results;
	}

	// gets all DBpedia Classes
	// public Set<String> getClasses() throws Exception {
	// SparqlTemplate st = SparqlTemplate.getInstance("allClasses.vm");
	// st.setLimit(0);
	// st.addFilter(sparqlEndpoint.like("classes", new
	// HashSet<String>(Arrays.asList(new
	// String[]{"http://dbpedia.org/ontology/"}))));
	// VelocityContext vc = st.putSgetVelocityContext();
	// String query = st.getQuery();
	// return new
	// HashSet<String>(ResultSetRenderer.asStringSet(sparqlEndpoint.executeSelect(query)));
	// }
	//
	public Set<String> getPosEx(String clazz) throws Exception {
		SparqlTemplate st = SparqlTemplate.getInstance("instancesOfClass.vm");
		st.setLimit(0);
		VelocityContext vc = st.getVelocityContext();
		vc.put("class", clazz);
		String queryString = st.getQuery();
		return this.executeResourceQuery(queryString);
	}

	/**
	 * gets all direct classes of all instances and has a look, what the most
	 * common is
	 * 
	 * @param clazz
	 * @param posEx
	 * @return
	 * @throws Exception
	 */
	public String selectClass(String clazz, Set<String> posEx) throws Exception {
		Map<String, Integer> m = new HashMap<String, Integer>();
		// TODO: use aksw-commons-sparql instead of sparql-scala
		/*
		 * for (String pos : posEx) { SparqlTemplate st =
		 * SparqlTemplate.getInstance("directClassesOfInstance.vm");
		 * st.setLimit(0); st.addFilter(sparqlEndpoint.like("direct", new
		 * HashSet<String>(Arrays.asList(new
		 * String[]{"http://dbpedia.org/ontology/"})))); VelocityContext vc =
		 * st.getVelocityContext(); vc.put("instance", pos); String query =
		 * st.getQuery(); Set<String> classes = new
		 * HashSet<String>(ResultSetRenderer
		 * .asStringSet(sparqlEndpoint.executeSelect(query)));
		 * classes.remove(clazz); for (String s : classes) { if (m.get(s) ==
		 * null) { m.put(s, 0); } m.put(s, m.get(s).intValue() + 1); } }
		 */

		int max = 0;
		String maxClass = "";
		for (String key : m.keySet()) {
			if (m.get(key).intValue() > max) {
				maxClass = key;
			}
		}

		return maxClass;
	}

	/**
	 * gets instances of a class or random instances
	 * 
	 * @param clazz
	 * @param posEx
	 * @return
	 * @throws Exception
	 */

	public Set<String> getNegEx(String clazz, Set<String> posEx)
			throws Exception {
		Set<String> negEx = new HashSet<String>();
		// TODO: use aksw-commons-sparql instead of sparql-scala
		/*
		 * String targetClass = getParallelClass(clazz);
		 * logger.info("using class for negatives: " + targetClass); if
		 * (targetClass != null) {
		 * 
		 * SparqlTemplate st =
		 * SparqlTemplate.getInstance("instancesOfClass.vm"); st.setLimit(0);
		 * VelocityContext vc = st.getVelocityContext(); vc.put("class",
		 * targetClass); // st.addFilter(sparqlEndpoint.like("class", new
		 * HashSet<String>(Arrays.asList(new
		 * String[]{"http://dbpedia.org/ontology/"})))); String query =
		 * st.getQuery(); // negEx.addAll(new
		 * HashSet<String>(ResultSetRenderer.asStringSet
		 * (sparqlEndpoint.executeSelect(query)))); } else {
		 * 
		 * SparqlTemplate st = SparqlTemplate.getInstance("someInstances.vm");
		 * st.setLimit(posEx.size() + 100); VelocityContext vc =
		 * st.getVelocityContext(); String query = st.getQuery(); //
		 * negEx.addAll(new
		 * HashSet<String>(ResultSetRenderer.asStringSet(sparqlEndpoint
		 * .executeSelect(query)))); } negEx.removeAll(posEx);
		 */

		String targetClass = getParallelClass(clazz);
		logger.info("using class for negatives: " + targetClass);
		if (targetClass != null) {
			SparqlTemplate st = SparqlTemplate
					.getInstance("instancesOfClass.vm");
			st.setLimit(0);
			VelocityContext vc = st.getVelocityContext();
			vc.put("class", targetClass);
			st.addFilter("FILTER ( ?class LIKE (<http://dbpedia.org/ontology/%>");

			String query = st.getQuery();
			negEx.addAll(this.executeResourceQuery(query));
		} else {
			SparqlTemplate st = SparqlTemplate.getInstance("someInstances.vm");
			st.setLimit(posEx.size() + 100);
			VelocityContext vc = st.getVelocityContext();
			String query = st.getQuery();
			negEx.addAll(this.executeResourceQuery(query));
		}
		negEx.removeAll(posEx);
		return negEx;

	}

	public String getParallelClass(String clazz) throws Exception {
		// TODO: use aksw-commons-sparql instead of sparql-scala
		// SparqlTemplate st = SparqlTemplate.getInstance("parallelClass.vm");
		// st.setLimit(0);
		// VelocityContext vc = st.getVelocityContext();
		// vc.put("class", clazz);
		// String query = st.getQuery();
		// Set<String> parClasses = new
		// HashSet<String>(ResultSetRenderer.asStringSet(sparqlEndpoint.executeSelect(query)));
		// for (String s : parClasses) {
		// return s;
		// }
		SparqlTemplate st = SparqlTemplate.getInstance("parallelClass.vm");
		st.setLimit(0);
		VelocityContext vc = st.getVelocityContext();
		vc.put("class", clazz);
		String query = st.getQuery();
		Set<String> parClasses = this.executeClassQuery(query);
		for (String s : parClasses) {
			if (s.startsWith("http://dbpedia.org/ontology")) {
				if (!s.endsWith("Unknown")) {
					return s;
				}
			}
		}
		return null;
	}

	public Set<String> executeResourceQuery(String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointurl,
				query);
		ResultSet resultSet = qexec.execSelect();
		QuerySolution solution;
		Set<String> results = new HashSet<String>();
		while (resultSet.hasNext()) {
			solution = resultSet.next();
			results.add(solution.getResource("instances").getURI());
		}
		return results;
	}

	public Set<String> executeClassQuery(String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointurl,
				query);
		ResultSet resultSet = qexec.execSelect();
		QuerySolution solution;
		Set<String> results = new HashSet<String>();
		while (resultSet.hasNext()) {
			solution = resultSet.next();
			results.add(solution.getResource("sub").getURI());
		}
		return results;
	}

}
