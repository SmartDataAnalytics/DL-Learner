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
package org.dllearner.kb.dataset;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.io.Files;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractOWLOntologyDataset implements AnalyzedOWLOntologyDataset{
	
	protected Collection<OWLOntology> ontologies = new TreeSet<>();
	protected Collection<OWLOntology> correctOntologies = new TreeSet<>();
	protected Collection<OWLOntology> incoherentOntologies = new TreeSet<>();
	protected Collection<OWLOntology> inconsistentOntologies = new TreeSet<>();
	
	protected String name;
	
	protected File directory;
	protected File correctSubdirectory;
	protected File inconsistentSubdirectory;
	protected File incoherentSubdirectory;
	protected File errorSubdirectory;
	protected File tooLargeSubdirectory;
	
	protected OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
	OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	
	protected Map<URL, String> ontologyURLs = new HashMap<>();
	
	private static final int nrOfThreads = 1;
	private boolean analyze = false;
	
	public AbstractOWLOntologyDataset(File datasetDirectory, String name, boolean analyze) {
		this.name = name;
		this.analyze = analyze;
		//create file structure
		directory = new File(datasetDirectory, name);
		directory.mkdirs();
		correctSubdirectory = new File(directory, "correct");
		correctSubdirectory.mkdirs();
		incoherentSubdirectory = new File(directory, "incoherent");
		incoherentSubdirectory.mkdirs();
		inconsistentSubdirectory = new File(directory, "inconsistent");
		inconsistentSubdirectory.mkdirs();
		tooLargeSubdirectory = new File(directory, "too_large");
		tooLargeSubdirectory.mkdirs();
		errorSubdirectory = new File(directory, "error");
		errorSubdirectory.mkdirs();
		addOntologyURLs();
		initialize();
	}
	
	public AbstractOWLOntologyDataset(File datasetDirectory, String name) {
		this(datasetDirectory, name, false);
	}
	
	private boolean analyzed(URL url){
		String filename = getFilename(url);
		for(File parent : Arrays.asList(tooLargeSubdirectory, correctSubdirectory, incoherentSubdirectory, inconsistentSubdirectory, errorSubdirectory)){
			File file = new File(parent, filename);
			if(file.exists()){
				return true;
			}
		}
		return false;
	}
	
	private Set<String> load403Errors(){
		Set<String> errors = new HashSet<>();
		try {
			if(new File(directory, "403.txt").exists()){
				errors = new HashSet<>(Files.readLines(new File(directory, "403.txt"), Charset.defaultCharset()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return errors;
	}
	
	private boolean analyzedDataset(){
		return new File(directory + "/" + "analyzed").exists();
	}

	public void initialize(){
		//check if dataset was already analyzed
		boolean analyzed = analyzedDataset();
		if(!analyzed){
			Set<String> errors = load403Errors();
			ExecutorService threadPool = Executors.newFixedThreadPool(nrOfThreads);
			List<Entry<URL, String>> urlList = new ArrayList<>(ontologyURLs.entrySet());
			Collections.shuffle(urlList);
			for (java.util.Map.Entry<URL, String> entry : urlList) {
				URL url = entry.getKey();
				if(!errors.contains(url.toString()) && !analyzed(url)){
					threadPool.submit(new OntologyLoadingTask(url));
				}
			}
			threadPool.shutdown();
			try {
				threadPool.awaitTermination(100, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				new File(directory + "/" + "analyzed").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			
		}
	}
	
	protected abstract void addOntologyURLs();
	
	private void analyzeAndCategorizeOntology(OWLOntology ontology, String filename){
		System.out.println("Analyzing ontology " + filename + "...");
		OWLReasoner reasoner;
		File from = new File(man.getOntologyDocumentIRI(ontology).toURI());
		try {
			Configuration conf = new Configuration();
			conf.reasonerProgressMonitor = new ConsoleProgressMonitor();
			conf.ignoreUnsupportedDatatypes = true;
			reasoner = new Reasoner(conf, ontology);
			int logicalAxiomCount = ontology.getLogicalAxiomCount();
			boolean consistent = reasoner.isConsistent();
			Set<OWLClass> unsatisfiableClasses = null;
			
			if(consistent){
				unsatisfiableClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
				if(!unsatisfiableClasses.isEmpty()){
					File to = new File(incoherentSubdirectory, filename);
					Files.move(from, to);
				} else {
					File to = new File(correctSubdirectory, filename);
					Files.move(from, to);
				}
			} else {
				File to = new File(inconsistentSubdirectory, filename);
				Files.move(from, to);
			}
			System.out.println(consistent + "\t" + logicalAxiomCount + "\t" + ((unsatisfiableClasses != null) ? unsatisfiableClasses.size() : "n/a"));
			reasoner.dispose();
		} catch (Exception e){
			e.printStackTrace();
			try {
				reasoner = reasonerFactory.createNonBufferingReasoner(ontology, new SimpleConfiguration(new ConsoleProgressMonitor()));
				int logicalAxiomCount = ontology.getLogicalAxiomCount();
				boolean consistent = reasoner.isConsistent();
				Set<OWLClass> unsatisfiableClasses = null;
				if(consistent){
					unsatisfiableClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
					if(!unsatisfiableClasses.isEmpty()){
						File to = new File(incoherentSubdirectory, filename);
						Files.move(from, to);
					} else {
						File to = new File(correctSubdirectory, filename);
						Files.move(from, to);
					}
				} else {
					File to = new File(inconsistentSubdirectory, filename);
					Files.move(from, to);
				}
				System.out.println(consistent + "\t" + logicalAxiomCount + "\t" + ((unsatisfiableClasses != null) ? unsatisfiableClasses.size() : "n/a"));
				reasoner.dispose();
			} catch (Exception e1){
				File to = new File(errorSubdirectory, filename);
				try {
					Files.move(from, to);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	
	protected OWLOntology loadOWLOntology(URL url) {
		OWLOntology ontology = loadFromLocal(url);
		if(ontology == null){
			File file = null;
			try {
				file = downloadFile(url);
				if(file != null){
					ontology = man.loadOntologyFromOntologyDocument(file);
				}
			} catch (Exception e) {
				e.printStackTrace();
				String filename = getFilename(url);
				File to = new File(errorSubdirectory, filename);
				try {
					Files.move(file, to);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
		return ontology;
	}
	
	private OWLOntology loadFromLocal(URL url){
		String filename = getFilename(url);
		for(File parent : Collections.singletonList(directory)){
			File file = new File(parent, filename);
			if(file.exists()){
				try {
					return man.loadOntologyFromOntologyDocument(file);
				} catch(Exception e){
					e.printStackTrace();
					File to = new File(errorSubdirectory, filename);
					try {
						Files.move(file, to);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	private String getFilename(URL url){
		return ontologyURLs.get(url);
//		String filename = url.toString().substring(url.toString().lastIndexOf("/"));
//		return filename;
	}
	
	/**
	 * Download the file such that later on we can load it from the local file system.
	 */
	protected File downloadFile(URL url){
		
		String filename = getFilename(url);
		File file = new File(directory + "/" + filename);
		if(!file.exists()){
			System.out.print("Downloading file...");
			try {
				InputStream is = url.openConnection().getInputStream();
				OutputStream out = new FileOutputStream(file);
				int read = 0;
				byte[] bytes = new byte[1024];
 
				while ((read = is.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
 
				is.close();
				out.flush();
				out.close();
				System.out.println("done.");
				return file;
			} catch (MalformedURLException | FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				add403Error(url);
			}
		}
		return null;
	}
	
	private void add403Error(URL url){
		org.dllearner.utilities.Files.appendToFile(new File(directory, "403.txt"), url.toString() + "\n");
	}
	
	@Override
	public Collection<OWLOntology> loadOntologies() {
		return ontologies;
	}

	@Override
	public Collection<OWLOntology> loadIncoherentOntologies() {
		return incoherentOntologies;
	}

	@Override
	public Collection<OWLOntology> loadInconsistentOntologies() {
		return inconsistentOntologies;
	}
	
	class OntologyLoadingTask implements Runnable{
		
		private URL url;

		public OntologyLoadingTask(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			System.out.println("Processing " + ontologyURLs.get(url));
			OWLOntology ontology = loadOWLOntology(url);
			if(ontology != null){
				if(analyze){
					analyzeAndCategorizeOntology(ontology, getFilename(url));
				} else {
					ontologies.add(ontology);
				}
			}
		}
		
	}

}
