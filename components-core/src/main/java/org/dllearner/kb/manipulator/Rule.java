package org.dllearner.kb.manipulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public abstract class Rule {
	
	
	public static final List<Months> MONTHS = new ArrayList<>(Arrays.asList(Months.values()));
	
	public enum Months {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY,
		AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
	}

	Months month;
	
	
	
	
	public Rule(Months month) {
		this.month = month;
	
	}

	
	public abstract  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples);

	
	public static void main(String[] args) {
		System.out.println();
		for (int i = 0; i < Months.values().length; i++) {
			System.out.println(Months.values()[i]);
			
		}
		System.out.println(Arrays.toString(Months.values()));
	}
	
	public abstract void logJamon();
}
