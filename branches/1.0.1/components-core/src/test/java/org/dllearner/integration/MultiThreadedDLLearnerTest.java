package org.dllearner.integration;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.integration.threading.DLLearnerCallable;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 11:05:38 AM
 * <p/>
 * Test the Simple DL Learner Use Case in multithreaded mode.
 */
public class MultiThreadedDLLearnerTest extends SingleThreadedDLLearnerTest {


    private final int threadCount = 20;

    @Override
    @Test
    public void runTest() throws Exception {
        DLLearnerCallable callable = getCallable();

        ExecutorService executor = new ThreadPoolExecutor(4, 8, 500, TimeUnit.MILLISECONDS, new LinkedBlockingDeque());

        Collection<FutureTask<EvaluatedDescription>> tasks = new ArrayList<FutureTask<EvaluatedDescription>>();

        /** Initialize the tasks */
        for (int ctr = 0; ctr < threadCount; ctr++) {
            FutureTask<EvaluatedDescription> ft = new FutureTask<EvaluatedDescription>(callable);
            tasks.add(ft);
        }

        /** Execute them all */
        for (FutureTask<EvaluatedDescription> task : tasks) {
            executor.execute(task);
        }

        int ctr = 0;
        /** Get the results and print them */
        for (FutureTask<EvaluatedDescription> task : tasks) {

            EvaluatedDescriptionPosNeg eDPN = (EvaluatedDescriptionPosNeg)task.get();
            System.out.println("Task " + ctr + " Complete, results:");
            ctr++;
            validateResults(eDPN);
        }

    }

    protected DLLearnerCallable getCallable(){
        return new DLLearnerCallable();
}

}
