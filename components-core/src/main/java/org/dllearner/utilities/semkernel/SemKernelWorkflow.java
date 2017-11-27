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
package org.dllearner.utilities.semkernel;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;

/**
 * Since the current setup for running a SemKernel example comprises several
 * steps, like preparing the training data, do the training, preparing the
 * prediction data and so on, this component is intended to encapsulate this
 * whole process and make it callable and configurable via the standard
 * DL-Learner CLI.
 * As already said, there are different steps, depending on the tasks to solve
 *
 * - training:
 *   T1) read URIs to train
 *   T2) read the underlying knowledge base
 *   T3) write the prepared training data (in SVM light format) to the training
 *       data directory
 *   T4) do the training run and write out the training model to the model
 *       directory
 *
 * - prediction
 *   P5) read URIs to predict
 *   P6) read the underlying knowledge base (if not done already)
 *   P7) write out the prepared prediction data to the prediction data directory
 *   P8) do the prediction and write out the prediction results to the result
 *       directory
 *
 * @author Patrick Westphal
 *
 */
@ComponentAnn(name="SemKernel Workflow", shortName="skw", version=0.1)
public class SemKernelWorkflow extends AbstractComponent {

    @Override
    public void init() throws ComponentInitException {
    	initialized = true;
    }

    public void start() {

    }

}
