package org.dllearner.autosparql.server.search;

import info.bliki.api.Page;
import info.bliki.api.XMLPagesParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.model.Example;
import org.xml.sax.SAXException;

public class WikipediaSearch implements Search{
	
	private int hitsPerPage = 10;
	

	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, 0);
	}

	@Override
	public List<String> getResources(String queryString, int offset) {
		List<String> resources = new ArrayList<String>();
		try {
			String[] words = queryString.split(" ");
			String modifiedQuery = "";
			for(int i = 0; i < words.length-1; i++){
				modifiedQuery += words[i] + "+";
			}
			modifiedQuery += words[words.length-1];
			URL url = new URL("http://en.wikipedia.org/w/api.php?action=query&list=search&format=xml&srsearch=" + modifiedQuery + "&srlimit=" + hitsPerPage);
			URLConnection conn = url.openConnection ();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			XMLPagesParser parser = new XMLPagesParser(sb.toString());
			parser.parse();
			for (Page page : parser.getPagesList()) {
				resources.add("http://dbpedia.org/resource/" + page.getTitle().replace(" ", "_"));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return resources;
	}

	@Override
	public List<Example> getExamples(String queryString) {
		return getExamples(queryString, 0);
	}

	@Override
	public List<Example> getExamples(String queryString, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalHits(String queryString) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setHitsPerPage(int hitsPerPage) {
		this.hitsPerPage = hitsPerPage;
		
	}

}
