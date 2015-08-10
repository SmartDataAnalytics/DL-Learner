package org.dllearner.algorithms.distributed.amqp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.url.URLSyntaxException;
import org.dllearner.algorithms.distributed.MessagingAgent;
import org.dllearner.algorithms.distributed.containers.MessageContainer;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAMQPAgent extends AbstractCELA implements MessagingAgent{
    private Logger logger = LoggerFactory.getLogger(AbstractAMQPAgent.class);

    // default AMPQ values
    private String user = "admin";
    private String password = "admin";
    private String host = "localhost";
    private String clientId = null;
    private String virtualHost = null;
    private int port = 5672;
    private final String masterDefaultSendQueueIdentifier = "request queue";
    private final String masterDefaultReceiveQueueIdentifier = "response queue";
    private final String workerDefaultSendQueueIdentifier = "response queue";
    private final String workerDefaultReceiveQueueIdentifier = "request queue";

    // misc
    protected int myID;
    private int receivedMessagesCount;
    private int sentMessagesCount;

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
    private long maxBlockingWaitMilisecs = 5000;


    public AbstractAMQPAgent(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
        super(problem, reasoner);
    }

    public AbstractAMQPAgent() {
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

        if (isMaster) initMasterQueues();
        else initWorkerQueues();

        termTopic = new AMQTopic((AMQConnection) connection, termRoutingKey);
        termMsgSubscriber = session.createDurableSubscriber(termTopic, Integer.toString(myID));
        termMsgPublisher = session.createProducer(termTopic);

        sentMessagesCount = 0;
        receivedMessagesCount = 0;
    }

    @Override
    public void finalizeMessaging() throws JMSException {
        session.close();
        connection.close();
    }

    @Override
    public void blockingSend(MessageContainer msgContainer) throws JMSException {
        logSend(msgContainer.toString(), true);
        Message msg = ((org.apache.qpid.jms.Session) session).createObjectMessage(msgContainer);
        sender.send(msg);

        sentMessagesCount++;
    }

    @Override
    public void nonBlockingSend(MessageContainer msgContainer) throws JMSException {
        logSend(msgContainer.toString(), false);
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
        sender.send(msg);

        sentMessagesCount++;
    }

    @Override
    public MessageContainer blockingReceive() throws JMSException {
        logReceive(true);
        // FIXME: find a better solution than using a timeout
        ObjectMessage msg = (ObjectMessage) receiver.receive(maxBlockingWaitMilisecs );

        if (msg == null) return null;

        MessageContainer msgContainer = (MessageContainer) msg.getObject();
        receivedMessagesCount++;

        return msgContainer;
    }

    @Override
    public MessageContainer nonBlockingReceive() throws JMSException {
        logReceive(false);
        ObjectMessage msg = (ObjectMessage) receiver.receiveNoWait();

        if (msg == null) {
            return null;

        } else {
            MessageContainer msgContainer = (MessageContainer) msg.getObject();
            receivedMessagesCount++;

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

    @Override
    public String toString() {
        if (isMaster) return "Master (" + myID + ")";
        else return "Worker " + myID;
    }

    private void logSend(String what, boolean blocking) {
        StringBuilder sb = new StringBuilder();

        if (blocking) sb.append("|-->| ");
        else sb.append("|>--| ");

        sb.append(this + " sending " + what);

        logger.info(sb.toString());
    }

    private void logReceive(boolean blocking) {
        StringBuilder sb = new StringBuilder();

        if (blocking) {
            sb.append("|<--| ");
            sb.append(this);
            sb.append(" wating for incoming messages");
        }
        else {
            sb.append("|--<| ");
            sb.append(this);
            sb.append(" checking incoming messages");
        }

        logger.info(sb.toString());
    }
 }
