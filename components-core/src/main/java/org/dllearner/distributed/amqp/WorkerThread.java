package org.dllearner.distributed.amqp;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.qpid.QpidException;
import org.apache.qpid.url.URLSyntaxException;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class WorkerThread extends AMQPAgent {

	private int runtimeBase = 10;

	@Override
	public void run() {
		while (true) {
			if (checkTerminateMsg()) {
				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void init() throws URLSyntaxException, QpidException, JMSException {
		setOutputQueue(AMQPAgent.processedMessagesQueue);
		setInputQueue(AMQPAgent.messagesToProcessQueue);
		super.init();
	}

	@Override
	public String toString() {
		return "Worker " + myID;
	}

	public boolean checkTerminateMsg() {
		Message msg;

		try {
			msg = termMsgSubscriber.receive(1000);
		} catch (JMSException e) {
			e.printStackTrace();
			return false;
		}

		if (msg != null) {
			return true;
		} else {
			/* since receive just waits for 1000 ms and cancels receiving then,
			 * msg might be null. In this case the worker should not terminate
			 */
			return false;
		}
	}

	public void setWorkerRuntimeBase(int runtimeInSecs) {
		runtimeBase = runtimeInSecs;
	}

	public int getRuntimeBase() {
		return runtimeBase;
	}

	protected void sendResults(SearchTree tree, double bestAccuracy,
			OWLClassExpression bestDescription,
			EvaluatedDescriptionSet bestEvaluatedDescriptions, int numTests,
			int minHorizExp, int maxHorizExp) {

		send(new SearchTreeContainer(tree, bestAccuracy, bestDescription,
				bestEvaluatedDescriptions, numTests, minHorizExp, maxHorizExp));
	}
}
