/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.kb.dataset;

import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;

/**
 * A collection of OWL ontologies that have been analyzed w.r.t. incoherency and inconsistency.
 */
public interface AnalyzedOWLOntologyDataset extends OWLOntologyDataset{

	/**
	 * @return the collection of incoherent OWL ontologies
	 */
	Collection<OWLOntology> loadIncoherentOntologies();

	/**
	 * @return the collection of inconsistent OWL ontologies
	 */
	Collection<OWLOntology> loadInconsistentOntologies();

}
