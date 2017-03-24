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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.KXml2Driver;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class BioPortalRepository implements OntologyRepository {
	
	private static final Logger log = Logger.getLogger(BioPortalRepository.class);
	
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
	
	public static void main(String[] args) throws Exception{
		Collection<OntologyRepositoryEntry> entries = new BioPortalRepository().getEntries();
		File dir = new File("/tmp/bioportal/");
		dir.mkdir();

		boolean parseEnabled = args.length == 1 && args[0].equals("parse");

		final Map<String, String> map = Collections.synchronizedMap(new TreeMap<>());

		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

		entries.parallelStream().forEach(entry -> {
			try {
				System.out.println("Loading " + entry.getOntologyShortName() + " from " + entry.getPhysicalURI());

				File f = new File(dir, entry.getOntologyShortName() + ".rdf");
				IOUtils.copy(getInputStream(entry.getPhysicalURI().toURL()), new FileOutputStream(f));

				// Get the number of bytes in the file
				long sizeInBytes = f.length();
				//transform in MB
				long sizeInMb = sizeInBytes / (1024 * 1024);

				System.out.println(entry.getOntologyShortName() + ": " + FileUtils.byteCountToDisplaySize(f.length()));
				map.put(entry.getOntologyShortName(), FileUtils.byteCountToDisplaySize(f.length()));

				if(parseEnabled && sizeInMb < 10) {
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
						map.replace(entry.getOntologyShortName(), map.get(entry.getOntologyShortName()) + "||#Axioms: " + ont.getLogicalAxiomCount());
						man.removeOntology(ont);
					} catch (Exception e1) {
						System.err.println("Failed to parse " + entry.getOntologyShortName());
						map.replace(entry.getOntologyShortName(), map.get(entry.getOntologyShortName()) + "||Parse Error");

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
