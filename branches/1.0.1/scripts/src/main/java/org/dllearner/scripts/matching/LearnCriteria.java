package org.dllearner.scripts.matching;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.CSVFileToArray;
import org.dllearner.utilities.datastructures.StringTuple;

import com.wcohen.ss.Jaro;
import com.wcohen.ss.api.StringDistance;

public class LearnCriteria {
	ArrayList<SameCollect>sameAs =new ArrayList<SameCollect>() ;
	Mcollect m ;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LearnCriteria lc = new LearnCriteria();
		@SuppressWarnings("unused")
		StringDistance distance = new Jaro();
		try{
		CSVFileToArray csv = new CSVFileToArray("osmdata/owlsameas_en.csv");
		ArrayList<String> al =null ;
		
		while ((al = csv.next()) != null){
			//System.out.println(al);
			if(al.size()!=2)continue;
			//if(distance.score(al.get(0), al.get(1))>=0.7){
			//System.out.println(distance.score(al.get(0), al.get(1)));
			//System.out.println(al);
			//}
			//String dbpedia = al.get(1).replace("%25", "%");
			String dbpedia = al.get(1);
			lc.sameAs.add(new SameCollect(al.get(0), dbpedia));
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		Mcollect m = new Mcollect();
		SPARQLTasks dbpedia = new SPARQLTasks(new Cache("matching"), SparqlEndpoint.getEndpointLOCALDBpedia());
		int countzerold = 0;
		int countzerodb = 0;
		for (int x = 0; x<lc.sameAs.size();x++) {
			SameCollect s = lc.sameAs.get(x);
			String query = "SELECT * WHERE {<"+s.db+"> ?p ?o}";
			s.dbdata = dbpedia.queryAsRDFNodeTuple(query, "?p", "?o");
			s.lddata = lc.getLinkedData(s.ld);
//			System.exit(0);
//			System.out.println(s.lddata);
//			for (StringTuple string : s.lddata ) {
//				System.out.println(string);
//			}
			m.add(s);
			if(s.dbdata.size() == 0){
				System.out.println(s.db);
				countzerodb+=1;
			}
			//if( s.lddata.size() == 0)countzerold+=1;
//			if(x>110) break;
			System.out.println(x);
		}
	
		System.out.println(countzerodb);
		System.out.println(countzerold);
	//	System.exit(0);

		System.out.println(m);
		//System.out.println(lc.sameAs);
		//System.out.println(lc.sameAs.size());
		
		
	}
	
	public SortedSet<StringTuple> getLinkedData(String url){
		SortedSet<StringTuple> result = new TreeSet<StringTuple>();
		try{
		URL linkedGeoDataURL = new URL(url);
		
		URLConnection conn = linkedGeoDataURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line="";
		@SuppressWarnings("unused")
		boolean oneLine = false;
		while ((line = rd.readLine()) != null)
			
		{	oneLine = true;
//			System.out.println(line);continue;
			line = line.replace("<"+url+"#id>", "");
			line = line.replace("<"+url+">", "");
			
			
			String p = line.substring(line.indexOf("<")+1,line.indexOf(">") );
			line = line.substring(line.indexOf(">")+1);
			line = line.substring(0,line.lastIndexOf("."));
			line = line.trim();
			line = line.substring(1);
			String o = line.substring(0,line.length()-1);
//			System.out.println(new StringTuple(p,o));
			result.add(new StringTuple(p,o));
		}
		
		rd.close();	
		
		}catch (Exception e) {
				e.printStackTrace();
		}
		return result;
	}

}
