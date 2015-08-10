package org.dllearner.algorithms.distributed.mpi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import mpi.MPI;
import mpi.MPIException;
import mpi.Request;
import mpi.Status;

import org.dllearner.algorithms.distributed.MessagingAgent;
import org.dllearner.algorithms.distributed.containers.MessageContainer;
import org.dllearner.algorithms.distributed.containers.NodeContainer;
import org.dllearner.algorithms.distributed.containers.RefinementAndScoreContainer;
import org.dllearner.algorithms.distributed.containers.RefinementDataContainer;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMPIAgent extends AbstractCELA implements
        MessagingAgent {

    private Logger logger = LoggerFactory.getLogger(AbstractMPIAgent.class);

    private String[] args;
    private int myMPIRank;
    protected int numMPIProcesses;
    protected int[] workers;
    private int nextWorker;
    private int receivedMessagesCount;
    private int sentMessagesCount;
    private int bufferSize = 10000;
    private boolean termMsgReceived = false;
    private Request nonBlockingReceiveRequest = null;
    private ByteBuffer nonBlockingReceiveBuffer = null;

    // known message types
    private final int nodeMsg = 1;  // --> NodeContainer
    private final int refinementMsg = 2;  // --> RefinementDataContainer
    private final int refinementScoreMsg = 3;  // --> RefinementAndScoreContainer
    private final int terminateMsg = 23;

    public AbstractMPIAgent(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
        super(problem, reasoner);
    }

    public AbstractMPIAgent() {
    }

    @Override
    public void initMessaging() throws MPIException {
        MPI.Init(args);
        myMPIRank = MPI.COMM_WORLD.getRank();
        numMPIProcesses = MPI.COMM_WORLD.getSize();
        int numWorkers = numMPIProcesses - 1;

        workers = new int[numWorkers];
        for (int i=1; i<=numWorkers; i++) {
            workers[i-1] = i;
        }
        nextWorker = 0;

        receivedMessagesCount = 0;
        sentMessagesCount = 0;
    }

    @Override
    public void finalizeMessaging() throws MPIException {
        MPI.Finalize();
    }

    @Override
    public void blockingSend(MessageContainer msgContainer) throws MPIException {
        ByteBuffer sendBuff = MPI.newByteBuffer(bufferSize);
        int buffLength = fillBuffer(sendBuff, msgContainer);

        int target;
        if (isMaster()) target = getNextWorker();
        else target = 0;  // workers always send to the master

        int msgType = getMsgType(msgContainer);

        logSend(msgContainer.toString(), getAgentName(target), true);
        MPI.COMM_WORLD.send(sendBuff, buffLength, MPI.BYTE, target, msgType);
        sentMessagesCount++;
    }

    @Override
    public void nonBlockingSend(MessageContainer msgContainer) throws MPIException {
        ByteBuffer sendBuf = MPI.newByteBuffer(bufferSize);
        int buffLength = fillBuffer(sendBuf, msgContainer);

        int target;
        if (isMaster()) target = getNextWorker();
        else target = 0;  // workers always send to the master

        int msgType = getMsgType(msgContainer);

        logSend(msgContainer.toString(), getAgentName(target), false);
        MPI.COMM_WORLD.iSend(sendBuf, buffLength, MPI.BYTE, target, msgType);
        sentMessagesCount++;
    }

    @Override
    public MessageContainer blockingReceive() throws MPIException {
        ByteBuffer recvBuff = MPI.newByteBuffer(bufferSize);

        int source;
        if (isMaster()) source = MPI.ANY_SOURCE;
        else source = 0;  // workers always receive from master

        logReceive(true);
        Status status = MPI.COMM_WORLD.recv(recvBuff, bufferSize, MPI.BYTE,
                source, MPI.ANY_TAG);

        if (status.getTag() == terminateMsg) {
            termMsgReceived = true;
            return null;
        }

        MessageContainer msgContainer = readFromBuffer(recvBuff, status);

        if (msgContainer != null) {
            logReceived(msgContainer.toString(), getAgentName(status.getSource()));
            receivedMessagesCount++;
        }

        return msgContainer;
    }

    @Override
    public MessageContainer nonBlockingReceive() throws MPIException {
        MessageContainer msgContainer = null;

        if (nonBlockingReceiveRequest == null) {
            /* The last non-blocking receive trial finished. So an new one is
             * made. */

            nonBlockingReceiveBuffer = MPI.newByteBuffer(bufferSize);

            int source;
            if (isMaster()) source = MPI.ANY_SOURCE;
            else source = 0;  // workers always receive from master

            logReceive(false);
            nonBlockingReceiveRequest = MPI.COMM_WORLD.iRecv(
                    nonBlockingReceiveBuffer, bufferSize, MPI.BYTE, source,
                    MPI.ANY_TAG);

            if (nonBlockingReceiveRequest.testStatus() != null) {
                /* There is something arriving... Waiting until receiving
                 * finished */

                Status status = nonBlockingReceiveRequest.waitStatus();

                msgContainer = readFromBuffer(nonBlockingReceiveBuffer, status);
                logReceived(msgContainer.toString(), getAgentName(status.getSource()));
                receivedMessagesCount++;

                nonBlockingReceiveRequest = null;  // reset request
            }

            return msgContainer;

        } else {
            /* There is an 'open' receive request that did not finish and is
             * thus checked again */

            if (nonBlockingReceiveRequest.testStatus() != null) {
                /* There is something arriving... Waiting until receiving
                 * finished */

                Status status = nonBlockingReceiveRequest.waitStatus();

                msgContainer = readFromBuffer(nonBlockingReceiveBuffer, status);
                logReceived(msgContainer.toString(), getAgentName(status.getSource()));
                receivedMessagesCount++;

                nonBlockingReceiveRequest = null;  // reset request
            }

            return msgContainer;
        }
    }

    @Override
    public void terminateAgents() throws MPIException {
        for (int worker : workers) {
            MPI.COMM_WORLD.send(MPI.newByteBuffer(0), 0, MPI.BYTE, worker, terminateMsg);
        }
    }

    @Override
    public boolean checkTerminateMsg() {
        return termMsgReceived;
    }

    @Override
    public void setAgentID(int id) {
        System.err.println("Cannot set agent ID! ID is set by MPI system.");
    }

    @Override
    public int getAgentID() {
        return myMPIRank;
    }

    @Override
    public boolean isMaster() {
        return myMPIRank == 0;
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
    private int getMsgType(MessageContainer msgContainer) {
        int msgType = 0;
        if (msgContainer instanceof NodeContainer) msgType = nodeMsg;
        else if (msgContainer instanceof RefinementDataContainer)
            msgType = refinementMsg;
        else if (msgContainer instanceof RefinementAndScoreContainer)
            msgType = refinementScoreMsg;

        return msgType;
    }

    /**
     * @param buff the buffer to fill
     * @param msgContainer the message container that should be written to the
     *      buffer
     * @return the size of the buffer which corresponds to the size of the
     *      serialized message container
     */
    private int fillBuffer(ByteBuffer buff, MessageContainer msgContainer) {
        int buffArrayLength = bufferSize;
        byte[] buffArray;

        // object serialization
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(msgContainer);
            buffArray = bos.toByteArray();
            buffArrayLength = buffArray.length;
            buff.put(buffArray);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);  // TODO: think of sth more clever to do here...
        }

        return buffArrayLength;
    }

    private MessageContainer readFromBuffer(ByteBuffer buff, Status status) throws MPIException {
        MessageContainer msgContainer = null;
        int byteCount = status.getCount(MPI.BYTE);
        byte[] msgContainerArray = new byte[bufferSize];

        // FIXME
        // for some reason refinementScoreRecvBuf.get(refinementScoreRecvArray)
        // does not work so I have top do this manually...
        int cntr = 0;
        while (cntr < bufferSize) {
            msgContainerArray[cntr] = buff.get(cntr);
            cntr++;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(msgContainerArray);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object obj = in.readObject();
            msgContainer = (MessageContainer) obj;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return msgContainer;
    }

    private String getAgentName(int agentId) {
        if (agentId == 0) return "Master";
        else return "Worker " + agentId;
    }

    @Override
    public String toString() {
        return getAgentName(myMPIRank);
    }

    private void logSend(String what, String target, boolean blocking) {
        StringBuilder sb = new StringBuilder();

        if (blocking) sb.append("|-->| ");
        else sb.append("|>--| ");

        sb.append(this);
        sb.append(" sending ");
        sb.append(what);
        sb.append(" to ");
        sb.append(target);

        logger.info(sb.toString());
    }

    private void logReceive(boolean blocking) {
        StringBuilder sb = new StringBuilder();

        if (blocking) {
            sb.append("|<..| ");
            sb.append(this);
            sb.append(" wating for incoming messages");
        }
        else {
            sb.append("|--<| ");
            sb.append(this);
            sb.append(" checking incoming messages");
        }

//        logger.info(sb.toString());
    }

    private void logReceived(String what, String from) {
        StringBuilder sb = new StringBuilder();

        sb.append("|<--| ");
        sb.append(this);
        sb.append(" received ");
        sb.append(what);
        sb.append(" from ");
        sb.append(from);

        logger.info(sb.toString());
    }

    /**
     * Returns to next worker agent to send a message container to.
     *
     * Currently implemented strategy: Round Robin
     * @return the rank number of the next worker agent to send a message
     * container to
     */
    private int getNextWorker() {
        nextWorker = (nextWorker + 1) % numMPIProcesses;

        // skip the master
        if (nextWorker == 0) nextWorker++;

        return nextWorker;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }
}
