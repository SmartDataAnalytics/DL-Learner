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
		
		double dPClass = nrOfDocuments == 0 ? 0 : ((double) documentsA.size() / (double) nrOfDocuments);
		double dPClassEntity = documentsB.size() == 0 ? 0 : (double) documentsAB.size() / (double) documentsB.size();
		double pmi = Math.log(dPClassEntity / dPClass);
		
		return pmi;
	}

}
