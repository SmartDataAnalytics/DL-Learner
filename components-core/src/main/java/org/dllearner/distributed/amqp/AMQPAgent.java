package org.dllearner.distributed.amqp;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.qpid.QpidException;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.url.URLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class AMQPAgent extends Thread {

	private static Logger logger = LoggerFactory.getLogger(AMQPAgent.class);

	protected AMQQueue queueOut;
	protected AMQQueue queueIn;
	protected String user = "admin";
	protected String password = "admin";
	protected String host = "localhost";
	protected String clientId = null;
	protected String virtualHost = null;
	protected int port = 5672;
	private AMQConnection connection;
	private boolean transacted = false;
	private org.apache.qpid.jms.Session session;
	private MessageProducer producer;
	private boolean debugLog = true;

	public AMQPAgent() {
		super();
	}

	public AMQPAgent(String user, String password, String hostName,
			String clientID, String virtualHost, Integer port) {
		super();

		this.user = user != null ? user : this.user;
		this.password = password != null ? password : this.password;
		this.host = hostName != null ? hostName : this.host;
		this.clientId = clientID != null ? clientID :this.clientId;
		this.virtualHost = virtualHost != null ? virtualHost : this.virtualHost;
		this.port = port != null ? port : this.port;
	}

	protected void init() throws URLSyntaxException, QpidException, JMSException {
		AMQPConfiguration url = new AMQPConfiguration(user, password, clientId,
				virtualHost, host, port);

		connection = new AMQConnection(url.getURL());
		session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(queueOut);

	}

	protected void terminateWorkers() throws JMSException {
		// TODO: PW: implement
		throw new RuntimeException("Implement me!");
	}

	public void setOutputQueue(String queueName) {
		queueOut = new AMQQueue("amq.direct", queueName);
	}

	public void setInputQueue(String queueName) {
		queueIn = new AMQQueue("amq.direct", queueName);
	}

	public boolean loadAMQPSettings(String propertiesFilePath) {
		Properties props = new Properties();
		try {
			props.load(new FileReader(new File(propertiesFilePath)));
		} catch (IOException e) {
			return false;
		}

		user = props.getProperty("user", user);
		password = props.getProperty("password", password);
		host = props.getProperty("host", host);
		clientId = props.getProperty("clientId", clientId);
		virtualHost = props.getProperty("virtualHost", virtualHost);
		port = props.getProperty("port") == null ? port : Integer.parseInt(
				props.getProperty("port"));
		return true;
	}

	protected void send(MessageContainer msgContainer) {
		Message msg;

		try {
			msg = session.createObjectMessage(msgContainer);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try {
				Monitor mon = MonitorFactory.start("obj_serialization");
				ObjectOutputStream oout = new ObjectOutputStream(bout);
				oout.writeObject(msgContainer);
				oout.flush();
				mon.stop();

				if (debugLog) {
					logger.info("######### MSG CONTAINER SIZE: " + bout.size());
					logger.info("SERIALIZATION DURATION: " + mon.getLastValue());
				}

				oout.close();
				bout.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			producer.send(msg);

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
