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
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.properties.FunctionalPropertyAxiomLearner;
import org.dllearner.algorithms.properties.PropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.PropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.SubPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitivePropertyAxiomLearner;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.Files;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Evaluation of enrichment algorithms on DBpedia (Live).
 * 
 * @author Jens Lehmann
 * 
 */
public class EnrichmentEvaluation {

	private static Logger logger = Logger.getLogger(EnrichmentEvaluation.class);

	// max. execution time for each learner for each entity
	private int maxExecutionTimeInSeconds = 10;

	// number of axioms which will be learned/considered (only applies to
	// some learners)
	private int nrOfAxiomsToLearn = 10;

	// can be used to only evaluate a part of DBpedia
	private int maxObjectProperties = 3;
	private int maxDataProperties = 3;
	private int maxClasses = 3;
	private List<Class<? extends AxiomLearningAlgorithm>> objectPropertyAlgorithms;
	private List<Class<? extends AxiomLearningAlgorithm>> dataPropertyAlgorithms;

	private Connection conn;
	private PreparedStatement ps;

	public EnrichmentEvaluation() {
		initDBConnection();

		objectPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		// objectPropertyAlgorithms.add(DisjointPropertyAxiomLearner.class);
		// objectPropertyAlgorithms.add(EquivalentPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(FunctionalPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(PropertyDomainAxiomLearner.class);
		objectPropertyAlgorithms.add(PropertyRangeAxiomLearner.class);
		objectPropertyAlgorithms.add(SubPropertyOfAxiomLearner.class);
		objectPropertyAlgorithms.add(SymmetricPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(TransitivePropertyAxiomLearner.class);

		dataPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		// dataPropertyAlgorithms.add(DisjointPropertyAxiomLearner.class);
		// dataPropertyAlgorithms.add(EquivalentPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(FunctionalPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(PropertyDomainAxiomLearner.class);
		dataPropertyAlgorithms.add(PropertyRangeAxiomLearner.class); // ?
		dataPropertyAlgorithms.add(SubPropertyOfAxiomLearner.class);
	}

	private void initDBConnection() {
		try {
			String iniFile = "db_settings.ini";
			Preferences prefs = new IniPreferences(new FileReader(iniFile));
			String dbServer = prefs.node("database").get("server", null);
			String dbName = "enrichment";
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);

			Statement s = conn.createStatement();
			s.executeUpdate("DROP TABLE IF EXISTS evaluation");
			s.executeUpdate("CREATE TABLE evaluation ("
					+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "entity VARCHAR(200), algorithm VARCHAR(100), axiom VARCHAR(500), score DOUBLE, runtime_ms INT(20))");
			s.close();
			ps = conn.prepareStatement("INSERT INTO evaluation ("
					+ "entity, algorithm, axiom, score, runtime_ms ) " + "VALUES(?,?,?,?,?)");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeToDB(String entity, String algorithm, String axiom, double score, long runTime) {
		try {
			ps.setString(1, entity);
			ps.setString(2, algorithm);
			ps.setString(3, axiom);
			ps.setDouble(4, score);
			ps.setLong(5, runTime);

			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while writing to DB.", e);
			e.printStackTrace();
		}

	}

	public void start() throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			ComponentInitException {

		ComponentManager cm = ComponentManager.getInstance();

		// create DBpedia Live knowledge source
		SparqlEndpoint se = SparqlEndpoint.getEndpointDBpediaLiveAKSW();

		Set<ObjectProperty> properties = getAllObjectProperties(se);

		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		ks.init();

		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : objectPropertyAlgorithms) {
			int objectProperties = 0;
			for (ObjectProperty property : properties) {

				// dynamically invoke constructor with SPARQL knowledge source
				AxiomLearningAlgorithm learner = algorithmClass.getConstructor(
						SparqlEndpointKS.class).newInstance(ks);
				ConfigHelper.configure(learner, "propertyToDescribe", property.toString());
				ConfigHelper.configure(learner, "maxExecutionTimeInSeconds",
						maxExecutionTimeInSeconds);
				learner.init();
				// learner.setPropertyToDescribe(property);
				// learner.setMaxExecutionTimeInSeconds(10);
				String algName = ComponentManager.getName(learner);
				System.out.println("Applying " + algName + " on " + property + " ... ");
				long startTime = System.currentTimeMillis();
				learner.start();
				long runTime = System.currentTimeMillis() - startTime;
				List<EvaluatedAxiom> learnedAxioms = learner
						.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn);
				if (learnedAxioms == null) {
					writeToDB(property.toString(), algName, "NULL", 0, runTime);
				} else {
					for (EvaluatedAxiom learnedAxiom : learnedAxioms) {
						double score = learnedAxiom.getScore().getAccuracy();
						if (Double.isNaN(score)) {
							score = -1;
						}
						writeToDB(property.toString(), algName, learnedAxiom.getAxiom().toString(),
								score, runTime);
					}
				}
				objectProperties++;
				if (objectProperties > maxObjectProperties) {
					break;
				}
			}
		}

	}

	private void getAllClasses() {
	}

	private Set<ObjectProperty> getAllObjectProperties(SparqlEndpoint se) {
		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:ObjectProperty}";
		SparqlQuery sq = new SparqlQuery(query, se);
		// Claus' API
		// Sparqler x = new SparqlerHttp(se.getURL().toString());
		// SelectPaginated q = new SelectPaginated(x, , 1000);
		ResultSet q = sq.send();
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			properties.add(new ObjectProperty(qs.getResource("p").getURI()));
		}
		return properties;
	}

	public void printResultsPlain() {

	}

	public void printResultsLaTeX() {

	}

	public String printHTMLTable() throws SQLException {
		StringBuffer sb = new StringBuffer();
		Statement s = conn.createStatement();
		s.executeQuery("SELECT * FROM evaluation");
		java.sql.ResultSet rs = s.getResultSet();

		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();
		sb.append("<table border=1>");
		sb.append("<tr>");
		for (int i = 1; i <= count; i++) {
			sb.append("<th>");
			sb.append(md.getColumnLabel(i));
			sb.append("</th>");
		}
		sb.append("</tr>");
		while (rs.next()) {
			sb.append("<tr>");
			for (int i = 1; i <= count; i++) {
				sb.append("<td>");
				sb.append(rs.getString(i));
				sb.append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		rs.close();
		s.close();
		return sb.toString();
	}

	public static void main(String[] args) throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, ComponentInitException, SQLException {
		EnrichmentEvaluation ee = new EnrichmentEvaluation();
		ee.start();
		// ee.printResultsPlain();
		Files.createFile(new File("enrichment_eval.html"), ee.printHTMLTable());
	}

}
