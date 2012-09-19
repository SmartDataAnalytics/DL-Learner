/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.utilities.owl;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.OWL2Datatype;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.UntypedConstant;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * A collection of methods for exchanging objects between OWL API and
 * DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public final class OWLAPIConverter {

	private static OWLDataFactory staticFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	
	/**
	 * Converts a DL-Learner axiom into an OWL API axiom.
	 * 
	 * @see OWLAPIAxiomConvertVisitor#convertAxiom(Axiom)
	 * @param axiom The axiom to convert.
	 * @return An OWL API axiom.
	 */
	public static OWLAxiom getOWLAPIAxiom(Axiom axiom) {
		return OWLAPIAxiomConvertVisitor.convertAxiom(axiom);
	}	
	
	/**
	 * Converts a DL-Learner description into an OWL API description.
	 * 
	 * @see OWLAPIDescriptionConvertVisitor#getOWLClassExpression(org.dllearner.core.owl.Description)
	 * @param description DL-Learner description.
	 * @return Corresponding OWL API description.
	 */
	public static OWLClassExpression getOWLAPIDescription(Description description) {
		return OWLAPIDescriptionConvertVisitor.getOWLClassExpression(description);
	}
	
	public static OWLIndividual getOWLAPIIndividual(Individual individual) {
		return staticFactory.getOWLNamedIndividual(IRI.create(individual.getName()));
	}	
	
	public static Set<OWLIndividual> getOWLAPIIndividuals(Set<Individual> individuals) {
		Set<OWLIndividual> inds = new TreeSet<OWLIndividual>();
		for(Individual individual : individuals) {
			inds.add(getOWLAPIIndividual(individual));
		}
		return inds;
	}	
	
	public static OWLObjectProperty getOWLAPIObjectProperty(ObjectProperty role) {
		return staticFactory.getOWLObjectProperty(IRI.create(role.getName()));
	}
	
	public static OWLDataProperty getOWLAPIDataProperty(DatatypeProperty datatypeProperty) {
		return staticFactory.getOWLDataProperty(IRI.create(datatypeProperty.getName()));
	}
	
	public static OWLEntity getOWLAPIEntity(Entity entity) {
		if(entity instanceof ObjectProperty) {
			return staticFactory.getOWLObjectProperty(IRI.create(entity.getName()));
		} else if(entity instanceof DatatypeProperty) {
			return staticFactory.getOWLDataProperty(IRI.create(entity.getName()));	
		} else if(entity instanceof NamedClass) {
			return staticFactory.getOWLClass(IRI.create(entity.getName()));			
		} else if(entity instanceof Individual) {
			return staticFactory.getOWLNamedIndividual(IRI.create(entity.getName()));						
		}
		// should never happen
		throw new Error("OWL API entity conversion for " + entity + " not supported.");
	}
	
	public static Individual convertIndividual(OWLNamedIndividual individual) {
		return new Individual(individual.getIRI().toString());
	}
	
	public static Set<Individual> convertIndividuals(Set<? extends OWLIndividual> individuals) {
		Set<Individual> inds = new TreeSet<Individual>();
		for(OWLIndividual individual : individuals) {
			inds.add(convertIndividual(individual.asOWLNamedIndividual()));
		}
		return inds;
	}	
	
	public static ObjectProperty convertObjectProperty(OWLObjectProperty property) {
		return new ObjectProperty(property.getIRI().toString());
	}
	
	public static DatatypeProperty convertIndividual(OWLDataProperty property) {
		return new DatatypeProperty(property.getIRI().toString());
	}	
	
	public static Description convertClass(OWLClass owlClass) {
		if(owlClass.isOWLThing()) {
			return Thing.instance;
		} else if(owlClass.isOWLNothing()) {
			return Nothing.instance;
		} else {
			return new NamedClass(owlClass.getIRI().toString());
		}
	}	
	
	public static Constant convertConstant(OWLLiteral constant) {
		Constant c;
		// for typed constants we have to figure out the correct
		// data type and value

		if(constant.isRDFPlainLiteral()){
			if(constant.hasLang()){
				c = new UntypedConstant(constant.getLiteral(), constant.getLang());
			} else {
				c = new UntypedConstant(constant.getLiteral());
			}
		} else {
			Datatype dt = OWLAPIConverter.convertDatatype(constant.getDatatype());
	        c = new TypedConstant(constant.getLiteral(),dt);
		}
       
		return c;
	}

	public static Set<Constant> convertConstants(Set<OWLLiteral> constants) {
		SortedSet<Constant> is = new TreeSet<Constant>();
		for(OWLLiteral oi : constants) {
			is.add(convertConstant(oi));
		}		
		return is;			
	}		
	
	public static Datatype convertDatatype(OWLDatatype dataType) {
		URI uri = dataType.getIRI().toURI();
		if(uri.equals(OWL2Datatype.BOOLEAN.getURI()))
			return OWL2Datatype.BOOLEAN.getDatatype();
		else if(uri.equals(OWL2Datatype.DOUBLE.getURI()))
			return OWL2Datatype.DOUBLE.getDatatype();
		else if(uri.equals(OWL2Datatype.INT.getURI()))
			return OWL2Datatype.INT.getDatatype();			
		else if(uri.equals(OWL2Datatype.INTEGER.getURI()))
			return OWL2Datatype.INTEGER.getDatatype();			
		else if(uri.equals(OWL2Datatype.STRING.getURI()))
			return OWL2Datatype.STRING.getDatatype();			
		else if(uri.equals(OWL2Datatype.DATE.getURI()))
			return OWL2Datatype.DATE.getDatatype();
		else if(uri.equals(OWL2Datatype.DATETIME.getURI()))
			return OWL2Datatype.DATETIME.getDatatype();
		throw new Error("Unsupported datatype " + dataType + ". Please inform a DL-Learner developer to add it.");
	}

}
