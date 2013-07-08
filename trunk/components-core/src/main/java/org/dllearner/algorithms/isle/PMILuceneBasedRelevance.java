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


package org.dllearner.algorithms.isle;

import org.semanticweb.owlapi.model.OWLOntology;


public class PMILuceneBasedRelevance extends LuceneBasedRelevance{

	/**
	 * @param ontology
	 * @param searcher
	 * @param textRetriever
	 */
	public PMILuceneBasedRelevance(OWLOntology ontology, LuceneSearcher searcher, EntityTextRetriever textRetriever) {
		super(ontology, searcher, textRetriever);
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.LuceneBasedRelevance#computeScore(int, int, int, int)
	 */
	@Override
	public double computeScore(int nrOfDocuments, int nrOfDocumentsA, int nrOfDocumentsB, int nrOfDocumentsAB) {
		double dPClass = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsA / (double) nrOfDocuments);
		double dPClassEntity = nrOfDocumentsB == 0 ? 0 : (double) nrOfDocumentsAB / (double) nrOfDocumentsB;
		double pmi = Math.log(dPClassEntity / dPClass);
		return pmi;
	}
}