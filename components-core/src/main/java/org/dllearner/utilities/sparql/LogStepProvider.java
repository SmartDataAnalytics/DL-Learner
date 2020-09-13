package org.dllearner.utilities.sparql;

import java.io.OutputStream;

public interface LogStepProvider {
    OutputStream getLogStream();

    String getStepUri();
}
