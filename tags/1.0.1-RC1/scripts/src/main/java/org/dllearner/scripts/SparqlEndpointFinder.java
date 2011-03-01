/**
 * Copyright (C) 2007-2010, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.scripts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dllearner.utilities.Files;

/**
 * Tries to find some public SPARQL endpoints on the web.
 * 
 * @author Jens Lehmann
 *
 */
public class SparqlEndpointFinder {

	public static void main(String[] args) throws MalformedURLException, IOException {
		SparqlEndpointFinder finder = new SparqlEndpointFinder();
		System.out.println(finder.find());
	}
		
	public List<String> find() throws MalformedURLException, IOException {
		// the W3 wiki SPARQL endpoints page is often updated
		String str = Files.readFile(new URL("http://esw.w3.org/index.php?title=SparqlEndpoints&action=edit"));
		// typical wiki syntax: [http://dbtune.org/bbc/peel/sparql endpoint]
		Pattern p = Pattern.compile("\\[(.+?) endpoint\\]");
		Matcher m = p.matcher(str);
		
		List<String> endpoints = new LinkedList<String>();
		while (m.find()) {
			String endpoint = m.group(1);
			if(SparqlEndpointFinder.validateEndpoint(endpoint)) {
				endpoints.add(endpoint);
			}
		}	
		
		// TODO: finde more endpoints e.g. at http://www.freebase.com/view/user/bio2rdf/public/sparql
		// or using voiD files
		
		// TODO: find graphs
		
		return endpoints;
	}
	
	public static boolean validateEndpoint(String str) {
//		URL url = null;
		try {
//			url = new URL(str);
			new URL(str);
		} catch (MalformedURLException e) {
			return false;
		}
		// TODO: send example query to check whether endpoint is alive
		return true;
	}
	
}
