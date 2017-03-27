package org.dllearner.kb.repository;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 */
public class LocalDirectoryOntologyRepository implements OntologyRepository{
	private static final Set<String> owlFileExtensions = Sets.newHashSet("owl", "rdf", "ttl", "nt", "xml");
	private static final PathMatcher filter = p -> owlFileExtensions.contains(
				com.google.common.io.Files.getFileExtension(p.getFileName().toString()));

	private final File directory;
	private long maxFileSizeInMB = Long.MAX_VALUE;

	public LocalDirectoryOntologyRepository(File directory){
		this.directory = directory;
	}

	public LocalDirectoryOntologyRepository(File directory, long maxFileSizeInMB){
		this.directory = directory;
		this.maxFileSizeInMB = maxFileSizeInMB;
	}

	@Override
	public String getName() {
		return "Local repository (" + directory + ")";
	}

	@Override
	public String getLocation() {
		return directory.toPath().toString();
	}

	@Override
	public void refresh() {

	}

	@Override
	public void initialize() {

	}

	@Override
	public Collection<OntologyRepositoryEntry> getEntries() {
		try {
			return Files.list(directory.toPath())
					.filter(filter::matches)
					.filter(path -> path.toFile().length() / 1024 / 1024 < maxFileSizeInMB)
					.map(Path::toFile)
					.sorted(Comparator.comparingLong(File::length).reversed())
					.map(path -> new RepositoryEntry(path.toURI()))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Object> getMetaDataKeys() {
		return null;
	}

	@Override
	public OWLOntology getOntology(OntologyRepositoryEntry entry) {
		return null;
	}

	private class RepositoryEntry implements OntologyRepositoryEntry {

		private String shortName;

		private URI ontologyURI;

		private URI physicalURI;

		public RepositoryEntry(URI ontologyIRI) {
			this.ontologyURI = ontologyIRI;
			OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
			shortName = sfp.getShortForm(IRI.create(ontologyIRI));
			physicalURI = ontologyIRI;
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
