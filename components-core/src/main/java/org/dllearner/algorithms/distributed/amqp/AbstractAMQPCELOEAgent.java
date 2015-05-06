package org.dllearner.algorithms.distributed.amqp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.qpid.AMQException;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.url.URLSyntaxException;
import org.dllearner.algorithms.distributed.MessageContainer;
import org.dllearner.algorithms.distributed.MessagingAgent;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;

public abstract class AbstractAMQPCELOEAgent extends AbstractCELA implements MessagingAgent{

    // default AMPQ values
    private String user = "admin";
    private String password = "admin";
    private String host = "localhost";
    private String clientId = null;
    private String virtualHost = null;
    private int port = 5672;
    private final String masterDefaultSendQueueIdentifier = "request send";
    private final String masterDefaultReceiveQueueIdentifier = "response receive";
    private final String workerDefaultSendQueueIdentifier = "response send";
    private final String workerDefaultReceiveQueueIdentifier = "request receive";

    private int myID;

    // connection, session, queue, consumers, ...
    private Connection connection;
    private Session session;
    // for sending requests (master)/responses (worker)
    private String sendQueueIdentifier;
    private AMQQueue sendQueue;
    private MessageProducer sender;
    // for receiving responses (master)/requests (worker)
    private String receiveQueueIdentifier;
    private AMQQueue receiveQueue;
    private MessageConsumer receiver;
    // channel to send/receive terminate messages
    private String termRoutingKey = "terminate";
    private Topic termTopic;
    private TopicSubscriber termMsgSubscriber;
    private MessageProducer termMsgPublisher;

    // AMQP settings
    private boolean transactional = false;
    private boolean isMaster = false;


    public AbstractAMQPCELOEAgent(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
        super(problem, reasoner);
    }

    public AbstractAMQPCELOEAgent() {
    }

    @Override
    public void initMessaging() throws URLSyntaxException, AMQException, JMSException {
        // just in case the messaging agent's ID is not set
        if (myID == 0) {
            myID = (new Random()).nextInt(100000);
        }

        AMQConfiguration url = new AMQConfiguration(user, password, null, null, host, port);
        connection = new AMQConnection(url.toString());
        connection.start();
        session = connection.createSession(transactional, Session.AUTO_ACKNOWLEDGE);

        if (isMaster) initMasterQueues();
        else initWorkerQueues();

        termTopic = new AMQTopic((AMQConnection) connection, termRoutingKey);
        termMsgSubscriber = session.createDurableSubscriber(termTopic, Integer.toString(myID));
        termMsgPublisher = session.createProducer(termTopic);
    }

    @Override
    public void finalizeMessaging() throws JMSException {
        session.close();
        connection.close();
    }

    @Override
    public void blockingSend(MessageContainer msgContainer) throws JMSException {
        Message msg = ((org.apache.qpid.jms.Session) session).createObjectMessage(msgContainer);
        sender.send(msg);
    }

    @Override
    public void nonBlockingSend(MessageContainer msgContainer) throws JMSException {
        Message msg = ((org.apache.qpid.jms.Session) session).createObjectMessage(msgContainer);
        CompletionListener completionListener = new DummyCompletionListener();
        sender.send(msg, completionListener);
    }

    @Override
    public MessageContainer blockingReceive() throws JMSException {
        ObjectMessage msg = (ObjectMessage) receiver.receive();
        MessageContainer msgContainer = (MessageContainer) msg.getObject();

        return msgContainer;
    }

    @Override
    public MessageContainer nonBlockingReceive() throws JMSException {
        ObjectMessage msg = (ObjectMessage) receiver.receiveNoWait();

        if (msg == null) {
            return null;
        } else {
            MessageContainer msgContainer = (MessageContainer) msg.getObject();

            return msgContainer;
        }
    }

    @Override
    public void terminateAgents() throws JMSException {
        termMsgPublisher.send(session.createBytesMessage());
    }

    @Override
    public boolean checkTerminateMsg() throws JMSException {
        Message msg = termMsgSubscriber.receiveNoWait();

        if (msg != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setAgentID(int id) {
        myID = id;
    }

    @Override
    public int getAgentID() {
        return myID;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    // <------------------------ non-interface methods ----------------------->
    public boolean updateAMQPSettings(String propertiesFilePath) {
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

    public void setSendQueueIdentifier(String queueIdentifier) {
        sendQueueIdentifier = queueIdentifier;
    }

    public void setReceiveQueueIdentifier(String queueIdentifier) {
        receiveQueueIdentifier = queueIdentifier;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public void setMaster() {
        isMaster = true;
    }

    private void initMasterQueues() throws JMSException {
        if (sendQueueIdentifier == null) sendQueueIdentifier = masterDefaultSendQueueIdentifier;
        if (receiveQueueIdentifier == null) receiveQueueIdentifier = masterDefaultReceiveQueueIdentifier;

        initQueues();
    }

    private void initWorkerQueues() throws JMSException {
        if (sendQueueIdentifier == null)
            sendQueueIdentifier = workerDefaultSendQueueIdentifier;

        if (receiveQueueIdentifier == null)
            receiveQueueIdentifier = workerDefaultReceiveQueueIdentifier;

        initQueues();
    }

    private void initQueues() throws JMSException {
        sendQueue = new AMQQueue((AMQConnection) connection, sendQueueIdentifier);
        sender = session.createProducer(sendQueue);

        receiveQueue = new AMQQueue((AMQConnection) connection, receiveQueueIdentifier);
        receiver = session.createConsumer(receiveQueue);
    }
 }
