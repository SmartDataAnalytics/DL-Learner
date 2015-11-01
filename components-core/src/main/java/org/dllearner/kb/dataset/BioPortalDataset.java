package org.dllearner.kb.dataset;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;

public class BioPortalDataset extends AbstractOWLOntologyDataset{
	
	private static final String name = "BioPortal";
	
	public BioPortalDataset() {
		super(name);
	}

	@Override
	protected void addOntologyURLs() {
		BioPortalRepository bioportal = new BioPortalRepository();
		bioportal.initialize();
		for (OntologyRepositoryEntry entry : bioportal.getEntries()) {
			try {
				String name = URLEncoder.encode(entry.getOntologyShortName(), "UTF-8");
				super.ontologyURLs.put(entry.getPhysicalURI().toURL(), name);
			} catch (MalformedURLException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

}
