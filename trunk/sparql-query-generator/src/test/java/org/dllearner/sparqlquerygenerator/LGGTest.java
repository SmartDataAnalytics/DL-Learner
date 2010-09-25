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
package org.dllearner.sparqlquerygenerator;

import java.io.PrintWriter;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.operations.LGG;
import org.dllearner.sparqlquerygenerator.operations.NBR;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGTest {
	
	@Test
	public void testLGGWithTrees(){
		QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
		
		System.out.println("TREE 1:");
		
		Model model = getLeibnitzExampleModel();
		QueryTreeImpl<String> tree1 = factory.getQueryTree(model.createResource("Leibnitz"), model);
		tree1.dump(new PrintWriter(System.out));
		
		System.out.println("TREE 2:");
		
		model = getMaxImmelmannExampleModel();
		QueryTreeImpl<String> tree2 = factory.getQueryTree(model.createResource("MaxImmelmann"), model);
		tree2.dump(new PrintWriter(System.out));
		
		QueryTree<String> lgg = LGG.computeLGG(tree1, tree2);
		
		System.out.println("LGG:");
		lgg.dump(new PrintWriter(System.out));
		
		QueryTreeImpl<String> tree = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> subTree1 = new QueryTreeImpl<String>("?");
		subTree1.addChild(new QueryTreeImpl<String>("SPD"), "leaderParty");
		subTree1.addChild(new QueryTreeImpl<String>("?"), "population");
		subTree1.addChild(new QueryTreeImpl<String>("Germany"), "locatedIn");
		tree.addChild(subTree1, "birthPlace");
		tree.addChild(new QueryTreeImpl<String>("?"), RDFS.label.toString());
		QueryTreeImpl<String> subTree2 = new QueryTreeImpl<String>("Person");
		subTree2.addChild(new QueryTreeImpl<String>(OWL.Thing.toString()), RDFS.subClassOf.toString());
		tree.addChild(subTree2, RDF.type.toString());
		QueryTreeImpl<String> subTree3 = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> subSubTree = new QueryTreeImpl<String>("Person");
		subSubTree.addChild(new QueryTreeImpl<String>(OWL.Thing.toString()), RDFS.subClassOf.toString());
		subTree3.addChild(subSubTree, RDFS.subClassOf.toString());
		tree.addChild(subTree3, RDF.type.toString());
		
		System.out.println(lgg.toSPARQLQueryString());
		
		QueryTree<String> nbr = NBR.computeNBR((QueryTreeImpl<String>)lgg, factory.getQueryTree("CharlesGarnier", getCharlesGarnierExampleModel()));
		System.out.println("NBR:");
		nbr.dump(new PrintWriter(System.out));
		
		Assert.assertTrue(lgg.equals(tree));
		
	}
	
	private Model getMaxImmelmannExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource maxImmelmann = model.createResource("MaxImmelmann");
		Resource dresden = model.createResource("Dresden");
		Resource cdu = model.createResource("SPD");
		Resource germany = model.createResource("Germany");
		Resource militaryPerson = model.createResource("MilitaryPerson");
		Resource person = model.createResource("Person");
		
		Property birthPlace = model.createProperty("birthPlace");
		Property leaderParty = model.createProperty("leaderParty");
		Property population = model.createProperty("population");
		Property locatedIn = model.createProperty("locatedIn");
		
		maxImmelmann.addProperty(birthPlace, dresden);
		dresden.addProperty(leaderParty, cdu);
		dresden.addLiteral(population, 512000);
		dresden.addProperty(locatedIn, germany);
		maxImmelmann.addProperty(RDFS.label, "Max Immelmann");
		maxImmelmann.addProperty(RDF.type, militaryPerson);
		militaryPerson.addProperty(RDFS.subClassOf, person);
		maxImmelmann.addProperty(RDF.type, person);
		person.addProperty(RDFS.subClassOf, OWL.Thing);
		
		return model;
	}
	
	private Model getLeibnitzExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource leibnitz = model.createResource("Leibnitz");
		Resource leipzig = model.createResource("Leipzig");
		Resource spd = model.createResource("SPD");
		Resource germany = model.createResource("Germany");
		Resource christianWolf = model.createResource("ChristianWolf");
		Resource immanuelKant = model.createResource("ImmanuelKant");
		Resource _18thCenturyPhilosophy = model.createResource("18thCenturyPhilosophy");
		Resource person = model.createResource("Person");
		Resource philosopher = model.createResource("Philosopher");
		
		Property birthPlace = model.createProperty("birthPlace");
		Property leaderParty = model.createProperty("leaderParty");
		Property population = model.createProperty("population");
		Property locatedIn = model.createProperty("locatedIn");
		Property birthDate = model.createProperty("birthDate");
		Property influenced = model.createProperty("influenced");
		Property era = model.createProperty("era");
		
		leibnitz.addProperty(birthPlace, leipzig);
		leipzig.addProperty(leaderParty, spd);
		leipzig.addLiteral(population, 515000);
		leipzig.addProperty(locatedIn, germany);
		leibnitz.addProperty(RDFS.label, "Gottfried Wilhelm Leibnitz");
		leibnitz.addLiteral(birthDate,model.createTypedLiteral("1646-07-01", XSDDatatype.XSDdate));
		leibnitz.addProperty(influenced, christianWolf);
		christianWolf.addProperty(influenced, immanuelKant);
		christianWolf.addProperty(era, _18thCenturyPhilosophy);
		leibnitz.addProperty(RDF.type, person);
		person.addProperty(RDFS.subClassOf, OWL.Thing);
		leibnitz.addProperty(RDF.type, philosopher);
		philosopher.addProperty(RDFS.subClassOf, person);
		
		return model;
	}
	
	private Model getCharlesGarnierExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource charlesGarnier = model.createResource("CharlesGarnier");
		Resource paris = model.createResource("Paris");
		Resource sp = model.createResource("SP");
		Resource france = model.createResource("France");
		Resource person = model.createResource("Person");
		Resource architekt = model.createResource("Architekt");
		
		Property birthPlace = model.createProperty("birthPlace");
		Property leaderParty = model.createProperty("leaderParty");
		Property population = model.createProperty("population");
		Property locatedIn = model.createProperty("locatedIn");
		Property birthDate = model.createProperty("birthDate");
		
		charlesGarnier.addProperty(birthPlace, paris);
		paris.addProperty(leaderParty, sp);
		paris.addLiteral(population, 2200000);
		paris.addProperty(locatedIn, france);
		charlesGarnier.addProperty(RDFS.label, "Charles Garnier");
		charlesGarnier.addLiteral(birthDate,model.createTypedLiteral("1825-11-06", XSDDatatype.XSDdate));
		charlesGarnier.addProperty(RDF.type, person);
		person.addProperty(RDFS.subClassOf, OWL.Thing);
		charlesGarnier.addProperty(RDF.type, architekt);
		architekt.addProperty(RDFS.subClassOf, person);
		
		return model;
	}
	
	private Model getNode660663336ExampleModel(){
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
