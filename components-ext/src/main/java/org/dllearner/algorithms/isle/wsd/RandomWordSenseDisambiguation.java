/**
 * Copyright (C) 2007-2013, Jens Lehmann
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
package org.dllearner.algorithms.isle.wsd;

import java.util.Random;
import java.util.Set;

import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.EntityScorePair;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Disambiguation by randomly selecting one of the candidates (baseline method).
 * 
 * @author Jens Lehmann
 *
 */
public class RandomWordSenseDisambiguation extends WordSenseDisambiguation {

	private Random random;
	
	public RandomWordSenseDisambiguation(OWLOntology ontology) {
		super(ontology);
		random = new Random();
	}

	@Override
	public SemanticAnnotation disambiguate(Annotation annotation,
			Set<EntityScorePair> candidateEntities) {
		int pos = random.nextInt(candidateEntities.size());
		int i = 0;
		for(EntityScorePair esp : candidateEntities) {
            OWLEntity e = esp.getEntity();
            if (i == pos) {
                return new SemanticAnnotation(annotation, e);
            }
            i++;
        }
        return null;
	}

}
