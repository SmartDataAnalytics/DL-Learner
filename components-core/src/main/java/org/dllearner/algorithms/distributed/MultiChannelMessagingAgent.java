package org.dllearner.algorithms.distributed;

import org.dllearner.algorithms.distributed.containers.MessageContainer;



public interface MultiChannelMessagingAgent {

    public void initMessaging() throws Exception;

    public void finalizeMessaging() throws Exception;

    public void blockingSend(MessageContainer msgContainer, String channelID) throws Exception;

    public void nonBlockingSend(MessageContainer msgContainer, String channelID) throws Exception;

    public MessageContainer blockingReceive(String channelID) throws Exception;

    public MessageContainer nonBlockingReceive(String channelID) throws Exception;

    public void terminateAgents() throws Exception;

    public boolean checkTerminateMsg() throws Exception;

    public void setAgentID(int id);

    public int getAgentID();

    public boolean isMaster();

    public int getReceivedMessagesCount();

    public int getSentMessagesCount();
}
