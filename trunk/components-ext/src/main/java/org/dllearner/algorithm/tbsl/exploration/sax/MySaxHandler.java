package org.dllearner.algorithm.tbsl.exploration.sax;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class MySaxHandler extends DefaultHandler
{
 
  private StringBuffer buffer;
  private boolean buffering;
  private ArrayList<String> indexObject;
 
  public MySaxHandler()
  {
    this.buffer = null;
    this.buffering = false;
    indexObject = new ArrayList<String>();
  }
 
  @Override
  public void startDocument() throws SAXException
  {
    this.buffer = new StringBuffer("");
  }
 
  @Override
  public void startElement(String namespaceURI, String localName, String tagName, Attributes attributes) throws SAXException
  {
    String tag = tagName;
    //name=td
    if (tag.equals("td"))
    {
      this.buffering = true;
    }
  }
 
  @Override
  public void endElement(String namespaceURI, String localName, String tagName) throws SAXException
  {
    String tag = tagName;
    String tagValue = null;
 
    //name=td
    if (tag.equals("td"))
    {
      tagValue = this.buffer.toString();
      this.buffering = false;
      this.buffer = new StringBuffer();
    }
 
    parseValue(tagValue);
  }
 
  @Override
  public void characters(char chars[], int start, int length)
  {
    if (this.buffering)
    {
      this.buffer = this.buffer.append(chars, start, length);
    }
  }
 
  private void parseValue(String value)
  {
    if (value != null)
    {
      this.indexObject.add(value);
    }
  }
 
  public ArrayList<String> getIndexObject()
  {
    return this.indexObject;
  }
}