package org.dllearner.distributed.amqp;

import javax.jms.JMSException;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.utilities.datastructures.SearchTree;

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

	protected void startMaster() throws JMSException {
		throw new RuntimeException("Not implemented");
	}

	protected void sendTree(SearchTree<OENode> tree) {
		send(new SearchTreeContainer(tree));
	}
}
