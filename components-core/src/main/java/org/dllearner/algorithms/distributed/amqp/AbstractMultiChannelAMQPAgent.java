package org.dllearner.algorithms.distributed.amqp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

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
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.url.URLSyntaxException;
import org.dllearner.algorithms.distributed.MultiChannelMessagingAgent;
import org.dllearner.algorithms.distributed.containers.MessageContainer;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultiChannelAMQPAgent extends AbstractCELA
        implements MultiChannelMessagingAgent{

    private Logger logger = LoggerFactory.getLogger(AbstractMultiChannelAMQPAgent.class);

    // default AMPQ values
    private String user = "admin";
    private String password = "admin";
    private String host = "localhost";
    private String clientId = null;
    private String virtualHost = null;
    private int port = 5672;
    private List<String> topicIdentifiers;

    // misc
    protected int myID;
    private int receivedMessagesCount;
    private int sentMessagesCount;

    // connection, session, queue, consumers, ...
    private Connection connection;
    private Session session;
    private Map<String, MessageProducer> senders;
    private Map<String, MessageConsumer> receivers;
    // channel to send/receive terminate messages
    private String termRoutingKey = "terminate";
    private Topic termTopic;
    private TopicSubscriber termMsgSubscriber;
    private MessageProducer termMsgPublisher;

    // AMQP settings
    private boolean transactional = false;
    private boolean isMaster = false;
    private long maxBlockingWaitMilisecs = 5000;


    public AbstractMultiChannelAMQPAgent(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
        super(problem, reasoner);
        topicIdentifiers = new ArrayList<String>();
        senders = new HashMap<String, MessageProducer>();
        receivers = new HashMap<String, MessageConsumer>();
    }

    public AbstractMultiChannelAMQPAgent() {
        topicIdentifiers = new ArrayList<String>();
        senders = new HashMap<String, MessageProducer>();
        receivers = new HashMap<String, MessageConsumer>();
    }

    @Override
    public void initMessaging() throws URLSyntaxException, AMQException, JMSException {
        // just in case the messaging agent's ID is not set
        if (myID == 0) {
            myID = (new Random()).nextInt(100000);
        }

        AMQConfiguration url = new AMQConfiguration(user, password, null, null, host, port);
        connection = new AMQConnection(url.getURL());
        connection.start();
        session = connection.createSession(transactional, Session.AUTO_ACKNOWLEDGE);

        senders = new HashMap<String, MessageProducer>();
        receivers = new HashMap<String, MessageConsumer>();
        initTopics();

        termTopic = new AMQTopic((AMQConnection) connection, termRoutingKey);
        termMsgSubscriber = session.createDurableSubscriber(termTopic, Integer.toString(myID));
        termMsgPublisher = session.createProducer(termTopic);

        sentMessagesCount = 0;
        receivedMessagesCount = 0;
    }

    @Override
    public void finalizeMessaging() throws JMSException {
        session.unsubscribe(Integer.toString(myID));

        for (String topicID : topicIdentifiers) {
            session.unsubscribe(topicID);
        }

        session.close();
        connection.close();
    }

    @Override
    public void blockingSend(MessageContainer msgContainer, String queueID) throws JMSException {
        logSend(msgContainer.toString(), queueID, true);
        Message msg = ((org.apache.qpid.jms.Session) session).createObjectMessage(msgContainer);
        senders.get(queueID).send(msg);

        sentMessagesCount++;
    }

    @Override
    public void nonBlockingSend(MessageContainer msgContainer, String queueID) throws JMSException {
        logSend(msgContainer.toString(), queueID, false);
        Message msg = ((org.apache.qpid.jms.Session) session).createObjectMessage(msgContainer);
//        CompletionListener completionListener = new DummyCompletionListener();
        /* FIXME: asynchronous sending is not implemented in the default message
         * producers:
         * Exception in thread "main" java.lang.AbstractMethodError: org.apache.qpid.client.BasicMessageProducer_0_10.send(Ljavax/jms/Message;Ljavax/jms/CompletionListener;)V
         * at org.dllearner.algorithms.distributed.amqp.AbstractAMQPCELOEAgent.nonBlockingSend(AbstractAMQPCELOEAgent.java:118)
         * at org.dllearner.algorithms.distributed.amqp.DistScoreCELOEAMQP.startMaster(DistScoreCELOEAMQP.java:646)
         * at org.dllearner.algorithms.distributed.amqp.DistScoreCELOEAMQP.start(DistScoreCELOEAMQP.java:560)
         * at org.dllearner.algorithms.distributed.amqp.DistScoreCELOEAMQP.main(DistScoreCELOEAMQP.java:1509)
         *
         * TODO: check if we really need this async stuff here; I guess async
         * just means non-blocking delivery to the queue, not to the receiver
         */
//        sender.send(msg, completionListener);
        senders.get(queueID).send(msg);

        sentMessagesCount++;
    }

    @Override
    public MessageContainer blockingReceive(String queueID) throws JMSException {
        logReceive(queueID, true);
        // FIXME: find a better solution than using a timeout
        ObjectMessage msg =
                (ObjectMessage) receivers.get(queueID).receive(maxBlockingWaitMilisecs);

        if (msg == null) return null;

        MessageContainer msgContainer = (MessageContainer) msg.getObject();
        receivedMessagesCount++;
        logReceived(msgContainer.toString(), queueID);

        return msgContainer;
    }

    @Override
    public MessageContainer nonBlockingReceive(String queueID) throws JMSException {
        logReceive(queueID, false);
        ObjectMessage msg = (ObjectMessage) receivers.get(queueID).receiveNoWait();

        if (msg == null) {
            return null;

        } else {
            MessageContainer msgContainer = (MessageContainer) msg.getObject();
            receivedMessagesCount++;

            logReceived(msgContainer.toString(), queueID);
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

    @Override
    public int getReceivedMessagesCount() {
        return receivedMessagesCount;
    }

    @Override
    public int getSentMessagesCount() {
        return sentMessagesCount;
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

    public void addTopic(String topicIdentifier) {
        topicIdentifiers.add(topicIdentifier);
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

    private void initTopics() throws JMSException {
        AMQTopic topic;
        TopicSubscriber subscriber;
        MessageProducer publisher;

        for (String topicID : topicIdentifiers) {
            topic = new AMQTopic((AMQConnection) connection, topicID);
            subscriber = session.createDurableSubscriber(topic, topicID);
            publisher = session.createProducer(topic);

            senders.put(topicID, publisher);
            receivers.put(topicID, subscriber);
        }
    }

    @Override
    public String toString() {
        if (isMaster) return "Master (" + myID + ")";
        else return "Worker " + myID;
    }

    private void logSend(String what, String to, boolean blocking) {
        StringBuilder sb = new StringBuilder();

        sb.append(System.currentTimeMillis());
        if (blocking) sb.append("|<--| ");
        else sb.append("| < | ");

        sb.append(this + " sending " + what + " to " + to);

        logger.info(sb.toString());
    }

    private void logReceive(String from, boolean blocking) {
        StringBuilder sb = new StringBuilder();

        sb.append(System.currentTimeMillis());
        if (blocking) {
            sb.append("|-->| ");
            sb.append(this);
            sb.append(" wating for incoming messages from ");
            sb.append(from);
        }
        else {
            sb.append("| > | ");
            sb.append(this);
            sb.append(" checking incoming messages from ");
            sb.append(from);
        }

        logger.info(sb.toString());
    }

    private void logReceived(String what, String from) {
        StringBuilder sb = new StringBuilder();

        sb.append(System.currentTimeMillis());
        sb.append("|  >| ");
        sb.append(this);
        sb.append(" received ");
        sb.append(what);
        sb.append(" from ");
        sb.append(from);

        logger.info(sb.toString());
    }
 }
