/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
 */
package org.dllearner.algorithms.qtl.util;

import java.util.HashMap;
import java.util.Map;

public class Prefixes {
	
	public static Map<String,String> getPrefixes(){
		
		Map<String,String> prefixes = new HashMap<>();
		prefixes.put("dbo","http://dbpedia.org/ontology/");
		prefixes.put("dbprop","http://dbpedia.org/property/");
		prefixes.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("skos","http://www.w3.org/2004/02/skos/core#");
		prefixes.put("geo","http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixes.put("georss","http://www.georss.org/georss/");
		prefixes.put("owl","http://www.w3.org/2002/07/owl#");
		prefixes.put("yago","http://dbpedia.org/class/yago/");
		prefixes.put("cyc","http://sw.opencyc.org/concept/");
		prefixes.put("foaf","http://xmlns.com/foaf/0.1/");
		
		return prefixes;
	}
	
	public static String getDBpediaBaseURI(){
		return  "http://dbpedia.org/resource/";
	}

}
