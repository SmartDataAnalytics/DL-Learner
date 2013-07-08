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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;


public abstract class LuceneBasedRelevance implements Relevance{
	
	private EntityTextRetriever textRetriever;
	private LuceneSearcher searcher;
	private OWLOntology ontology;
	private Set<OWLEntity> entities;
	
//	public void printScores() throws Exception {
//		for( OWLClass c: m_classes )
//		{
//			Map<OWLEntity,Double> hmEntity2Score = getEntityRelevance(c);
//			// normalization per class?
//			hmEntity2Score = normalize( hmEntity2Score );
//			for( OWLEntity e : hmEntity2Score.keySet() )
//			{
//				double dScore = hmEntity2Score.get(e);
//				System.out.println( "P( "+ getLabel(c) +", "+ getLabel(e) +" ) = "+ dScore );
//			}
//		}		
//		m_searcher.close();
//	}

	public LuceneBasedRelevance(OWLOntology ontology, LuceneSearcher searcher, EntityTextRetriever textRetriever) {
		this.searcher = searcher;
		this.ontology = ontology;
		this.textRetriever = textRetriever;
		
		entities = new HashSet<OWLEntity>();
		entities.addAll(ontology.getClassesInSignature());
		entities.addAll(ontology.getObjectPropertiesInSignature());
		entities.addAll(ontology.getDataPropertiesInSignature());
	}
	
	public Map<OWLEntity,Double> normalizeMinMax( Map<OWLEntity,Double> hmEntity2Score ){
		Map<OWLEntity,Double> hmEntity2Norm = new HashMap<OWLEntity,Double>();
		double dMin = Double.MAX_VALUE;
		Double dMax = Double.MIN_VALUE;
		for( OWLEntity e : hmEntity2Score.keySet() )
		{
			double dValue = hmEntity2Score.get(e);
			if( dValue < dMin ){
				dMin = dValue;
			}
			else if( dValue > dMax ){
				dMax = dValue;
			}
		}
		// System.out.println( "min="+ dMin +" max="+ dMax );
		for( OWLEntity e : hmEntity2Score.keySet() )
		{
			double dValue = hmEntity2Score.get(e);
			double dNorm = 0;
			if( dMin == dMax ){
				dNorm = dValue;
			} 
			else {
				dNorm = ( dValue - dMin ) / ( dMax - dMin );
			}
			hmEntity2Norm.put( e, dNorm );
		}
		return hmEntity2Norm;
	}
	
	@Override
	public Map<Entity,Double> getEntityRelevance(Entity entity) throws Exception {
		// computes relevance of entity for this class
		// conditional probability: P(C,E)=f(C,E)/f(E)
		// PMI(C,E)=log( P(C,E) / P(C) )
		Map<Entity, Double> hmEntity2Score = new HashMap<Entity, Double>();
		Map<String, Double> relevantText = textRetriever.getRelevantText(entity);
		
		for (Entry<String, Double> entry : relevantText.entrySet()) {
			String text = entry.getKey();
			Double value = entry.getValue();

			String sClass = text;
			int nrOfDocumentsA = searcher.count(sClass);
			int nrOfDocuments = searcher.indexSize();
			
			for (OWLEntity otherEntity : entities) {

				Map<String, Double> otherRelevantText = textRetriever.getRelevantText(OWLAPIConverter
						.getEntity(otherEntity));

				for (Entry<String, Double> entry2 : otherRelevantText.entrySet()) {
					String otherText = entry2.getKey();
					Double otherValue = entry2.getValue();

					String sEntity = otherText;
					int nrOfDocumentsB = searcher.count(sEntity);
					int nrOfDocumentsAB = searcher.count(sClass + " AND " + sEntity);
					// double dPEntity = (double)iEntity / (double)iAll;
					
					double score = computeScore(nrOfDocuments, nrOfDocumentsA, nrOfDocumentsB, nrOfDocumentsAB);
					if (!Double.isNaN(score)){// && !Double.isInfinite(score)) {
						hmEntity2Score.put(OWLAPIConverter.getEntity(otherEntity), score);
					}
				}
			}
		}
		
		return hmEntity2Score;
	}
	
	/**
	 * Computes the score which is returned in {@link org.dllearner.algorithms.isle.LuceneBasedRelevance#getEntityRelevance} 
	 * @return
	 */
	public abstract double computeScore(int nrOfDocuments, int nrOfDocumentsA, int nrOfDocumentsB, int nrOfDocumentsAB);
	
}