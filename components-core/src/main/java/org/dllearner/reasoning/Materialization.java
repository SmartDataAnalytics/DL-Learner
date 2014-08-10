package org.dllearner.reasoning;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;

class Materialization implements Serializable{
		// we use sorted sets (map indices) here, because they have only log(n)
		// complexity for checking whether an element is contained in them
		// instances of classes
		public Map<OWLClass, TreeSet<Individual>> classInstancesPos = new TreeMap<OWLClass, TreeSet<Individual>>();
		public Map<OWLClass, TreeSet<Individual>> classInstancesNeg = new TreeMap<OWLClass, TreeSet<Individual>>();
		// object property mappings
		public Map<ObjectProperty, Map<Individual, SortedSet<Individual>>> opPos = new TreeMap<ObjectProperty, Map<Individual, SortedSet<Individual>>>();
		// data property mappings
		public Map<OWLDataProperty, Map<Individual, SortedSet<Constant>>> dpPos = new TreeMap<OWLDataProperty, Map<Individual, SortedSet<Constant>>>();
			
		
		// datatype property mappings
		// for boolean data properties we have one mapping for true and false for efficiency reasons
		public Map<OWLDataProperty, TreeSet<Individual>> bdPos = new TreeMap<OWLDataProperty, TreeSet<Individual>>();
		public Map<OWLDataProperty, TreeSet<Individual>> bdNeg = new TreeMap<OWLDataProperty, TreeSet<Individual>>();
		//double datatype property mappings
		public Map<OWLDataProperty, Map<Individual, SortedSet<Double>>> dd = new TreeMap<OWLDataProperty, Map<Individual, SortedSet<Double>>>();
		//int datatype property
		public Map<OWLDataProperty, Map<Individual, SortedSet<Integer>>> id = new TreeMap<OWLDataProperty, Map<Individual, SortedSet<Integer>>>();
		//string datatype property
		public Map<OWLDataProperty, Map<Individual, SortedSet<String>>> sd = new TreeMap<OWLDataProperty, Map<Individual, SortedSet<String>>>();
	}