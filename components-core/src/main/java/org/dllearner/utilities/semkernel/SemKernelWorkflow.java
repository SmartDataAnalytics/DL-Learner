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
        // TODO Auto-generated method stub

    }

    public void start() {

    }

}
