package org.dllearner.algorithm.tbsl.exploration.sax;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ParseXmlHtml {
	  public static HashMap<String, String> parse_xml(String dateiname, HashMap<String, String> hm)
	  {
	    ArrayList<String> indexObject = null;
	    File file = new File(dateiname);
	    try
	    {
	      MySaxParser parser = new MySaxParser(file);
	      parser.parse();
	      indexObject = parser.getIndexObject();
	      /*for (int i = 0; i < indexObject.size(); i++)
	      {
			hm.put((indexObject.get(i+1)).toLowerCase(), indexObject.get(i));
	      }*/
	      for (int i = 1; i < indexObject.size(); i=i+2)
	      {
			hm.put((indexObject.get(i)).toLowerCase(), indexObject.get(i-1));
	      }
	      indexObject.clear();

	    }
	    catch (Exception ex)
	    {
	      System.out.println("Another exciting error occured: " + ex.getLocalizedMessage());
	    }
	    return hm;
	  }
	  
	  
}
