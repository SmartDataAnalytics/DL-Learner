package org.dllearner.kb;

import java.io.File;
import java.net.URI;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.owl.KB;

import org.semanticweb.owl.model.OWLOntology;

public class OWLAPIOntology extends KnowledgeSource {

	private OWLOntology ontology;
	
	public OWLAPIOntology(OWLOntology onto)
	{
		this.ontology = onto;
	}
	
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException 
	{
		
	}
	
	public OWLOntology getOWLOntolgy()
	{
		return ontology;
	}
	
	public KB toKB()
	{
		throw new Error("OWL -> KB conversion not implemented yet.");
	}
	
	public void init()
	{
		
	}
	
	public void export(File file, OntologyFormat format)
	{
		
	}
	
	public String toDIG(URI kbURI)
	{
		return null;
	}
}
