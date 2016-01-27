package org.dllearner.distributed.amqp;

import javax.jms.JMSException;

import org.apache.qpid.QpidException;
import org.apache.qpid.url.URLSyntaxException;

public abstract class MasterThread extends AMQPAgent {
	@Override
	public void run() {
		try {
			startMaster();
		} catch (JMSException e) {
			e.printStackTrace();
		}

		try {
			terminateWorkers();
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
		System.exit(1);
	}

	@Override
	public void init() throws URLSyntaxException, QpidException, JMSException {
		setOutputQueue(AMQPAgent.messagesToProcessQueue);
		setInputQueue(AMQPAgent.processedMessagesQueue);
		super.init();
	}

	@Override
	public String toString() {
		return "Master (" + myID + ")";
	}

	protected abstract void startMaster() throws JMSException;

	protected void sendTree(SearchTree tree, int minHorizExp, int maxHorizExp) {
		send(new SearchTreeContainer(tree, minHorizExp, maxHorizExp));
	}
}
