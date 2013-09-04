/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.Set;

import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.core.owl.Entity;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class PMIRelevanceMetric extends AbstractRelevanceMetric {

	public PMIRelevanceMetric(SemanticIndex index) {
		super(index);
	}

	@Override
	public double getRelevance(Entity entityA, Entity entityB){
		Set<AnnotatedDocument> documentsA = index.getDocuments(entityA);
		Set<AnnotatedDocument> documentsB = index.getDocuments(entityB);
		Set<AnnotatedDocument> documentsAB = Sets.intersection(documentsA, documentsB);
		int nrOfDocuments = index.getSize();
		
		double pA = nrOfDocuments == 0 ? 0 : ((double) documentsA.size() / (double) nrOfDocuments);
		double pB = nrOfDocuments == 0 ? 0 : ((double) documentsB.size() / (double) nrOfDocuments);
		double pAB = nrOfDocuments == 0 ? 0 : ((double) documentsAB.size() / (double) nrOfDocuments);
		
		double pmi = Math.log(pAB / pA * pB);
		
		return pmi;
	}
	
	@Override
	public double getNormalizedRelevance(Entity entityA, Entity entityB){
		Set<AnnotatedDocument> documentsA = index.getDocuments(entityA);
		Set<AnnotatedDocument> documentsB = index.getDocuments(entityB);
		Set<AnnotatedDocument> documentsAB = Sets.intersection(documentsA, documentsB);
		int nrOfDocuments = index.getSize();
		
		double pA = nrOfDocuments == 0 ? 0 : ((double) documentsA.size() / (double) nrOfDocuments);
		double pB = nrOfDocuments == 0 ? 0 : ((double) documentsB.size() / (double) nrOfDocuments);
		double pAB = nrOfDocuments == 0 ? 0 : ((double) documentsAB.size() / (double) nrOfDocuments);
		
		if(pA * pB == 0){
			return 0;
		}
		double pmi = Math.log(pAB / pA * pB);
		
		double normalizedPMI = (pmi/-Math.log(pAB) + 1)/2;
		
		return normalizedPMI;
	}

}
