/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.learningproblems;

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.configurators.PosOnlyInclusionLPConfigurator;
import org.dllearner.core.owl.Description;

/**
 * @author Jens Lehmann
 *
 */
public class PosOnlyInclusionLP extends PosOnlyLP implements InclusionLP {

	private PosOnlyInclusionLPConfigurator configurator;
	@Override
	public PosOnlyInclusionLPConfigurator getConfigurator(){
		return configurator;
	}
	
	
	public PosOnlyInclusionLP(ReasonerComponent reasoningService) {
		super(reasoningService);
		this.configurator = new PosOnlyInclusionLPConfigurator(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "pos only inclusion learning problem";
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblemNew#computeScore(org.dllearner.core.dl.Concept)
	 */
	@Override
	public Score computeScore(Description concept) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

}
