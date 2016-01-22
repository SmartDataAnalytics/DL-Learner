package org.dllearner.distributed.amqp;

import javax.jms.DeliveryMode;

import org.apache.qpid.client.AMQQueue;

public class WorkerThread extends AMQPAgent {

	private String processedTreesQueue;

	@Override
	public void init() {


		setOutputQueue(processedTreesQueue);
		amqpProducer = amqpSession.createProducer(amqpOutQueue);
		amqpProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		amqpInQueue = new AMQQueue("amq.direct", treesToProcessQueue);
		amqpConsumer = amqpSession.createConsumer(amqpInQueue);
		amqpConsumer.setMessageListener(new WorkerMessageListener());
	}

}
