package org.dllearner.algorithms.qtl.examples;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;

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
public class DBpediaExample {
	
	static QueryTreeFactory factory = new QueryTreeFactoryBase();
	
	public static List<RDFResourceTree> getPosExampleTrees(){
		List<RDFResourceTree> posExampleTrees = new ArrayList<RDFResourceTree>();
		
		posExampleTrees.add(factory.getQueryTree("Leibnitz", getLeibnitzExampleModel()));
		posExampleTrees.add(factory.getQueryTree("MaxImmelmann", getMaxImmelmannExampleModel()));
		
		return posExampleTrees;
	}
	
	public static List<RDFResourceTree> getNegExampleTrees(){
		List<RDFResourceTree> negExampleTrees = new ArrayList<RDFResourceTree>();
		
		negExampleTrees.add(factory.getQueryTree("CharlesGarnier", getCharlesGarnierExampleModel()));
		
		return negExampleTrees;
	}
	
	private static Model getLeibnitzExampleModel(){
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
	
	private static Model getMaxImmelmannExampleModel(){
		Model model = ModelFactory.createDefaultModel();
		
		Resource maxImmelmann = model.createResource("MaxImmelmann");
		Resource dresden = model.createResource("Dresden");
		Resource cdu = model.createResource("CDU");
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
	
	private static Model getCharlesGarnierExampleModel(){
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

}
