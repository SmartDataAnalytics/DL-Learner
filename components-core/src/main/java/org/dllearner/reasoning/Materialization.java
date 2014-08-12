package org.dllearner.reasoning;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import com.hp.hpl.jena.ontology.ObjectProperty;

class Materialization implements Serializable{
		// we use sorted sets (map indices) here, because they have only log(n)
		// complexity for checking whether an element is contained in them
		// instances of classes
		public Map<OWLClass, TreeSet<OWLIndividual>> classInstancesPos = new TreeMap<OWLClass, TreeSet<OWLIndividual>>();
		public Map<OWLClass, TreeSet<OWLIndividual>> classInstancesNeg = new TreeMap<OWLClass, TreeSet<OWLIndividual>>();
		// object property mappings
		public Map<ObjectProperty, Map<OWLIndividual, SortedSet<OWLIndividual>>> opPos = new TreeMap<ObjectProperty, Map<OWLIndividual, SortedSet<OWLIndividual>>>();
		// data property mappings
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<OWLLiteral>>> dpPos = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<OWLLiteral>>>();
			
		
		// datatype property mappings
		// for boolean data properties we have one mapping for true and false for efficiency reasons
		public Map<OWLDataProperty, TreeSet<OWLIndividual>> bdPos = new TreeMap<OWLDataProperty, TreeSet<OWLIndividual>>();
		public Map<OWLDataProperty, TreeSet<OWLIndividual>> bdNeg = new TreeMap<OWLDataProperty, TreeSet<OWLIndividual>>();
		//double datatype property mappings
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Double>>> dd = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<Double>>>();
		//int datatype property
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Integer>>> id = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<Integer>>>();
		//string datatype property
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<String>>> sd = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<String>>>();
	}