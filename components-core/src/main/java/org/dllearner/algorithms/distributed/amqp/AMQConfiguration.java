package org.dllearner.algorithms.distributed.amqp;

public class AMQConfiguration {
    private String user;
    private String password;
    private String clientId;
    private String virtualHost;
    private String host;
    private int port;
    // amqp://guest:guest@test/?brokerlist='tcp://localhost:5672'
    /* expected format:
     * ConnectionURL.AMQ_PROTOCOL + "://" + username + ":" + password + "@" +
     *      ((clientName == null) ? "" : clientName) + virtualHost + "?brokerlist='tcp://" + host + ":" + port + "'"));
     */
    private String strTemplate = "amqp://%s:%s@%s%s?brokerlist='tcp://%s:%d'";

    AMQConfiguration(String user, String password, String clientId, String virtualHost, String host, int port) {
        this.user = user;
        this.password = password;
        this.clientId = (clientId == null) ? "" : clientId;
        this.virtualHost = (virtualHost == null) ? "/" : virtualHost;
        this.host = host;
        this.port = port;
    }

    public String getURL() {
        return String.format(strTemplate, user, password, clientId, virtualHost, host, port);
    }
}