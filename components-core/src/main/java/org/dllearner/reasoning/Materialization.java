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
import org.semanticweb.owlapi.model.OWLObjectProperty;

class Materialization implements Serializable{
		// we use sorted sets (map indices) here, because they have only log(n)
		// complexity for checking whether an element is contained in them
		// instances of classes
		public Map<OWLClass, TreeSet<OWLIndividual>> classInstancesPos = new TreeMap<>();
		public Map<OWLClass, TreeSet<OWLIndividual>> classInstancesNeg = new TreeMap<>();
		// object property mappings
		public Map<OWLObjectProperty, Map<OWLIndividual, SortedSet<OWLIndividual>>> opPos = new TreeMap<>();
		// data property mappings
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<OWLLiteral>>> dpPos = new TreeMap<>();
			
		
		// datatype property mappings
		// for boolean data properties we have one mapping for true and false for efficiency reasons
		public Map<OWLDataProperty, TreeSet<OWLIndividual>> bdPos = new TreeMap<>();
		public Map<OWLDataProperty, TreeSet<OWLIndividual>> bdNeg = new TreeMap<>();
		//double datatype property mappings
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Double>>> dd = new TreeMap<>();
		//int datatype property
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Integer>>> id = new TreeMap<>();
		//string datatype property
		public Map<OWLDataProperty, Map<OWLIndividual, SortedSet<String>>> sd = new TreeMap<>();
	}