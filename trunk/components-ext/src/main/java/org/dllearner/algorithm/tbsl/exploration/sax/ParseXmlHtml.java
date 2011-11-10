package org.dllearner.algorithm.tbsl.exploration.sax;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ParseXmlHtml {
	
	//TODO: zweite Hashmap in der als Key (barack,Obama) auf die Value barack Obama verweißt
	//TODO: Rückgabewert in List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();  ändern und dann beide hashmaps übergeben und dann hm aus dem Funktionsheader nehmen
	
	  public static HashMap<String, String> parse_xml(String dateiname, HashMap<String, String> hm)
	  {
	    ArrayList<String> indexObject = null;
	    File file = new File(dateiname);
	    HashMap<String, String> hm_new = new HashMap<String, String>();
	    try
	    {
	      MySaxParser parser = new MySaxParser(file);
	      parser.parse();
	      indexObject = parser.getIndexObject();
	      /*for (int i = 0; i < indexObject.size(); i++)
	      {
			hm.put((indexObject.get(i+1)).toLowerCase(), indexObject.get(i));
	      }*/
	      int zaehler=0;
	      for (int i = 1; i < indexObject.size(); i=i+2)
	      {
			hm.put((indexObject.get(i)).toLowerCase(), indexObject.get(i-1));
			String[] tmp_array = indexObject.get(i).toLowerCase().split(" ");
			if(tmp_array.length>=2) {
				for(String tmp : tmp_array)hm_new.put(tmp.toLowerCase(), indexObject.get(i-1));
			}
			zaehler=zaehler+1;
	      }
	      indexObject.clear();
	      System.out.println("Anzahl: "+zaehler);

	    }
	    catch (Exception ex)
	    {
	      System.out.println("Another exciting error occured: " + ex.getLocalizedMessage());
	    }
	    return hm;
	  }
	  
	  
}
