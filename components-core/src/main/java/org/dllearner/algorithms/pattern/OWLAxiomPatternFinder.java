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
package org.dllearner.algorithms.pattern;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.io.FileUtils;
import org.dllearner.kb.dataset.OWLOntologyDataset;
import org.dllearner.kb.repository.LocalDirectoryOntologyRepository;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.utilities.ProgressBar;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.ini4j.IniPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.functional.renderer.FunctionalSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class OWLAxiomPatternFinder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OWLAxiomPatternFinder.class);
	
	private OntologyRepository repository;
	private OWLOntologyManager manager;
	private OWLDataFactory dataFactory;
	
	private Connection conn;
	private PreparedStatement selectOntologyIdPs;
	private PreparedStatement insertOntologyPs;
	private PreparedStatement insertOntologyImportPs;
	private PreparedStatement insertOntologyErrorPs;

	private PreparedStatement selectPatternIdPs;
	private PreparedStatement insertPatternIdPs;
	private PreparedStatement insertOntologyPatternPs;

	private PreparedStatement insertPatternGeneralizationPs;
	private PreparedStatement insertPatternToPatternGeneralizationPs;
	private PreparedStatement selectGeneralizedPatternIdPs;

	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();
	
	private boolean randomOrder = false;

	private boolean multithreadedEnabled = false;
	private int numThreads = 4;//Runtime.getRuntime().availableProcessors() - 1

	public OWLAxiomPatternFinder(OWLOntologyDataset dataset) {
		
	}
	
	public OWLAxiomPatternFinder(OntologyRepository repository) {
		this.repository = repository;
		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();
		
		initDBConnection();
		prepare();
	}
	
	public OWLAxiomPatternFinder(OntologyRepository repository, Connection conn) {
		this.repository = repository;
		this.conn = conn;
		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();
		
		prepare();
	}

	/**
	 * Start the pattern detection.
	 */
	public void start() {
		final ExecutorService tp = Executors.newFixedThreadPool(multithreadedEnabled ? numThreads : 1);

		Collection<OntologyRepositoryEntry> entries = repository.getEntries();
		if(randomOrder){
			List<OntologyRepositoryEntry> entryList = new ArrayList<>(repository.getEntries());
			Collections.shuffle(entryList);
			entries = entryList;
		}

		final Multiset<OWLAxiom> allAxiomPatterns = HashMultiset.create();

		AtomicInteger i = new AtomicInteger(0);

		manager = OWLManager.createConcurrentOWLOntologyManager();

		entries = entries.stream().filter(e -> e.getOntologyShortName().equals("NIDM-RESULTS")).collect(Collectors.toList());

		for (OntologyRepositoryEntry entry : entries) {
			tp.execute(() -> {
					OWLAxiomRenamer renamer = new OWLAxiomRenamer(dataFactory);

					URI uri = entry.getPhysicalURI();
					LOGGER.info(i.incrementAndGet() + ": " + entry.getOntologyShortName() + " (" +
											   FileUtils.byteCountToDisplaySize(new File(uri).length()) + ")");
//					if(uri.toString().startsWith("http://rest.bioontology.org/bioportal/ontologies/download/42764")){
					if (true) {//!ontologyProcessed(uri)) {
						LOGGER.info("Loading \"" + entry.getOntologyShortName() + "\" from " + uri + " ...");
						try {

							OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

							OWLOntology ontology = manager.loadOntology(IRI.create(uri));
							Set<OWLLogicalAxiom> logicalAxioms = ontology.getLogicalAxioms(Imports.EXCLUDED);
							LOGGER.info("   finished loading \"" + entry.getOntologyShortName() + "\". #Axioms: " + logicalAxioms.size());
							addOntology(uri, ontology, Imports.EXCLUDED);

							LOGGER.info("Running pattern detection for \"" + entry.getOntologyShortName() + "\" ...");
							Multiset<OWLAxiom> axiomPatterns = HashMultiset.create();
							int cnt = 0;
							ProgressBar mon = new ProgressBar();
							for (OWLAxiom axiom : logicalAxioms) {
//								if(axiom.isOfType(AxiomType.SUBCLASS_OF)) {
//									System.out.println(MaximumModalDepthDetector.getMaxModalDepth(axiom));
//									System.out.println(((OWLSubClassOfAxiom)axiom).getSuperClass());
//									OWLClassExpression sup = ((OWLSubClassOfAxiom) axiom).getSuperClass();
//									if(sup instanceof OWLObjectIntersectionOf) {
//										List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) sup).getOperandsAsList();
//										System.out.println("OPs: " + operands.size());
//										if(operands.get(0).toString().equals("Oxidative-Phosphorylation")) {
//											manager.createOntology(Sets.newHashSet(axiom)).saveOntology(
//													new RDFXMLDocumentFormat(),
//													new FileOutputStream("/tmp/longaxiom.owl"));
//											System.exit(0);
//										}
//									}
//								}

								OWLAxiom renamedAxiom = renamer.rename(axiom);
								axiomPatterns.add(renamedAxiom);
								cnt++;
								if(cnt % 100 == 0) {
									mon.update(cnt, logicalAxioms.size());
								}
							}

							LOGGER.info("   finished pattern detection for \"" + entry.getOntologyShortName() + "\". #Patterns: " +
												axiomPatterns.elementSet().size());
//							allAxiomPatterns.addAll(axiomPatterns);
							addOntologyPatterns(uri, ontology, axiomPatterns);
//							for (OWLAxiom owlAxiom : Multisets.copyHighestCountFirst(allAxiomPatterns).elementSet()) {
//								System.out.println(owlAxiom + ": " + allAxiomPatterns.count(owlAxiom));
//							}

							// process the imports separately
							Set<OWLOntology> imports = ontology.getImports();
							if(!imports.isEmpty()) {
								LOGGER.info("Processing the imports of \"" + entry.getOntologyShortName() + "\" ...");
							}
							imports.stream().forEach(importedOntology -> {
								IRI iri = importedOntology.getOntologyID().getOntologyIRI().or(manager.getOntologyDocumentIRI(importedOntology));
								System.out.println(iri.toString());

								// check if it was already processed before
								if(!ontologyProcessed(iri.toURI())) {
									addOntology(iri.toURI(), importedOntology, Imports.INCLUDED);
									LOGGER.info("Running pattern detection for import from " + iri + " ...");
									axiomPatterns.clear();
									importedOntology.getLogicalAxioms(Imports.INCLUDED).stream().forEach(axiom -> {
										OWLAxiom renamedAxiom = renamer.rename(axiom);
										axiomPatterns.add(renamedAxiom);
									});
									addOntologyPatterns(iri.toURI(), importedOntology, axiomPatterns);
									addOntologyImport(uri, ontology, iri.toURI(), importedOntology);
									LOGGER.info("   finished pattern detection for import from " + iri + ". #Patterns: " +
														axiomPatterns.elementSet().size());
								} else {
									LOGGER.info("Import " + iri + " already processed.");
								}

							});



							manager.removeOntology(ontology);
						} catch (OWLOntologyAlreadyExistsException e) {
							e.printStackTrace();
						} catch (UnloadableImportException e) {
							LOGGER.error("Import loading failed.", e.getMessage());
							addOntologyError(uri, e);
						} catch(UnparsableOntologyException e) {
							e.printStackTrace();
							LOGGER.error("Parsing of ontology failed.", e.getMessage());
							addOntologyError(uri, e);
						} catch (Exception e) {
							e.printStackTrace();
							LOGGER.error("Ontology processing failed", e);
							addOntologyError(uri, e);
						}
					} else {
						LOGGER.info("Already processed.");
					}
				});
		}

		try {
			tp.shutdown();
			tp.awaitTermination(600, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			System.err.println("tasks interrupted");

			// Preserve interrupt status
			Thread.currentThread().interrupt();
		} finally {
			if (!tp.isTerminated()) {
				System.err.println("cancel non-finished tasks");
			}
			tp.shutdownNow();
			System.out.println("shutdown finished");
		}


		computeAndAddGeneralizedPatterns();

		try {
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("Failed to close DB connection.", e);
		}
	}

	public void setMultithreadedEnabled(boolean multithreadedEnabled) {
		this.multithreadedEnabled = multithreadedEnabled;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
		if(numThreads < 0) {
			numThreads = Runtime.getRuntime().availableProcessors() - 1;
		}
	}

	private void prepare(){
		createTables();
		try {
			selectOntologyIdPs = conn.prepareStatement("SELECT id FROM Ontology WHERE url=?");
			insertOntologyPs = conn.prepareStatement("INSERT INTO Ontology (url, iri, repository, logical_axioms, tbox_axioms, rbox_axioms" +
					", abox_axioms, classes, object_properties, data_properties, individuals) VALUES(?,?,?,?,?,?,?,?,?,?,?)");

			insertOntologyImportPs = conn.prepareStatement("INSERT INTO Ontology_Import (ontology_id1, ontology_id2) VALUES(?,?)");
			insertOntologyErrorPs = conn.prepareStatement("INSERT INTO Ontology_Error (url, repository, error) VALUES(?,?,?)");

			selectPatternIdPs = conn.prepareStatement("SELECT id FROM Pattern WHERE pattern=?");
			insertPatternIdPs = conn.prepareStatement("INSERT INTO Pattern (pattern,pattern_pretty,axiom_type) VALUES(?,?,?)");
			insertOntologyPatternPs = conn.prepareStatement("INSERT INTO Ontology_Pattern (ontology_id, pattern_id, occurrences) VALUES(?,?,?)");

			insertPatternGeneralizationPs = conn.prepareStatement("INSERT INTO Pattern_Generalized (pattern,pattern_pretty,axiom_type) VALUES(?,?,?)");
			insertPatternToPatternGeneralizationPs = conn.prepareStatement("INSERT INTO Pattern_Pattern_Generalized (pattern_id, generalized_pattern_id) VALUES(?,?)");
			selectGeneralizedPatternIdPs = conn.prepareStatement("SELECT id FROM Pattern_Generalized WHERE pattern=?");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String render(OWLAxiom axiom){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology();
			man.addAxiom(ontology, axiom);
			StringWriter sw = new StringWriter();
			FunctionalSyntaxObjectRenderer r = new FunctionalSyntaxObjectRenderer(ontology, sw);
			axiom.accept(r);
			return sw.toString();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void initDBConnection() {
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(
					"org/dllearner/algorithms/pattern/db_settings.ini");
			Preferences prefs = new IniPreferences(is);
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);
		} catch (IOException e) {
			LOGGER.error("Failed to settings.", e);
		} catch (SQLException e) {
			LOGGER.error("Failed to setup database connection.", e);
		}
	}
	
	private void createTables(){
		try (Statement statement = conn.createStatement()){
			statement.execute("CREATE TABLE IF NOT EXISTS Pattern ("
			        + "id MEDIUMINT NOT NULL AUTO_INCREMENT,"
					+ "pattern TEXT NOT NULL,"
					+ "pattern_pretty TEXT NOT NULL,"
					+ "axiom_type VARCHAR(15) NOT NULL,"
					+ "PRIMARY KEY(id),"
					+ "INDEX(pattern(1000))) DEFAULT CHARSET=utf8");
			
			statement.execute("CREATE TABLE IF NOT EXISTS Ontology (" 
			        + "id MEDIUMINT NOT NULL AUTO_INCREMENT,"
					+ "url VARCHAR(1000) NOT NULL,"
					+ "iri VARCHAR(2000) NOT NULL,"
					+ "repository VARCHAR(200) NOT NULL,"
					+ "logical_axioms MEDIUMINT DEFAULT 0,"
					+ "tbox_axioms MEDIUMINT DEFAULT 0,"
					+ "rbox_axioms MEDIUMINT DEFAULT 0,"
					+ "abox_axioms MEDIUMINT DEFAULT 0,"
					+ "classes MEDIUMINT DEFAULT 0,"
					+ "object_properties MEDIUMINT DEFAULT 0,"
					+ "data_properties MEDIUMINT DEFAULT 0,"
					+ "individuals MEDIUMINT DEFAULT 0,"
					+ "PRIMARY KEY(id),"
					+ "INDEX(url)) DEFAULT CHARSET=utf8");

			statement.execute("CREATE TABLE IF NOT EXISTS Ontology_Import ("
									  + "ontology_id1 MEDIUMINT NOT NULL,"
									  + "ontology_id2 MEDIUMINT NOT NULL,"
									  + "PRIMARY KEY(ontology_id1, ontology_id2))"
									  );

			statement.execute("CREATE TABLE IF NOT EXISTS Ontology_Error ("
									  + "url VARCHAR(1000) NOT NULL,"
									  + "repository VARCHAR(200) NOT NULL,"
									  + "error VARCHAR(2000) NOT NULL,"
									  + "PRIMARY KEY(url, repository))"
			);

			statement.execute("CREATE TABLE IF NOT EXISTS Ontology_Pattern (" 
			        + "ontology_id MEDIUMINT NOT NULL,"
					+ "pattern_id MEDIUMINT NOT NULL,"
					+ "occurrences INTEGER(8) NOT NULL,"
					+ "FOREIGN KEY (ontology_id) REFERENCES Ontology(id) ON DELETE CASCADE,"
					+ "FOREIGN KEY (pattern_id) REFERENCES Pattern(id) ON DELETE CASCADE,"
					+ "PRIMARY KEY(ontology_id, pattern_id)) DEFAULT CHARSET=utf8");

			statement.execute("CREATE TABLE IF NOT EXISTS Pattern_Generalized ("
					+ "id MEDIUMINT NOT NULL AUTO_INCREMENT,"
					+ "pattern TEXT NOT NULL,"
					+ "pattern_pretty TEXT NOT NULL,"
					+ "axiom_type VARCHAR(15) NOT NULL,"
					+ "PRIMARY KEY(id),"
					+ "INDEX(pattern(1000))) DEFAULT CHARSET=utf8");

			statement.execute("CREATE TABLE IF NOT EXISTS Pattern_Pattern_Generalized ("
					+ "pattern_id MEDIUMINT NOT NULL,"
					+ "generalized_pattern_id MEDIUMINT NOT NULL,"
					+ "FOREIGN KEY (pattern_id) REFERENCES Pattern(id) ON DELETE CASCADE,"
					+ "FOREIGN KEY (generalized_pattern_id) REFERENCES Pattern_Generalized(id) ON DELETE CASCADE,"
					+ "PRIMARY KEY(pattern_id, generalized_pattern_id)) DEFAULT CHARSET=utf8");


		} catch (SQLException e) {
			LOGGER.error("Failed to setup database tables.", e);
		}
	}

	OWLAxiomGeneralizer generalizer = new OWLAxiomGeneralizer();

	private void computeAndAddGeneralizedPatterns() {
		// get all patterns from DB
		String sql = "SELECT id, pattern FROM Pattern";
		try(Statement stmt = conn.createStatement()) {
			try(ResultSet rs = stmt.executeQuery(sql)){
				while(rs.next()) {
					int id = rs.getInt(1);
					String patternStr = rs.getString(2);

					// parse to OWLAxiom
					OWLAxiom axiom = parse(patternStr);

					// compute generalizations
					System.out.println(axiom);
					Set<OWLAxiom> generalizations = generalizer.generalize(axiom);

					// add to DB
					generalizations.forEach(gen -> {
						// add generalized pattern if not exist and get ID
						int genID = addGeneralizedPattern(gen);

						// add mapping from pattern to generalized pattern
						try {
							insertPatternToPatternGeneralizationPs.setInt(1, id);
							insertPatternToPatternGeneralizationPs.setInt(2, genID);
							insertPatternToPatternGeneralizationPs.addBatch();
						} catch (SQLException e) {
							LOGGER.error("Failed to insert pattern to gen. pattern mapping.", e);
						}
					});
					insertPatternToPatternGeneralizationPs.executeBatch();
				}
			} catch(SQLException e) {
				LOGGER.error("Failed to get patterns from DB.", e);
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to create statement.", e);
		}

	}

	private OWLAxiom parse(String axiomStr) {
		String ontologyStr = "Ontology (" + axiomStr + ")";
		try {
			OWLOntology ont = manager
					.loadOntologyFromOntologyDocument(new ByteArrayInputStream(ontologyStr.getBytes()));
			return ont.getLogicalAxioms().iterator().next();
		} catch (OWLOntologyCreationException e) {
			LOGGER.error("Failed to parse axiom from " + axiomStr, e);
		}
		return null;
	}

	private int addGeneralizedPattern(OWLAxiom axiom){
		String axiomString = render(axiom);

		// check for existing entry
		Integer patternID = getGeneralizedPatternID(axiom);
		if(patternID != null) {
			return patternID;
		}

		// otherwise, add pattern entry
		try {
			insertPatternGeneralizationPs.setString(1, axiomString);
			insertPatternGeneralizationPs.setString(2, axiomRenderer.render(axiom));
			insertPatternGeneralizationPs.setString(3, getAxiomType(axiom));
			insertPatternGeneralizationPs.execute();
		} catch (SQLException e) {
			LOGGER.error("Failed to insert pattern. Maybe too long with a length of " + axiomString.length() + "?", e);
		}

		// get the pattern ID after insertion
		return getGeneralizedPatternID(axiom);
	}

	private Integer getGeneralizedPatternID(OWLAxiom axiom) {
		try {
			selectGeneralizedPatternIdPs.setString(1, render(axiom));
			try(ResultSet rs = selectGeneralizedPatternIdPs.executeQuery()){
				if(rs.next()){
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to get pattern ID.", e);
		}
		return null;
	}
	
	
	private int addPattern(OWLAxiom axiom){
		String axiomString = render(axiom);

		// check for existing entry
		Integer patternID = getPatternID(axiom);
		if(patternID != null) {
			return patternID;
		}

		// otherwise, add pattern entry
		try {
			insertPatternIdPs.setString(1, axiomString);
			insertPatternIdPs.setString(2, axiomRenderer.render(axiom));
			insertPatternIdPs.setString(3, getAxiomType(axiom));
			insertPatternIdPs.execute();
		} catch (SQLException e) {
			LOGGER.error("Failed to insert pattern. Maybe too long with a length of " + axiomString.length() + "?", e);
		}

		// get the pattern ID after insertion
		return getPatternID(axiom);
	}

	private Integer getPatternID(OWLAxiom axiom) {
		try {
			selectPatternIdPs.setString(1, render(axiom));
			try(ResultSet rs = selectPatternIdPs.executeQuery()) {
				if(rs.next()){
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to get pattern ID.", e);
		}
		return null;
	}

	private String getAxiomType(OWLAxiom axiom) {
		AxiomType<?> type = axiom.getAxiomType();
		String s;
		if (AxiomType.TBoxAxiomTypes.contains(type)) {
			s = "TBox";
		} else if (AxiomType.RBoxAxiomTypes.contains(type)) {
			s = "RBox";
		} else if (AxiomType.ABoxAxiomTypes.contains(type)) {
			s = "ABox";
		} else {
			System.out.println(axiom + "-" + type);
			//should not happen
			s = "Non-Logical";
		}
		return s;
	}
	
	private synchronized boolean ontologyProcessed(URI uri){
		//check if ontology was already processed
		try {
			selectOntologyIdPs.setString(1, uri.toString());
			try(ResultSet rs = selectOntologyIdPs.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private synchronized void addOntologyError(URI physicalURI, Exception ex){
		String url = physicalURI.toString();
		//add ontology loading/parsing/... error entry
		try {
			insertOntologyErrorPs.setString(1, url);
			String errorMessage = "ERROR:" + ex.getClass().getSimpleName();
			if(!(ex instanceof UnparsableOntologyException)){
				errorMessage += (ex.getMessage() != null ? ("->" + ex.getMessage()) : "");
			}
			if(errorMessage.length() > 1900){
				errorMessage = errorMessage.substring(0, 1900);
			}
			insertOntologyErrorPs.setString(2, repository.getName());
			insertOntologyErrorPs.setString(3, errorMessage);
			insertOntologyErrorPs.execute();
		} catch (SQLException e) {
			LOGGER.error("Failed to insert ontology error statement.", e);
		}
	}
	
	private synchronized int addOntology(URI physicalURI, OWLOntology ontology, Imports imports){
		String url = physicalURI.toString();
		String ontologyIRI = "Anonymous";
		if(!ontology.getOntologyID().isAnonymous()){
			ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
		}
		// check for existing entry
		Integer ontologyID = getOntologyID(physicalURI, ontology);
		if(ontologyID != null) {
			return ontologyID;
		}

		// add ontology entry
		try {
			insertOntologyPs.setString(1, url);
			insertOntologyPs.setString(2, ontologyIRI);
			insertOntologyPs.setString(3, repository.getName());
			Set<OWLAxiom> tbox = ontology.getTBoxAxioms(imports);
			Set<OWLAxiom> rbox = ontology.getRBoxAxioms(imports);
			Set<OWLAxiom> abox = ontology.getABoxAxioms(imports);
			
			insertOntologyPs.setInt(4, tbox.size() + rbox.size() + abox.size());
			insertOntologyPs.setInt(5, tbox.size());
			insertOntologyPs.setInt(6, rbox.size());
			insertOntologyPs.setInt(7, abox.size());
			insertOntologyPs.setInt(8, ontology.getClassesInSignature(Imports.INCLUDED).size());
			insertOntologyPs.setInt(9, ontology.getObjectPropertiesInSignature(Imports.INCLUDED).size());
			insertOntologyPs.setInt(10, ontology.getDataPropertiesInSignature(Imports.INCLUDED).size());
			insertOntologyPs.setInt(11, ontology.getIndividualsInSignature(Imports.INCLUDED).size());
			insertOntologyPs.execute();
		} catch (SQLException e) {
			LOGGER.error("Failed to insert ontology.", e);
		}

		// get and return the auto generated ID
		return getOntologyID(physicalURI, ontology);
	}

	private synchronized void addOntologyImport(URI physicalURI1, OWLOntology ontology1, URI physicalURI2, OWLOntology ontology2){
		// get ID of first ontology
		Integer ontologyID1 = getOntologyID(physicalURI1, ontology1);

		// get ID of second ontology
		Integer ontologyID2 = getOntologyID(physicalURI2, ontology2);

		// add ontology entry
		try {
			insertOntologyImportPs.setInt(1, ontologyID1);
			insertOntologyImportPs.setInt(2, ontologyID2);

			insertOntologyImportPs.execute();
		} catch (SQLException e) {
			LOGGER.error("Failed to insert ontology.", e);
		}
	}

	private Integer getOntologyID(OWLOntology ontology) {
		String ontologyIRI = "Anonymous";
		if(!ontology.getOntologyID().isAnonymous()){
			ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
		}
		//check for existing entry
		try {
			selectOntologyIdPs.setString(1, ontologyIRI);
			try(ResultSet rs = selectOntologyIdPs.executeQuery()) {
				if(rs.next()){
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to get ontology ID.", e);
		}
		return null;
	}

	private Integer getOntologyID(URI physicalURI, OWLOntology ontology) {
		String url = physicalURI.toString();
		String ontologyIRI = "Anonymous";
		if(!ontology.getOntologyID().isAnonymous()){
			ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
		}
		//check for existing entry
		try {
			selectOntologyIdPs.setString(1, url);
			try(ResultSet rs = selectOntologyIdPs.executeQuery()) {
				if(rs.next()){
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to get ontology ID.", e);
		}
		return null;
	}
	
	private synchronized void addOntologyPatterns(URI physicalURI, OWLOntology ontology, Multiset<OWLAxiom> patterns){
		int ontologyId = getOntologyID(physicalURI, ontology);
		for (OWLAxiom pattern : patterns.elementSet()) {
			try {
				int patternId = addPattern(pattern);
				int occurrences = patterns.count(pattern);
				insertOntologyPatternPs.setInt(1, ontologyId);
				insertOntologyPatternPs.setInt(2, patternId);
				insertOntologyPatternPs.setInt(3, occurrences);
				insertOntologyPatternPs.addBatch();
			} catch (SQLException e) {
				LOGGER.error("Failed to insert pattern\n" + pattern + "\"", e);
			}
		}
		try {
			insertOntologyPatternPs.executeBatch();
		} catch (BatchUpdateException e) {
			LOGGER.error("Failed to insert some pattern. Reason: {}", e.getMessage());
		} catch (SQLException e) {
			LOGGER.error("Failed to insert patterns.", e);
		}
	}

	public static void main(String[] args) throws Exception {
//		ManchesterOWLSyntaxOWLObjectRendererImplExt renderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt(
//				true, true);
//		ToStringRenderer.getInstance().setRenderer(renderer);
		// create Options object
		OptionParser parser = new OptionParser();

		OptionSpec<File> dir =
				parser.accepts( "dir" ).withRequiredArg().ofType( File.class ).required();

		OptionSpec<Long> maxFileSize =
				parser.accepts( "maxFileSize" ).withRequiredArg().ofType(Long.class).defaultsTo(Long.MAX_VALUE);

		OptionSpec<Boolean> multiThreadedEnabledOpt =
				parser.accepts( "multiThreadedEnabled" ).withOptionalArg().ofType(Boolean.class).defaultsTo(false);

		OptionSpec<Integer> numThreadsOpt =
				parser.accepts( "numThreads" ).availableIf(multiThreadedEnabledOpt).withRequiredArg().ofType(Integer.class).defaultsTo(4);

		parser.printHelpOn( System.out );

		OptionSet options = parser.parse(args);

		boolean multiThreadedEnabled = options.has(multiThreadedEnabledOpt) &&
				(!options.hasArgument(multiThreadedEnabledOpt) || options.valueOf(multiThreadedEnabledOpt));
		int numThreads = options.valueOf(numThreadsOpt);

		OntologyRepository repository = new LocalDirectoryOntologyRepository(options.valueOf(dir), options.valueOf(maxFileSize));
		repository.initialize();

		OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository);
		patternFinder.setMultithreadedEnabled(multiThreadedEnabled);
		patternFinder.setNumThreads(numThreads);
		patternFinder.start();
		
//		String ontologyURL = "ontologyURL";
//		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLDataFactory dataFactory = man.getOWLDataFactory();
//		OWLFunctionalDataPropertyAxiom axiom = dataFactory.getOWLFunctionalDataPropertyAxiom(dataFactory.getOWLDataProperty(IRI.create("http://ex.org/p")));
//		OWLOntology ontology = man.createOntology();
//		man.addAxiom(ontology, axiom);
//		StringWriter sw = new StringWriter();
//		FunctionalSyntaxObjectRenderer r = new FunctionalSyntaxObjectRenderer(ontology, sw);
//		axiom.accept(r);
//		System.out.println(sw.toString());
//		StringDocumentSource s = new StringDocumentSource("Ontology(<http://www.pattern.org>" + sw.toString() + ")");
//		OWLFunctionalSyntaxOWLParser p = new OWLFunctionalSyntaxOWLParser();
//		OWLOntology newOntology = man.createOntology();
//		p.parse(s, newOntology, new OWLOntologyLoaderConfiguration());
//		System.out.println(newOntology.getLogicalAxioms());

	}
}
