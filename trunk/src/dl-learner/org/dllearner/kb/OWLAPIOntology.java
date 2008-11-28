package org.dllearner.kb;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.configurators.OWLAPIOntologyConfigurator;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.owl.KB;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

public class OWLAPIOntology extends KnowledgeSource {

	private OWLAPIOntologyConfigurator configurator;
	@Override
	public OWLAPIOntologyConfigurator getConfigurator(){
		return configurator;
	}
	
	private OWLOntology ontology;
	private Set<OWLOntology> ontologies;
	private Set<OWLClass> classes;
	private Set<OWLObjectProperty> prop;
	private Set<OWLDataProperty> dataProp;
	private Set<OWLIndividual> individuals;
	
	public OWLAPIOntology() {
		this(null);
	}
	
	public OWLAPIOntology(OWLOntology onto)
	{
		this.ontology = onto;
		classes = ontology.getReferencedClasses();
		prop = ontology.getReferencedObjectProperties();
		dataProp = ontology.getReferencedDataProperties();
		individuals = ontology.getReferencedIndividuals();
		this.configurator = new OWLAPIOntologyConfigurator(this);
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
			if(ont.getReferencedClasses() != null) {
				classes.addAll(ont.getReferencedClasses());
			}
			if(ont.getReferencedObjectProperties() != null) {
				prop.addAll(ont.getReferencedObjectProperties());
			}
			if(ont.getReferencedDataProperties() != null) {
				dataProp.addAll(ont.getReferencedDataProperties());
			}
			if(ont.getReferencedIndividuals() != null) {
				individuals.addAll(ont.getReferencedIndividuals());
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
	
	public Set<OWLIndividual> getOWLIndividuals() {
		return individuals;
	}
}
