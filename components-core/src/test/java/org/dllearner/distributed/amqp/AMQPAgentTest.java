package org.dllearner.distributed.amqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AMQPAgentTest {

	String dir = "src/test/resources/org/dllearner/distributed/amqp/";

	@Test
	public void TestConstructor01() {
		AMQPAgent dummy = new AMQPAgent();
		String defaultUser = dummy.user;
		String defaultPW = dummy.password;
		String defaultHost = dummy.host;
		String defaultClientID = dummy.clientId;
		String defaultVirtHost = dummy.virtualHost;
		int defaultPort = dummy.port;

		AMQPAgent agent = new AMQPAgent(null, null, null, null, null, null);

		assertEquals(defaultUser, agent.user);
		assertEquals(defaultPW, agent.password);
		assertEquals(defaultHost, agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void TestConstructor02() {
		String user = "abc";

		AMQPAgent dummy = new AMQPAgent();
		String defaultPW = dummy.password;
		String defaultHost = dummy.host;
		String defaultClientID = dummy.clientId;
		String defaultVirtHost = dummy.virtualHost;
		int defaultPort = dummy.port;

		AMQPAgent agent = new AMQPAgent(user, null, null, null, null, null);

		assertEquals(user, agent.user);
		assertEquals(defaultPW, agent.password);
		assertEquals(defaultHost, agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void TestConstructor03() {
		String password = "def";

		AMQPAgent dummy = new AMQPAgent();
		String defaultUser = dummy.user;
		String defaultHost = dummy.host;
		String defaultClientID = dummy.clientId;
		String defaultVirtHost = dummy.virtualHost;
		int defaultPort = dummy.port;

		AMQPAgent agent = new AMQPAgent(null, password, null, null, null, null);

		assertEquals(defaultUser, agent.user);
		assertEquals(password, agent.password);
		assertEquals(defaultHost, agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void TestConstructor04() {
		String hostName = "somehost";

		AMQPAgent dummy = new AMQPAgent();
		String defaultUser = dummy.user;
		String defaultPW = dummy.password;
		String defaultClientID = dummy.clientId;
		String defaultVirtHost = dummy.virtualHost;
		int defaultPort = dummy.port;

		AMQPAgent agent = new AMQPAgent(null, null, hostName, null, null, null);

		assertEquals(defaultUser, agent.user);
		assertEquals(defaultPW, agent.password);
		assertEquals(hostName, agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void TestConstructor05() {
		String clientID = "ghi";

		AMQPAgent dummy = new AMQPAgent();
		String defaultUser = dummy.user;
		String defaultPW = dummy.password;
		String defaultHost = dummy.host;
		String defaultVirtHost = dummy.virtualHost;
		int defaultPort = dummy.port;

		AMQPAgent agent = new AMQPAgent(null, null, null, clientID, null, null);

		assertEquals(defaultUser, agent.user);
		assertEquals(defaultPW, agent.password);
		assertEquals(defaultHost, agent.host);
		assertEquals(clientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void TestConstructor06() {
		String virtualHost = "develop";

		AMQPAgent dummy = new AMQPAgent();
		String defaultUser = dummy.user;
		String defaultPW = dummy.password;
		String defaultHost = dummy.host;
		String defaultClientID = dummy.clientId;
		int defaultPort = dummy.port;

		AMQPAgent agent = new AMQPAgent(null, null, null, null, virtualHost, null);

		assertEquals(defaultUser, agent.user);
		assertEquals(defaultPW, agent.password);
		assertEquals(defaultHost, agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(virtualHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void TestConstructor07() {
		int port = 2342;

		AMQPAgent dummy = new AMQPAgent();
		String defaultUser = dummy.user;
		String defaultPW = dummy.password;
		String defaultHost = dummy.host;
		String defaultClientID = dummy.clientId;
		String defaultVirtHost = dummy.virtualHost;

		AMQPAgent agent = new AMQPAgent(null, null, null, null, null, port);

		assertEquals(defaultUser, agent.user);
		assertEquals(defaultPW, agent.password);
		assertEquals(defaultHost, agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(port, agent.port);
	}

	@Test
	public void TestConstructor08() {
		String user = "abc";
		String password = "def";
		String hostName = "somehost";
		String clientID = "ghi";
		String virtualHost = "develop";
		int port = 2342;

		AMQPAgent agent = new AMQPAgent(user, password, hostName, clientID,
				virtualHost, port);

		assertEquals(user, agent.user);
		assertEquals(password, agent.password);
		assertEquals(hostName, agent.host);
		assertEquals(clientID, agent.clientId);
		assertEquals(virtualHost, agent.virtualHost);
		assertEquals(port, agent.port);
	}

	@Test
	public void testSetOutputQueue() {
		AMQPAgent agent = new AMQPAgent();
		assertNull(agent.queueOut);

		String queueName = "abc";
		agent.setOutputQueue(queueName);

		assertEquals(queueName, agent.queueOut.getAMQQueueName());
	}

	@Test
	public void testSetInputQueue() {
		AMQPAgent agent = new AMQPAgent();
		assertNull(agent.queueOut);

		String queueName = "abc";
		agent.setInputQueue(queueName);

		assertEquals(queueName, agent.queueIn.getAMQQueueName());
	}

	@Test
	public void testLoadAMQPSettings01() {
		String propertiesFilePath = dir + "amqp_conf_01.properties";
		AMQPAgent agent = new AMQPAgent();

		boolean wasSuccessful = agent.loadAMQPSettings(propertiesFilePath);

		assertEquals(true, wasSuccessful);

		assertEquals("abc", agent.user);
		assertEquals("def", agent.password);
		assertEquals("oklasos", agent.host);
		assertEquals("ghi", agent.clientId);
		assertEquals("development", agent.virtualHost);
		assertEquals(2345, agent.port);
	}

	@Test
	public void testLoadAMQPSettings02() {
		String propertiesFilePath = dir + "amqp_conf_02.properties";
		AMQPAgent agent = new AMQPAgent();

		String defaultUser = agent.user;
		String defaultClientID = agent.clientId;
		String defaultVirtHost = agent.virtualHost;
		int defaultPort = agent.port;

		boolean wasSuccessful = agent.loadAMQPSettings(propertiesFilePath);

		assertEquals(true, wasSuccessful);

		assertEquals(defaultUser, agent.user);
		assertEquals("def", agent.password);
		assertEquals("oklasos", agent.host);
		assertEquals(defaultClientID, agent.clientId);
		assertEquals(defaultVirtHost, agent.virtualHost);
		assertEquals(defaultPort, agent.port);
	}

	@Test
	public void testLoadAMQPSettings03() {
		String propertiesFilePath = dir +
				"amqp_conf_does_not_exist.properties";
		AMQPAgent agent = new AMQPAgent();

		boolean wasSuccessful = agent.loadAMQPSettings(propertiesFilePath);

		assertEquals(false, wasSuccessful);
	}
}
