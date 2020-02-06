package org.dllearner.cli.parcel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

public class Test {
	
	public static void main(String[] args) {

		
		/*
		Set<Long> setA, setB;
		
		Long l1 = new Long(1);
		Long l2 = new Long(2);
		Long l3 = new Long(3);
		Long l4 = new Long(4);
		Long l5 = new Long(5);
		

		setA = new HashSet<Long>();
		setB = new HashSet<Long>();
		
		
		setA.add(l1);
		setA.add(l2);
		setA.add(l3);
		
		setB.add(l5);
		setB.add(l4);
		
		
		System.out.println(setA.removeAll(setB));
		*/

		Stat a[] = new Stat[3];
		a[0].addNumber(0);
		
	}
	

	class MyLong {
		public long value;
		
		public MyLong() {
			this.value  = 0;
		}
	}
	
	
	
}
