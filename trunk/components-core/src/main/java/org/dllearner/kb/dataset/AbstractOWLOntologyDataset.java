package org.dllearner.kb.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.io.Files;

public abstract class AbstractOWLOntologyDataset implements OWLOntologyDataset{
	
	protected Collection<OWLOntology> ontologies = new TreeSet<OWLOntology>();
	protected Collection<OWLOntology> correctOntologies = new TreeSet<OWLOntology>();
	protected Collection<OWLOntology> incoherentOntologies = new TreeSet<OWLOntology>();
	protected Collection<OWLOntology> inconsistentOntologies = new TreeSet<OWLOntology>();
	
	protected String name;
	
	protected File directory;
	protected File correctSubdirectory;
	protected File inconsistentSubdirectory;
	protected File incoherentSubdirectory;
	protected File errorSubdirectory;
	protected File tooLargeSubdirectory;
	
	protected OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
	OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	
	protected Map<URL, String> ontologyURLs = new HashMap<URL, String>();
	
	private final int nrOfThreads = 1;
	private boolean analyze = false;
	
	public AbstractOWLOntologyDataset(String name, boolean analyze) {
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
	
	public AbstractOWLOntologyDataset(String name) {
		this(name, false);
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
		Set<String> errors = new HashSet<String>();
		try {
			if(new File(directory, "403.txt").exists()){
				errors = new HashSet<String>(Files.readLines(new File(directory, "403.txt"), Charset.defaultCharset()));
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
			List<Entry<URL, String>> urlList = new ArrayList<java.util.Map.Entry<URL, String>>(ontologyURLs.entrySet());
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
		for(File parent : Arrays.asList(directory)){
			File file = new File(parent, filename);
			if(file.exists()){
				try {
					OWLOntology ontology = man.loadOntologyFromOntologyDocument(file);
					return ontology;
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
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				add403Error(url);
			}
		}
		return null;
	}
	
	private void add403Error(URL url){
		try {
			Files.append(url.toString() + "\n", new File(directory, "403.txt"), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
