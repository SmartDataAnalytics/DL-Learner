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
package org.dllearner.algorithms.qtl.examples;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LinkedGeoDataExample {
	
	static QueryTreeFactory factory = new QueryTreeFactoryBase();
	
	public static List<RDFResourceTree> getPosExampleTrees(){
		List<RDFResourceTree> posExampleTrees = new ArrayList<>();
		
		posExampleTrees.add(factory.getQueryTree("lgd:node660663336", getNode660663336ExampleModel()));
		posExampleTrees.add(factory.getQueryTree("lgd:node265046409", getNode265046409ExampleModel()));
		
		return posExampleTrees;
	}
	
	public static List<RDFResourceTree> getNegExampleTrees(){
		List<RDFResourceTree> negExampleTrees = new ArrayList<>();
		
		negExampleTrees.add(factory.getQueryTree("lgd:node101156499", getNode101156499ExampleModel()));
		negExampleTrees.add(factory.getQueryTree("lgd:node26608237", getNode26608237ExampleModel()));
		
		
		return negExampleTrees;
	}
	
	private static Model getNode660663336ExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource lgd_node = model.createResource("lgd:node660663336");
		Resource lgdo_aerodome = model.createResource("lgdo:Aerodome");
		Resource lgdo_aeroway = model.createResource("lgdo:Aeroway");
		
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
		lgd_node.addProperty(georss_point, "51.423889 12.236389");
		lgd_node.addLiteral(geo_lat, 51.423889);
		lgd_node.addLiteral(geo_long, 12.236389);
		
		return model;
	}
	
	private static Model getNode265046409ExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource lgd_node = model.createResource("lgd:node265046409");
		Resource lgdo_aerodome = model.createResource("lgdo:Aerodome");
		Resource lgdo_aeroway = model.createResource("lgdo:Aeroway");
		
		Property lgdp_createdBy = model.createProperty("lgdp:created_by");
		Property lgdp_icao = model.createProperty("lgdp:icao");
		Property georss_point = model.createProperty("georss:point");
		Property geo_lat = model.createProperty("geo:lat");
		Property geo_long = model.createProperty("geo:long");
		
		lgd_node.addProperty(RDF.type, lgdo_aerodome);
		lgdo_aerodome.addProperty(RDFS.subClassOf, lgdo_aeroway);
		lgd_node.addProperty(RDFS.label, "Flughafen Halle-Oppin");
		lgd_node.addProperty(lgdp_createdBy, "Potlatch 0.9a");
		lgd_node.addProperty(lgdp_icao, "EDAQ");
		lgd_node.addProperty(georss_point, "51.550091 12.0537899");
		lgd_node.addLiteral(geo_lat, 51.550091);
		lgd_node.addLiteral(geo_long, 12.0537899);
		
		return model;
	}
	
	private static Model getNode101156499ExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource lgd_node = model.createResource("lgd:node101156499");
		Resource lgdo_aerodome = model.createResource("lgdo:Aerodome");
		Resource lgdo_aeroway = model.createResource("lgdo:Aeroway");
		
		Property lgdo_ele = model.createProperty("lgdo:ele");
		Property lgdo_wikipedia = model.createProperty("lgdo:wikipedia");
		Property lgdp_altName = model.createProperty("lgdp:alt_name");
		Property lgdp_closestTown = model.createProperty("lgdp:closest_town");
		Property lgdp_createdBy = model.createProperty("lgdp:created_by");
		Property lgdp_iata = model.createProperty("lgdp:iata");
		Property lgdp_icao = model.createProperty("lgdp:icao");
		Property lgdp_isIn = model.createProperty("lgdp:is_in");
		Property lgdp_source = model.createProperty("lgdp:source");
		Property lgdp_type = model.createProperty("lgdp:type");
		Property name_en = model.createProperty("name_en");
		Property name_de = model.createProperty("name_de");
		Property georss_point = model.createProperty("georss:point");
		Property geo_lat = model.createProperty("geo:lat");
		Property geo_long = model.createProperty("geo:long");
		
		lgd_node.addProperty(RDF.type, lgdo_aerodome);
		lgdo_aerodome.addProperty(RDFS.subClassOf, lgdo_aeroway);
		lgd_node.addProperty(RDFS.label, "Flughafen Dresden");
		lgd_node.addLiteral(lgdo_ele, model.createTypedLiteral(230, XSDDatatype.XSDfloat));
		lgd_node.addLiteral(lgdo_wikipedia, model.createLiteral("Dresden_Airport", "en"));
		lgd_node.addProperty(lgdp_altName, "Dresden-Klotzsche");
		lgd_node.addProperty(lgdp_closestTown, "Dresden, Germany");
		lgd_node.addProperty(lgdp_createdBy, "Potlatch 0.5d");
		lgd_node.addProperty(lgdp_iata, "DRS");
		lgd_node.addProperty(lgdp_icao, "EDDC");
		lgd_node.addProperty(lgdp_isIn, "DE");
		lgd_node.addProperty(name_de, "Flughafen Dresden");
		lgd_node.addProperty(name_en, "Dresden Airport");
		lgd_node.addProperty(lgdp_source, "wikipedia");
		lgd_node.addProperty(lgdp_type, "Public");
		lgd_node.addProperty(georss_point, "51.1299428 13.7656598");
		lgd_node.addLiteral(geo_lat, 51.1299428);
		lgd_node.addLiteral(geo_long, 13.7656598);
		
		return model;
	}
	
	private static Model getNode26608237ExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource lgd_node = model.createResource("lgd:node26608237");
		Resource lgdo_aerodome = model.createResource("lgdo:Aerodome");
		Resource lgdo_aeroway = model.createResource("lgdo:Aeroway");
		
		Property lgdp_createdBy = model.createProperty("lgdp:created_by");
		Property lgdp_iata = model.createProperty("lgdp:iata");
		Property lgdp_icao = model.createProperty("lgdp:icao");
		Property lgdp_isIn = model.createProperty("lgdp:is_in");
		Property lgdp_place = model.createProperty("lgdp:source");
		Property lgdp_source = model.createProperty("lgdp:source");
		Property lgdp_type = model.createProperty("lgdp:type");
		Property name_en = model.createProperty("name_en");
		Property georss_point = model.createProperty("georss:point");
		Property geo_lat = model.createProperty("geo:lat");
		Property geo_long = model.createProperty("geo:long");
		
		lgd_node.addProperty(RDF.type, lgdo_aerodome);
		lgdo_aerodome.addProperty(RDFS.subClassOf, lgdo_aeroway);
		lgd_node.addProperty(RDFS.label, "Flughafen Berlin-Schönefeld");
		lgd_node.addProperty(lgdp_createdBy, "Potlatch 0.10f");
		lgd_node.addProperty(lgdp_iata, "SXF");
		lgd_node.addProperty(lgdp_icao, "EDDB");
		lgd_node.addProperty(lgdp_isIn, "Schönefeld, Bundesrepublik Deutschland, Europe");
		lgd_node.addProperty(name_en, "Berlin-Schönefeld International Airport");
		lgd_node.addProperty(lgdp_place, "airport");
		lgd_node.addProperty(lgdp_source, "Gagravarr_Airports");
		lgd_node.addProperty(lgdp_type, "civil");
		lgd_node.addProperty(georss_point, "52.3799909 13.5224656");
		lgd_node.addLiteral(geo_lat, 52.3799909);
		lgd_node.addLiteral(geo_long, 13.5224656);
		
		return model;
	}

}
