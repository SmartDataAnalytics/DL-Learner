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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.IrreflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.utilities.CommonPrefixMap;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

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
	private int maxObjectProperties = 0;
	private int maxDataProperties = 0;
	private int maxClasses = 10;
	private List<Class<? extends AxiomLearningAlgorithm>> objectPropertyAlgorithms;
	private List<Class<? extends AxiomLearningAlgorithm>> dataPropertyAlgorithms;
	private List<Class<? extends LearningAlgorithm>> classAlgorithms;

	private String baseURI = "http://dbpedia.org/resource/";
	private Map<String,String> prefixes = new CommonPrefixMap();
	
	private Connection conn;
	private PreparedStatement ps;
	
	private OWLOntology dbPediaOntology;
	private OWLReasoner reasoner;

	public EnrichmentEvaluation() {

		prefixes = new HashMap<String,String>();
		prefixes.put("dbp","http://dbpedia.org/property/");
		prefixes.put("dbo","http://dbpedia.org/ontology/");
		
		objectPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
//		objectPropertyAlgorithms.add(DisjointObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(EquivalentObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(FunctionalObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(InverseFunctionalObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(ObjectPropertyDomainAxiomLearner.class);
//		objectPropertyAlgorithms.add(ObjectPropertyRangeAxiomLearner.class);
//		objectPropertyAlgorithms.add(SubObjectPropertyOfAxiomLearner.class);
//		objectPropertyAlgorithms.add(SymmetricObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(TransitiveObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(IrreflexiveObjectPropertyAxiomLearner.class);
//		objectPropertyAlgorithms.add(ReflexiveObjectPropertyAxiomLearner.class);

		dataPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
//		dataPropertyAlgorithms.add(DisjointDataPropertyAxiomLearner.class);
//		dataPropertyAlgorithms.add(EquivalentDataPropertyAxiomLearner.class);
//		dataPropertyAlgorithms.add(FunctionalDataPropertyAxiomLearner.class);
//		dataPropertyAlgorithms.add(DataPropertyDomainAxiomLearner.class);
//		dataPropertyAlgorithms.add(DataPropertyRangeAxiomLearner.class); 
//		dataPropertyAlgorithms.add(SubDataPropertyOfAxiomLearner.class);
		
		classAlgorithms = new LinkedList<Class<? extends LearningAlgorithm>>();
//		classAlgorithms.add(DisjointClassesLearner.class);
		classAlgorithms.add(SimpleSubclassLearner.class);
//		classAlgorithms.add(CELOE.class);
		
		initDBConnection();
		loadDBpediaOntology();
	}

	private void initDBConnection() {
		try {
			String iniFile = "db_settings.ini";
			Preferences prefs = new IniPreferences(new FileReader(iniFile));
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);

			Statement s = conn.createStatement();
			s.executeUpdate("DROP TABLE IF EXISTS evaluation");
			s.executeUpdate("CREATE TABLE evaluation ("
					+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "entity VARCHAR(200), algorithm VARCHAR(100), axiom VARCHAR(500), score DOUBLE, runtime_ms INT(20), entailed BOOLEAN)");
			s.close();
			ps = conn.prepareStatement("INSERT INTO evaluation ("
					+ "entity, algorithm, axiom, score, runtime_ms, entailed ) " + "VALUES(?,?,?,?,?,?)");
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

	private void writeToDB(String entity, String algorithm, String axiom, double score, long runTime, boolean entailed) {
		try {
			ps.setString(1, entity);
			ps.setString(2, algorithm);
			ps.setString(3, axiom);
			ps.setDouble(4, score);
			ps.setLong(5, runTime);
			ps.setBoolean(6, entailed);

			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while writing to DB.", e);
			e.printStackTrace();
		}

	}

	public void start() throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			ComponentInitException {
		long overallStartTime = System.currentTimeMillis();
//		ComponentManager cm = ComponentManager.getInstance();

		// create DBpedia Live knowledge source
		SparqlEndpoint se = SparqlEndpoint.getEndpointDBpediaLiveAKSW();

		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		ks.init();
		
		evaluateObjectProperties(ks);
		
		evaluateDataProperties(ks);
		
		evaluateClasses(ks);
		
		System.out.println("Overall runtime: " + (System.currentTimeMillis()-overallStartTime)/1000 + "s.");

	}
	
	private void evaluateObjectProperties(SparqlEndpointKS ks)throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ComponentInitException{
		Set<ObjectProperty> properties = new SPARQLTasks(ks.getEndpoint()).getAllObjectProperties();

		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : objectPropertyAlgorithms) {
			int objectProperties = 0;
			
			String algName = "";
				for (ObjectProperty property : properties) {
					
					try {
						// dynamically invoke constructor with SPARQL knowledge source
						AxiomLearningAlgorithm learner = algorithmClass.getConstructor(
								SparqlEndpointKS.class).newInstance(ks);
						ConfigHelper.configure(learner, "propertyToDescribe", property.toString());
						ConfigHelper.configure(learner, "maxExecutionTimeInSeconds",
								maxExecutionTimeInSeconds);
						learner.init();
						// learner.setPropertyToDescribe(property);
						// learner.setMaxExecutionTimeInSeconds(10);
						algName = AnnComponentManager.getName(learner);
						System.out.println("Applying " + algName + " on " + property + " ... ");
						long startTime = System.currentTimeMillis();
						boolean timeout = false;
						try {
							learner.start();
						} catch (Exception e) {
							if(e.getCause() instanceof SocketTimeoutException){
								timeout = true;throw e;
							}
						}
						long runTime = System.currentTimeMillis() - startTime;
						List<EvaluatedAxiom> learnedAxioms = learner
								.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn);
						if(timeout){
							writeToDB(property.toManchesterSyntaxString(baseURI, prefixes), algName, "TIMEOUT", 0, runTime, false);
						} else if (learnedAxioms == null || learnedAxioms.isEmpty()) {
							writeToDB(property.toManchesterSyntaxString(baseURI, prefixes), algName, "NULL", 0, runTime, false);
						} else {
							for (EvaluatedAxiom learnedAxiom : learnedAxioms) {
								double score = learnedAxiom.getScore().getAccuracy();
								if (Double.isNaN(score)) {
									score = -1;
								}
								writeToDB(property.toManchesterSyntaxString(baseURI, prefixes) .toString(), algName, learnedAxiom.getAxiom().toManchesterSyntaxString(baseURI, prefixes),
										score, runTime, isEntailed(learnedAxiom));
							}
						}
						objectProperties++;
						if (maxObjectProperties != 0 && objectProperties > maxObjectProperties) {
							break;
						}
					
					} catch (Exception e) {
						logger.error("Error occured for object property " + property.getName() + " with algorithm " + algName , e);
					}
				}
			
		}
	}
	
	private void evaluateDataProperties(SparqlEndpointKS ks) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ComponentInitException{
		Set<DatatypeProperty> properties = new SPARQLTasks(ks.getEndpoint()).getAllDataProperties();

		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : dataPropertyAlgorithms) {
			int dataProperties = 0;
			
			String algName = "";
			for (DatatypeProperty property : properties) {

				try{
				// dynamically invoke constructor with SPARQL knowledge source
				AxiomLearningAlgorithm learner = algorithmClass.getConstructor(
						SparqlEndpointKS.class).newInstance(ks);
				ConfigHelper.configure(learner, "propertyToDescribe", property.toString());
				ConfigHelper.configure(learner, "maxExecutionTimeInSeconds",
						maxExecutionTimeInSeconds);
				learner.init();
				// learner.setPropertyToDescribe(property);
				// learner.setMaxExecutionTimeInSeconds(10);
				algName = AnnComponentManager.getName(learner);
				System.out.println("Applying " + algName + " on " + property + " ... ");
				long startTime = System.currentTimeMillis();
				boolean timeout = false;
				try {
					learner.start();
				} catch (Exception e) {
					if(e.getCause() instanceof SocketTimeoutException){
						timeout = true;
					}
				}
				long runTime = System.currentTimeMillis() - startTime;
				List<EvaluatedAxiom> learnedAxioms = learner
						.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn);
				if(timeout){
					writeToDB(property.toManchesterSyntaxString(baseURI, prefixes), algName, "TIMEOUT", 0, runTime, false);
				} else if (learnedAxioms == null || learnedAxioms.isEmpty()) {
					writeToDB(property.toManchesterSyntaxString(baseURI, prefixes), algName, "NULL", 0, runTime, false);
				} else {
					for (EvaluatedAxiom learnedAxiom : learnedAxioms) {
						double score = learnedAxiom.getScore().getAccuracy();
						if (Double.isNaN(score)) {
							score = -1;
						}
						writeToDB(property.toManchesterSyntaxString(baseURI, prefixes) .toString(), algName, learnedAxiom.getAxiom().toManchesterSyntaxString(baseURI, prefixes),
								score, runTime, isEntailed(learnedAxiom));
					}
				}
				dataProperties++;
				if (maxDataProperties != 0 && dataProperties > maxDataProperties) {
					break;
				}
				
			} catch (Exception e) {
				logger.error("Error occured for data property " + property.getName() + " with algorithm " + algName , e);
			}
			}
		}
	}
	
	private void evaluateClasses(SparqlEndpointKS ks) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ComponentInitException{
		Set<NamedClass> classes = new SPARQLTasks(ks.getEndpoint()).getAllClasses();

		for (Class<? extends LearningAlgorithm> algorithmClass : classAlgorithms) {
			int classesCnt = 0;
			for (NamedClass cls : classes) {

				try{
					// dynamically invoke constructor with SPARQL knowledge source
					LearningAlgorithm learner = algorithmClass.getConstructor(
							SparqlEndpointKS.class).newInstance(ks);
					ConfigHelper.configure(learner, "classToDescribe", cls.toString());
					ConfigHelper.configure(learner, "maxExecutionTimeInSeconds",
							maxExecutionTimeInSeconds);
					learner.init();
					// learner.setPropertyToDescribe(property);
					// learner.setMaxExecutionTimeInSeconds(10);
					String algName = AnnComponentManager.getName(learner);
					System.out.println("Applying " + algName + " on " + cls + " ... ");
					long startTime = System.currentTimeMillis();
					boolean timeout = false;
					try {
						learner.start();
					} catch (Exception e) {
						if(e.getCause() instanceof SocketTimeoutException){
							timeout = true;
						}
					}
					long runTime = System.currentTimeMillis() - startTime;
					List<EvaluatedAxiom> learnedAxioms = null;
					if(learner instanceof AxiomLearningAlgorithm){
						learnedAxioms = ((AxiomLearningAlgorithm)learner)
								.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn);
					} else if(learner instanceof AbstractCELA){
						learnedAxioms = new ArrayList<EvaluatedAxiom>();
						List<? extends EvaluatedDescription> learnedDescriptions = ((AbstractCELA)learner).getCurrentlyBestEvaluatedDescriptions(nrOfAxiomsToLearn);
						for(EvaluatedDescription learnedDescription : learnedDescriptions){
							//TODO create axioms and differ between equivalent and subclass mode in CELOE
							learnedAxioms.add(new EvaluatedAxiom(null, new AxiomScore(learnedDescription.getAccuracy())));
						}
					}
					if(timeout){
						writeToDB(cls.toManchesterSyntaxString(baseURI, prefixes), algName, "TIMEOUT", 0, runTime, false);
					} else if (learnedAxioms == null || learnedAxioms.isEmpty()) {
						writeToDB(cls.toManchesterSyntaxString(baseURI, prefixes), algName, "NULL", 0, runTime, false);
					} else {
						for (EvaluatedAxiom learnedAxiom : learnedAxioms) {
							double score = learnedAxiom.getScore().getAccuracy();
							if (Double.isNaN(score)) {
								score = -1;
							}
							writeToDB(cls.toManchesterSyntaxString(baseURI, prefixes) .toString(), algName, learnedAxiom.getAxiom().toManchesterSyntaxString(baseURI, prefixes),
									score, runTime, isEntailed(learnedAxiom));
						}
					}
					
					classesCnt++;
					if (maxClasses != 0 && classesCnt > maxClasses) {
						break;
					}
					
				} catch(Exception e){
					logger.error("Error occured for class " + cls.getName(), e);
				}
			}
		}
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
	
	private boolean isEntailed(EvaluatedAxiom evalAxiom){
		OWLAxiom axiom = OWLAPIConverter.getOWLAPIAxiom(evalAxiom.getAxiom());
		boolean entailed = reasoner.isEntailed(axiom);
		return entailed;
	}
	
	/**
	 * Loads DBpedia ontology from remote URL and initializes the reasoner.
	 */
	private void loadDBpediaOntology(){
		try {
			URL url = new URL("http://downloads.dbpedia.org/3.6/dbpedia_3.6.owl.bz2");
			InputStream is = new BufferedInputStream(url.openStream());
			 CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			 dbPediaOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(in);
			 reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(dbPediaOntology);
			 reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) throws Exception {
		EnrichmentEvaluation ee = new EnrichmentEvaluation();
		ee.start();
		// ee.printResultsPlain();
		Files.createFile(new File("enrichment_eval.html"), ee.printHTMLTable());
		FileAppender app = new FileAppender(new SimpleLayout(), "log/enrichmentEvalErrors.log");
		Logger.getRootLogger().setLevel(Level.ERROR);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(app);
	}

}
