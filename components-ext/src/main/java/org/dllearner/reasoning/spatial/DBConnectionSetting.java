package org.dllearner.reasoning.spatial;

/**
 * TODO (PW): Check if such a class makes sense.
 *
 * This class should serve as an abstraction from an actual DB connection. So a
 * user just has to know the credentials/connection settings and not which
 * JDBC implementation to use etc. Furthermore this class should ideally be
 * agnostic to the actual DB in use.
 */
public class DBConnectionSetting {
    String host;
    int port;
    String dbName;
    String user;
    String password;

    public DBConnectionSetting(
            String host, int port, String dbName, String user, String password) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.user = user;
        this.password = password;
    }
}
