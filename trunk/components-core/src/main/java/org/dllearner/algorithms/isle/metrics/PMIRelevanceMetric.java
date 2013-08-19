/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import com.google.common.collect.Sets;
import org.dllearner.algorithms.isle.index.Document;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.core.owl.Entity;

import java.util.Set;

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
		Set<Document> documentsA = index.getDocuments(entityA);
		Set<Document> documentsB = index.getDocuments(entityB);
		Set<Document> documentsAB = Sets.intersection(documentsA, documentsB);
		int nrOfDocuments = index.getSize();
		
		double dPClass = nrOfDocuments == 0 ? 0 : ((double) documentsA.size() / (double) nrOfDocuments);
		double dPClassEntity = documentsB.size() == 0 ? 0 : (double) documentsAB.size() / (double) documentsB.size();
		double pmi = Math.log(dPClassEntity / dPClass);
		
		return pmi;
	}

}
