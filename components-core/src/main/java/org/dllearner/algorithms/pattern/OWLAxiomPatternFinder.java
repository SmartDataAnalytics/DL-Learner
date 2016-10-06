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
import org.dllearner.kb.dataset.OWLOntologyDataset;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.ini4j.IniPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParser;
import org.semanticweb.owlapi.functional.renderer.FunctionalSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

public class OWLAxiomPatternFinder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OWLAxiomPatternFinder.class);
	
	private static Queue<String> classVarQueue = new LinkedList<>();
	private static Queue<String> propertyVarQueue = new LinkedList<>();
	private static Queue<String> individualVarQueue = new LinkedList<>();
	private static Queue<String> datatypeVarQueue = new LinkedList<>();
	
	static{
		for(int i = 65; i <= 90; i++){
			classVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 97; i <= 111; i++){
			individualVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 112; i <= 122; i++){
			propertyVarQueue.add(String.valueOf((char)i));
		}
		
	}

	private OntologyRepository repository;
	private OWLOntologyManager manager;
	private OWLDataFactory dataFactory;
	
	private Connection conn;
	private PreparedStatement selectOntologyIdPs;
	private PreparedStatement insertOntologyPs;
	private PreparedStatement insertOntologyErrorPs;
	private PreparedStatement selectPatternIdPs;
	private PreparedStatement insertPatternIdPs;
	private PreparedStatement insertOntologyPatternPs;
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();
	
	private boolean randomOrder = false;

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
		final ExecutorService tp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

		Collection<OntologyRepositoryEntry> entries = repository.getEntries();
		if(randomOrder){
			List<OntologyRepositoryEntry> entryList = new ArrayList<>(repository.getEntries());
			Collections.shuffle(entryList);
			entries = entryList;
		}

		final Multiset<OWLAxiom> allAxiomPatterns = HashMultiset.create();

		AtomicInteger i = new AtomicInteger(1);

		manager = OWLManager.createConcurrentOWLOntologyManager();

		for (OntologyRepositoryEntry entry : entries) {
			tp.execute(new Runnable() {
				@Override
				public void run() {
					OWLAxiomRenamer renamer = new OWLAxiomRenamer(dataFactory);

					System.out.print(i.incrementAndGet() + ": ");
					URI uri = entry.getPhysicalURI();
//					if(uri.toString().startsWith("http://rest.bioontology.org/bioportal/ontologies/download/42764")){
					if (!ontologyProcessed(uri)) {//if(entry.getOntologyShortName().equals("00698"))continue;
						LOGGER.info("Loading \"" + entry.getOntologyShortName() + "\" from " + uri);
						try {

							OWLOntology ontology = manager.loadOntology(IRI.create(uri));
							Multiset<OWLAxiom> axiomPatterns = HashMultiset.create();
							Set<OWLAxiom> logicalAxioms = new HashSet<>();
							for (AxiomType<?> type : AxiomType.AXIOM_TYPES) {
								if (type.isLogical()) {
									logicalAxioms.addAll(ontology.getAxioms(type, Imports.INCLUDED));
								}
							}
							LOGGER.info(" (" + logicalAxioms.size() + " axioms)");
							for (OWLAxiom axiom : logicalAxioms) {
								OWLAxiom renamedAxiom = renamer.rename(axiom);
								axiomPatterns.add(renamedAxiom);
							}
//							allAxiomPatterns.addAll(axiomPatterns);
							addOntologyPatterns(uri, ontology, axiomPatterns);
//							for (OWLAxiom owlAxiom : Multisets.copyHighestCountFirst(allAxiomPatterns).elementSet()) {
//								System.out.println(owlAxiom + ": " + allAxiomPatterns.count(owlAxiom));
//							}
							manager.removeOntology(ontology);
						} catch (OWLOntologyAlreadyExistsException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
							addOntologyError(uri, e);
						}
					} else {
						LOGGER.info("Already processed.");
					}
				}
			});
		}

		tp.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!tp.awaitTermination(60, TimeUnit.MINUTES)) {
				tp.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!tp.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			tp.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	private void prepare(){
		createTables();
		try {
			selectOntologyIdPs = conn.prepareStatement("SELECT id FROM Ontology WHERE url=?");
			insertOntologyPs = conn.prepareStatement("INSERT INTO Ontology (url, iri, repository, logical_axioms, tbox_axioms, rbox_axioms" +
					", abox_axioms, classes, object_properties, data_properties, individuals) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
			insertOntologyErrorPs = conn.prepareStatement("INSERT INTO Ontology (url, iri, repository) VALUES(?,?,?)");
			selectPatternIdPs = conn.prepareStatement("SELECT id FROM Pattern WHERE pattern=?");
			insertPatternIdPs = conn.prepareStatement("INSERT INTO Pattern (pattern,pattern_pretty,axiom_type) VALUES(?,?,?)");
			insertOntologyPatternPs = conn.prepareStatement("INSERT INTO Ontology_Pattern (ontology_id, pattern_id, occurrences) VALUES(?,?,?)");
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
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/dllearner/algorithms/pattern/db_settings.ini");
			Preferences prefs = new IniPreferences(is);
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);
		} catch (ClassNotFoundException | IOException | SQLException  e) {
			e.printStackTrace();
		}
	}
	
	private void createTables(){
		try {
			Statement statement = conn.createStatement();
			
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
			
			statement.execute("CREATE TABLE IF NOT EXISTS Ontology_Pattern (" 
			        + "ontology_id MEDIUMINT NOT NULL,"
					+ "pattern_id MEDIUMINT NOT NULL,"
					+ "occurrences INTEGER(8) NOT NULL,"
					+ "FOREIGN KEY (ontology_id) REFERENCES Ontology(id) ON DELETE CASCADE,"
					+ "FOREIGN KEY (pattern_id) REFERENCES Pattern(id) ON DELETE CASCADE,"
					+ "PRIMARY KEY(ontology_id, pattern_id)) DEFAULT CHARSET=utf8");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private int addPattern(OWLAxiom axiom){
		String axiomString = render(axiom);
		//check for existing entry
		try {
			selectPatternIdPs.setString(1, axiomString);
			ResultSet rs = selectPatternIdPs.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//add pattern entry
		try {
			insertPatternIdPs.setString(1, axiomString);
			insertPatternIdPs.setString(2, axiomRenderer.render(axiom));
			insertPatternIdPs.setString(3, getAxiomType(axiom));
			insertPatternIdPs.execute();
		} catch (SQLException e) {
			System.err.println("Pattern too long for database?" + axiomString.length());
			e.printStackTrace();
		}
		//get the auto generated ID
		try {
			selectPatternIdPs.setString(1, axiomString);
			ResultSet rs = selectPatternIdPs.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private String getAxiomType(OWLAxiom axiom){
		AxiomType<?> type = axiom.getAxiomType();
		String s;
		if(AxiomType.TBoxAxiomTypes.contains(type)){
			s = "TBox";
		} else if(AxiomType.RBoxAxiomTypes.contains(type)){
			s = "RBox";
		} else if(AxiomType.ABoxAxiomTypes.contains(type)){
			s = "ABox";
		} else {System.out.println(axiom + "-" + type);
			//should not happen
			s="Non-Logical";
		}
		return s;
	}
	
	private synchronized boolean ontologyProcessed(URI uri){
		//check if ontology was already processed
		try {
			selectOntologyIdPs.setString(1, uri.toString());
			ResultSet rs = selectOntologyIdPs.executeQuery();
			return rs.next();
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
			insertOntologyErrorPs.setString(2, errorMessage);
			insertOntologyErrorPs.setString(3, repository.getName());
			insertOntologyErrorPs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized int addOntology(URI physicalURI, OWLOntology ontology){
		String url = physicalURI.toString();
		String ontologyIRI = "Anonymous";
		if(!ontology.getOntologyID().isAnonymous()){
			ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
		}
		//check for existing entry
		try {
			selectOntologyIdPs.setString(1, url);
			ResultSet rs = selectOntologyIdPs.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//add ontology entry
		try {
			insertOntologyPs.setString(1, url);
			insertOntologyPs.setString(2, ontologyIRI);
			insertOntologyPs.setString(3, repository.getName());
			Set<OWLAxiom> tbox = ontology.getTBoxAxioms(Imports.INCLUDED);
			Set<OWLAxiom> rbox = ontology.getRBoxAxioms(Imports.INCLUDED);
			Set<OWLAxiom> abox = ontology.getABoxAxioms(Imports.INCLUDED);
			
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
			e.printStackTrace();
		}
		//get the auto generated ID
		try {
			selectOntologyIdPs.setString(1, url);
			ResultSet rs = selectOntologyIdPs.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private synchronized void addOntologyPatterns(URI physicalURI, OWLOntology ontology, Multiset<OWLAxiom> patterns){
		int ontologyId = addOntology(physicalURI, ontology);
		for (OWLAxiom pattern : patterns.elementSet()) {
			try {
				int patternId = addPattern(pattern);
				int occurrences = patterns.count(pattern);
				insertOntologyPatternPs.setInt(1, ontologyId);
				insertOntologyPatternPs.setInt(2, patternId);
				insertOntologyPatternPs.setInt(3, occurrences);
				insertOntologyPatternPs.execute();
			} catch (SQLException e) {
				System.err.println("Adding pattern\n" + pattern + "\nfailed." + e.getMessage());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		
		String ontologyURL = "ontologyURL";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = man.getOWLDataFactory();
		OWLFunctionalDataPropertyAxiom axiom = dataFactory.getOWLFunctionalDataPropertyAxiom(dataFactory.getOWLDataProperty(IRI.create("http://ex.org/p")));
		OWLOntology ontology = man.createOntology();
		man.addAxiom(ontology, axiom);
		StringWriter sw = new StringWriter();
		FunctionalSyntaxObjectRenderer r = new FunctionalSyntaxObjectRenderer(ontology, sw);
		axiom.accept(r);
		System.out.println(sw.toString());
		StringDocumentSource s = new StringDocumentSource("Ontology(<http://www.pattern.org>" + sw.toString() + ")");
		OWLFunctionalSyntaxOWLParser p = new OWLFunctionalSyntaxOWLParser();
		OWLOntology newOntology = man.createOntology();
		p.parse(s, newOntology, new OWLOntologyLoaderConfiguration());
		System.out.println(newOntology.getLogicalAxioms());
		
	}
}
