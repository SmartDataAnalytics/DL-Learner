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
package org.dllearner.sparqlquerygenerator.examples;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LinkedGeoDataExample {
	
	public static Set<QueryTree<String>> getPosExampleTrees(){
		Set<QueryTree<String>> posExampleTrees = new HashSet<QueryTree<String>>();
		
		QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
		
		
		return posExampleTrees;
	}
	
	public static Set<QueryTree<String>> getNegExampleTrees(){
		Set<QueryTree<String>> negExampleTrees = new HashSet<QueryTree<String>>();
		
		QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
		
		
		return negExampleTrees;
	}
	
	private static Model getNode660663336ExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource lgd_node = model.createResource("lgd:node660663336");
		Resource lgdo_aerodome = model.createResource("Aerodome");
		Resource lgdo_aeroway = model.createResource("Aeroway");
		
		Property lgdp_description = model.createProperty("lgdp:description");
		Property lgdp_iata = model.createProperty("lgdp:iata");
		Property lgdp_icao = model.createProperty("lgdp:icao");
		Property georss_point = model.createProperty("georss:point");
		Property geo_lat = model.createProperty("geo:lat");
		Property geo_long = model.createProperty("geo:long");
		
		lgd_node.addProperty(RDF.type, lgdo_aerodome);
		lgdo_aerodome.addProperty(RDFS.subClassOf, lgdo_aeroway);
		lgd_node.addProperty(RDFS.label, "Flughafen Leipzig-Halle");
		lgd_node.addProperty(lgdp_description, "ARP");
		lgd_node.addProperty(lgdp_iata, "LEJ");
		lgd_node.addProperty(lgdp_icao, "EDDP");
		lgd_node.addProperty(georss_point, "");
		lgd_node.addLiteral(geo_lat, 51.423889);
		lgd_node.addLiteral(geo_long, 12.236389);
		
		return model;
	}

}
