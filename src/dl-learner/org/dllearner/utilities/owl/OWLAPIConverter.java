/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.UntypedConstant;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;

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
	 * @see OWLAPIDescriptionConvertVisitor#getOWLDescription(Description)
	 * @param description DL-Learner description.
	 * @return Corresponding OWL API description.
	 */
	public static OWLDescription getOWLAPIDescription(Description description) {
		return OWLAPIDescriptionConvertVisitor.getOWLDescription(description);
	}
	
	public static OWLIndividual getOWLAPIIndividual(Individual individual) {
		return staticFactory.getOWLIndividual(URI.create(individual.getName()));
	}	
	
	public static OWLObjectProperty getOWLAPIObjectProperty(ObjectProperty role) {
		return staticFactory.getOWLObjectProperty(URI.create(role.getName()));
	}
	
	public static OWLDataProperty getOWLAPIDataProperty(DatatypeProperty datatypeProperty) {
		return staticFactory.getOWLDataProperty(URI.create(datatypeProperty.getName()));
	}
	
	public static OWLEntity getOWLAPIEntity(Entity entity) {
		if(entity instanceof ObjectProperty) {
			return staticFactory.getOWLObjectProperty(URI.create(entity.getName()));
		} else if(entity instanceof DatatypeProperty) {
			return staticFactory.getOWLDataProperty(URI.create(entity.getName()));	
		} else if(entity instanceof NamedClass) {
			return staticFactory.getOWLClass(URI.create(entity.getName()));			
		} else if(entity instanceof OWLIndividual) {
			return staticFactory.getOWLIndividual(URI.create(entity.getName()));						
		}
		// should never happen
		throw new Error("OWL API entity conversion for " + entity + " not supported.");
	}
	
	public static Individual convertIndividual(OWLIndividual individual) {
		return new Individual(individual.getURI().toString());
	}
	
	public static Set<Individual> convertIndividuals(Set<OWLIndividual> individuals) {
		Set<Individual> inds = new TreeSet<Individual>();
		for(OWLIndividual individual : individuals) {
			inds.add(convertIndividual(individual));
		}
		return inds;
	}	
	
	public static ObjectProperty convertObjectProperty(OWLObjectProperty property) {
		return new ObjectProperty(property.getURI().toString());
	}
	
	public static DatatypeProperty convertIndividual(OWLDataProperty property) {
		return new DatatypeProperty(property.getURI().toString());
	}	
	
	public static Description convertClass(OWLClass owlClass) {
		if(owlClass.isOWLThing()) {
			return Thing.instance;
		} else if(owlClass.isOWLNothing()) {
			return Nothing.instance;
		} else {
			return new NamedClass(owlClass.getURI().toString());
		}
	}	
	
	public static Constant convertConstant(OWLConstant constant) {
		Constant c;
		// for typed constants we have to figure out the correct
		// data type and value
		if(constant instanceof OWLTypedConstant) {
			Datatype dt = OWLAPIConverter.convertDatatype(((OWLTypedConstant)constant).getDataType());
			c = new TypedConstant(constant.getLiteral(),dt);
		// for untyped constants we have to figure out the value
		// and language tag (if any)
		} else {
			OWLUntypedConstant ouc = (OWLUntypedConstant) constant;
			if(ouc.hasLang())
				c = new UntypedConstant(ouc.getLiteral(), ouc.getLang());
			else
				c = new UntypedConstant(ouc.getLiteral());
		}		
		return c;
	}

	public static Set<Constant> convertConstants(Set<OWLConstant> constants) {
		SortedSet<Constant> is = new TreeSet<Constant>();
		for(OWLConstant oi : constants) {
			is.add(convertConstant(oi));
		}		
		return is;			
	}		
	
	public static Datatype convertDatatype(OWLDataType dataType) {
		URI uri = dataType.getURI();
		if(uri.equals(Datatype.BOOLEAN.getURI()))
			return Datatype.BOOLEAN;
		else if(uri.equals(Datatype.DOUBLE.getURI()))
			return Datatype.DOUBLE;
		else if(uri.equals(Datatype.INT.getURI()))
			return Datatype.INT;			
		
		throw new Error("Unsupported datatype " + dataType + ". Please inform a DL-Learner developer to add it.");
	}

}
