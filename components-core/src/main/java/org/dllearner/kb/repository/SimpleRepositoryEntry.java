package org.dllearner.kb.repository;

import java.net.URI;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

public class SimpleRepositoryEntry implements OntologyRepositoryEntry {
		private String shortName;
		private URI ontologyURI;
		private URI physicalURI;

		private static OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();

		public SimpleRepositoryEntry(URI ontologyURI, URI physicalURI, String shortName) {
			this.ontologyURI = ontologyURI;
			this.physicalURI = physicalURI;
			this.shortName = shortName;
		}

		public SimpleRepositoryEntry(URI physicalURI) {
			this(physicalURI, physicalURI, sfp.getShortForm(IRI.create(physicalURI)));
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