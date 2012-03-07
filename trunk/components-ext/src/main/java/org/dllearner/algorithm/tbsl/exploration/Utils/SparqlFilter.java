package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.util.HashMap;

public class SparqlFilter {
	public void create_Sparql_who(String string,HashMap<String, String> hm){
		// string=string.replaceAll("?", "");
		 String[] array= string.split(" ");
		 //schauen ob erstes Wort ein who ist!
		 if(array[0].contains("who")){
			 int position=0;
			 for(int i=0;i<array.length;i++){
				 if (array[i].contains("of")){
					 position=i;
					 break;
				 }
			 }
			 String vor_of=array[position-1];
			 String nach_of="";
			 //wenn nur ein element hinter of kommt
			 if(array.length-position-1==1){
				 nach_of=array[position+1];
			 }
			 else{
				 for(int i=position+1; i<array.length;i++){
					 //nach_of=nach_of+array[i]+" ";
					 nach_of=(nach_of.concat(array[i])).concat(" ");
				 }
				 
				 //letztes leerzeichen loeschen
				 nach_of = nach_of.substring(0, nach_of.length()-1);
			 }
			 String uri_vor_of=" ";
			 String uri_nach_of=" ";
			 
			 uri_vor_of=hm.get(vor_of);
			 uri_nach_of=hm.get(nach_of);
			 if(uri_vor_of!=null && uri_nach_of!=null){
				 uri_nach_of=uri_nach_of.replace("Category:", "");
				 uri_nach_of=uri_nach_of.replace("category:", "");

				 
				 String anfrage=null;
				 anfrage="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>select ?x  where { <"+uri_nach_of+"> <"+uri_vor_of+"> ?x.}";

			 }
			 else{
				 //System.out.println("Nothing to do");
			 }
			 
		 }
		 
	 }
}
