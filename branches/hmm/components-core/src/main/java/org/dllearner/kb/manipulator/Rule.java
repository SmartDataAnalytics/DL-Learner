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

package org.dllearner.kb.manipulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public abstract class Rule {
	
	
	public static final List<Months> MONTHS = new ArrayList<Months>(Arrays.asList(Months.values()));
	
	public enum Months {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY,
		AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
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
		System.out.println(Months.values());
	}
	
	public abstract void logJamon();
}
