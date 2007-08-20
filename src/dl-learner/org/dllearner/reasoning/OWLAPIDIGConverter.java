package org.dllearner.reasoning;

import java.io.ByteArrayOutputStream;
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
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.w3c.dom.Document;

import uk.ac.manchester.cs.owl.inference.dig11.DIGReasonerException;
import uk.ac.manchester.cs.owl.inference.dig11.DIGTranslatorImpl;

public class OWLAPIDIGConverter {

	public static String getTellsString(URL file, OntologyFileFormat format, URI kbURI) {
	 // public static String getTellsString(URL file,  URI kbURI){//throws OWLOntologyCreationException{
		 String ret="";
		 try{
		 
		 /* Load an ontology from a physical URI */
		 OWLOntologyManager manager = OWLManager.createOWLOntologyManager();   
		 OWLOntology ontology = manager.loadOntologyFromPhysicalURI(file.toURI());
	 
		 DIGTranslatorImpl dig=new DIGTranslatorImpl(manager);
	     Document doc=dig.createTellsDocument(kbURI.toString());
	     dig.translateToDIG(manager.getOntologies(), doc, doc.getDocumentElement() );
	     ret=xml2string(doc);
		// }catch (Exception e) {e.printStackTrace();}
	 }catch (DIGReasonerException e) {e.printStackTrace();}
	 catch (OWLOntologyCreationException e) {e.printStackTrace();}
	 catch (URISyntaxException e) {e.printStackTrace();}
	 
	 	// System.out.println(ret);
	 	// System.exit(0);
	 
	     return ret;
	 }
	
	 
	 //for convenience
	 /*
	 public static String getTellsString(String file,  String kbURI){
		 // String ret="";
		 //try{
		 // URL u=new URL(file);
	     // URI ui=new URI(kbURI);
	     return getTellsString(file, kbURI);
		 //}catch (MalformedURLException e) {e.printStackTrace();}
		 //catch (URISyntaxException e) {e.printStackTrace();}
		
	      // return ret;
	 }*/
	
	public static String xml2string(Document d){
		  // XML als String erzeugen (ziemlich umst�ndlich)
	    String tellString="";
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
	    	// Transformation ausf�hren
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
		return tellString;
	}

	
	
/*public static void main(String[] args) {
        
		try{
			
		 String inputOntology = "http://localhost/dllearner/ontologies/arch.owl";
		 //String inputOntology  = "http://localhost/dllearner/ontologies/ttttt.rdf";
	
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
         URL res = new URL("http://localhost:8081");
         HTTPReasonerImpl h=new HTTPReasonerImpl(manager);
        h.setReasonerURL(res);
       URL u=new URL(inputOntology);
        String kb=(h.createKnowledgeBase()); 
        URI ui=new URI(kb);
        System.out.println(getTellsString(u, ui));
        
        System.out.println("Done");
        
    } catch (Exception e) {
        e.printStackTrace();
    }

	}*/
}

