/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.Set;

import org.dllearner.algorithms.isle.index.SemanticIndex;
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
		Set<String> documentsA = index.getDocuments(entityA);
		Set<String> documentsB = index.getDocuments(entityB);
		Set<String> documentsAB = Sets.intersection(documentsA, documentsB);
		int nrOfDocuments = index.getSize();
		
		double dPClass = nrOfDocuments == 0 ? 0 : ((double) documentsA.size() / (double) nrOfDocuments);
		double dPClassEntity = documentsB.size() == 0 ? 0 : (double) documentsAB.size() / (double) documentsB.size();
		double pmi = Math.log(dPClassEntity / dPClass);
		
		return pmi;
	}

}
