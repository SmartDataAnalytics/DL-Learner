package org.dllearner.algorithms.refinement;

import java.util.Comparator;

import org.dllearner.Config;
import org.dllearner.utilities.ConceptComparator;

/**
 * 
 * Die zweite Heuristik ist flexibel, das sie einen Tradeoff zwischen
 * prozentualer Richtigkeit und horizontal expansion bietet (die
 * Standardheuristik ist lexikographisch, d.h. ein schlecht klassifizierendes
 * Konzept wird nie vorgezogen).
 * 
 * @author jl
 *
 */
public class NodeComparator2 implements Comparator<Node> {

	// Vergleich von Konzepten, falls alle anderen Kriterien fehlschlagen
	ConceptComparator conceptComparator = new ConceptComparator();
	int nrOfNegativeExamples;
	// 5% sind eine Verlängerung um 1 wert
	// double percentPerLengthUnit = 0.05;
	
	public NodeComparator2(int nrOfNegativeExamples) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
	}
	
	// implementiert einfach die Definition in der Diplomarbeit
	public int compare(Node n1, Node n2) {
		
		// sicherstellen, dass Qualität ausgewertet wurde
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated() && !n1.isTooWeak() && !n2.isTooWeak()) {
			
			// alle scores sind negativ, größere scores sind besser
			double score1 = -n1.getCoveredNegativeExamples()/(double)nrOfNegativeExamples;
			score1 -= Config.percentPerLengthUnit * n1.getConcept().getLength();
			
			double score2 = -n2.getCoveredNegativeExamples()/(double)nrOfNegativeExamples;
			score2 -= Config.percentPerLengthUnit * n2.getConcept().getLength();
			
			double diff = score1 - score2;
			
			if(diff>0)
				return 1;
			else if(diff<0)
				return -1;
			else
				return conceptComparator.compare(n1.getConcept(), n2.getConcept());
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality or are too weak.");
	}

	@Override		
	public boolean equals(Object o) {
		return (o instanceof NodeComparator2);
	}
	
}
