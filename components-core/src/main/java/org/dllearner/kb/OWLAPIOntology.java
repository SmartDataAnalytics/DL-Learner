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

package org.dllearner.kb;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.owl.KB;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class OWLAPIOntology extends AbstractKnowledgeSource {
	
	private OWLOntology ontology;
	private Set<OWLOntology> ontologies;
	private Set<OWLClass> classes;
	private Set<OWLObjectProperty> prop;
	private Set<OWLDataProperty> dataProp;
	private Set<OWLNamedIndividual> individuals;
	
	public OWLAPIOntology() {
		this(null);
	}
	
	public OWLAPIOntology(OWLOntology onto)
	{
		this.ontology = onto;
		classes = ontology.getClassesInSignature();
		prop = ontology.getObjectPropertiesInSignature();
		dataProp = ontology.getDataPropertiesInSignature();
		individuals = ontology.getIndividualsInSignature();
	}
	
	public static String getName() {
		return "OWL API Ontology";
	}
	
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException 
	{
		
	}
	
	public OWLOntology getOWLOntolgy()
	{
		return ontology;
	}
	
	@Override
	public KB toKB()
	{
		throw new Error("OWL -> KB conversion not implemented yet.");
	}
	
	@Override
	public void init()
	{
		
	}
	
	@Override
	public void export(File file, OntologyFormat format)
	{
		
	}
	
	@Override
	public String toDIG(URI kbURI)
	{
		return null;
	}
	
	public void setOWLOntologies(Set<OWLOntology> onto) {
		ontologies = onto;
		System.out.println("ONTO: " + ontologies);
		Iterator<OWLOntology> it = ontologies.iterator();
		while(it.hasNext()) {
			OWLOntology ont = it.next();
			if(ont.getClassesInSignature() != null) {
				classes.addAll(ont.getClassesInSignature());
			}
			if(ont.getObjectPropertiesInSignature() != null) {
				prop.addAll(ont.getObjectPropertiesInSignature());
			}
			if(ont.getDataPropertiesInSignature() != null) {
				dataProp.addAll(ont.getDataPropertiesInSignature());
			}
			if(ont.getIndividualsInSignature() != null) {
				individuals.addAll(ont.getIndividualsInSignature());
			}
		}
	}
	
	public Set<OWLOntology> getOWLOnntologies() {
		return ontologies;
	}
	
	public Set<OWLClass> getOWLClasses() {
		return classes;
	}
	
	public Set<OWLObjectProperty> getOWLObjectProperies() {
		return prop;
	}
	
	public Set<OWLDataProperty> getOWLDataProperies() {
		return dataProp;
	}
	
	public Set<OWLNamedIndividual> getOWLIndividuals() {
		return individuals;
	}
}
