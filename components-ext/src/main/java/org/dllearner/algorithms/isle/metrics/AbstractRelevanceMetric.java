/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Lorenz Buehmann
 * 
 */
public abstract class AbstractRelevanceMetric implements RelevanceMetric {

	protected Index index;
	protected String name;

	public AbstractRelevanceMetric(Index index) {
		this.index = index;

		name = getClass().getSimpleName().replace("RelevanceMetric", "");
	}

	public static Map<OWLEntity, Double> normalizeMinMax(Map<OWLEntity, Double> hmEntity2Score) {
		Map<OWLEntity, Double> hmEntity2Norm = new HashMap<>();

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		OWLEntity minE=null;
		OWLEntity maxE=null;
		for (OWLEntity e : hmEntity2Score.keySet()) {
			double value = hmEntity2Score.get(e);
			if (value < min) {
				min = value;minE = e;
			} else if (value > max) {
				max = value;maxE = e;
			}
		}
//		System.err.println("Max: " + max + "-" + maxE);
//		System.err.println("Min: " + min + "-" + minE);
		// System.out.println( "min="+ dMin +" max="+ dMax );
		for (OWLEntity e : hmEntity2Score.keySet()) {
			double value = hmEntity2Score.get(e);
			double normalized = 0;
			if (min == max) {
				normalized = value;
				normalized = 0.5;
			} else {
				normalized = (value - min) / (max - min);
			}
			hmEntity2Norm.put(e, normalized);
		}
		return hmEntity2Norm;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public double getRelevance(OWLEntity entity, OWLClassExpression desc){
		Set<OWLEntity> entities = desc.getSignature();
		double score = 0;
		for (OWLEntity otherEntity : entities) {
			double relevance = getRelevance(entity, otherEntity);
			if(!Double.isInfinite(relevance)){
				score += relevance/entities.size();
			}
		}
		return score;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractRelevanceMetric other = (AbstractRelevanceMetric) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	

}
