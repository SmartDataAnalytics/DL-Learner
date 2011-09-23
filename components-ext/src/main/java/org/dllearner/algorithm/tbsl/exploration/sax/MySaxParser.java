package org.dllearner.algorithm.tbsl.exploration.sax;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
 
public class MySaxParser
{
  private URL url;
  private ArrayList<String> indexObject;
 
  public MySaxParser()
  {
    super();
  }
 
  public MySaxParser(File file) throws MalformedURLException
  {
    this.url = file.toURI().toURL();
  }
 
  public void parse() throws ParserConfigurationException, SAXException, IOException
  {
    // Initialize SAX Parser:
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    XMLReader reader = parser.getXMLReader();
    // Create SAX Handler:
    MySaxHandler handler = new MySaxHandler();
    reader.setContentHandler(handler);
    // Parse XML file:
    InputSource input = new InputSource(url.openStream());
    reader.parse(input);
    // Get the result:
    this.indexObject = handler.getIndexObject();
  }
 
  public ArrayList<String> getIndexObject()
  {
    return this.indexObject;
  }
}
