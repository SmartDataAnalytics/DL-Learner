package org.dllearner.reasoning;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dllearner.OntologyFileFormat;
import org.w3c.dom.Document;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.dig.DIGAdapter;

public class JenaOWLDIGConverter {

	public static long nrOfStatementsLastConversion = 0;
	
	public static void main(String[] args) {
		File file = new File("files/examples/father2.owl");
		System.out.println(file.toURI().toString());
		
		String tells = "";
		try {
			URL url = file.toURI().toURL();			
			tells = getTellsString(url, OntologyFileFormat.RDF_XML, new URI("kk"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		System.out.println(tells);
	}
	
	// returns a DIG 1.1 Tells String from an ontology file
	// using the Jena library
	public static String getTellsString(URL file, OntologyFileFormat format, URI kbURI) {
		String tellString = "";
		
	    // Spezifikation erzeugen: OWL DL
	    OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
		// OntModelSpec spec = new OntModelSpec( OntModelSpec.);
	    
	    // Datei-String in eine URI umwandeln, falls es nicht schon eine ist
	    // (TODO: dieser Codeteil ist nicht besonders schön, man sollte besser eine
	    // generische Methode aufrufen, die testet, ob etwas eine URI ist)
	    
	    String uri = file.toString();
		// String uri = "";
		/*
		if(!file.toString().startsWith("file:") && !file.toString().startsWith("http:") ) {
			try {
				uri = file.toURI().toURL().toString();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}			
		} else
			uri = file.toString();
		*/
		
		// Datei einlesen und in einen Jena-Graph umwandeln
	    // OntModel m = ModelFactory.createOntologyModel();
		Model m = spec.createBaseModel();
		String lang = "";
		if(format.equals(OntologyFileFormat.RDF_XML))
			lang = "RDF/XML";
		else
			lang = "N-TRIPLES";
		
	    m.read(uri,lang);
	    Graph g = m.getGraph();
	    // OntModelSpec spec = new OntModelSpec(m);
	    
	    /*
	    System.out.println("JENA STATEMENTS");
	    StmtIterator it = m.listStatements();
	    while(it.hasNext()) {
	    	System.out.println(it.next());
	    }
	    System.out.println(m.size());
	    */
	    
	    nrOfStatementsLastConversion = m.size();
	    
	    // DIGAdapter erzeugen und ein org.w3c.document generieren
	    DIGAdapter da = new DIGAdapter(spec, g);
	    // DIGConnection dc = new DIGConnection();
	    // DIGAdapter da = new DIGAdapter(spec, g, dc, m);
	    Document d = da.translateKbToDig();
	    
	    // XML so modifizieren, dass KB-URI eingebaut wird
	    d.getDocumentElement().setAttribute("uri", kbURI.toString());
	    
	    // XML als String erzeugen (ziemlich umständlich)
	    try {
	    	// transformer erzeugen mit identity transformation
	    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    	// Quelle ist das DOM-Objekt
	    	DOMSource source = new DOMSource( d );
	    	// Resultat ist ein OutputStream, leider kann man das Resultat nicht
	    	// direkt als String bekommen
	    	ByteArrayOutputStream os = new ByteArrayOutputStream();
	    	StreamResult result = new StreamResult( os );
	    	// Transformation ausführen
	    	transformer.transform( source, result );
	    	// String aus OutputStream generieren
	    	tellString = os.toString();

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		// System.out.println(tellString);
		
    	return tellString;
	}

}
