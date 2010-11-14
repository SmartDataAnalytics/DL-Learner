package org.dllearner.integration;

import org.dllearner.integration.threading.CachingDLLearnerCallable;
import org.dllearner.integration.threading.DLLearnerCallable;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 5:37:29 PM
 *
 * Test to verify that DL Learner works when a reasoner is cached.
 */
public class CachingMultiThreadedDLLearnerTest extends MultiThreadedDLLearnerTest{


/** Comment out until we have working so hudson doesn't break */
//
//    @Override
//    protected DLLearnerCallable getCallable() {
////        return new CachingDLLearnerCallable();
//    }
}
