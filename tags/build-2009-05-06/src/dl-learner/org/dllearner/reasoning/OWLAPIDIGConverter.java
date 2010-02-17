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

import org.dllearner.core.OntologyFormat;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.w3c.dom.Document;

import uk.ac.manchester.cs.owl.inference.dig11.DIGReasonerException;
import uk.ac.manchester.cs.owl.inference.dig11.DIGTranslatorImpl;

public class OWLAPIDIGConverter {

	public static String getTellsString(URL file, OntologyFormat format, URI kbURI) {
		
		String ret = "";
		try {

			// Load an ontology from a physical URI
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			// the next function could return an ontology
			manager.loadOntologyFromPhysicalURI(file.toURI());

			DIGTranslatorImpl dig = new DIGTranslatorImpl(manager);
			Document doc = dig.createTellsDocument(kbURI.toString());
			dig.translateToDIG(manager.getOntologies(), doc, doc.getDocumentElement());
			ret = xml2string(doc);

		} catch (DIGReasonerException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static String xml2string(Document d) {
		// XML als String erzeugen (ziemlich umst�ndlich)
		String tellString = "";
		try {
			// transformer erzeugen mit identity transformation
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			// Quelle ist das DOM-Objekt
			DOMSource source = new DOMSource(d);
			// Resultat ist ein OutputStream, leider kann man das Resultat nicht
			// direkt als String bekommen
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			// Transformation ausf�hren
			transformer.transform(source, result);
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
}
