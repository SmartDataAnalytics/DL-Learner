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
package org.dllearner.kb.repository.bioportal;

import com.google.common.base.Charsets;
import com.google.common.base.StandardSystemProperty;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.utilities.Files;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class BioPortalRepository implements OntologyRepository {
	
	private static final Logger log = LoggerFactory.getLogger(BioPortalRepository.class);
	
	private static final String apiKey = "20caf25c-f140-4fef-be68-ff1a3936f405";
	private static final String serviceURL = "http://rest.bioontology.org/bioportal/ontologies";

	private static final String listOntologiesURL = "http://data.bioontology.org/ontologies";
	private static final String downloadURL = "http://data.bioontology.org/ontologies/%s/download";
	
	private boolean initialized = false;
	
	private List<OntologyRepositoryEntry> entries = new ArrayList<>();

	@Override
	public String getName() {
		return "BioPortal";
	}

	@Override
	public String getLocation() {
		return "http://www.bioontology.org/";
	}
	
	@Override
	public void initialize() {
		refresh();
		initialized = true;
	}

	@Override
	public void refresh() {
		fillRepository();
	}
	
	private void fillRepository(){

		try {
			HttpURLConnection conn = (HttpURLConnection)new URL(listOntologiesURL).openConnection();
			conn.setRequestProperty("Authorization", "apikey token=" + apiKey);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			// list all ontologies
			try (InputStream is = conn.getInputStream()) {

//				JsonParser jp = new JsonParser();
//				jp.parse(new InputStreamReader(is));


				JsonReader rdr = Json.createReader(is);
				JsonArray array = rdr.readArray();

				// for each ontology get the download link
				for (JsonObject obj : array.getValuesAs(JsonObject.class)) {
					String acronym = obj.getString("acronym");
					String name = obj.getString("name");

					URI physicalURI = URI.create(obj.getJsonObject("links").getString("download"));
					String shortName = acronym;
					boolean add = false;
					entries.add(new RepositoryEntry(physicalURI, physicalURI, shortName));
				}
			} catch( Exception e){
				e.printStackTrace();
			}
		} catch(Exception e) {

		}

		log.info("Loaded " + entries.size() + " ontology entries from BioPortal.");
	}

	@Override
	public Collection<OntologyRepositoryEntry> getEntries() {
		if(!initialized){
			initialize();
		}
		return entries;
	}

	@Override
	public List<Object> getMetaDataKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static InputStream getInputStream(URL url) throws IOException {
		if (url.getProtocol().equals("http")) {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Authorization", "apikey token=" + apiKey);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/rdf+xml");
			conn.addRequestProperty("Accept", "text/xml");
			conn.addRequestProperty("Accept", "*/*");
			return conn.getInputStream();
		} else {
			return url.openStream();
		}
	}

	/**
	 * Download the ontologies and save them in the given directory.
	 * @param dir the directory
	 */
	public void download(File dir) {

	}

	/**
	 * Returns the ontology for the entry.
	 * @param entry the entry
	 * @return the OWL ontology
	 */
	public OWLOntology getOntology(OntologyRepositoryEntry entry) {
		try(InputStream is = getInputStream(entry.getPhysicalURI().toURL())) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			man.addMissingImportListener(e -> {
				log.warn("Missing import: " + e.getImportedOntologyURI());
			});

			// handle missing imports
			OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration();
			conf.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
			// the List ontology isn't online anymore, thus, we ignore it'S import
			conf.addIgnoredImport(IRI.create("http://www.co-ode.org/ontologies/lists/2008/09/11/list.owl"));
			man.setOntologyLoaderConfiguration(conf);
			OWLOntology ont = man.loadOntologyFromOntologyDocument(is);
			return ont;
		} catch (Exception e) {
			log.error("Failed to load ontology from " + entry.getPhysicalURI(), e);
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception{

		// create Options object
		OptionParser parser = new OptionParser();
		OptionSpec<File> baseDir =
				parser.accepts( "basedir" )
						.withRequiredArg().ofType( File.class )
						.defaultsTo(new File(System.getProperty("java.io.tmpdir") + File.separator + "bioportal" + File.separator));
		OptionSpec<Void> downloadOption =
				parser.accepts( "download" );
		OptionSpec<Void> parseOption =
				parser.accepts( "parse" );

		OptionSet options = parser.parse(args);

		File dir = options.valueOf(baseDir);
		dir.mkdirs();

		File downloadDir = new File(dir, "download");
		File downloadSuccessfulDir = new File(downloadDir, "successful");
		File downloadFailedDir = new File(downloadDir, "failed");
		downloadSuccessfulDir.mkdirs();
		downloadFailedDir.mkdirs();
		File parsedDir = new File(dir, "parsed");
		File parsedSuccessfulDir = new File(parsedDir, "successful");
		File parsedFailedDir = new File(parsedDir, "failed");
		parsedSuccessfulDir.mkdirs();
		parsedFailedDir.mkdirs();

		BioPortalRepository repo = new BioPortalRepository();
		repo.initialize();

		Collection<OntologyRepositoryEntry> entries = repo.getEntries();
		System.out.println("BioPortal repository size: " + entries.size());

		boolean downloadEnabled = options.has(downloadOption);
		boolean parseEnabled = options.has(parseOption);

		final Map<String, String> map = Collections.synchronizedMap(new TreeMap<>());

		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

		System.out.println("download dir is " + downloadDir);

		entries.parallelStream().forEach(entry -> {
			try {

				File f = null;
				long sizeInMb = 101;
				if(downloadEnabled && !new File(downloadSuccessfulDir, entry.getOntologyShortName() + ".rdf").exists()) {


					System.out.println("Loading " + entry.getOntologyShortName() + " from " + entry.getPhysicalURI());

					try(InputStream is = getInputStream(entry.getPhysicalURI().toURL())) {
						f = new File(downloadSuccessfulDir, entry.getOntologyShortName() + ".rdf");

						IOUtils.copy(is, new FileOutputStream(f));

						sizeInMb = f.length() / (1024 * 1024);

						System.out.println(entry.getOntologyShortName() + ": " + FileUtils.byteCountToDisplaySize(f.length()));
						map.put(entry.getOntologyShortName(), FileUtils.byteCountToDisplaySize(f.length()));
					} catch (Exception e) {
						com.google.common.io.Files.asCharSink(new File(downloadFailedDir, entry.getOntologyShortName() + ".txt"),
								Charsets.UTF_8).write(ExceptionUtils.getMessage(e));
						return;
					}
				}

				if(f == null) {
					System.out.println("Loading " + entry.getOntologyShortName() + " from disk");

					f = new File(downloadSuccessfulDir, entry.getOntologyShortName() + ".rdf");

					System.out.println(entry.getOntologyShortName() + ": " + FileUtils.byteCountToDisplaySize(f.length()));

					sizeInMb = f.length() / (1024 * 1024);
				}

				if(f.exists() && parseEnabled && sizeInMb < 100) {
					try {
						OWLOntologyManager man = OWLManager.createOWLOntologyManager();
						man.addMissingImportListener(e -> {
							System.out.println("Missing import: " + e.getImportedOntologyURI());
						});
						OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration();
						conf.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
						conf.addIgnoredImport(IRI.create("http://www.co-ode.org/ontologies/lists/2008/09/11/list.owl"));
						man.setOntologyLoaderConfiguration(conf);
						OWLOntology ont = man.loadOntologyFromOntologyDocument(f);
						System.out.println("#Axioms: " + ont.getLogicalAxiomCount());

						com.google.common.io.Files.asCharSink(
								new File(parsedSuccessfulDir, entry.getOntologyShortName() + ".txt"),
								Charsets.UTF_8).write(
								ont.getLogicalAxiomCount() + "\t" +
										ont.getClassesInSignature().size() + "\t" +
										ont.getObjectPropertiesInSignature().size() + "\t" +
										ont.getDataPropertiesInSignature().size() + "\t" +
										ont.getIndividualsInSignature().size()
						);

						map.replace(entry.getOntologyShortName(), map.get(entry.getOntologyShortName()) + "||#Axioms: " + ont.getLogicalAxiomCount());
						man.removeOntology(ont);
					} catch (Exception e1) {
						System.err.println("Failed to parse " + entry.getOntologyShortName());
						map.replace(entry.getOntologyShortName(), map.get(entry.getOntologyShortName()) + "||Parse Error");
						com.google.common.io.Files.asCharSink(
								new File(parsedFailedDir, entry.getOntologyShortName() + ".txt"),
								Charsets.UTF_8).write(ExceptionUtils.getMessage(e1));
					}
				}
			} catch (Exception e) {
				System.err.println("Failed to load "  + entry.getOntologyShortName() + ". Reason: " + e.getMessage());
//				e.printStackTrace();
				map.put(entry.getOntologyShortName(), "Load error");
			}
		});

		map.forEach((k, v) -> System.out.println(k + " -> " + v));
	}
	
	private class RepositoryEntry implements OntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI physicalURI;

        public RepositoryEntry(URI ontologyURI, URI physicalURI, String shortName) {
            this.ontologyURI = ontologyURI;
            this.physicalURI = physicalURI;
            this.shortName = shortName;
        }

        @Override
        public String getOntologyShortName() {
            return shortName;
        }

        @Override
        public URI getOntologyURI() {
            return ontologyURI;
        }

        @Override
        public URI getPhysicalURI() {
            return physicalURI;
        }

        @Override
        public String getMetaData(Object key) {
            return null;
        }

    }

}
