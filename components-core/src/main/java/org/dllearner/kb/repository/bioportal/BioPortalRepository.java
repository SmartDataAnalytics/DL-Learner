package org.dllearner.kb.repository.bioportal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.KXml2Driver;

public class BioPortalRepository implements OntologyRepository {
	
	private static final Logger log = Logger.getLogger(BioPortalRepository.class);
	
	private String apiKey = "8fadfa2c-47de-4487-a1f5-b7af7378d693";
	private String serviceURL = "http://rest.bioontology.org/bioportal/ontologies";
	
	private boolean initialized = false;
	
	private List<OntologyRepositoryEntry> entries = new ArrayList<OntologyRepositoryEntry>();

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
		XStream xstream = new XStream(new KXml2Driver());
		xstream.alias("success", Success.class);
		xstream.alias("data", Data.class);
		xstream.alias("ontologyBean", OntologyBean.class);
//		xstream.alias("userAcl", UserAcl.class);
		xstream.alias("userEntry", UserEntry.class);
		
		InputStream is = null;
		try {
			is = getInputStream(new URL(withAPIKey(serviceURL)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (is == null) {
			return;
		}
		Success success = (Success) xstream.fromXML(is);
		if (success == null) {
			return;
		}
		List<OntologyBean> beans = success.getData().getOntologyBeans();
		entries = new ArrayList<OntologyRepositoryEntry>();
		for(OntologyBean bean : beans){
			URI physicalURI = URI.create(withAPIKey(serviceURL + "/download/" + bean.getId()));
			String shortName = bean.getDisplayLabel();
			boolean add = false;
			for(String filename : bean.getFilenames()){
				if(filename.endsWith(".owl") || filename.endsWith("rdf") || filename.endsWith(".obo") || filename.endsWith(".nt") || filename.endsWith("*.ttl")){
					add = true;
					break;
				}
			}
			if(add){
				entries.add(new RepositoryEntry(physicalURI, physicalURI, shortName));
			}
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
	
	private InputStream getInputStream(URL url) throws IOException {
		if (url.getProtocol().equals("http")) {
			URLConnection conn;
			conn = url.openConnection();
			conn.setRequestProperty("Accept", "application/rdf+xml");
			conn.addRequestProperty("Accept", "text/xml");
			conn.addRequestProperty("Accept", "*/*");
			return conn.getInputStream();
		} else {
			return url.openStream();
		}
	}
	
	private String withAPIKey(String url){
		return url + "?apikey=" + apiKey;
	}
	
	public static void main(String[] args) throws Exception{
		Collection<OntologyRepositoryEntry> entries = new BioPortalRepository().getEntries();
		for(OntologyRepositoryEntry entry : entries){
			System.out.println("Loading " + entry.getOntologyShortName());
			System.out.println("From " + entry.getPhysicalURI());
		}
		
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


        public String getOntologyShortName() {
            return shortName;
        }


        public URI getOntologyURI() {
            return ontologyURI;
        }


        public URI getPhysicalURI() {
            return physicalURI;
        }


        public String getMetaData(Object key) {
            return null;
        }

    }

}
