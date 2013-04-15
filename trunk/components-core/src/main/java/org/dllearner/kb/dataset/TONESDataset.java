package org.dllearner.kb.dataset;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.kb.repository.tones.TONESRepository;

public class TONESDataset extends AbstractOWLOntologyDataset{
	
	private static final String name = "TONES";

	public TONESDataset() {
		super(name);
	}
	
	@Override
	protected void addOntologyURLs() {
		TONESRepository tones = new TONESRepository();
		tones.initialize();
		for (OntologyRepositoryEntry entry : tones.getEntries()) {
			try {
				String name = URLEncoder.encode(entry.getOntologyShortName(), "UTF-8");
				ontologyURLs.put(entry.getPhysicalURI().toURL(), name);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	

}
